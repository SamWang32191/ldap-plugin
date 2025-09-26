package com.ldapplugin.model;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;

import java.util.Objects;

/**
 * LDAP 連線模型類
 */
public class LdapConnection {
    private String name;
    private String host;
    private int port;
    private String bindDn;
    private String password;
    private boolean useSSL;
    private String baseDn;
    private LDAPConnection connection;
    private boolean connected;

    public LdapConnection() {
        this.port = 389; // 預設 LDAP 端口
        this.useSSL = false;
        this.connected = false;
    }

    public LdapConnection(String name, String host, int port, String bindDn, String password, boolean useSSL, String baseDn) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.bindDn = bindDn;
        this.password = password;
        this.useSSL = useSSL;
        this.baseDn = baseDn;
        this.connected = false;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getBindDn() {
        return bindDn;
    }

    public void setBindDn(String bindDn) {
        this.bindDn = bindDn;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isUseSSL() {
        return useSSL;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    public String getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    public LDAPConnection getConnection() {
        return connection;
    }

    public void setConnection(LDAPConnection connection) {
        this.connection = connection;
    }

    public boolean isConnected() {
        return connected && connection != null && connection.isConnected();
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * 建立 LDAP 連線
     */
    public void connect() throws LDAPException {
        if (connection != null && connection.isConnected()) {
            return;
        }

        if (useSSL) {
            connection = new LDAPConnection(host, port == 389 ? 636 : port);
        } else {
            connection = new LDAPConnection(host, port);
        }

        if (bindDn != null && !bindDn.trim().isEmpty()) {
            connection.bind(bindDn, password);
        }

        connected = true;
    }

    /**
     * 斷開 LDAP 連線
     */
    public void disconnect() {
        if (connection != null && connection.isConnected()) {
            connection.close();
        }
        connected = false;
    }

    /**
     * 測試連線
     */
    public boolean testConnection() {
        try {
            connect();
            return isConnected();
        } catch (LDAPException e) {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LdapConnection that = (LdapConnection) o;
        return port == that.port &&
                useSSL == that.useSSL &&
                Objects.equals(name, that.name) &&
                Objects.equals(host, that.host) &&
                Objects.equals(bindDn, that.bindDn) &&
                Objects.equals(baseDn, that.baseDn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, host, port, bindDn, useSSL, baseDn);
    }

    @Override
    public String toString() {
        return name + " (" + host + ":" + port + ")";
    }
}
