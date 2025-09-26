package com.ldapplugin.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.ldapplugin.model.LdapConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * LDAP 設定狀態管理
 */
@Service
@State(
    name = "LdapSettingsState",
    storages = @Storage("ldap-plugin-settings.xml")
)
public final class LdapSettingsState implements PersistentStateComponent<LdapSettingsState> {
    
    public List<ConnectionData> connections = new ArrayList<>();
    
    public static LdapSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(LdapSettingsState.class);
    }
    
    @Override
    public @Nullable LdapSettingsState getState() {
        return this;
    }
    
    @Override
    public void loadState(@NotNull LdapSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
    
    public List<LdapConnection> getConnections() {
        List<LdapConnection> result = new ArrayList<>();
        for (ConnectionData data : connections) {
            LdapConnection connection = new LdapConnection();
            connection.setName(data.name);
            connection.setHost(data.host);
            connection.setPort(data.port);
            connection.setBindDn(data.bindDn);
            connection.setPassword(data.password); // 注意：在生產環境中應該加密存儲密碼
            connection.setUseSSL(data.useSSL);
            connection.setBaseDn(data.baseDn);
            result.add(connection);
        }
        return result;
    }
    
    public void setConnections(List<LdapConnection> connections) {
        this.connections.clear();
        for (LdapConnection connection : connections) {
            ConnectionData data = new ConnectionData();
            data.name = connection.getName();
            data.host = connection.getHost();
            data.port = connection.getPort();
            data.bindDn = connection.getBindDn();
            data.password = connection.getPassword(); // 注意：在生產環境中應該加密存儲密碼
            data.useSSL = connection.isUseSSL();
            data.baseDn = connection.getBaseDn();
            this.connections.add(data);
        }
    }
    
    /**
     * 連線資料類別，用於序列化
     */
    public static class ConnectionData {
        public String name = "";
        public String host = "";
        public int port = 389;
        public String bindDn = "";
        public String password = "";
        public boolean useSSL = false;
        public String baseDn = "";
    }
}
