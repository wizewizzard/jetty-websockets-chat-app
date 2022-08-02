import React, {useState} from 'react'

import styles from './Tabs.module.css'
export default function Tabs({tabs, activeTab = tabs[0]}) {
    const [active, setActive] = useState(activeTab);
    console.log('Active tab is: ', active);
    return (
        <>
            <div className={styles['tab-names']}>
                {tabs.map((t, i) => {
                    return (
                        <div key = {i} className={[styles['tab-name'], t === active ? styles['active'] : null].join(' ')} 
                        onClick={() => { setActive(t);}}>
                            {t.name}
                        </div>
                        );
                })}
            </div>
            {
                <div className={styles['tab-content']}>
                    {active.content}
                </div>
                
            }
        </>
    )
}
