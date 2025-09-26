package com.ldapplugin.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * LDAP 條目編輯對話框
 */
public class EntryEditDialog extends DialogWrapper {
    
    private final Entry originalEntry;
    private JBTable attributeTable;
    private AttributeTableModel tableModel;
    private JLabel dnLabel;
    
    public EntryEditDialog(@Nullable Project project, Entry entry) {
        super(project, true);
        this.originalEntry = entry;
        
        setTitle("編輯 LDAP 條目");
        setResizable(true);
        init();
    }
    
    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(600, 400));
        
        // 頂部：顯示 DN
        JPanel dnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dnPanel.add(new JLabel("DN: "));
        dnLabel = new JLabel(originalEntry.getDN());
        dnLabel.setFont(dnLabel.getFont().deriveFont(Font.BOLD));
        dnPanel.add(dnLabel);
        mainPanel.add(dnPanel, BorderLayout.NORTH);
        
        // 中間：屬性編輯表格
        createAttributeTable();
        JBScrollPane scrollPane = new JBScrollPane(attributeTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("屬性"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 底部：操作按鈕
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("新增屬性");
        JButton removeButton = new JButton("移除屬性");
        
        addButton.addActionListener(e -> addNewAttribute());
        removeButton.addActionListener(e -> removeSelectedAttribute());
        
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private void createAttributeTable() {
        tableModel = new AttributeTableModel(originalEntry);
        attributeTable = new JBTable(tableModel);
        
        // 設置欄位寬度
        attributeTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        attributeTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        attributeTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        
        // 設置多值屬性的自定義渲染器和編輯器
        attributeTable.getColumnModel().getColumn(1).setCellRenderer(new MultiValueCellRenderer());
        attributeTable.getColumnModel().getColumn(1).setCellEditor(new MultiValueCellEditor());
        
        // 設置行高
        attributeTable.setRowHeight(25);
    }
    
    private void addNewAttribute() {
        String attributeName = Messages.showInputDialog(
            "請輸入新屬性名稱：",
            "新增屬性",
            Messages.getQuestionIcon()
        );
        
        if (attributeName != null && !attributeName.trim().isEmpty()) {
            String attributeValue = Messages.showInputDialog(
                "請輸入屬性值：",
                "新增屬性",
                Messages.getQuestionIcon()
            );
            
            if (attributeValue != null) {
                tableModel.addAttribute(attributeName.trim(), attributeValue);
            }
        }
    }
    
    private void removeSelectedAttribute() {
        int selectedRow = attributeTable.getSelectedRow();
        if (selectedRow >= 0) {
            String attributeName = (String) tableModel.getValueAt(selectedRow, 0);
            int result = Messages.showYesNoDialog(
                "確定要移除屬性 '" + attributeName + "' 嗎？",
                "確認移除",
                Messages.getQuestionIcon()
            );
            
            if (result == Messages.YES) {
                tableModel.removeAttribute(selectedRow);
            }
        } else {
            Messages.showWarningDialog("請選擇要移除的屬性", "警告");
        }
    }
    
    /**
     * 取得修改後的條目
     */
    public Entry getModifiedEntry() {
        return tableModel.createModifiedEntry();
    }
    
    /**
     * 檢查是否有修改
     */
    public boolean isModified() {
        return tableModel.isModified();
    }
    
    /**
     * 屬性表格模型
     */
    private static class AttributeTableModel extends AbstractTableModel {
        private final String[] columnNames = {"屬性名稱", "值", "多值"};
        private final List<AttributeRow> attributes = new ArrayList<>();
        private final Entry originalEntry;
        private boolean modified = false;
        
        public AttributeTableModel(Entry entry) {
            this.originalEntry = entry;
            loadAttributes();
        }
        
        private void loadAttributes() {
            for (Attribute attr : originalEntry.getAttributes()) {
                String[] values = attr.getValues();
                if (values.length == 1) {
                    attributes.add(new AttributeRow(attr.getName(), values[0], false));
                } else {
                    attributes.add(new AttributeRow(attr.getName(), String.join("; ", values), true));
                }
            }
        }
        
        @Override
        public int getRowCount() {
            return attributes.size();
        }
        
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 2) {
                return Boolean.class;
            }
            return String.class;
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            // DN 屬性不可編輯
            String attrName = attributes.get(rowIndex).name;
            if ("dn".equalsIgnoreCase(attrName) || "distinguishedName".equalsIgnoreCase(attrName)) {
                return false;
            }
            // 屬性名稱和值可以編輯，多值標記不可編輯
            return columnIndex != 2;
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            AttributeRow row = attributes.get(rowIndex);
            switch (columnIndex) {
                case 0: return row.name;
                case 1: return row.value;
                case 2: return row.isMultiValue;
                default: return null;
            }
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            AttributeRow row = attributes.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    row.name = (String) aValue;
                    modified = true;
                    break;
                case 1:
                    row.value = (String) aValue;
                    // 檢查是否為多值（包含分號分隔符）
                    row.isMultiValue = ((String) aValue).contains(";");
                    modified = true;
                    break;
            }
            fireTableCellUpdated(rowIndex, columnIndex);
            if (columnIndex == 1) {
                fireTableCellUpdated(rowIndex, 2); // 更新多值欄位
            }
        }
        
        public void addAttribute(String name, String value) {
            attributes.add(new AttributeRow(name, value, value.contains(";")));
            modified = true;
            fireTableRowsInserted(attributes.size() - 1, attributes.size() - 1);
        }
        
        public void removeAttribute(int rowIndex) {
            if (rowIndex >= 0 && rowIndex < attributes.size()) {
                attributes.remove(rowIndex);
                modified = true;
                fireTableRowsDeleted(rowIndex, rowIndex);
            }
        }
        
        public boolean isModified() {
            return modified;
        }
        
        public Entry createModifiedEntry() {
            List<Attribute> attrs = new ArrayList<>();
            
            for (AttributeRow row : attributes) {
                if (row.isMultiValue) {
                    String[] values = row.value.split(";");
                    for (int i = 0; i < values.length; i++) {
                        values[i] = values[i].trim();
                    }
                    attrs.add(new Attribute(row.name, values));
                } else {
                    attrs.add(new Attribute(row.name, row.value));
                }
            }
            
            return new Entry(originalEntry.getDN(), attrs);
        }
        
        private static class AttributeRow {
            String name;
            String value;
            boolean isMultiValue;
            
            public AttributeRow(String name, String value, boolean isMultiValue) {
                this.name = name;
                this.value = value;
                this.isMultiValue = isMultiValue;
            }
        }
    }
    
    /**
     * 多值屬性單元格渲染器
     */
    private static class MultiValueCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null && value.toString().contains(";")) {
                setToolTipText("多值屬性，使用分號(;)分隔");
                setForeground(isSelected ? Color.WHITE : Color.BLUE);
            } else {
                setToolTipText(null);
                setForeground(isSelected ? Color.WHITE : Color.BLACK);
            }
            
            return c;
        }
    }
    
    /**
     * 多值屬性單元格編輯器
     */
    private static class MultiValueCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JBTextField textField;
        private JButton editButton;
        private JPanel panel;
        private String currentValue;
        
        public MultiValueCellEditor() {
            textField = new JBTextField();
            editButton = new JButton("...");
            editButton.setPreferredSize(new Dimension(25, 20));
            
            panel = new JPanel(new BorderLayout());
            panel.add(textField, BorderLayout.CENTER);
            panel.add(editButton, BorderLayout.EAST);
            
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showMultiValueEditor();
                }
            });
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentValue = value != null ? value.toString() : "";
            textField.setText(currentValue);
            
            // 如果是多值屬性，顯示編輯按鈕
            if (currentValue.contains(";")) {
                return panel;
            } else {
                return textField;
            }
        }
        
        @Override
        public Object getCellEditorValue() {
            return textField.getText();
        }
        
        private void showMultiValueEditor() {
            String[] values = currentValue.split(";");
            for (int i = 0; i < values.length; i++) {
                values[i] = values[i].trim();
            }
            
            MultiValueEditDialog dialog = new MultiValueEditDialog(values);
            if (dialog.showAndGet()) {
                String[] newValues = dialog.getValues();
                textField.setText(String.join("; ", newValues));
                fireEditingStopped();
            }
        }
    }
    
    /**
     * 多值編輯對話框
     */
    private static class MultiValueEditDialog extends DialogWrapper {
        private JList<String> valueList;
        private DefaultListModel<String> listModel;
        
        public MultiValueEditDialog(String[] values) {
            super(true);
            setTitle("編輯多值屬性");
            
            listModel = new DefaultListModel<>();
            for (String value : values) {
                if (!value.trim().isEmpty()) {
                    listModel.addElement(value.trim());
                }
            }
            
            init();
        }
        
        @Override
        protected @Nullable JComponent createCenterPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setPreferredSize(new Dimension(400, 300));
            
            valueList = new JList<>(listModel);
            valueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JBScrollPane scrollPane = new JBScrollPane(valueList);
            panel.add(scrollPane, BorderLayout.CENTER);
            
            // 按鈕面板
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton addButton = new JButton("新增");
            JButton editButton = new JButton("編輯");
            JButton removeButton = new JButton("移除");
            
            addButton.addActionListener(e -> addValue());
            editButton.addActionListener(e -> editValue());
            removeButton.addActionListener(e -> removeValue());
            
            buttonPanel.add(addButton);
            buttonPanel.add(editButton);
            buttonPanel.add(removeButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            
            return panel;
        }
        
        private void addValue() {
            String value = Messages.showInputDialog("請輸入新值：", "新增值", Messages.getQuestionIcon());
            if (value != null && !value.trim().isEmpty()) {
                listModel.addElement(value.trim());
            }
        }
        
        private void editValue() {
            int selectedIndex = valueList.getSelectedIndex();
            if (selectedIndex >= 0) {
                String currentValue = listModel.getElementAt(selectedIndex);
                String newValue = Messages.showInputDialog(getContentPanel(), "請輸入新值：", "編輯值", Messages.getQuestionIcon(), currentValue, null);
                if (newValue != null && !newValue.trim().isEmpty()) {
                    listModel.setElementAt(newValue.trim(), selectedIndex);
                }
            } else {
                Messages.showWarningDialog("請選擇要編輯的值", "警告");
            }
        }
        
        private void removeValue() {
            int selectedIndex = valueList.getSelectedIndex();
            if (selectedIndex >= 0) {
                listModel.removeElementAt(selectedIndex);
            } else {
                Messages.showWarningDialog("請選擇要移除的值", "警告");
            }
        }
        
        public String[] getValues() {
            String[] values = new String[listModel.getSize()];
            for (int i = 0; i < listModel.getSize(); i++) {
                values[i] = listModel.getElementAt(i);
            }
            return values;
        }
    }
}
