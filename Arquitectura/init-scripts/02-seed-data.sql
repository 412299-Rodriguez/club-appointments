-- Club Los Amigos Training Session Management System
-- Seed Data for Initial Testing
-- MySQL 8.0.0

USE turnero_db;

-- Insert default users
-- Password for all users: Admin123!, Trainer123!, User123! respectively
-- BCrypt hashed with strength 10

INSERT INTO users (full_name, email, password, `role`, is_deleted) VALUES
('Admin Club', 'admin@clublosamigos.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1w2qKPZ8F4tKPLQ5xPkf5h8qW0qW0qW', 'SUPER_ADMIN', FALSE),
('Diego Martínez', 'diego.martinez@clublosamigos.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1w2qKPZ8F4tKPLQ5xPkf5h8qW0qW0qW', 'ENTRENADOR', FALSE),
('Laura Fernández', 'laura.fernandez@clublosamigos.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1w2qKPZ8F4tKPLQ5xPkf5h8qW0qW0qW', 'ENTRENADOR', FALSE),
('Roberto Silva', 'roberto.silva@clublosamigos.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1w2qKPZ8F4tKPLQ5xPkf5h8qW0qW0qW', 'ENTRENADOR', FALSE),
('Juan Pérez', 'juan.perez@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1w2qKPZ8F4tKPLQ5xPkf5h8qW0qW0qW', 'USUARIO', FALSE),
('María González', 'maria.gonzalez@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1w2qKPZ8F4tKPLQ5xPkf5h8qW0qW0qW', 'USUARIO', FALSE),
('Usuario 8527', 'usuario8527@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1w2qKPZ8F4tKPLQ5xPkf5h8qW0qW0qW', 'USUARIO', FALSE);

-- Insert sample training sessions for the current week
-- Note: Adjust dates as needed for testing

-- Friday sessions (viernes, 28 de noviembre)
INSERT INTO training_sessions (name, description, trainer_id, date, start_time, end_time, location, max_participants, status) VALUES
('Entrenamiento técnico - Fundamentos', 'Sesión enfocada en mejorar la técnica individual y fundamentos del fútbol', 2, CURDATE() + INTERVAL (5 - DAYOFWEEK(CURDATE()) + 7) % 7 DAY, '09:00:00', '11:00:00', 'Cancha Principal', 20, 'ACTIVE'),
('Preparación física y resistencia', 'Trabajo de resistencia cardiovascular y fuerza funcional', 3, CURDATE() + INTERVAL (5 - DAYOFWEEK(CURDATE()) + 7) % 7 DAY, '16:00:00', '18:00:00', 'Cancha Auxiliar', 15, 'ACTIVE'),
('Táctica y juego posicional', 'Desarrollo de conceptos tácticos y posicionamiento en el campo', 4, CURDATE() + INTERVAL (5 - DAYOFWEEK(CURDATE()) + 7) % 7 DAY, '19:00:00', '21:00:00', 'Gimnasio Techado', 12, 'ACTIVE');

-- Saturday sessions (sábado, 29 de noviembre)
INSERT INTO training_sessions (name, description, trainer_id, date, start_time, end_time, location, max_participants, status) VALUES
('Entrenamiento técnico - Fundamentos', 'Sesión enfocada en mejorar la técnica individual y fundamentos del fútbol', 2, CURDATE() + INTERVAL (6 - DAYOFWEEK(CURDATE()) + 7) % 7 DAY, '09:00:00', '11:00:00', 'Cancha Principal', 20, 'ACTIVE'),
('Preparación física y resistencia', 'Trabajo de resistencia cardiovascular y fuerza funcional', 3, CURDATE() + INTERVAL (6 - DAYOFWEEK(CURDATE()) + 7) % 7 DAY, '16:00:00', '18:00:00', 'Cancha Auxiliar', 15, 'ACTIVE'),
('Táctica y juego posicional', 'Desarrollo de conceptos tácticos y posicionamiento en el campo', 4, CURDATE() + INTERVAL (6 - DAYOFWEEK(CURDATE()) + 7) % 7 DAY, '19:00:00', '21:00:00', 'Gimnasio Techado', 12, 'ACTIVE');

-- Insert sample bookings
-- Session 1 has 3 bookings (2/20 in the UI mock = confirmed count / total participants who booked)
INSERT INTO bookings (user_id, training_session_id, status) VALUES
(5, 1, 'CONFIRMED'),
(6, 1, 'CONFIRMED'),
(7, 1, 'CONFIRMED');

-- Session 2 has 2 bookings
INSERT INTO bookings (user_id, training_session_id, status) VALUES
(5, 2, 'CONFIRMED'),
(6, 2, 'CONFIRMED');

-- Insert a sample slot configuration
INSERT INTO slot_configurations (name, recurrence_type, days_of_week, start_date, end_date) VALUES
('Tandas Semanales Fundamentos', 'WEEKLY', '1,3,5', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 MONTH));
