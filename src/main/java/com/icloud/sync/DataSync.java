package com.icloud.sync;

import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

import com.icloud.sync.valueobject.DbDataInfo;
import com.icloud.sync.valueobject.DbDataSync;
import com.icloud.task.TaskManager;
import com.icloud.util.BeanUtil;
import com.icloud.util.DesEncrypter;
import com.icloud.util.PropertyUtil;

public class DataSync implements SyncCallback {
    private Logger logger = Logger.getLogger(getClass());
    
    private Map<Integer, DbDataInfo> dbDataInfos;//数据库连接信息
    private Map<Integer, DbDataSync> dbDataSyncs;//数据库同步任务信息
    private TaskManager taskManager;//任务管理器
    private List<DbDataSync> pendingList = new CopyOnWriteArrayList<DbDataSync>();

    public void doSync() throws Exception {
        this.init();
        this.taskManager = new TaskManager();
        this.taskManager.init();

        // 先加入到等待列表，防止未加到等待列表时任务就已经结束
        for (DbDataSync dbDataSync : this.dbDataSyncs.values()) {
            if (dbDataSync.getParentSyncId() != null && !"".equals(dbDataSync.getParentSyncId())) {
                this.pendingList.add(dbDataSync);
            }
        }

        // 先开始不需要等待父同步id的任务
        for (DbDataSync dbDataSync : this.dbDataSyncs.values()) {
            if (dbDataSync.getParentSyncId() == null || "".equals(dbDataSync.getParentSyncId())) {
                this.taskManager.addTask(new SyncTask(this.createDao(), this.dbDataInfos.get(dbDataSync.getDbId()), dbDataSync, this));
            }
        }
        
        this.waitFinished();
    }
    
    public void waitFinished() throws InterruptedException {
        boolean isFinished = false;
        while(!isFinished) {
            Thread.sleep(10 * 1000); // 等待10s
            this.logger.info("当前等待任务数：" + this.pendingList.size() + "，正在处理的任务数：" + this.taskManager.size());
            isFinished = this.pendingList.isEmpty() && this.taskManager.isIdle();
            if (this.taskManager.isIdle() && !this.pendingList.isEmpty()) {
                isFinished = true;
                this.logger.info("当前等待任务数：" + this.pendingList.size() 
                    + "，正在处理的任务数：" + this.taskManager.size() 
                    + "，无法触发等任务的执行，直接退出，不在执行等待任务！");
            }
        }
        this.taskManager.shutdown();
    }

    /**
     * 任务完成回调
     * 
     * @throws Exception
     */
    @Override
    public void callback(DbDataSync dbDataSync) {
        for (DbDataSync temp : this.pendingList) {
            if (temp.finishedParent(dbDataSync.getSyncId())) {
                try {
                    this.taskManager.addTask(new SyncTask(this.createDao(), this.dbDataInfos.get(temp.getDbId()), temp, this));
                    this.pendingList.remove(temp);
                } catch (Exception e) {
                    this.logger.error("新增任务出错！", e);
                }
            }
        }
    }

    public void init() throws Exception {
        this.initDbDataInfos();//
        this.initDbDataSyncs();//
    }

    /**
     *初始化 同步任务信息
     * @throws Exception
     */
    public void initDbDataSyncs() throws Exception {
        this.dbDataSyncs = new HashMap<Integer, DbDataSync>();
        String sql = "select * from t_cfg_dbdata_sync where is_valid=1";
        List<Map<String, Object>> list = this.createDao().queryList(sql);
        for (Map<String, Object> data : list) {
            DbDataSync dbDataSync = new DbDataSync();
            dbDataSync.setSyncId(BeanUtil.getIntegerValue(data, "SYNC_ID"));
            dbDataSync.setParentSyncId(BeanUtil.getStringValue(data, "PARENT_SYNC_ID"));
            dbDataSync.setDbId(BeanUtil.getIntegerValue(data, "DB_ID"));
            dbDataSync.setFromTableName(BeanUtil.getStringValue(data, "FROM_TABLE_NAME"));
            dbDataSync.setFromSql(BeanUtil.getStringValue(data, "FROM_SQL"));
            dbDataSync.setToTableName(BeanUtil.getStringValue(data, "TO_TABLE_NAME"));
            dbDataSync.setCreateTableSql(BeanUtil.getStringValue(data, "CREATE_TABLE_SQL"));
            dbDataSync.setCreateFile(BeanUtil.getStringValue(data, "CREATE_FILE"));
            dbDataSync.setIndexFields(BeanUtil.getStringValue(data, "INDEX_FIELDS"));
            dbDataSync.setIsDelete(BeanUtil.getIntegerValue(data, "IS_DELETE"));
            dbDataSync.setDeleteSql(BeanUtil.getStringValue(data, "DELETE_SQL"));
            dbDataSync.setParamSql(BeanUtil.getStringValue(data, "PARAM_SQL"));
            dbDataSync.setSyncType(BeanUtil.getIntegerValue(data, "SYNC_TYPE"));
            dbDataSync.setScheduledTime(BeanUtil.getStringValue(data, "SCHEDULED_TIME"));
            dbDataSync.setBak(BeanUtil.getStringValue(data, "BAK"));

            this.dbDataSyncs.put(dbDataSync.getSyncId(), dbDataSync);
        }
    }

    /**
     * 初始化数需要同步的据库连接信息
     * @throws Exception
     */
    public void initDbDataInfos() throws Exception {
        this.dbDataInfos = new HashMap<Integer, DbDataInfo>();
        String sql = "select * from t_cfg_dbdata_info";
        List<Map<String, Object>> list = this.createDao().queryList(sql);
        for (Map<String, Object> data : list) {
            DbDataInfo dbDataInfo = new DbDataInfo();
            dbDataInfo.setDbId(BeanUtil.getIntegerValue(data, "DB_ID"));
            dbDataInfo.setDbLink(BeanUtil.getStringValue(data, "DB_LINK"));
            dbDataInfo.setDbUser(BeanUtil.getStringValue(data, "DB_USER"));
            dbDataInfo.setDbPassword(DesEncrypter.decrypt(BeanUtil.getStringValue(data, "DB_PASSWORD")));
            dbDataInfo.setDbDriver(BeanUtil.getStringValue(data, "DB_DRIVER"));
            dbDataInfo.setDbTns(BeanUtil.getStringValue(data, "DB_TNS"));
            dbDataInfo.setBak(BeanUtil.getStringValue(data, "BAK"));

            this.dbDataInfos.put(dbDataInfo.getDbId(), dbDataInfo);
        }
    }

    /**
     * 初始化本应用所连接的库信息
     * @return
     * @throws Exception
     */
    public DataSyncDao createDao() throws Exception {
        ConnectionInfo info = new ConnectionInfo();
        info.setDbDriver(PropertyUtil.getValue("jdbc.driver"));
        info.setDbName(PropertyUtil.getValue("jdbc.dbname"));
        info.setDbTns(PropertyUtil.getValue("jdbc.url"));
        info.setDbUser(PropertyUtil.getValue("jdbc.username"));
        info.setDbPassword(PropertyUtil.getValue("jdbc.password"));
//        info.setDbType(PropertyUtil.getValue("jdbc.password"));
      //  info.setDbPassword(DesEncrypter.decrypt(PropertyUtil.getValue("jdbc.password")));
        return new DataSyncDao(info.getConnection(), info.getDbType());
    }

    public static void main(String[] args) throws Exception {
        new DataSync().doSync();
    }
}
