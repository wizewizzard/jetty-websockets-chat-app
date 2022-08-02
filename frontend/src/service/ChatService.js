import { chatRooms } from "../mocks/chatRooms";
import { mockMessages } from "../mocks/messages";

class ChatService{

    getUserChatRooms(){
        return new Promise((resolve, reject) => {
            setTimeout(() => {
              resolve(
                {
                    status: 200,
                    json: () =>  new Promise((resolve, reject) => {
                        resolve(chatRooms)
                    })
                }
            )
            }, 1500);
            
        })
    }

    findChatRooms({criteria}){
      return new Promise((resolve, reject) => {
        setTimeout(() => {
          resolve(
            {
                status: 200,
                json: () =>  new Promise((resolve, reject) => {
                    resolve(chatRooms)
                })
            }
        )
        }, 1500);
        
    })
    }

    getChatHistory(params){
        const {chatId, depth = 20} = params;
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            resolve(
              {
                  status: 200,
                  json: () =>  new Promise((resolve, reject) => {
                      resolve(mockMessages)
                  })
              }
          )
          }, 1500);
          
      })
    }

    createChatRoom(){

    }

    connectToChatRoom(){
        
    }

    disconnectFromChatRoom(){

    }

    leaveChatRoom(){

    }
    

}

export default new ChatService();