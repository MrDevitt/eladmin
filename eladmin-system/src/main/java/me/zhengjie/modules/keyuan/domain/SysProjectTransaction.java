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
 * @date 2025-11-11
 **/
@Entity
@Data
@Table(name = "sys_project_transaction")
public class SysProjectTransaction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @Column(name = "`comment`", nullable = false)
    @NotBlank
    @ApiModelProperty(value = "摘要")
    private String comment;

    @Column(name = "`amount`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "金额")
    private Long amount;

    @Column(name = "`direction`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "交易类型")
    private Integer direction;

    @Column(name = "`account_number`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "科目编号（关联业务人、部门）")
    private Long accountNumber;

    @Column(name = "`bank_number`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "银行账号编号（关联银行账户）")
    private Long bankNumber;

    @Column(name = "`certificate_number`", nullable = false)
    @NotBlank
    @ApiModelProperty(value = "记账凭证编号")
    private String certificateNumber;

    @Column(name = "`transaction_time`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "交易时间")
    private Timestamp transactionTime;

    @Column(name = "`project_receive_id`")
    @ApiModelProperty(value = "项目收款id")
    private Long projectReceiveId;

    @Column(name = "`create_by`")
    @ApiModelProperty(value = "创建人")
    private String createBy;

    @Column(name = "`update_by`")
    @ApiModelProperty(value = "修改人")
    private String updateBy;

    @Column(name = "`create_time`")
    @CreationTimestamp
    @ApiModelProperty(value = "创建时间")
    private Timestamp createTime;

    @Column(name = "`update_time`")
    @UpdateTimestamp
    @ApiModelProperty(value = "修改时间")
    private Timestamp updateTime;

    public void copy(SysProjectTransaction source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
