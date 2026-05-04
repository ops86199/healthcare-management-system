// src/pages/DashboardPage.jsx

import React from "react";
import Navbar from "../components/Navbar";

// Stat card data — extend this as you wire up real API endpoints
const STATS = [
  { icon: "👤", label: "Total Patients",    value: "1,284", change: "+12 this week",  color: "#2563eb" },
  { icon: "📅", label: "Appointments Today", value: "34",    change: "8 remaining",    color: "#059669" },
  { icon: "💊", label: "Prescriptions",      value: "562",   change: "+23 this month", color: "#7c3aed" },
  { icon: "🏥", label: "Departments",        value: "12",    change: "All operational", color: "#dc2626" },
];

// Recent activity mock data
const RECENT_ACTIVITY = [
  { id: 1, action: "New patient registered",        time: "2 min ago",  type: "patient"  },
  { id: 2, action: "Appointment confirmed — Rm 4",  time: "15 min ago", type: "appt"     },
  { id: 3, action: "Lab results uploaded",          time: "1 hr ago",   type: "lab"      },
  { id: 4, action: "Prescription issued #RX-8821",  time: "2 hr ago",   type: "rx"       },
  { id: 5, action: "Staff shift change logged",     time: "3 hr ago",   type: "staff"    },
];

const activityIcon = (type) =>
  ({ patient: "👤", appt: "📅", lab: "🔬", rx: "💊", staff: "🏥" }[type] || "📋");

const DashboardPage = () => {
  const name = localStorage.getItem("userName") || "User";

  return (
    <div className="dashboard-layout">
      <Navbar />

      <main className="dashboard-main">
        {/* Welcome banner */}
        <section className="welcome-banner">
          <div>
            <h2 className="welcome-title">Good morning, {name} 👋</h2>
            <p className="welcome-sub">
              Here's what's happening across your facility today.
            </p>
          </div>
          <div className="welcome-date">
            {new Date().toLocaleDateString("en-IN", {
              weekday: "long",
              year: "numeric",
              month: "long",
              day: "numeric",
            })}
          </div>
        </section>

        {/* Stat cards */}
        <section className="stats-grid">
          {STATS.map((s) => (
            <div className="stat-card" key={s.label}>
              <div className="stat-icon" style={{ background: s.color + "1a", color: s.color }}>
                {s.icon}
              </div>
              <div className="stat-info">
                <span className="stat-value">{s.value}</span>
                <span className="stat-label">{s.label}</span>
                <span className="stat-change">{s.change}</span>
              </div>
            </div>
          ))}
        </section>

        {/* Bottom panels */}
        <section className="bottom-panels">
          {/* Recent activity */}
          <div className="panel">
            <h3 className="panel-title">Recent Activity</h3>
            <ul className="activity-list">
              {RECENT_ACTIVITY.map((item) => (
                <li key={item.id} className="activity-item">
                  <span className="activity-icon">{activityIcon(item.type)}</span>
                  <div className="activity-text">
                    <span className="activity-action">{item.action}</span>
                    <span className="activity-time">{item.time}</span>
                  </div>
                </li>
              ))}
            </ul>
          </div>

          {/* Quick actions */}
          <div className="panel">
            <h3 className="panel-title">Quick Actions</h3>
            <div className="quick-actions">
              {[
                { icon: "➕", label: "Add Patient"      },
                { icon: "📅", label: "Schedule Appointment" },
                { icon: "💊", label: "Issue Prescription" },
                { icon: "📄", label: "Generate Report"  },
              ].map((a) => (
                <button key={a.label} className="quick-action-btn">
                  <span>{a.icon}</span>
                  {a.label}
                </button>
              ))}
            </div>
          </div>
        </section>
      </main>
    </div>
  );
};

export default DashboardPage;