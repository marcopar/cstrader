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

import javax.swing.JOptionPane;

public class PriceWatchDialog extends javax.swing.JDialog {

    ItemProperty itemProperties;

    /** Creates new form ItemPropertyDialog */
    public PriceWatchDialog(java.awt.Frame parent, ItemProperty itemProperties) {
        super(parent, true);
        initComponents();
        this.itemProperties = itemProperties;
        getRootPane().setDefaultButton(jbOk);
        if (itemProperties.getMaximumForSalePrice() != null) {
            jtfMaxForSale.setText("" + itemProperties.getMaximumForSalePrice());
        }
        if (itemProperties.getMinimumWantedPrice() != null) {
            jtfMinWanted.setText("" + itemProperties.getMinimumWantedPrice());
        }
        jlItem.setText(itemProperties.getName());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jlItem = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jtfMaxForSale = new javax.swing.JTextField();
        jtfMinWanted = new javax.swing.JTextField();
        jbOk = new javax.swing.JButton();
        jbCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Edit price watch");

        jLabel1.setText("Edit price watch for");

        jlItem.setText(" ");

        jLabel2.setText("Maximum for sale price:");

        jLabel3.setText("Minimum wanted price:");

        jbOk.setText("OK");
        jbOk.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbOkActionPerformed(evt);
            }
        });

        jbCancel.setText("Cancel");
        jbCancel.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(12, 12, 12).addComponent(jLabel1).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jlItem, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING).addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jtfMinWanted, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE).addComponent(jtfMaxForSale, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE))).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addComponent(jbCancel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jbOk))).addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel1).addComponent(jlItem)).addGap(18, 18, 18).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel2).addComponent(jtfMaxForSale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel3).addComponent(jtfMinWanted, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jbOk).addComponent(jbCancel)).addContainerGap()));

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 410) / 2, (screenSize.height - 162) / 2, 410, 162);
    }// </editor-fold>//GEN-END:initComponents

    private void jbCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCancelActionPerformed
        itemProperties = null;
        dispose();
    }//GEN-LAST:event_jbCancelActionPerformed

    private void jbOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbOkActionPerformed
        if (jtfMaxForSale.getText().length() > 0) {
            try {
                long max = Long.parseLong(jtfMaxForSale.getText());
                if (max <= 0) {
                    throw new Exception();
                }
                itemProperties.setMaximumForSalePrice(max);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Maximum for sale price is not valid. Must be > 0.");
                return;
            }
        } else {
            itemProperties.setMaximumForSalePrice(null);
        }
        if (jtfMinWanted.getText().length() > 0) {
            try {
                long min = Long.parseLong(jtfMinWanted.getText());
                if (min <= 0) {
                    throw new Exception();
                }
                itemProperties.setMinimumWantedPrice(min);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Minimum wanted price is not valid. Must be > 0.");
                return;
            }
        } else {
            itemProperties.setMinimumWantedPrice(null);
        }
        dispose();
    }//GEN-LAST:event_jbOkActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton jbCancel;
    private javax.swing.JButton jbOk;
    private javax.swing.JLabel jlItem;
    private javax.swing.JTextField jtfMaxForSale;
    private javax.swing.JTextField jtfMinWanted;
    // End of variables declaration//GEN-END:variables

    public ItemProperty getItemPorperties() {
        return itemProperties;
    }
}
