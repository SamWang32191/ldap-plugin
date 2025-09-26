package com.ldapplugin.ui.tree;

import com.unboundid.ldap.sdk.Entry;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * LDAP 樹狀節點
 */
public class LdapTreeNode extends DefaultMutableTreeNode {
    
    private final String displayName;
    private final Entry entry;
    
    public LdapTreeNode(String displayName, Entry entry) {
        super(displayName);
        this.displayName = displayName;
        this.entry = entry;
    }
    
    public String getDisplayName() {
        if (entry != null) {
            // 嘗試取得更友善的顯示名稱
            String cn = entry.getAttributeValue("cn");
            if (cn != null) {
                return cn;
            }
            
            String ou = entry.getAttributeValue("ou");
            if (ou != null) {
                return ou;
            }
            
            String uid = entry.getAttributeValue("uid");
            if (uid != null) {
                return uid;
            }
            
            // 如果沒有友善名稱，使用 DN 的第一部分
            String dn = entry.getDN();
            if (dn != null && !dn.isEmpty()) {
                String[] parts = dn.split(",");
                if (parts.length > 0) {
                    String firstPart = parts[0].trim();
                    if (firstPart.contains("=")) {
                        return firstPart.substring(firstPart.indexOf("=") + 1);
                    }
                }
            }
        }
        
        return displayName;
    }
    
    public Entry getEntry() {
        return entry;
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
}
