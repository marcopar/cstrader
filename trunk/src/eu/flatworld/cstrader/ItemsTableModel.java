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
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class ItemsTableModel extends AbstractTableModel {

    String COLUMNS[] = new String[]{"Item", "Weight", "Quantity", "Price", "Location"};
    List<ItemLine> data;
    boolean forSale;

    public ItemsTableModel(List<ItemLine> data, boolean forSale) {
        this.data = data;
        this.forSale = forSale;
    }

    String[] getColumns() {
        return COLUMNS;
    }

    public ItemLine getItemAtRow(int row) {
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
        ItemLine i = data.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return i.getItem().getName();
            case 1:
                return i.getItem().getWeight();
            case 2:
                return i.getQuantity();
            case 3:
                return i.getPrice();
            case 4:
                return i.getLocation().getName();
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
                return String.class;
        }
        return null;
    }

    @Override
    public String getColumnName(int column) {
        return getColumns()[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 2) {
            return true;
        }
        return false;
    }

    public boolean isForSale() {
        return forSale;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        ItemLine i = data.get(rowIndex);
        switch (columnIndex) {
            case 2:
                i.setQuantity((Long) value);
            default:
                return;
        }
    }
}
