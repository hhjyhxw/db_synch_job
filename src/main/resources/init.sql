-- mysql 初始化开始...
--drop table t_cfg_dbdata_info;
create table t_cfg_dbdata_info(
    db_id int not null auto_increment comment '数据库连接信息主键',
    db_user varchar(100) not null comment '连接数据库使用的用户名',
    db_password varchar(100) not null comment '连接数据库使用的密码，使用DesEncrypter.java类加密解密',
    db_driver varchar(100) not null comment 'jdbc连接时使用的连接驱动类',
    db_tns varchar(4000) not null comment '数据库链接tns',
    bak varchar(4000) comment '备注',
    primary key(db_id)
) comment '数据库连接信息';

--drop table t_cfg_dbdata_sync;
create table t_cfg_dbdata_sync(
    sync_id int not null auto_increment comment '同步主键，自增',
    parent_sync_id varchar(100) comment '等待完成的同步主键ID，多个以逗号分割',
    db_id int not null comment '与t_cfg_db_info关联',
    from_table_name varchar(1000) comment '源表，包括库名，如：cmdb.activity，可使用变量{}，如：cmdb.activity{yyyymmdd}',
    from_sql varchar(4000) comment '同步时使用的sql，不为空时使用此sql查询进行同步，查询出来的字段必须与to_table_name所有的字段一一对应，可使用变量{}',
    to_table_name varchar(1000) not null comment '目的表，包括库名，如：cmdb.activity，可使用变量{}，如：cmdb.activity{yyyymmdd}',
    create_table_sql varchar(4000) comment '目标库的表不存在时使用此sql进行建表，可使用变量{}，如：cmdb.activity{yyyymmdd}',
    create_file varchar(100) comment '超过10w记录时保存的文件路径，绝对路径，可使用变量{}，如：cmdb.activity{yyyymmdd}',
    index_fields varchar(100) comment '索引列，多个用逗号分割',
    is_delete int comment '是否清除原有数据：1、清除，0、不清除',
    delete_sql varchar(4000) comment '清除数据使用的sql',
    sync_type int not null comment '同步方式：1、手动同步，什么时候使用什么时候手动调用，2、调度同步，待开发',
    scheduled_time varchar(100) comment '调度时间',
    param_sql varchar(4000) comment '参数获取',
    bak varchar(4000) comment '备注',
    is_valid int commend '是否有效',
    primary key(sync_id)
) comment '数据库数据同步表，将t_cfg_db_info表配置的数据库从源表同步到目的表中';

--drop table t_cfg_dbdata_sync_his;
create table t_cfg_dbdata_sync_his(
    sync_his_id int not null auto_increment comment '同步主键，自增',
    sync_id int not null comment '与t_cfg_dbdata_sync关联',
    parent_sync_id varchar(100) comment '等待完成的同步主键ID，多个以逗号分割',
    db_id int not null comment '与t_cfg_db_info关联',
    from_table_name varchar(1000) comment '源表',
    from_sql varchar(4000) comment '同步时使用的sql',
    to_table_name varchar(1000) not null comment '目的表',
    create_table_sql varchar(4000) comment '建表语句',
    delete_sql varchar(4000) comment '清除数据使用的sql',
    sync_type int not null comment '同步方式：1、手动同步，什么时候使用什么时候手动调用，2、调度同步，待开发',
    status int comment '同步状态：1、同步中，2、同步出错，3、同步完成',
    params varchar(4000) comment '同步时使用的参数json格式',
    error text comment '同步出错内容',
    start_time datetime comment '同步开始时间',
    end_time datetime comment '同步结束时间',
    duration int comment '同步时长，单位：秒',
    total_count int comment '同步的记录数',
    primary key(sync_his_id)
) comment '数据同步历史记录';
-- mysql 初始化结束.

-- oracle 初始化开始...
--drop table t_cfg_dbdata_info;
create table t_cfg_dbdata_info(
    db_id int not null ,
    db_user varchar(100) not null ,
    db_password varchar(100) not null ,
    db_driver varchar(100) not null ,
    db_tns varchar(4000) not null ,
    bak varchar(4000) ,
    primary key(db_id)
);
comment on column t_cfg_dbdata_info.db_id is '数据库连接信息主键';
comment on column t_cfg_dbdata_info.db_user is '连接数据库使用的用户名';
comment on column t_cfg_dbdata_info.db_password is '连接数据库使用的密码，使用DesEncrypter.java类加密解密';
comment on column t_cfg_dbdata_info.db_driver is 'jdbc连接时使用的连接驱动类';
comment on column t_cfg_dbdata_info.db_tns is '数据库链接tns';
comment on column t_cfg_dbdata_info.bak is '备注';
comment on table t_cfg_dbdata_info  is  '数据库连接信息';

--drop table t_cfg_dbdata_sync;
create table t_cfg_dbdata_sync(
    sync_id int not null,
    parent_sync_id varchar(100),
    db_id int not null,
    from_table_name varchar(1000),
    from_sql varchar(4000),
    to_table_name varchar(1000) not null,
    create_table_sql varchar(4000),
    create_file varchar(100),
    index_fields varchar(100),
    is_delete int,
    delete_sql varchar(4000),
    sync_type int not null,
    scheduled_time varchar(100),
    param_sql varchar(4000),
    bak varchar(4000),
    is_valid int,
    primary key(sync_id)
);
comment on column t_cfg_dbdata_sync.sync_id is '同步主键，自增';
comment on column t_cfg_dbdata_sync.parent_sync_id is '等待完成的同步主键ID，多个以逗号分割';
comment on column t_cfg_dbdata_sync.db_id is '与t_cfg_db_info关联';
comment on column t_cfg_dbdata_sync.from_table_name is '源表，包括库名，如：cmdb.activity，可使用变量{}，如：cmdb.activity{yyyymmdd}';
comment on column t_cfg_dbdata_sync.from_sql is '同步时使用的sql，不为空时使用此sql查询进行同步，查询出来的字段必须与to_table_name所有的字段一一对应，可使用变量{}';
comment on column t_cfg_dbdata_sync.to_table_name is '目的表，包括库名，如：cmdb.activity，可使用变量{}，如：cmdb.activity{yyyymmdd}';
comment on column t_cfg_dbdata_sync.create_table_sql is '目标库的表不存在时使用此sql进行建表，可使用变量{}，如：cmdb.activity{yyyymmdd}';
comment on column t_cfg_dbdata_sync.create_file is '超过10w记录时保存的文件路径，绝对路径，可使用变量{}，如：cmdb.activity{yyyymmdd}';
comment on column t_cfg_dbdata_sync.index_fields is '索引列，多个用逗号分割';
comment on column t_cfg_dbdata_sync.is_delete is '是否清除原有数据：1、清除，0、不清除';
comment on column t_cfg_dbdata_sync.delete_sql is '清除数据使用的sql';
comment on column t_cfg_dbdata_sync.sync_type is '同步方式：1、手动同步，什么时候使用什么时候手动调用，2、调度同步，待开发';
comment on column t_cfg_dbdata_sync.scheduled_time is '调度时间';
comment on column t_cfg_dbdata_sync.param_sql is '参数获取';
comment on column t_cfg_dbdata_sync.bak is '备注';
comment on column t_cfg_dbdata_sync.is_valid is '是否有效';
comment on table t_cfg_dbdata_sync is '数据库数据同步表，将t_cfg_db_info表配置的数据库从源表同步到目的表中';
    
--drop table t_cfg_dbdata_sync_his;
create table t_cfg_dbdata_sync_his(
    sync_his_id int not null,
    sync_id int not null,
    parent_sync_id varchar(100),
    db_id int not null,
    from_table_name varchar(1000),
    from_sql varchar(4000),
    to_table_name varchar(1000) not null,
    create_table_sql varchar(4000),
    delete_sql varchar(4000),
    sync_type int not null,
    status int,
    params varchar(4000),
    error clob,
    start_time date,
    end_time date,
    duration int,
    total_count int,
    primary key(sync_his_id)
);
comment on column t_cfg_dbdata_sync_his.sync_his_id is '同步主键，自增';
comment on column t_cfg_dbdata_sync_his.sync_id is '与t_cfg_dbdata_sync关联';
comment on column t_cfg_dbdata_sync_his.db_id is '与t_cfg_db_info关联';
comment on column t_cfg_dbdata_sync_his.from_table_name is '源表';
comment on column t_cfg_dbdata_sync_his.from_sql is '同步时使用的sql';
comment on column t_cfg_dbdata_sync_his.to_table_name is '目的表';
comment on column t_cfg_dbdata_sync_his.create_table_sql is '建表语句';
comment on column t_cfg_dbdata_sync_his.delete_sql is '清除数据使用的sql';
comment on column t_cfg_dbdata_sync_his.sync_type is '同步方式：1、手动同步，什么时候使用什么时候手动调用，2、调度同步，待开发';
comment on column t_cfg_dbdata_sync_his.status is '同步状态：1、同步中，2、同步出错，3、同步完成';
comment on column t_cfg_dbdata_sync_his.params is '同步时使用的参数json格式';
comment on column t_cfg_dbdata_sync_his.error is '同步出错内容';
comment on column t_cfg_dbdata_sync_his.start_time is '同步开始时间';
comment on column t_cfg_dbdata_sync_his.end_time is '同步结束时间';
comment on column t_cfg_dbdata_sync_his.duration is '同步时长，单位：秒';
comment on column t_cfg_dbdata_sync_his.total_count is '同步的记录数';
comment on table t_cfg_dbdata_sync_his is '数据同步历史记录';

create sequence SEQ_CFG_DBDATA_SYNC_HIS increment by 1 start with 1 maxvalue 99999999999999999 nocycle nocache noorder;

-- oracle 初始化结束