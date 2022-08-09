import React from 'react'

import styles from './ChatWindow.module.css'

export default function ConnectionBox({header, message}) {
  return (
    <div className={styles['to-start']}>
        <h2>{header}</h2>
        <p>
        {message}
        </p>
    </div>
  )
}
