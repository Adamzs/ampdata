package amp.lib.io.db;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

public class Database {
    String name;
    String url;
    Connection connection;

    public Database(String name, String url, Connection connection) {
        super();
        this.name = name;
        this.url = url;
        this.connection = connection;
    }

    public void execute(List<String> sql) {
        for (String statement : sql) {
            execute(statement);
        }
    }

    public void execute(String statement) {
        try {
            Statement stmt = getConnection().createStatement();
            stmt.execute(statement + ";");
        } catch (Exception e) {
            throw new RuntimeException(statement + "\nERROR: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void importCSVFile() {

    }

}
