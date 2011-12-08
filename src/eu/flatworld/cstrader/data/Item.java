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

public class Item implements Serializable {

    public final static long serialVersionUID = 1L;
    String name;
    long weight;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getWeight() {
        return weight;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Item other = (Item) obj;
        String tname = new String(this.name);
        tname = tname.replace('*', ' ').trim();
        String oname = new String(other.name);
        oname = oname.replace('*', ' ').trim();
        if ((tname == null) ? (oname != null) : !tname.equals(oname)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        String tname = new String(this.name);
        tname = tname.replace('*', ' ').trim();
        hash = 97 * hash + (tname != null ? tname.hashCode() : 0);
        return hash;
    }
}
