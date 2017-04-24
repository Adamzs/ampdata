package amp.lib.io.props;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Properties {
    private static final String USER_HOME = "user.home";
    private static final String AMP_PROPERTIES = "AMP.properties";
    public static final String DB_NAME = "db.name";
    public static final String DB_URL = "db.url";
    public static final String DB_USER = "db.user";
    public static final String DB_PASSWORD = "db.password";
    public static final String PROJ_ROOT = "proj.root";

    private static Properties properties = new Properties();
    private java.util.Properties props = new java.util.Properties();;

    public Properties() {
        read();
    }

    public String get(String property) {
        return props.getProperty(property);
    }

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

    public void set(String property, String value) {
        props.setProperty(property, value);
        save();
    }

    public static Properties getProperties() {
        return properties;
    }
}
