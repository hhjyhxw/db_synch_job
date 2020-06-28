package com.icloud.sync.table;

import java.util.Map;

public class SqlParam {
    private String sql;
    private Map<Integer, String> param;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Map<Integer, String> getParam() {
        return param;
    }

    public void setParam(Map<Integer, String> param) {
        this.param = param;
    }
}
