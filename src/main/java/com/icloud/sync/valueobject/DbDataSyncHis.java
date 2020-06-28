package com.icloud.sync.valueobject;

import java.util.Date;

public class DbDataSyncHis {
    /** 状态：1、开始 */
    public static Integer STATUS_START = new Integer(1);
    /** 状态：2、结束状态（出错） */
    public static Integer STATUS_ERROR = new Integer(2);
    /** 状态：3、结束状态（出错） */
    public static Integer STATUS_FINISH = new Integer(3);

    /** 调度方式：1、手工方式 */
    public static Integer SYNC_TYPE_MANUAL = new Integer(1);

    private Integer syncHisId;
    private Integer syncId;
    private String parentSyncId;
    private Integer dbId;
    private String fromTableName;
    private String fromSql;
    private String toTableName;
    private String createTableSql;
    private String createFile;
    private String deleteSql;
    private String paramSql;
    private Integer syncType;
    private Integer status;
    private String params;
    private String error;
    private Date startTime;
    private Date endTime;
    private Integer duration;
    private Integer totalCount;

    public Integer getSyncHisId() {
        return syncHisId;
    }

    public void setSyncHisId(Integer syncHisId) {
        this.syncHisId = syncHisId;
    }

    public Integer getSyncId() {
        return syncId;
    }

    public void setSyncId(Integer syncId) {
        this.syncId = syncId;
    }

    public String getParentSyncId() {
        return parentSyncId;
    }

    public void setParentSyncId(String parentSyncId) {
        this.parentSyncId = parentSyncId;
    }

    public Integer getDbId() {
        return dbId;
    }

    public void setDbId(Integer dbId) {
        this.dbId = dbId;
    }

    public String getFromTableName() {
        return fromTableName;
    }

    public void setFromTableName(String fromTableName) {
        this.fromTableName = fromTableName;
    }

    public String getFromSql() {
        return fromSql;
    }

    public void setFromSql(String fromSql) {
        this.fromSql = fromSql;
    }

    public String getToTableName() {
        return toTableName;
    }

    public void setToTableName(String toTableName) {
        this.toTableName = toTableName;
    }

    public String getCreateTableSql() {
        return createTableSql;
    }

    public String getCreateFile() {
        return createFile;
    }

    public void setCreateFile(String createFile) {
        this.createFile = createFile;
    }

    public void setCreateTableSql(String createTableSql) {
        this.createTableSql = createTableSql;
    }

    public String getDeleteSql() {
        return deleteSql;
    }

    public void setDeleteSql(String deleteSql) {
        this.deleteSql = deleteSql;
    }

    public Integer getSyncType() {
        return syncType;
    }

    public void setSyncType(Integer syncType) {
        this.syncType = syncType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
        if (this.startTime != null && this.endTime != null) {
            this.duration = new Integer((int) ((this.endTime.getTime() - this.startTime.getTime()) / 1000));
        }
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public String toString() {
        return "DbDataSyncHis [syncHisId=" + syncHisId + ", syncId=" + syncId + ", dbId=" + dbId + ", fromTableName=" + fromTableName
                + ", fromSql=" + fromSql + ", toTableName=" + toTableName + ", createTableSql=" + createTableSql + ", createFile="
                + createFile + ", deleteSql=" + deleteSql + ", syncType=" + syncType + ", status=" + status + ", params=" + params
                + ", error=" + error + ", startTime=" + startTime + ", endTime=" + endTime + ", duration=" + duration + ", totalCount="
                + totalCount + "]";
    }

    public String getParamSql() {
        return paramSql;
    }

    public void setParamSql(String paramSql) {
        this.paramSql = paramSql;
    }

}
