import React from 'react'
import { Link } from 'react-router-dom'

export default function ToStart() {
  return (
    <div className='starter'>
        <h2>Welcome to the chat app</h2>
        <p>
            To start using chat app either {<Link to = "/signup">Sign up</Link>} or {<Link to = "/signin">Sign in</Link>}
        </p>
    </div>
  )
}
