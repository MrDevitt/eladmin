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
 * @date 2024-04-04
 **/
@Data
public class SysProjectDetailQueryCriteria {

    @Query
    private Long id;

    @Query(type = Query.Type.IN, propName = "id")
    private List<Long> ids;

    @Query(type = Query.Type.NOT_IN, propName = "id")
    private List<Long> idsNotIn;

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> contractTime;

    /**
     * 模糊
     */
    @Query(type = Query.Type.INNER_LIKE)
    private String projectName;

    /**
     * 模糊
     */
    @Query
    private Long salesPerson;

    @Query(type = Query.Type.NOT_IN, propName = "salesPerson")
    private List<Long> salesPersonNotIn;

    @Query
    private Integer projectType;

    @Query
    private String projectRegion;

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;

    @Query(type = Query.Type.GREATER_THAN_NQ)
    private Integer shouldReceiveAmount;

    @Query(type = Query.Type.INNER_LIKE)
    private String contractNumber;

    @Query(type = Query.Type.BETWEEN)
    private List<Integer> projectProgress;

    @Query
    private String partyB;

    @Query(type = Query.Type.IN)
    private List<Integer> contractPayWay;

    @Query(type = Query.Type.GREATER_THAN_COLUMN)
    private String receiveAmount;

    @Query(type = Query.Type.LESS_THAN_NQ_COLUMN, propName = "receiveAmount")
    private String receiveAmountLess;

    private List<Timestamp> receiveTime;

    //0-无合同，1-无明细表，2-有合同，3-有明细表
    private List<Integer> attachmentStatus;

    private String idsStr;

    private Boolean receiveFinished;

    @Query(type = Query.Type.INNER_LIKE)
    private String partyA;
}