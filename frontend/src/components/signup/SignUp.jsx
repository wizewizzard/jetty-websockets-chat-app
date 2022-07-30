import React, {useState} from 'react'
import { Link, useNavigate } from 'react-router-dom';
import AuthService from '../../service/AuthService';

export default function SignUp() {
  const [userName, setUserName] = useState('');
  const [password, setPassword] = useState('');
  const [email, setEmail] = useState('');
  const navigate = useNavigate();


  const handleSubmit = (event) => {
    event.preventDefault();
    AuthService
      .signUp({userName, password, email})
      .then(resp => {
        if(resp.status === 201){
          navigate('/signin');
        }
        else{
          //TODO: output errors
          console.log('Server responded with code: ', resp.status);
          resp.json().then(data => console.log(data.message));
        }
          
      });
    ;
  }

  return (
    <>
      <h2>Create a new account</h2>
      <p className='hint'>If you already have an account, then {<Link to='/signin'>sign in</Link>}</p>
      <form onSubmit={handleSubmit}>
        <input type='text' placeholder='User name' onChange={e => setUserName(e.target.value)}/>
        <input type='password' placeholder='Password' onChange={e => setPassword(e.target.value)}/>
        <input type='text' placeholder='Email' onChange={e => setEmail(e.target.value)}/>
        <button>Sign up</button>
      </form>
    </>
  )
}
