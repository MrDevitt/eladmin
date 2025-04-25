package me.zhengjie.modules.keyuan.domain.balance;

import lombok.Data;

@Data
public class AccountBalanceData {

    long initialBalance = 0L;
    long expenseLast = 0L;
    long expenseThisMonth = 0L;
    long expenseThisYear = 0L;

    public static AccountBalanceData add(AccountBalanceData a, AccountBalanceData b) {
        AccountBalanceData res = new AccountBalanceData();
        res.setInitialBalance(a.getInitialBalance() + b.getInitialBalance());
        res.setExpenseLast(a.getExpenseLast() + b.getExpenseLast());
        res.setExpenseThisYear(a.getExpenseThisYear() + b.getExpenseThisYear());
        res.setExpenseThisMonth(a.getExpenseThisMonth() + b.getExpenseThisMonth());
        return res;
    }
}
