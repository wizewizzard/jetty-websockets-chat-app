import React, {useState, useEffect, useContext} from 'react'
import Loader from '../../../static/Loader';
import ChatInfo from './ChatInfo';
import { ChatRoomContext } from '../../../../context/ChatRoomContext';
import ChatService from '../../../../service/ChatService';
import styles from './ChatList.module.css'

export const chatStatus = { 
  Connected: 'Connected', 
  Connecting: 'Connecting', 
  Disconnected: 'Disconnected'
}

export default function ChatList() {
  const [loaded, setLoaded] = useState(false)
  const [chatRooms, setChatRooms] = useState([])

  useEffect(() => {
    console.log('Rerendered')
  })

  useEffect(() => {
    console.log('Mounted')
  }, []);

  useEffect(() => {
    setLoaded(false);
      ChatService
      .getUserChatRooms()
      .then(resp => {
        if(resp.status === 200){
            resp.json().then(data => {
              setChatRooms(data);
              setLoaded(true);
            });
        }
        else{
          //TODO: error process
          setLoaded(true);
        }
    })

    }, [])
  

  return (
    <>
      <div className={styles["listing-box"]}>
      {!loaded ? 
          <Loader visible={!loaded} message = {'Loading chat rooms'}/>
          :
          
          <section className={styles["chat-list"]}>
            {chatRooms.map((c, i) => {
              return(
                <ChatInfo key = {i} chatRoom={c} />
              )
            })}
          </section>
        
        }
      </div>
    </>
  )
}
