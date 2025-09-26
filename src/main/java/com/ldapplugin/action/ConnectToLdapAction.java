package com.ldapplugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.ldapplugin.service.LdapConnectionService;
import com.ldapplugin.ui.dialog.ConnectionConfigDialog;
import org.jetbrains.annotations.NotNull;

/**
 * 連線到 LDAP 動作
 */
public class ConnectToLdapAction extends AnAction {
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ConnectionConfigDialog dialog = new ConnectionConfigDialog(e.getProject(), null);
        if (dialog.showAndGet()) {
            LdapConnectionService service = ApplicationManager.getApplication().getService(LdapConnectionService.class);
            service.addConnection(dialog.getConnection());
        }
    }
}
