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

/**
 * @author MrDevitt
 * @website https://eladmin.vip
 * @date 2024-04-04
 **/
@Data
public class SysProjectDetailQueryCriteria {

    @Query
    private Long id;

    @Query(propName = "contractTime", type = Query.Type.LESS_THAN)
    private Timestamp endContractTime;

    @Query(propName = "contractTime", type = Query.Type.GREATER_THAN)
    private Timestamp beginContractTime;
}