import React, {useState} from 'react'
import { Link, useNavigate } from 'react-router-dom';
import AuthService from '../../service/AuthService';
import styles from './Sign.module.css'

export default function SignUp() {
  const [userName, setUserName] = useState('');
  const [password, setPassword] = useState('');
  const [email, setEmail] = useState('');
  const [error, setError] = useState(null);
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
          resp.json()
          .then(data => {
            console.log(data);
            setError(data);
          })
          .catch(err => {
            console.log(resp)
            setError({message: resp.statusText});
        });
        }
      })
      .catch(err => {
        setError(err);
      })
      ;
    ;
  }

  return (
    <div className={styles['sign-container']}>
      <h2>Create a new account</h2>
      <p className='hint'>If you already have an account, then {<Link to='/signin'>sign in</Link>}
      <br />
      No validation. Just fill it with any data you want</p>
      {error ? <div className='error-message'>{error.message}</div> : <></>}
      <div className={styles['grid']}>
        <div className={styles['form-column']}>
          <form onSubmit={handleSubmit}>
            <input type='text' className='input' placeholder='User name' onChange={e => setUserName(e.target.value)}/>
            <input type='password' className='input' placeholder='Password' onChange={e => setPassword(e.target.value)}/>
            <input type='text' className='input' placeholder='Email' onChange={e => setEmail(e.target.value)}/>
            <button className='button'>Sign up</button>
          </form>
        </div>
      </div>
      
    </div>
  )
}
