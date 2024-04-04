create table sys_project_detail
(
    `id`                  bigint(11)   not null auto_increment,
    `project_type`        tinyint      not null comment '项目类型 0-检测，1-监理，2-设计',
    `project_name`        varchar(64)  not null comment '项目名',
    `party_a`             varchar(64)  not null comment '甲方名称',
    `party_b`             varchar(64)  not null comment '乙方名称',
    `contract_number`     varchar(32)  not null comment '合同编号',
    `contract_amount`     int          not null comment '合同金额',
    `contact_time`        timestamp    not null comment '签订时间',
    `project_start_time`  timestamp default null comment '开工时间',
    `project_finish_time` timestamp default null comment '竣工时间',
    `sales_person`        bigint(11)   not null comment '业务人员',
    `technical_person`    bigint(11)   not null comment '技术人员',
    `party_a_person`      bigint(11) comment '甲方负责人',
    `party_a_leader`      bigint(11) comment '甲方领导',
    `invoice_type`        tinyint      not null comment '发票类型 0-专票，1-普票',
    `remark`              blob comment '备注',
    `sales_percent`       int unsigned not null comment '业务中心百分比',
    `technical_percent`   int unsigned not null comment '技术中心百分比',
    `management_percent`  int unsigned not null comment '管理中心百分比',
    `president_percent`   int unsigned not null comment '总裁办百分比',
    `receive_amount`      int       default 0 comment '收款金额',
    `is_deleted`          tinyint   default 0 comment '0-未删除，1-已删除',
    `create_time`         timestamp    not null comment '记录创建的时间',
    `update_time`         timestamp    not null comment '记录修改的时间',
    primary key (`id`),
    unique key `idx_contract_number` (`contract_number`),
    key `idx_sales_person` (`sales_person`)
) engine = InnoDB,
  default charset = utf8,comment = '项目明细表';

create table sys_project_person
(
    `id`           bigint(11)  not null auto_increment,
    `name`         varchar(64) not null comment '姓名',
    `phone_number` varchar(16) not null comment '手机号',
    `is_deleted`   tinyint default 0 comment '0-未删除，1-已删除',
    `create_time`  timestamp   not null comment '记录创建的时间',
    `update_time`  timestamp   not null comment '记录修改的时间',
    primary key (`id`),
    key `idx_name` (`name`)
) engine = InnoDB,
  default charset = utf8,comment = '人员信息表';

create table sys_project_receive
(
    `id`             bigint(11) not null auto_increment,
    `project_id`     bigint(11) not null comment '项目ID',
    `invoice_amount` int        not null comment '开票金额',
    `invoice_time`   timestamp  not null comment '开票时间',
    `receive_amount` int       default null comment '到账金额',
    `receive_time`   timestamp default null comment '到账时间',
    `is_deleted`     tinyint   default 0 comment '0-未删除，1-已删除',
    `create_time`    timestamp  not null comment '记录创建的时间',
    `update_time`    timestamp  not null comment '记录修改的时间',
    primary key (`id`),
    key `idx_project_id` (`project_id`)
) engine = InnoDB,
  default charset = utf8,comment = '项目收款进度表';

