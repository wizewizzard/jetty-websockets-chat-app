import React, {useState} from 'react'

import styles from './Tabs.module.css'
export default function Tabs({tabs, initial = 0}) {
    const [active, setActive] = useState(initial);
    console.log('Active tab is: ', active);
    return (
        <>
            <div className={styles['tab-names']}>
                {tabs.map((t, i) => {
                    return (
                        <div key = {i} className={[styles['tab-name'], i === active ? styles['active'] : null].join(' ')} onClick={() => {
                            setActive(i);
                        }}>
                            {t.name}
                        </div>
                        );
                })}
            </div>
            {tabs[active].content}
        </>
    )
}
