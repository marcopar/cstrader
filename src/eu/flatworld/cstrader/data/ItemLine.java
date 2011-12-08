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
package eu.flatworld.cstrader.data;

import java.io.Serializable;
import java.util.ArrayList;

public class ItemLine implements Serializable {

    public final static long serialVersionUID = 1L;
    Item item;
    long quantity;
    long price;
    Location location;
    private boolean forSale;
    ArrayList<ItemLine> connectedItemlines;

    public ItemLine() {
        connectedItemlines = new ArrayList<ItemLine>();
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public ArrayList<ItemLine> getConnectedItemLines() {
        return connectedItemlines;
    }

    public void setConnectedItemLines(ArrayList<ItemLine> connectedItemLines) {
        this.connectedItemlines = connectedItemLines;
    }

    public void clearConnectedItemLines() {
        connectedItemlines.clear();
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return item.getName() + "/" + getLocation().getName() + "/" + getLocation().getSystem().name() + "/" + getQuantity() + "/" + getPrice();
    }

    public boolean isForSale() {
        return forSale;
    }

    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}
