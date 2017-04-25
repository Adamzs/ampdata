package amp.lib.io.db;

import java.sql.Connection;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    public void printWarnings(SQLWarning warnings, String statement) {
        AtomicInteger ai = new AtomicInteger(0);
        warnings.forEach(warn -> {
        	if (ai.getAndIncrement() == 0) {
        		System.out.println(statement);
        	}
        	System.out.println(warn.getMessage());
        });
    }
    
    public void execute(String statement) {
        try {
            Statement stmt = getConnection().createStatement();
            stmt.clearWarnings();
            stmt.execute(statement + ";");
            SQLWarning warnings = stmt.getWarnings();
            if (warnings != null) {
            	printWarnings(warnings, statement);
            }
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
