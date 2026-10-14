-- Local MySQL schema captured on 2026-09-28; isolated integration tests only.

CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `age` int NOT NULL,
  `body_weight_kg` decimal(5,2) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `users_chk_1` CHECK ((`age` >= 0)),
  CONSTRAINT `users_chk_2` CHECK ((`body_weight_kg` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `exercises` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `barbell` tinyint(1) NOT NULL DEFAULT '0',
  `equipment` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `pattern` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `muscle` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `exercise_muscles` (
  `exercise_id` bigint NOT NULL,
  `muscle_id` bigint NOT NULL,
  PRIMARY KEY (`exercise_id`,`muscle_id`),
  KEY `fk_exercise_muscles_muscle` (`muscle_id`),
  CONSTRAINT `fk_exercise_muscles_exercise` FOREIGN KEY (`exercise_id`) REFERENCES `exercises` (`id`),
  CONSTRAINT `fk_exercise_muscles_muscle` FOREIGN KEY (`muscle_id`) REFERENCES `muscle` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `workout_sessions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `started_at` datetime NOT NULL,
  `finished_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_workout_sessions_user` (`user_id`),
  CONSTRAINT `fk_workout_sessions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `workout_sets` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL,
  `exercise_id` bigint NOT NULL,
  `exercise_order` int NOT NULL DEFAULT '1',
  `set_number` int NOT NULL,
  `weight_kg` decimal(6,2) NOT NULL,
  `reps` int NOT NULL,
  `note` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_workout_sets_session` (`session_id`),
  KEY `fk_workout_sets_exercise` (`exercise_id`),
  CONSTRAINT `fk_workout_sets_exercise` FOREIGN KEY (`exercise_id`) REFERENCES `exercises` (`id`),
  CONSTRAINT `fk_workout_sets_session` FOREIGN KEY (`session_id`) REFERENCES `workout_sessions` (`id`),
  CONSTRAINT `workout_sets_chk_1` CHECK ((`set_number` > 0)),
  CONSTRAINT `workout_sets_chk_2` CHECK ((`weight_kg` >= 0)),
  CONSTRAINT `workout_sets_chk_3` CHECK ((`reps` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
