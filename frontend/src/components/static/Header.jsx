import React from "react";
import { Link } from "react-router-dom";

export default function Header() {
    return (
        <div className="header">
          <Link to='/'>
            <div className="logo">
              <img src="/message-logo.svg"></img>
              <div>Chat app</div>
            </div>
          </Link>
        </div>
    );
}