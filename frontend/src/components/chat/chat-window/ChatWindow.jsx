import { useEffect, useState, useContext, useRef } from 'react';
import { ChatRoomSelectionContext } from '../../../context/ChatRoomSelectionContext';
import ChatService from '../../../service/ChatService';
import Loader from '../../static/Loader';
import ChatInput from './ChatInput';
import styles from './ChatWindow.module.css'



export default function ChatWindow(){
    const [loaded, setLoaded] = useState(false);
    const [messages, setMessages] = useState([]);
    const {selectedRoom} = useContext(ChatRoomSelectionContext);

    useEffect(() => {
        setLoaded(false);
        if(selectedRoom){
            console.log('Room is ', selectedRoom);
            ChatService.getChatHistory({})
                .then(resp => {
                    if(resp.status === 200){
                        console.log('RESP MADE')
                        resp.json().then(data => {
                            setMessages(data);
                            setLoaded(true);
                        });                   
                    }
                    else{
                        //TODO: process error
                        setLoaded(true);
                    }
                });
        }
        else{
            setLoaded(true);
        }
     
    }, [selectedRoom])
    
    return (
        <>
        {!loaded ? 
        <Loader visible={!loaded} message = {'Loading chat'}/>
        :
        selectedRoom ? 
        <>
            <section className={styles.chatbox}>
                <section className={styles["chat-window"]}>
                {
                    messages.map((m, i) => {
                        return (
                            <article key={i} className={[styles["msg-container"], m.createdBy === 'Bob' ? styles['msg-remote'] : styles['msg-self']].join(' ')} id="msg-0">
                                <div className={styles['msg-box']}>
                                    <img className={styles['user-img']} id="user-0" src="https://via.placeholder.com/50" />
                                    <div className={styles['flr']}>
                                        <p className={styles.msg} id="msg-0">
                                            {m.body}
                                        </p>
                                        <span className={styles["timestamp"]}><span className={styles["username"]}>{m.createdBy}</span>&bull;<span className={styles["posttime"]}>{m.publishedAt}</span></span>
                                    </div>
                                </div>
                            </article>
                        );
                    })
                }
                </section>
                <ChatInput />
            </section>
        </>
        :
        <>
            <div className={styles['to-start']}>
                <h2>Connect to a chat room</h2>
                <p>
                    Select any chat you are connected in oder to start communication
                </p>
            </div>
        </>
    }
    </>);
}