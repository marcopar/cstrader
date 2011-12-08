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

package eu.flatworld.commons.properties;

import java.util.*;
import java.io.*;

public class PropertiesX extends Properties {

    String header;
    String file;
    Vector<Object> orderedKeys;

    public PropertiesX(String header, String file) {
        super();
        this.header = header;
        this.file = file;
        orderedKeys = new Vector<Object>();
    }

    @Override
    public Object put(Object key, Object value) {
        if (!super.keySet().contains(key)) {
            orderedKeys.add(key);
        }
        return super.put(key, value);
    }

    @Override
    public Enumeration<Object> keys() {
        return orderedKeys.elements();
    }

    @Override
    public void clear() {
        orderedKeys.clear();
        super.clear();
    }

    public void load() throws IOException {
        orderedKeys.clear();

        FileInputStream fis = new FileInputStream(file);
        super.load(fis);
        fis.close();
    }

    public void store() throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        super.store(fos, header);
        fos.close();
    }

    public Integer getIntegerProperty(String key, Integer defaultValue) {
        String buf = super.getProperty(key);
        if (buf == null || buf.equals("")) {
            return defaultValue;
        } else {
            try {
                return new Integer(buf);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    public Integer getIntegerProperty(String key) {
        return getIntegerProperty(key, null);
    }

    public void setIntegerProperty(String key, Integer value) {
        super.setProperty(key, value == null ? "" : value.toString());
    }

    public Long getLongProperty(String key, Long defaultValue) {
        String buf = super.getProperty(key);
        if (buf == null || buf.equals("")) {
            return defaultValue;
        } else {
            try {
                return new Long(buf);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    public Long getLongProperty(String key) {
        return getLongProperty(key, null);
    }

    public void setLongProperty(String key, Long value) {
        super.setProperty(key, value == null ? "" : value.toString());
    }

    public Boolean getBooleanProperty(String key, Boolean defaultValue) {
        String buf = super.getProperty(key);
        if (buf == null || buf.equals("")) {
            return defaultValue;
        } else {
            try {
                return (buf.equals("0") ? Boolean.FALSE : Boolean.TRUE);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    public Boolean getBooleanProperty(String key) {
        return getBooleanProperty(key, null);
    }

    public void setBooleanProperty(String key, Boolean value) {
        super.setProperty(key,
                value == null ? "" : value.booleanValue() ? "1" : "0");
    }

    public String getStringProperty(String key, String defaultValue) {
        String buf = super.getProperty(key);
        if (buf == null || buf.equals("")) {
            return defaultValue;
        } else {
            return buf;
        }
    }

    public String getStringProperty(String key) {
        return getStringProperty(key, null);
    }

    public void setStringProperty(String key, String value) {
        super.setProperty(key, value == null ? "" : value);
    }

    public Double getDoubleProperty(String key, Double defaultValue) {
        String buf = super.getProperty(key);
        if (buf == null || buf.equals("")) {
            return defaultValue;
        } else {
            try {
                return new Double(buf);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    public Double getDoubleProperty(String key) {
        return getDoubleProperty(key, null);
    }

    public void setDoubleProperty(String key, Double value) {
        super.setProperty(key, value == null ? "" : value.toString());
    }

    public Float getFloatProperty(String key, Float defaultValue) {
        String buf = super.getProperty(key);
        if (buf == null || buf.equals("")) {
            return defaultValue;
        } else {
            try {
                return new Float(buf);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    public Float getFloatProperty(String key) {
        return getFloatProperty(key, null);
    }

    public void setFloatProperty(String key, Float value) {
        super.setProperty(key, value == null ? "" : value.toString());
    }

    public Short getShortProperty(String key, Short defaultValue) {
        String buf = super.getProperty(key);
        if (buf == null || buf.equals("")) {
            return defaultValue;
        } else {
            try {
                return new Short(buf);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    public Short getShortProperty(String key) {
        return getShortProperty(key, null);
    }

    public void setShortProperty(String key, Short value) {
        super.setProperty(key, value == null ? "" : value.toString());
    }

    public Byte getByteProperty(String key, Byte defaultValue) {
        String buf = super.getProperty(key);
        if (buf == null || buf.equals("")) {
            return defaultValue;
        } else {
            try {
                return new Byte(buf);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    public Byte getByteProperty(String key) {
        return getByteProperty(key, null);
    }

    public void setByteProperty(String key, Byte value) {
        super.setProperty(key, value == null ? "" : value.toString());
    }
}
