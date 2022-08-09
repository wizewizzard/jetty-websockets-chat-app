import React from 'react'

export default function Error({message}) {
  return (
    <div className='starter'>
        <h2>Something happend</h2>
        <p>
            Sorry :(
                {message? message : <></>}
        </p>
    </div>
  )
}
