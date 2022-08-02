import { createContext, useReducer } from "react";
const WsConnectionContext = createContext();

const connectionReducer = (prevList, action) => {
    switch (action.type){
      case 'connect': 
        console.log('Connecting to chat room');
        return [...prevList, action.chatRoom];
        break;
      case 'disconnect':
        console.log('Disconnecting from the chat room');
        return prevList.filter(e => e.id !== action.chatRoom.id);
        break;
      case 'leave':
        console.log('Leaving the chat room');
        break;
    }
  }

export const chatRoomStatus = { 
    Connected: 'Connected', 
    Connecting: 'Connecting', 
    Disconnecting: 'Disconnecting', 
    Disconnected: 'Disconnected',
    Leaving: 'Leaving'
}

const WsConnectionProvider =  ({children}) => {
    const [connectionList, setConnectionList] = useReducer(connectionReducer, []);
    
    const openConnection = (chatRoom) => {
        console.log(`Opening connection to the room ${chatRoom}`);
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                if(!connectionList.some(e => e.id === chatRoom.id)){
                    //TODO: really open ws connection
                    // const client = new W3CWebSocket('ws://127.0.0.1:8080/chat');

                    // client.onopen = () => {
                    //     setConnectionList({chatRoom, type :'connect' });
                    //     resolve("Connected");
                    // }

                    // client.onmessage = (message) => {
                    //     console.log(message);
                    // };

                    // client.onclose = () => {
                    //     console.log('Connection closed by server');
                    // }

                    // client.onerror = function() {
                    //     console.log('Connection Error');
                    // };

                    setConnectionList({chatRoom, type :'connect' });
                    resolve("Connected");
                }
                else{
                    reject('Already connected')
                }
            }, 1500)
        })
    }
    
    const closeConnection = (chatRoom) => {
        console.log(`Closing connection with the room ${chatRoom}`, );
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                if(connectionList.some(e => e.id === chatRoom.id)){
                    //TODO: really close ws connection
                    setConnectionList({chatRoom, type :'disconnect' });
                    resolve("Disconnected");
                }
                else{
                    reject('No connection established')
                }
            }, 1500)
        })
    }

    const getConnection = (chatRoom) => {
        connectionList.find(cr => cr.id )
    }

    const getChatRoomStatus = (chatRoom) => {
        if(connectionList.some(e => e.id === chatRoom.id)){
            return chatRoomStatus.Connected;
        }
        return chatRoomStatus.Disconnected;
    }

    return <WsConnectionContext.Provider value={{
        openConnection,
        closeConnection,
        getChatRoomStatus
    }}>{children}</WsConnectionContext.Provider>
}

export {WsConnectionProvider, WsConnectionContext};
