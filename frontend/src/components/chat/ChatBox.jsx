import React, { createContext, useReducer, useState, useEffect } from 'react'
import ChatRoomCreate from './chat-management/chat-room-create/ChatRoomCreate';
import ChatRoomSearch from './chat-management/chat-room-list/ChatRoomSearch';
import Tabs from '../util/Tabs';

import styles from './ChatBox.module.css'
import ChatList from './chat-management/chat-room-list/ChatList';
import ChatWindow from './chat-window/ChatWindow';
import { ChatRoomSelectionProvider } from '../../context/ChatRoomSelectionContext';
import { ChatRoomListProvider } from '../../context/ChatRoomContext';
import { MessageStorageProvider } from '../../context/MessageStorageContext';

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
                      <div className='flex fl-col'>
                          <h3>Your chat rooms</h3>
                          <div className='fl-gr-1'>
                          <ChatList />
                          </div>
                      </div>
                  },
                  {
                      name: 'Create room',
                      content: 
                      <div className='flex fl-col'>
                          <h3>Create new chat room</h3>
                          <ChatRoomCreate />
                      </div>
                  },
                  {
                      name: 'Find room',
                      content:
                      <div className='flex fl-col'>
                          <h3>Search for chat room</h3>
                          <ChatRoomSearch />
                      </div>
                  }
              ]
          } />
        </div>
        <MessageStorageProvider>
          <div className={styles['chat-container']}>
            <ChatWindow />
          </div>
        </MessageStorageProvider>
      </ChatRoomListProvider>
      </ChatRoomSelectionProvider>
    </>
    
  )
}
