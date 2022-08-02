import styles from './ChatList.module.css'

import React, {useContext, useEffect, useState} from 'react'

import { WsConnectionContext } from '../../../../context/WsConnectionContext'
import { ChatRoomSelectionContext } from '../../../../context/ChatRoomSelectionContext'
import { ChatManagementContext, chatRoomStatus } from '../../../../context/WsConnectionContext'

export default function ChatInfo({chatRoom}) {
    const [status, setStatus] = useState(null);
    const {selectedRoom, setSelectedRoom} = useContext(ChatRoomSelectionContext);
    const {openConnection, closeConnection, getChatRoomStatus} = useContext(WsConnectionContext);

    useEffect(() => {    
        setStatus(getChatRoomStatus(chatRoom));
    }, [])
    


    const handleSelection = (event) => {
        if(status === chatRoomStatus.Connected)
            setSelectedRoom(chatRoom)
    };

    const handleConnect = (event) => {
        const prevStatus = status;
        setStatus(chatRoomStatus.Connecting);
        openConnection(chatRoom)
            .then(res => {
                setStatus(chatRoomStatus.Connected);
            })
            .catch(err => {
                console.log(err);
                setStatus(prevStatus);
            })
            
    };
    const handleDisconnect = (event) => {
        const prevStatus = status;
        setStatus(chatRoomStatus.Disconnecting);
        closeConnection(chatRoom)
            .then(res => {
                //setSelected(false);
                if(selectedRoom && selectedRoom.id === chatRoom.id){
                    setSelectedRoom(null);
                } 
                setStatus(chatRoomStatus.Disconnected);
            })
            .catch(err => {
                console.log(err);
                setStatus(prevStatus);
            });
    };

    const handleLeave = (event) => {
        // const prevStatus = status;
        // chatRoomContext
        //     .leaveChatRoom(chatRoom)
        //     .then(res => {

        //     })
        //     .catch(err => {
        //         console.log(err);
        //         setStatus(prevStatus);
        //     });
    };

  return (
    <article key = {chatRoom.id} className={styles["chat-info"]}>
        <div className={[styles["chat-room-name"], selectedRoom && selectedRoom.id === chatRoom.id ? styles["selected"] : null].join(' ')} onClick={handleSelection}>{chatRoom.name}</div>
        <div className={styles["chat-actions"]}>
        { (() => {
            switch(status){
                case chatRoomStatus.Connected: 
                    return (<><a href='#' onClick={handleDisconnect}>Disconnect</a><a href='#' onClick={handleLeave}>Leave</a></>);
                case chatRoomStatus.Disconnected: 
                    return (<><a href='#' onClick={handleConnect}>Connect</a><a href='#' onClick={handleLeave}>Leave</a></>);
                case chatRoomStatus.Connecting:
                    return (<><span>Connecting...</span></>)
                case chatRoomStatus.Disconnecting:
                    return (<><span>Disconnecting...</span></>)
                default:
                    return(<><span>Unknown</span></>)
        }})()}
        </div>
    </article>
  )
}
