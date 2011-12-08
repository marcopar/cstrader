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

import java.util.List;
import javax.swing.table.AbstractTableModel;

public class RoutesTableModel extends AbstractTableModel {

    public final static String COLUMNS[] = new String[]{
        "Item", "For sale", "Wanted", "Turn cost", "Total profit", "TP/TC",
        "Sale $", "Wanted $", "Unit profit", "UP/TC", "UP %", "From", "System", "To", "System"};
    List<Route> data;
    eu.flatworld.cstrader.data.System currentLocation;
    Long hSpeed;
    Long lSpeed;
    Long sector;
    Long grid;
    boolean singleHop;

    public RoutesTableModel(List<Route> data, eu.flatworld.cstrader.data.System currentLocation, Long sector, Long grid, Long hSpeed, Long lSpeed, boolean singleHop) {
        this.data = data;
        this.currentLocation = currentLocation;
        this.hSpeed = hSpeed;
        this.lSpeed = lSpeed;
        this.sector = sector;
        this.grid = grid;
    }

    public Route getRouteAtRow(int row) {
        return data.get(row);
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Route r = data.get(rowIndex);
        long rTurns;
        long dTurns;
        switch (columnIndex) {
            case 0:
                return r.getItemForSale().getItem().getName();
            case 1:
                return r.getItemForSale().getQuantity();
            case 2:
                return r.getItemWanted().getQuantity();
            case 3:
                return PricelistsTools.getRouteTurns(r, currentLocation, sector, grid, singleHop);
            case 4:
                return r.getTotalProfit();
            case 5:
                return PricelistsTools.getRouteTPTC(r, currentLocation, sector, grid, singleHop);
            case 6:
                return r.getItemForSale().getPrice();
            case 7:
                return r.getItemWanted().getPrice();
            case 8:
                return r.getAbsoluteProfit();
            case 9:
                return r.getAbsoluteProfit() * 1d / PricelistsTools.getRouteTurns(r, currentLocation, sector, grid, singleHop);
            case 10:
                return r.getRelativeProfit();
            case 11:
                return r.getItemForSale().getLocation().getName();
            case 12:
                return r.getItemForSale().getLocation().getSystem().name();
            case 13:
                return r.getItemWanted().getLocation().getName();
            case 14:
                return r.getItemWanted().getLocation().getSystem().name();
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
                return Long.class;
            case 5:
                return Long.class;
            case 6:
                return Long.class;
            case 7:
                return Long.class;
            case 8:
                return Long.class;
            case 9:
                return Double.class;
            case 10:
                return Long.class;
            case 11:
                return String.class;
            case 12:
                return String.class;
            case 13:
                return String.class;
            case 14:
                return String.class;
        }
        return null;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
