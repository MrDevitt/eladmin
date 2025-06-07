package me.zhengjie.modules.keyuan.domain.statistics;

import lombok.Data;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailDto;
import me.zhengjie.utils.PageResult;
import org.springframework.beans.BeanUtils;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;

@Data
public class InvoicedNotReceiveData {

    private List<Row> rows;
    private PageResult<Detail> sysProjectDetailDtoPageResult;

    @Data
    public static class Row {
        private String name;
        private String amount;

        public static Comparator<Row> COMPARATOR = Comparator.comparingDouble(e -> Double.parseDouble(e.getAmount()));
    }

    public static class Detail {

        private Long id;

        /**
         * 项目类型 0-检测，1-监理，2-设计
         */
        private Integer projectType;

        /**
         * 项目名
         */
        private String projectName;

        /**
         * 甲方名称
         */
        private String partyA;

        /**
         * 乙方名称
         */
        private String partyB;

        /**
         * 合同编号
         */
        private String contractNumber;

        /**
         * 合同金额
         */
        private Integer contractAmount;

        /**
         * 业务人员
         */
        private Long salesPerson;

        /**
         * 甲方负责人
         */
        private Long partyAPerson;

        /**
         * 发票类型 0-专票，1-普票
         */
        private Integer invoiceType;

        /**
         * 备注
         */
        private String remark;

        /**
         * 业务中心百分比
         */
        private Integer salesPercent;

        /**
         * 技术中心百分比
         */
        private Integer technicalPercent;

        /**
         * 管理中心百分比
         */
        private Integer managementPercent;

        /**
         * 总裁办百分比
         */
        private Integer presidentPercent;

        /**
         * 收款金额
         */
        private Integer receiveAmount;

        /**
         * 记录创建的时间
         */
        private Timestamp createTime;

        /**
         * 记录修改的时间
         */
        private Timestamp updateTime;

        private String projectRegion;

        /**
         * 签订时间
         */
        private Timestamp contractTime;

        /**
         * 合同付款方式 0-签合同50，完工结清；1-一次性付清；2-签合同30进度50付30完工结清；3-按进度拨付
         */
        private Integer contractPayWay;

        /**
         * 应收款金额
         */
        private Integer shouldReceiveAmount;

        /**
         * 项目进度
         */
        private Integer projectProgress;

        private Integer invoicedAmount;

        public Detail(SysProjectDetailDto dto, Integer invoicedAmount) {
            BeanUtils.copyProperties(dto, this);
            this.invoicedAmount = invoicedAmount;
        }
    }
}
