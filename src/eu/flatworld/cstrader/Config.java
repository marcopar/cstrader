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

import eu.flatworld.commons.properties.PropertiesX;
import java.io.IOException;

public class Config extends PropertiesX {

    static Config instance = null;
    private Long weightLimit = null;
    private Long credits = null;
    private eu.flatworld.cstrader.data.System currentLocation = eu.flatworld.cstrader.data.System.Raxian;
    private Long sector = null;
    private Long grid = null;
    private Long hyperjumpSpeed = null;
    private Long lightSpeed = null;
    private boolean singleHop = false;
    private String version = "";
    private Long turnLimit = null;

    private Config() {
        super(Main.NAME + " " + Main.VERSION, Main.CONFIG_FILE);
    }

    public synchronized static Config getConfig() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    @Override
    public synchronized void store() throws IOException {
        setLongProperty("weightlimit", weightLimit);
        setLongProperty("credits", credits);
        setLongProperty("currentlocation", (long) currentLocation.ordinal());
        setLongProperty("hyperjumpspeed", hyperjumpSpeed);
        setLongProperty("lightspeed", lightSpeed);
        setBooleanProperty("singlehop", singleHop);
        setLongProperty("turnlimit", turnLimit);
        setLongProperty("sector", sector);
        setLongProperty("grid", grid);
        setStringProperty("version", version);
        super.store();
    }

    @Override
    public synchronized void load() throws IOException {
        super.load();
        if (getLongProperty("volumelimit") != null) {
            weightLimit = getLongProperty("volumelimit", weightLimit);
        } else {
            weightLimit = getLongProperty("weightlimit", weightLimit);
        }
        credits = getLongProperty("credits", credits);
        int ord = (int) ((long) getLongProperty("currentlocation", (long) currentLocation.ordinal()));
        currentLocation = eu.flatworld.cstrader.data.System.values()[ord];
        hyperjumpSpeed = getLongProperty("hyperjumpspeed", hyperjumpSpeed);
        lightSpeed = getLongProperty("lightspeed", lightSpeed);
        singleHop = getBooleanProperty("singlehop", singleHop);
        version = getStringProperty("version", version);
        turnLimit = getLongProperty("turnlimit", turnLimit);
        sector = getLongProperty("sector", sector);
        grid = getLongProperty("grid", grid);
    }

    public Long getWeightLimit() {
        return weightLimit;
    }

    public void setWeightLimit(Long weightLimit) {
        this.weightLimit = weightLimit;
    }

    public eu.flatworld.cstrader.data.System getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(eu.flatworld.cstrader.data.System currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Long getHyperjumpSpeed() {
        return hyperjumpSpeed;
    }

    public void setHyperjumpSpeed(Long hyperjumpSpeed) {
        this.hyperjumpSpeed = hyperjumpSpeed;
    }

    public Long getLightSpeed() {
        return lightSpeed;
    }

    public void setLightSpeed(Long lightSpeed) {
        this.lightSpeed = lightSpeed;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setTurnLimit(Long turnLimit) {
        this.turnLimit = turnLimit;
    }

    public Long getTurnLimit() {
        return turnLimit;
    }

    public Long getCredits() {
        return credits;
    }

    public void setCredits(Long credits) {
        this.credits = credits;
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

    public void setGrid(Long grid) {
        this.grid = grid;
    }

    public boolean isSingleHop() {
        return singleHop;
    }

    public void setSingleHop(boolean singleHop) {
        this.singleHop = singleHop;
    }
}
