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
package me.zhengjie.modules.keyuan.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author MrDevitt
 * @website https://eladmin.vip
 * @description /
 * @date 2024-04-04
 **/
@Entity
@Data
@Table(name = "sys_project_detail")
public class SysProjectDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    @ApiModelProperty(value = "id")
    private Long id;

    @Column(name = "`project_type`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "项目类型 0-检测，1-监理，2-设计")
    private Integer projectType;

    @Column(name = "`project_name`", nullable = false)
    @NotBlank
    @ApiModelProperty(value = "项目名")
    private String projectName;

    @Column(name = "`party_a`", nullable = false)
    @NotBlank
    @ApiModelProperty(value = "甲方名称")
    private String partyA;

    @Column(name = "`party_b`", nullable = false)
    @NotBlank
    @ApiModelProperty(value = "乙方名称")
    private String partyB;

    @Column(name = "`contract_number`", unique = true, nullable = false)
    @NotBlank
    @ApiModelProperty(value = "合同编号")
    private String contractNumber;

    @Column(name = "`contract_amount`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "合同金额")
    private Integer contractAmount;

    @Column(name = "`contact_time`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "签订时间")
    private Timestamp contactTime;

    @Column(name = "`project_start_time`")
    @ApiModelProperty(value = "开工时间")
    private Timestamp projectStartTime;

    @Column(name = "`project_finish_time`")
    @ApiModelProperty(value = "竣工时间")
    private Timestamp projectFinishTime;

    @Column(name = "`sales_person`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "业务人员")
    private Long salesPerson;

    @Column(name = "`technical_person`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "技术人员")
    private Long technicalPerson;

    @Column(name = "`party_a_person`")
    @ApiModelProperty(value = "甲方负责人")
    private Long partyAPerson;

    @Column(name = "`party_a_leader`")
    @ApiModelProperty(value = "甲方领导")
    private Long partyALeader;

    @Column(name = "`invoice_type`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "发票类型 0-专票，1-普票")
    private Integer invoiceType;

    @Column(name = "`remark`")
    @ApiModelProperty(value = "备注")
    private String remark;

    @Column(name = "`sales_percent`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "业务中心百分比")
    private Integer salesPercent;

    @Column(name = "`technical_percent`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "技术中心百分比")
    private Integer technicalPercent;

    @Column(name = "`management_percent`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "管理中心百分比")
    private Integer managementPercent;

    @Column(name = "`president_percent`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "总裁办百分比")
    private Integer presidentPercent;

    @Column(name = "`receive_amount`")
    @ApiModelProperty(value = "收款金额")
    private Integer receiveAmount;

    @Column(name = "`is_deleted`")
    @ApiModelProperty(value = "0-未删除，1-已删除")
    private Integer isDeleted;

    @Column(name = "`create_time`")
    @CreationTimestamp
    @ApiModelProperty(value = "记录创建的时间")
    private Timestamp createTime;

    @Column(name = "`update_time`")
    @UpdateTimestamp
    @ApiModelProperty(value = "记录修改的时间")
    private Timestamp updateTime;

    public void copy(SysProjectDetail source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }

    public int totalPercentage() {
        return salesPercent + technicalPercent + managementPercent + presidentPercent;
    }
}
