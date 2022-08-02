import React, {useEffect, useState} from 'react';
import { Link } from 'react-router-dom';
import AuthService from '../service/AuthService';
import ChatBox from './chat/ChatBox';
import ProfileInfo from './profile-info/ProfileInfo';
import Loader from './static/Loader';
import ToStart from './static/ToStart';

import styles from './AppContainer.module.css'

export default function AppContainer() {
    const [loaded, setLoaded] = useState(false); 
    const [loggedIn, setLoggedIn] = useState(false);
    const [userName, setUserName] = useState('');

    const handleLogout = () => {
        //TODO: close all connections
        AuthService.logout();
        setLoggedIn(false);
    }

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

    return (
        <>
        {!loaded ? 
            <Loader visible={!loaded} message = {'Verifying your token'}/>
            :
            loggedIn ? 
            <>
                <div className={styles["parent"]}>
                    <div className={styles['profile-info']}>
                        <h6>Your are logged in as</h6>
                        <ProfileInfo userName={userName}/>
                        <div>
                        <Link className={styles["logout"]} to='#' onClick={handleLogout}>Logout</Link>
                        </div>
                    </div>
                    <ChatBox />
                </div>
            </> 
            :
            <ToStart />   
        }
        </>
    )
}
