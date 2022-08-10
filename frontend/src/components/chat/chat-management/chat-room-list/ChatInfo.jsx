import React, {useContext, useEffect, useState} from 'react'

import { ChatRoomSelectionContext } from '../../../../context/ChatRoomSelectionContext'
import { ChatRoomContext, chatStatus } from '../../../../context/ChatRoomContext'

import styles from '../ChatManagement.module.css'

export default function ChatInfo({chatRoom}) {
    const [status, setStatus] = useState(null);
    const {selectedRoom, setSelectedRoom} = useContext(ChatRoomSelectionContext);
    const {chatRooms,
        connectChatRoom, 
        leaveChatRoom} = useContext(ChatRoomContext);

    useEffect(() => {   
        if(status === null || status == chatStatus.Unknown){
            if(chatRooms.some(e => e.id === chatRoom.id))
            setStatus(chatStatus.Connected);
        else
            setStatus(chatStatus.Disconnected);
        }   
    })
    
    const handleSelection = (event) => {
        if(status === chatStatus.Connected)
            setSelectedRoom(chatRoom)
    };

    const handleConnect = (event) => {
        const prevStatus = status;
        setStatus(chatStatus.Connecting);

        connectChatRoom({id: chatRoom.id})
            .then(res => {
                setStatus(chatStatus.Connected);
            })
            .catch(err => {
                setStatus(prevStatus);
            })
            
    };

    const handleLeave = (event) => {
        const prevStatus = status;
        setStatus(chatStatus.Leaving);
        leaveChatRoom(chatRoom)
            .then(res => {
                if(selectedRoom && selectedRoom.id === chatRoom.id){
                    setSelectedRoom(null);
                } 
                setStatus(chatStatus.Disconnected);
            })
            .catch(err => {
                setStatus(prevStatus);
            });
    };

  return (
    <article key = {chatRoom.id} className={[styles["chat-info"], selectedRoom && selectedRoom.id === chatRoom.id ? styles["selected"] : null].join(' ')}>
        <div className={styles["chat-room-name"]} onClick={handleSelection}>
        {chatRoom.chatName}
        </div>
        <div className={styles["chat-actions"]}>
        { (() => {
            switch(status){
                case chatStatus.Connected: 
                    return (<><a href='#' onClick={handleLeave}>Leave</a></>);
                case chatStatus.Disconnected: 
                    return (<><a href='#' onClick={handleConnect}>Connect</a></>);
                case chatStatus.Connecting:
                    return (<><span>Connecting...</span></>)
                case chatStatus.Leaving:
                    return (<><span>Leaving...</span></>)
                default:
                    return(<><span>Unknown</span></>)
        }})()}
        </div>
    </article>
  )
}
