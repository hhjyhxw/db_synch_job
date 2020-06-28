package com.icloud.sync;

import org.apache.log4j.Logger;

import com.icloud.sync.valueobject.DbDataInfo;
import com.icloud.sync.valueobject.DbDataSync;
import com.icloud.task.Task;

public class SyncTask extends Task {
    private Logger logger = Logger.getLogger(getClass());
    private DataSyncDao dao;
    private DbDataInfo dbDataInfo;
    private DbDataSync dbDataSync;
    private SyncCallback syncCallback;

    public SyncTask(DataSyncDao dao, DbDataInfo dbDataInfo, DbDataSync dbDataSync, SyncCallback syncCallback) {
        this.dao = dao;
        this.dbDataInfo = dbDataInfo;
        this.dbDataSync = dbDataSync;
        this.syncCallback = syncCallback;
    }

    @Override
    public int compareTo(Task task) {
        return this.hashCode() - (task == null ? 0 : task.hashCode());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dbDataSync == null) ? 0 : dbDataSync.getSyncId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SyncTask other = (SyncTask) obj;
        if (dbDataSync == null) {
            if (other.dbDataSync != null) {
                return false;
            }
        } else if (!dbDataSync.getSyncId().equals(other.dbDataSync.getSyncId())) {
            return false;
        }
        return true;
    }

    @Override
    public void doTask() {
        try {
            new DataSyncTransfer(this.dao, this.dbDataInfo, this.dbDataSync, null).doSync();
        } catch (Exception e) {
            this.logger.error("任务同步失败：" + this.dbDataSync.getSyncId() + "！", e);
        }
    }

    @Override
    public void finished() {
        this.syncCallback.callback(this.dbDataSync);
    }

    @Override
    public String toString() {
        return "SyncTask[syncId: " + (this.dbDataSync == null ? null : this.dbDataSync.getSyncId()) + "]";
    }
}
