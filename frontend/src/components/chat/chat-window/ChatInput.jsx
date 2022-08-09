import React, { useContext, useEffect, useReducer, useRef, useState } from 'react'
import { ChatRoomSelectionContext } from '../../../context/ChatRoomSelectionContext';
import styles from './ChatWindow.module.css'

export default function ChatInput({sendMessage}) {
  const {selectedRoom} = useContext(ChatRoomSelectionContext);
  const [messageText, setMessageText] = useState('');
  const [validity, setValidity] = useState(false);
  const ref = useRef(null);

  const handleSend = (e) => {
    e.preventDefault();
    if(messageText)
      sendMessage(JSON.stringify({chatId: selectedRoom.id, body: messageText}));
      ref.current.value = '';
      setMessageText('');
  }

  useEffect(() => {
    if(messageText.length > 0)
      setValidity(true);
    else
      setValidity(false);
  }, [messageText])
  

  return (
        <form className={styles["chat-input"]} onSubmit={handleSend}>
            <input type="text" ref={ref} placeholder="Type a message" onChange={(e) => setMessageText(e.target.value)}/>
            <button className={validity ? styles['good'] : ''} disabled={!validity}>
              <img src='send.svg' alt='Send message' /> 
            </button>
        </form>
  )
}
