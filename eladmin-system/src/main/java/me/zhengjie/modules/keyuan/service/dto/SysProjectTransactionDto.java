/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.zhengjie.modules.keyuan.service.dto;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author MrDevitt
 * @website https://eladmin.vip
 * @description /
 * @date 2025-11-11
 **/
@Data
public class SysProjectTransactionDto implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 摘要
     */
    private String comment;

    /**
     * 金额
     */
    private Long amount;

    /**
     * 交易类型
     */
    private Integer direction;

    /**
     * 科目编号（关联业务人、部门）
     */
    private Long accountNumber;

    /**
     * 银行账号编号（关联银行账户）
     */
    private Long bankNumber;


    /**
     * 记账凭证编号
     */
    private String certificateNumber;

    /**
     * 交易时间
     */
    private Timestamp transactionTime;

    private Long projectReceiveId;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 修改时间
     */
    private Timestamp updateTime;
}