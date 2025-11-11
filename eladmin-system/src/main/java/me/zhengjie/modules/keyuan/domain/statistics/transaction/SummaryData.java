package me.zhengjie.modules.keyuan.domain.statistics.transaction;

import lombok.Data;

import java.util.List;

@Data
public class SummaryData {
    private Long accountNumber;
    private String name;
    private long initialAmount;
    private long beginIncome;
    private long beginExpense;
    private long beginRemain;
    private long currentIncome;
    private long currentExpense;
    private long currentRemain;
    private long endIncome;
    private long endExpense;
    private long endRemain;
    private List<SummaryData> children;

    public SummaryData add(SummaryData other) {
        this.initialAmount += other.initialAmount;
        this.beginIncome += other.beginIncome;
        this.beginExpense += other.beginExpense;
        this.currentIncome += other.currentIncome;
        this.currentExpense += other.currentExpense;
        this.endIncome += other.endIncome;
        this.endExpense += other.endExpense;
        return this;
    }

    public void calcRemain() {
        this.beginRemain = this.beginIncome - this.beginExpense + this.initialAmount;
        this.currentRemain = this.currentIncome - this.currentExpense;
        this.endRemain = this.endIncome - this.endExpense + this.initialAmount;
    }
}
