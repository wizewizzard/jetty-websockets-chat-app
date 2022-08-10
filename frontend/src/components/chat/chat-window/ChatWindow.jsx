import { useEffect, useCallback, useState, useContext, useRef, useMemo, useReducer } from 'react';
import { ChatRoomSelectionContext } from '../../../context/ChatRoomSelectionContext';
import ChatService from '../../../service/ChatService';
import Loader from '../../static/Loader';
import ChatInput from './ChatInput';
import styles from './ChatWindow.module.css'
import useWebSocket, { ReadyState } from 'react-use-websocket';
import { AuthContext } from '../../../context/AuthContext';
import ConnectionBox from './ConnectionBox';
import { MessagingContext } from '../../../context/MessageStorageContext';

const messagesReducer = (prev, data) => {
    console.log('Prev:', prev);
    console.log('Data:', data);
    switch(data.action){
        case 'replace':{
            const list = [...data.messages];
            return list;
        }
        case 'append':
            {
                const list = [...prev];
                return list.concat(data.messages);
            }
            
        case 'insertFirst':
            {
                const list = [...data.messages];
                return list.concat(prev);
            }  
    }

}

export default function ChatWindow(){
    const [loaded, setLoaded] = useState(false);
    const [messages, setMessages] = useReducer(messagesReducer, []);
    const {selectedRoom} = useContext(ChatRoomSelectionContext);
    const {token, userName} = useContext(AuthContext);
    const {getMessages, addMessage} = useContext(MessagingContext);
    const [canBeQueriedMore, setCanBeQueriedMore] = useState(true);
    const [depth, setDepth] = useState(20)

    const queryParams = {
        token: token
      };
    
    const [socketUrl, setSocketUrl] = useState(() => {
        if (window.location.protocol === "https:") {
            return `wss://${window.location.hostname}/wssocket/chat`
        }
        else{
            console.log('Creating up ws websocket')
            return `ws://${window.location.hostname}:8080/wssocket/chat`
        }
    });

    //TODO: close connection when unmounts
    const { sendMessage, lastMessage, readyState } = useWebSocket(socketUrl, {queryParams});

    useEffect(() => {
        setLoaded(false);
        if(selectedRoom){
            console.log('Room is ', selectedRoom);
            getMessages({id: selectedRoom.id, untilDateExcluded: Math.floor(new Date().getTime() / 1000), depth: depth})
            .then(messages => {
                setMessages({action: 'replace', messages});
                setLoaded(true);
                setCanBeQueriedMore(messages.length >= depth);
            })
        }
        else{
            setLoaded(true);
        }
     
    }, [selectedRoom])

    const loadMore = useCallback(() => {
        const last = messages.slice(-1)[0];
        let untilDateExcluded = new Date().toISOString;
        if(last){
            untilDateExcluded = last.publishedAt
        }
        getMessages({id: selectedRoom.id, untilDateExcluded: Math.floor(Date.parse(untilDateExcluded + 'Z') / 1000), depth: 20})
            .then(messages => {
                if(messages && messages.length > 0){
                    setMessages({action: 'append', messages});
                }
                else{
                    setCanBeQueriedMore(false);
                }
            })
            .catch(err => {
                setCanBeQueriedMore(false);
            });
    },
    [messages])

    useEffect(() => {
        if(lastMessage != null){
            const msg = JSON.parse(lastMessage.data);
            console.log(msg)
            addMessage(msg);
            if(selectedRoom != null && selectedRoom.id === msg.chatId) 
                setMessages({action: 'insertFirst', messages: [msg]})
        }
    }, [lastMessage])
    return (
        <>
        {
            (() => {
                switch (readyState){
                    case ReadyState.CONNECTING: 
                        return <Loader visible={!loaded} message = {'Connecting'}/>
                    case ReadyState.OPEN: 
                        return (
                            selectedRoom ? 
                            <>
                                <section className={styles.chatbox}>
                                    <section className={styles["chat-window"]}>
                                    {
                                        messages.map((m, i) => {
                                            return (
                                                <article key={i} className={[styles["msg-container"], m.createdBy !== userName ? styles['msg-remote'] : styles['msg-self']].join(' ')} id="msg-0">
                                                    <div className={styles['msg-box']}>
                                                        <img className={styles['user-img']} id="user-0" src="https://placehold.co/50" />
                                                        <div className={styles['flr']}>
                                                            <p className={styles.msg} id="msg-0">
                                                                {m.body}
                                                            </p>
                                                            <span className={styles["timestamp"]}>
                                                            <span className={styles["username"]}>{m.createdBy}</span>&bull;<span className={styles["posttime"]}>{
                                                                new Date(Date.parse(m.publishedAt + 'Z') -new Date().getTimezoneOffset()*60000).toISOString() 
                                                                }</span></span>
                                                        </div>
                                                    </div>
                                                </article>
                                            );
                                        })
                                    }
                                    {canBeQueriedMore ? 
                                    <div className={styles['message-history-requester']} onClick={loadMore}>Load more</div>
                                    :
                                    <div className={styles['message-history-requester']}>This is the beggining of the chat</div>}
                                    
                                    </section>
                                    <ChatInput sendMessage = {sendMessage} />
                                </section>
                            </>
                            :
                            <>
                                <ConnectionBox header={'Select a chat room'} message={'Select any chat to start communication'}/>
                            </>
                        );
                    case ReadyState.CLOSING: 
                        return <Loader visible={!loaded} message = {'Closing'} />;
                    case ReadyState.CLOSED: 
                        return (<ConnectionBox header={'Connection closed'} message={'May be something is wrong with a server'}/>);
                    case ReadyState.UNINSTANTIATED: 
                        return (<ConnectionBox header={'Connection closed'} message={'May be something is wrong with a server'}/>);
                }
            })()        
    }
    </>
    );
}