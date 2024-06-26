CREATE TABLE IF NOT EXISTS qrtz_job_details (
    sched_name        varchar(120) not null,
    job_name          varchar(200) not null,
    job_group         varchar(200) not null,
    description       varchar(250),
    job_class_name    varchar(250) not null,
    is_durable        boolean      not null,
    is_nonconcurrent  boolean      not null,
    is_update_data    boolean      not null,
    requests_recovery boolean      not null,
    job_data          bytea,
    primary key (sched_name, job_name, job_group)
);

CREATE TABLE IF NOT EXISTS qrtz_triggers (
    sched_name     varchar(120) not null,
    trigger_name   varchar(200) not null,
    trigger_group  varchar(200) not null,
    job_name       varchar(200) not null,
    job_group      varchar(200) not null,
    description    varchar(250),
    next_fire_time bigint,
    prev_fire_time bigint,
    priority       integer,
    trigger_state  varchar(16)  not null,
    trigger_type   varchar(8)   not null,
    start_time     bigint       not null,
    end_time       bigint,
    calendar_name  varchar(200),
    misfire_instr  smallint,
    job_data       bytea,
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, job_name, job_group) references qrtz_job_details
);

CREATE TABLE IF NOT EXISTS qrtz_blob_triggers (
    sched_name    varchar(120) not null,
    trigger_name  varchar(200) not null,
    trigger_group varchar(200) not null,
    blob_data     bytea,
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers
);

CREATE TABLE IF NOT EXISTS qrtz_calendars (
    sched_name    varchar(120) not null,
    calendar_name varchar(200) not null,
    calendar      bytea        not null,
    primary key (sched_name, calendar_name)
);

CREATE TABLE IF NOT EXISTS qrtz_cron_triggers (
    sched_name      varchar(120) not null,
    trigger_name    varchar(200) not null,
    trigger_group   varchar(200) not null,
    cron_expression varchar(120) not null,
    time_zone_id    varchar(80),
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers
);

CREATE TABLE IF NOT EXISTS qrtz_fired_triggers (
    sched_name        varchar(120) not null,
    entry_id          varchar(95)  not null,
    trigger_name      varchar(200) not null,
    trigger_group     varchar(200) not null,
    instance_name     varchar(200) not null,
    fired_time        bigint       not null,
    sched_time        bigint       not null,
    priority          integer      not null,
    state             varchar(16)  not null,
    job_name          varchar(200),
    job_group         varchar(200),
    is_nonconcurrent  boolean,
    requests_recovery boolean,
    primary key (sched_name, entry_id)
);

CREATE TABLE IF NOT EXISTS qrtz_locks (
    sched_name varchar(120) not null,
    lock_name  varchar(40)  not null,
    primary key (sched_name, lock_name)
);

CREATE TABLE IF NOT EXISTS qrtz_paused_trigger_grps (
    sched_name    varchar(120) not null,
    trigger_group varchar(200) not null,
    primary key (sched_name, trigger_group)
);

CREATE TABLE IF NOT EXISTS qrtz_scheduler_state (
    sched_name        varchar(120) not null,
    instance_name     varchar(200) not null,
    last_checkin_time bigint       not null,
    checkin_interval  bigint       not null,
    primary key (sched_name, instance_name)
);

CREATE TABLE IF NOT EXISTS qrtz_simple_triggers (
    sched_name      varchar(120) not null,
    trigger_name    varchar(200) not null,
    trigger_group   varchar(200) not null,
    repeat_count    bigint       not null,
    repeat_interval bigint       not null,
    times_triggered bigint       not null,
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers
);

CREATE TABLE IF NOT EXISTS qrtz_simprop_triggers (
    sched_name    varchar(120) not null,
    trigger_name  varchar(200) not null,
    trigger_group varchar(200) not null,
    str_prop_1    varchar(512),
    str_prop_2    varchar(512),
    str_prop_3    varchar(512),
    int_prop_1    integer,
    int_prop_2    integer,
    long_prop_1   bigint,
    long_prop_2   bigint,
    dec_prop_1    numeric(13, 4),
    dec_prop_2    numeric(13, 4),
    bool_prop_1   boolean,
    bool_prop_2   boolean,
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers
);

CREATE INDEX IF NOT EXISTS idx_qrtz_t_j
    ON qrtz_triggers (sched_name, job_name, job_group);

CREATE INDEX IF NOT EXISTS idx_qrtz_t_jg
    ON qrtz_triggers (sched_name, job_group);

CREATE INDEX IF NOT EXISTS idx_qrtz_t_c
    ON qrtz_triggers (sched_name, calendar_name);

CREATE INDEX IF NOT EXISTS idx_qrtz_t_g
    ON qrtz_triggers (sched_name, trigger_group);

CREATE INDEX IF NOT EXISTS idx_qrtz_t_state
    ON qrtz_triggers (sched_name, trigger_state);

CREATE INDEX IF NOT EXISTS idx_qrtz_t_n_state
    ON qrtz_triggers (sched_name, trigger_name, trigger_group, trigger_state);

CREATE INDEX IF NOT EXISTS idx_qrtz_t_n_g_state
    ON qrtz_triggers (sched_name, trigger_group, trigger_state);

CREATE INDEX IF NOT EXISTS idx_qrtz_t_next_fire_time
    ON qrtz_triggers (sched_name, next_fire_time);

CREATE INDEX IF NOT EXISTS idx_qrtz_t_nft_st
    ON qrtz_triggers (sched_name, trigger_state, next_fire_time);

CREATE INDEX IF NOT EXISTS idx_qrtz_t_nft_misfire
    ON qrtz_triggers (sched_name, misfire_instr, next_fire_time);

CREATE INDEX IF NOT EXISTS idx_qrtz_t_nft_st_misfire
    ON qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_state);

CREATE INDEX IF NOT EXISTS idx_qrtz_t_nft_st_misfire_grp
    ON qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_group, trigger_state);

ALTER TABLE qrtz_blob_triggers
    OWNER TO emir;

ALTER TABLE qrtz_calendars
    OWNER TO emir;

ALTER TABLE qrtz_cron_triggers
    OWNER TO emir;

ALTER TABLE qrtz_fired_triggers
    OWNER TO emir;

ALTER TABLE qrtz_job_details
    OWNER TO emir;

ALTER TABLE qrtz_locks
    OWNER TO emir;

ALTER TABLE qrtz_paused_trigger_grps
    OWNER TO emir;

ALTER TABLE qrtz_scheduler_state
    OWNER TO emir;

ALTER TABLE qrtz_simple_triggers
    OWNER TO emir;

ALTER TABLE qrtz_simprop_triggers
    OWNER TO emir;

ALTER TABLE qrtz_triggers
    OWNER TO emir;
