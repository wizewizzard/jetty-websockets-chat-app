import React from 'react'
import { Link } from 'react-router-dom'

export default function SignIn() {
  return (
    <form>
      <h2>Log in</h2>
      <p className='hint'>If you don't have an account, then {<Link to='/signup'>create</Link>} a new one</p>
    </form>
  )
}
