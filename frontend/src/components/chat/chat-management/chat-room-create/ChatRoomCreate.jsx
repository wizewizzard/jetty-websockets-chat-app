import React, { useContext, useState } from 'react'
import { ChatRoomContext } from '../../../../context/ChatRoomContext';
import ChatService from '../../../../service/ChatService';
import Loader from '../../../static/Loader';

import styles from '../ChatManagement.module.css'

export default function ChatRoomCreate() {
  const {connectChatRoom, addChatRoom} = useContext(ChatRoomContext);
  const [chatName, setChatName] = useState('');
  const [operationRequested, setOperationRequested] = useState(false);
  const [loaded, setLoaded] = useState(false);
  const [error, setError] = useState(null);


  const handleSubmit = (event) => {
    setOperationRequested(true);
    setError(null);
    setLoaded(false);
    event.preventDefault();
    ChatService.createChatRoom({chatName})
    .then(resp => {
      if(resp.status == 201){
        resp.json().then(data => {
          addChatRoom(data);
        });
        setLoaded(true);
      }
      else{
        resp.json().then(data => {
          setError({message: data.message});
        })
        .catch(err => {
          setError({message: resp.statusText})
        });
        setLoaded(true);
      }
    })
    .catch(err => {
      setError({message: 'Error when making request'});
})
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className={styles['single-line-form']}>
        <input type='text' className='input-mini' placeholder='Chat room name' onChange={e => setChatName(e.target.value)} />
        <button className='button-mini'>Create</button>
      </div>
      {operationRequested?
        !loaded ? 
          <Loader visible={!loaded} message = {'Loading chat rooms...'}/>
        :
        error ? 
          <div className={[styles['request-result'], styles['bad']].join(' ')}>{error.message}</div>
          :
          <div className={[styles['request-result'], styles['good']].join(' ')}>Created</div>
        :
        <></>
      }
    </form>
  )
}
