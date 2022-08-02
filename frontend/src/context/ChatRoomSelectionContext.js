import { createContext, useContext, useReducer, useState } from "react";
const ChatRoomSelectionContext = createContext();

const ChatRoomSelectionProvider = ({children}) => {
    const [selectedRoom, setSelectedRoom] = useState(null)

    return <ChatRoomSelectionContext.Provider value={{
        selectedRoom,
        setSelectedRoom
    }}>{children}</ChatRoomSelectionContext.Provider>
}

export {ChatRoomSelectionProvider, ChatRoomSelectionContext}