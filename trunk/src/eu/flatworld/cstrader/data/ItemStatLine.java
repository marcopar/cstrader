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

public class ItemStatLine implements Serializable {

    public final static long serialVersionUID = 1L;
    private Item item;
    
    private long totalSaleQuantity;
    private double meanSaleQuantity;
    private double saleQuantityStdDev;
    private double meanSalePrice;
    private double salePriceStdDev;

    private long totalWantedQuantity;
    private double meanWantedQuantity;
    private double wantedQuantityStdDev;
    private double meanWantedPrice;
    private double wantedPriceStdDev;

    public ItemStatLine() {
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public long getTotalSaleQuantity() {
        return totalSaleQuantity;
    }

    public void setTotalSaleQuantity(long totalSaleQuantity) {
        this.totalSaleQuantity = totalSaleQuantity;
    }

    public double getMeanSaleQuantity() {
        return meanSaleQuantity;
    }

    public void setMeanSaleQuantity(double meanSaleQuantity) {
        this.meanSaleQuantity = meanSaleQuantity;
    }

    public double getSaleQuantityStdDev() {
        return saleQuantityStdDev;
    }

    public void setSaleQuantityStdDev(double saleQuantityStdDev) {
        this.saleQuantityStdDev = saleQuantityStdDev;
    }

    public double getMeanSalePrice() {
        return meanSalePrice;
    }

    public void setMeanSalePrice(double meanSalePrice) {
        this.meanSalePrice = meanSalePrice;
    }

    public double getSalePriceStdDev() {
        return salePriceStdDev;
    }

    public void setSalePriceStdDev(double salePriceStdDev) {
        this.salePriceStdDev = salePriceStdDev;
    }

    public long getTotalWantedQuantity() {
        return totalWantedQuantity;
    }

    public void setTotalWantedQuantity(long totalWantedQuantity) {
        this.totalWantedQuantity = totalWantedQuantity;
    }

    public double getMeanWantedQuantity() {
        return meanWantedQuantity;
    }

    public void setMeanWantedQuantity(double meanWantedQuantity) {
        this.meanWantedQuantity = meanWantedQuantity;
    }

    public double getWantedQuantityStdDev() {
        return wantedQuantityStdDev;
    }

    public void setWantedQuantityStdDev(double wantedQuantityStdDev) {
        this.wantedQuantityStdDev = wantedQuantityStdDev;
    }

    public double getMeanWantedPrice() {
        return meanWantedPrice;
    }

    public void setMeanWantedPrice(double meanWantedPrice) {
        this.meanWantedPrice = meanWantedPrice;
    }

    public double getWantedPriceStdDev() {
        return wantedPriceStdDev;
    }

    public void setWantedPriceStdDev(double wantedPriceStdDev) {
        this.wantedPriceStdDev = wantedPriceStdDev;
    }

}
