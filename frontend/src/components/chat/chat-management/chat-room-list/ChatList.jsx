import React, {useState, useEffect} from 'react'
import Loader from '../../../static/Loader';

import styles from './ChatList.module.css'

export const chatStatus = { 
  Connected: 'Connected', 
  Connecting: 'Connecting', 
  Disconnected: 'Disconnected'
}

export default function ChatList({items}) {
  items = [
    {
      name: "Bob's chat",
      status: chatStatus.Connected
    },
    {
      name: "Boooo's chatddddddddddddddddddddddd dddddddd",
      status: chatStatus.Disconnected
    },
    {
      name: "Boooo's chat",
      status: chatStatus.Connecting
    }
  ];

  return (
    <>
      <section className={styles["chat-list"]}>
        {items.map((c, i) => {
          return(
            <article key = {i} className={styles["chat-info"]}>
            <div className={styles["chat-room-name"]}>{c.name}</div>
            <div className={styles["chat-actions"]}>
            { (() => {
              switch(c.status){
                case chatStatus.Connected: 
                  return (<><a href='#'>Disconnect</a><a href='#'>Leave</a></>);
                case chatStatus.Disconnected: 
                  return (<><a href='#'>Connect</a><a href='#'>Leave</a></>);
            }})()}
            </div>
          </article>
          )
        })}
      </section>
    </>
  )
}
