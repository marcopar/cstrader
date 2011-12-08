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

import java.awt.Component;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class RouteCellRenderer extends DefaultTableCellRenderer {

    DecimalFormat df;
    DecimalFormat dfDouble;

    public RouteCellRenderer() {
        df = new DecimalFormat("#,##0");
        dfDouble = new DecimalFormat("#,##0.00");
        DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        dfs.setGroupingSeparator(',');
        df.setDecimalFormatSymbols(dfs);

        dfs = dfDouble.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        dfs.setGroupingSeparator(',');
        dfDouble.setDecimalFormatSymbols(dfs);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        RoutesTableModel model = (RoutesTableModel) table.getModel();
        Route route = model.getRouteAtRow(table.convertRowIndexToModel(row));
        if (value instanceof Double) {
            c.setText(dfDouble.format(value));
        } else if (value instanceof Number) {
            c.setText(df.format(value));
        }

        //possible solution
        //Point p = MouseInfo.getPointerInfo().getLocation();
        //SwingUtilities.convertPointFromScreen(p, table);
        //and then?

        //if (hasFocus) {
        c.setToolTipText(PricelistsTools.buildRouteTooltip(route));
        //}
        return c;
    }
}
