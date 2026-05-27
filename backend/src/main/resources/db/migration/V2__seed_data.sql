INSERT INTO users (email, password_hash, name, surname)
VALUES
(
    'student@test.com',
    '$2a$10$G.4FrgIfCwRmGUadzgvv4OXj42LTfSSqdwxKMzokeZfn8dX1D3tYK',
    'Test',
    'Student'
);

INSERT INTO rooms (number, name, capacity, description, is_active)
VALUES
('101', 'Кабинет математики', 25, 'Обычный учебный кабинет', TRUE),
('204', 'Кабинет информатики', 20, 'Кабинет с компьютерами', TRUE),
('305', 'Лекционный кабинет', 35, 'Большой кабинет для занятий', TRUE),
('410', 'Малый кабинет', 12, 'Небольшой кабинет для групповой работы', TRUE);

INSERT INTO bookings (room_id, user_id, start_time, end_time, status)
VALUES
(1, 1, '2026-05-21T10:00:00', '2026-05-21T11:30:00', 'ACTIVE'),
(2, 1, '2026-05-21T14:00:00', '2026-05-21T15:00:00', 'ACTIVE'),
(3, 1, '2026-05-21T16:30:00', '2026-05-21T18:00:00', 'ACTIVE');