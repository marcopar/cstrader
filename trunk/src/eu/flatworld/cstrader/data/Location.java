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

public class Location implements Serializable {

    public final static long serialVersionUID = 1L;
    String name;
    eu.flatworld.cstrader.data.System system;
    Long sector;
    Long grid;
    boolean planet = false;

    public Location() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public eu.flatworld.cstrader.data.System getSystem() {
        return system;
    }

    public void setSystem(eu.flatworld.cstrader.data.System system) {
        this.system = system;
    }

    public boolean isPlanet() {
        return planet;
    }

    public void setPlanet(boolean planet) {
        this.planet = planet;
    }

    public Long getSector() {
        return sector;
    }

    public void setSector(Long sectorid) {
        this.sector = sectorid;
    }

    public Long getGrid() {
        return grid;
    }

    public void setGrid(Long gridid) {
        this.grid = gridid;
    }

    private int distance(long fromx, long fromy, long tox, long toy) {
        long dx = fromx - tox;
        long dy = fromy - toy;
        int dist = (int) Math.round(Math.sqrt(dx * dx + dy * dy));
        if (dist == 0) {
            dist = 1;
        }
        return dist;
    }

    private long toX(long id) {
        return (id - 1) / 20;
    }

    private long toY(long id) {
        return (id - 1) % 20;
    }

    // function to implement the light speed turn formula.
    private long lsFormula(long ls, long dist) {
        double temp;
        temp = 0.0;

        // coefficients
        double a = -4.9266214256674701E+00;
        double b = 1.9294914452306985E-01;
        double c = 2.4995535919284402E+00;
        double d = -8.7041614909837335E-04;
        double e = -1.0128758303689762E-01;
        double f = 3.7371101613908088E-02;
        temp += a;
        temp += b * ls;
        temp += c * dist;
        temp += d * Math.pow(ls, 2.0);
        temp += e * Math.pow(dist, 2.0);
        temp += f * ls * dist;
        return Math.round(temp);
    }

    public long getTurns(Location toplace, Long hspeed, Long lspeed, boolean singleHop) {
        return getTurns(toplace.getSystem(), toplace.getSector(), toplace.getGrid(), hspeed, lspeed, singleHop);
    }

    public long getTurns(eu.flatworld.cstrader.data.System tosys, Long tosec, Long togrid, Long hspeed, Long lspeed, boolean singleHop) {
        if ((tosys == null) || (this.system == null)) {
            throw new IllegalArgumentException("missing start or end system");
        }
        if (hspeed == null) {
            hspeed = 100L;
        }
        if (lspeed == null) {
            lspeed = 100L;
        }
        if (tosys != this.system) {
            int dist;
            if (singleHop) {
                // find distance as longest of deltaX or deltaY
                int diff_x = Math.abs(this.system.getX() - tosys.getX());
                int diff_y = Math.abs(this.system.getY() - tosys.getY());
                if (diff_x >= diff_y) {
                    dist = diff_x;
                } else {
                    dist = diff_y;
                }
                return dist * hspeed; // if one hop per system
            } else {
                dist = distance(this.system.getX(), this.system.getY(), tosys.getX(), tosys.getY());
                return dist * hspeed + 2 * dist * (dist - 1);
            }
        }
        if (this.sector == null || tosec == null) {
            // unknown sector move, give it a guess.
            return lsFormula(lspeed, 10);
        }
        if (this.sector != tosec) {
            int dist = distance(toX(this.sector), toY(this.sector), toX(tosec), toY(tosec));
            return lsFormula(lspeed, dist);
        }
        if ((this.grid == null) || (togrid == null)) {
            // dunno. guess.
            return 10;
        }
        if (this.grid != togrid) {
            int minls = (int) lsFormula(lspeed, 1) * 2;
            int turns = distance(toX(this.grid), toY(this.grid), toX(togrid), toY(togrid));
            if (turns > minls) {
                turns = minls; // can jump to next sector and back in 22 or less
            }
            return turns;
        }
        // we aren't actually moving. return 1 so we don't get div0 errors.
        return 1;
    }
}
