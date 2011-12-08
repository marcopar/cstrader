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
import eu.flatworld.cstrader.data.Item;
import eu.flatworld.cstrader.data.System;
import eu.flatworld.cstrader.data.ItemLine;
import eu.flatworld.cstrader.data.Location;
import eu.flatworld.cstrader.data.Pricelist;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Collections;
import java.util.Comparator;

public class PricelistsTools {

    public static String exportPricelist(Pricelist pricelist) {
        StringBuffer sb = new StringBuffer();
        String eol = java.lang.System.getProperty("line.separator");

        String system = pricelist.getSystem().name();
        Map<String, Long> weightCache = (Map<String, Long>) Storage.getStorage().get(Main.STORAGE_WEIGHTCACHE);
        // sb.append("Item prices exported from " + Main.NAME + " " + Main.VERSION + eol);
        sb.append(eol);
        sb.append("For Sale At " + system + " System Planets" + eol);
        List<ItemLine> forSale = pricelist.getForSale();
        for (ItemLine item : forSale) {
            if (item.getLocation().isPlanet()) {
                Long w = weightCache.get(item.getItem().getName());
                if (w == null) {
                    w = 1L;
                }
                sb.append(item.getItem().getName() + "\tX " + item.getQuantity() + " (" + w + " Isaton)\tfor $" + item.getPrice() + "\t" + item.getLocation().getName() + eol);
            }
        }
        sb.append(eol);

        sb.append("For Sale At " + system + " Starbases" + eol);
        forSale = pricelist.getForSale();
        for (ItemLine item : forSale) {
            if (!item.getLocation().isPlanet()) {
                Long w = weightCache.get(item.getItem().getName());
                if (w == null) {
                    w = 1L;
                }
                sb.append(item.getItem().getName() + "\tX " + item.getQuantity() + " (" + w + " Isaton)\tfor $" + item.getPrice() + "\t" + item.getLocation().getName() + eol);
            }
        }
        sb.append(eol);

        sb.append("Wanted At " + system + " System Planets" + eol);
        List<ItemLine> wanted = pricelist.getWanted();
        for (ItemLine item : wanted) {
            if (item.getLocation().isPlanet()) {
                sb.append(item.getItem().getName() + "\tX " + item.getQuantity() + "\tat $" + item.getPrice() + "\tFor 0.00 CE\t" + item.getLocation().getName() + eol);
            }
        }
        sb.append(eol);

        sb.append("Wanted At " + system + " Starbases" + eol);
        wanted = pricelist.getWanted();
        for (ItemLine itemLine : wanted) {
            if (!itemLine.getLocation().isPlanet()) {
                sb.append(itemLine.getItem().getName() + "\tX " + itemLine.getQuantity() + "\tat $" + itemLine.getPrice() + "\t" + itemLine.getLocation().getName() + eol);
            }
        }
        sb.append(eol);

        return sb.toString();
    }

    public static Pricelist importPricelistFromSource(String text, System galaxy) throws Exception {
        Map<String, LocationProperty> locationsProperties = (Map<String, LocationProperty>) Storage.getStorage().get(Main.STORAGE_LOCATIONSPROPERTIES);
        Map<String, Long> weightCache = (Map<String, Long>) Storage.getStorage().get(Main.STORAGE_WEIGHTCACHE);

        Pricelist pl = new Pricelist();
        pl.setSystem(galaxy);
        text = text.replace(",", "");
        String pattern = "<[^>]*item_id=([0-9]+)&[^>]*>([^<]*)(?:<[^>]*>\\s*)+X ([0-9,]+)(?: [(]([0-9,]+)[^<]*)?\\s*(?:<[^>]*>\\s*)+(?:for|at) [$]([0-9,]+)(?:<[^>]*>\\s*)+(?:For [0-9][^<]*)?(?:<[^>]*>\\s*)+<[^>]*system_id=([0-9]+)&sector_id=([0-9]+)&grid_id=([0-9]+)[^>]*>([^<]*)";
        Matcher m = Pattern.compile(pattern).matcher(text);

        while (m.find()) {
            // groups: 1-itemid, 2-item name, 3-quantity, 4-weight or null
            // 5-price 6,7,8-sys,sec,grid 9-place name
            ItemLine itemLine = new ItemLine();
            itemLine.setItem(new Item());
            itemLine.getItem().setName(m.group(2).replace('*', ' ').trim());
            itemLine.setQuantity(Long.parseLong(m.group(3)));
            if (m.group(4) == null) {
                itemLine.setForSale(false);
                itemLine.getItem().setWeight(PricelistsTools.loadWeight(itemLine.getItem().getName()));
            } else {
                itemLine.setForSale(true);
                long w = Long.parseLong(m.group(4));
                weightCache.put(itemLine.getItem().getName(), new Long(w));
                itemLine.getItem().setWeight(w);
            }
            itemLine.setPrice(Long.parseLong(m.group(5)));
            Location l = new Location();
            l.setName(m.group(9).trim());
            Long grd = Long.parseLong(m.group(8));
            if (grd == 190) {
                l.setPlanet(true);
            } else {
                l.setPlanet(false);
            }
            l.setGrid(grd);
            Long sector = Long.parseLong(m.group(7));
            l.setSector(sector);
            // get system id (6) and cross check
            l.setSystem(galaxy);
            // NEW: stash coords in file.
            LocationProperty lp = locationsProperties.get(itemLine.getItem().getName());
            if (lp == null) {
                lp = new LocationProperty();
            }
            lp.setLocation(galaxy, sector, grd);
            locationsProperties.put(itemLine.getItem().getName(), lp);

            itemLine.setLocation(l);
            if (itemLine.isForSale()) {
                pl.getForSale().add(itemLine);
            } else {
                pl.getWanted().add(itemLine);
            }
        }
        pl.setPricelistDate(new java.util.Date());
        return pl;
    }

    public static Pricelist importPricelist(String text, System galaxy) throws Exception {
        Map<String, LocationProperty> locationsProperties = (Map<String, LocationProperty>) Storage.getStorage().get(Main.STORAGE_LOCATIONSPROPERTIES);
        Map<String, Long> weightCache = (Map<String, Long>) Storage.getStorage().get(Main.STORAGE_WEIGHTCACHE);

        Pricelist pl = new Pricelist();
        pl.setSystem(galaxy);
        text = text.replace("\r", "");
        text = text.replace(",", "");
        text = text.replace("\t\t\t\t", "\t");
        text = text.replace("\t\t\t", "\t");
        text = text.replace("\t\t", "\t");
        text = text.replace("\t", " ");
        boolean forSale = false;
        boolean wanted = false;
        boolean planet = false;
        boolean starbase = false;
        java.lang.System.err.println(text);
        String rows[] = text.split("\n");
        boolean startOfListFound = false;
        for (String row : rows) {
            //this should avoid problems with stuff appearing at the begin of the list
            //and internet explorer
            //like recent greasemonkey planet buttons script from layze
            if (row.toLowerCase().contains("for sale at")) {
                startOfListFound = true;
            }
            if (!startOfListFound) {
                continue;
            }

            if (row.length() <= 1) {
                continue;
            }
            //end
            if (row.toLowerCase().contains("not looking to buy")) {
                forSale = false;
                wanted = false;
                continue;
            }
            if (row.toLowerCase().contains("have nothing to sell")) {
                forSale = false;
                wanted = false;
                continue;
            }
            if (row.toLowerCase().contains("for sale at")) {
                //if (!columns[0].toLowerCase().contains(galaxy.name().toLowerCase())) {
                //    throw new Exception("You have pasted a pricelist of a different galaxy.");
                //}
                forSale = true;
                wanted = false;
                if (row.toLowerCase().contains("planet")) {
                    planet = true;
                    starbase = false;
                }
                if (row.toLowerCase().contains("starbase")) {
                    planet = false;
                    starbase = true;
                }
                continue;
            }
            if (row.toLowerCase().contains("wanted at")) {
                //if (!columns[0].toLowerCase().contains(galaxy.name().toLowerCase())) {
                //    throw new Exception("You have pasted a pricelist from a different galaxy.");
                //}
                forSale = false;
                wanted = true;
                if (row.toLowerCase().contains("planet")) {
                    planet = true;
                    starbase = false;
                }
                if (row.toLowerCase().contains("starbase")) {
                    planet = false;
                    starbase = true;
                }
                continue;
            }
            if (row.indexOf("-------") != -1) {
                continue;
            }
            if (row.indexOf("_______") != -1) {
                continue;
            }


            if (forSale) {
                try {
                    int begin = 0;
                    int end = 0;
                    ItemLine itemLine = new ItemLine();
                    itemLine.setItem(new Item());
                    itemLine.setForSale(true);
                    begin = 0;
                    end = row.indexOf("X ", begin);
                    if (Character.isDigit(row.charAt(end + 2))) {
                        //the X of the price
                        itemLine.getItem().setName(row.substring(begin, end).replace('*', ' ').trim());
                    } else {
                        //next X
                        end = row.indexOf("X ", end + 1);
                        itemLine.getItem().setName(row.substring(begin, end).replace('*', ' ').trim());
                    }


                    begin = end + 2;
                    end = row.indexOf(" (", begin);
                    String qty = row.substring(begin, end).trim();
                    itemLine.setQuantity(Long.parseLong(qty));

                    begin = end + 2;
                    end = row.indexOf("Isaton");
                    String weight = row.substring(begin, end).trim();
                    long w = Long.parseLong(weight);
                    weightCache.put(itemLine.getItem().getName(), new Long(w));
                    itemLine.getItem().setWeight(w);

                    begin = row.indexOf("for $") + 5;
                    end = row.indexOf(" ", begin);
                    itemLine.setPrice(Long.parseLong(row.substring(begin, end).trim()));

                    Location l = new Location();
                    begin = end + 1;
                    l.setName(row.substring(begin).trim());
                    if (planet) {
                        l.setPlanet(true);
                        l.setSystem(galaxy);
                    }
                    if (starbase) {
                        l.setPlanet(false);
                        l.setSystem(galaxy);
                    }
                    // JUST a sec, lets check the coord cache.
                    LocationProperty lp = locationsProperties.get(itemLine.getItem().getName());
                    if (lp != null) {
                        l.setSector(lp.getSector());
                        l.setGrid(lp.getGrid());
                    }
                    itemLine.setLocation(l);
                    pl.getForSale().add(itemLine);
                } catch (Exception ex) {
                    throw new Exception("Impossible to import the pricelist. Offending line: " + row, ex);
                }
            }
            if (wanted) {
                try {
                    int begin = 0;
                    int end = 0;
                    ItemLine itemLine = new ItemLine();
                    itemLine.setItem(new Item());
                    itemLine.setForSale(false);
                    begin = 0;
                    end = row.indexOf("X ", begin);
                    if (Character.isDigit(row.charAt(end + 2))) {
                        //the X of the price
                        itemLine.getItem().setName(row.substring(begin, end).replace('*', ' ').trim());
                    } else {
                        //next X
                        end = row.indexOf("X ", end + 1);
                        itemLine.getItem().setName(row.substring(begin, end).replace('*', ' ').trim());
                    }

                    begin = end + 2;
                    end = row.indexOf(" ", begin);
                    String qty = row.substring(begin, end).trim();
                    itemLine.setQuantity(Long.parseLong(qty));

                    begin = row.indexOf("at $") + 4;
                    end = row.indexOf(" ", begin);
                    itemLine.setPrice(Long.parseLong(row.substring(begin, end).trim()));

                    itemLine.getItem().setWeight(PricelistsTools.loadWeight(itemLine.getItem().getName()));

                    Location l = new Location();
                    if (planet) {
                        begin = row.indexOf("CE") + 2;
                        l.setName(row.substring(begin).trim());
                        l.setPlanet(true);
                        l.setSystem(galaxy);
                    }
                    if (starbase) {
                        begin = end + 1;
                        l.setName(row.substring(begin).trim());
                        l.setPlanet(false);
                        l.setSystem(galaxy);
                    }
                    // JUST a sec, lets check the coord cache.
                    LocationProperty lp = locationsProperties.get(itemLine.getItem().getName());
                    if (lp != null) {
                        l.setSector(lp.getSector());
                        l.setGrid(lp.getGrid());
                    }
                    itemLine.setLocation(l);
                    pl.getWanted().add(itemLine);
                } catch (Exception ex) {
                    throw new Exception("Impossible to import the pricelist. Offending line: " + row, ex);
                }
            }
        }
        pl.setPricelistDate(new java.util.Date());
        return pl;
    }

    public static Pricelist[] importAllPricelistFromSource(String text) throws Exception {
        Map<String, LocationProperty> locationsProperties = (Map<String, LocationProperty>) Storage.getStorage().get(Main.STORAGE_LOCATIONSPROPERTIES);
        Map<String, Long> weightCache = (Map<String, Long>) Storage.getStorage().get(Main.STORAGE_WEIGHTCACHE);

        Pricelist newpls[] = new Pricelist[System.stars() + System.expanses()];
        for (int i = 0; i < newpls.length; i++) {
            newpls[i] = new Pricelist();
            newpls[i].setSystem(System.values()[i]);
            newpls[i].setPricelistDate(new java.util.Date());
        }

        text = text.replace(",", "");
        String pattern = "<[^>]*item_id=([0-9]+)&[^>]*>([^<]*)(?:<[^>]*>\\s*)+X ([0-9,]+)(?: [(]([0-9,]+)[^<]*)?\\s*(?:<[^>]*>\\s*)+(?:for|at) [$]([0-9,]+)(?:<[^>]*>\\s*)+(?:For [0-9][^<]*)?(?:<[^>]*>\\s*)+<[^>]*system_id=([0-9]+)&sector_id=([0-9]+)&grid_id=([0-9]+)[^>]*>([^<]*)";
        Matcher m = Pattern.compile(pattern).matcher(text);

        while (m.find()) {
            // groups: 1-itemid, 2-item name, 3-quantity, 4-weight or null
            // 5-price 6,7,8-sys,sec,grid 9-place name
            ItemLine itemLine = new ItemLine();
            itemLine.setItem(new Item());
            itemLine.getItem().setName(m.group(2).replace('*', ' ').trim());
            itemLine.setQuantity(Long.parseLong(m.group(3)));
            if (m.group(4) == null) {
                itemLine.setForSale(false);
                itemLine.getItem().setWeight(PricelistsTools.loadWeight(itemLine.getItem().getName()));
            } else {
                itemLine.setForSale(true);
                long w = Long.parseLong(m.group(4));
                weightCache.put(itemLine.getItem().getName(), new Long(w));
                itemLine.getItem().setWeight(w);
            }
            itemLine.setPrice(Long.parseLong(m.group(5)));
            Location l = new Location();
            l.setName(m.group(9).trim());
            Long grd = Long.parseLong(m.group(8));
            if (grd == 190) {
                l.setPlanet(true);
            } else {
                l.setPlanet(false);
            }
            l.setGrid(grd);
            Long sector = Long.parseLong(m.group(7));
            l.setSector(sector);
            // get system id (6) and cross check
            Integer sysid = Integer.parseInt(m.group(6));
            System system = System.findSystemById(sysid);
            l.setSystem(system);

            // NEW: stash coords in file.
            LocationProperty lp = locationsProperties.get(itemLine.getItem().getName());
            if (lp == null) {
                lp = new LocationProperty();
            }
            lp.setLocation(system, sector, grd);
            locationsProperties.put(itemLine.getItem().getName(), lp);

            itemLine.setLocation(l);
            if (itemLine.isForSale()) {
                newpls[system.ordinal()].getForSale().add(itemLine);
            } else {
                newpls[system.ordinal()].getWanted().add(itemLine);
            }
        }
        return newpls;
    }

    public static Pricelist[] importAllPricelist(String text) throws Exception {
        Map<String, LocationProperty> locationsProperties = (Map<String, LocationProperty>) Storage.getStorage().get(Main.STORAGE_LOCATIONSPROPERTIES);
        Map<String, Long> weightCache = (Map<String, Long>) Storage.getStorage().get(Main.STORAGE_WEIGHTCACHE);

        Pricelist newpls[] = new Pricelist[System.stars() + System.expanses()];
        for (int i = 0; i < newpls.length; i++) {
            newpls[i] = new Pricelist();
            newpls[i].setSystem(System.values()[i]);
            newpls[i].setPricelistDate(new java.util.Date());
        }
        Pricelist curpl = null;
        System cursys = null;
        String regex1 = "(for sale|wanted) at (\\w+) system (planets|starbases)";
        Pattern pat1 = Pattern.compile(regex1, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        text = text.replace("\r", "");
        text = text.replace(",", "");
        text = text.replace("\t\t\t\t", "\t");
        text = text.replace("\t\t\t", "\t");
        text = text.replace("\t\t", "\t");
        text = text.replace("\t", " ");
        boolean forSale = false;
        boolean wanted = false;
        boolean planet = false;
        boolean starbase = false;
        java.lang.System.err.println(text);
        String rows[] = text.split("\n");
        for (String row : rows) {
            if (row.length() == 0) {
                continue;
            }
            //compatibility with internet explorer copy
            if (row.length() == 1) {
                continue;
            }
            if (row.toLowerCase().contains("item prices")) {
                continue;
            }
            if (row.toLowerCase().contains("adarian systemaltian systembasian")) {
                continue;
            }
            if (row.toLowerCase().contains("year")) {
                continue;
            }
            //end
            if (row.toLowerCase().contains("not looking to buy")) {
                forSale = false;
                wanted = false;
                continue;
            }
            if (row.toLowerCase().contains("have nothing to sell")) {
                forSale = false;
                wanted = false;
                continue;
            }
            Matcher m1 = pat1.matcher(row);
            if (m1.find()) {
                cursys = System.valueOf(m1.group(2));
                curpl = newpls[cursys.ordinal()];
            }
            if (row.toLowerCase().contains("for sale at")) {
                //if (!columns[0].toLowerCase().contains(galaxy.name().toLowerCase())) {
                //    throw new Exception("You have pasted a pricelist of a different galaxy.");
                //}
                forSale = true;
                wanted = false;
                if (row.toLowerCase().contains("planet")) {
                    planet = true;
                    starbase = false;
                }
                if (row.toLowerCase().contains("starbase")) {
                    planet = false;
                    starbase = true;
                }
                continue;
            }
            if (row.toLowerCase().contains("wanted at")) {
                //if (!columns[0].toLowerCase().contains(galaxy.name().toLowerCase())) {
                //    throw new Exception("You have pasted a pricelist from a different galaxy.");
                //}
                forSale = false;
                wanted = true;
                if (row.toLowerCase().contains("planet")) {
                    planet = true;
                    starbase = false;
                }
                if (row.toLowerCase().contains("starbase")) {
                    planet = false;
                    starbase = true;
                }
                continue;
            }
            if (row.indexOf("-------") != -1) {
                continue;
            }
            if (row.indexOf("_______") != -1) {
                continue;
            }


            if (forSale) {
                try {
                    int begin = 0;
                    int end = 0;
                    ItemLine itemLine = new ItemLine();
                    itemLine.setItem(new Item());
                    itemLine.setForSale(true);
                    begin = 0;
                    end = row.indexOf("X ", begin);
                    if (Character.isDigit(row.charAt(end + 2))) {
                        //the X of the price
                        itemLine.getItem().setName(row.substring(begin, end).replace('*', ' ').trim());
                    } else {
                        //next X
                        end = row.indexOf("X ", end + 1);
                        itemLine.getItem().setName(row.substring(begin, end).replace('*', ' ').trim());
                    }


                    begin = end + 2;
                    end = row.indexOf(" (", begin);
                    String qty = row.substring(begin, end).trim();
                    itemLine.setQuantity(Long.parseLong(qty));

                    begin = end + 2;
                    end = row.indexOf("Isaton");
                    String weight = row.substring(begin, end).trim();
                    long w = Long.parseLong(weight);
                    weightCache.put(itemLine.getItem().getName(), new Long(w));
                    itemLine.getItem().setWeight(w);

                    begin = row.indexOf("for $") + 5;
                    end = row.indexOf(" ", begin);
                    itemLine.setPrice(Long.parseLong(row.substring(begin, end).trim()));

                    Location l = new Location();
                    begin = end + 1;
                    l.setName(row.substring(begin).replace('*', ' ').trim());
                    if (planet) {
                        l.setPlanet(true);
                        l.setSystem(cursys);
                    }
                    if (starbase) {
                        l.setPlanet(false);
                        l.setSystem(cursys);
                    }
                    // JUST a sec, lets check the coord cache.
                    LocationProperty lp = locationsProperties.get(itemLine.getItem().getName());
                    if (lp != null) {
                        l.setSector(lp.getSector());
                        l.setGrid(lp.getGrid());
                    }
                    itemLine.setLocation(l);
                    curpl.getForSale().add(itemLine);
                } catch (Exception ex) {
                    throw new Exception("Impossible to import the pricelist. Offending line: " + row, ex);
                }
            }
            if (wanted) {
                try {
                    int begin = 0;
                    int end = 0;
                    ItemLine itemLine = new ItemLine();
                    itemLine.setItem(new Item());
                    itemLine.setForSale(false);
                    begin = 0;
                    end = row.indexOf("X ", begin);
                    if (Character.isDigit(row.charAt(end + 2))) {
                        //the X of the price
                        itemLine.getItem().setName(row.substring(begin, end).replace('*', ' ').trim());
                    } else {
                        //next X
                        end = row.indexOf("X ", end + 1);
                        itemLine.getItem().setName(row.substring(begin, end).replace('*', ' ').trim());
                    }

                    begin = end + 2;
                    end = row.indexOf(" ", begin);
                    String qty = row.substring(begin, end).trim();
                    itemLine.setQuantity(Long.parseLong(qty));

                    begin = row.indexOf("at $") + 4;
                    end = row.indexOf(" ", begin);
                    itemLine.setPrice(Long.parseLong(row.substring(begin, end).trim()));

                    itemLine.getItem().setWeight(PricelistsTools.loadWeight(itemLine.getItem().getName()));

                    Location l = new Location();
                    if (planet) {
                        begin = row.indexOf("CE") + 2;
                        l.setName(row.substring(begin).trim());
                        l.setPlanet(true);
                        l.setSystem(cursys);
                    }
                    if (starbase) {
                        begin = end + 1;
                        l.setName(row.substring(begin).trim());
                        l.setPlanet(false);
                        l.setSystem(cursys);
                    }
                    // JUST a sec, lets check the coord cache.
                    LocationProperty lp = locationsProperties.get(itemLine.getItem().getName());
                    if (lp != null) {
                        l.setSector(lp.getSector());
                        l.setGrid(lp.getGrid());
                    }
                    itemLine.setLocation(l);
                    curpl.getWanted().add(itemLine);
                } catch (Exception ex) {
                    throw new Exception("Impossible to import the pricelist. Offending line: " + row, ex);
                }
            }
        }
        return newpls;
    }

    public static Long getRouteTurns(Route r, eu.flatworld.cstrader.data.System system, Long sector, Long grid, boolean singleHop) {
        long rTurns;
        long dTurns;
        rTurns = r.getItemForSale().getLocation().getTurns(r.getItemWanted().getLocation(), Config.getConfig().getHyperjumpSpeed(), Config.getConfig().getLightSpeed(), singleHop);
        if (system != eu.flatworld.cstrader.data.System.NoSystem) {
            dTurns = r.getItemForSale().getLocation().getTurns(system, sector, grid, Config.getConfig().getHyperjumpSpeed(), Config.getConfig().getLightSpeed(), singleHop);
        } else {
            //we want only the route cost
            dTurns = 0;
            if (rTurns == 0) {
                rTurns = 1; // avoid divide by 0
            }
        }
        return rTurns + dTurns;
    }

    public static Long getRouteTurns(Route r) {
        return getRouteTurns(r, Config.getConfig().getCurrentLocation(), Config.getConfig().getSector(), Config.getConfig().getGrid(), Config.getConfig().isSingleHop());
    }

    public static Long getRouteTPTC(Route r, eu.flatworld.cstrader.data.System currentLocation, Long sector, Long grid, boolean singleHop) {
        return Math.round(r.getTotalProfit() * 1d / (getRouteTurns(r, currentLocation, sector, grid, singleHop)));
    }

    public static String buildTooltip(ItemLine item) {
        System sys = Config.getConfig().getCurrentLocation();
        Long sec = Config.getConfig().getSector();
        Long grd = Config.getConfig().getGrid();
        Long ls = Config.getConfig().getLightSpeed();
        Long hs = Config.getConfig().getHyperjumpSpeed();
        boolean hop = Config.getConfig().isSingleHop();

        StringBuffer sb = new StringBuffer();
        sb.append("<html><table border=0 cellpadding=0 cellspacing=0>");
        sb.append("<tr><TH>System&nbsp;</TH><TH>Location&nbsp;</TH><TH>Quantity&nbsp;</TH><TH>Profit&nbsp;</TH><TH>Turns</TH></tr>");
        for (ItemLine ii : item.getConnectedItemLines()) {
            sb.append("<tr>");
            long profit = 0;
            if (item.isForSale()) {
                profit = ii.getPrice() - item.getPrice();
            } else {
                profit = item.getPrice() - ii.getPrice();
            }
            // I'm not entirely sure the turns calculation is correct. From current location to destination? We should be using either the whole d+t thing or just the from-to distance. I think.
            long t = ii.getLocation().getTurns(sys, sec, grd, hs, ls, hop);
            sb.append(String.format("<td>%s&nbsp;</td><td>%s&nbsp;</td><td ALIGN=right>%,d&nbsp;</td><td ALIGN=right>%,d&nbsp;</td><td ALIGN=right>%,d</td>",
                    ii.getLocation().getSystem().name(),
                    ii.getLocation().getName(),
                    ii.getQuantity(),
                    profit,
                    t));
            sb.append("</tr>");
        }
        sb.append("</table></html>");
        return sb.toString();
    }

    public static long loadWeight(String name) {
        Map<String, Long> weightCache = (Map<String, Long>) Storage.getStorage().get(Main.STORAGE_WEIGHTCACHE);
        Long w = weightCache.get(name);
        if (w == null) {
            w = 1L;
        }
        return w;
    }

    public static String buildRouteTooltip(Route route) {

        StringBuffer sb = new StringBuffer();
        sb.append("<html>");
        sb.append("<center>Optimized turns=" +
                (getRouteTurns(route, Config.getConfig().getCurrentLocation(),
                Config.getConfig().getSector(), Config.getConfig().getGrid(), true)) + "; optimized TP/TC= " +
                getRouteTPTC(route, Config.getConfig().getCurrentLocation(),
                Config.getConfig().getSector(), Config.getConfig().getGrid(), true) +
                "</center><br/>");

        Long extracreds = 100000000000L;
        Long extraweight = 250000L;
        long bailout = 10;
        if (Config.getConfig().getWeightLimit() != null) {
            extraweight = Config.getConfig().getWeightLimit();
        }
        if (Config.getConfig().getCredits() != null) {
            extracreds = Config.getConfig().getCredits();
        }
        if ((route.getConnectedRoutes() != null) && ((extraweight - route.getWeight()) > 0)) {

            Comparator rrcomp = new Comparator() {

                public int compare(Object o1, Object o2) {
                    Route r1 = (Route) o1;
                    Route r2 = (Route) o2;
                    return (int) (r2.getAbsoluteProfit() - r1.getAbsoluteProfit());
                }
            };
            Collections.sort(route.getConnectedRoutes(), rrcomp);

            sb.append("Suggested combined trades:<br/>");
            sb.append("<html><table border=0 cellpadding=0 cellspacing=0>");
            sb.append("<tr ALIGN=left><TH>Item&nbsp;</TH><TH>Weight&nbsp</TH><TH>Quantity&nbsp;</TH><TH>Profit&nbsp;</TH></tr>");
            long stp = 0;
            long stptc = 0;
            long saveqs[] = new long[route.getConnectedRoutes().size()];
            for (Route rr : route.getConnectedRoutes()) {
                if ((bailout <= 0) || (extraweight <= 0) || (extracreds <= 0)) {
                    break;
                }
                long sw = rr.getWeight(extraweight, extracreds);
                long sq = rr.getQuantity(extraweight, extracreds);
                long sp = rr.getAbsoluteProfit() * sq;
                saveqs[rr.getConnectedRoutes().indexOf(rr)] = sq;
                rr.getItemForSale().setQuantity(rr.getItemForSale().getQuantity() - sq);
                rr.getItemWanted().setQuantity(rr.getItemWanted().getQuantity() - sq);
                if (sq == 0) {
                    continue;  // don't do null trades.
                }

                sb.append(String.format("<tr><td>%s&nbsp</td> <td ALIGN=right>%,d&nbsp</td> <td ALIGN=right>%,d&nbsp</td> <td ALIGN=right>%,d&nbsp</td></tr>",
                        rr.getItemForSale().getItem().getName(), sw, sq, sp));
                stp += sp;
                extraweight -= sw;
                bailout--;
                extracreds -= rr.getItemForSale().getPrice() * sq;
            }
            sb.append("</table>");
            stptc = stp / getRouteTurns(route);
            sb.append("Total Profit=" + stp + ",&nbsp TP/TC=" + stptc);
            // put inventories back in place.
            for (Route rr : route.getConnectedRoutes()) {
                rr.getItemForSale().setQuantity(rr.getItemForSale().getQuantity() +
                        saveqs[rr.getConnectedRoutes().indexOf(rr)]);
                rr.getItemWanted().setQuantity(rr.getItemWanted().getQuantity() +
                        saveqs[rr.getConnectedRoutes().indexOf(rr)]);


            }
        }
        sb.append("</html>");
        return (sb.toString());
    }

    public static Long sum(ArrayList<Long> data) {
        Long sum = 0L;
        for (Long l : data) {
            sum += l;
        }
        return sum;
    }

    public static Double mean(ArrayList<Long> data, ArrayList<Long> qty) {
        Double sumd = 0d;
        Double sumq = 0d;
        for (int i = 0; i < data.size(); i++) {
            long d = data.get(i);
            long q = 1;
            if (qty != null) {
                q = qty.get(i);
            }
            sumd += (d * q);
            sumq += q;
        }
        return sumd / sumq;
    }

    public static Double standardDeviation(ArrayList<Long> data, ArrayList<Long> qty, Double mean) {
        Double sumd = 0d;
        Double sumq = 0d;
        for (int i = 0; i < data.size(); i++) {
            long d = data.get(i);
            long q = 1;
            if (qty != null) {
                q = qty.get(i);
            }
            sumd += q * (d - mean) * (d - mean);
            sumq += q;
        }
        return Math.sqrt(sumd / sumq);
    }
}
