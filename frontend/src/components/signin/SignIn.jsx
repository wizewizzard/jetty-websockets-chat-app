import React, {useState} from 'react'
import { Link, useNavigate } from 'react-router-dom';
import AuthService from '../../service/AuthService';

export default function SignIn() {
  const [userName, setUserName] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleSubmit = (event) => {
    event.preventDefault();
    AuthService
      .signIn({userName: userName, password: password})
      .then(resp => {
        if(resp.status === 200){
          resp.json().then(data => {
            console.log('Token received from server: ', data.token)
            AuthService.setToken(data.token)
            navigate('/');
          });
        }
        else{
          //TODO: handle error
          resp.json().then(data => {
            console.log(data.message);
          });
        }
      });
    ;
  }
  return (
    <>
      <h2>Log in</h2>
        <p className='hint'>If you don't have an account, then {<Link to='/signup'>create</Link>} a new one</p>
      <form onSubmit={handleSubmit}>
        <input type='text' placeholder='User name' onChange={e => setUserName(e.target.value)}/>
        <input type='password' placeholder='Password' onChange={e => setPassword(e.target.value)}/>
        <button>Sign in</button>
      </form>
    </>
  )
}
