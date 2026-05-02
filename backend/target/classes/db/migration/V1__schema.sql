-- =============================================================
--  Healthcare Management System — PostgreSQL Schema
--  Modules: Patients · Doctors · Appointments · Billing ·
--           Medicine Inventory · Admin / Audit
-- =============================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- -------------------------------------------------------------
-- ENUMS
-- -------------------------------------------------------------
CREATE TYPE gender_type       AS ENUM ('male', 'female', 'other');
CREATE TYPE blood_type        AS ENUM ('A+','A-','B+','B-','AB+','AB-','O+','O-');
CREATE TYPE user_role         AS ENUM ('admin', 'doctor', 'receptionist', 'pharmacist');
CREATE TYPE appointment_status AS ENUM ('scheduled','confirmed','completed','cancelled','no_show');
CREATE TYPE appointment_type  AS ENUM ('in_person','telemedicine','follow_up','emergency');
CREATE TYPE day_of_week       AS ENUM ('monday','tuesday','wednesday','thursday','friday','saturday','sunday');
CREATE TYPE invoice_status    AS ENUM ('draft','issued','paid','overdue','cancelled');
CREATE TYPE payment_method    AS ENUM ('cash','card','insurance','bank_transfer','upi');
CREATE TYPE invoice_item_type AS ENUM ('consultation','medicine','lab_test','procedure','other');

-- =============================================================
-- MODULE 1 — ADMIN / USER MANAGEMENT
-- =============================================================

CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username      VARCHAR(60)  NOT NULL UNIQUE,
    email         VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          user_role    NOT NULL DEFAULT 'receptionist',
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    last_login    TIMESTAMP WITH TIME ZONE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role  ON users (role);

-- Audit trail for every sensitive operation
CREATE TABLE audit_logs (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID REFERENCES users (id) ON DELETE SET NULL,
    action     VARCHAR(50) NOT NULL,          -- e.g. INSERT, UPDATE, DELETE, LOGIN
    table_name VARCHAR(80) NOT NULL,
    record_id  UUID,
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_user      ON audit_logs (user_id);
CREATE INDEX idx_audit_table     ON audit_logs (table_name);
CREATE INDEX idx_audit_created   ON audit_logs (created_at DESC);

-- =============================================================
-- MODULE 2 — PATIENT REGISTRATION
-- =============================================================

CREATE TABLE patients (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name        VARCHAR(80)  NOT NULL,
    last_name         VARCHAR(80)  NOT NULL,
    date_of_birth     DATE         NOT NULL,
    gender            gender_type  NOT NULL,
    blood_type        blood_type,
    address           TEXT,
    phone             VARCHAR(20)  NOT NULL,
    email             VARCHAR(120),
    emergency_contact VARCHAR(120),  -- "Name: +phone"
    insurance_provider VARCHAR(120),
    insurance_number   VARCHAR(80),
    allergies          TEXT[],        -- array of known allergens
    notes              TEXT,
    is_active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_patients_name  ON patients (last_name, first_name);
CREATE INDEX idx_patients_phone ON patients (phone);
CREATE INDEX idx_patients_email ON patients (email);

-- =============================================================
-- MODULE 3 — DOCTOR MANAGEMENT
-- =============================================================

CREATE TABLE doctors (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID UNIQUE REFERENCES users (id) ON DELETE SET NULL,
    first_name       VARCHAR(80)  NOT NULL,
    last_name        VARCHAR(80)  NOT NULL,
    specialization   VARCHAR(120) NOT NULL,
    license_number   VARCHAR(60)  NOT NULL UNIQUE,
    phone            VARCHAR(20)  NOT NULL,
    email            VARCHAR(120) NOT NULL UNIQUE,
    experience_years INT          NOT NULL DEFAULT 0 CHECK (experience_years >= 0),
    consultation_fee NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (consultation_fee >= 0),
    bio              TEXT,
    profile_image    TEXT,         -- URL / file path
    is_available     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_doctors_specialization ON doctors (specialization);
CREATE INDEX idx_doctors_available      ON doctors (is_available);

-- Weekly recurring availability slots per doctor
CREATE TABLE doctor_schedules (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id   UUID NOT NULL REFERENCES doctors (id) ON DELETE CASCADE,
    day_of_week day_of_week NOT NULL,
    slot_start  TIME NOT NULL,
    slot_end    TIME NOT NULL,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT no_slot_overlap CHECK (slot_start < slot_end)
);

CREATE UNIQUE INDEX idx_schedule_unique
    ON doctor_schedules (doctor_id, day_of_week, slot_start);

-- One-off date-level overrides (holidays, vacations, extra sessions)
CREATE TABLE doctor_schedule_overrides (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id   UUID NOT NULL REFERENCES doctors (id) ON DELETE CASCADE,
    override_date DATE NOT NULL,
    is_available  BOOLEAN NOT NULL DEFAULT FALSE,   -- FALSE = blocked
    reason        TEXT,
    UNIQUE (doctor_id, override_date)
);

-- =============================================================
-- MODULE 4 — APPOINTMENT BOOKING
-- =============================================================

CREATE TABLE appointments (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id       UUID NOT NULL REFERENCES patients (id) ON DELETE RESTRICT,
    doctor_id        UUID NOT NULL REFERENCES doctors  (id) ON DELETE RESTRICT,
    schedule_id      UUID          REFERENCES doctor_schedules (id) ON DELETE SET NULL,
    appointment_time TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_minutes INT NOT NULL DEFAULT 30 CHECK (duration_minutes > 0),
    status           appointment_status NOT NULL DEFAULT 'scheduled',
    type             appointment_type   NOT NULL DEFAULT 'in_person',
    chief_complaint  TEXT,
    notes            TEXT,
    diagnosis        TEXT,
    follow_up_date   DATE,
    created_by       UUID REFERENCES users (id) ON DELETE SET NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_appt_patient  ON appointments (patient_id);
CREATE INDEX idx_appt_doctor   ON appointments (doctor_id);
CREATE INDEX idx_appt_time     ON appointments (appointment_time);
CREATE INDEX idx_appt_status   ON appointments (status);

-- Vital signs recorded at each appointment
CREATE TABLE vital_signs (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id   UUID NOT NULL REFERENCES appointments (id) ON DELETE CASCADE,
    temperature_c    NUMERIC(4,1),
    pulse_bpm        INT,
    systolic_bp      INT,
    diastolic_bp     INT,
    oxygen_saturation NUMERIC(5,2),
    respiratory_rate INT,
    weight_kg        NUMERIC(6,2),
    height_cm        NUMERIC(5,1),
    recorded_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- =============================================================
-- MODULE 5 — MEDICINE INVENTORY
-- =============================================================

CREATE TABLE medicines (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(200) NOT NULL,
    generic_name  VARCHAR(200),
    category      VARCHAR(100) NOT NULL,    -- e.g. Antibiotic, Analgesic
    manufacturer  VARCHAR(200),
    unit          VARCHAR(40)  NOT NULL DEFAULT 'tablet',
    strength      VARCHAR(60),             -- e.g. "500mg"
    price         NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (price >= 0),
    reorder_level INT NOT NULL DEFAULT 50 CHECK (reorder_level >= 0),
    requires_prescription BOOLEAN NOT NULL DEFAULT TRUE,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_medicines_name     ON medicines (name);
CREATE INDEX idx_medicines_category ON medicines (category);

-- Batched stock entries; multiple batches per medicine
CREATE TABLE medicine_inventory (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    medicine_id    UUID NOT NULL REFERENCES medicines (id) ON DELETE RESTRICT,
    quantity       INT  NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    expiry_date    DATE NOT NULL,
    batch_number   VARCHAR(80) NOT NULL,
    supplier       VARCHAR(200),
    received_date  DATE NOT NULL DEFAULT CURRENT_DATE,
    purchase_price NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (purchase_price >= 0),
    notes          TEXT,
    UNIQUE (medicine_id, batch_number)
);

CREATE INDEX idx_inventory_medicine ON medicine_inventory (medicine_id);
CREATE INDEX idx_inventory_expiry   ON medicine_inventory (expiry_date);

-- Append-only movement log (purchases in, dispensing out, adjustments)
CREATE TABLE inventory_transactions (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inventory_id   UUID NOT NULL REFERENCES medicine_inventory (id) ON DELETE RESTRICT,
    medicine_id    UUID NOT NULL REFERENCES medicines (id) ON DELETE RESTRICT,
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('purchase','dispense','adjustment','expired','returned')),
    quantity_change INT NOT NULL,          -- positive = in, negative = out
    reference_id   UUID,                  -- appointment_id or purchase order
    performed_by   UUID REFERENCES users (id) ON DELETE SET NULL,
    notes          TEXT,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_inv_tx_medicine ON inventory_transactions (medicine_id);
CREATE INDEX idx_inv_tx_date     ON inventory_transactions (created_at DESC);

-- Prescriptions issued at appointments
CREATE TABLE prescriptions (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id UUID NOT NULL REFERENCES appointments (id) ON DELETE CASCADE,
    medicine_id    UUID NOT NULL REFERENCES medicines   (id) ON DELETE RESTRICT,
    dosage         VARCHAR(60)  NOT NULL,   -- e.g. "500 mg"
    frequency      VARCHAR(60)  NOT NULL,   -- e.g. "twice daily"
    duration_days  INT NOT NULL CHECK (duration_days > 0),
    instructions   TEXT,
    quantity       INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    dispensed      BOOLEAN NOT NULL DEFAULT FALSE,
    dispensed_at   TIMESTAMP WITH TIME ZONE,
    dispensed_by   UUID REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_prescriptions_appointment ON prescriptions (appointment_id);
CREATE INDEX idx_prescriptions_medicine    ON prescriptions (medicine_id);

-- =============================================================
-- MODULE 6 — BILLING
-- =============================================================

CREATE TABLE invoices (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id   UUID UNIQUE REFERENCES appointments (id) ON DELETE RESTRICT,
    patient_id       UUID NOT NULL REFERENCES patients (id) ON DELETE RESTRICT,
    consultation_fee NUMERIC(10,2) NOT NULL DEFAULT 0,
    medicine_total   NUMERIC(10,2) NOT NULL DEFAULT 0,
    lab_total        NUMERIC(10,2) NOT NULL DEFAULT 0,
    tax_amount       NUMERIC(10,2) NOT NULL DEFAULT 0,
    discount         NUMERIC(10,2) NOT NULL DEFAULT 0,
    total_amount     NUMERIC(10,2) NOT NULL GENERATED ALWAYS AS
                         (consultation_fee + medicine_total + lab_total + tax_amount - discount) STORED,
    status           invoice_status NOT NULL DEFAULT 'draft',
    payment_method   payment_method,
    insurance_claimed BOOLEAN NOT NULL DEFAULT FALSE,
    insurance_amount  NUMERIC(10,2) NOT NULL DEFAULT 0,
    notes            TEXT,
    issued_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    paid_at          TIMESTAMP WITH TIME ZONE,
    created_by       UUID REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_invoices_patient ON invoices (patient_id);
CREATE INDEX idx_invoices_status  ON invoices (status);
CREATE INDEX idx_invoices_issued  ON invoices (issued_at DESC);

CREATE TABLE invoice_items (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id   UUID NOT NULL REFERENCES invoices (id) ON DELETE CASCADE,
    item_type    invoice_item_type NOT NULL,
    description  VARCHAR(255) NOT NULL,
    quantity     INT          NOT NULL DEFAULT 1 CHECK (quantity > 0),
    unit_price   NUMERIC(10,2) NOT NULL CHECK (unit_price >= 0),
    subtotal     NUMERIC(10,2) NOT NULL GENERATED ALWAYS AS (quantity * unit_price) STORED,
    reference_id UUID          -- medicine_id, prescription_id, etc.
);

CREATE INDEX idx_invoice_items_invoice ON invoice_items (invoice_id);

-- =============================================================
-- ADMIN DASHBOARD VIEWS
-- =============================================================

-- Today's appointment snapshot
CREATE VIEW vw_todays_appointments AS
SELECT
    a.id,
    a.appointment_time,
    a.status,
    a.type,
    p.first_name || ' ' || p.last_name AS patient_name,
    p.phone                             AS patient_phone,
    d.first_name || ' ' || d.last_name AS doctor_name,
    d.specialization
FROM appointments a
JOIN patients p ON p.id = a.patient_id
JOIN doctors  d ON d.id = a.doctor_id
WHERE a.appointment_time::date = CURRENT_DATE
ORDER BY a.appointment_time;

-- Revenue summary by month
CREATE VIEW vw_monthly_revenue AS
SELECT
    DATE_TRUNC('month', issued_at) AS month,
    COUNT(*)                        AS invoice_count,
    SUM(total_amount)               AS gross_revenue,
    SUM(discount)                   AS total_discounts,
    SUM(total_amount - discount)    AS net_revenue,
    COUNT(*) FILTER (WHERE status = 'paid')      AS paid_count,
    COUNT(*) FILTER (WHERE status = 'overdue')   AS overdue_count
FROM invoices
GROUP BY DATE_TRUNC('month', issued_at)
ORDER BY month DESC;

-- Low stock alert (below reorder level, aggregated across batches)
CREATE VIEW vw_low_stock AS
SELECT
    m.id,
    m.name,
    m.category,
    m.reorder_level,
    COALESCE(SUM(inv.quantity), 0) AS current_stock
FROM medicines m
LEFT JOIN medicine_inventory inv
    ON inv.medicine_id = m.id AND inv.expiry_date > CURRENT_DATE
WHERE m.is_active = TRUE
GROUP BY m.id, m.name, m.category, m.reorder_level
HAVING COALESCE(SUM(inv.quantity), 0) <= m.reorder_level
ORDER BY current_stock ASC;

-- Doctor performance (30-day window)
CREATE VIEW vw_doctor_performance AS
SELECT
    d.id,
    d.first_name || ' ' || d.last_name AS doctor_name,
    d.specialization,
    COUNT(a.id)                                          AS total_appointments,
    COUNT(a.id) FILTER (WHERE a.status = 'completed')   AS completed,
    COUNT(a.id) FILTER (WHERE a.status = 'no_show')     AS no_shows,
    ROUND(
        COUNT(a.id) FILTER (WHERE a.status = 'completed') * 100.0
        / NULLIF(COUNT(a.id), 0), 1
    )                                                    AS completion_rate_pct
FROM doctors d
LEFT JOIN appointments a
    ON a.doctor_id = d.id
   AND a.appointment_time >= NOW() - INTERVAL '30 days'
GROUP BY d.id, d.first_name, d.last_name, d.specialization
ORDER BY completed DESC;

-- Expiring medicines (next 90 days)
CREATE VIEW vw_expiring_medicines AS
SELECT
    m.name,
    m.category,
    inv.batch_number,
    inv.quantity,
    inv.expiry_date,
    (inv.expiry_date - CURRENT_DATE) AS days_until_expiry
FROM medicine_inventory inv
JOIN medicines m ON m.id = inv.medicine_id
WHERE inv.expiry_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '90 days'
  AND inv.quantity > 0
ORDER BY inv.expiry_date;

-- =============================================================
-- UTILITY — auto-update updated_at on row change
-- =============================================================
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

DO $$
DECLARE tbl TEXT;
BEGIN
    FOREACH tbl IN ARRAY ARRAY['users','patients','doctors','appointments','medicines']
    LOOP
        EXECUTE FORMAT(
            'CREATE TRIGGER trg_%s_updated_at
             BEFORE UPDATE ON %s
             FOR EACH ROW EXECUTE FUNCTION set_updated_at()',
            tbl, tbl
        );
    END LOOP;
END;
$$;
