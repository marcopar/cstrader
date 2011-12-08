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
import eu.flatworld.commons.log.LogX;
import eu.flatworld.cstrader.data.ItemLine;
import eu.flatworld.cstrader.data.Pricelist;
import eu.flatworld.cstrader.data.Location;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

public class RouteAnalyzer {

    public static void clearRoutes(Pricelist pricelist[]) {
        for (Pricelist p1 : pricelist) {
            for (ItemLine ifs : p1.getForSale()) {
                ifs.clearConnectedItemLines();
            }
            for (ItemLine iw : p1.getWanted()) {
                iw.clearConnectedItemLines();
            }
        }
    }

    public static ArrayList<Route> findRoutes(Pricelist pricelist[],
            Long minimumAbsoluteProfit, Long minimumRelativeProfit, Long minimumTotalProfit, Long minimumTPTC,
            String item, String fromSys, String fromLoc, String toSys, String toLoc,
            Long weightLimit, Long credits,
            eu.flatworld.cstrader.data.System currentLocation, Long sector, Long grid, Long hSpeed, Long lSpeed, Long turnLimit) {
        ArrayList<Route> routes = new ArrayList<Route>();

        Map<String, LocationProperty> locationsProperties = (Map<String, LocationProperty>) Storage.getStorage().get(Main.STORAGE_LOCATIONSPROPERTIES);

        Pattern itemP = null;
        Pattern fromSysP = null;
        Pattern toSysP = null;
        Pattern fromLocP = null;
        Pattern toLocP = null;
        boolean hop = Config.getConfig().isSingleHop();
        if (item != null) {
            itemP = Pattern.compile(item);
        }
        if (fromSys != null) {
            fromSysP = Pattern.compile(fromSys);
        }
        if (toSys != null) {
            toSysP = Pattern.compile(toSys);
        }
        if (fromLoc != null) {
            fromLocP = Pattern.compile(fromLoc);
        }
        if (toLoc != null) {
            toLocP = Pattern.compile(toLoc);
        }
        ArrayList<ItemLine> forSale = new ArrayList<ItemLine>();
        ArrayList<ItemLine> wanted = new ArrayList<ItemLine>();
        for (Pricelist p1 : pricelist) {
            forSale.addAll(p1.getForSale());
            wanted.addAll(p1.getWanted());
        }
        Comparator itemLineCompare = new Comparator() {

            public int compare(Object o1, Object o2) {
                ItemLine i1 = (ItemLine) o1;
                ItemLine i2 = (ItemLine) o2;
                return i1.getItem().getName().compareTo(i2.getItem().getName());
            }
        };
        Collections.sort(forSale, itemLineCompare);
        Collections.sort(wanted, itemLineCompare);
        int iwbb = 0;   // item wanted begin bracket.
        ItemLine iw;
        int iwndx;
        for (ItemLine ifs : forSale) {
            if (item != null && !itemP.matcher(ifs.getItem().getName()).find()) {
                continue;
            }
            if (fromSys != null && !fromSysP.matcher(ifs.getLocation().getSystem().name()).find()) {
                continue;
            }
            if (fromLoc != null && !fromLocP.matcher(ifs.getLocation().getName()).find()) {
                continue;
            }
            LocationProperty lpfs = locationsProperties.get(ifs.getLocation().getName());
            if (lpfs != null && lpfs.isIgnore()) {
                continue;
            }
            for (iwndx = iwbb; iwndx < wanted.size(); iwndx++) {
                iw = wanted.get(iwndx);
                if (ifs.getItem().getName().compareTo(iw.getItem().getName()) > 0) {
                    iwbb++;
                    continue;
                }
                if (ifs.getItem().getName().compareTo(iw.getItem().getName()) < 0) {
                    break;
                }
                if (item != null && !itemP.matcher(iw.getItem().getName()).find()) {
                    continue;
                }
                if (toSys != null && !toSysP.matcher(iw.getLocation().getSystem().name()).find()) {
                    continue;
                }
                if (toLoc != null && !toLocP.matcher(iw.getLocation().getName()).find()) {
                    continue;
                }
                LocationProperty lpw = locationsProperties.get(iw.getLocation().getName());
                if (lpw != null && lpw.isIgnore()) {
                    continue;
                }
                if (ifs.getItem().getName().equals(iw.getItem().getName())) {
                    Route r = new Route(ifs, iw, weightLimit, credits);
                    if ((minimumAbsoluteProfit != null) &&
                            (r.getAbsoluteProfit() < minimumAbsoluteProfit)) {
                        continue;
                    }
                    if ((minimumRelativeProfit != null) &&
                            (r.getRelativeProfit() < minimumRelativeProfit)) {
                        continue;
                    }
                    if ((minimumTotalProfit != null) &&
                            (r.getTotalProfit() < minimumTotalProfit)) {
                        continue;
                    }
                    long turns = PricelistsTools.getRouteTurns(r, currentLocation, sector, grid, hop);
                    if ((turnLimit != null) && (turns > turnLimit)) {
                        continue;
                    }
                    long tptc = r.getTotalProfit() / turns;
                    if ((minimumTPTC != null) && (tptc < minimumTPTC)) {
                        continue;
                    }
                    ifs.getConnectedItemLines().add(iw);
                    iw.getConnectedItemLines().add(ifs);
                    routes.add(new Route(ifs, iw, weightLimit, credits));
                }
            }
        }
        // here goes nothing.
        findConnectedRoutes(routes);
        return routes;
    }
    public static Comparator routeLocCompare = new Comparator() {

        public int compare(Object o1, Object o2) {
            Route r1 = (Route) o1;
            Route r2 = (Route) o2;

            if ((r1.getItemForSale() == null) || (r2.getItemForSale() == null)) {
                // null matches everything.
                return 0;
            }
            int cmp = r1.getItemForSale().getLocation().getSystem().name().compareTo(r2.getItemForSale().getLocation().getSystem().name());
            if (cmp != 0) {
                return cmp;
            }
            if ((r1.getItemForSale().getLocation().getName() == null) ||
                    (r2.getItemForSale().getLocation().getName() == null)) {
                // if system only, match all in system.
                return 0;
            }
            cmp = r1.getItemForSale().getLocation().getName().compareTo(r2.getItemForSale().getLocation().getName());
            if (cmp != 0) {
                return cmp;
            }
            if ((r1.getItemWanted() == null) || (r2.getItemWanted() == null)) {
                return 0;
            }
            cmp = r1.getItemWanted().getLocation().getSystem().name().compareTo(r2.getItemWanted().getLocation().getSystem().name());
            if (cmp != 0) {
                return cmp;
            }
            cmp = r1.getItemWanted().getLocation().getName().compareTo(r2.getItemWanted().getLocation().getName());
            return cmp;
        }
    };

    public static void findConnectedRoutes(ArrayList<Route> Routes) {
        // first sort the routes by locations.
        Collections.sort(Routes, routeLocCompare);
        for (int i = 0; i < Routes.size() - 1; i++) {
            Route r1 = Routes.get(i);
            if (routeLocCompare.compare((Object) r1, (Object) (Routes.get(i + 1))) == 0) {
                // same same, create a connected list.
                ArrayList<Route> connList = new ArrayList<Route>();
                connList.add(r1);
                r1.setConnectedRoutes(connList);
                for (int j = i + 1; routeLocCompare.compare(r1, Routes.get(j)) == 0; j++) {
                    connList.add(Routes.get(j));
                    Routes.get(j).setConnectedRoutes(connList);
                    i = j;
                    if (j >= Routes.size() - 1) {
                        return;
                    }
                }
            }
        }
    } // end findConnectedRoutes
}


