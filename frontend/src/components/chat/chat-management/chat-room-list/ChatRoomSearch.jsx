import React, {useEffect, useState} from 'react'

import ChatService from '../../../../service/ChatService';
import Loader from '../../../static/Loader';
import ChatInfo from '../chat-room-list/ChatInfo';

import styles from '../ChatManagement.module.css'

export default function ChatRoomSearch() {
  const [loaded, setLoaded] = useState(false);
  const [chatRooms, setChatRooms] = useState(null)
  const [chatName, setChatName] = useState('');
  const [queried, setQueried] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoaded(true)    ;
  }, [chatRooms]);

  const handleSearch = (event) => {
    event.preventDefault();
    if(!loaded)
      return;
    setQueried(true);
    setLoaded(false);
    ChatService
      .findChatRooms({chatName})
      .then(resp => {
        if(resp.status === 200){
          resp.json().then(data => {
            setError(null);
            setLoaded(true);
            setChatRooms(data)
            
          })
        }
        else{
          resp.json().then(data => {
            setLoaded(true);
            setError(data.message);
          })
        }
      })
      .catch(err => {
        setLoaded(true);
        setError("Request was not successful");
      })
  }
  return (
    <>
      <div className={styles["listing-box"]}>
        <form onSubmit={handleSearch}>
          <div className={styles['single-line-form']}>
            <input type='text' className='input-mini' placeholder='Chat name or part of it' onChange={e => setChatName(e.target.value)} />
            <button className='button-mini'>Find</button>
          </div>
        </form>
        <>
        { !queried ?
          <>
          </>
          :
          !loaded ? 
          <Loader visible={!loaded} message = {'Loading chat rooms...'}/>
          :
          error !== null ? 
          <div className={[styles['request-result'], styles['bad']].join(' ')}>{error}</div>
          :
          chatRooms && chatRooms.length > 0 ? 
          <>
            <h4>Сhat rooms found</h4>
            <section className={styles["chat-list"]}>
            {chatRooms.map((c, i) => {
              return(
                <ChatInfo key = {i} chatRoom={c} />
              )
            })}
            </section>
          </>
          :
          <div className={[styles['request-result'], styles['good']].join(' ')}>Nothing was found</div>
        }
          
        </>
      </div>
    </>
  )
}
