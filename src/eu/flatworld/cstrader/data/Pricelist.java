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
import java.util.List;

public class Pricelist implements Serializable {

    public final static long serialVersionUID = 1L;
    System system;
    List<ItemLine> forSale;
    List<ItemLine> wanted;
    java.util.Date pricelistDate;

    public Pricelist() {
        forSale = new ArrayList<ItemLine>();
        wanted = new ArrayList<ItemLine>();
    }

    public List<ItemLine> getForSale() {
        return forSale;
    }

    public void setForSale(List<ItemLine> forSale) {
        this.forSale = forSale;
    }

    public List<ItemLine> getWanted() {
        return wanted;
    }

    public void setWanted(List<ItemLine> wanted) {
        this.wanted = wanted;
    }

    public System getSystem() {
        return system;
    }

    public void setSystem(System galaxy) {
        this.system = galaxy;
    }

    public java.util.Date getPricelistDate() {
        return pricelistDate;
    }

    public void setPricelistDate(java.util.Date pricelistDate) {
        this.pricelistDate = pricelistDate;
    }
}
