import AuthService from "./AuthService";

class ChatService{
    authURL = '/api/auth';
    getUsersChatRooms({userId}){
        return fetch('/api/chat/membership?'  + `user=${userId}`, {
            method: 'GET',
            headers: {
                "Accept": "application/json",
                'Authorization': 'Bearer ' + AuthService.getToken()
            }
        });
    }

    findChatRooms(criteria){
        return fetch('/api/chat/search?' + `name=${criteria.chatName}`, {
            method: 'GET',
            headers: {
                "Accept": "application/json",
                'Authorization': 'Bearer ' + AuthService.getToken()
            }
        });
    }

    getChatHistory({id, untilDateExcluded = new Date().getTime(), depth = 20}){
        return fetch('/api/chat/history?' + `chat=${id}&date=${untilDateExcluded}&depth=${depth}`, {
            method: 'GET',
            headers: {
                "Accept": "application/json",
                'Authorization': 'Bearer ' + AuthService.getToken()
            }
        });
    }

    createChatRoom({chatName}){
        return fetch('/api/chat', {
            method: 'POST',
            headers: {
                "Accept": "application/json",
                'Authorization': 'Bearer ' + AuthService.getToken()
            },
            body: JSON.stringify({chatName})
        });
    }

    connectChatRoom({id}){
        return fetch(`/api/chat/membership/${id}`, {
            method: 'POST',
            headers: {
                "Accept": "application/json",
                'Authorization': 'Bearer ' + AuthService.getToken()
            }
        });
    }

    leaveChatRoom({id}){
        return fetch(`/api/chat/membership/${id}`, {
            method: 'DELETE',
            headers: {
                "Accept": "application/json",
                'Authorization': 'Bearer ' + AuthService.getToken()
            }
        });
    }
    

}


export default new ChatService();