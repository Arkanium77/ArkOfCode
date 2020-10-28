------------------------------
--      CREATE SCHEMA       --
------------------------------
CREATE SCHEMA main;
ALTER SCHEMA main OWNER TO noah;

------------------------------
--      CREATE TABLES       --
------------------------------
CREATE TABLE main.role
(
    name        VARCHAR(20) PRIMARY KEY,
    create_dttm TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE main.user
(
    id                BIGINT PRIMARY KEY                DEFAULT shard.next_id(),
    create_dttm       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    modify_dttm       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    login             VARCHAR(25)              NOT NULL UNIQUE,
    password          VARCHAR(50)              NOT NULL,
    token_verify_code UUID                     NOT NULL,
    banned            BOOLEAN                  NOT NULL DEFAULT FALSE,
    role              VARCHAR(20)
        CONSTRAINT user_role_constraint_fk REFERENCES main.role
);

create unique index user_table_login_uindex
    on main.user (login);

----------------------------
--      OWNER SETUP       --
----------------------------
ALTER TABLE main.user
    OWNER TO noah;

ALTER TABLE main.role
    OWNER TO noah;

-------------------------------------------------------------
-------------------------------------------------------------
------                 COMMENT SETUP                   ------
-------------------------------------------------------------
-------------------------------------------------------------

---------------------
--      USER       --
---------------------
COMMENT ON TABLE main.user
    IS 'Таблица содержащая данные пользователей';

COMMENT ON COLUMN main.user.id
    IS 'Идентификатор пользователя';
COMMENT ON COLUMN main.user.create_dttm
    IS 'Время создания записи';
COMMENT ON COLUMN main.user.modify_dttm
    IS 'Время последнего изменения записи';
COMMENT ON COLUMN main.user.login
    IS 'Логин пользователя';
COMMENT ON COLUMN main.user.password
    IS 'Пароль (шифрованный) пользователя';
COMMENT ON COLUMN main.user.role
    IS 'Роль пользователя';

---------------------
--      ROLE       --
---------------------
COMMENT ON TABLE main.role
    IS 'Таблица содержащая уровни доступа (роли)';

COMMENT ON COLUMN main.role.name
    IS 'Название роли (уникальное)';
COMMENT ON COLUMN main.role.create_dttm
    IS 'Время создания роли';