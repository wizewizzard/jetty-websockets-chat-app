import React, {useState, useEffect, useContext} from 'react'
import Loader from '../../../static/Loader';
import ChatInfo from './ChatInfo';
import { ChatRoomContext, chatStatus } from '../../../../context/ChatRoomContext';
import ChatService from '../../../../service/ChatService';
import styles from './ChatList.module.css'
import { AuthContext } from '../../../../context/AuthContext';

export default function ChatList() {
  const [loaded, setLoaded] = useState(false)
  const [chatRooms, setChatRooms] = useState([])
  const {userId} = useContext(AuthContext);
  const [error, setError] = useState(null);
  const {
    chatRooms: chatRoomList,
    synchChatRooms
  } = useContext(ChatRoomContext);

  useEffect(() => {
    console.log('Rerendered')
  })

  useEffect(() => {
    console.log('Mounted')
  }, []);

  useEffect(() => {
    setLoaded(false);
      ChatService
      .getUsersChatRooms({userId})
      .then(resp => {
        if(resp.status === 200){
            resp.json().then(data => {
              synchChatRooms(data.map(e => {return {id: e.id, chatName: e.chatName}}));
              setChatRooms(chatRoomList);
              setLoaded(true);
            });
        }
        else{
          //TODO: error process
          setLoaded(true);
        }
    })
    useEffect(() => {
      console.log('chatRoomList: ', chatRoomList)
      setChatRooms(chatRoomList);
    }, [chatRoomList])
    
  

  return (
    <>
      <div className={styles["listing-box"]}>
      {!loaded ? 
          <Loader visible={!loaded} message = {'Loading chat rooms'}/>
          :
          <section className={styles["chat-list"]}>
            {chatRooms.map((c, i) => {
              return(
                <ChatInfo key = {c.id} chatRoom={c} />
              )
            })}
          </section>
        
        }
      </div>
    </>
  )
}
