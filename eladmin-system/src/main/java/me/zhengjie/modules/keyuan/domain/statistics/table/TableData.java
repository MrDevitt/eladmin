package me.zhengjie.modules.keyuan.domain.statistics.table;

import lombok.Data;
import me.zhengjie.modules.keyuan.utils.ProjectUtils;

@Data
public class TableData {
    private String name;
    private double month1;
    private double month2;
    private double month3;
    private double month4;
    private double month5;
    private double month6;
    private double month7;
    private double month8;
    private double month9;
    private double month10;
    private double month11;
    private double month12;
    private double year;

    public TableData(String name, double[] data) {
        this.name = name;
        month1 = ProjectUtils.roundFix2(data[0]);
        month2 = ProjectUtils.roundFix2(data[1]);
        month3 = ProjectUtils.roundFix2(data[2]);
        month4 = ProjectUtils.roundFix2(data[3]);
        month5 = ProjectUtils.roundFix2(data[4]);
        month6 = ProjectUtils.roundFix2(data[5]);
        month7 = ProjectUtils.roundFix2(data[6]);
        month8 = ProjectUtils.roundFix2(data[7]);
        month9 = ProjectUtils.roundFix2(data[8]);
        month10 = ProjectUtils.roundFix2(data[9]);
        month11 = ProjectUtils.roundFix2(data[10]);
        month12 = ProjectUtils.roundFix2(data[11]);
        year = ProjectUtils.roundFix2(data[12]);
    }

}
