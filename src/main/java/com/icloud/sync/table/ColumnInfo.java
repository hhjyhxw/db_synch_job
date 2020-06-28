package com.icloud.sync.table;

import java.sql.Types;

public class ColumnInfo {
    //https://blog.csdn.net/u012255097/article/details/53036452
    private String catalogName;// 指定列的表目录名称
    private String tableName;//列的表名称
    private String name;//ColumnName 列的名称
    private int type;//ColumnType   SQL 类型
    private String typeName;// ColumnTypeName 列的数据库特定的类型名称
    private String schemaName;// SchemaName 列的表模式
    private String comment;
    private String defaultValue;
    private int displaySize;// 列的最大标准宽度，以字符为单位
    private int scale;// 列的小数点右边的位数
    private boolean isNotNull;
    private boolean isClob;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public int getDisplaySize() {
        return displaySize;
    }

    public void setDisplaySize(int displaySize) {
        this.displaySize = displaySize;
    }

    public boolean isClob() {
        return isClob;
    }

    public void setIsClob(boolean isClob) {
        this.isClob = isClob;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isNotNull() {
        return isNotNull;
    }

    public void setNotNull(boolean isNotNull) {
        this.isNotNull = isNotNull;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnDDL() {
        StringBuffer columnDDL = new StringBuffer();
        // 字段名
        columnDDL.append("`");
        columnDDL.append(this.getName());
        columnDDL.append("` ");
        // 字段类型
        boolean addSize = true;
        switch (this.getType()) {
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                columnDDL.append("int");
                break;
            case Types.BIGINT:
                columnDDL.append("decimal");
                break;
            case Types.DATE:
                columnDDL.append("datetime");
                addSize = false;
                break;
            case Types.TIMESTAMP:
                columnDDL.append("datetime");
                addSize = false;
                break;
            case Types.VARCHAR:
                if (this.getDisplaySize() >= 3000) {
                    columnDDL.append("text");
                    addSize = false;
                } else {
                    columnDDL.append("varchar");
                }
                break;
            case Types.NUMERIC:
                columnDDL.append("numeric");
                break;
            case Types.BLOB:
            case Types.CLOB:
            case Types.LONGVARCHAR:
                columnDDL.append("text");
                addSize = false;
                break;
            case Types.CHAR:
                columnDDL.append("char");
                break;
            case Types.DECIMAL:
                columnDDL.append("decimal");
                break;
            default:
                if ("UROWID".equalsIgnoreCase(this.getTypeName())) {
                    columnDDL.append("text");
                    addSize = false;
                } else {
                    System.out.println(0);
                }
        }
        if (addSize && this.getDisplaySize() > 0) {
            if (this.getScale() > 0) {
                columnDDL.append("(" + (this.getDisplaySize() - this.getScale()) + ", " + this.getScale() + ")");
            } else {
                columnDDL.append("(" + this.getDisplaySize() + ")");
            }
        }
        columnDDL.append(" ");
        // 是否为空
        if (this.isNotNull()) {
            columnDDL.append(" NOT NULL ");
        }
        // 默认值
        if (this.getDefaultValue() != null && !"".equals(this.getDefaultValue())) {
            columnDDL.append(" DEFAULT ");
            if (this.getDefaultValue().trim().equalsIgnoreCase("sysdate")) {
                columnDDL.append("CURRENT_TIMESTAMP");
            } else {
                columnDDL.append("'");
                columnDDL.append(this.getDefaultValue().trim());
                columnDDL.append("'");
            }
        }
        // 注释
        if (this.getComment() != null && !"".equals(this.getComment())) {
            columnDDL.append(" COMMENT '");
            columnDDL.append(this.getComment());
            columnDDL.append("'");
        }
        columnDDL.append(" ");
        return columnDDL.toString();
    }

    public String toString() {
        return "ColumnInfo [catalogName=" + catalogName + ", tableName=" + tableName + ", name=" + name + ", type=" + type + ", typeName="
                + typeName + ", schemaName=" + schemaName + ", comment=" + comment + ", defaultValue=" + defaultValue + ", displaySize="
                + displaySize + ", scale=" + scale + ", isNotNull=" + isNotNull + ", isClob=" + isClob + "]";
    }
}
