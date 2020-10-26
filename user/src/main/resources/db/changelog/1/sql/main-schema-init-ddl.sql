------------------------------
--      CREATE SCHEMA       --
------------------------------
CREATE SCHEMA main;
ALTER SCHEMA main OWNER TO noah;

------------------------------
--      CREATE TABLES       --
------------------------------

CREATE TABLE main.users
(
    id          BIGINT PRIMARY KEY                DEFAULT shard.next_id(),
    create_dttm TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    login       VARCHAR(100)             NOT NULL,
    password    VARCHAR(250)             NOT NULL
);

----------------------------
--      OWNER SETUP       --
----------------------------
ALTER TABLE main.users
    OWNER TO noah;

-------------------------------------------------------------
-------------------------------------------------------------
------                 COMMENT SETUP                   ------
-------------------------------------------------------------
-------------------------------------------------------------

--------------------------------------
--      EXTERNAL_SYSTEM_LOGIN       --
--------------------------------------
COMMENT ON TABLE main.users
    IS 'Text';

COMMENT ON COLUMN main.users.id
    IS 'Text';