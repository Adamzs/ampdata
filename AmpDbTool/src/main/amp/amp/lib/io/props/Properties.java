/***************************************************************************
 *
 * <rrl>
 * =========================================================================
 *                                  LEGEND
 *
 * Use, duplication, or disclosure by the Government is as set forth in the
 * Rights in technical data noncommercial items clause DFAR 252.227-7013 and
 * Rights in noncommercial computer software and noncommercial computer
 * software documentation clause DFAR 252.227-7014, with the exception of
 * third party software known as Sun Microsystems' Java Runtime Environment
 * (JRE), Quest Software's JClass, Oracle's JDBC, and JGoodies which are
 * separately governed under their commercial licenses.  Refer to the
 * license directory for information regarding the open source packages used
 * by this software.
 *
 * Copyright 2016 by BBN Technologies Corporation.
 * =========================================================================
 * </rrl>
 *
 **************************************************************************/
package amp.lib.io.props;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utilities for managing user preference properties.
 */
public class Properties {
    private static final String USER_HOME = "user.home";
    private static final String AMP_PROPERTIES = "AMP.properties";
    public static final String DB_NAME = "db.name";
    public static final String DB_URL = "db.url";
    public static final String DB_USER = "db.user";
    public static final String DB_PASSWORD = "db.password";
    public static final String PROJ_ROOT = "proj.root";
    public static final String SQL_ROOT = "sql.root";

    private static Properties properties = new Properties();
    private java.util.Properties props = new java.util.Properties();

    /**
     * Instantiates a new properties.
     */
    private Properties() {
        read();
    }

    /**
     * Get a property by name
     * @param property the property name
     * @return the property or null if not found.
     */
    public String get(String property) {
        return props.getProperty(property);
    }

    /**
     * Reads the properties from the user's home directory.
     */
    public void read() {
        File dir = new File(System.getProperty(USER_HOME));
        File f = new File(dir, AMP_PROPERTIES);
        if (f.exists()) {
            try {
                FileInputStream fis = new FileInputStream(f);
                props.load(fis);
            } catch (IOException e) {
                System.err.println("Can't load properties: " + e.getMessage());

            }
        }
    }

    /**
     * Save the properties to the user's home directory.
     */
    public void save() {
        File dir = new File(System.getProperty(USER_HOME));
        File f = new File(dir, AMP_PROPERTIES);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(f);
            props.store(fos, "");
        } catch (IOException e) {
            System.err.println("Can't save properties: " + e.getMessage());
        }
    }

    /**
     * Sets a property by name
     *
     * @param property the property name
     * @param value the property value value
     */
    public void set(String property, String value) {
        props.setProperty(property, value);
        save();
    }

    /**
     * Get the static properties manager.
     * @return the properties manager.
     */
    public static Properties getProperties() {
        return properties;
    }
}
