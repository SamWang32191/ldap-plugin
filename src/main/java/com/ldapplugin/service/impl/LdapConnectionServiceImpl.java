package com.ldapplugin.service.impl;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.ldapplugin.model.LdapConnection;
import com.ldapplugin.service.LdapConnectionService;
import com.ldapplugin.settings.LdapSettingsState;
import com.unboundid.ldap.sdk.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * LDAP 連線服務實現類
 */
@Service
public final class LdapConnectionServiceImpl implements LdapConnectionService {
    
    private final ConcurrentMap<String, LdapConnection> connections = new ConcurrentHashMap<>();
    private final LdapSettingsState settingsState;
    
    public LdapConnectionServiceImpl() {
        this.settingsState = ApplicationManager.getApplication().getService(LdapSettingsState.class);
        loadConnectionsFromSettings();
    }
    
    private void loadConnectionsFromSettings() {
        List<LdapConnection> savedConnections = settingsState.getConnections();
        for (LdapConnection connection : savedConnections) {
            connections.put(connection.getName(), connection);
        }
    }
    
    private void saveConnectionsToSettings() {
        settingsState.setConnections(new ArrayList<>(connections.values()));
    }
    
    @Override
    public void addConnection(LdapConnection connection) {
        connections.put(connection.getName(), connection);
        saveConnectionsToSettings();
    }
    
    @Override
    public void removeConnection(String connectionName) {
        LdapConnection connection = connections.remove(connectionName);
        if (connection != null) {
            connection.disconnect();
            saveConnectionsToSettings();
        }
    }
    
    @Override
    public void updateConnection(String originalName, LdapConnection connection) {
        // 先斷開舊連線
        LdapConnection oldConnection = connections.get(originalName);
        if (oldConnection != null) {
            oldConnection.disconnect();
        }
        
        // 如果連線名稱改變了，需要移除舊的
        if (!originalName.equals(connection.getName())) {
            connections.remove(originalName);
        }
        
        // 更新連線
        connections.put(connection.getName(), connection);
        saveConnectionsToSettings();
    }
    
    @Override
    public List<LdapConnection> getAllConnections() {
        return new ArrayList<>(connections.values());
    }
    
    @Override
    public LdapConnection getConnection(String name) {
        return connections.get(name);
    }
    
    @Override
    public boolean connect(String connectionName) throws LDAPException {
        LdapConnection connection = connections.get(connectionName);
        if (connection == null) {
            return false;
        }
        
        connection.connect();
        return connection.isConnected();
    }
    
    @Override
    public void disconnect(String connectionName) {
        LdapConnection connection = connections.get(connectionName);
        if (connection != null) {
            connection.disconnect();
        }
    }
    
    @Override
    public boolean testConnection(LdapConnection connection) {
        return connection.testConnection();
    }
    
    @Override
    public SearchResult search(String connectionName, String baseDn, String filter, String... attributes) throws LDAPException {
        LdapConnection connection = connections.get(connectionName);
        if (connection == null || !connection.isConnected()) {
            throw new LDAPException(ResultCode.CONNECT_ERROR, "連線不存在或未連線");
        }
        
        SearchRequest searchRequest = new SearchRequest(baseDn, SearchScope.SUB, filter, attributes);
        return connection.getConnection().search(searchRequest);
    }
    
    @Override
    public List<Entry> getChildren(String connectionName, String parentDn) throws LDAPException {
        LdapConnection connection = connections.get(connectionName);
        if (connection == null || !connection.isConnected()) {
            throw new LDAPException(ResultCode.CONNECT_ERROR, "連線不存在或未連線");
        }
        
        SearchRequest searchRequest = new SearchRequest(parentDn, SearchScope.ONE, "(objectClass=*)", "*");
        SearchResult searchResult = connection.getConnection().search(searchRequest);
        
        return searchResult.getSearchEntries().stream()
                .map(searchEntry -> (Entry) searchEntry)
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    public void addEntry(String connectionName, Entry entry) throws LDAPException {
        LdapConnection connection = connections.get(connectionName);
        if (connection == null || !connection.isConnected()) {
            throw new LDAPException(ResultCode.CONNECT_ERROR, "連線不存在或未連線");
        }
        
        connection.getConnection().add(entry);
    }
    
    @Override
    public void modifyEntry(String connectionName, String dn, String attributeName, String newValue) throws LDAPException {
        LdapConnection connection = connections.get(connectionName);
        if (connection == null || !connection.isConnected()) {
            throw new LDAPException(ResultCode.CONNECT_ERROR, "連線不存在或未連線");
        }
        
        Modification modification = new Modification(ModificationType.REPLACE, attributeName, newValue);
        ModifyRequest modifyRequest = new ModifyRequest(dn, modification);
        
        connection.getConnection().modify(modifyRequest);
    }
    
    @Override
    public void modifyEntry(String connectionName, Entry originalEntry, Entry modifiedEntry) throws LDAPException {
        LdapConnection connection = connections.get(connectionName);
        if (connection == null || !connection.isConnected()) {
            throw new LDAPException(ResultCode.CONNECT_ERROR, "連線不存在或未連線");
        }
        
        List<Modification> modifications = new ArrayList<>();
        
        // 比較原始條目和修改後的條目，找出差異
        for (Attribute modifiedAttr : modifiedEntry.getAttributes()) {
            String attrName = modifiedAttr.getName();
            Attribute originalAttr = originalEntry.getAttribute(attrName);
            
            if (originalAttr == null) {
                // 新增的屬性
                modifications.add(new Modification(ModificationType.ADD, attrName, modifiedAttr.getValues()));
            } else {
                // 檢查屬性值是否有變更
                String[] originalValues = originalAttr.getValues();
                String[] modifiedValues = modifiedAttr.getValues();
                
                if (!java.util.Arrays.equals(originalValues, modifiedValues)) {
                    // 屬性值有變更，替換整個屬性
                    modifications.add(new Modification(ModificationType.REPLACE, attrName, modifiedValues));
                }
            }
        }
        
        // 檢查是否有被刪除的屬性
        for (Attribute originalAttr : originalEntry.getAttributes()) {
            String attrName = originalAttr.getName();
            if (modifiedEntry.getAttribute(attrName) == null) {
                // 屬性被刪除
                modifications.add(new Modification(ModificationType.DELETE, attrName));
            }
        }
        
        // 如果有修改，執行修改請求
        if (!modifications.isEmpty()) {
            ModifyRequest modifyRequest = new ModifyRequest(originalEntry.getDN(), modifications);
            connection.getConnection().modify(modifyRequest);
        }
    }
    
    @Override
    public void deleteEntry(String connectionName, String dn) throws LDAPException {
        LdapConnection connection = connections.get(connectionName);
        if (connection == null || !connection.isConnected()) {
            throw new LDAPException(ResultCode.CONNECT_ERROR, "連線不存在或未連線");
        }
        
        connection.getConnection().delete(dn);
    }
    
    @Override
    public Entry getEntry(String connectionName, String dn) throws LDAPException {
        LdapConnection connection = connections.get(connectionName);
        if (connection == null || !connection.isConnected()) {
            throw new LDAPException(ResultCode.CONNECT_ERROR, "連線不存在或未連線");
        }
        
        return connection.getConnection().getEntry(dn);
    }
}
