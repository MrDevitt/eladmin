package me.zhengjie.modules.keyuan.domain;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SysShouldReceiveData {

    private List<Map<String, String>> tableData;
}
