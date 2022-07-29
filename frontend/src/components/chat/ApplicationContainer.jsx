import React, {useState, useEffect} from 'react'
import ChatList from './chat-management/chat-room-list/ChatList';
import ChatWindow from '../chat-window/ChatWindow';
import ProfileInfo from '../profile-info/ProfileInfo';
import Loader from '../static/Loader';
import ToStart from '../static/ToStart';
import styles from './ApplicationContainer.module.css'
import Tabs from '../util/Tabs';
import ChatRoomCreate from './chat-management/chat-room-create/ChatRoomCreate';
import ChatRoomSearch from './chat-management/chat-room-search/ChatRoomSearch';

export const LoginContext = React.createContext();

export default function ApplicationContainer() {
    const [loading, setloading] = useState(true); 
    const [loggedIn, setloggedIn] = useState(true);

    useEffect(() => {
    console.log("Verifying the token");
    const token = localStorage.getItem("bearer-token");
    if(token){
        console.log("Calling server to verify token");
    }
    else{
        setloading(false);
        setloggedIn(true);
    }

    }, []);

  return (
    <>
    <Loader visible={loading} message = {'Verifying your token'}/>
    <LoginContext.Provider value={loggedIn}>
        {loggedIn ? <>
        <div className={styles['double-column-container']}>
            <div className={styles['info-column']}>
                <h6>Your are logged in as</h6>
                
                <div className={styles['profile-box']}>
                    <ProfileInfo />
                </div>
                <div className={styles['chat-management-box']}>
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
                
            </div>
            <div className={styles['chat-column']}>
                <ChatWindow />
            </div>
        </div>
        </> :
            <ToStart />
        }
    </LoginContext.Provider>
    </>
  )
}
