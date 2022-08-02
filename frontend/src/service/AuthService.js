/*class AuthService{
    authURL = '/api/auth';
    
    setToken(token){
        localStorage.setItem('bearer-token', token);
    }

    getToken(){
        return localStorage.getItem('bearer-token');
    }

    signUp(signUpObj){
        console.log('Signing up');
        return fetch(this.authURL  + "/signup", {
            method: 'POST',
            headers: {
                "Accept": "application/json"
            },
            body: JSON.stringify(signUpObj)
        });
    }

    signIn(signInObj){
        console.log('Signing in');
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
            console.log('Making api call to verify token');
            return fetch(this.authURL  + `/verify?token=${token}`, {
                method: 'GET',
                headers: {
                    "Accept": "application/json"
                }
            });
        }
        else{
            console.log('No token to verify');
            return new Promise((resolve, reject) => {
                reject();
            });
        }        
    }

    logout(){
        console.log('Logging out');
        return new Promise((resolve,reject) => {
            localStorage.removeItem('bearer-token');
            resolve();
        });
    }
}*/

class AuthService{
    authURL = '/api/auth';
    
    setToken(token){
        localStorage.setItem('bearer-token', token);
    }

    getToken(){
        return localStorage.getItem('bearer-token');
    }

    signUp(signUpObj){
        console.log('Signing up');
        return new Promise((resolve, reject) => {
            resolve(
                {
                    status: 201
                }
            )
        })
    }

    signIn(signInObj){
        console.log('Signing in');
        console.log(signInObj);
        return new Promise((resolve, reject) => {
            resolve(
                {
                    status: 200,
                    json: () =>  new Promise((resolve, reject) => {
                        resolve("MOCK_TOKEN")
                    })
                }
            )
        })
    }

    verify(){
        const token = this.getToken();
        if(token){
            console.log('Making api call to verify token');
            return new Promise((resolve, reject) => {
                setTimeout(() => {
                    resolve(
                        {
                            status: 200,
                            json: () =>  new Promise((resolve, reject) => {
                                resolve({userName: 'w'})
                            })
                        }
                    )
                }, 500);
                
            })
        }
        else{
            console.log('No token to verify');
            return new Promise((resolve, reject) => {
                reject();
            });
        }        
    }

    logout(){
        console.log('Logging out');
        return new Promise((resolve,reject) => {
            localStorage.removeItem('bearer-token');
            resolve();
        });
    }
}

export default new AuthService();