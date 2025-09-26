package com.ldapplugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.ldapplugin.model.LdapConnection;
import com.ldapplugin.service.LdapConnectionService;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 斷開 LDAP 連線動作
 */
public class DisconnectLdapAction extends AnAction {
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        LdapConnectionService service = ApplicationManager.getApplication().getService(LdapConnectionService.class);
        List<LdapConnection> connections = service.getAllConnections();
        
        List<LdapConnection> connectedConnections = connections.stream()
                .filter(LdapConnection::isConnected)
                .toList();
        
        if (connectedConnections.isEmpty()) {
            Messages.showInfoMessage("沒有已連線的 LDAP 伺服器", "資訊");
            return;
        }
        
        if (connectedConnections.size() == 1) {
            LdapConnection connection = connectedConnections.get(0);
            service.disconnect(connection.getName());
            Messages.showInfoMessage("已斷開連線: " + connection.getName(), "資訊");
        } else {
            // 多個連線時顯示選擇對話框
            String[] connectionNames = connectedConnections.stream()
                    .map(LdapConnection::getName)
                    .toArray(String[]::new);
            
            String selected = Messages.showEditableChooseDialog(
                    "選擇要斷開的連線:",
                    "斷開 LDAP 連線",
                    Messages.getQuestionIcon(),
                    connectionNames,
                    connectionNames[0],
                    null
            );
            
            if (selected != null) {
                service.disconnect(selected);
                Messages.showInfoMessage("已斷開連線: " + selected, "資訊");
            }
        }
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        LdapConnectionService service = ApplicationManager.getApplication().getService(LdapConnectionService.class);
        List<LdapConnection> connections = service.getAllConnections();
        
        boolean hasConnectedConnections = connections.stream().anyMatch(LdapConnection::isConnected);
        e.getPresentation().setEnabled(hasConnectedConnections);
    }
}
