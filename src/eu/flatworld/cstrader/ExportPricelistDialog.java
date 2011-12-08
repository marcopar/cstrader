/*
   Copyright 2011 marcopar@gmail.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package eu.flatworld.cstrader;

import eu.flatworld.commons.log.LogX;
import eu.flatworld.cstrader.data.Pricelist;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.logging.Level;
import javax.swing.JFileChooser;

public class ExportPricelistDialog extends javax.swing.JDialog {

    /** Creates new form UpdatePricelistDialog */
    public ExportPricelistDialog(java.awt.Frame parent, Pricelist pricelist) {
        super(parent, true);
        initComponents();
        getRootPane().setDefaultButton(jbCopyExit);
        jtaExportArea.setText(PricelistsTools.exportPricelist(pricelist));
    }
    /* used for export all */

    public ExportPricelistDialog(java.awt.Frame parent, Pricelist pricelists[]) {
        super(parent, true);
        StringBuffer sb = new StringBuffer();
        initComponents();
        getRootPane().setDefaultButton(jbCopyExit);
        for (int i = 0; i < pricelists.length; i++) {
            sb.append(PricelistsTools.exportPricelist(pricelists[i]));
        }
        jtaExportArea.setText(sb.toString());
    }

    void copyToClipBoard() {
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            StringSelection content = new StringSelection(jtaExportArea.getText());
            if (content != null) {
                c.setContents(content, content);
            }
        } catch (Exception ex) {
            LogX.log(Level.WARNING, "Error accessing clipboard", ex, true);
        }
        jbCopyExit.grabFocus();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jpmMenu = new javax.swing.JPopupMenu();
        jmiCopy = new javax.swing.JMenuItem();
        jmiClearAll = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtaExportArea = new javax.swing.JTextArea();
        jbCopyExit = new javax.swing.JButton();
        jbCancel = new javax.swing.JButton();
        jbSave = new javax.swing.JButton();

        jmiCopy.setText("Paste");
        jmiCopy.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiCopyActionPerformed(evt);
            }
        });
        jpmMenu.add(jmiCopy);

        jmiClearAll.setText("jmiClearAll");
        jmiClearAll.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiClearAllActionPerformed(evt);
            }
        });
        jpmMenu.add(jmiClearAll);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Export pricelist");

        jtaExportArea.setColumns(20);
        jtaExportArea.setRows(5);
        jtaExportArea.setComponentPopupMenu(jpmMenu);
        jScrollPane1.setViewportView(jtaExportArea);

        jbCopyExit.setText("Copy to clipboard and exit");
        jbCopyExit.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCopyExitActionPerformed(evt);
            }
        });

        jbCancel.setText("Cancel");
        jbCancel.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCancelActionPerformed(evt);
            }
        });

        jbSave.setText("Save...");
        jbSave.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSaveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE).addGroup(layout.createSequentialGroup().addComponent(jbSave).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 146, Short.MAX_VALUE).addComponent(jbCancel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jbCopyExit))).addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jbCopyExit).addComponent(jbCancel).addComponent(jbSave)).addContainerGap()));

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 531) / 2, (screenSize.height - 536) / 2, 531, 536);
    }// </editor-fold>//GEN-END:initComponents

    private void jbCopyExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCopyExitActionPerformed
        copyToClipBoard();
        dispose();
    }//GEN-LAST:event_jbCopyExitActionPerformed

    private void jbCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCancelActionPerformed
        dispose();
    }//GEN-LAST:event_jbCancelActionPerformed

    private void jmiCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiCopyActionPerformed
        copyToClipBoard();
    }//GEN-LAST:event_jmiCopyActionPerformed

    private void jmiClearAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiClearAllActionPerformed
        jtaExportArea.setText("");
    }//GEN-LAST:event_jmiClearAllActionPerformed

    private void jbSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSaveActionPerformed
        JFileChooser jfc = new JFileChooser();
        int rv = jfc.showSaveDialog(this);

        if (rv != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = jfc.getSelectedFile();
        try {
            Writer owf = new BufferedWriter(new FileWriter(file));
            owf.write(jtaExportArea.getText());
            owf.close();
        } catch (Exception ex) {
            LogX.log(Level.SEVERE, "Error saving exported pricelists: " + file.getAbsolutePath(), ex, true);
        }
    }//GEN-LAST:event_jbSaveActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jbCancel;
    private javax.swing.JButton jbCopyExit;
    private javax.swing.JButton jbSave;
    private javax.swing.JMenuItem jmiClearAll;
    private javax.swing.JMenuItem jmiCopy;
    private javax.swing.JPopupMenu jpmMenu;
    private javax.swing.JTextArea jtaExportArea;
    // End of variables declaration//GEN-END:variables
}