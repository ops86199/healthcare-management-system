// src/components/Navbar.jsx
// Top navigation bar shown only on authenticated pages.

import React from "react";
import { useNavigate } from "react-router-dom";

const Navbar = () => {
  const navigate = useNavigate();
  const name = localStorage.getItem("userName") || "User";

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userName");
    navigate("/login");
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <span className="brand-icon">⚕</span>
        <span className="brand-text">HealthCare MS</span>
      </div>
      <div className="navbar-right">
        <span className="nav-greeting">Hello, {name}</span>
        <button className="logout-btn" onClick={handleLogout}>
          Logout
        </button>
      </div>
    </nav>
  );
};

export default Navbar;