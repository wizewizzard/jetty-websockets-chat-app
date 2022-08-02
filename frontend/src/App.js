import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Route, Routes, useNavigate } from 'react-router-dom';

import './App.css';
import AppContainer from './components/AppContainer';
import SignIn from './components/signin/SignIn';
import SignUp from './components/signup/SignUp';
import Header from './components/static/Header';



function App() {
  return (  
    <Router>
        <div className="app">
          <Header />
          <div className='content'>
            <Routes >
              <Route path= "/signin" element={<SignIn />} />
              <Route path= "/signup" element={<SignUp />} />
              <Route path='/' element={<AppContainer />} />
            </Routes>
          </div>
        </div>
    </Router>
  );
  
}

export default App;
