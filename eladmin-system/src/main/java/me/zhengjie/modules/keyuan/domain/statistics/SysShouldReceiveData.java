package me.zhengjie.modules.keyuan.domain.statistics;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SysShouldReceiveData {

    private List<Map<String, String>> tableData;

}
