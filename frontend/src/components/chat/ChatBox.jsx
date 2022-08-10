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

const ChatManagementContext = createContext();

export default function ChatBox() {
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
