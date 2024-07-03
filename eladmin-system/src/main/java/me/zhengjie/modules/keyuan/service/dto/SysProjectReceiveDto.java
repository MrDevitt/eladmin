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
 * @date 2024-07-03
 **/
@Data
public class SysProjectReceiveDto implements Serializable {

    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 开票金额
     */
    private Integer invoiceAmount;

    /**
     * 开票时间
     */
    private Timestamp invoiceTime;

    /**
     * 到账金额
     */
    private Integer receiveAmount;

    /**
     * 到账时间
     */
    private Timestamp receiveTime;

    /**
     * 0-未删除，1-已删除
     */
    private Integer isDeleted;

    /**
     * 记录创建的时间
     */
    private Timestamp createTime;

    /**
     * 记录修改的时间
     */
    private Timestamp updateTime;
}