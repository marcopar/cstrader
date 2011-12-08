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

import eu.flatworld.commons.Storage;
import eu.flatworld.cstrader.data.ItemLine;
import java.util.Map;
import java.util.ArrayList;

public class Route {

    ItemLine itemForSale = null;
    ItemLine itemWanted = null;
    Long weightLimit = null;
    Long creditLimit = null;
    ArrayList<Route> connectedRoutes = null;

    public Route(ItemLine itemForSale, ItemLine itemWanted, Long weightLimit, Long credits) {
        this.itemForSale = itemForSale;
        this.itemWanted = itemWanted;
        this.weightLimit = weightLimit;
        this.creditLimit = credits;
    }

    public ItemLine getItemForSale() {
        return itemForSale;
    }

    public void setItemForSale(ItemLine itemForSale) {
        this.itemForSale = itemForSale;
    }

    public ItemLine getItemWanted() {
        return itemWanted;
    }

    public void setItemWanted(ItemLine itemWanted) {
        this.itemWanted = itemWanted;
    }

    public long getRelativeProfit() {
        return ((itemWanted.getPrice() - itemForSale.getPrice()) * 100) / itemForSale.getPrice();
    }

    public long getAbsoluteProfit() {
        return itemWanted.getPrice() - itemForSale.getPrice();
    }

    public long getWeight() {
        return getWeight(this.weightLimit, this.creditLimit);
    }

    public long getWeight(Long weightLimit, Long creditLimit) {
        Map<String, Long> weightCache = (Map<String, Long>) Storage.getStorage().get(Main.STORAGE_WEIGHTCACHE);
        Long w = weightCache.get(itemWanted.getItem().getName());
        if (w == null) {
            w = 1L;
        }
        long weight = Math.min(itemWanted.getQuantity() * w, itemForSale.getQuantity() * w);

        if (weightLimit != null) {
            weight = Math.min(weight, weightLimit);
        }
        if (creditLimit != null) {
            weight = Math.min(weight, creditLimit / itemForSale.getPrice() * w);
        }
        return weight;
    }

    public long getQuantity() {
        return getQuantity(this.weightLimit, this.creditLimit);
    }

    public long getQuantity(Long weightLimit, Long creditLimit) {
        Map<String, Long> weightCache = (Map<String, Long>) Storage.getStorage().get(Main.STORAGE_WEIGHTCACHE);
        Long w = weightCache.get(itemWanted.getItem().getName());
        if (w == null) {
            w = 1L;
        }
        long weight = Math.min(itemWanted.getQuantity() * w, itemForSale.getQuantity() * w);
        if (weightLimit != null) {
            weight = Math.min(weight, weightLimit);
        }
        if (creditLimit != null) {
            return Math.min(creditLimit / itemForSale.getPrice(), weight / w);
        } else {
            return weight / w;
        }
    }

    public long getTotalProfit() {
        return getQuantity() * getAbsoluteProfit();
    }

    public Long getWeightLimit() {
        return weightLimit;
    }

    public void setWeightLimit(Long weightLimit) {
        this.weightLimit = weightLimit;
    }

    public Long getCreditLimit() {
        return creditLimit;
    }

    public ArrayList<Route> getConnectedRoutes() {
        return connectedRoutes;
    }

    public void setConnectedRoutes(ArrayList<Route> connRoutes) {
        connectedRoutes = connRoutes;
    }
}
