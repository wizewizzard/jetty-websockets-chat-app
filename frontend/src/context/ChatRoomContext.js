import { createContext, useState, useReducer } from "react";
import ChatService from "../service/ChatService";

const ChatRoomContext = createContext();

const chatStatus = { 
    Connected: 'Connected', 
    Connecting: 'Connecting', 
    Disconnecting: 'Disconnecting', 
    Disconnected: 'Disconnected',
    Leaving: 'Leaving',
    Unknown: 'Unknown'
}

const connectionReducer = (prevList, action) => {
    switch (action.type){
      case 'sync': 
        return action.chatRooms;
      case 'add': 
        console.log('Connecting to chat room');
        return [...prevList, action.chatRoom];
      case 'remove':
        console.log('Disconnecting from the chat room');
        return prevList.filter(e => e.id !== action.chatRoom.id);
    }
}

const ChatRoomListProvider = ({children}) => {
    const [chatRooms, setChatRooms] = useReducer(connectionReducer, []);

    const synchChatRooms = (chatRooms) => {
        setChatRooms({type: 'sync', chatRooms});
    }

    const addChatRoom = (chatRoom) => {
        setChatRooms({type: 'add', chatRoom});
    }

    const removeChatRoom = (chatRoom) => {
        setChatRooms({type: 'remove', chatRoom});
    }

    const connectChatRoom = (chatRoom) => {
        console.log(`Connection to chat room ${chatRoom.id}`, );
        return new Promise((resolve, reject) => {
            ChatService
            .connectChatRoom({id: chatRoom.id})
            .then(resp => {
                if(resp.status === 200){
                    setChatRooms({type: 'add', chatRoom});
                    resolve({status: 'ok'});
                }
                else{
                    reject({message: 'Invalid status code'})
                }
            })
            .catch(err => {
                reject({message: 'Request ended up with error'})
            })
        }
        );
    }

    const leaveChatRoom = (chatRoom) => {
        console.log(`Closing connection with the room ${chatRoom}`, );
        return new Promise((resolve, reject) => {
            ChatService
            .leaveChatRoom({id: chatRoom.id})
            .then(resp => {
                if(resp.status === 200){
                    setChatRooms({type: 'remove', chatRoom});
                    resolve({status: 'ok'});
                }
                else{
                    reject({message: 'Invalid status code'})
                }
            })
            .catch(err => {
                reject({message: 'Request ended up with error'})
            })
        }
        );
    }

    const sendMessage = (message) => {
        console.log(`Sending ${message} to the room ${this.chatRoomSelected}` );
    }

    return <ChatRoomContext.Provider value={
        {
            chatRooms,
            addChatRoom,
            removeChatRoom,
            synchChatRooms,
            connectChatRoom, 
            leaveChatRoom, 
            sendMessage
        }
        }>
        {children}
    </ChatRoomContext.Provider>
}

export {ChatRoomListProvider, ChatRoomContext, chatStatus}