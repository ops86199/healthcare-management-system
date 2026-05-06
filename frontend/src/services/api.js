// src/services/api.js
// Central Axios instance and all API calls live here.
// Swap BASE_URL if your Spring Boot backend runs on a different port.

import axios from "axios";

const BASE_URL = "http://localhost:8080/api/v1";

// Axios instance shared by every request
const api = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
});

// Attach JWT automatically for authenticated requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ── Auth endpoints ────────────────────────────────────────────────────────────

/**
 * Register a new user.
 * @param {{ name: string, email: string, password: string }} data
 */
export const registerUser = (data) => api.post("/auth/register", data);

/**
 * Login an existing user.
 * @param {{ email: string, password: string }} data
 * @returns JWT token in response.data.token (adjust if your backend differs)
 */
export const loginUser = (data) => api.post("/auth/login", data);

export default api;