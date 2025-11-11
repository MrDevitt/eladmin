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
package me.zhengjie.modules.keyuan.service;

import me.zhengjie.modules.keyuan.domain.SysProjectTransaction;
import me.zhengjie.modules.keyuan.domain.statistics.transaction.SummaryData;
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectTransactionDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectTransactionQueryCriteria;
import me.zhengjie.utils.PageResult;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author MrDevitt
 * @website https://eladmin.vip
 * @description 服务接口
 * @date 2025-11-11
 **/
public interface SysProjectTransactionService {

    /**
     * 查询数据分页
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return Map<String, Object>
     */
    PageResult<SysProjectTransactionDto> queryAll(SysProjectTransactionQueryCriteria criteria, Pageable pageable);

    /**
     * 查询所有数据不分页
     *
     * @param criteria 条件参数
     * @return List<SysProjectTransactionDto>
     */
    List<SysProjectTransactionDto> queryAll(SysProjectTransactionQueryCriteria criteria);

    /**
     * 根据ID查询
     *
     * @param id ID
     * @return SysProjectTransactionDto
     */
    SysProjectTransactionDto findById(Long id);

    /**
     * 创建
     *
     * @param resources /
     */
    void create(SysProjectTransaction resources);

    /**
     * 编辑
     *
     * @param resources /
     */
    void update(SysProjectTransaction resources);

    /**
     * 多选删除
     *
     * @param ids /
     */
    void deleteAll(Long[] ids);

    /**
     * 导出数据
     *
     * @param all      待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<SysProjectTransactionDto> all, HttpServletResponse response) throws IOException;

    List<SummaryData> getTransactionSummary(Timestamp begin, Timestamp end);


    void updateTransactionByReceive(SysProjectReceiveDto receive);
}