package com.icloud.sync;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.icloud.util.Constants;

public class ConnectionInfo {
    private String dbId;
    private String dbName;
    private String dbPort;
    private String dbUser;
    private String dbPassword;
    private String dbDriver;
    private String dbTns;
    private String dbType;
    private boolean cache = true;
    private int indexHost = 0;
    private Connection connection;

    private int connectionTime = 10; // 连接失败时重新连接次数年
    private int waitTime = 5; // 重新链接等待时间

    private Logger logger = Logger.getLogger(ConnectionInfo.class);

    public String getDbId() {
        return dbId;
    }

    public void setDbId(String dbId) {
        this.dbId = dbId;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public boolean isCache() {
        return cache;
    }

    public void setCache(boolean cache) {
        this.cache = cache;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbPort() {
        return dbPort;
    }

    public void setDbPort(String dbPort) {
        this.dbPort = dbPort;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDbDriver() {
        return dbDriver;
    }

    public void setDbDriver(String dbDriver) {
        this.dbDriver = dbDriver;
        if (this.dbDriver != null && !"".equals(this.dbDriver)) {
            if (this.dbDriver.toUpperCase().indexOf(Constants.DB_TYPE_ORACLE.toUpperCase()) >= 0) {
                this.setDbType(Constants.DB_TYPE_ORACLE);
            } else if (this.dbDriver.toUpperCase().indexOf(Constants.DB_TYPE_MYSQL.toUpperCase()) >= 0) {
                this.setDbType(Constants.DB_TYPE_MYSQL);
            }
        }
    }

    public String getDbTns() {
        return dbTns;
    }

    public void setDbTns(String dbTns) {
        this.dbTns = dbTns;
    }

    public Connection getConnection() throws Exception {
        if (this.connection != null && !this.connection.isClosed()) {
            return this.connection;
        } else {
            this.init();
            return this.connection;
        }
    }

    private String getDbTnsUrl() {
        return this.getDbTns();
    }

    private void init() throws Exception {
        this.initTns();
    }

    private void initTns() throws Exception {
        while (this.connectionTime > 0) {
            try {
                Class.forName(this.getDbDriver());
                this.logger.info("创建链接中：" + this.getDbTnsUrl() + "," + this.getDbDriver());
                this.connection = DriverManager.getConnection(this.getDbTnsUrl(), this.getDbUser(), this.getDbPassword());
                this.logger.debug("创建链接成功：" + this.getDbTnsUrl() + "," + this.getDbDriver());
                break;
            } catch (SQLException e) {
                this.connectionTime--;
                if (this.connectionTime <= 0) {
                    throw e;
                }
                this.logger.warn("创建链接失败。", e);
            }
            this.logger.info("创建链接失败，等待" + this.waitTime + "秒后重试...");
            Thread.sleep(this.waitTime * 1000); // n秒后重试
        }
    }

    public String toString() {
        String hosts = "";
        return "ConnectionInfo [dbId=" + dbId + ", dbName=" + dbName + ", dbTnds=" + this.dbTns + "dbHosts=" + hosts + ", dbPort=" + dbPort + ", dbUser=" + dbUser
                + ", dbPassword=" + dbPassword + ", dbDriver=" + dbDriver + ", dbType=" + dbType + ", cache=" + cache + ", indexHost=" + indexHost + "]";
    }
}
