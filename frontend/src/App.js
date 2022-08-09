import React from 'react';
import { BrowserRouter as Router, Route, Routes, useNavigate } from 'react-router-dom';

import './App.css';
import AppContainer from './components/AppContainer';
import SignIn from './components/static/SignIn';
import SignUp from './components/static/SignUp';
import Header from './components/static/Header';
import { AuthContextProvider } from './context/AuthContext';
import Error from './components/static/Error';



function App() {
  return (  
    <Router>
        <div className="app">
          <Header />
          <div className='content'>
          <AuthContextProvider>
            <Routes >
                  <Route path= "/error" element={<Error />} />
                  <Route path= "/signin" element={<SignIn />} />
                  <Route path= "/signup" element={<SignUp />} /> 
              <Route path='/' element={<AppContainer />} />
            </Routes>
            </AuthContextProvider>
          </div>
        </div>
    </Router>
  );
  
}

export default App;
