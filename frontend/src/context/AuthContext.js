import {useState, createContext } from "react";
import AuthService from "../service/AuthService";

const AuthContext = createContext();

const AuthContextProvider = ({children}) => {
    const [loggedIn, setLoggedIn] = useState(false);
    const [userName, setUserName] = useState('');
    const [userId, setUserId] = useState(null);
    const [token, setToken] = useState('');

    const logOut = () => {
        return new Promise((resolve, reject) => {
            AuthService.logout();
            setToken('');
            setLoggedIn(false);
            resolve();
        })
        
    }

    const logIn = ({userName, password}) => {
        return new Promise((resolve, reject) => {

            AuthService
                .signIn({userName: userName, password: password})
                .then(resp => {
                    if(resp.status === 200){
                        resp.json().then(data => {
                            console.log('Token received from server: ', data.token);
                            setToken(data.token);
                            setUserId(data.id);
                            setUserName(data.userName);
                            AuthService.setToken(data.token);
                            resolve();
                        });
                    }
                    else{
                        resp.json()
                        .then(data => {
                            reject({message: data.message});
                        })
                        .catch(err => {
                            reject({message: resp.statusText});
                        })
                    }
            })
            .catch(error => {
                reject({message: error});
            });
            
        })
        
    }

    const verifyToken = () => {
        return new Promise((resolve, reject) => {
            AuthService
            .verify()
            .then(resp => {
                if(resp.status === 200){
                    resp.json().then(data => {
                        setLoggedIn(true);
                        setToken(AuthService.getToken());
                        setUserName(data.userName);
                        setUserId(data.id);
                        resolve('Verified');
                    });
                }
                else{
                    console.log('Not verified')
                    setLoggedIn(false);
                    reject('Not verified');
                }
            })
            .catch(e => {
                setLoggedIn(false);
                reject('Not verified');
            });
        });
        
    }

    return <AuthContext.Provider value={
        {
            token,
            userName,
            loggedIn,
            userId,
            setToken,
            setUserName,
            setLoggedIn,
            verifyToken,
            logIn,
            logOut
        }
    }>
    {children}
    </AuthContext.Provider>
}

export {AuthContext, AuthContextProvider};