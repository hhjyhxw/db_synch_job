package com.icloud.sync.valueobject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DbDataSync {
    private Integer syncId;
    private String parentSyncId;
    private Integer dbId;
    private String fromTableName;
    private String fromSql;
    private String toTableName;
    private String createTableSql;
    private String createFile;
    private String indexFields;
    private Integer isDelete;
    private String deleteSql;
    private String paramSql;
    private Integer syncType;
    private String scheduledTime;
    private String bak;
    private Integer isValid;
    
    private List<Integer> parentSyncIds;
    
    public boolean finishedParent(Integer syncId) {
        if (this.parentSyncIds == null) {
            if (this.parentSyncId != null && !"".equals(this.parentSyncId.trim())) {
                this.parentSyncIds = new CopyOnWriteArrayList<Integer>();
                String[] ids = this.parentSyncId.split(",");
                for (String id : ids) {
                    if (id != null && !"".equals(id.trim())) {
                        this.parentSyncIds.add(new Integer(id.trim()));
                    }
                }
            }
        }
        if (this.parentSyncIds.contains(syncId)) {
            this.parentSyncIds.remove(syncId);
            if (this.parentSyncIds.isEmpty()) {
                return true;
            }
        }
        return false;
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

    public void setCreateTableSql(String createTableSql) {
        this.createTableSql = createTableSql;
    }

    public String getCreateFile() {
        return createFile;
    }

    public void setCreateFile(String createFile) {
        this.createFile = createFile;
    }

    public String getIndexFields() {
        return indexFields;
    }

    public void setIndexFields(String indexFields) {
        this.indexFields = indexFields;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
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

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getBak() {
        return bak;
    }

    public void setBak(String bak) {
        this.bak = bak;
    }

    public Integer getIsValid() {
        return isValid;
    }

    public void setIsValid(Integer isValid) {
        this.isValid = isValid;
    }

    public String toString() {
        return "DbDataSync [syncId=" + syncId + ", dbId=" + dbId + ", fromTableName=" + fromTableName + ", fromSql=" + fromSql
                + ", toTableName=" + toTableName + ", createTableSql=" + createTableSql + ", createFile=" + createFile + ", isDelete="
                + isDelete + ", deleteSql=" + deleteSql + ", syncType=" + syncType + ", scheduledTime=" + scheduledTime + ", bak=" + bak
                + "]";
    }

    public String getParamSql() {
        return paramSql;
    }

    public void setParamSql(String paramSql) {
        this.paramSql = paramSql;
    }

}
