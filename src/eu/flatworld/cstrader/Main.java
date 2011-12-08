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
import eu.flatworld.cstrader.data.Item;
import eu.flatworld.cstrader.data.ItemLine;
import eu.flatworld.cstrader.data.ItemStatLine;
import eu.flatworld.cstrader.data.System;
import eu.flatworld.cstrader.data.Pricelist;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author  marcopar
 */
public class Main extends javax.swing.JFrame implements PropertyChangeListener, Thread.UncaughtExceptionHandler {

    public final static String NAME = "CSTrader";
    public final static String VERSION = "0.9.7";
    public final static String WORK_DIR = java.lang.System.getProperty("user.home") + File.separator + "cstrader";
    public final static String CONFIG_FILE = WORK_DIR + File.separator + "config.txt";
    public final static String LOCATIONS_FILE = WORK_DIR + File.separator + "locationproperties.dat";
    public final static String ITEMS_FILE = WORK_DIR + File.separator + "itemproperties.dat";
    public final static String PRICELISTS_FILE = WORK_DIR + File.separator + "pricelists.dat";
    public final static String WEIGHTCACHE_FILE = WORK_DIR + File.separator + "weightcache.dat";
    public final static String LOG_FILE = WORK_DIR + File.separator + "cstrader";
    public final static String STORAGE_LOCATIONSPROPERTIES = "storage.locationProperties";
    public final static String STORAGE_ITEMPROPERTIES = "storage.itemProperties";
    public final static String STORAGE_WEIGHTCACHE = "storage.weightCache";
    Pricelist pricelists[] = new Pricelist[System.stars() + System.expanses()];
    PricelistPanel ppSystems[] = new PricelistPanel[pricelists.length];
    RoutesTableModel routesModel;
    StatsTableModel statsTableModel;

    public Main() {
        try {
            new File(WORK_DIR).mkdirs();
            LogX.configureLog(LOG_FILE, true, 1024 * 1024, 1, true, Level.ALL);
            LogX.log(Level.CONFIG, NAME + " " + VERSION + " hello!");
            LogX.log(Level.CONFIG, "Work dir: " + WORK_DIR);

            Thread.setDefaultUncaughtExceptionHandler(this);
            Locale.setDefault(Locale.ENGLISH);
            ToolTipManager.sharedInstance().setDismissDelay(5000);
            UIManager.put("ToolTip.font", new Font("Monospaced", Font.PLAIN, 11));

            dumpEnv();

            loadLocationsProperties();
            loadItemProperties();
            loadWeightCache();
            loadConfig();
            loadPricelists();
            initComponents();

            for (Pricelist pricelist : pricelists) {
                PricelistPanel pp = new PricelistPanel();
                pp.setup(pricelist);
                pp.addPropertyChangeListener(this);
                ppSystems[pricelist.getSystem().ordinal()] = pp;
                String name = pricelist.getSystem().name();
                name = name.replaceAll("Expanse", "E");
                jtbPricelists.addTab(name, pp);
            }

            galaxyPricelistPanel.addPropertyChangeListener(this);

            System currentLocation = Config.getConfig().getCurrentLocation();
            for (eu.flatworld.cstrader.data.System system : eu.flatworld.cstrader.data.System.values()) {
                jcbCurrentLocation.addItem(system);
            }
            jcbCurrentLocation.setSelectedItem(currentLocation);
            if (Config.getConfig().getWeightLimit() != null) {
                jtfWeightLimit.setText("" + Config.getConfig().getWeightLimit());
            }
            if (Config.getConfig().getCredits() != null) {
                jtfCredits.setText("" + Config.getConfig().getCredits());
            }
            if (Config.getConfig().getHyperjumpSpeed() != null) {
                jtfHyperjump.setText("" + Config.getConfig().getHyperjumpSpeed());
            }
            if (Config.getConfig().getLightSpeed() != null) {
                jtfLightspeed.setText("" + Config.getConfig().getLightSpeed());
            }
            if (Config.getConfig().getTurnLimit() != null) {
                jtfTurns.setText("" + Config.getConfig().getTurnLimit());
            }
            if (Config.getConfig().getSector() != null) {
                jtfSector.setText("" + Config.getConfig().getSector());
            }
            if (Config.getConfig().getGrid() != null) {
                jtfGrid.setText("" + Config.getConfig().getGrid());
            }
            routesModel = new RoutesTableModel(new ArrayList<Route>(), Config.getConfig().getCurrentLocation(),
                    Config.getConfig().getSector(), Config.getConfig().getGrid(), Config.getConfig().getHyperjumpSpeed(),
                    Config.getConfig().getLightSpeed(), Config.getConfig().isSingleHop());
            jtRoutes.setModel(routesModel);
            //Tables.adjustColumnsWidth(jtRoutes);
            for (int i = 0; i < jtRoutes.getModel().getColumnCount(); i++) {
                jtRoutes.setDefaultRenderer(jtRoutes.getModel().getColumnClass(i), new RouteCellRenderer());
            }

            statsTableModel = new StatsTableModel(new ArrayList<ItemStatLine>());
            jtStats.setModel(statsTableModel);
            for (int i = 0; i < jtStats.getModel().getColumnCount(); i++) {
                jtStats.setDefaultRenderer(jtStats.getModel().getColumnClass(i), new StatsCellRenderer());
            }

            fullUpdate();

            Image icon = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/eu/flatworld/cstrader/yellowstar.png"));
            setIconImage(icon);
        } catch (Throwable ex) {
            LogX.log(Level.SEVERE, "Fatal error", ex, true);
            java.lang.System.exit(0);
        }
    }

    void dumpEnv() {
        Set<Object> props = java.lang.System.getProperties().keySet();
        for (Object object : props) {
            String sp = (String) object;
            LogX.log(Level.CONFIG, sp + "=" + java.lang.System.getProperty(sp));
        }
        for (String elem : java.lang.System.getenv().keySet()) {
            LogX.log(Level.CONFIG, elem + "=" + java.lang.System.getenv().get(elem));
        }
    }

    void close() {
//        int resp = JOptionPane.showConfirmDialog(this, "Do you want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
//        if (resp != JOptionPane.YES_OPTION) {
//            return;
//        }
        try {
            Config.getConfig().setWeightLimit(Long.parseLong(jtfWeightLimit.getText()));
        } catch (Exception ex) {
            Config.getConfig().setWeightLimit(null);
        }
        try {
            Config.getConfig().setCredits(Long.parseLong(jtfCredits.getText()));
        } catch (Exception ex) {
            Config.getConfig().setCredits(null);
        }
        try {
            Config.getConfig().setHyperjumpSpeed(Long.parseLong(jtfHyperjump.getText()));
        } catch (Exception ex) {
            Config.getConfig().setHyperjumpSpeed(null);
        }
        try {
            Config.getConfig().setLightSpeed(Long.parseLong(jtfLightspeed.getText()));
        } catch (Exception ex) {
            Config.getConfig().setLightSpeed(null);
        }
        try {
            Config.getConfig().setTurnLimit(Long.parseLong(jtfTurns.getText()));
        } catch (Exception ex) {
            Config.getConfig().setTurnLimit(null);
        }
        try {
            Config.getConfig().setSector(Long.parseLong(jtfSector.getText()));
        } catch (Exception ex) {
            Config.getConfig().setSector(null);
        }
        try {
            Config.getConfig().setGrid(Long.parseLong(jtfGrid.getText()));
        } catch (Exception ex) {
            Config.getConfig().setGrid(null);
        }
        Config.getConfig().setCurrentLocation((eu.flatworld.cstrader.data.System) jcbCurrentLocation.getSelectedItem());

        RouteAnalyzer.clearRoutes(pricelists);
        saveLocationsProperties();
        saveItemsProperties();
        saveWeightCache();
        saveConfig();
        savePricelists();
        java.lang.System.exit(0);
    }

    void clearPricelists() {
        for (int i = 0; i < pricelists.length; i++) {
            pricelists[i] = new Pricelist();
        }
    }

    void loadPricelists() {
        File f = new File(PRICELISTS_FILE);
        try {
            if (!f.exists()) {
                LogX.log(Level.INFO, "Pricelists file not found, creating...");
                for (int i = 0; i < pricelists.length; i++) {
                    Pricelist p = new Pricelist();
                    p.setSystem(System.values()[i]);
                    pricelists[i] = p;
                }
                savePricelists();
            }
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            pricelists = (Pricelist[]) ois.readObject();
            ois.close();
            if(pricelists.length != (System.stars() + System.expanses())) {
                pricelists = new Pricelist[System.stars() + System.expanses()];
            }
            for (int i = 0; i < pricelists.length; i++) {
                Pricelist p = pricelists[i];
                if (p == null || p.getForSale() == null || p.getWanted() == null
                        || p.getSystem() == null || p.getPricelistDate() == null) {
                    //something wrong with the loaded pricelist
                    LogX.log(Level.WARNING, "Recreating corrupted pricelist for system " + System.values()[i]);
                    Pricelist pn = new Pricelist();
                    pn.setSystem(System.values()[i]);
                    pricelists[i] = pn;
                    p = pn;
                }
                List<ItemLine> l = pricelists[i].getForSale();
                for (ItemLine itemLine : l) {
                    itemLine.setForSale(true);
                    itemLine.getItem().setWeight(PricelistsTools.loadWeight(itemLine.getItem().getName()));
                }
                l = p.getWanted();
                for (ItemLine itemLine : l) {
                    itemLine.setForSale(false);
                    itemLine.getItem().setWeight(PricelistsTools.loadWeight(itemLine.getItem().getName()));
                }
            }
        } catch (Exception ex) {
            LogX.log(Level.WARNING, "Error loading pricelists: " + f.getAbsolutePath(), ex, true);
            for (int i = 0; i < pricelists.length; i++) {
                Pricelist p = new Pricelist();
                p.setSystem(System.values()[i]);
                pricelists[i] = p;
            }
        }
    }

    void savePricelists() {
        File f = new File(PRICELISTS_FILE);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(pricelists);
            oos.flush();
            oos.close();
        } catch (Exception ex) {
            LogX.log(Level.WARNING, "Error saving pricelists: " + f.getAbsolutePath(), ex, true);
        }
    }

    void loadConfig() {
        File f = new File(CONFIG_FILE);
        try {
            if (!f.exists()) {
                Config.getConfig().store();
            }
            Config.getConfig().load();
        } catch (Exception ex) {
            LogX.log(Level.WARNING, "Error loading configuration file: " + f.getAbsolutePath(), ex, true);
        }
    }

    void saveConfig() {
        File f = new File(CONFIG_FILE);
        try {
            Config.getConfig().store();
        } catch (Exception ex) {
            LogX.log(Level.WARNING, "Error saving configuration file: " + f.getAbsolutePath(), ex, true);
        }
    }

    void loadLocationsProperties() {
        File f = new File(LOCATIONS_FILE);
        Map<String, LocationProperty> locationsProperties = null;
        try {
            if (!f.exists()) {
                LogX.log(Level.INFO, "Locations properties file not found, creating...");
                locationsProperties = new LinkedHashMap<String, LocationProperty>();
                saveLocationsProperties();
            }
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            locationsProperties = (Map<String, LocationProperty>) ois.readObject();
            ois.close();
            if (locationsProperties == null) {
                LogX.log(Level.WARNING, "Locations properties file corrupted, recreating: " + f.getAbsolutePath());
                locationsProperties = new HashMap<String, LocationProperty>();
            }
        } catch (Exception ex) {
            LogX.log(Level.WARNING, "Error loading locations properties: " + f.getAbsolutePath(), ex, true);
            locationsProperties = new LinkedHashMap<String, LocationProperty>();
        }
        Storage.getStorage().set(STORAGE_LOCATIONSPROPERTIES, locationsProperties);
    }

    void saveLocationsProperties() {
        File f = new File(LOCATIONS_FILE);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(Storage.getStorage().get(STORAGE_LOCATIONSPROPERTIES));
            oos.flush();
            oos.close();
        } catch (Exception ex) {
            LogX.log(Level.WARNING, "Error saving locations properties: " + f.getAbsolutePath(), ex, true);
        }
    }

    void loadItemProperties() {
        File f = new File(ITEMS_FILE);
        Map<String, ItemProperty> itemsProperties = null;
        try {
            if (!f.exists()) {
                LogX.log(Level.INFO, "Items properties file not found, creating...");
                itemsProperties = new LinkedHashMap<String, ItemProperty>();
                saveItemsProperties();
            }
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            itemsProperties = (Map<String, ItemProperty>) ois.readObject();
            ois.close();
            if (itemsProperties == null) {
                LogX.log(Level.WARNING, "Item properties file corrupted, recreating: " + f.getAbsolutePath());
                itemsProperties = new HashMap<String, ItemProperty>();
            }
        } catch (Exception ex) {
            LogX.log(Level.WARNING, "Error loading items properties: " + f.getAbsolutePath(), ex, true);
            itemsProperties = new LinkedHashMap<String, ItemProperty>();
        }
        Storage.getStorage().set(STORAGE_ITEMPROPERTIES, itemsProperties);
    }

    void saveItemsProperties() {
        File f = new File(ITEMS_FILE);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(Storage.getStorage().get(STORAGE_ITEMPROPERTIES));
            oos.flush();
            oos.close();
        } catch (Exception ex) {
            LogX.log(Level.WARNING, "Error saving items properties: " + f.getAbsolutePath(), ex, true);
        }
    }

    void loadWeightCache() {
        File f = new File(WEIGHTCACHE_FILE);
        Map<String, Long> weightCache = null;
        try {
            if (!f.exists()) {
                LogX.log(Level.INFO, "Weight cache file not found, creating...");
                weightCache = new HashMap<String, Long>();
                saveWeightCache();
            }
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            weightCache = (Map<String, Long>) ois.readObject();
            ois.close();
            if (weightCache == null) {
                LogX.log(Level.WARNING, "Weight cache file corrupted, recreating: " + f.getAbsolutePath());
                weightCache = new HashMap<String, Long>();
            }
        } catch (Exception ex) {
            LogX.log(Level.WARNING, "Error loading weight cache: " + f.getAbsolutePath(), ex, true);
            weightCache = new HashMap<String, Long>();
        }
        Storage.getStorage().set(STORAGE_WEIGHTCACHE, weightCache);
    }

    void saveWeightCache() {
        File f = new File(WEIGHTCACHE_FILE);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(Storage.getStorage().get(STORAGE_WEIGHTCACHE));
            oos.flush();
            oos.close();
        } catch (Exception ex) {
            LogX.log(Level.WARNING, "Error saving weight cache: " + f.getAbsolutePath(), ex, true);
        }
    }

    private void changeOnePricelist(Pricelist p) {
        pricelists[p.getSystem().ordinal()] = p;
        ppSystems[p.getSystem().ordinal()].setup(p);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == GalaxyPricelistPanel.P_PROPERTIESCHANGED) {
            fullUpdate();
            return;
        }
        if (evt.getPropertyName().equals(GalaxyPricelistPanel.P_PRICELISTSUPDATED)) {
            if (evt.getNewValue() == null) {
                Pricelist p;
                for (int i = 0; i < pricelists.length; i++) {
                    p = new Pricelist();
                    p.setSystem(System.values()[i]);
                    changeOnePricelist(p);
                }
            } else {
                Pricelist pp[] = (Pricelist[]) evt.getNewValue();
                for (int i = 0; i < pricelists.length; i++) {
                    changeOnePricelist(pp[i]);
                }
            }
            fullUpdate();
            return;
        }
        if (evt.getPropertyName().equals(PricelistPanel.P_PRICELISTUPDATED)) {
            Pricelist p = (Pricelist) evt.getNewValue();
            //java.lang.System.err.println("" + p);
            //java.lang.System.err.println("" + p.getSystem());
            changeOnePricelist(p);
            fullUpdate();
            return;
        }
    }

    void findRoutes() {
        Long minimumAbsolute = null;
        Long minimumRelative = null;
        Long minimumTotal = null;
        Long weightLimit = null;
        Long credits = null;
        Long minimumTPTC = null;
        Long hSpeed = null;
        Long lSpeed = null;
        Long turnLimit = null;
        Long sector = null;
        Long grid = null;

        if (jtfMinimumAbsolute.getText().length() != 0) {
            try {
                minimumAbsolute = Long.parseLong(jtfMinimumAbsolute.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Minimum absolute profit is not valid.");
                return;
            }
        } else {
            minimumAbsolute = null;
        }
        if (jtfMinimumRelative.getText().length() != 0) {
            try {
                minimumRelative = Long.parseLong(jtfMinimumRelative.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Minimum relative profit is not valid.");
                return;
            }
        } else {
            minimumRelative = null;
        }
        if (jtfMinimumTotal.getText().length() != 0) {
            try {
                minimumTotal = Long.parseLong(jtfMinimumTotal.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Minimum total profit is not valid.");
                return;
            }
        } else {
            minimumTotal = null;
        }
        if (jtfMinimumTPTC.getText().length() != 0) {
            try {
                minimumTPTC = Long.parseLong(jtfMinimumTPTC.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Minimum TP/TC is not valid.");
                return;
            }
        } else {
            minimumTPTC = null;
        }
        if (jtfWeightLimit.getText().length() != 0) {
            try {
                weightLimit = Long.parseLong(jtfWeightLimit.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Weight limit is not valid.");
                return;
            }
        } else {
            weightLimit = null;
        }
        if (jtfCredits.getText().length() != 0) {
            try {
                credits = Long.parseLong(jtfCredits.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Credits field is not valid.");
                return;
            }
        } else {
            credits = null;
        }
        if (jtfHyperjump.getText().length() != 0) {
            try {
                hSpeed = Long.parseLong(jtfHyperjump.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Hyperjump speed is not valid.");
                return;
            }
        } else {
            JOptionPane.showMessageDialog(this, "Hyperjump speed is not valid.");
            return;
        }
        if (jtfLightspeed.getText().length() != 0) {
            try {
                lSpeed = Long.parseLong(jtfLightspeed.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Light speed is not valid.");
                return;
            }
        } else {
            JOptionPane.showMessageDialog(this, "Light speed is not valid.");
            return;
        }
        if (jtfTurns.getText().length() != 0) {
            try {
                turnLimit = Long.parseLong(jtfTurns.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Available turns is not valid.");
                return;
            }
        } else {
            turnLimit = null;
        }
        if (jtfSector.getText().length() != 0) {
            try {
                sector = Long.parseLong(jtfSector.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Current location sector is not valid.");
            }
        }
        if (jtfGrid.getText().length() != 0) {
            try {
                grid = Long.parseLong(jtfGrid.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Current location grid is not valid.");
            }
        }
        eu.flatworld.cstrader.data.System currentLocation = (eu.flatworld.cstrader.data.System) jcbCurrentLocation.getSelectedItem();
        String itemF = jtfItemFilter.getText().equals("") ? null : "(?i)" + jtfItemFilter.getText();
        String fromSF = jtfFromSystemFilter.getText().equals("") ? null : "(?i)" + jtfFromSystemFilter.getText();
        String toSF = jtfToSystemFilter.getText().equals("") ? null : "(?i)" + jtfToSystemFilter.getText();
        String fromLF = jtfFromLocationFilter.getText().equals("") ? null : "(?i)" + jtfFromLocationFilter.getText();
        String toLF = jtfToLocationFilter.getText().equals("") ? null : "(?i)" + jtfToLocationFilter.getText();

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        RouteAnalyzer.clearRoutes(pricelists);
        ArrayList<Route> al = RouteAnalyzer.findRoutes(
                pricelists, minimumAbsolute, minimumRelative, minimumTotal, minimumTPTC,
                itemF, fromSF, fromLF, toSF, toLF,
                weightLimit,
                credits,
                currentLocation, sector, grid,
                hSpeed,
                lSpeed,
                turnLimit);

        routesModel = new RoutesTableModel(al, currentLocation, sector, grid, hSpeed, lSpeed, jcbSingleHop.isSelected());

        List<? extends RowSorter.SortKey> sk = jtRoutes.getRowSorter().getSortKeys();
        jtRoutes.setModel(routesModel);
        //Tables.adjustColumnsWidth(jtRoutes);
        jtRoutes.getRowSorter().setSortKeys(sk);
        ((PricelistPanel) jtbPricelists.getSelectedComponent()).forceTablesUpdate();
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    void buildGalaxyPricelist() {
        galaxyPricelistPanel.setup(pricelists);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jpRouteStuff = new javax.swing.JPopupMenu();
        jmiCompleteTrade = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jtfMinimumRelative = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jtfMinimumTotal = new javax.swing.JTextField();
        jtfMinimumTPTC = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jtfCredits = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jtfMinimumAbsolute = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jtfToSystemFilter = new javax.swing.JTextField();
        jtfTurns = new javax.swing.JTextField();
        jtfToLocationFilter = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jtfFromLocationFilter = new javax.swing.JTextField();
        jtfFromSystemFilter = new javax.swing.JTextField();
        jtfWeightLimit = new javax.swing.JTextField();
        jtfItemFilter = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jlTurns = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jtfGrid = new javax.swing.JTextField();
        jtfLightspeed = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jtfSector = new javax.swing.JTextField();
        jbFindRoutes = new javax.swing.JButton();
        jtfHyperjump = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jcbCurrentLocation = new javax.swing.JComboBox();
        jcbSingleHop = new javax.swing.JCheckBox();
        jtpData = new javax.swing.JTabbedPane();
        jtbPricelists = new javax.swing.JTabbedPane();
        galaxyPricelistPanel = new eu.flatworld.cstrader.GalaxyPricelistPanel();
        jpRoutes = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtRoutes = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jtStats = new javax.swing.JTable();
        jmbMain = new javax.swing.JMenuBar();
        jmFile = new javax.swing.JMenu();
        jmiFileExit = new javax.swing.JMenuItem();
        jmHelp = new javax.swing.JMenu();
        jmiHelp = new javax.swing.JMenuItem();
        jmiReleaseNotes = new javax.swing.JMenuItem();
        jmiAbout = new javax.swing.JMenuItem();

        jmiCompleteTrade.setText("Complete Trade");
        jmiCompleteTrade.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiCompleteTradeActionPerformed(evt);
            }
        });
        jpRouteStuff.add(jmiCompleteTrade);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(Main.NAME + " " + Main.VERSION);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jtfMinimumRelative.setText("0");
        jtfMinimumRelative.setToolTipText("Minimum profit % for the route");
        jtfMinimumRelative.setNextFocusableComponent(jtfMinimumAbsolute);
        jtfMinimumRelative.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtfMinimumRelativeActionPerformed(evt);
            }
        });

        jLabel13.setText("Minimum TP/TC:");

        jtfMinimumTotal.setText("0");
        jtfMinimumTotal.setToolTipText("Minimum profit at full cargo (weight limit) for the route");
        jtfMinimumTotal.setNextFocusableComponent(jtfMinimumTPTC);
        jtfMinimumTotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtfMinimumTotalActionPerformed(evt);
            }
        });

        jtfMinimumTPTC.setText("0");
        jtfMinimumTPTC.setToolTipText("Total profit / Turn cost ratio");
        jtfMinimumTPTC.setNextFocusableComponent(jtfItemFilter);
        jtfMinimumTPTC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtfMinimumTPTCActionPerformed(evt);
            }
        });

        jLabel2.setText("Minimum unit profit:");

        jtfCredits.setToolTipText("Credits available");
        jtfCredits.setNextFocusableComponent(jcbCurrentLocation);
        jtfCredits.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtfCreditsActionPerformed(evt);
            }
        });

        jLabel11.setText("Credits:");

        jLabel1.setText("Minimum profit %:");

        jtfMinimumAbsolute.setText("0");
        jtfMinimumAbsolute.setToolTipText("Minimum profit per unit for the route");
        jtfMinimumAbsolute.setNextFocusableComponent(jtfMinimumTotal);
        jtfMinimumAbsolute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtfMinimumAbsoluteActionPerformed(evt);
            }
        });

        jLabel4.setText("Minimum total profit:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel11)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jtfMinimumAbsolute, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                    .addComponent(jtfMinimumRelative, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                    .addComponent(jtfMinimumTotal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                    .addComponent(jtfMinimumTPTC, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                    .addComponent(jtfCredits, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jtfMinimumRelative, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jtfMinimumAbsolute, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jtfMinimumTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jtfMinimumTPTC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jtfCredits, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jLabel14.setText("loc:");

        jLabel9.setText("loc:");

        jLabel6.setText("To sys:");

        jLabel5.setText("From sys:");

        jtfToSystemFilter.setToolTipText("Destination system filter (regex)");
        jtfToSystemFilter.setNextFocusableComponent(jtfToLocationFilter);
        jtfToSystemFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtfToSystemFilterActionPerformed(evt);
            }
        });

        jtfTurns.setToolTipText("Turns available");
        jtfTurns.setNextFocusableComponent(jbFindRoutes);
        jtfTurns.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtfTurnsActionPerformed(evt);
            }
        });
        jtfTurns.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jtfTurnsFocusLost(evt);
            }
        });

        jtfToLocationFilter.setToolTipText("Destination location filter (regex)");
        jtfToLocationFilter.setNextFocusableComponent(jtfWeightLimit);
        jtfToLocationFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtfToLocationFilterActionPerformed(evt);
            }
        });

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Item:");

        jtfFromLocationFilter.setToolTipText("Departure location filter (regex)");
        jtfFromLocationFilter.setNextFocusableComponent(jtfToSystemFilter);
        jtfFromLocationFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtfFromLocationFilterActionPerformed(evt);
            }
        });

        jtfFromSystemFilter.setToolTipText("Departure system filter (regex)");
        jtfFromSystemFilter.setNextFocusableComponent(jtfFromLocationFilter);
        jtfFromSystemFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtfFromSystemFilterActionPerformed(evt);
            }
        });

        jtfWeightLimit.setToolTipText("Cargo hold of the ship");
        jtfWeightLimit.setNextFocusableComponent(jtfCredits);
        jtfWeightLimit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtfWeightLimitActionPerformed(evt);
            }
        });

        jtfItemFilter.setToolTipText("Item name filter (regex)");
        jtfItemFilter.setNextFocusableComponent(jtfFromSystemFilter);
        jtfItemFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtfItemFilterActionPerformed(evt);
            }
        });

        jLabel7.setText("Weight:");

        jlTurns.setText("Turns:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jlTurns, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jtfToSystemFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                            .addComponent(jtfTurns, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                            .addComponent(jtfFromSystemFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jtfFromLocationFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                            .addComponent(jtfToLocationFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                            .addComponent(jtfWeightLimit, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)))
                    .addComponent(jtfItemFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jtfItemFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jtfFromSystemFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(jtfFromLocationFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jtfToSystemFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(jtfToLocationFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jlTurns)
                    .addComponent(jtfTurns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(jtfWeightLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jLabel12.setText("LS:");

        jtfGrid.setToolTipText("Current grid");
        jtfGrid.setNextFocusableComponent(jtfHyperjump);
        jtfGrid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtfGridActionPerformed(evt);
            }
        });

        jtfLightspeed.setToolTipText("Light speed parameter, see help for details");
        jtfLightspeed.setNextFocusableComponent(jtfTurns);
        jtfLightspeed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtfLightspeedActionPerformed(evt);
            }
        });
        jtfLightspeed.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jtfLightspeedFocusLost(evt);
            }
        });

        jLabel15.setText("Sector:");

        jLabel16.setText("Grid:");

        jtfSector.setToolTipText("Current sector");
        jtfSector.setNextFocusableComponent(jtfGrid);
        jtfSector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtfSectorActionPerformed(evt);
            }
        });

        jbFindRoutes.setText("Calculate routes");
        jbFindRoutes.setMargin(new java.awt.Insets(0, 2, 0, 2));
        jbFindRoutes.setNextFocusableComponent(jtbPricelists);
        jbFindRoutes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbFindRoutesActionPerformed(evt);
            }
        });

        jtfHyperjump.setToolTipText("Hyperjump speed as indicated in ship specifications");
        jtfHyperjump.setFocusTraversalPolicyProvider(true);
        jtfHyperjump.setNextFocusableComponent(jtfLightspeed);
        jtfHyperjump.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtfHyperjumpActionPerformed(evt);
            }
        });
        jtfHyperjump.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jtfHyperjumpFocusLost(evt);
            }
        });

        jLabel8.setText("System:");

        jLabel10.setText("HJ:");

        jcbCurrentLocation.setMaximumRowCount(16);
        jcbCurrentLocation.setToolTipText("Current system");
        jcbCurrentLocation.setNextFocusableComponent(jtfSector);
        jcbCurrentLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbCurrentLocationActionPerformed(evt);
            }
        });

        jcbSingleHop.setText("Single hop jumps");
        jcbSingleHop.setToolTipText("Optimize turn calculations simulating shrto jumps only");
        jcbSingleHop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbSingleHopActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jcbSingleHop)
                        .addContainerGap())
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jtfHyperjump, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(jtfSector, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jtfLightspeed, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(jtfGrid, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)))
                    .addComponent(jcbCurrentLocation, 0, 253, Short.MAX_VALUE)
                    .addComponent(jbFindRoutes, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jcbCurrentLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jtfSector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16)
                    .addComponent(jtfGrid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jtfHyperjump, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(jtfLightspeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jcbSingleHop)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jbFindRoutes))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jtpData.addTab("Systems pricelists", jtbPricelists);
        jtpData.addTab("Galaxy pricelist", galaxyPricelistPanel);

        jtRoutes.setAutoCreateRowSorter(true);
        jtRoutes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jtRoutes.setComponentPopupMenu(jpRouteStuff);
        jScrollPane1.setViewportView(jtRoutes);

        javax.swing.GroupLayout jpRoutesLayout = new javax.swing.GroupLayout(jpRoutes);
        jpRoutes.setLayout(jpRoutesLayout);
        jpRoutesLayout.setHorizontalGroup(
            jpRoutesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 994, Short.MAX_VALUE)
        );
        jpRoutesLayout.setVerticalGroup(
            jpRoutesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
        );

        jtpData.addTab("Routes", jpRoutes);

        jtStats.setAutoCreateRowSorter(true);
        jScrollPane2.setViewportView(jtStats);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 994, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
        );

        jtpData.addTab("Statistics", jPanel5);

        jmFile.setMnemonic('f');
        jmFile.setText("File");

        jmiFileExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        jmiFileExit.setMnemonic('x');
        jmiFileExit.setText("Exit");
        jmiFileExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiFileExitActionPerformed(evt);
            }
        });
        jmFile.add(jmiFileExit);

        jmbMain.add(jmFile);

        jmHelp.setMnemonic('h');
        jmHelp.setText("Help");

        jmiHelp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        jmiHelp.setMnemonic('h');
        jmiHelp.setText("Help");
        jmiHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiHelpActionPerformed(evt);
            }
        });
        jmHelp.add(jmiHelp);

        jmiReleaseNotes.setMnemonic('r');
        jmiReleaseNotes.setText("Release notes");
        jmiReleaseNotes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiReleaseNotesActionPerformed(evt);
            }
        });
        jmHelp.add(jmiReleaseNotes);

        jmiAbout.setMnemonic('a');
        jmiAbout.setText("About");
        jmiAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiAboutActionPerformed(evt);
            }
        });
        jmHelp.add(jmiAbout);

        jmbMain.add(jmHelp);

        setJMenuBar(jmbMain);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jtpData, 0, 0, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jtpData, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE))
        );

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-1009)/2, (screenSize.height-594)/2, 1009, 594);
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        close();
    }//GEN-LAST:event_formWindowClosing

    private void jbFindRoutesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbFindRoutesActionPerformed
        fullUpdate();
        jtpData.setSelectedComponent(jpRoutes);
    }//GEN-LAST:event_jbFindRoutesActionPerformed

    private void jtfMinimumRelativeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfMinimumRelativeActionPerformed
        fullUpdate();
        jtpData.setSelectedComponent(jpRoutes);
    }//GEN-LAST:event_jtfMinimumRelativeActionPerformed

    private void jtfMinimumAbsoluteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfMinimumAbsoluteActionPerformed
        fullUpdate();
        jtpData.setSelectedComponent(jpRoutes);
    }//GEN-LAST:event_jtfMinimumAbsoluteActionPerformed

    private void jtfItemFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfItemFilterActionPerformed
        applyFilter();
    }//GEN-LAST:event_jtfItemFilterActionPerformed

    private void jmiFileExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiFileExitActionPerformed
        close();
    }//GEN-LAST:event_jmiFileExitActionPerformed

    private void jmiAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiAboutActionPerformed
        new AboutDialog(this, true).setVisible(true);
    }//GEN-LAST:event_jmiAboutActionPerformed

    private void jtfMinimumTotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfMinimumTotalActionPerformed
        fullUpdate();
        jtpData.setSelectedComponent(jpRoutes);
    }//GEN-LAST:event_jtfMinimumTotalActionPerformed

    private void jtfFromSystemFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfFromSystemFilterActionPerformed
        applyFilter();
    }//GEN-LAST:event_jtfFromSystemFilterActionPerformed

    private void jtfToSystemFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfToSystemFilterActionPerformed
        applyFilter();
    }//GEN-LAST:event_jtfToSystemFilterActionPerformed

    private void jtfWeightLimitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfWeightLimitActionPerformed
        fullUpdate();
        jtpData.setSelectedComponent(jpRoutes);
    }//GEN-LAST:event_jtfWeightLimitActionPerformed

    private void jtfMinimumTPTCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfMinimumTPTCActionPerformed
        fullUpdate();
        jtpData.setSelectedComponent(jpRoutes);
    }//GEN-LAST:event_jtfMinimumTPTCActionPerformed

    private void jtfLightspeedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfLightspeedActionPerformed
        fullUpdate();
        jtpData.setSelectedComponent(jpRoutes);
    }//GEN-LAST:event_jtfLightspeedActionPerformed

    private void jtfHyperjumpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfHyperjumpActionPerformed
        fullUpdate();
        jtpData.setSelectedComponent(jpRoutes);
    }//GEN-LAST:event_jtfHyperjumpActionPerformed

    private void jmiReleaseNotesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiReleaseNotesActionPerformed
        new ReleaseNotesDialog(this, true).setVisible(true);
    }//GEN-LAST:event_jmiReleaseNotesActionPerformed

    private void jtfFromLocationFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfFromLocationFilterActionPerformed
        applyFilter();
    }//GEN-LAST:event_jtfFromLocationFilterActionPerformed

    private void jtfToLocationFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfToLocationFilterActionPerformed
        applyFilter();
    }//GEN-LAST:event_jtfToLocationFilterActionPerformed

    private void jcbCurrentLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbCurrentLocationActionPerformed
        eu.flatworld.cstrader.data.System system = (eu.flatworld.cstrader.data.System) jcbCurrentLocation.getSelectedItem();
        Config.getConfig().setCurrentLocation(system);
    }//GEN-LAST:event_jcbCurrentLocationActionPerformed

    private void jtfHyperjumpFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jtfHyperjumpFocusLost
        Long hSpeed = null;
        if (jtfHyperjump.getText().length() != 0) {
            try {
                hSpeed = Long.parseLong(jtfHyperjump.getText());
            } catch (Exception ex) {
            }
        }
        Config.getConfig().setHyperjumpSpeed(hSpeed);
    }//GEN-LAST:event_jtfHyperjumpFocusLost

    private void jtfLightspeedFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jtfLightspeedFocusLost
        Long lSpeed = null;
        if (jtfHyperjump.getText().length() != 0) {
            try {
                lSpeed = Long.parseLong(jtfLightspeed.getText());
            } catch (Exception ex) {
            }
        }
        Config.getConfig().setLightSpeed(lSpeed);
    }//GEN-LAST:event_jtfLightspeedFocusLost

    private void jtfTurnsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfTurnsActionPerformed
        fullUpdate();
        jtpData.setSelectedComponent(jpRoutes);
    }//GEN-LAST:event_jtfTurnsActionPerformed

    private void jtfTurnsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jtfTurnsFocusLost
        Long turnLimit = null;
        if (jtfTurns.getText().length() != 0) {
            try {
                turnLimit = Long.parseLong(jtfTurns.getText());
            } catch (Exception ex) {
            }
        }
        Config.getConfig().setTurnLimit(turnLimit);
    }//GEN-LAST:event_jtfTurnsFocusLost

    private void jtfCreditsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfCreditsActionPerformed
        fullUpdate();
        jtpData.setSelectedComponent(jpRoutes);
    }//GEN-LAST:event_jtfCreditsActionPerformed

    private void jtfSectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfSectorActionPerformed
        fullUpdate();
        jtpData.setSelectedComponent(jpRoutes);
    }//GEN-LAST:event_jtfSectorActionPerformed

    private void jtfGridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfGridActionPerformed
        fullUpdate();
        jtpData.setSelectedComponent(jpRoutes);
    }//GEN-LAST:event_jtfGridActionPerformed

    private void jcbSingleHopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbSingleHopActionPerformed
        Config.getConfig().setSingleHop(jcbSingleHop.isSelected());
        fullUpdate();
    }//GEN-LAST:event_jcbSingleHopActionPerformed

    private void jmiHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiHelpActionPerformed
        new HelpDialog(this, true).setVisible(true);
    }//GEN-LAST:event_jmiHelpActionPerformed

    private void jmiCompleteTradeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiCompleteTradeActionPerformed
        RoutesTableModel model = (RoutesTableModel) jtRoutes.getModel();
        int row = jtRoutes.getSelectedRow();
        Route r = model.getRouteAtRow(jtRoutes.convertRowIndexToModel(row));
        Long tl = Config.getConfig().getTurnLimit();
        if (tl != null) {
            // subtract TC from tl, set.
            Long rtc = (Long) jtRoutes.getValueAt(row, 3);
            tl -= rtc;
            Config.getConfig().setTurnLimit(tl);
            jtfTurns.setText("" + tl);
        }
        Long cl = Config.getConfig().getCredits();
        if (cl != null) {
            Long rtp = (Long) jtRoutes.getValueAt(row, 4);
            cl += rtp;
            Config.getConfig().setCredits(cl);
            jtfCredits.setText("" + cl);
        }
        System newsys = r.getItemWanted().getLocation().getSystem();
        Config.getConfig().setCurrentLocation(newsys);
        jcbCurrentLocation.setSelectedItem(newsys);
        Long newsec = r.getItemWanted().getLocation().getSector();
        Config.getConfig().setSector(newsec);
        if (newsec != null) {
            jtfSector.setText("" + newsec);
        }
        Long newgrd = r.getItemWanted().getLocation().getGrid();
        Config.getConfig().setGrid(newgrd);
        if (newgrd != null) {
            jtfGrid.setText("" + newgrd);
        }
        // adjust inventories
        Long quant = (Long) jtRoutes.getValueAt(row, 2);
        r.getItemForSale().setQuantity(r.getItemForSale().getQuantity() - quant);
        r.getItemWanted().setQuantity(r.getItemWanted().getQuantity() - quant);
        fullUpdate();
    }//GEN-LAST:event_jmiCompleteTradeActionPerformed

    void applyFilter() {
        if (jtfItemFilter.getText().equals("")
                && jtfFromSystemFilter.getText().equals("") && jtfToSystemFilter.getText().equals("")
                && jtfFromLocationFilter.getText().equals("") && jtfToLocationFilter.getText().equals("")) {
            galaxyPricelistPanel.setRowFilter(null, null);
            ((TableRowSorter) jtRoutes.getRowSorter()).setRowFilter(null);
            for (PricelistPanel pricelistPanel : ppSystems) {
                pricelistPanel.setRowFilter(null, null);
            }
            return;
        }

        String itemF = "(?i)" + jtfItemFilter.getText();
        String fromSF = "(?i)" + jtfFromSystemFilter.getText();
        String toSF = "(?i)" + jtfToSystemFilter.getText();
        String fromLF = "(?i)" + jtfFromLocationFilter.getText();
        String toLF = "(?i)" + jtfToLocationFilter.getText();

        ArrayList<RowFilter<Object, Object>> vForSaleGalaxy = new ArrayList<RowFilter<Object, Object>>();
        ArrayList<RowFilter<Object, Object>> vWantedGalaxy = new ArrayList<RowFilter<Object, Object>>();
        ArrayList<RowFilter<Object, Object>> vForSaleSystem = new ArrayList<RowFilter<Object, Object>>();
        ArrayList<RowFilter<Object, Object>> vWantedSystem = new ArrayList<RowFilter<Object, Object>>();
        if (!jtfItemFilter.getText().equals("")) {
            vForSaleGalaxy.add(RowFilter.regexFilter(itemF, 0));
            vWantedGalaxy.add(RowFilter.regexFilter(itemF, 0));
            vForSaleSystem.add(RowFilter.regexFilter(itemF, 0));
            vWantedSystem.add(RowFilter.regexFilter(itemF, 0));
        }
        if (!jtfFromSystemFilter.getText().equals("")) {
            vForSaleGalaxy.add(RowFilter.regexFilter(fromSF, 5));
        }
        if (!jtfToSystemFilter.getText().equals("")) {
            vWantedGalaxy.add(RowFilter.regexFilter(toSF, 5));
        }
        if (!jtfFromLocationFilter.getText().equals("")) {
            vForSaleGalaxy.add(RowFilter.regexFilter(fromLF, 4));
            vForSaleSystem.add(RowFilter.regexFilter(fromLF, 4));
        }
        if (!jtfToLocationFilter.getText().equals("")) {
            vWantedGalaxy.add(RowFilter.regexFilter(toLF, 4));
            vWantedSystem.add(RowFilter.regexFilter(toLF, 4));
        }
        galaxyPricelistPanel.setRowFilter(RowFilter.andFilter(vForSaleGalaxy), RowFilter.andFilter(vWantedGalaxy));

        for (PricelistPanel pricelistPanel : ppSystems) {
            pricelistPanel.setRowFilter(RowFilter.andFilter(vForSaleSystem), RowFilter.andFilter(vWantedSystem));
        }

        ArrayList<RowFilter<Object, Object>> vrf = new ArrayList<RowFilter<Object, Object>>();
        if (!jtfItemFilter.getText().equals("")) {
            vrf.add(RowFilter.regexFilter(itemF, 0));
        }
        if (!jtfFromSystemFilter.getText().equals("")) {
            vrf.add(RowFilter.regexFilter(fromSF, 12));
        }
        if (!jtfToSystemFilter.getText().equals("")) {
            vrf.add(RowFilter.regexFilter(toSF, 14));
        }
        if (!jtfFromLocationFilter.getText().equals("")) {
            vrf.add(RowFilter.regexFilter(fromLF, 11));
        }
        if (!jtfToLocationFilter.getText().equals("")) {
            vrf.add(RowFilter.regexFilter(toLF, 13));
        }
        ((TableRowSorter) jtRoutes.getRowSorter()).setRowFilter(RowFilter.andFilter(vrf));
    }

    void buildStatistics() {
        HashMap<Item, ArrayList<Long>> hmSalePrice = new HashMap<Item, ArrayList<Long>>();
        HashMap<Item, ArrayList<Long>> hmWantedPrice = new HashMap<Item, ArrayList<Long>>();
        HashMap<Item, ArrayList<Long>> hmSaleQty = new HashMap<Item, ArrayList<Long>>();
        HashMap<Item, ArrayList<Long>> hmWantedQty = new HashMap<Item, ArrayList<Long>>();
        for (Pricelist pricelist : pricelists) {
            for (ItemLine il : pricelist.getForSale()) {
                ArrayList<Long> alSalePrice = hmSalePrice.get(il.getItem());
                if (alSalePrice == null) {
                    alSalePrice = new ArrayList<Long>();
                    hmSalePrice.put(il.getItem(), alSalePrice);
                }
                ArrayList<Long> alSaleQty = hmSaleQty.get(il.getItem());
                if (alSaleQty == null) {
                    alSaleQty = new ArrayList<Long>();
                    hmSaleQty.put(il.getItem(), alSaleQty);
                }
                alSalePrice.add(il.getPrice());
                alSaleQty.add(il.getQuantity());
            }
            for (ItemLine il : pricelist.getWanted()) {
                ArrayList<Long> alWantedPrice = hmWantedPrice.get(il.getItem());
                if (alWantedPrice == null) {
                    alWantedPrice = new ArrayList<Long>();
                    hmWantedPrice.put(il.getItem(), alWantedPrice);
                }
                ArrayList<Long> alWantedQty = hmWantedQty.get(il.getItem());
                if (alWantedQty == null) {
                    alWantedQty = new ArrayList<Long>();
                    hmWantedQty.put(il.getItem(), alWantedQty);
                }
                alWantedPrice.add(il.getPrice());
                alWantedQty.add(il.getQuantity());
            }
        }
        HashMap<Item, ItemStatLine> hm = new HashMap<Item, ItemStatLine>();
        for (Item item : hmSalePrice.keySet()) {
            ItemStatLine isl = hm.get(item);
            if (isl == null) {
                isl = new ItemStatLine();
                isl.setItem(item);
                hm.put(item, isl);
            }
            ArrayList<Long> alPrice = hmSalePrice.get(item);
            ArrayList<Long> alQty = hmSaleQty.get(item);
            isl.setMeanSalePrice(PricelistsTools.mean(alPrice, alQty));
            isl.setMeanSaleQuantity(PricelistsTools.mean(alQty, null));
            isl.setSalePriceStdDev(PricelistsTools.standardDeviation(alPrice, alQty, isl.getMeanSalePrice()));
            isl.setTotalSaleQuantity(PricelistsTools.sum(alQty));
            isl.setSaleQuantityStdDev(PricelistsTools.standardDeviation(alQty, null, isl.getMeanSaleQuantity()));
        }
        for (Item item : hmWantedPrice.keySet()) {
            ItemStatLine isl = hm.get(item);
            if (isl == null) {
                isl = new ItemStatLine();
                isl.setItem(item);
                hm.put(item, isl);
            }
            ArrayList<Long> alPrice = hmWantedPrice.get(item);
            ArrayList<Long> alQty = hmWantedQty.get(item);
            isl.setMeanWantedPrice(PricelistsTools.mean(alPrice, alQty));
            isl.setMeanWantedQuantity(PricelistsTools.mean(alQty, null));
            isl.setWantedPriceStdDev(PricelistsTools.standardDeviation(alPrice, alQty, isl.getMeanWantedPrice()));
            isl.setTotalWantedQuantity(PricelistsTools.sum(alQty));
            isl.setWantedQuantityStdDev(PricelistsTools.standardDeviation(alQty, null, isl.getMeanWantedQuantity()));
        }
        hmSalePrice.clear();
        hmSaleQty.clear();
        hmWantedPrice.clear();
        hmWantedQty.clear();
        ArrayList<ItemStatLine> al = new ArrayList<ItemStatLine>();
        al.addAll(hm.values());
        Collections.sort(al, new Comparator<ItemStatLine>() {

            public int compare(ItemStatLine o1, ItemStatLine o2) {
                return o1.getItem().getName().compareTo(o2.getItem().getName());
            }
        });
        StatsTableModel model = new StatsTableModel(al);
        jtStats.setModel(model);
    }

    void fullUpdate() {
        findRoutes();
        buildGalaxyPricelist();
        buildStatistics();
        applyFilter();
        for (PricelistPanel pricelistPanel : ppSystems) {
            pricelistPanel.forceTablesUpdate();
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            LogX.log(Level.SEVERE, "PANIC TIME: please report this error", e, true);
        } catch(Throwable e2) {
            e2.printStackTrace();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        final StartupFrame f = new StartupFrame();
        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {

                public void run() {
                    f.setVisible(true);
                }
            });
        } catch (Exception ex) {
        }
        Main m = new Main();
        m.setVisible(true);
        f.dispose();
        if (Config.getConfig().equals("") || !Config.getConfig().getVersion().equals(Main.VERSION)) {
            new ReleaseNotesDialog(m, true).setVisible(true);
            Config.getConfig().setVersion(Main.VERSION);
        }

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    eu.flatworld.cstrader.GalaxyPricelistPanel galaxyPricelistPanel;
    javax.swing.JLabel jLabel1;
    javax.swing.JLabel jLabel10;
    javax.swing.JLabel jLabel11;
    javax.swing.JLabel jLabel12;
    javax.swing.JLabel jLabel13;
    javax.swing.JLabel jLabel14;
    javax.swing.JLabel jLabel15;
    javax.swing.JLabel jLabel16;
    javax.swing.JLabel jLabel2;
    javax.swing.JLabel jLabel3;
    javax.swing.JLabel jLabel4;
    javax.swing.JLabel jLabel5;
    javax.swing.JLabel jLabel6;
    javax.swing.JLabel jLabel7;
    javax.swing.JLabel jLabel8;
    javax.swing.JLabel jLabel9;
    javax.swing.JPanel jPanel1;
    javax.swing.JPanel jPanel2;
    javax.swing.JPanel jPanel3;
    javax.swing.JPanel jPanel4;
    javax.swing.JPanel jPanel5;
    javax.swing.JScrollPane jScrollPane1;
    javax.swing.JScrollPane jScrollPane2;
    javax.swing.JButton jbFindRoutes;
    javax.swing.JComboBox jcbCurrentLocation;
    javax.swing.JCheckBox jcbSingleHop;
    javax.swing.JLabel jlTurns;
    javax.swing.JMenu jmFile;
    javax.swing.JMenu jmHelp;
    javax.swing.JMenuBar jmbMain;
    javax.swing.JMenuItem jmiAbout;
    javax.swing.JMenuItem jmiCompleteTrade;
    javax.swing.JMenuItem jmiFileExit;
    javax.swing.JMenuItem jmiHelp;
    javax.swing.JMenuItem jmiReleaseNotes;
    javax.swing.JPopupMenu jpRouteStuff;
    javax.swing.JPanel jpRoutes;
    javax.swing.JTable jtRoutes;
    javax.swing.JTable jtStats;
    javax.swing.JTabbedPane jtbPricelists;
    javax.swing.JTextField jtfCredits;
    javax.swing.JTextField jtfFromLocationFilter;
    javax.swing.JTextField jtfFromSystemFilter;
    javax.swing.JTextField jtfGrid;
    javax.swing.JTextField jtfHyperjump;
    javax.swing.JTextField jtfItemFilter;
    javax.swing.JTextField jtfLightspeed;
    javax.swing.JTextField jtfMinimumAbsolute;
    javax.swing.JTextField jtfMinimumRelative;
    javax.swing.JTextField jtfMinimumTPTC;
    javax.swing.JTextField jtfMinimumTotal;
    javax.swing.JTextField jtfSector;
    javax.swing.JTextField jtfToLocationFilter;
    javax.swing.JTextField jtfToSystemFilter;
    javax.swing.JTextField jtfTurns;
    javax.swing.JTextField jtfWeightLimit;
    javax.swing.JTabbedPane jtpData;
    // End of variables declaration//GEN-END:variables
}
