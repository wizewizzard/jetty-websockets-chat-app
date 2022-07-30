import React from 'react'

export default function Loader({visible, message = 'Loading...'}) {
    if(visible)
        return (
            <>
                <div className='loader'>
                    <div className="lds-ring">
                        <div></div><div></div><div></div><div></div></div>
                    {message}
                </div>
            </>
        )
}
