-- ============================================================
--  HostelFinder – XAMPP / MySQL setup script
--
--  Database : hostel_app
--  Host     : localhost:3306   (XAMPP default)
--  User     : root             (XAMPP default, no password)
--
--  How to import:
--    1. Start XAMPP and make sure MySQL is running.
--    2. Open phpMyAdmin (http://localhost/phpmyadmin).
--    3. Click "Import", choose this file and click "Go".
--       – OR –
--    2. Open a terminal and run:
--       mysql -u root < hostel_finder.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS hostel_app
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE hostel_app;

-- ============================================================
--  Table: students
--  Stores all users regardless of role (STUDENT / OWNER / ADMIN)
-- ============================================================
CREATE TABLE IF NOT EXISTS students (
    id               INT          NOT NULL AUTO_INCREMENT,
    full_name        VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL,
    password         VARCHAR(255) NOT NULL,          -- BCrypt hash
    phone            VARCHAR(20),
    university       VARCHAR(255),
    role             VARCHAR(50)  NOT NULL DEFAULT 'STUDENT', -- STUDENT | OWNER | ADMIN
    is_verified      BOOLEAN      NOT NULL DEFAULT FALSE,
    profile_image    LONGTEXT,
    verification_otp VARCHAR(10),
    otp_expiry       TIMESTAMP    NULL DEFAULT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_students_email (email),
    INDEX idx_students_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
--  Table: hostels
-- ============================================================
CREATE TABLE IF NOT EXISTS hostels (
    id            INT          NOT NULL AUTO_INCREMENT,
    owner_id      INT          NOT NULL,
    name          VARCHAR(255) NOT NULL,
    location      VARCHAR(255) NOT NULL,
    description   TEXT,
    amenities     TEXT,
    image_url     VARCHAR(500),
    rating        DOUBLE       NOT NULL DEFAULT 0,
    total_reviews INT          NOT NULL DEFAULT 0,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_hostels_owner_id  (owner_id),
    INDEX idx_hostels_is_active (is_active),
    INDEX idx_hostels_rating    (rating),
    CONSTRAINT fk_hostels_owner FOREIGN KEY (owner_id)
        REFERENCES students (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
--  Table: rooms
-- ============================================================
CREATE TABLE IF NOT EXISTS rooms (
    id              INT          NOT NULL AUTO_INCREMENT,
    hostel_id       INT          NOT NULL,
    room_number     VARCHAR(50)  NOT NULL,
    room_type       VARCHAR(100),                    -- e.g. Single, Double, Dorm
    price_per_month DOUBLE       NOT NULL,
    is_available    BOOLEAN      NOT NULL DEFAULT TRUE,
    capacity        INT          NOT NULL DEFAULT 1,
    description     TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_rooms_hostel_id    (hostel_id),
    INDEX idx_rooms_is_available (is_available),
    INDEX idx_rooms_price        (price_per_month),
    CONSTRAINT fk_rooms_hostel FOREIGN KEY (hostel_id)
        REFERENCES hostels (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
--  Table: bookings
-- ============================================================
CREATE TABLE IF NOT EXISTS bookings (
    id              INT         NOT NULL AUTO_INCREMENT,
    student_id      INT         NOT NULL,
    room_id         INT         NOT NULL,
    hostel_id       INT         NOT NULL,
    check_in_date   DATE        NOT NULL,
    check_out_date  DATE        NOT NULL,
    total_price     DOUBLE      NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'CONFIRMED', -- CONFIRMED | CANCELLED
    payment_method  VARCHAR(100),
    booked_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_bookings_student_id (student_id),
    INDEX idx_bookings_room_id    (room_id),
    INDEX idx_bookings_hostel_id  (hostel_id),
    INDEX idx_bookings_status     (status),
    INDEX idx_bookings_booked_at  (booked_at),
    CONSTRAINT fk_bookings_student FOREIGN KEY (student_id)
        REFERENCES students (id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_room    FOREIGN KEY (room_id)
        REFERENCES rooms    (id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_hostel  FOREIGN KEY (hostel_id)
        REFERENCES hostels  (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
--  Table: reviews
-- ============================================================
CREATE TABLE IF NOT EXISTS reviews (
    id         INT       NOT NULL AUTO_INCREMENT,
    student_id INT       NOT NULL,
    hostel_id  INT       NOT NULL,
    rating     INT       NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment    TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_reviews_student_hostel (student_id, hostel_id), -- one review per student per hostel
    INDEX idx_reviews_hostel_id  (hostel_id),
    INDEX idx_reviews_created_at (created_at),
    CONSTRAINT fk_reviews_student FOREIGN KEY (student_id)
        REFERENCES students (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_hostel  FOREIGN KEY (hostel_id)
        REFERENCES hostels  (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
--  Table: notifications
-- ============================================================
CREATE TABLE IF NOT EXISTS notifications (
    id         INT          NOT NULL AUTO_INCREMENT,
    student_id INT          NOT NULL,
    title      VARCHAR(255),
    message    TEXT,
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_notifications_student_id (student_id),
    INDEX idx_notifications_is_read    (is_read),
    INDEX idx_notifications_created_at (created_at),
    CONSTRAINT fk_notifications_student FOREIGN KEY (student_id)
        REFERENCES students (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
--  Table: saved_hostels  (many-to-many: students ↔ hostels)
-- ============================================================
CREATE TABLE IF NOT EXISTS saved_hostels (
    student_id INT       NOT NULL,
    hostel_id  INT       NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (student_id, hostel_id),
    INDEX idx_saved_hostels_hostel_id (hostel_id),
    CONSTRAINT fk_saved_hostels_student FOREIGN KEY (student_id)
        REFERENCES students (id) ON DELETE CASCADE,
    CONSTRAINT fk_saved_hostels_hostel  FOREIGN KEY (hostel_id)
        REFERENCES hostels  (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
--  Default admin account
--  Password: admin123  (BCrypt hash)
--  ⚠ IMPORTANT: Change this password immediately after first login,
--  or create the admin account manually and remove this INSERT.
-- ============================================================
INSERT IGNORE INTO students
    (full_name, email, password, role, is_verified)
VALUES
    ('Administrator',
     'admin@hostelfinder.com',
     '$2a$12$XO/m1d7xjpAHFSX6fMrmHeGcPAz7xMgL5K2P0mHo6f4.I7BPY8.tS',
     'ADMIN',
     TRUE);
