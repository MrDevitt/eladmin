package me.zhengjie.modules.keyuan.domain.balance;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BalanceData {
    private List<BalanceTableRow> departmentRows = new ArrayList<>();
    private List<BalanceTableRow> personRows = new ArrayList<>();
}
