import ChatInput from './ChatInput';
import styles from './ChatWindow.module.css'

function generateMessages(){
    const messages = [
        {
            body: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent varius, neque non tristique tincidunt, mauris nunc efficitur erat, elementum semper justo odio id nisi.',
            createdBy: 'Bob',
            publishedAt: new Date().toString()
        },
        {
            body: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent varius, neque non tristique tincidunt, mauris nunc efficitur erat, elementum semper justo odio id nisi.',
            createdBy: 'Alice',
            publishedAt: new Date().toString()
        },
        {
            body: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent varius, neque non tristique tincidunt, mauris nunc efficitur erat, elementum semper justo odio id nisi.',
            createdBy: 'Bob',
            publishedAt: new Date().toString()
        },
        {
            body: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent varius, neque non tristique tincidunt, mauris nunc efficitur erat, elementum semper justo odio id nisi.',
            createdBy: 'John',
            publishedAt: new Date().toString()
        },
        {
            body: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent varius, neque non tristique tincidunt, mauris nunc efficitur erat, elementum semper justo odio id nisi.',
            createdBy: 'Bob',
            publishedAt: new Date().toString()
        },
    ];
    return messages;
} 

export default function ChatWindow(props){
    const messages = generateMessages();

    return (
        <section className={styles.chatbox}>
            <section className={styles["chat-window"]}>
            {
                messages.map((m, i) => {
                    return (
                        <article key={i} className={[styles["msg-container"], m.createdBy === 'Bob' ? styles['msg-remote'] : styles['msg-self']].join(' ')} id="msg-0">
                            <div className={styles['msg-box']}>
                                <img className={styles['user-img']} id="user-0" src="https://via.placeholder.com/50" />
                                <div className={styles['flr']}>
                                    <p className={styles.msg} id="msg-0">
                                        {m.body}
                                    </p>
                                    <span className={styles["timestamp"]}><span className={styles["username"]}>{m.createdBy}</span>&bull;<span className={styles["posttime"]}>{m.publishedAt}</span></span>
                                </div>
                            </div>
                        </article>
                    );
                })
            }
            </section>
            <ChatInput />
        </section>
    );
}