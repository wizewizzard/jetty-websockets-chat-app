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
import AuthService from '../../service/AuthService';
import { Link } from 'react-router-dom';


export const LoginContext = React.createContext();

export default function ApplicationContainer() {
    const [loaded, setLoaded] = useState(false); 
    const [loggedIn, setLoggedIn] = useState(false);
    const [userName, setUserName] = useState('');

    useEffect(() => {
    console.log("Verifying the token");
    AuthService
        .verify()
        .then(resp => {
            if(resp.status === 200){
                resp.json().then(data => {
                    console.log('Verified')
                    setLoggedIn(true);
                    setUserName(data.userName);
                });
            }
            else{
                console.log('Not verified')
                AuthService.logout();
                setLoggedIn(false);
            }
            setLoaded(true);
        })
        .catch(e => {
            AuthService.logout();
            setLoggedIn(false);
            setLoaded(true);
        });
    }, []);

    console.log('Render')
  return (
    <>
    {!loaded ? 
        <Loader visible={!loaded} message = {'Verifying your token'}/>
        :
        <LoginContext.Provider value={loggedIn}>
        {loggedIn ? <>
        <div className={styles['double-column-container']}>
            <div className={styles['info-column']}>
                <h6>Your are logged in as</h6>
                
                <div className={styles['profile-box']}>
                    <ProfileInfo userName={userName}/>
                    <Link className={styles["logout"]} to='#' onClick={() => {
                        AuthService.logout();
                        setLoggedIn(false);
                        }}>Logout</Link>
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
    }
    </>
  )
}
