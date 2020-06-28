package com.icloud.sync;

import com.icloud.sync.valueobject.DbDataSync;

public interface SyncCallback {
    void callback(DbDataSync dbDataSync);
}
