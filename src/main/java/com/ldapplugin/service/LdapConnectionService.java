package com.ldapplugin.service;

import com.ldapplugin.model.LdapConnection;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResult;

import java.util.List;

/**
 * LDAP 連線服務介面
 */
public interface LdapConnectionService {
    
    /**
     * 添加 LDAP 連線
     */
    void addConnection(LdapConnection connection);
    
    /**
     * 移除 LDAP 連線
     */
    void removeConnection(String connectionName);
    
    /**
     * 取得所有連線
     */
    List<LdapConnection> getAllConnections();
    
    /**
     * 根據名稱取得連線
     */
    LdapConnection getConnection(String name);
    
    /**
     * 連線到 LDAP 伺服器
     */
    boolean connect(String connectionName) throws LDAPException;
    
    /**
     * 斷開 LDAP 連線
     */
    void disconnect(String connectionName);
    
    /**
     * 測試連線
     */
    boolean testConnection(LdapConnection connection);
    
    /**
     * 搜尋 LDAP 條目
     */
    SearchResult search(String connectionName, String baseDn, String filter, String... attributes) throws LDAPException;
    
    /**
     * 取得子條目
     */
    List<Entry> getChildren(String connectionName, String parentDn) throws LDAPException;
    
    /**
     * 新增 LDAP 條目
     */
    void addEntry(String connectionName, Entry entry) throws LDAPException;
    
    /**
     * 修改 LDAP 條目
     */
    void modifyEntry(String connectionName, String dn, String attributeName, String newValue) throws LDAPException;
    
    /**
     * 刪除 LDAP 條目
     */
    void deleteEntry(String connectionName, String dn) throws LDAPException;
    
    /**
     * 取得條目詳細資訊
     */
    Entry getEntry(String connectionName, String dn) throws LDAPException;
}
