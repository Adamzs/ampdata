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
package amp.lib.io.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import amp.lib.io.errors.Report;
import amp.lib.io.meta.MetaException;
import amp.lib.io.meta.MetaObject;
import amp.lib.io.meta.MetaTable;
import amp.lib.io.meta.MetaTable.Column;
import amp.lib.io.meta.MetaTable.ForeignKey;
import amp.lib.io.meta.MetaTable.PrimaryKey;
import amp.lib.io.meta.MetaView;
import amp.lib.io.meta.MetadataFactory;

/**
 * The Database class encapsulates database operations that are mostly engine independent. It tries
 * to hide those differences that JDBC does not and to provide higher-level operations.
 */
public class Database {
    private static SQLFactory sqlFactory = SQLFactory.getSQLFactory();
    private static MetadataFactory metadataFactory = MetadataFactory.getMetadataFactory();
    private static Database database = new Database();

    private static final String BASE_USER = "root";
    private static final String BASE_PASSWORD = "p0O(i8U&y6";
    private static List<String> systemDBList = Arrays.asList("information_schema", "mysql", "performance_schema", "sys");

    private Connection dbmConnection;
    private String name;
    private String password;
    private String user;

    private Database() {
        openDatabase(null, BASE_USER, BASE_PASSWORD);
    }

    /**
     * Builds the schema from the given metadata, including all tables, indexes, keys, and views.
     * @param metadata the metadata
     */
    public void buildSchema(List<MetaObject> metadata) {
        validataMetadata(metadata);
        referentialIntegrity(false);
        buildTables(metadata);
        buildIndexes(metadata);
        buildKeys(metadata);
        buildViews(metadata);
        referentialIntegrity(true);
    }

    /**
     * Executes the SQL in the supplied files. See 'getAllSQLFiles' for execution order of files.
     * Each file may have zero or more SQL statements, each terminated by a non-escaped ';' at the
     * end of the line.
     * @param sqlFiles the SQL files and directories
     */
    public void buildSQL(List<File> sqlFiles) {
        for (File f : getAllSQLFiles(sqlFiles)) {
            try {
                String sql = FileUtils.readFileToString(f, Charset.defaultCharset());
                String[] statements = sql.split(";");
                for (String statement : statements) {
                    execute(statement);
                }
            } catch (Exception e) {
                Report.error(e.getMessage());
            }
        }
    }

    /**
     * Creates the database given its name, user id, and password.
     * @param name the database name
     * @param user the database user
     * @param password the database password
     */
    public void createDatabase(String name, String user, String password) {
        this.name = name;
        this.user = user;
        this.password = password;
        execute(sqlFactory.toCreateDatabaseSQL(name));
        use(name);
    }

    /**
     * Drop the current database.
     * @param name the database name
     */
    public void dropDatabase(String name) {
        execute(sqlFactory.toDropDatabaseSQL(name));
    }

    /**
     * Execute an SQL statement and report its execution to any listeners. Terminal semicolon added
     * if needed.
     * @param statement the statement to execute
     */
    public void execute(String statement) {
        statement = statement.trim();
        if (statement.length() == 0) {
            return;
        }
        if (!statement.endsWith(";")) {
            statement = statement + ";";
        }
        try {
            Statement stmt = getConnection().createStatement();
            stmt.execute(statement);
            Report.okay("\n" + statement);
        } catch (Exception e) {
            throw new RuntimeException("\n" + statement + "\n" + e.getMessage());
        }
    }

    /**
     * Gets the selection of databases available. The list excludes the "standard" databases.
     * @return the database selection
     */
    public List<String> getDatabaseSelection() {
        try {
            Statement s = getConnection().createStatement();
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

    /**
     * Gets the name of the database.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the password of the database.
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the user name of the database.
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * Checks if the database is open.
     * @return true, if is open
     */
    public boolean isOpen() {
        try {
            return getConnection() != null && !getConnection().isClosed();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Open a database.
     * @param name the database name
     * @param user the database user
     * @param password database the password
     */
    public void openDatabase(String name, String user, String password) {
        this.name = name;
        this.user = user;
        this.password = password;
        try {
            if (getConnection() != null) {
                getConnection().close();
                setConnection(null);
            }
            Class.forName(sqlFactory.getDriverClassName());
            dbmConnection = DriverManager.getConnection(sqlFactory.getBaseUrl(null), user, password);
            use(name);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Populate database tables from CSV files. For every metadata file of the form "foo.csv.meta",
     * the corresponding data is found in "foo.csv", with exactly corresponding column names.
     * @param metadata the list of metadata objects
     */
    public void populateTables(List<MetaObject> metadata, String scenarioName) {
        referentialIntegrity(false);
        for (MetaObject meta : metadata) {
            if (meta.isValid() && meta instanceof MetaTable) {
                importCSVFile((MetaTable) meta, scenarioName);
            }
        }
        referentialIntegrity(true);
    }

    /**
     * Execute an SQL Query.
     * @param querySql the query SQL
     * @return the result set returned from the query
     */
    public ResultSet query(String querySql) {
        try {
            Statement stmt = getConnection().createStatement();
            return stmt.executeQuery(querySql + ";");
        } catch (Exception e) {
            throw new RuntimeException(querySql + "\n" + e.getMessage());
        }
    }

    /*
     * Build indexes to support the supplied metadata objects. These get referenced in the later
     * step of creating foreign keys. For each table: 1. A primary key index (if required) on the
     * specified columns. 2. Zero or more foreign key indexes on the referenced columns.
     */
    private void buildIndexes(List<MetaObject> metadata) {
        Set<Column> indexColumns = new HashSet<>();
        SQLFactory sqlm = SQLFactory.getSQLFactory();
        for (MetaObject mo : metadata) {
            if (mo.isValid() && mo instanceof MetaTable) {
                MetaTable mt = (MetaTable) mo;
                PrimaryKey pk = mt.getPrimaryKey();
                if (pk != null) {
                    indexColumns.addAll(pk.getPrimaryKeyColumns());
                }
                for (ForeignKey fk : mt.getForeignKeys()) {
                    indexColumns.add(fk.getFkColumn());
                    indexColumns.add(fk.getReferenceColumn());
                }
            }
        }
        for (Column col : indexColumns) {
            try {
                execute(sqlm.toDropIndexSQL(col));
            } catch (Exception e) {
                // no-op
            }
            try {
                execute(sqlm.toCreateIndexSQL(col));
            } catch (Exception e) {
                Report.error(col.getTable(), e.getMessage());
            }
        }
    }

    /*
     * Create the keys for the metadata tables. For each table in the metadata: 1. zero or one
     * primary key on the specified columns 2. zero or more foreign keys on specified columns
     * referring to other table colums.
     */
    private void buildKeys(List<MetaObject> metadata) {
        SQLFactory sqlm = SQLFactory.getSQLFactory();
        for (MetaObject mo : metadata) {
            if (mo.isValid() && mo instanceof MetaTable) {
                MetaTable mt = (MetaTable) mo;
//                PrimaryKey pk = mt.getPrimaryKey();
//                if (pk != null) {
//                    try {
//                        validatePrimaryKey(pk);
//                        String sql = sqlm.toPrimaryKeySQL(pk);
//                        execute(sql);
//                    } catch (Exception e) {
//                        Report.error(mt, e.getMessage());
//                    }
//                }
                for (ForeignKey fk : mt.getForeignKeys()) {
                    try {
                        validateForeignKey(fk);
                        String sql = sqlm.toForeignKeySQL(fk);
                        execute(sql);
                    } catch (Exception e) {
                        Report.error(mt, e.getMessage());
                    }
                }
            }
        }
    }

    /*
     * Creates the tables from the specified metadata. For each MetaTable: 1. Drop the table if it
     * exists. 2. Build the "Create table" SQL to fit this database. 3. Execute it.
     */
    private void buildTables(List<MetaObject> metadata) {
        for (MetaObject mo : metadata) {
            if (mo.isValid() && mo instanceof MetaTable) {
                MetaTable mt = (MetaTable) mo;
                try {
                    execute(sqlFactory.toDropTableSQL(mt));
                } catch (Exception e1) {
                    // no-op
                }
                try {
                    execute(sqlFactory.toTableSQL(mt));
                } catch (Exception e) {
                    Report.error(mt, e.getMessage());
                }
            }
        }
    }

    /*
     * Creates the views from the specified metadata. For each MetaView: 1. Build the "Create view"
     * SQL to fit this database. 2. Execute it.
     */
    private void buildViews(List<MetaObject> metadata) {
        for (MetaObject mo : metadata) {
            if (mo.isValid() && mo instanceof MetaView) {
                MetaView mv = (MetaView) mo;
                try {
                    String sql = sqlFactory.toViewSQL(mv);
                    execute(sql);
                } catch (Exception e) {
                    Report.error(mv, e.getMessage());
                }
            }
        }
    }

    /*
     * Count the rows of a table, so we can report them after a load.
     */
    private int countRowsLoaded(MetaTable meta) throws SQLException {
        SQLFactory sqlm = SQLFactory.getSQLFactory();
        ResultSet rs = query(sqlm.toCountRowsSql(meta));
        int rows = rs.next() ? rs.getInt(1) : 0;
        return rows;
    }

    /*
     * Make sure that we loaded all the rows we expected.
     */
    private void ensureAllRowsLoaded(MetaTable meta, CsvFileSummary csvfs) throws SQLException {
        int rowsLoaded = countRowsLoaded(meta);
        int fileRecords = csvfs.records;
        int notLoaded = Math.abs(rowsLoaded - fileRecords);
        Report.info(meta, "read " + fileRecords + " records from " + csvfs.csvFile.getPath());
        Report.info(meta, "loaded " + rowsLoaded + " rows into " + meta.getTableName());
        if (notLoaded > 0) {
            Report.error(meta, notLoaded + " records not loaded: check primary key " + meta.getPrimaryKey());
        }
    }

    /*
     * Make sure that the CSV headers are an exact match (modulo case) in order, name, and sequence.
     */
    private void ensureColumnsMatch(MetaTable meta, CsvFileSummary csvfs) {
        boolean columnsMatch = true;
        List<String> csvColumns = csvfs.columnNames;
        List<String> tblColumns = new ArrayList<>();
        for (Column c : meta.getAllColumns()) {
            tblColumns.add(c.getName());
        }
        if (csvColumns.size() != tblColumns.size()) {
            columnsMatch = false;
        } else {
            for (int i = 0; i < csvColumns.size(); i++) {
                if (!csvColumns.get(i).equalsIgnoreCase(tblColumns.get(i))) {
                    columnsMatch = false;
                    break;
                }
            }
        }
        if (!columnsMatch) {
            String message = "column lists do not match for load" + "\n" + csvfs.csvFile.getPath() + ": " + csvColumns + "\n" + meta + ": " + tblColumns;
            throw new RuntimeException(message);
        }
    }

    /*
     * Make sure the table is unique in the database.
     */
    private void ensureUniqueTable(MetaTable mt) {
        for (MetaTable mt1 : metadataFactory.getAllMetaTables()) {
            if (!mt.equals(mt1) && mt1.getTableName().equals(mt.getTableName())) {
                throw new MetaException(mt, "Table name " + mt.getTableName() + " in " + mt + " duplicates table name in " + mt1);
            }
        }
    }

    private void ensureValidColumns(MetaTable mo) {
        if (mo.getAllColumns().size() == 0) {
            throw new MetaException(mo, "No columns defined");
        }

    }

    /*
     * Make sure all foreign keys are valid
     */
    private void ensureValidForeignKeys(MetaTable mo) {
        for (ListIterator<ForeignKey> i = mo.getForeignKeys().listIterator(); i.hasNext();) {
            ForeignKey fk = i.next();
            Column fkColumn = fk.getFkColumn();
            boolean valid = true;
            if (fkColumn == null) {
                Report.error(mo, "Foreign key " + fk + " table column is null");
                valid = false;
            }
            Column refColumn = fk.getReferenceColumn();
            if (refColumn == null) {
                Report.error(mo, "Foreign key " + fk + " reference column is not defined");
                valid = false;
            }
            if (!valid) {
                i.remove();
            }
        }
    }

    /*
     * Make sure primary key (if any) is valid
     */
    private void ensureValidPrimaryKey(MetaTable mo) {
        PrimaryKey pk = mo.getPrimaryKey();
        if (pk != null) {
            if (pk.getPrimaryKeyColumns().size() == 0) {
                Report.error(mo, "primary key " + pk + " has no columns");
                mo.setPrimaryKey(null);
            }
        }
    }

    /**
     * Finds all the SQL files that descend from the given files/directories. Files are sorted as
     * follows: 1. depth-first traversal of directories first 2. SQL files of this directory, sorted
     * by file name. So, to ensure ordering of SQL execution, place dependent files in
     * sub-directories, or name files something like "1_Foo.sql", "2_Bar.sql".
     * @param sqlFiles List of directories/files to search
     * @return all SQL files in the supplied directory tree, sorted as described abobe.
     */
    private List<File> getAllSQLFiles(List<File> sqlFiles) {
        List<File> orderedFiles = new ArrayList<>();
        for (File f : sqlFiles) {
            if (f.isDirectory()) {
                List<File> files = Arrays.asList(f.listFiles());
                files.sort(new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        return o1.getName().compareTo(o2.getName());
                    };
                });
                orderedFiles.addAll(getAllSQLFiles(files));
            }
        }
        for (File f : sqlFiles) {
            if (!f.isDirectory() && f.getName().endsWith(".sql")) {
                orderedFiles.add(f);
            }
        }
        return orderedFiles;
    }

    /*
     * The JDBC connection.
     */
    private Connection getConnection() {
        return dbmConnection;
    }

    /*
     * Get the summary info from the CSV file that we need to do checks.
     */
    private CsvFileSummary getCsvSummary(MetaTable meta, File csvFile) {
        CsvFileSummary csvfs = new CsvFileSummary();
        csvfs.csvFile = csvFile;
        try (BufferedReader bsr = new BufferedReader(new FileReader(csvFile))) {
            String columns = bsr.readLine();
            if (columns == null || columns.length() == 0) {
                throw new RuntimeException(csvFile.getPath() + " is empty");
            }
            if (columns == null || columns.length() == 0) {
                throw new RuntimeException(csvFile.getPath() + " has no column headers");
            }
            csvfs.columnNames = Lists.newArrayList(Splitter.on(",").split(columns));
            while (bsr.readLine() != null) {
                csvfs.records++;
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return csvfs;
    }

    /*
     * Import data into a table from its corresponding CSV file.
     */
    private void importCSVFile(MetaTable meta, String scenarioName) {
        SQLFactory sqlm = SQLFactory.getSQLFactory();
        try {
            File csvFile = new File(sqlm.metadataToCsvFile(meta));
            CsvFileSummary csvfs = getCsvSummary(meta, csvFile);
            ensureColumnsMatch(meta, csvfs);
            execute(sqlm.toDeleteAllSql(meta, scenarioName));
            execute(sqlm.toLoadSql(meta, scenarioName));
            ensureAllRowsLoaded(meta, csvfs);
        } catch (Exception e) {
            // Here we extract the useful information from the over-specified
            // error message.
            String message = sqlm.simplifyErrorMessage(e.getMessage());
            Report.error(meta, message);
        }
    }

    /*
     * Turn referential integrity checks on/off
     */
    private void referentialIntegrity(boolean integrity) {
        String ref = sqlFactory.toReferentialIntegrity(integrity);
        execute(ref);
    }

    private void setConnection(Connection connection) {
        dbmConnection = connection;
    }

    /*
     * Specify the database to use.
     */
    private void use(String name) {
        if (name != null && name.length() > 0) {
            execute(sqlFactory.toUseSQL(name));
        }
    }

    /*
     * Checks a MetaObject for validity, removes it if not valid.
     */
    private void validataMetadata(List<MetaObject> metadata) {
        for (ListIterator<MetaObject> i = metadata.listIterator(); i.hasNext();) {
            MetaObject mo = i.next();
            boolean valid = true;
            if (mo instanceof MetaTable) {
                valid = validateTable((MetaTable) mo);
            } else if (mo instanceof MetaView) {
                valid = validateView((MetaView) mo);
            }
            mo.setValid(valid);
        }

    }

    /*
     * Ensures foreign key is valid.
     */
    private void validateForeignKey(ForeignKey fk) {
        if (fk != null) {
            if (fk.getReferenceColumn() == null) {
                throw new RuntimeException(fk + ": " + " column reference is unknown");
            }
            if (fk.getFkColumn() == null) {
                throw new RuntimeException(fk + ": " + " column is unknown");
            }
            if (fk.getReferenceColumn() == null) {
                throw new RuntimeException(fk + ": " + " table reference is unknown");
            }
        }
    }

    /*
     * Ensures primary key is valid.
     */
    private void validatePrimaryKey(PrimaryKey pk) {
        if (pk != null) {
            List<Column> pkColumns = pk.getPrimaryKeyColumns();
            for (Column pkColumn : pkColumns) {
                if (pkColumn == null) {
                    throw new RuntimeException("primary key has null column");
                }
                if (pkColumn.getName() == null || pkColumn.getName().trim().isEmpty()) {
                    throw new RuntimeException("primary key has empty column");
                }
            }
        }
    }

    /*
     * Validates a MetaTable
     */
    private boolean validateTable(MetaTable mo) {
        try {
            ensureValidColumns(mo);
            ensureUniqueTable(mo);
            ensureValidPrimaryKey(mo);
            ensureValidForeignKeys(mo);
        } catch (Exception e) {
            Report.error("Skipping creation of " + mo.getIdentifier() + "\n" + e.getMessage());
            return false;
        }
        return true;
    }

    /*
     * Validates a MetaView.
     */
    private boolean validateView(MetaView mv) {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * Gets the current database.
     * @return the current database
     */
    public static Database getDatabase() {
        return database;
    }

    /*
     * The CSV file summary information.
     */
    private class CsvFileSummary {
        File csvFile = null;
        List<String> columnNames = new ArrayList<>();
        int records = 0;
    }

}
