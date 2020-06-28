package com.icloud.sync.valueobject;

public class DbDataInfo {
    private Integer dbId;
    private String dbLink;
    private String dbUser;
    private String dbPassword;
    private String dbDriver;
    private String dbTns;
    private String bak;

    public Integer getDbId() {
        return dbId;
    }

    public void setDbId(Integer dbId) {
        this.dbId = dbId;
    }

    public String getDbLink() {
        return dbLink;
    }

    public void setDbLink(String dbLink) {
        this.dbLink = dbLink;
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
    }

    public String getDbTns() {
        return dbTns;
    }

    public void setDbTns(String dbTns) {
        this.dbTns = dbTns;
    }

    public String getBak() {
        return bak;
    }

    public void setBak(String bak) {
        this.bak = bak;
    }

    public String toString() {
        return "DbDataInfo [dbId=" + dbId + ", dbLink=" + dbLink + ", dbUser=" + dbUser + ", dbDriver="
                + dbDriver + ", dbTns=" + dbTns + ", bak=" + bak + "]";
    }
}
