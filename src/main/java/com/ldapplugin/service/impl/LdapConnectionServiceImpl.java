package com.ldapplugin.service.impl;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.ldapplugin.model.LdapConnection;
import com.ldapplugin.service.LdapConnectionService;
import com.ldapplugin.settings.LdapSettingsState;
import com.unboundid.ldap.sdk.*;
import java.util.stream.Collectors;

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
