package me.zhengjie.modules.keyuan.domain.statistics.balance;

import lombok.Data;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Data
public class ProjectDepartment {

    private String name;
    private List<String> accountNumberList;
    private long initialBalance;
    private Function<SysProjectDetailDto, Integer> percentageGetter;
    private Map<Integer, long[]> receiveByType = new HashMap<>();
    private Map<Integer, Map<String, long[]>> receiveByTypeAndRegion = new HashMap<>();

    private List<ProjectDepartment> children;
    private Function<ProjectDepartment, Map<Integer, long[]>> dataCalculator;

    public ProjectDepartment(String name) {
        this.name = name;
    }

    public ProjectDepartment(ProjectDepartment other) {
        this.name = other.getName();
        this.initialBalance = other.getInitialBalance();
        this.percentageGetter = other.getPercentageGetter();
        this.dataCalculator = other.dataCalculator;

        // 深拷贝 accountNumberList
        if (other.accountNumberList != null) {
            this.accountNumberList = new ArrayList<>(other.accountNumberList);
        }

        // 深拷贝 receiveByType
        if (other.receiveByType != null) {
            this.receiveByType = new HashMap<>();
            for (Map.Entry<Integer, long[]> entry : other.receiveByType.entrySet()) {
                this.receiveByType.put(entry.getKey(), Arrays.copyOf(entry.getValue(), entry.getValue().length));
            }
        }

        // 深拷贝 receiveByTypeAndRegion
        if (other.receiveByTypeAndRegion != null) {
            this.receiveByTypeAndRegion = new HashMap<>();
            for (Map.Entry<Integer, Map<String, long[]>> outerEntry : other.receiveByTypeAndRegion.entrySet()) {
                Map<String, long[]> innerMapCopy = new HashMap<>();
                for (Map.Entry<String, long[]> innerEntry : outerEntry.getValue().entrySet()) {
                    innerMapCopy.put(innerEntry.getKey(), Arrays.copyOf(innerEntry.getValue(), innerEntry.getValue().length));
                }
                this.receiveByTypeAndRegion.put(outerEntry.getKey(), innerMapCopy);
            }
        }

        // 深拷贝 children（递归）
        if (other.children != null) {
            this.children = new ArrayList<>();
            for (ProjectDepartment child : other.children) {
                this.children.add(new ProjectDepartment(child));
            }
        }
    }
}
