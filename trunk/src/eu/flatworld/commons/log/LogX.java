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

package eu.flatworld.commons.log;

import java.util.logging.*;
import java.io.*;
import java.util.*;

public class LogX {

    final static String NAME = "LogX";
    final static String VERSION = "1.1";
    static String LOGGER = "logx";
    static String LOG_FILE = "logx-%g.log";
    static boolean configured = false;
    static boolean debug = false;
    static boolean warningConfigured = true;
    static ObservableSupport observable;
    static LogXFormatter formatter;
    
    static Logger logger = Logger.getLogger(LOGGER);

    static String exceptionToString(Throwable ex) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos, true);
            ex.printStackTrace(ps);
            ps.flush();
            baos.flush();
            ps.close();
            baos.close();
            return baos.toString();
        } catch (IOException ex1) {
            ex1.printStackTrace();
            return "No stack strace available. Cause: " + ex1.toString();
        }
    }

    public synchronized static void log(Level l, String msg) {
        log(l, msg, null, false);
    }

    public synchronized static void log(Level l, String msg, Throwable ex) {
        log(l, msg, ex, false);
    }

    public synchronized static void log(Level l, String msg, Throwable ex, boolean showDialog) {
        if (configured) {
            StringBuffer sb = new StringBuffer();
            sb.append(msg);
            if (ex != null) {
                sb.append(": EXCEPTION stack trace:\n");
                sb.append(exceptionToString(ex));
            }
            LogRecord lr = new LogRecord(l, sb.toString());
            if (lr.getLevel().intValue() < logger.getLevel().intValue() || logger.getLevel().equals(Level.OFF)) {
                return;
            }
            logger.log(lr);
            observable.setChanged();
            observable.notifyObservers(formatter.format(lr));
            if (showDialog) {
                new LogXDialog("Exception", ex.toString(), ex).setVisible(true);
            }
        } else {
            if (warningConfigured) {
                System.err.println(NAME + ": not configured.");
            }
            if (msg != null) {
                System.err.println(msg);
            }
            if (ex != null) {
                ex.printStackTrace();
            }
            warningConfigured = false;
        }
    }

    public synchronized static void configureLog(String logname, boolean logToFile, int limit, int count, boolean logToConsole, Level maxLevel) {
        if (!configured) {
            observable = new ObservableSupport();
            formatter = new LogXFormatter();

            if (logname != null) {
                LOGGER = logname;
                LOG_FILE = logname + ".%g.log";
            }

            logger = logger;
            resetLogger();

            try {
                Level l = maxLevel;
                logger.setUseParentHandlers(false);
                if (logToFile) {
                    FileHandler fh = new FileHandler(LOG_FILE,
                            limit,
                            count, true);
                    fh.setFormatter(formatter);
                    logger.addHandler(fh);
                    fh.setLevel(l);
                }
                if (logToConsole) {
                    ConsoleHandler ch = new ConsoleHandler();
                    ch.setFormatter(formatter);
                    logger.addHandler(ch);
                    ch.setLevel(l);
                }
                logger.setLevel(l);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.err.println(NAME + " " + VERSION + " configured.");
        } else {
            System.err.println(NAME + " " + VERSION + " already configured.");
        }
        configured = true;

        LogX.debug = false;
    }

    public static void configureLog(String logname, boolean logToFile, boolean logToConsole, Level maxLevel) {
        configureLog(logname, logToFile, 5 * 1024 * 1024, 1, logToConsole, maxLevel);
    }

    public static void configureLog(String logname, Level maxLevel) {
        configureLog(logname, true, true, maxLevel);
    }

    public static void configureLog(Level maxLevel) {
        configureLog(null, true, true, maxLevel);
    }

    public static void setConfigured(boolean configured) {
        LogX.configured = configured;
    }

    public static boolean isConfigured() {
        return configured;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void resetLogger() {
        logger = Logger.getLogger(LOGGER);
        Handler hh[] = logger.getHandlers();
        for (int i = 0; i < hh.length; i++) {
            logger.removeHandler(hh[i]);
        }
    }

    public static void addObserver(Observer o) {
        observable.addObserver(o);
    }

    public static void deleteObserver(Observer o) {
        observable.deleteObserver(o);
    }

    public static void deleteObservers() {
        observable.deleteObservers();
    }

    public static int countObservers() {
        return observable.countObservers();
    }

    static void setChanged() {
        observable.setChanged();
    }

    static void clearChanged() {
        observable.clearChanged();
    }

    public static ObservableSupport getObservable() {
        return observable;
    }

    public static void setLevel(Level l) {
        if (!configured || l == null) {
            return;
        }
        logger.setLevel(l);
    }
}
