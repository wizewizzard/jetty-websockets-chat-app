import React, {useState, useEffect, useContext} from 'react'
import Loader from '../../../static/Loader';
import ChatInfo from './ChatInfo';
import { ChatRoomContext, chatStatus } from '../../../../context/ChatRoomContext';
import ChatService from '../../../../service/ChatService';
import styles from '../ChatManagement.module.css'
import { AuthContext } from '../../../../context/AuthContext';

export default function ChatList() {
  const [loaded, setLoaded] = useState(false)
  const [chatRooms, setChatRooms] = useState([])
  const {userId} = useContext(AuthContext);
  const [error, setError] = useState(null);
  const {
    chatRooms: chatRoomList,
    synchChatRooms
  } = useContext(ChatRoomContext);

  useEffect(() => {
    setLoaded(false);
      ChatService
      .getUsersChatRooms({userId})
      .then(resp => {
        if(resp.status === 200){
            resp.json().then(data => {
              synchChatRooms(data.map(e => {return {id: e.id, chatName: e.chatName}}));
              setChatRooms(chatRoomList);
              setLoaded(true);
            });
        }
        else{
          resp.json().then(data => {
            setError({message: data.message});
          }).
          catch(err => {
            setError({message: resp.statusText});
          });
          setLoaded(true);
        }
    })
    .catch(err => {
          setError({message: 'Error when making request'});
          setLoaded(true);
    });
    }, []);

    useEffect(() => {
      console.log('chatRoomList: ', chatRoomList)
      setChatRooms(chatRoomList);
    }, [chatRoomList]);
    
  

  return (
    <>
      <div className={styles["listing-box"]}>
      {!loaded ? 
          <Loader visible={!loaded} message = {'Loading chat rooms'}/>
          :
          error ? <div className='error-message'>{error.message}</div>
          :
          <section className={styles["chat-list"]}>
            {chatRooms.map((c, i) => {
              return(
                <ChatInfo key = {c.id} chatRoom={c} />
              )
            })}
          </section>
        }
      </div>
    </>
  )
}
