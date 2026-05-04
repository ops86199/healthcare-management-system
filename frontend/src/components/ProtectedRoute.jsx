// src/components/ProtectedRoute.jsx
// Wraps any route that requires authentication.
// If no token is found in localStorage the user is redirected to /login.

import React from "react";
import { Navigate } from "react-router-dom";

const ProtectedRoute = ({ children }) => {
  const token = localStorage.getItem("token");
  return token ? children : <Navigate to="/login" replace />;
};

export default ProtectedRoute;