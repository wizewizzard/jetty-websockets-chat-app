
# Chat app using Websockets 

A chat app. Besides chatting users are able to create, connect, search and leave chat rooms.

My goal was to get a grasp of servlets, websocket connection and embedded Jetty server and practice multithreading and React a bit.

## Tech Stack

**Client:** React

**Server:** Embedded Jetty, Weld CDI, Hibernate, PostgresSQL


## Challenges and what I've learned

#### In fact, I did not expect any big challenges from this study as I had pretty clear picture of how backend should work like. Still there was a couple...
* Servlets... These guys are not as user-friendly as Spring's controllers and a lot of boilerplate code is involved. However, it feels good to skip managing of `*.xml` files in order to configure servlets.
* Application and servlet contexts. Yeah, they are not the same... all that they have in common is a context word. It was pretty funny when for hours I was expecting a bean that had been put inside the ServletContext to be accessible in the ApplicationContext by `@Inject` annotation. 
* JPA EntityManager management. It is said that EntityManager instances are not thread safe, so one should avoid share the same instance among, let's say, request handlers. So `@RequestScoped` or `@Dependent` annotations come in handy.

#### And what I learned
* Weld CDI is pretty much the same as Spring Context. And what I really liked about it are event observers `@Observer` that allow to avoid injecting additional dependencies and process events as soon as they occur.
* Got a good practice with the useContext hook

## Features

- Chat rooms have finite lifetime. After timeout is exceeded a room thread is terminated. Room launches again as soon as user writes or connects.
- Single websocket connection is used to communicate in multiple chat rooms. 
- User must provide bearer token to make requests and open websocket connection.



## Current state

- [x]  JWT Authentication
- [ ]  Tracking chat room's members and their online status. *Currently, it does show user goes online/offline but does not provide a list of chat room members and their online statuses yet.*
- [x]  Users are allowed to create, search and connect/disconnect to/from a specific chat room
- [x]  And chatting obviously 


#### Optional
- [x]  Deploy on Heroku
## Demo

Available on [Heroku](https://arcane-castle-98793.herokuapp.com/)