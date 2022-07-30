import React, {useEffect, useState} from 'react'
import Loader from '../../../static/Loader';
import ChatList from '../chat-room-list/ChatList'

export default function ChatRoomSearch() {
  const [loaded, setLoaded] = useState(false);
  const [chatRooms, setChatRooms] = useState(null)

  useEffect(() => {
    setLoaded(true)    ;
  }, [chatRooms]);

  const handleSearch = (event) => {
    event.preventDefault();
    if(!loaded)
      return;
    setLoaded(false);

    console.log('Searching')

    setTimeout(() => {
      console.log('Found')
      setChatRooms([]);
    }, 1000);
  }

  return (
    <>
      <form onSubmit={handleSearch}>
        <div>
          <input type='text' placeholder='Full chat name or part of it' />
        </div>
        <div>
          <button>Find</button>
        </div>
      </form>
      <>
      {!loaded ? 
        <Loader visible={!loaded} message = {'Loading chat rooms...'}/>
        :
        <>
          <h6>Сhat rooms found</h6>
          <ChatList />
        </>
      }
        
      </>
    </>
  )
}
