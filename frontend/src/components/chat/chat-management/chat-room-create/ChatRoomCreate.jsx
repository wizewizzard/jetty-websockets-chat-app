import React, { useContext, useState } from 'react'
import { ChatRoomContext } from '../../../../context/ChatRoomContext';

export default function ChatRoomCreate() {
  const {connectChatRoom, addChatRoom} = useContext(ChatRoomContext);
  return (
    <form>
      <div>
        <input type='text' placeholder='Chat room name' />
      </div>
      <div>
        <button>Create</button>
      </div>
    </form>
  )
}
