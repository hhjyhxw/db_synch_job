package com.icloud.sync;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.icloud.util.BeanUtil;
import com.icloud.util.Constants;
import com.icloud.sync.table.SqlParam;
import com.icloud.sync.table.TableInfo;
import com.icloud.sync.valueobject.DbDataInfo;
import com.icloud.sync.valueobject.DbDataSync;
import com.icloud.sync.valueobject.DbDataSyncHis;
import com.icloud.util.PropertyUtil;

public class DataSyncTransfer {
    private Logger logger = Logger.getLogger(DataSyncTransfer.class);
    private Map<String, String> params = new HashMap<String, String>();
    private String paramsJson;

    private DataSyncDao dao;
    private DbDataInfo dbDataInfo;
    private DbDataSync dbDataSync;
    private DbDataSyncHis dbDataSyncHis;
    private TableInfo destTableInfo;

    public DataSyncTransfer(DataSyncDao dao, DbDataInfo dbDataInfo, DbDataSync dbDataSync, Map<String, String> params) throws Exception {
        this.dao = dao;
        this.dbDataInfo = dbDataInfo;
        this.dbDataSync = dbDataSync;
        this.setParams(params);
        this.createParamsJson();
    }

    /**
     * 判断此同步信息是否正在运行
     *
     * @return
     * @throws Exception
     */
    public boolean isRunning() throws Exception {
        StringBuffer buf = new StringBuffer("select count(*) from t_cfg_dbdata_sync_his ");
        buf.append(" where sync_id=").append(this.dbDataSync.getSyncId());
        buf.append("    and status=1 ");
        if (Constants.DB_TYPE_MYSQL.equals(this.dao.getDbType())) {
            buf.append("    and start_time > date_sub(now(), interval 1 day) ");
        } else {
            buf.append("    and start_time > sysdate - 1 ");
        }
        if (this.paramsJson != null && !"".equals(this.paramsJson)) {
            buf.append("    and params='").append(this.paramsJson).append("'");
        }
        String sql = buf.toString();
        Object value = this.dao.queryValue(sql);
        this.logger.info("判断数据同步是否正在运行使用的sql: " + sql + ", 记录数：" + value);
        return value == null ? false : new Integer(value.toString()) <= 0 ? false : true;
    }

    /**
     * 开始同步
     *
     * @return
     * @throws Exception
     */
    public boolean doSync() throws Exception {
        this.logger.info("运行参数：" + this.params);

        // 创建源表连接信息
        this.createSourConnectionInfo();

        // 从原表查询并获取sql值生成参数
        this.loadParams();
        this.logger.info("获取sql参数后的参数：" + this.params);

        // 初始化同步历史记录信息
        this.initDbDataSyncHis();

        // 创建目标表
        this.createDestTable();

        // 清空目标表数据
        this.cleanDestTableData();

        // 加载目标表结构信息
        this.loadDestTableInfo();

        // 加载源表需要同步的记录数
        this.loadSyncCount();

        // 保存同步历史记录信息，数据同步前调用
        this.insertDbDataSyncHis();

        try {
            // 开始同步数据
            this.transferData();
            this.dbDataSyncHis.setStatus(DbDataSyncHis.STATUS_FINISH);
        } catch (Exception e) {
            this.dbDataSyncHis.setError(e.getMessage());
            this.dbDataSyncHis.setStatus(DbDataSyncHis.STATUS_ERROR);
            throw e;
        } finally {
            // 注：需要保证同步信息得到更新。
            // 数据同步结束更新同步历史记录信息
            this.updateDbDataSyncHis();
            this.dao.close();
        }
        return false;
    }

    /**
     * 将map参数转换成json格式，参数按字母排序
     */
    private void createParamsJson() {
        if (this.params != null && !this.params.isEmpty()) {
            StringBuffer buf = new StringBuffer();
            buf.append("{");
            Set<String> keySet = this.params.keySet();
            int i = 0;
            for (Iterator<String> iter = keySet.iterator(); iter.hasNext();) {
                Object key = iter.next();
                Object value = this.params.get(key);
                if (i > 0) {
                    buf.append(",");
                }
                buf.append("\"");
                buf.append(key.toString());
                buf.append("\"");
                buf.append(":");
                buf.append("\"");
                buf.append(value == null ? "" : value.toString());
                buf.append("\"");
                i++;
            }
            buf.append("}");
            this.paramsJson = buf.toString();
            this.logger.info("参数：" + this.paramsJson);
        }
    }

    /**
     * 加载参数
     *
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    private void loadParams() throws Exception {
        String sql = null;
        if (this.dbDataSync.getParamSql() != null && !"".equals(this.dbDataSync.getParamSql())) {
            try {
                sql = this.dbDataSync.getParamSql();
                sql = this.parseParams(sql);
                logger.debug("query use sql=" + sql);

                ConnectionInfo info = this.createSourConnectionInfo();
                DataSyncDao dataSyncDao = new DataSyncDao(info.getConnection(), info.getDbType());
                Map paramMap = dataSyncDao.queryFirst(sql);
                for (Iterator iter = paramMap.keySet().iterator(); iter.hasNext();) {
                    String key = (String) iter.next();
                    Object value = paramMap.get(key);
                    if (!this.params.containsKey(key)) {
                        this.params.put(key, value == null ? "" : value.toString());
                    }
                }
                dataSyncDao.close();
                this.createParamsJson();
            } catch (Exception e) {
                String info = "查询出错：query value use sql=" + sql;
                this.logger.error(info, e);
                throw new Exception(info, e);
            }
        }
    }

    /**
     * 初始化调度记录表
     */
    private void initDbDataSyncHis() {
        this.dbDataSyncHis = new DbDataSyncHis();
        this.dbDataSyncHis.setDbId(this.dbDataInfo.getDbId());
        this.dbDataSyncHis.setSyncId(this.dbDataSync.getSyncId());
        this.dbDataSyncHis.setParentSyncId(this.dbDataSync.getParentSyncId());
        this.dbDataSyncHis.setFromTableName(parseParams(this.dbDataSync.getFromTableName()));
        this.dbDataSyncHis.setFromSql(parseParams(this.dbDataSync.getFromSql()));
        this.dbDataSyncHis.setParamSql(parseParams(this.dbDataSync.getParamSql()));
        this.dbDataSyncHis.setToTableName(parseParams(this.dbDataSync.getToTableName()));
        this.dbDataSyncHis.setCreateFile(parseParams(this.dbDataSync.getCreateFile()));
        this.dbDataSyncHis.setParams(this.paramsJson);
        this.dbDataSyncHis.setStatus(DbDataSyncHis.STATUS_START);
        this.dbDataSyncHis.setSyncType(DbDataSyncHis.SYNC_TYPE_MANUAL);

        this.logger.debug(this.dbDataSyncHis);
    }

    /**
     * 创建目标表
     *
     * @throws Exception
     */
    private void createDestTable() throws Exception {
        // 先判断目标表是否存在，不存在时创建
        String table = this.dbDataSyncHis.getToTableName();
        table = table.toUpperCase().replaceAll("`", "");
        String[] strs = table.split("\\.", 2);
        if (strs.length == 2) {
            StringBuffer buf = new StringBuffer();
            if (Constants.DB_TYPE_MYSQL.equals(this.dao.getDbType())) {
                buf.append("select count(*) from INFORMATION_SCHEMA.TABLES ");
                buf.append("where lower(TABLE_SCHEMA)=lower('" + strs[0] + "') ");
                buf.append("   and lower(TABLE_NAME)=lower('" + strs[1] + "') ");
            } else {
            	this.logger.info("DbType=============================：" + this.dao.getDbType());
                buf.append("select count(*) from dba_tables ");
                buf.append("where owner='" + strs[0] + "' and table_name='" + strs[1] + "'");
            }
            String sql = buf.toString();
            this.logger.info("判断目标表是否存在使用的sql：" + sql);
            Object value = this.dao.queryValue(sql);
            Integer count = null;
            if (value != null && !"".equals(value)) {
                count = Integer.valueOf(value.toString());
            }
            if (count == null || count.intValue() <= 0) {
                this.logger.info("表不存在：" + this.dbDataSyncHis.getToTableName());
                sql = this.dbDataSync.getCreateTableSql();
                sql = this.parseParams(sql);
                if (sql == null || "".equals(sql)) {
                    TableInfo tableInfo = null;
                    ConnectionInfo info = this.createSourConnectionInfo();
                    DataSyncDao dataSyncDao = new DataSyncDao(info.getConnection(), info.getDbType());
                    if (this.dbDataSyncHis.getFromSql() != null && !"".equals(this.dbDataSyncHis.getFromSql())) {
                        String limitSql = null;
                        if (Constants.DB_TYPE_ORACLE.equalsIgnoreCase(info.getDbType())) {
                            limitSql = "select * from (select row_.*, rownum rownum_ from( "
                                    + this.dbDataSyncHis.getFromSql() + " )row_ ";
                            limitSql += " where rownum <= 1) where rownum_ > 1";
                        } else {
                            limitSql = this.dbDataSyncHis.getFromSql() + " limit 1";
                        }
                        tableInfo = dataSyncDao.getTableInfo(limitSql);
                    } else {
                        String sourTable = this.dbDataSyncHis.getFromTableName();
                        String[] sourStrs = sourTable.split("\\.", 2);
                        if (sourStrs.length == 2) {
                            tableInfo = dataSyncDao.getTableInfo(sourStrs[0], sourStrs[1], true);
                            if (tableInfo.getColumnList() == null || tableInfo.getColumnList().isEmpty()) {

                                tableInfo = dataSyncDao.getTableInfo("select * from " + sourTable);
                            }
                        } else {
                            String message = "源表配置不正确：" + this.dbDataSync.getToTableName();
                            this.logger.error(message);
                            throw new RuntimeException(message);
                        }
                    }
                    dataSyncDao.close();
                    sql = tableInfo.createTableSql("mysql", strs[0], strs[1], this.dbDataSync.getIndexFields());
                }
                this.logger.info("目标表不存在，创建目标表使用的sql：" + sql);
                this.dao.execute(sql);
                this.dbDataSyncHis.setCreateTableSql(sql);
            }
        } else {
            String message = "目标表配置不正确：" + this.dbDataSync.getToTableName();
            this.logger.error(message);
            throw new RuntimeException(message);
        }
    }

    /**
     * 清空目标表的数据
     * 
     * @throws Exception
     */
    private void cleanDestTableData() throws Exception {
        if (this.dbDataSync.getIsDelete() != null && this.dbDataSync.getIsDelete().intValue() == 1) {
            String sql = this.dbDataSync.getDeleteSql();
            if (sql == null || "".equals(sql)) {
                sql = "delete from " + this.dbDataSync.getToTableName();
            }
            sql = this.parseParams(sql);
            this.logger.info("清空目标表数据使用的sql：" + sql);
            this.dao.execute(sql);
            this.dbDataSyncHis.setDeleteSql(sql);
        }
    }

    /**
     * 加载目标表结构信息
     *
     * @throws SQLException
     */
    private void loadDestTableInfo() throws SQLException {
        String table = this.dbDataSyncHis.getToTableName();
        table = table.replaceAll("`", "");
        String[] strs = table.split("\\.", 2);
        if (strs.length == 2) {
            this.destTableInfo = this.dao.getTableInfo(strs[0], strs[1], true);
            if (this.destTableInfo == null) {
                String message = "无法获取目标表结构信息！";
                this.logger.error(message);
                throw new RuntimeException(message);
            }
        }
    }

    /**
     * 创建链接信息
     */
    private ConnectionInfo createSourConnectionInfo() {
        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.setCache(false);
        connectionInfo.setDbTns(this.dbDataInfo.getDbTns());
        connectionInfo.setDbUser(this.dbDataInfo.getDbUser());
        connectionInfo.setDbPassword(this.dbDataInfo.getDbPassword());
        connectionInfo.setDbDriver(this.dbDataInfo.getDbDriver());
        
        return connectionInfo;
    }

    /**
     * 加载要同步的数据记录数
     *
     * @throws Exception
     */
    private void loadSyncCount() throws Exception {
        String sql = null;
        if (this.dbDataSyncHis.getFromSql() != null && !"".equals(this.dbDataSyncHis.getFromSql())) {
            sql = this.dbDataSyncHis.getFromSql();
            sql = "select count(*) from (" + sql + ") a";
        } else {
            sql = "select count(*) from " + this.dbDataSyncHis.getFromTableName();
        }
        try {
            logger.info("query use sql=" + sql);
            ConnectionInfo info = this.createSourConnectionInfo();
            Statement statement = info.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            Object value = null;
            if (resultSet.next()) {
                value = resultSet.getObject(1);
            }
            resultSet.close();
            statement.close();
            info.getConnection().close();
            if (value != null) {
            	logger.info("total count:"+value.toString());
                this.dbDataSyncHis.setTotalCount(Integer.valueOf(value.toString()));
            }
        } catch (Exception e) {
            String info = "查询出错：query value use sql=" + sql;
            this.logger.error(info, e);
            throw new Exception(info, e);
        }
    }

    /**
     * 数据同步
     *
     * @throws Exception
     *             @throws
     */
    private void transferData() throws Exception {
        Integer count = this.dbDataSyncHis.getTotalCount();
        if (count == null || count.intValue() <= 0) {
            this.logger.info("源表无数据，不同步！");
            this.dbDataSyncHis.setStatus(DbDataSyncHis.STATUS_FINISH);
            return;
        }
        this.transferDataByJdbc();
    }

    /**
     * jdbc方式同步数据
     *
     * @throws Exception
     */
    private void transferDataByJdbc() throws Exception {
        String sql = null;
        if (this.dbDataSyncHis.getFromSql() != null && !"".equals(this.dbDataSyncHis.getFromSql())) {
            sql = this.dbDataSyncHis.getFromSql();
        } else {
            sql = "select * from " + this.dbDataSyncHis.getFromTableName();
        }
        Integer totalSize = this.dbDataSyncHis.getTotalCount();
        int pageSize = 5000; // 分页查询，5000条一页
        int totalPageNumber = (int) Math.ceil(totalSize.intValue() * 1.0 / pageSize);
        ConnectionInfo info = this.createSourConnectionInfo();
        for (int pageNumber = 1; pageNumber <= totalPageNumber; pageNumber++) {
            long firstRow = (pageNumber - 1) * pageSize;
            String limitSql = null;
            if (Constants.DB_TYPE_ORACLE.equalsIgnoreCase(info.getDbType())) {
                limitSql = "select * from (select row_.*, rownum rownum_ from( " + sql + " )row_ ";
                limitSql += " where rownum <= " + (firstRow + pageSize) + ") where rownum_ > " + firstRow;
            } else {
                limitSql = sql + " limit " + firstRow + ", " + pageSize;
            }
            logger.info("query use sql=" + limitSql);
            List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
            Statement statement = info.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(limitSql);
            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int columnCount = 1; columnCount <= metaData.getColumnCount(); columnCount++) {
                    String columnName = metaData.getColumnLabel(columnCount);
                    if (columnName != null && !"".equals(columnName)) {
                        map.put(columnName.toUpperCase(), resultSet.getObject(columnCount));
                    }
                }
                dataList.add(map);
            }
            if (!dataList.isEmpty()) {
                this.saveData(dataList);
            }
            resultSet.close();
            statement.close();
        }
        info.getConnection().close();
    }

    /**
     * 保存数据到目标表
     *
     * @param dataList
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    private void saveData(List dataList) throws Exception {
        SqlParam sqlParam = this.destTableInfo.createInsertSql();
        logger.info("saveData: sql=" + sqlParam.getSql() + ", param=" + sqlParam.getParam());
        try {
            Connection connection = this.dao.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement prep = connection.prepareStatement(sqlParam.getSql());
            int count = 0;
            for (Iterator iter = dataList.iterator(); iter.hasNext();) {
                Map data = (Map) iter.next();
                for (Iterator iter2 = sqlParam.getParam().entrySet().iterator(); iter2.hasNext();) {
                    Entry entry = (Entry) iter2.next();
                    int key = ((Integer) entry.getKey()).intValue();
                    String value = (String) entry.getValue();
//                    logger.info("key:"+key+"-----value:"+value);
                    if (this.destTableInfo.hasClob() && this.destTableInfo.getColumn(value).isClob()) {
                        if (data.get(entry.getValue()) != null) {
                            prep.setCharacterStream(key, new StringReader(value), value.length());
                        } else {
                            prep.setCharacterStream(key, null, 0);
                        }
                    } else {
                        prep.setObject(key, data.get(value));
//                        logger.info("key:"+value+"-----value:"+data.get(value));
                    }
                }
                prep.addBatch();
                count++;
                if (count >= 10000) {
                    prep.executeBatch();
                    connection.commit();
                    count = 0;
                }
            }
            prep.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
            prep.close();
            dataList.clear();
        } catch (Exception e) {
            throw new Exception("数据插入出错：sql=" + sqlParam.getSql(), e);
        }
    }

    /**
     * 插入同步记录
     * @throws Exception 
     */
    private void insertDbDataSyncHis() throws Exception {
    	 Connection connection = this.dao.getConnection();
    	  PreparedStatement prep = null;
    	if (Constants.DB_TYPE_ORACLE.equals(this.dao.getDbType())) {
            String sql = "select SEQ_CFG_DBDATA_SYNC_HIS.nextVal from dual";
            Object value = this.dao.queryValue(sql);
            this.dbDataSyncHis.setSyncHisId(new Integer(value.toString()));
        }    
        if (Constants.DB_TYPE_MYSQL.equals(this.dao.getDbType())) {
//          ResultSet rs = prep.getGeneratedKeys(); // 获取结果
          prep = connection.prepareStatement("select nextval('SEQ_CFG_DBDATA_SYNC_HIS')");
          this.logger.info("prep=======" + prep);
          prep.executeQuery();
          ResultSet rs = prep.getResultSet();
          if (rs.next()) {
          	this.logger.info("rs.getInt(1)=====" + rs.getInt(1));
            this.dbDataSyncHis.setSyncHisId(rs.getInt(1));
          }
      }
        this.dbDataSyncHis.setStartTime(new Date());
        String dbName = PropertyUtil.getValue("jdbc.dbname");
        TableInfo hisTableInfo = this.dao.getTableInfo(dbName, "t_cfg_dbdata_sync_his", true);
        SqlParam sqlParam = hisTableInfo.createInsertSql();
       
        this.logger.info("sqlParam.getSql()======: " +sqlParam.getSql());
        prep = connection.prepareStatement(sqlParam.getSql());
        for (Iterator<Entry<Integer, String>> iter = sqlParam.getParam().entrySet().iterator(); iter.hasNext();) {
            Entry<Integer, String> entry = iter.next();
            int key = ((Integer) entry.getKey()).intValue();
            String value = (String) entry.getValue();
            if (this.destTableInfo.hasClob() && this.destTableInfo.getColumn(value).isClob()) {
                Object data = BeanUtil.getValue(this.dbDataSyncHis, entry.getValue());
                if (data != null) {
                    prep.setCharacterStream(key, new StringReader(data.toString()), data.toString().length());
                } else {
                    prep.setCharacterStream(key, null, 0);
                }
            } else {
                Object data = BeanUtil.getValue(this.dbDataSyncHis, BeanUtil.convertFieldName(entry.getValue()));
                if (data instanceof Date) {
                    prep.setTimestamp(key, data == null ? null : new Timestamp(((Date) data).getTime()));
                } else {
                    prep.setObject(key, data);
                }
            }
        }
        
        prep.executeUpdate();
//        if (Constants.DB_TYPE_MYSQL.equals(this.dao.getDbType())) {
////            ResultSet rs = prep.getGeneratedKeys(); // 获取结果
//            prep = connection.prepareStatement("select nextval('SEQ_CFG_DBDATA_SYNC_HIS')");
//            this.logger.info("prep=======" + prep);
//            prep.executeQuery();
//            ResultSet rs = prep.getResultSet();
//            if (rs.next()) {
//            	this.logger.info("rs.getInt(1)=====" + rs.getInt(1));
//                this.dbDataSyncHis.setSyncHisId(rs.getInt(1));
//            }
//        }
    }

    /**
     * 更新同步记录，更新内容包括状态，完成时间，出错信息
     *
     * @throws SQLException
     */
    private void updateDbDataSyncHis() throws SQLException {
        this.dbDataSyncHis.setEndTime(new Date());
        String dbName = PropertyUtil.getValue("jdbc.dbname");
        TableInfo hisTableInfo = this.dao.getTableInfo(dbName, "t_cfg_dbdata_sync_his", false);
        SqlParam sqlParam = hisTableInfo.createUpdateSql();
        Connection connection = this.dao.getConnection();
        PreparedStatement prep = connection.prepareStatement(sqlParam.getSql());
        this.logger.info("updateDbDataSyncHis=======sqlParam.getSql()" + sqlParam.getSql());
        this.logger.info("sqlParam.getParam().entrySet()=========" + sqlParam.getParam().entrySet());
        for (Iterator<Entry<Integer, String>> iter = sqlParam.getParam().entrySet().iterator(); iter.hasNext();) {
            Entry<Integer, String> entry = iter.next();
            int key = ((Integer) entry.getKey()).intValue();
            String value = (String) entry.getValue();
            if (this.destTableInfo.hasClob() && this.destTableInfo.getColumn(value).isClob()) {
                Object data = BeanUtil.getValue(this.dbDataSyncHis, entry.getValue());
                if (data != null) {
                    prep.setCharacterStream(key, new StringReader(data.toString()), data.toString().length());
                } else {
                    prep.setCharacterStream(key, null, 0);
                }
            } else {
                Object data = BeanUtil.getValue(this.dbDataSyncHis, BeanUtil.convertFieldName(entry.getValue()));
                if (data instanceof Date) {
                    prep.setTimestamp(key, data == null ? null : new Timestamp(((Date) data).getTime()));
                } else {
                    prep.setObject(key, data);
                }
            }
        }
        prep.executeUpdate();
    }

    /**
     * 使用参数将${xxx}转换成实际的值
     *
     * @param str
     * @return
     */
    private String parseParams(String str) {
        if (str == null || "".equals(str)) {
            return null;
        }

        Pattern pattern = Pattern.compile("[{]([^}]*)[}]");
        Matcher matcher = pattern.matcher(str);
        String result = str;
        while (matcher.find()) {
            String key = matcher.group();
            if (key != null && key.length() > 3) {
                key = key.substring(1, key.length() - 1);
                Object value = this.params.get(key);
                String regex = "\\{" + key + "\\}";
                if (value != null) {
                    result = result.replaceFirst(regex, value.toString());
                } else {
                    result = result.replaceFirst(regex, "");
                }
            } else {
                result = result.replaceAll(key, "");
            }
        }
        return result;
    }

    /**
     * 设置params值，并将所有的key转换成大写
     *
     * @param params
     */
    private void setParams(Map<String, String> params) {
        if (params != null && !params.isEmpty()) {
            Map<String, String> map = new TreeMap<String, String>();
            Set<String> keySet = params.keySet();
            for (Iterator<String> iter = keySet.iterator(); iter.hasNext();) {
                Object key = iter.next();
                if (key != null) {
                    map.put(key.toString().toUpperCase(), params.get(key));
                }
            }
            this.params = map;
        }
    }
}
