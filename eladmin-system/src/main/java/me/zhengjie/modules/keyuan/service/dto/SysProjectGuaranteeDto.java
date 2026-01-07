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
 * @date 2025-04-11
 **/
@Data
public class SysProjectGuaranteeDto implements Serializable {

    public static int STATUS_NORMAL = 0;
    public static int STATUS_ABNORMAL = 1;
    public static int STATUS_COMPLETE = 2;

    private Long id;

    /**
     * 担保状态 0-担保中,1-担保逾期,2-担保完成
     */
    private Integer status;

    /**
     * 项目类型 0-检测，1-监理，2-设计
     */
    private Integer projectType;

    /**
     * 项目名
     */
    private String projectName;

    /**
     * 甲方名称
     */
    private String partyA;

    /**
     * 甲方联系人
     */
    private Long partyAPerson;

    /**
     * 担保金额
     */
    private Long guaranteeAmount;

    /**
     * 担保结束时间
     */
    private Timestamp guaranteeTime;

    /**
     * 担保人员
     */
    private Long guaranteePerson;

    /**
     * 备注
     */
    private String remark;

    /**
     * 记录创建的时间
     */
    private Timestamp createTime;

    /**
     * 记录修改的时间
     */
    private Timestamp updateTime;
}