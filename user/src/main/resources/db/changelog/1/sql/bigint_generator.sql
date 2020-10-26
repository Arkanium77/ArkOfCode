------------------------------------------------------------- Инициализация шарда
create schema shard;

comment on schema shard is 'schema for unique bigint generator';

alter schema shard owner to noah;

create sequence shard.table_id_seq;

create domain shard.id as bigint;

comment on type shard.id is 'internal ID type';

alter domain shard.id owner to noah;
-------------------------------------------------------------


------------------------------------------------------------- Возвращает идентификатор по timestamp
create or replace function shard.get_id_from_ts(ts bigint) returns shard.id
    language plpgsql
as
$$
DECLARE
    our_epoch  bigint   := 1577836800000;--2020.01.01

    result     shard.id;
    now_millis bigint;
    shard_id   smallint := 1;
BEGIN
    SELECT FLOOR(ts * 1000) INTO now_millis;
    result := (now_millis - our_epoch) << 23;
    result := result | (shard_id << 10);
    RETURN result;
END;
$$;

alter function shard.get_id_from_ts(ts bigint) owner to noah;
-------------------------------------------------------------

------------------------------------------------------------- Возвращает идентификатор по timestamp
create or replace function shard.get_id_from_ts(ts timestamptz) returns shard.id
    language plpgsql
as
$$
DECLARE
    our_epoch  bigint   := 1577836800000;--2020.01.01
    result     shard.id;
    now_millis bigint;
    shard_id   smallint := 1;
BEGIN
    SELECT FLOOR(extract(epoch from ts) * 1000) INTO now_millis;
    result := (now_millis - our_epoch) << 23;
    result := result | (shard_id << 10);
    RETURN result;
END;
$$;

alter function shard.get_id_from_ts(ts timestamptz) owner to noah;
-------------------------------------------------------------

--------------------------------------- Возвращает элемент sequence из идентификатора записи
create function shard.get_sequence(id shard.id) returns smallint
    immutable
    language sql
as
$$
SELECT ((id << 54) >> 54)::smallint;
$$;

comment on function shard.get_sequence(shard.id) is '10 bits from insta5.id representing sequence';

alter function shard.get_sequence(shard.id) owner to noah;
---------------------------------------

--------------------------------------- Возвращает номер шарда из идентификатора записи
create function shard.get_shard(id shard.id) returns smallint
    immutable
    language sql
as
$$
SELECT ((id << 41) >> 51)::smallint;
$$;

comment on function shard.get_shard(shard.id) is '13 bits from insta5.id representing shard';

alter function shard.get_shard(shard.id) owner to noah;
---------------------------------------


--------------------------------------- Возвращает timestamp и идентификатора записи
create function shard.get_ts(id shard.id) returns timestamp without time zone
    immutable
    language sql
as
$$
SELECT to_timestamp(((id >> 23) + 1577836800000) / 1000)::timestamp without time zone;
$$;

comment on function shard.get_ts(shard.id) is '41 bits from insta5.id representing timestamp';

alter function shard.get_ts(shard.id) owner to noah;
---------------------------------------


create function shard.next_id() returns shard.id
    language plpgsql
as
$$
DECLARE
    our_epoch    bigint   := 1577836800000;--2020.01.01
    seq_id       bigint;
    result       shard.id;
    now_millis   bigint;
    shard_id     smallint := 1;
    max_shard_id bigint   := 2048;
BEGIN
    SELECT nextval('shard.table_id_seq') % max_shard_id INTO seq_id;

    SELECT FLOOR(EXTRACT(EPOCH FROM clock_timestamp()) * 1000) INTO now_millis;
    result := (now_millis - our_epoch) << 23;
    result := result | (shard_id << 10);
    result := result | (seq_id);
    RETURN result;
END;
$$;

alter function shard.next_id() owner to noah;