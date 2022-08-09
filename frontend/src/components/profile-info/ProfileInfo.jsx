import React from 'react'

import styles from './ProfileInfo.module.css'

export default function ProfileInfo({userName}) {
  return (
    <article className={styles["profile-container"]}>
        <div className={styles["profile-info-box"]}>
            <img src="https://placehold.co/80"/>
            <div className={styles["username"]}>
              {userName}
            </div>
        </div>
    </article>
  )
}
