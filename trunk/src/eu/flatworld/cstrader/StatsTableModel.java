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

import eu.flatworld.cstrader.data.ItemStatLine;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class StatsTableModel extends AbstractTableModel {

    String COLUMNS[] = new String[]{
        "Item", "For sale", "Wanted", "FS-W",
        "For sale (mean)", "Wanted (mean)", "For sale (stddev)", "Wanted (stddev)",
        "Sale $ (mean)", "Wanted $ (mean)", "Sale $ (stddev)", "Wanted $ (stddev)"};
    List<ItemStatLine> data;

    public StatsTableModel(List<ItemStatLine> data) {
        this.data = data;
    }

    String[] getColumns() {
        return COLUMNS;
    }

    public ItemStatLine getItemAtRow(int row) {
        return data.get(row);
    }

    @Override
    public int getColumnCount() {
        return getColumns().length;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ItemStatLine i = data.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return i.getItem().getName();
            case 1:
                return i.getTotalSaleQuantity();
            case 2:
                return i.getTotalWantedQuantity();
            case 3:
                return i.getTotalSaleQuantity() - i.getTotalWantedQuantity();
            case 4:
                return i.getMeanSaleQuantity();
            case 5:
                return i.getMeanWantedQuantity();
            case 6:
                return i.getSaleQuantityStdDev();
            case 7:
                return i.getWantedQuantityStdDev();
            case 8:
                return i.getMeanSalePrice();
            case 9:
                return i.getMeanWantedPrice();
            case 10:
                return i.getSalePriceStdDev();
            case 11:
                return i.getWantedPriceStdDev();
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return Long.class;
            case 2:
                return Long.class;
            case 3:
                return Long.class;
            case 4:
                return Double.class;
            case 5:
                return Double.class;
            case 6:
                return Double.class;
            case 7:
                return Double.class;
            case 8:
                return Double.class;
            case 9:
                return Double.class;
            case 10:
                return Double.class;
            case 11:
                return Double.class;
        }
        return null;
    }

    @Override
    public String getColumnName(int column) {
        return getColumns()[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
