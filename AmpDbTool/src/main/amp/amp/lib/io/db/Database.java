package amp.lib.io.db;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import amp.lib.io.errors.Report;
import amp.lib.io.meta.MetaObject;
import amp.lib.io.meta.MetaTable;
import amp.lib.io.meta.MetaTable.Column;
import amp.lib.io.meta.MetaTable.ForeignKey;
import amp.lib.io.meta.MetaTable.PrimaryKey;
import amp.lib.io.meta.MetaView;
import amp.lib.io.meta.MetadataFactory;

public class Database {
    private static Database database = new Database();

    private static final String BASE_USER = "root";
    private static final String BASE_PASSWORD = "root";
    private static final String BASE_URL = "jdbc:mysql://localhost";
    private static List<String> systemDBList = Arrays.asList("information_schema", "mysql", "performance_schema", "sys");

    private Connection dbmConnection;

    private String name;

    private String password;

    private String user;

    private Database() {
        openDatabase(null, BASE_USER, BASE_PASSWORD);
    }

    public void buildSchema(List<MetaObject> metadata) {
        validataMetadata(metadata);
        referentialIntegrity(false);
        buildTables(metadata);
        buildIndexes(metadata);
        buildKeys(metadata);
        buildViews(metadata);
        referentialIntegrity(true);
    }

    public void buildSQL(List<File> sqlFiles) {
        for (File f : sqlFiles) {
            try {
                if (f.isDirectory()) {
                    buildSQL(Arrays.asList(f.listFiles()));
                } else if (f.getPath().endsWith(".sql")) {
                    String sql = FileUtils.readFileToString(f, Charset.defaultCharset());
                    execute(sql);
                }
            } catch (Exception e) {
                Report.error(e.getMessage());
            }
        }
    }

    public void createDatabase(String name, String user, String password) {
        try {
            this.name = name;
            this.user = user;
            this.password = password;
            Statement s = getConnection().createStatement();
            s.executeUpdate("create database " + name);
            use(name);
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

    public void execute(String statement) {
        try {
            Statement stmt = getConnection().createStatement();
            stmt.execute(statement + ";");
            Report.info("\n" + statement);
        } catch (Exception e) {
            throw new RuntimeException("\n" + statement + "\n" + e.getMessage());
        }
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

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getUser() {
        return user;
    }

    public boolean isOpen() {
        try {
            return dbmConnection != null && !dbmConnection.isClosed();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void openDatabase(String name, String user, String password) {
        try {
            this.name = name;
            this.user = user;
            this.password = password;
            try {
                if (dbmConnection != null) {
                    dbmConnection.close();
                    dbmConnection = null;
                }
                Class.forName("com.mysql.jdbc.Driver");
                dbmConnection = DriverManager.getConnection(BASE_URL, user, password);
            } catch (ClassNotFoundException | SQLException e) {
                throw new RuntimeException(e.getMessage());
            }
            if (name != null) {
                use(name);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void populateTables(List<MetaObject> metadata) {
        referentialIntegrity(false);
        for (MetaObject meta : metadata) {
            if (meta instanceof MetaTable) {
                importCSVFile((MetaTable) meta);
            }
        }
        referentialIntegrity(true);
    }

    public ResultSet query(String querySql) {
        try {
            Statement stmt = getConnection().createStatement();
            return stmt.executeQuery(querySql + ";");
        } catch (Exception e) {
            throw new RuntimeException(querySql + "\n" + e.getMessage());
        }
    }

    private void buildIndexes(List<MetaObject> metadata) {
        SQLFactory sqlm = SQLFactory.getSQLManager();
        for (MetaObject mo : metadata) {
            if (mo instanceof MetaTable) {
                MetaTable mt = (MetaTable) mo;
                PrimaryKey pk = mt.getPrimaryKey();
                if (pk != null) {
                    String indexName = sqlm.toIndexName(pk);
                    try {
                        execute(sqlm.toDropIndexSQL(indexName));
                    } catch (Exception e) {
                        // no-op
                    }
                    try {
                        execute(sqlm.toPrimaryIndexSQL(pk));
                    } catch (Exception e) {
                        Report.error(mt, e.getMessage());
                    }
                }
                for (ForeignKey fk : mt.getForeignKeys()) {
                    try {
                        execute(sqlm.toForeignIndexSQL(fk));
                    } catch (Exception e) {
                        Report.error(mt, e.getMessage());
                    }
                }
            }
        }
    }

    private void buildKeys(List<MetaObject> metadata) {
        SQLFactory sqlm = SQLFactory.getSQLManager();
        for (MetaObject mo : metadata) {
            if (mo instanceof MetaTable) {
                MetaTable mt = (MetaTable) mo;
                PrimaryKey pk = mt.getPrimaryKey();
                if (pk != null) {
                    try {
                        String sql = sqlm.toPrimaryKeySQL(pk);
                        execute(sql);
                    } catch (Exception e) {
                        Report.error(mt, e.getMessage());
                    }
                }
                for (ForeignKey fk : mt.getForeignKeys()) {
                    try {
                        String sql = sqlm.toForeignKeySQL(fk);
                        execute(sql);
                    } catch (Exception e) {
                        Report.error(mt, e.getMessage());
                    }
                }
            }
        }
    }

    private void buildTables(List<MetaObject> metadata) {
        SQLFactory sqlh = SQLFactory.getSQLManager();
        for (MetaObject mo : metadata) {
            if (mo instanceof MetaTable) {
                MetaTable mt = (MetaTable) mo;
                try {
                    execute(sqlh.toDropTableSQL(mt));
                } catch (Exception e1) {
                    // no-op
                }
                try {
                    execute(sqlh.toTableSQL(mt));
                } catch (Exception e) {
                    Report.error(mt, e.getMessage());
                }
            }
        }
    }

    private void buildViews(List<MetaObject> metadata) {
        SQLFactory sqlh = SQLFactory.getSQLManager();
        for (MetaObject mo : metadata) {
            if (mo instanceof MetaView) {
                MetaView mv = (MetaView) mo;
                try {
                    String sql = sqlh.toViewSQL(mv);
                    execute(sql);
                } catch (Exception e) {
                    Report.error(mv, e.getMessage());
                }
            }
        }
    }

    private int countRowsLoaded(MetaTable meta) throws SQLException {
        SQLFactory sqlm = SQLFactory.getSQLManager();
        ResultSet rs = query(sqlm.toCountRowsSql(meta));
        int rows = rs.next() ? rs.getInt(1) : 0;
        return rows;
    }

    private Connection getConnection() {
        return dbmConnection;
    }

    private void importCSVFile(MetaTable meta) {
        SQLFactory sqlm = SQLFactory.getSQLManager();
        try {
            File csvFile = new File(sqlm.metadataToCsvFile(meta));
            if (!csvFile.exists()) {
                Report.error(meta, "Can't load from missing file " + csvFile.getPath());
                return;
            }
            CsvFileSummary csvfs = getCsvSummary(csvFile);
            ensureColumnsMatch(meta, csvfs);
            execute(sqlm.toDeleteAllSql(meta));
            execute(sqlm.toLoadSql(meta));
            ensureAllRowsLoaded(meta, csvfs);
        } catch (Exception e) {
            String message = e.getMessage();
            Pattern p = Pattern.compile("MESSAGE: *(.*\\n)", Pattern.MULTILINE);
            Matcher m = p.matcher(message);
            if (m.find() && m.groupCount() > 0) {
                message = m.group(1);
            }
            Report.error(meta, message);
        }
    }

    private void ensureAllRowsLoaded(MetaTable meta, CsvFileSummary csvfs) throws SQLException {
        int rowsLoaded = countRowsLoaded(meta);
        int fileRecords = csvfs.records;
        int notLoaded = Math.abs(rowsLoaded-fileRecords);
        Report.info(meta, "read " + fileRecords + " records from " + csvfs.csvFile.getPath());
        Report.info(meta, "loaded " + rowsLoaded + " rows into " + meta.getTableName());            
        if (notLoaded > 0) {
            Report.error(meta, notLoaded + " records not loaded: check primary key " + meta.getPrimaryKey());
        }
    }

    private void ensureColumnsMatch(MetaTable meta, CsvFileSummary csvfs) {
        boolean columnsMatch = true;
        List<String> csvColumns = csvfs.columnNames;
        List<String> tblColumns = new ArrayList<>();
        for (Column c : meta.getAllColumns()) {
            tblColumns.add(c.getName());
        }
        if (csvColumns.size() != tblColumns.size()) {
            columnsMatch = false;
        }
        else {
            for (int i = 0; i < csvColumns.size(); i++) {
                if (!csvColumns.get(i).equalsIgnoreCase(tblColumns.get(i))) {
                    columnsMatch = false;
                    break;
                }
            }
        }
        if (!columnsMatch) {
            String message = "column lists do not match for load" + "\n" +
                            csvfs.csvFile.getPath() + ": " + csvColumns + "\n" +
                            meta + ": " + tblColumns;
            Report.error(meta, message);
        }
    }
    
    private CsvFileSummary getCsvSummary(File csvFile) {
        CsvFileSummary csvfs = new CsvFileSummary();
        csvfs.csvFile = csvFile;
        try (BufferedReader bsr = new BufferedReader(new FileReader(csvFile))) {
            String columns = bsr.readLine();
            csvfs.columnNames = Lists.newArrayList(Splitter.on(",").split(columns));
            while (bsr.readLine() != null) {
                csvfs.records++;
            }
        } catch (IOException e) {
            // no-op
        }
        return csvfs;
    }
    
    private class CsvFileSummary {
        File csvFile = null;
        List<String> columnNames = new ArrayList<>(); 
        int records = 0;
    }

    private int countFileRecords(MetaTable meta) {
        SQLFactory sqlm = SQLFactory.getSQLManager();
        String csvFilePath = sqlm.metadataToCsvFile(meta);
        File csvFile = new File(csvFilePath);
        int records = 0;
        InputStream is = null;
            try { 
                is = new BufferedInputStream(new FileInputStream(csvFile));
                for (int c = is.read(); c >= 0; c = is.read()) {
                    if (c == '\n') {
                        records++;
                    }
                }
                is.close();
            } catch (IOException e) {
            }
            finally {
                try {
                    if (is != null) is.close();
                } catch (IOException e) {
                    // no-op
                }
            }
        return Math.max(0, records-1);
    }


    private void isUniqueTable(MetaTable mt) {
        for (MetaTable mt1 : MetadataFactory.getMetadataFactory().getAllMetaTables()) {
            if (!mt.equals(mt1) && mt1.getTableName().equals(mt.getTableName())) {
                throw new RuntimeException("Table name " + mt.getTableName() + " in " + mt + " duplicates table name in " + mt1);
            }
        }
    }

    private void isValidPrimaryKey(MetaTable mo) {
        PrimaryKey pk = mo.getPrimaryKey();
        if (pk != null) {
            if (pk.getPrimaryKeyColumns().size() == 0) {
                throw new RuntimeException("primary key has no columns");
            }
        }
    }

    private void isValidSecondaryKeys(MetaTable mo) {
        for (ForeignKey fk : mo.getForeignKeys()) {
            Column fkColumn = fk.getFkColumn();
            if (fkColumn == null) {
                throw new RuntimeException(" foreign key column is null");
            }
            Column refColumn = fk.getReferenceColumn();
            if (refColumn == null) {
                throw new RuntimeException(" foreign key column is not defined");
            }
        }
    }

    private void referentialIntegrity(boolean integrity) {
        SQLFactory sqlh = SQLFactory.getSQLManager();
        String ref = sqlh.toReferentialIntegrity(integrity);
        execute(ref);
    }

    private void use(String name) throws SQLException {
        Statement s = getConnection().createStatement();
        s.executeQuery("use " + name);
    }

    private void validataMetadata(List<MetaObject> metadata) {
        for (ListIterator<MetaObject> i = metadata.listIterator(); i.hasNext();) {
            MetaObject mo = i.next();
            boolean valid = true;
            if (mo instanceof MetaTable) {
                valid = validateTable((MetaTable) mo);
            } else if (mo instanceof MetaView) {
                valid = validateView((MetaView) mo);
            }
            if (!valid) {
                i.remove();
            }
        }

    }

    private boolean validateTable(MetaTable mo) {
        try {
            isUniqueTable(mo);
            isValidPrimaryKey(mo);
            isValidSecondaryKeys(mo);
        } catch (Exception e) {
            Report.error("Skipping creation of " + mo.getIdentifier() + "\n" + e.getMessage());
            return false;
        }
        return true;
    }

    private boolean validateView(MetaView mv) {
        // TODO Auto-generated method stub
        return true;
    }

    public static Database getDatabase() {
        return database;
    }

}
