import React from 'react'
import styles from './ChatWindow.module.css'

export default function ChatInput() {
  return (
        <form className={styles["chat-input"]} onSubmit={null}>
            <input type="text" placeholder="Type a message" />
            <button>
              <img src='send-svgrepo-com-2.svg' alt='Send message' /> 
            </button>
        </form>
  )
}
