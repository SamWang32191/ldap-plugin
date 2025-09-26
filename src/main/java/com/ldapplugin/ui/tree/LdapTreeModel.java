package com.ldapplugin.ui.tree;

import javax.swing.tree.DefaultTreeModel;

/**
 * LDAP 樹狀模型
 */
public class LdapTreeModel extends DefaultTreeModel {
    
    public LdapTreeModel() {
        super(new LdapTreeNode("未連線", null));
    }
    
    public void setRoot(LdapTreeNode root) {
        super.setRoot(root);
        reload();
    }
}
