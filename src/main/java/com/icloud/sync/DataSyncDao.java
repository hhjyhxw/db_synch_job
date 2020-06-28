package com.icloud.sync;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.icloud.sync.table.ColumnInfo;
import com.icloud.sync.table.IndexInfo;
import com.icloud.sync.table.TableInfo;

public class DataSyncDao {
    private Logger logger = Logger.getLogger(DataSyncDao.class);
    private Connection connection;
    private String dbType;
    private Map<String, TableInfo> cache = new HashMap<String, TableInfo>();

    public DataSyncDao(Connection connection, String dbType) {
        this.connection = connection;
        this.dbType = dbType;
    }

    public TableInfo getTableInfo(String sql) {
        this.logger.debug("getTableInfo...");
        TableInfo tableInfo = null;
        try {
            tableInfo = new TableInfo();
            tableInfo.setDbType(this.dbType);
            // 获取表字段信息
            Statement statement = this.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            ResultSetMetaData rsmd = resultSet.getMetaData();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                ColumnInfo columnInfo = new ColumnInfo();
                columnInfo.setCatalogName(rsmd.getCatalogName(i));
                columnInfo.setSchemaName(rsmd.getSchemaName(i));
                columnInfo.setTableName(rsmd.getTableName(i));
                columnInfo.setName(rsmd.getColumnName(i));
                columnInfo.setType(rsmd.getColumnType(i));
                columnInfo.setTypeName(rsmd.getColumnTypeName(i));
                columnInfo.setDisplaySize(rsmd.getColumnDisplaySize(i));
                columnInfo.setScale(rsmd.getScale(i));
                columnInfo.setNotNull(false);

                if (!tableInfo.hasColumn(columnInfo.getName())) {
                    tableInfo.addColumn(columnInfo);
                }
                this.logger.debug("getTableInfo columnInfo = " + columnInfo);
            }
            resultSet.close();
            statement.close();
            logger.debug(tableInfo);
            return tableInfo;
        } catch (Exception e) {
            throw new RuntimeException("表结构获取: dbType=" + tableInfo.getDbType() + ", sql=" + sql, e);
        } finally {
            this.logger.debug("getTableInfo end!");
        }
    }

    public TableInfo getTableInfo(String dbName, String tableName, boolean onlyColumn) {
        String key = dbName + "_" + tableName + "_" + new Boolean(onlyColumn);
        if (!this.cache.containsKey(key)) {
            this.logger.debug("getTableInfo...");
            TableInfo tableInfo = null;
            try {
                tableInfo = new TableInfo();
                tableInfo.setDbName(dbName);
                tableInfo.setDbType(this.dbType);
                tableInfo.setTableName(tableName);
                // 获取表字段信息
                DatabaseMetaData dbmd = connection.getMetaData();
                ResultSet columnRSet = null;
                if (tableInfo.getDbType().equals("mysql")) {
                    columnRSet = dbmd.getColumns(dbName, dbName, tableName, null);
                    while (columnRSet.next()) {
                        ColumnInfo columnInfo = new ColumnInfo();
                        columnInfo.setCatalogName(columnRSet.getString("TABLE_CAT"));
                        columnInfo.setSchemaName(columnRSet.getString("TABLE_SCHEM"));
                        columnInfo.setTableName(columnRSet.getString("TABLE_NAME"));
                        columnInfo.setName(columnRSet.getString("COLUMN_NAME").toUpperCase());
                        columnInfo.setType(columnRSet.getInt("DATA_TYPE"));
                        columnInfo.setTypeName(columnRSet.getString("TYPE_NAME"));
                        columnInfo.setDisplaySize(columnRSet.getInt("COLUMN_SIZE"));
                        columnInfo.setScale(columnRSet.getInt("DECIMAL_DIGITS"));
                        columnInfo.setNotNull("NO".equals(columnRSet.getString("IS_NULLABLE")));
                        columnInfo.setComment(columnRSet.getString("REMARKS"));

                        if (!tableInfo.hasColumn(columnInfo.getName())) {
                            tableInfo.addColumn(columnInfo);
                        }
                        this.logger.debug("getTableInfo columnInfo = " + columnInfo);
                    }
                    columnRSet.close();
                } else {
                    columnRSet = dbmd.getColumns(dbName.toUpperCase(), dbName.toUpperCase(), tableName.toUpperCase(),
                            "%");
                    while (columnRSet.next()) {
                        ColumnInfo columnInfo = new ColumnInfo();
                        columnInfo.setCatalogName(columnRSet.getString("TABLE_CAT"));
                        columnInfo.setSchemaName(columnRSet.getString("TABLE_SCHEM"));
                        columnInfo.setTableName(columnRSet.getString("TABLE_NAME"));
                        columnInfo.setName(columnRSet.getString("COLUMN_NAME").toUpperCase());
                        columnInfo.setType(columnRSet.getInt("DATA_TYPE"));
                        columnInfo.setTypeName(columnRSet.getString("TYPE_NAME"));
                        columnInfo.setDisplaySize(columnRSet.getInt("COLUMN_SIZE"));
                        columnInfo.setScale(columnRSet.getInt("DECIMAL_DIGITS"));
                        columnInfo.setNotNull("NO".equals(columnRSet.getString("IS_NULLABLE")));
                        columnInfo.setComment(columnRSet.getString("REMARKS"));

                        if (!tableInfo.hasColumn(columnInfo.getName())) {
                            tableInfo.addColumn(columnInfo);
                        }
                        this.logger.debug("getTableInfo columnInfo = " + columnInfo);
                    }
                    columnRSet.close();
                }

                if (!onlyColumn) {
                    // 获取主键信息
                    this.logger.debug("getTableInfo getPrimaryKeys");
                    ResultSet pkRSet;
                    if (tableInfo.getDbType().equals("mysql")) {
                        pkRSet = dbmd.getPrimaryKeys(dbName, dbName, tableName);
                    } else {
                        pkRSet = dbmd.getPrimaryKeys(dbName.toUpperCase(), dbName.toUpperCase(),
                                tableName.toUpperCase());
                    }
                    while (pkRSet.next()) {
                        String primaryKey = (String) pkRSet.getObject(4);
                        if (tableInfo.hasColumn(primaryKey) && !tableInfo.hasPrimaryKey(primaryKey)) {
                            tableInfo.addPrimaryKey(tableInfo.getColumn(primaryKey));
                            this.logger.debug("getTableInfo primaryKey = " + primaryKey);
                        }
                    }
                    pkRSet.close();

                    // 获取索引
                    ResultSet idxRSet = dbmd.getIndexInfo(null, null, tableName, false, true);
                    while (idxRSet.next()) {
                        IndexInfo indexInfo = new IndexInfo();
                        indexInfo.setCatalog(idxRSet.getString("TABLE_CAT"));
                        indexInfo.setSchema(idxRSet.getString("TABLE_SCHEM"));
                        indexInfo.setTableName(idxRSet.getString("TABLE_NAME"));
                        indexInfo.setNonUnique(idxRSet.getBoolean("NON_UNIQUE"));
                        indexInfo.setIndexQualifier(idxRSet.getString("INDEX_QUALIFIER"));
                        indexInfo.setIndexName(idxRSet.getString("INDEX_NAME"));
                        indexInfo.setType(idxRSet.getShort("TYPE"));
                        indexInfo.setOrdinalPosition(idxRSet.getShort("ORDINAL_POSITION"));
                        indexInfo.setColumnName(idxRSet.getString("COLUMN_NAME"));
                        indexInfo.setAscOrDesc(idxRSet.getString("ASC_OR_DESC"));
                        indexInfo.setCardinality(idxRSet.getInt("CARDINALITY"));
                        indexInfo.setPages(idxRSet.getInt("PAGES"));
                        indexInfo.setFilterCondition(idxRSet.getString("FILTER_CONDITION"));
                        if (indexInfo.getIndexName() != null) {
                            tableInfo.addIndex(indexInfo);
                        }
                    }
                    idxRSet.close();
                }
                logger.debug(tableInfo);
                this.cache.put(key, tableInfo);
                return tableInfo;
            } catch (Exception e) {
                throw new RuntimeException(
                        "表结构获取: dbType=" + tableInfo.getDbType() + ", dbName=" + dbName + ", tableName=" + tableName,
                        e);
            } finally {
                this.logger.debug("getTableInfo end!");
            }
        }
        return this.cache.get(key);
    }

    public List<Map<String, Object>> queryList(final String sql) throws Exception {
        try {
            logger.debug("query use sql=" + sql);
            List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
            Statement statement = this.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            ResultSetMetaData rsmd = resultSet.getMetaData();
            while (resultSet.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    map.put(rsmd.getColumnName(i).toUpperCase(), resultSet.getObject(i));
                }
                dataList.add(map);
            }
            resultSet.close();
            statement.close();
            return dataList;
        } catch (Exception e) {
            throw new Exception("查询出错：query use sql=" + sql, e);
        }
    }

    public Object queryValue(String sql) throws Exception {
        try {
            logger.debug("query use sql=" + sql);
            Statement statement = this.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            Object value = null;
            if (resultSet.next()) {
                value = resultSet.getObject(1);
            }
            resultSet.close();
            statement.close();
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("查询出错：query value use sql=" + sql, e);
        }
    }

    public Map<String, Object> queryFirst(String sql) throws Exception {
        try {
            Statement statement = this.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            ResultSetMetaData rsmd = resultSet.getMetaData();
            Map<String, Object> map = new HashMap<String, Object>();
            if (resultSet.next()) {
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    map.put(rsmd.getColumnName(i).toUpperCase(), resultSet.getObject(i));
                }
            }
            resultSet.close();
            statement.close();
            return map;
        } catch (Exception e) {
            throw new Exception("查询出错：query use sql=" + sql, e);
        }
    }

    /**
     * 获取总记录数，在兼容以往处理机制的基础上，增加对分组数据的处理
     *
     * @param tableName
     *            查询语句
     * @return 总记录数
     * @throws SQLException
     */
    public long getRecordCount(final String tableName) throws Exception {
        String sql = "select count(*) TOTAL from " + tableName;
        logger.debug("query count sql=" + sql);
        Statement statement = this.connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        long total = 0;
        if (resultSet.next()) {
            total = resultSet.getLong("TOTAL");
        }
        resultSet.close();
        statement.close();
        return total;
    }

    /**
     * 获取总记录数，在兼容以往处理机制的基础上，增加对分组数据的处理
     *
     * @param sql
     *            查询语句
     * @return 总记录数
     * @throws SQLException
     */
    public long queryCount(final String sql) throws Exception {
        String countSql = "select count(*) TOTAL from (" + sql + ") aaa";
        logger.debug("query count sql=" + countSql);
        Statement statement = this.connection.createStatement();
        ResultSet resultSet = statement.executeQuery(countSql);
        long total = 0;
        if (resultSet.next()) {
            total = resultSet.getLong("TOTAL");
        }
        resultSet.close();
        statement.close();
        return total;
    }

    public boolean execute(String sql) throws Exception {
        logger.warn("execute sql=" + sql);
        Statement statement = this.connection.createStatement();
        boolean result = statement.execute(sql);
        statement.close();
        return result;
    }

    public Connection getConnection() {
        return connection;
    }

    public String getDbType() {
        return dbType;
    }

    public void close() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }
        } catch (Exception e) {
        }
    }

}
