CREATE TABLE IF NOT EXISTS STUDENT
(
    `id`           int AUTO_INCREMENT NOT NULL UNIQUE,
    `first_name`   varchar(100)       NOT NULL,
    `last_name`    varchar(100)       NOT NULL,
    `dob`          date,
    `phone`        int,
    `created_date` TIMESTAMP default current_timestamp,
    `updated_date` TIMESTAMP default current_timestamp,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS SUBJECT
(
    `id`           int AUTO_INCREMENT NOT NULL UNIQUE,
    `subject_name` VARCHAR(45),
    `tutor`        VARCHAR(45),
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS EXAM_RESULT
(
    `id`         int AUTO_INCREMENT NOT NULL UNIQUE,
    `student_id` int REFERENCES student (`id`) ON UPDATE CASCADE ON DELETE CASCADE,
    `subject_id` int REFERENCES subject (`id`) ON UPDATE CASCADE,
    `mark`       integer            NOT NULL,
    PRIMARY KEY (`id`)
)

