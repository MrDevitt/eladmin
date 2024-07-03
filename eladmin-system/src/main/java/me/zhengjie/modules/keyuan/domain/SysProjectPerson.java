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
@Table(name = "sys_project_person")
public class SysProjectPerson implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    @ApiModelProperty(value = "id")
    private Long id;

    @Column(name = "`name`", nullable = false)
    @NotBlank
    @ApiModelProperty(value = "姓名")
    private String name;

    @Column(name = "`phone_number`", nullable = false)
    @NotBlank
    @ApiModelProperty(value = "手机号")
    private String phoneNumber;

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

    public void copy(SysProjectPerson source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
