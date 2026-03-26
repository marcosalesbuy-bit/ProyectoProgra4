-- ============================================================
--  BOLSA DE EMPLEO — Script SQL completo
--  EIF209 Programación 4 — 2026-01
-- ============================================================

CREATE DATABASE IF NOT EXISTS bolsa_empleo
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE bolsa_empleo;

-- ── Administrador ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS administrador (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    identificacion  VARCHAR(30)  NOT NULL,
    nombre          VARCHAR(150) NOT NULL,
    clave           VARCHAR(255) NOT NULL,   -- BCrypt hash
    PRIMARY KEY (id),
    UNIQUE KEY uq_admin_identificacion (identificacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Empresa ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS empresa (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    nombre              VARCHAR(150) NOT NULL,
    localizacion        VARCHAR(250),
    correo_electronico  VARCHAR(150) NOT NULL,
    telefono            VARCHAR(30),
    descripcion         TEXT,
    clave               VARCHAR(255),           -- NULL until admin approves
    aprobada            TINYINT(1)   NOT NULL DEFAULT 0,
    fecha_registro      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_empresa_correo (correo_electronico)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Oferente ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS oferente (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    identificacion      VARCHAR(30)  NOT NULL,
    nombre              VARCHAR(100) NOT NULL,
    primer_apellido     VARCHAR(100) NOT NULL,
    nacionalidad        VARCHAR(60),
    telefono            VARCHAR(30),
    correo_electronico  VARCHAR(150) NOT NULL,
    lugar_residencia    VARCHAR(200),
    clave               VARCHAR(255),           -- NULL until admin approves
    aprobado            TINYINT(1)   NOT NULL DEFAULT 0,
    cv_path             VARCHAR(500),
    fecha_registro      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_oferente_correo (correo_electronico),
    UNIQUE KEY uq_oferente_id (identificacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Característica (jerárquica) ──────────────────────────────
CREATE TABLE IF NOT EXISTS caracteristica (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    nombre    VARCHAR(120) NOT NULL,
    padre_id  BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_caracteristica_padre
        FOREIGN KEY (padre_id) REFERENCES caracteristica (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Puesto ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS puesto (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    empresa_id       BIGINT         NOT NULL,
    descripcion      TEXT           NOT NULL,
    salario_ofrecido DECIMAL(10,2),
    es_publico       TINYINT(1)     NOT NULL DEFAULT 1,
    activo           TINYINT(1)     NOT NULL DEFAULT 1,
    fecha_registro   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_puesto_empresa
        FOREIGN KEY (empresa_id) REFERENCES empresa (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Requisito de puesto ──────────────────────────────────────
CREATE TABLE IF NOT EXISTS requisito_puesto (
    id                BIGINT NOT NULL AUTO_INCREMENT,
    puesto_id         BIGINT NOT NULL,
    caracteristica_id BIGINT NOT NULL,
    nivel_requerido   TINYINT NOT NULL CHECK (nivel_requerido BETWEEN 1 AND 5),
    PRIMARY KEY (id),
    UNIQUE KEY uq_requisito (puesto_id, caracteristica_id),
    CONSTRAINT fk_req_puesto
        FOREIGN KEY (puesto_id) REFERENCES puesto (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_req_caracteristica
        FOREIGN KEY (caracteristica_id) REFERENCES caracteristica (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Habilidad del oferente ───────────────────────────────────
CREATE TABLE IF NOT EXISTS habilidad_oferente (
    id                BIGINT NOT NULL AUTO_INCREMENT,
    oferente_id       BIGINT NOT NULL,
    caracteristica_id BIGINT NOT NULL,
    nivel             TINYINT NOT NULL CHECK (nivel BETWEEN 1 AND 5),
    PRIMARY KEY (id),
    UNIQUE KEY uq_habilidad (oferente_id, caracteristica_id),
    CONSTRAINT fk_hab_oferente
        FOREIGN KEY (oferente_id) REFERENCES oferente (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_hab_caracteristica
        FOREIGN KEY (caracteristica_id) REFERENCES caracteristica (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  DATOS INICIALES
-- ============================================================

-- Admin por defecto
--   identificacion: admin
--   clave (plain): admin1234
--   hash BCrypt generado con strength=10
INSERT IGNORE INTO administrador (identificacion, nombre, clave)
VALUES (
    'admin',
    'Administrador del Sistema',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lXSm'
);

-- ── Características de ejemplo ───────────────────────────────

-- Raíces
INSERT IGNORE INTO caracteristica (id, nombre, padre_id) VALUES
(1,  'Bases de Datos',           NULL),
(2,  'Ciberseguridad',           NULL),
(3,  'Lenguajes de programación',NULL),
(4,  'Tecnologías Web',          NULL),
(5,  'Testing',                  NULL),
(6,  'Modelado',                 NULL);

-- Bases de Datos → Motores
INSERT IGNORE INTO caracteristica (id, nombre, padre_id) VALUES
(7,  'Motores',  1);

INSERT IGNORE INTO caracteristica (id, nombre, padre_id) VALUES
(8,  'MySQL',    7),
(9,  'Oracle',   7),
(10, 'PostgreSQL',7),
(11, 'SQL Server',7);

-- Lenguajes de programación
INSERT IGNORE INTO caracteristica (id, nombre, padre_id) VALUES
(12, 'C#',     3),
(13, 'Java',   3),
(14, 'Kotlin', 3),
(15, 'Python', 3);

-- Tecnologías Web
INSERT IGNORE INTO caracteristica (id, nombre, padre_id) VALUES
(16, 'HTML',       4),
(17, 'CSS',        4),
(18, 'JavaScript', 4),
(19, 'Spring Boot',4),
(20, 'Thymeleaf',  4);

-- Testing → JUnit
INSERT IGNORE INTO caracteristica (id, nombre, padre_id) VALUES
(21, 'JUnit', 5);

INSERT IGNORE INTO caracteristica (id, nombre, padre_id) VALUES
(22, 'Assertions', 21),
(23, 'Test cases', 21);

-- Ciberseguridad
INSERT IGNORE INTO caracteristica (id, nombre, padre_id) VALUES
(24, 'OWASP',     2),
(25, 'Pentesting',2);

-- Modelado (raíz) → UML
INSERT IGNORE INTO caracteristica (id, nombre, padre_id) VALUES
(26, 'UML', 6);
