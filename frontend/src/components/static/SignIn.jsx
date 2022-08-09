import React, {useContext, useState} from 'react'
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../../context/AuthContext';
import styles from './Sign.module.css'

export default function SignIn() {
  const [userName, setUserName] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const {logIn} = useContext(AuthContext);
  const navigate = useNavigate();

  const handleSubmit = (event) => {
    event.preventDefault();
    setError(null);
    logIn({userName: userName, password: password})
    .then(res => {
      navigate('/');
    })
    .catch(err => {
      console.log(err);
      setError(err);
    })
  }
  return (
    <div className={styles['sign-container']}>
      <h2>Log in</h2>
        <p className='hint'>If you don't have an account, then {<Link to='/signup'>create</Link>} a new one</p>
        {error ? <div className='error-message'>{error.message}</div> : <></>}
        <div className={styles['grid']}>
          <div className={styles['form-column']}>
            <form onSubmit={handleSubmit}>
              <input type='text' className='input' placeholder='User name' onChange={e => setUserName(e.target.value)}/>
              <input type='password' className='input' placeholder='Password' onChange={e => setPassword(e.target.value)}/>
              <button className='button'>Sign in</button>
            </form>
        </div>
      </div>
    </div>
  )
}
