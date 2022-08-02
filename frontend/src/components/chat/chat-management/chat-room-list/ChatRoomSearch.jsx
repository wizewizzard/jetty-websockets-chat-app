import React, {useEffect, useState} from 'react'
import ChatService from '../../../../service/ChatService';
import Loader from '../../../static/Loader';
import ChatInfo from '../chat-room-list/ChatInfo';
import ChatList from '../chat-room-list/ChatList'

import styles from './ChatList.module.css'

export default function ChatRoomSearch() {
  const [loaded, setLoaded] = useState(false);
  const [chatRooms, setChatRooms] = useState(null)
  const [criteria, setCriteria] = useState();

  useEffect(() => {
    setLoaded(true)    ;
  }, [chatRooms]);

  const handleSearch = (event) => {
    event.preventDefault();
    if(!loaded)
      return;
    setLoaded(false);
    ChatService
      .findChatRooms({criteria})
      .then(resp => {
        if(resp.status === 200){
          resp.json().then(data => {
            setChatRooms(data)
            setLoaded(true);
          })
        }
      })
  }

  return (
    <>
      <div className={styles["listing-box"]}>
        <form onSubmit={handleSearch}>
          <div>
            <input type='text' placeholder='Full chat name or part of it' onChange={e => setCriteria(e.target.value)} />
          </div>
          <div>
            <button>Find</button>
          </div>
        </form>
        <>
        {!loaded ? 
          <Loader visible={!loaded} message = {'Loading chat rooms...'}/>
          :
          chatRooms ? 
          <>
            <h6>Сhat rooms found</h6>
            <section className={styles["chat-list"]}>
            {chatRooms.map((c, i) => {
              return(
                <ChatInfo key = {i} chatRoom={c} />
              )
            })}
            </section>
          </>
          :
          <>
            Press search to find
          </>
          
        }
          
        </>
      </div>
    </>
  )
}
