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

import eu.flatworld.cstrader.data.ItemLine;
import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

public class ItemCellRenderer extends DefaultTableCellRenderer {

    final static Color ignoreColor = Color.lightGray;
    final static Color nicePriceColor = Color.red;
    final static Color routeColor = Color.blue;
    Map<String, LocationProperty> locationsProperties;
    Map<String, ItemProperty> itemsProperties;
    DecimalFormat df;

    public ItemCellRenderer(Map<String, LocationProperty> locationsProperties, Map<String, ItemProperty> itemsProperties) {
        this.locationsProperties = locationsProperties;
        this.itemsProperties = itemsProperties;
        df = new DecimalFormat("#,##0");
        DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        dfs.setGroupingSeparator(',');
        df.setDecimalFormatSymbols(dfs);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        ItemsTableModel model = (ItemsTableModel) table.getModel();
        ItemLine itemLine = model.getItemAtRow(table.convertRowIndexToModel(row));
        ItemProperty in = itemsProperties.get(itemLine.getItem().getName());

        LocationProperty ln = locationsProperties.get(itemLine.getLocation().getName());
        if ((ln != null) && ln.isIgnore()) {
            c.setForeground(ignoreColor);
        } else if ((in != null) && model.isForSale() && (in.getMaximumForSalePrice() != null) &&
                (itemLine.getPrice() <= in.getMaximumForSalePrice())) {
            c.setForeground(nicePriceColor);
        } else if ((in != null) && !model.isForSale() && (in.getMinimumWantedPrice() != null) &&
                itemLine.getPrice() >= in.getMinimumWantedPrice()) {
            c.setForeground(nicePriceColor);
        } else if (itemLine.getConnectedItemLines().size() != 0) {
            c.setForeground(routeColor);
        } else {
            c.setForeground(UIManager.getColor("Table.foreground"));
        }
        if (itemLine.getConnectedItemLines().size() > 0) {
            c.setToolTipText(PricelistsTools.buildTooltip(itemLine));
        }
        if (value instanceof Number) {
            c.setText(df.format(value));
        }
        return c;
    }
}
