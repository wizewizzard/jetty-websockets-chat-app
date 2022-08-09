import React, { createContext, useReducer, useState, useEffect } from 'react'
import ChatRoomCreate from './chat-management/chat-room-create/ChatRoomCreate';
import ChatRoomSearch from './chat-management/chat-room-list/ChatRoomSearch';
import Tabs from '../util/Tabs';

import styles from './ChatBox.module.css'
import ChatList from './chat-management/chat-room-list/ChatList';
import ChatWindow from './chat-window/ChatWindow';
import { ChatRoomSelectionProvider } from '../../context/ChatRoomSelectionContext';
import { ChatRoomListProvider } from '../../context/ChatRoomContext';

function chatRoomSelectionReducer(prevSelection, newSelection){

}

function chatRoomListReducer(prevList, action){
  switch (action.type){
    case 'connect': 
      console.log('Connecting to chat room');
      break;
    case 'disconnect':
      console.log('Disconnecting from the chat room');
      break;
    case 'leave':
      console.log('Leaving the chat room');
      break;
  }
}

const ChatManagementContext = createContext();

export default function ChatBox() {

  useEffect(() => {
    console.log('ChatBox render');
  })
  

  return (
    <>
    <ChatRoomSelectionProvider>
      <ChatRoomListProvider>
        <div className={styles['chat-management']}>
          <Tabs tabs = {
              [   {
                      name: 'Chat list',
                      content: 
                      <>
                          <h6>Your chat rooms</h6>
                          <ChatList />
                      </>
                  },
                  {
                      name: 'Create room',
                      content: 
                      <>
                          <h6>Create new chat room</h6>
                          <ChatRoomCreate />
                      </>
                  },
                  {
                      name: 'Find room',
                      content:
                      <>
                          <h6>Search for chat room</h6>
                          <ChatRoomSearch />
                      </>
                  }
              ]
          } />
        </div>
          <div className={styles['chat-container']}>
            <ChatWindow />
          </div>
        
      </ChatRoomListProvider>
      </ChatRoomSelectionProvider>
    </>
    
  )
}
