DROP DATABASE IF EXISTS edutest;
CREATE DATABASE edutest;

-- 2. ОБОВ'ЯЗКОВО вказуємо, що працюватимемо саме в ній
USE edutest;

DELETE FROM questions WHERE test_id IN (SELECT id FROM tests WHERE title LIKE '%(Сесія)%');
DELETE FROM tests WHERE title LIKE '%(Сесія)%';

-- 3. Створюємо таблицю користувачів
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL
);

-- 4. Створюємо таблицю тестів
CREATE TABLE tests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    time_limit INT,
    test_code VARCHAR(50) NOT NULL UNIQUE,
    author_email VARCHAR(255),
    subject VARCHAR(255),
    grade VARCHAR(50),
    category VARCHAR(255),
    target_audience VARCHAR(255),
    hide_answers BOOLEAN DEFAULT FALSE,
    published BOOLEAN DEFAULT FALSE
);

-- 5. Створюємо таблицю питань
CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    test_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    image_url VARCHAR(500),
    video_url VARCHAR(500),
    option_a VARCHAR(255) NOT NULL,
    option_b VARCHAR(255) NOT NULL,
    option_c VARCHAR(255),
    option_d VARCHAR(255),
    correct_answer VARCHAR(255) NOT NULL,
    points INT DEFAULT 1,
    CONSTRAINT fk_question_test FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE CASCADE
);

-- 6. Створюємо таблицю результатів
CREATE TABLE results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT,
    test_id BIGINT NOT NULL,
    percentage DOUBLE NOT NULL,
    CONSTRAINT fk_result_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_result_test FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE CASCADE
);