package me.zhengjie.modules.keyuan.domain.balance;

import lombok.Data;

@Data
public class AccountBalanceData {

    long initialBalance = 0L;
    long expenseLast = 0L;
    long expenseThisMonth = 0L;
    long expenseThisYear = 0L;

}
