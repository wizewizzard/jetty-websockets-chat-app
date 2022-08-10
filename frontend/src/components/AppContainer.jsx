import React, {useContext, useEffect, useState} from 'react';
import { Link, useNavigate } from 'react-router-dom';
import ChatBox from './chat/ChatBox';
import ProfileInfo from './profile-info/ProfileInfo';
import Loader from './static/Loader';
import ToStart from './static/ToStart';

import styles from './AppContainer.module.css'
import { AuthContext } from '../context/AuthContext';

export default function AppContainer() {
    const [loaded, setLoaded] = useState(false); 
    const navigate = useNavigate();
    const {userName, loggedIn, verifyToken, logOut} = useContext(AuthContext);

    const handleLogout = () => {
        logOut().then(res => {
            navigate('/');
        })
        .catch(err => {
            navigate('/error')
        })
    }

    useEffect(() => {
        verifyToken()
        .then(res => {
            setLoaded(true);
        })
        .catch(err => {
            setLoaded(true);
        })
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
                        <h3>Your are logged in as</h3>
                        <ProfileInfo userName={userName}/>
                        <div className='hint'>
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
