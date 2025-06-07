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
package me.zhengjie.modules.keyuan.repository;

import me.zhengjie.modules.keyuan.domain.SysProjectReceive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author MrDevitt
 * @website https://eladmin.vip
 * @date 2024-07-03
 **/
public interface SysProjectReceiveRepository extends JpaRepository<SysProjectReceive, Long>, JpaSpecificationExecutor<SysProjectReceive> {
    @Query(value = "SELECT *\n" +
            "FROM sys_project_receive\n" +
            "WHERE project_id IN (\n" +
            "    SELECT project_id\n" +
            "    FROM sys_project_receive\n" +
            "    GROUP BY project_id\n" +
            "    HAVING SUM(invoice_amount) > SUM(receive_amount));", nativeQuery = true)
    List<SysProjectReceive> findInvoicedNotReceive();
}