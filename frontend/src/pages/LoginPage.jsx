// src/pages/LoginPage.jsx

import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { loginUser } from "../services/api";

const LoginPage = () => {
  const navigate = useNavigate();

  // Form state
  const [form, setForm] = useState({ email: "", password: "" });

  // UI state
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  // Generic change handler — works for every input
  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError(""); // clear error on new input
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await loginUser(form);
      console.log("Full response:", response.data);
      // Adjust the key paths below to match your Spring Boot response shape.
      // Common patterns:  response.data.token  |  response.data.accessToken
      const token = response.data.token || response.data.accessToken;
      const name  = response.data.name  || response.data.username || response.data.jwt;
      if (!token) throw new Error("Token not received from server.");

      localStorage.setItem("token", token);
      localStorage.setItem("userName", name);

      navigate("/dashboard");
    } catch (err) {
      // Show the backend message when available, else a friendly fallback
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        err.message ||
        "Login failed. Please check your credentials.";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        {/* Header */}
        <div className="auth-header">
          <div className="auth-logo">⚕</div>
          <h1 className="auth-title">Welcome Back</h1>
          <p className="auth-subtitle">Sign in to your healthcare portal</p>
        </div>

        {/* Error banner */}
        {error && <div className="alert alert-error">{error}</div>}

        {/* Form */}
        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="email">Email Address</label>
            <input
              id="email"
              type="email"
              name="email"
              placeholder="doctor@hospital.com"
              value={form.email}
              onChange={handleChange}
              required
              autoComplete="email"
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              name="password"
              placeholder="••••••••"
              value={form.password}
              onChange={handleChange}
              required
              autoComplete="current-password"
            />
          </div>

          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? <span className="spinner" /> : "Sign In"}
          </button>
        </form>

        {/* Footer link */}
        <p className="auth-footer">
          Don't have an account?{" "}
          <Link to="/register" className="auth-link">
            Register here
          </Link>
        </p>
      </div>
    </div>
  );
};

export default LoginPage;