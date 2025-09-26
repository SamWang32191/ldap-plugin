package com.ldapplugin.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.ldapplugin.model.LdapConnection;
import com.ldapplugin.service.LdapConnectionService;
import com.ldapplugin.ui.dialog.ConnectionConfigDialog;
import com.ldapplugin.ui.tree.LdapTreeModel;
import com.ldapplugin.ui.tree.LdapTreeNode;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPException;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * LDAP 工具視窗主面板
 */
public class LdapToolWindowPanel extends JPanel {
    
    private final Project project;
    private final LdapConnectionService connectionService;
    private Tree ldapTree;
    private LdapTreeModel treeModel;
    private JTextArea detailsArea;
    private JComboBox<LdapConnection> connectionComboBox;
    
    public LdapToolWindowPanel(Project project) {
        this.project = project;
        this.connectionService = ApplicationManager.getApplication().getService(LdapConnectionService.class);
        initializeUI();
        refreshConnectionList();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // 頂部工具列
        JPanel toolbarPanel = createToolbarPanel();
        add(toolbarPanel, BorderLayout.NORTH);
        
        // 主要內容區域
        JBSplitter splitter = new JBSplitter(false, 0.6f);
        
        // 左側：LDAP 樹狀視圖
        JPanel treePanel = createTreePanel();
        splitter.setFirstComponent(treePanel);
        
        // 右側：詳細資訊面板
        JPanel detailsPanel = createDetailsPanel();
        splitter.setSecondComponent(detailsPanel);
        
        add(splitter, BorderLayout.CENTER);
    }
    
    private JPanel createToolbarPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // 連線選擇下拉選單
        connectionComboBox = new JComboBox<>();
        connectionComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LdapConnection) {
                    LdapConnection conn = (LdapConnection) value;
                    // 顯示連線狀態文字
                    if (conn.isConnected()) {
                        setText(conn.getName() + " (" + conn.getHost() + ") [已連線]");
                    } else {
                        setText(conn.getName() + " (" + conn.getHost() + ") [未連線]");
                    }
                }
                return this;
            }
        });
        
        connectionComboBox.addActionListener(e -> onConnectionSelected());
        
        // 按鈕
        JButton newConnectionBtn = new JButton("新增連線");
        JButton connectBtn = new JButton("連線");
        JButton disconnectBtn = new JButton("斷開");
        JButton refreshBtn = new JButton("重新整理");
        
        newConnectionBtn.addActionListener(e -> showConnectionDialog(null));
        connectBtn.addActionListener(e -> connectToSelectedLdap());
        disconnectBtn.addActionListener(e -> disconnectFromSelectedLdap());
        refreshBtn.addActionListener(e -> refreshTree());
        
        panel.add(new JLabel("連線:"));
        panel.add(connectionComboBox);
        panel.add(newConnectionBtn);
        panel.add(connectBtn);
        panel.add(disconnectBtn);
        panel.add(refreshBtn);
        
        return panel;
    }
    
    private JPanel createTreePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("LDAP 目錄"));
        
        treeModel = new LdapTreeModel();
        ldapTree = new Tree(treeModel);
        ldapTree.setCellRenderer(new LdapTreeCellRenderer());
        ldapTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                Object last = event.getPath().getLastPathComponent();
                if (last instanceof LdapTreeNode) {
                    LdapTreeNode node = (LdapTreeNode) last;
                    loadChildrenIfNeeded(node);
                }
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                // 不需處理
            }
        });
        
        ldapTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    TreePath path = ldapTree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        LdapTreeNode node = (LdapTreeNode) path.getLastPathComponent();
                        showEntryDetails(node);
                    }
                }
            }
        });
        
        JBScrollPane scrollPane = new JBScrollPane(ldapTree);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("詳細資訊"));
        
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JBScrollPane scrollPane = new JBScrollPane(detailsArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void refreshConnectionList() {
        connectionComboBox.removeAllItems();
        List<LdapConnection> connections = connectionService.getAllConnections();
        for (LdapConnection connection : connections) {
            connectionComboBox.addItem(connection);
        }
    }
    
    private void onConnectionSelected() {
        LdapConnection selected = (LdapConnection) connectionComboBox.getSelectedItem();
        if (selected != null && selected.isConnected()) {
            refreshTree();
        } else {
            treeModel.setRoot(new LdapTreeNode("未連線", null));
            detailsArea.setText("");
        }
    }
    
    private void showConnectionDialog(LdapConnection connection) {
        ConnectionConfigDialog dialog = new ConnectionConfigDialog(project, connection);
        if (dialog.showAndGet()) {
            LdapConnection newConnection = dialog.getConnection();
            if (connection == null) {
                connectionService.addConnection(newConnection);
            }
            refreshConnectionList();
        }
    }
    
    private void connectToSelectedLdap() {
        LdapConnection selected = (LdapConnection) connectionComboBox.getSelectedItem();
        if (selected == null) {
            Messages.showWarningDialog("請選擇一個連線", "警告");
            return;
        }
        
        try {
            if (connectionService.connect(selected.getName())) {
                Messages.showInfoMessage("連線成功", "資訊");
                refreshTree();
                connectionComboBox.repaint(); // 更新圖示
            } else {
                Messages.showErrorDialog("連線失敗", "錯誤");
            }
        } catch (LDAPException e) {
            Messages.showErrorDialog("連線錯誤: " + e.getMessage(), "錯誤");
        }
    }
    
    private void disconnectFromSelectedLdap() {
        LdapConnection selected = (LdapConnection) connectionComboBox.getSelectedItem();
        if (selected != null) {
            connectionService.disconnect(selected.getName());
            Messages.showInfoMessage("已斷開連線", "資訊");
            treeModel.setRoot(new LdapTreeNode("未連線", null));
            detailsArea.setText("");
            connectionComboBox.repaint(); // 更新圖示
        }
    }
    
    private void refreshTree() {
        LdapConnection selected = (LdapConnection) connectionComboBox.getSelectedItem();
        if (selected == null || !selected.isConnected()) {
            return;
        }
        
        try {
            // 從基礎 DN 開始載入樹狀結構
            String baseDn = selected.getBaseDn();
            if (baseDn == null || baseDn.trim().isEmpty()) {
                baseDn = ""; // 根目錄
            }
            
            List<Entry> children = connectionService.getChildren(selected.getName(), baseDn);
            LdapTreeNode rootNode = new LdapTreeNode(baseDn.isEmpty() ? "根目錄" : baseDn, null);
            
            for (Entry entry : children) {
                LdapTreeNode childNode = new LdapTreeNode(entry.getDN(), entry);
                // 先放一個 placeholder 讓節點可展開，實際 children 於展開時載入
                childNode.add(new LdapTreeNode("...", null));
                rootNode.add(childNode);
            }
            
            treeModel.setRoot(rootNode);
            ldapTree.expandRow(0);
            
        } catch (LDAPException e) {
            Messages.showErrorDialog("載入樹狀結構失敗: " + e.getMessage(), "錯誤");
        }
    }
    
    private void loadChildrenIfNeeded(LdapTreeNode node) {
        if (node.getEntry() == null) {
            return; // 根或 placeholder 無需載入
        }
        if (node.isChildrenLoaded()) {
            return;
        }

        LdapConnection selected = (LdapConnection) connectionComboBox.getSelectedItem();
        if (selected == null || !selected.isConnected()) {
            return;
        }

        try {
            String parentDn = node.getEntry().getDN();
            List<Entry> children = connectionService.getChildren(selected.getName(), parentDn);

            node.removeAllChildren();
            for (Entry entry : children) {
                LdapTreeNode childNode = new LdapTreeNode(entry.getDN(), entry);
                // 亦加入 placeholder 以支援更深層展開
                childNode.add(new LdapTreeNode("...", null));
                node.add(childNode);
            }

            node.setChildrenLoaded(true);
            treeModel.reload(node);
        } catch (LDAPException e) {
            Messages.showErrorDialog("載入子節點失敗: " + e.getMessage(), "錯誤");
        }
    }

    private void showEntryDetails(LdapTreeNode node) {
        if (node.getEntry() == null) {
            detailsArea.setText("沒有詳細資訊");
            return;
        }
        
        Entry entry = node.getEntry();
        StringBuilder details = new StringBuilder();
        details.append("DN: ").append(entry.getDN()).append("\n\n");
        
        entry.getAttributes().forEach(attr -> {
            details.append(attr.getName()).append(": ");
            String[] values = attr.getValues();
            if (values.length == 1) {
                details.append(values[0]);
            } else {
                details.append("\n");
                for (String value : values) {
                    details.append("  - ").append(value).append("\n");
                }
            }
            details.append("\n");
        });
        
        detailsArea.setText(details.toString());
        detailsArea.setCaretPosition(0);
    }
    
    
    /**
     * LDAP 樹狀視圖單元格渲染器
     */
    private static class LdapTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            
            if (value instanceof LdapTreeNode) {
                LdapTreeNode node = (LdapTreeNode) value;
                setText(node.getDisplayName());
                
                if (node.getEntry() != null) {
                    // 根據物件類別在文字前添加標識符
                    Entry entry = node.getEntry();
                    String displayText = node.getDisplayName();
                    if (entry.hasObjectClass("organizationalUnit")) {
                        setText("[OU] " + displayText);
                    } else if (entry.hasObjectClass("person") || entry.hasObjectClass("user")) {
                        setText("[User] " + displayText);
                    } else {
                        setText("[Entry] " + displayText);
                    }
                }
            }
            
            return this;
        }
    }
}
