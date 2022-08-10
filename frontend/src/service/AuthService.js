class AuthService{
    authURL = '/api/auth';
    
    setToken(token){
        localStorage.setItem('bearer-token', token);
    }

    getToken(){
        return localStorage.getItem('bearer-token');
    }

    signUp(signUpObj){
        return fetch(this.authURL  + "/signup", {
            method: 'POST',
            headers: {
                "Accept": "application/json"
            },
            body: JSON.stringify(signUpObj)
        });
    }

    signIn(signInObj){
        return fetch(this.authURL  + "/signin", {
            method: 'POST',
            headers: {
                "Accept": "application/json"
            },
            body: JSON.stringify(signInObj)
        });
    }

    verify(){
        const token = this.getToken();
        if(token){
            return fetch(this.authURL  + `/verify?token=${token}`, {
                method: 'GET',
                headers: {
                    "Accept": "application/json"
                }
            });
        }
        else{
            return new Promise((resolve, reject) => {
                reject();
            });
        }        
    }

    logout(){
        return new Promise((resolve,reject) => {
            localStorage.removeItem('bearer-token');
            resolve();
        });
    }
}

export default new AuthService();