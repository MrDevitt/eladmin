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
import me.zhengjie.annotation.Query;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author MrDevitt
 * @website https://eladmin.vip
 * @date 2025-11-11
 **/
@Data
public class SysProjectTransactionQueryCriteria {

    /**
     * 模糊
     */
    @Query(type = Query.Type.INNER_LIKE)
    private String comment;

    /**
     * 精确
     */
    @Query
    private Integer direction;

    /**
     * 精确
     */
    @Query
    private Long accountNumber;

    @Query(type = Query.Type.IN, propName = "accountNumber")
    private List<Long> accountNumberList;

    /**
     * 精确
     */
    @Query
    private Long bankNumber;

    @Query(type = Query.Type.IN, propName = "bankNumber")
    private List<Long> bankNumberList;

    /**
     * 模糊
     */
    @Query(type = Query.Type.INNER_LIKE)
    private String certificateNumber;

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> transactionTime;

    @Query
    private Long projectReceiveId;

    @Query(type = Query.Type.IN, propName = "projectReceiveId")
    private List<Long> projectReceiveIds;


    private Long parentAccountNumber;

    private Boolean blackListEnable;

    @Query(type = Query.Type.NOT_IN, propName = "id")
    private List<Long> IdsNotIn;
}