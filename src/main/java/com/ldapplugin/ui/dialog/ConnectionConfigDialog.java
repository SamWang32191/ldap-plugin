package com.ldapplugin.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBTextField;
import com.ldapplugin.model.LdapConnection;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * LDAP 連線配置對話框
 */
public class ConnectionConfigDialog extends DialogWrapper {
    
    private JBTextField nameField;
    private JBTextField hostField;
    private JSpinner portSpinner;
    private JBTextField bindDnField;
    private JPasswordField passwordField;
    private JCheckBox sslCheckBox;
    private JBTextField baseDnField;
    private JButton testButton;
    
    private LdapConnection connection;
    private final boolean isEdit;
    
    public ConnectionConfigDialog(@Nullable Project project, @Nullable LdapConnection connection) {
        super(project);
        this.connection = connection;
        this.isEdit = connection != null;
        
        setTitle(isEdit ? "編輯 LDAP 連線" : "新增 LDAP 連線");
        init();
        
        if (isEdit) {
            populateFields();
        }
    }
    
    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 連線名稱
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("連線名稱:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        nameField = new JBTextField(20);
        panel.add(nameField, gbc);
        
        // 主機
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("主機:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        hostField = new JBTextField(20);
        panel.add(hostField, gbc);
        
        // 端口
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("端口:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        portSpinner = new JSpinner(new SpinnerNumberModel(389, 1, 65535, 1));
        panel.add(portSpinner, gbc);
        
        // SSL
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("使用 SSL:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        sslCheckBox = new JCheckBox();
        sslCheckBox.addActionListener(e -> {
            if (sslCheckBox.isSelected() && (Integer) portSpinner.getValue() == 389) {
                portSpinner.setValue(636);
            } else if (!sslCheckBox.isSelected() && (Integer) portSpinner.getValue() == 636) {
                portSpinner.setValue(389);
            }
        });
        panel.add(sslCheckBox, gbc);
        
        // 綁定 DN
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("綁定 DN:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        bindDnField = new JBTextField(30);
        panel.add(bindDnField, gbc);
        
        // 密碼
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("密碼:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);
        
        // 基礎 DN
        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("基礎 DN:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        baseDnField = new JBTextField(30);
        panel.add(baseDnField, gbc);
        
        // 測試連線按鈕
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        testButton = new JButton("測試連線");
        testButton.addActionListener(e -> testConnection());
        panel.add(testButton, gbc);
        
        return panel;
    }
    
    private void populateFields() {
        if (connection != null) {
            nameField.setText(connection.getName());
            hostField.setText(connection.getHost());
            portSpinner.setValue(connection.getPort());
            bindDnField.setText(connection.getBindDn());
            passwordField.setText(connection.getPassword());
            sslCheckBox.setSelected(connection.isUseSSL());
            baseDnField.setText(connection.getBaseDn());
        }
    }
    
    private void testConnection() {
        LdapConnection testConn = createConnectionFromFields();
        if (testConn == null) {
            return;
        }
        
        testButton.setEnabled(false);
        testButton.setText("測試中...");
        
        SwingUtilities.invokeLater(() -> {
            try {
                boolean success = testConn.testConnection();
                if (success) {
                    Messages.showInfoMessage("連線測試成功！", "測試結果");
                } else {
                    Messages.showErrorDialog("連線測試失敗！", "測試結果");
                }
            } catch (Exception e) {
                Messages.showErrorDialog("連線測試失敗: " + e.getMessage(), "測試結果");
            } finally {
                testConn.disconnect();
                testButton.setEnabled(true);
                testButton.setText("測試連線");
            }
        });
    }
    
    private LdapConnection createConnectionFromFields() {
        String name = nameField.getText().trim();
        String host = hostField.getText().trim();
        String bindDn = bindDnField.getText().trim();
        String password = new String(passwordField.getPassword());
        String baseDn = baseDnField.getText().trim();
        
        if (name.isEmpty()) {
            Messages.showErrorDialog("請輸入連線名稱", "錯誤");
            nameField.requestFocus();
            return null;
        }
        
        if (host.isEmpty()) {
            Messages.showErrorDialog("請輸入主機地址", "錯誤");
            hostField.requestFocus();
            return null;
        }
        
        int port = (Integer) portSpinner.getValue();
        boolean useSSL = sslCheckBox.isSelected();
        
        return new LdapConnection(name, host, port, bindDn, password, useSSL, baseDn);
    }
    
    @Override
    protected void doOKAction() {
        connection = createConnectionFromFields();
        if (connection != null) {
            super.doOKAction();
        }
    }
    
    public LdapConnection getConnection() {
        return connection;
    }
}
