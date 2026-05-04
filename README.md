# healthcare-management-system
# Healthcare Management System — React Frontend

A clean, production-style React frontend that connects to a Spring Boot REST API.

---

## Quick Start

```bash
# 1. Install dependencies
npm install

# 2. Start the dev server (Spring Boot must be running on port 8080)
npm start
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

---

## Folder Structure

```
src/
├── components/
│   ├── Navbar.jsx          # Top nav with logout
│   └── ProtectedRoute.jsx  # Guards /dashboard from unauthenticated users
├── pages/
│   ├── LoginPage.jsx       # POST /api/auth/login
│   ├── RegisterPage.jsx    # POST /api/auth/register
│   └── DashboardPage.jsx   # Protected home screen
├── services/
│   └── api.js              # Axios instance + all API calls
├── App.js                  # React Router setup
├── App.css                 # All styles
└── index.js                # React entry point
```

---

## API Endpoints Used

| Method | URL                          | Page      |
|--------|------------------------------|-----------|
| POST   | `/api/auth/register`         | Register  |
| POST   | `/api/auth/login`            | Login     |

### Expected Login Response (from Spring Boot)

```json
{
  "token": "eyJhbGci...",
  "name": "Dr. Jane Smith"
}
```

> If your backend uses `accessToken` instead of `token`, or returns the user's
> name under a different key, update the mappings in `src/pages/LoginPage.jsx`.

---

## Routes

| Path          | Component       | Protected |
|---------------|-----------------|-----------|
| `/login`      | `LoginPage`     | No        |
| `/register`   | `RegisterPage`  | No        |
| `/dashboard`  | `DashboardPage` | Yes ✔     |
| `*` (any)     | → `/login`      | —         |

---

## Token Storage

- JWT is stored in `localStorage` under the key `token`.
- The Axios interceptor in `api.js` automatically attaches it as `Authorization: Bearer <token>` to every request.
- Clicking **Logout** clears `token` + `userName` and redirects to `/login`.

---

## Customising the Dashboard

The stats and activity feed in `DashboardPage.jsx` use hardcoded mock data.
Wire them up by adding new functions to `src/services/api.js` and fetching
them inside a `useEffect` in the dashboard component.