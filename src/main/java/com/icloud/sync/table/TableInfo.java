package com.icloud.sync.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.icloud.util.Constants;

public class TableInfo {
    private String dbName;//数据库名称
    private String dbType;//数据库类型
    private String tableName;//表名

    private List<ColumnInfo> columnList = new ArrayList<ColumnInfo>();
    private List<ColumnInfo> primaryKeys = new ArrayList<ColumnInfo>();
    private List<IndexInfo> indexList = new ArrayList<IndexInfo>();

    private Map<String, SqlParam> cache = new HashMap<String, SqlParam>();
    private Map<String, ColumnInfo> cacheColumn = new HashMap<String, ColumnInfo>();

    private boolean hasClob = false;
    private boolean isSort = false;

    public boolean hasPrimaryKey(String primaryKey) {
        for (Iterator<ColumnInfo> iter = this.primaryKeys.iterator(); iter.hasNext();) {
            ColumnInfo columnInfo = (ColumnInfo) iter.next();
            if (columnInfo.getName().toUpperCase().equals(primaryKey.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    public void addPrimaryKey(ColumnInfo columnInfo) {
        this.primaryKeys.add(columnInfo);
    }

    public void addIndex(IndexInfo indexInfo) {
        boolean hasIndex = false;
        for (Iterator<IndexInfo> iter = this.indexList.iterator(); iter.hasNext();) {
            IndexInfo temp = (IndexInfo) iter.next();
            if (temp.getIndexName().equalsIgnoreCase(indexInfo.getIndexName())) {
                hasIndex = true;
                String sour = "," + temp.getColumnName() + ",";
                String dest = "," + indexInfo.getColumnName() + ",";
                if (sour.indexOf(dest) < 0) {
                    temp.setColumnName(temp.getColumnName() + "," + indexInfo.getColumnName());
                }
            }
        }
        if (!hasIndex) {
            this.indexList.add(indexInfo);
        }
    }

    public void addColumn(ColumnInfo columnInfo) {
        if (columnInfo.isClob()) {
            this.setHasClob(true);
        }
        this.columnList.add(columnInfo);
    }

    public ColumnInfo getColumn(String columnName) {
        if (!this.cacheColumn.containsKey(columnName)) {
            for (Iterator<ColumnInfo> iter = this.columnList.iterator(); iter.hasNext();) {
                ColumnInfo columnInfo = (ColumnInfo) iter.next();
                if (columnInfo.getName().equalsIgnoreCase(columnName)) {
                    this.cacheColumn.put(columnName, columnInfo);
                    break;
                }
            }
        }
        return (ColumnInfo) this.cacheColumn.get(columnName);
    }

    public boolean hasColumn(String columnName) {
        if ("ROWNUM_".equalsIgnoreCase(columnName)) {
            return true; // ROWNUM_ 用于查询过滤某些字段，不需要插入。
        }
        for (Iterator<ColumnInfo> iter = this.columnList.iterator(); iter.hasNext();) {
            ColumnInfo columnInfo = (ColumnInfo) iter.next();
            if (columnInfo.getName().toUpperCase().equals(columnName.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    public List<ColumnInfo> getColumnList() {
        if (!isSort) {
            List<ColumnInfo> list = new ArrayList<ColumnInfo>();
            for (Iterator<ColumnInfo> iter = this.columnList.iterator(); iter.hasNext();) {
                ColumnInfo columnInfo = (ColumnInfo) iter.next();
                if (columnInfo.getTypeName().equalsIgnoreCase("LONG") || columnInfo.getTypeName().indexOf("LOB") > 0) {
                    list.add(0, columnInfo);
                } else {
                    list.add(columnInfo);
                }
            }
            this.columnList = list;
            this.isSort = true;
        }
        return this.columnList;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean hasClob() {
        return this.hasClob;
    }

    public void setHasClob(boolean hasClob) {
        this.hasClob = hasClob;
    }

    public String createDisableKeySql() {
        String sql = "ALTER TABLE ";
        if (dbName != null && !"".equals(dbName)) {
            sql += dbName + ".";
        }
        sql += this.tableName + "  DISABLE KEYS";
        return sql;
    }

    public String createEnableKeySql() {
        String sql = "ALTER TABLE ";
        if (dbName != null && !"".equals(dbName)) {
            sql += dbName + ".";
        }
        sql += this.tableName + "  ENABLE KEYS";
        return sql;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    /**
     * @return
     */
    public String createDeleteSql() {
        String sql = "delete from ";
        if (dbName != null && !"".equals(dbName)) {
            sql += dbName + ".";
        }
        return sql + this.tableName;
    }

    /**
     * 根据插入数据的sql
     * @return
     */
    public SqlParam createInsertSql() {
        if (!this.cache.containsValue("insertSql")) {
            int i = 1;
            Map<Integer, String> param = new HashMap<Integer, String>();
            boolean isFirst = true;
            StringBuffer insertField = new StringBuffer();
            StringBuffer insertParam = new StringBuffer();
            for (Iterator<ColumnInfo> iter = this.columnList.iterator(); iter.hasNext();) {
                ColumnInfo columnInfo = (ColumnInfo) iter.next();
                if (isFirst) {
                    isFirst = false;
                } else {
                    insertField.append(",");
                    insertParam.append(",");
                }
                if (Constants.DB_TYPE_MYSQL.equals(this.dbType)) {
                    insertField.append("`");
                }
                insertField.append(columnInfo.getName());
                if (Constants.DB_TYPE_MYSQL.equals(this.dbType)) {
                    insertField.append("`");
                }
                insertParam.append("?");
                param.put(new Integer(i++), columnInfo.getName());
            }

            String sql = "insert into ";
            if (dbName != null && !"".equals(dbName)) {
                sql += dbName + ".";
            }
            sql += this.tableName + "(" + insertField.toString() + ") values (" + insertParam.toString() + ")";
            SqlParam sqlParam = new SqlParam();
            sqlParam.setSql(sql);
            sqlParam.setParam(param);
            this.cache.put("insertSql", sqlParam);
        }
        return (SqlParam) this.cache.get("insertSql");
    }

    /**
     * 根据插入数据的sql
     * @return
     */
    public SqlParam createInsertPrefixSql() {
        if (this.cache.get("InsertPrefixSql") == null) {
            int i = 1;
            Map<Integer, String> param = new TreeMap<Integer, String>();
            boolean isFirst = true;
            StringBuffer insertField = new StringBuffer();
            StringBuffer insertParam = new StringBuffer();
            for (Iterator<ColumnInfo> iter = this.columnList.iterator(); iter.hasNext();) {
                ColumnInfo columnInfo = iter.next();
                if (isFirst) {
                    isFirst = false;
                } else {
                    insertField.append(",");
                    insertParam.append(",");
                }
                if (Constants.DB_TYPE_MYSQL.equals(this.dbType)) {
                    insertField.append("`");
                }
                insertField.append(columnInfo.getName());
                if (Constants.DB_TYPE_MYSQL.equals(this.dbType)) {
                    insertField.append("`");
                }
                insertParam.append("?");
                param.put(new Integer(i++), columnInfo.getName());
            }

            String sql = "insert into ";
            if (dbName != null && !"".equals(dbName)) {
                sql += dbName + ".";
            }
            sql += this.tableName + "(" + insertField.toString() + ") values ";
            SqlParam sqlParam = new SqlParam();
            sqlParam.setSql(sql);
            sqlParam.setParam(param);
            this.cache.put("InsertPrefixSql", sqlParam);
        }
        return (SqlParam) this.cache.get("InsertPrefixSql");
    }
    
    /**
     * 根据插入数据的sql
     * @return
     */
    public SqlParam createUpdateSql() {
        if (!this.cache.containsValue("updateSql")) {
            int i = 1;
            Map<Integer, String> param = new HashMap<Integer, String>();
            boolean isFirst = true;
            StringBuffer updateField = new StringBuffer();
            for (Iterator<ColumnInfo> iter = this.columnList.iterator(); iter.hasNext();) {
                ColumnInfo columnInfo = (ColumnInfo) iter.next();
                if (isFirst) {
                    isFirst = false;
                } else {
                    updateField.append(",");
                }
                if (Constants.DB_TYPE_MYSQL.equals(this.dbType)) {
                    updateField.append("`");
                }
                updateField.append(columnInfo.getName());
                if (Constants.DB_TYPE_MYSQL.equals(this.dbType)) {
                    updateField.append("`");
                }
                updateField.append("=?");
                param.put(new Integer(i++), columnInfo.getName());
            }
            
            isFirst = true;
            StringBuffer primaryKeyField = new StringBuffer();
            for (Iterator<ColumnInfo> iter = this.primaryKeys.iterator(); iter.hasNext();) {
                ColumnInfo columnInfo = (ColumnInfo) iter.next();
                if (isFirst) {
                    isFirst = false;
                } else {
                    primaryKeyField.append(" and ");
                }
                if (Constants.DB_TYPE_MYSQL.equals(this.dbType)) {
                    primaryKeyField.append("`");
                }
                primaryKeyField.append(columnInfo.getName());
                if (Constants.DB_TYPE_MYSQL.equals(this.dbType)) {
                    primaryKeyField.append("`");
                }
                primaryKeyField.append("=?");
                param.put(new Integer(i++), columnInfo.getName());
            }

            String sql = "update ";
            if (dbName != null && !"".equals(dbName)) {
                sql += dbName + ".";
            }
            sql += this.tableName + " set " + updateField.toString() + " where " + primaryKeyField.toString();
            SqlParam sqlParam = new SqlParam();
            sqlParam.setSql(sql);
            sqlParam.setParam(param);
            this.cache.put("updateSql", sqlParam);
        }
        return (SqlParam) this.cache.get("updateSql");
    }

    public String createTableSql(String dbType, String dbName, String tableName, String indexFields) {
        StringBuffer buf = new StringBuffer();
        boolean isFirst = true;
        buf.append("create table ");
        if (dbName != null && !"".equals(dbName)) {
            buf.append(dbName);
            buf.append(".");
        }
        buf.append(tableName);
        buf.append("(");
        // 字段信息
        for (Iterator<ColumnInfo> iter = this.columnList.iterator(); iter.hasNext();) {
            ColumnInfo columnInfo = (ColumnInfo) iter.next();
            if (isFirst) {
                isFirst = false;
            } else {
                buf.append(", ");
            }
            buf.append(columnInfo.getColumnDDL());
        }
        // 索引信息
        if (indexFields != null && !"".equals(indexFields)) {
            String indexField[] = indexFields.split(",");
            for(int i=0; i < indexField.length; i++) {
                if (indexField[i] != null && !"".equals(indexField[i])) {
                    buf.append(", INDEX `" + tableName + "_idx" + i + "`(`" + indexField[i] + "`)");
                }
            }
        } else {
            for (Iterator<IndexInfo> iter = this.indexList.iterator(); iter.hasNext();) {
                IndexInfo indexInfo = (IndexInfo) iter.next();
                buf.append(",");
                buf.append(" INDEX `" + indexInfo.getIndexName() + "`(`" + indexInfo.getColumnName() + "`)");
            }
        }
        if (this.columnList.size() == 1 && this.indexList.size() == 0) { // 只有一个字段，且无索引可建时，将此字段建立索引
            ColumnInfo columnInfo = (ColumnInfo) this.columnList.get(0);
            buf.append(",");
            buf.append(" INDEX `" + tableName + "_" + columnInfo.getName() + "`(`" + columnInfo.getName() + "`)");
        }
        // 主键信息
        StringBuffer pKeys = new StringBuffer();
        isFirst = true;
        for (Iterator<ColumnInfo> iter = this.primaryKeys.iterator(); iter.hasNext();) {
            ColumnInfo columnInfo = (ColumnInfo) iter.next();
            if (isFirst) {
                isFirst = false;
            } else {
                pKeys.append(", ");
            }
            pKeys.append(columnInfo.getName());
        }
        if (pKeys.length() > 0) {
            buf.append(",");
            buf.append(" primary key (").append(pKeys).append(")");
        }

        buf.append(") DEFAULT CHARSET=utf8;");
        return buf.toString();
    }

    public String getFullName() {
        String fullName = "";
        if (dbName != null && !"".equals(dbName)) {
            fullName += dbName + ".";
        }
        return fullName + this.tableName;
    }

    public String toString() {
        return "TableInfo [tableName=" + tableName + ", hasClob=" + hasClob + "]";
    }
}
