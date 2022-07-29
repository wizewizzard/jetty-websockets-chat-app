import React from 'react'
import { Link } from 'react-router-dom'

import styles from './ProfileInfo.module.css'

export default function ProfileInfo() {
  return (
    <article className={styles["profile-container"]}>
        <div className={styles["profile-info-box"]}>
            <img src="https://via.placeholder.com/80"/>
            <div className={styles["username"]}>
              Username
            </div>
            <Link className={styles["logout"]} to='/logout'>Logout</Link>
        </div>
    </article>
  )
}
