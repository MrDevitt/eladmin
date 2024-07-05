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
 * @date 2024-04-04
 **/
@Data
public class SysProjectDetailDto implements Serializable {

    private Long id;

    /**
     * 项目类型 0-检测，1-监理，2-设计
     */
    private Integer projectType;

    /**
     * 项目名
     */
    private String projectName;

    /**
     * 项目地址
     */
    private String projectRegion;

    /**
     * 甲方名称
     */
    private String partyA;

    /**
     * 乙方名称
     */
    private String partyB;

    /**
     * 合同编号
     */
    private String contractNumber;

    /**
     * 合同金额
     */
    private Integer contractAmount;

    /**
     * 签订时间
     */
    private Timestamp contractTime;

    /**
     * 开工时间
     */
    private Timestamp projectStartTime;

    /**
     * 竣工时间
     */
    private Timestamp projectFinishTime;

    /**
     * 业务人员
     */
    private Long salesPerson;

    /**
     * 技术人员
     */
    private Long technicalPerson;

    /**
     * 甲方负责人
     */
    private Long partyAPerson;

    /**
     * 甲方领导
     */
    private Long partyALeader;

    /**
     * 发票类型 0-专票，1-普票
     */
    private Integer invoiceType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 业务中心百分比
     */
    private Integer salesPercent;

    /**
     * 技术中心百分比
     */
    private Integer technicalPercent;

    /**
     * 管理中心百分比
     */
    private Integer managementPercent;

    /**
     * 总裁办百分比
     */
    private Integer presidentPercent;

    /**
     * 收款金额
     */
    private Integer receiveAmount;

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