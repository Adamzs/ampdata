package amp.lib.io.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Joiner;

import amp.lib.io.errors.ErrorEvent;
import amp.lib.io.errors.ErrorEventListener;
import amp.lib.io.errors.ErrorReporter;
import amp.lib.io.meta.Metadata;

public class DBManager implements ErrorReporter {
    private static DBManager dbManager = new DBManager();

    private static final String USER = "root";
    private static final String PASSWORD = "root";
    private static final String BASE_URL = "jdbc:mysql://localhost";
    private static List<String> systemDBList = Arrays.asList("information_schema", "mysql", "performance_schema", "sys");

    Connection dbmConnection;

    private Database openDatabase;

    private List<ErrorEventListener> listeners = new ArrayList<>();

    private DBManager() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = BASE_URL;
            dbmConnection = DriverManager.getConnection(url, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public void addErrorEventListener(ErrorEventListener listener) {
        listeners.add(listener);

    }

    public void buildSchema(List<Metadata> metadata) {
        SQLManager sqlh = SQLManager.getSQLManager();
        String ref = sqlh.toReferentialIntegrity(false);
        this.reportError(new ErrorEvent(this, ref, ErrorEvent.Severity.INFO));
        this.getDatabase().execute(ref);
        for (Metadata md : metadata) {
            try {
                List<String> sql = sqlh.toTableSQL(md);
                this.getDatabase().execute(sql);
                String text = Joiner.on("\n").join(sql);
                this.reportError(new ErrorEvent(this, text, ErrorEvent.Severity.INFO));
            } catch (Exception e) {
                this.reportError(new ErrorEvent(this, e.getMessage()));
            }
        }
        ref = sqlh.toReferentialIntegrity(true);
        this.reportError(new ErrorEvent(this, ref, ErrorEvent.Severity.INFO));
        this.getDatabase().execute(ref);
    }

    public Database createDatabase(String name, String user, String password) {
        try {
            String url = BASE_URL + "/" + name;
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement s = conn.createStatement();
            s.executeUpdate("create database " + name);
            s.executeQuery("use " + name);
            Database db = new Database(name, url, conn);
            return db;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public void dropDatabase(String name) {
        try {
            Statement s = dbmConnection.createStatement();
            s.executeUpdate("drop database " + name);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public Database getDatabase() {
        return openDatabase;
    }

    public List<String> getDatabaseSelection() {
        try {
            Statement s = dbmConnection.createStatement();
            ResultSet rs = s.executeQuery("show databases");
            List<String> selections = new ArrayList<>();
            while (rs.next()) {
                String dbName = rs.getString(1);
                if (!systemDBList.contains(dbName)) {
                    selections.add(dbName);
                }
            }
            return selections;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());

        }
    }

    public Database openDatabase(String name, String user, String password) {
        try {
            String url = BASE_URL + "/" + name;
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement s = conn.createStatement();
            s.executeQuery("use " + name);
            openDatabase = new Database(name, url, conn);
            return openDatabase;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public void reportError(ErrorEvent event) {
        for (ErrorEventListener l : listeners) {
            l.errorOccurred(event);
        }
    }

    public static DBManager getDbManager() {
        return dbManager;
    }

}
