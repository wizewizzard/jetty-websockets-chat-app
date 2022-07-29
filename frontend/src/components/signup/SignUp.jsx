import React from 'react'
import { Link } from 'react-router-dom'

export default function SignUp() {
  return (
    <div>
      <h2>Create a new account</h2>
      <p className='hint'>If you already have an account, then {<Link to='/signin'>sign in</Link>}</p>
    </div>
  )
}
