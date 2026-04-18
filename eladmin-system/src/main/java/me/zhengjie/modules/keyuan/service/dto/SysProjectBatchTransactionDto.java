package me.zhengjie.modules.keyuan.service.dto;

import cn.hutool.core.bean.BeanUtil;
import lombok.Data;
import me.zhengjie.modules.keyuan.domain.SysProjectTransaction;

import java.util.ArrayList;
import java.util.List;

@Data
public class SysProjectBatchTransactionDto extends SysProjectTransactionDto {

    private List<Detail> details;

    public SysProjectTransaction toDomain() {
        if (details == null || details.size() != 1) {
            throw new RuntimeException("子项数据仅能有一项，实际项数:" + (details == null ? 0 : details.size()));
        }
        SysProjectTransaction res = new SysProjectTransaction();
        BeanUtil.copyProperties(this, res);

        Detail detail = details.get(0);
        res.setComment(detail.getComment());
        res.setAmount(detail.getAmount());
        res.setDirection(detail.getDirection());
        res.setAccountNumber(detail.getAccountNumber());
        res.setBankNumber(detail.getBankNumber());
        res.setRemark(detail.getRemark());
        return res;
    }

    public List<SysProjectTransaction> toDomainList() {
        List<SysProjectTransaction> res = new ArrayList<>();
        for (Detail detail : details) {
            SysProjectTransaction transaction = new SysProjectTransaction();
            BeanUtil.copyProperties(this, transaction);

            transaction.setComment(detail.getComment());
            transaction.setAmount(detail.getAmount());
            transaction.setDirection(detail.getDirection());
            transaction.setAccountNumber(detail.getAccountNumber());
            transaction.setBankNumber(detail.getBankNumber());
            transaction.setRemark(detail.getRemark());
            res.add(transaction);
        }
        return res;
    }

    @Data
    public static class Detail {
        private String comment;
        private Long amount;
        private Integer direction;
        private Long accountNumber;
        private Long bankNumber;
        private String remark;
    }

}
