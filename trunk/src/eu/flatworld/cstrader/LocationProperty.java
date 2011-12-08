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

import eu.flatworld.cstrader.data.System;
import java.io.Serializable;

public class LocationProperty implements Serializable {

    public final static long serialVersionUID = 1L;
    String name;
    boolean ignore = false;
    System system = null;
    Long sector = null;
    Long grid = null;

    public LocationProperty() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    public void setSystem(System sys) {
        this.system = sys;
    }

    public System getSystem() {
        return system;
    }

    public void setSector(Long sec) {
        this.sector = sec;
    }

    public Long getSector() {
        return sector;
    }

    public void setGrid(Long grd) {
        this.grid = grd;
    }

    public Long getGrid() {
        return grid;
    }

    public void setLocation(System sys, Long sec, Long grd) {
        this.system = sys;
        this.sector = sec;
        this.grid = grd;
    }
}
