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
@Table(name = "sys_project_account")
public class SysProjectAccount implements Serializable {

    @Id
    @Column(name = "`account_number`", nullable = false)
    @ApiModelProperty(value = "科目编号")
    private Long accountNumber;

    @Column(name = "`description`", nullable = false)
    @NotBlank
    @ApiModelProperty(value = "科目含义")
    private String description;

    @Column(name = "`parent`")
    @ApiModelProperty(value = "上级科目")
    private Long parent;

    @Column(name = "`initial_amount`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "初始余额")
    private Integer initialAmount;

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

    public void copy(SysProjectAccount source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(false));
    }
}
