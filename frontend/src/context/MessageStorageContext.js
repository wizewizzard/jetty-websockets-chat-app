import { createContext, useContext, useReducer, useState } from "react";
import ChatService from "../service/ChatService";

const MessagingContext = createContext();
/*
    Messages can be added during lifetime of the application
    When some chat room is selected messages for other rooms are added in the background
    When messages requested check if they are already queried
    To get messages list date is required 
*/

const messageStoreReducer = (messageStore, data) => {
    if(messageStore[data.id]){
        messageStore[data.id].messages = messageStore[data.id].messages.concat(data.messages);
        messageStore[data.id].noMore = data.noMore;
    }
    else{
        messageStore[data.id] = {messages: data.messages, noMore: data.noMore}
    }
    return messageStore;
}

const MessageStorageProvider = ({children}) => {

    const [messageStore, setMessageStore] = useReducer(messageStoreReducer, {});

    const getMessages = ({id, untilDateExcluded, depth}) => {
        //console.log(`Called getMessages with params: ${id} ${untilDateExcluded} ${depth}`);
        return new Promise((resolve, reject) => {
            if(messageStore[id]){
                const storedMessages = messageStore[id].messages.filter(m => Math.floor(Date.parse(m.publishedAt + 'Z') / 1000) < untilDateExcluded);
                if(storedMessages.length >= depth){
                    resolve(storedMessages.slice(0, depth));
                    return;
                }
                else{
                    if(messageStore[id].noMore){
                        resolve(storedMessages);
                        return;
                    }
                    let depthRequired = depth - storedMessages.length;
                    const last = storedMessages.slice(-1)[0];
                    if(last != null){
                        untilDateExcluded = Math.floor(Date.parse(last.publishedAt + 'Z') / 1000)
                    }
                    ChatService.getChatHistory({id, untilDateExcluded, depth: depthRequired})
                    .then(resp => {
                        resp.json()
                        .then(data => {
                            const queriedMessages = data;
                            if(!queriedMessages || queriedMessages.length < depthRequired){
                                setMessageStore({id, messages: queriedMessages, noMore: true});
                            }
                            else{
                                setMessageStore({id, messages: queriedMessages, noMore: false});
                            }
                            resolve(storedMessages.concat(queriedMessages));
                        })
                    })
                    .catch(err => {

                    })
                }
            }
            else{
                ChatService.getChatHistory({id, untilDateExcluded: untilDateExcluded, depth})
                .then(resp => {
                    resp.json()
                    .then(data => {
                        const queriedMessages = data;
                        if(!queriedMessages || queriedMessages.length < depth){
                            setMessageStore({id, messages: queriedMessages, noMore: true});
                        }
                        else{
                            setMessageStore({id, messages: queriedMessages, noMore: false});
                        }
                        resolve(queriedMessages);
                    })
                })
                .catch(err => {
                    reject(err);
                    })
            }
        })
    }

    const addMessage = (message) => {
        if(messageStore[message.chatId]){
            messageStore[message.chatId].messages.unshift(message);
        }
        else{
            setMessageStore({id: message.chatId, messages: [message] });

        }
    }

    return <MessagingContext.Provider value={{
        addMessage,
        getMessages,
    }}>{children}
    </MessagingContext.Provider>
}

export {MessageStorageProvider, MessagingContext};