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
 * @date 2025-04-11
 **/
@Entity
@Data
@Table(name = "sys_project_guarantee")
public class SysProjectGuarantee implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    @ApiModelProperty(value = "id")
    private Long id;

    @Column(name = "`status`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "担保状态 0-担保中,1-担保逾期,2-担保完成")
    private Integer status;

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

    @Column(name = "`party_a_person`")
    @ApiModelProperty(value = "甲方联系人")
    private Long partyAPerson;

    @Column(name = "`guarantee_amount`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "担保金额")
    private Long guaranteeAmount;

    @Column(name = "`guarantee_time`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "担保结束时间")
    private Timestamp guaranteeTime;

    @Column(name = "`guarantee_person`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "担保人员")
    private Long guaranteePerson;

    @Column(name = "`remark`")
    @ApiModelProperty(value = "备注")
    private String remark;

    @Column(name = "`create_time`")
    @CreationTimestamp
    @ApiModelProperty(value = "记录创建的时间")
    private Timestamp createTime;

    @Column(name = "`update_time`")
    @UpdateTimestamp
    @ApiModelProperty(value = "记录修改的时间")
    private Timestamp updateTime;

    public void copy(SysProjectGuarantee source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
