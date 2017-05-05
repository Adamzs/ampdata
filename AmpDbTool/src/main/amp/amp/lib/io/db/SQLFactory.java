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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.MapUtils;

import com.google.common.base.Joiner;

import amp.lib.io.meta.MetaTable;
import amp.lib.io.meta.MetaTable.Column;
import amp.lib.io.meta.MetaTable.ForeignKey;
import amp.lib.io.meta.MetaTable.PrimaryKey;
import amp.lib.io.meta.MetaView;
import amp.lib.io.meta.MetaView.ViewColumn;
import amp.lib.io.meta.MetaView.ViewJoin;

/**
 * A factory for creating SQL strings for the current database engine.
 */
public class SQLFactory {

    private static SQLFactory sqlManager = new SQLFactory();

    @SuppressWarnings("unchecked")
    private static final Map<String, String> dataTypeMap = MapUtils.putAll(new HashMap<String, String>(),
        new String[][] { 
        { "string", "VARCHAR(50)" }, 
        { "integer", "INT" }, 
        { "long", "BIGINT" }, 
        { "float", "DECIMAL(10,2)" }, 
        { "boolean", "BOOLEAN" }, 
        { "double", "DECIMAL(10,2)" } 
        });

    /**
     * Deblank a string
     *
     * @param text the text.
     * @return the deblanked string.
     */
    public String deblank(String text) {
        return text.replaceAll(" +", "");
    }

    /**
     * Dequote a string.
     *
     * @param text the text.
     * @return the dequoted string.
     */
    public String dequote(String text) {
        return text.replaceAll("['\"`]", " ");
    }

    /**
     * Gets the SQL datatype string for the given column
     *
     * @param c the Column
     * @return the SQL datatype string
     */
    public String toSqlDatatype(Column c) {
        String sqlType = dataTypeMap.get(c.getDatatype());
        if (sqlType == null) {
            throw new RuntimeException("Unknown datatype:" + c.getDatatype());
        } else if (c.getMaxLength() > 0) {
            sqlType = "VARCHAR(" + c.getMaxLength() + ")";
        }
        return sqlType;
    }

    /**
     * Normalize an SQL identifier by dequoting, deblanking, and escaping it
     *
     * @param text the text
     * @return the normalized string
     */
    public String normalize(String text) {
        text = dequote(text);
        text = deblank(text);
        text = escapeKeywords(text);
        return text;
    }

    /**
     * SQL to count rows in a table.
     *
     * @param meta the metaTable
     * @return the SQL string
     */
    public String toCountRowsSql(MetaTable meta) {
        return "SELECT COUNT(*) FROM " + normalize(meta.getTableName());
    }

    /**
     * SQL to delete all rows in a table.
     *
     * @param meta the metaTable
     * @return the SQL string
     */
    public String toDeleteAllSql(MetaTable meta) {
        return "DELETE FROM " + normalize(meta.getTableName()) + " WHERE(1=1)";
    }

    /**
     * SQL to drop index.
     *
     * @param indexName the index name
     * @return the SQL string
     */
    public String toDropIndexSQL(String indexName) {
        return "DROP INDEX " + indexName;
    }

    /**
     * SQL to drop a table.
     *
     * @param table the metaTable
     * @return the SQL string
     */
    public String toDropTableSQL(MetaTable table) {
        return "DROP TABLE IF EXISTS " + normalize(table.getTableName());
    }

    /**
     * SQL to create a foreign index.
     *
     * @param fk the foreign key
     * @return the SQL string
     */
    public String toForeignIndexSQL(ForeignKey fk) {
        validateForeignKey(fk);
        Column fkRef = fk.getFkColumn();
        String fkColumnName = normalize(fkRef.getName());
        String fkTableName = normalize(fkRef.getTable().getTableName());
        String sql = "CREATE INDEX " + toForeignKeyIndexName(fk) + " ON " + fkTableName + "(" + fkColumnName + ")";
        return sql;
    }

    /**
     * SQL to create a foreign key.
     *
     * @param fk the foreign key
     * @return the SQLstring
     */
    public String toForeignKeySQL(ForeignKey fk) {
        validateForeignKey(fk);
        Column pkRef = fk.getReferenceColumn();
        String pkTableName = normalize(pkRef.getTable().getTableName());
        String pkColumnName = normalize(pkRef.getName());
        Column fkRef = fk.getFkColumn();
        String fkColumnName = normalize(fkRef.getName());
        String fkTableName = normalize(fkRef.getTable().getTableName());
        String sql = "ALTER TABLE " + fkTableName + " ADD CONSTRAINT " + toForeignKeyIndexName(fk) + " FOREIGN KEY (" + fkColumnName + ")" + " REFERENCES " + pkTableName + "(" + pkColumnName + ")";
        return sql;
    }

    /**
     * Creates a foreign key index name
     *
     * @param fk the foreign key
     * @return the string
     */
    public String toForeignKeyIndexName(ForeignKey fk) {
        Column fkRef = fk.getFkColumn();
        String fkColumnName = fkRef.getName();
        String fkTableName = fkRef.getTable().getTableName();
        return "FK_" + deblank(fkTableName) + "_" + deblank(fkColumnName);
    }

    /**
     * Creates a primary key index name.
     *
     * @param pk the Primary key
     * @return the string
     */
    public String toPrimaryKeyIndexName(PrimaryKey pk) {
        return "PK_" + pk.getTable().getTableName();
    }

    /**
     * SQL to load a table from a CSV file.
     * Handles conversion of "true" to "1" in boolean columns
     *
     * @param meta the metaTable
     * @return the string
     */
    public String toLoadSql(MetaTable meta) {
        String csvFilePath = metadataToCsvFile(meta);
        StringBuilder sql = new StringBuilder();
        sql.append("LOAD DATA LOCAL INFILE '" + csvFilePath + "'");
        sql.append("\n INTO TABLE " + normalize(meta.getTableName()));
        sql.append("\n COLUMNS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"'");
        sql.append("\n IGNORE 1 ROWS");
        List<String> columns = new ArrayList<>();
        List<String> assignments = new ArrayList<>();
        for (Column c : meta.getAllColumns()) {
            String columnName = c.getName();
            String variable = toVariable(columnName);
            if (c.getDatatype().equals("boolean")) {
                columns.add(variable);
                assignments.add(normalize(columnName) + " = (" + variable + "='true'" + ")");
            } else {
                columns.add(normalize(columnName));
            }
        }
        sql.append("\n (" + Joiner.on(",").join(columns) + ")");
        if (assignments.size() > 0) {
            sql.append("\n SET ");
            sql.append(Joiner.on(",\n     ").join(assignments));
        }
        return sql.toString();
    }

    /**
     * The CSV file associated with this metaFile.
     *
     * @param meta the metaFile
     * @return the CSV file path.
     */
    public String metadataToCsvFile(MetaTable meta) {
        String metaFilePath = meta.getFile().getAbsolutePath();
        String csvFilePath = metaFilePath.replaceAll(".meta", "");
        return csvFilePath;
    }

    /**
     * SQL to create a primary index.
     *
     * @param pk the primary key.
     * @return the string
     */
    public String toPrimaryIndexSQL(PrimaryKey pk) {
        validatePrimaryKey(pk);
        String pkTableName = normalize(pk.getTable().getTableName());
        String indexName = normalize(toPrimaryKeyIndexName(pk));
        List<String> pkColumns = new ArrayList<>();
        for (Column pkColumn : pk.getPrimaryKeyColumns()) {
            pkColumns.add(normalize(pkColumn.getName()));
        }
        return "CREATE INDEX " + indexName + " ON " + pkTableName + "(" + Joiner.on(",").join(pkColumns) + ")";
    }

    /**
     * SQL to create a primary key.
     *
     * @param pk the primary key.
     * @return the string
     */
    public String toPrimaryKeySQL(PrimaryKey pk) {
        validatePrimaryKey(pk);
        String pkTableName = normalize(pk.getTable().getTableName());
        List<String> pkColumns = new ArrayList<>();
        for (Column pkColumn : pk.getPrimaryKeyColumns()) {
            pkColumns.add(normalize(pkColumn.getName()));
        }
        return "ALTER TABLE " + pkTableName + " ADD PRIMARY KEY(" + Joiner.on(",").join(pkColumns) + ")";
    }

    /**
     * SQL to turn referential integrity checking on/off
     *
     * @param state the referential integrity state
     * @return the string
     */
    public String toReferentialIntegrity(boolean state) {
        return "SET @@foreign_key_checks = " + (state ? "1" : "0");
    }

    /**
     * SQL to create a table.
     *
     * @param meta the metaTable 
     * @return the string
     */
    public String toTableSQL(MetaTable meta) {
        StringBuffer sql = new StringBuffer();
        sql.append("CREATE TABLE " + normalize(meta.getTableName()));
        List<String> columnDecls = new ArrayList<>();
        for (Column col : meta.getAllColumns()) {
            String decl = toColumnSQL(col);
            columnDecls.add(decl);
        }
        sql.append(" (\n   ");
        sql.append(Joiner.on(",\n   ").join(columnDecls));
        sql.append("\n)");
        sql.append(" ENGINE = INNODB");
        return sql.toString();
    }

    /**
     * SQL to create a view.
     *
     * @param view the MetaView.
     * @return the string
     */
    public String toViewSQL(MetaView view) {
        StringBuilder sql = new StringBuilder();
        Set<String> tables = new HashSet<>();
        List<String> columns = new ArrayList<>();
        List<String> joins = new ArrayList<>();
        for (ViewColumn col : view.getColumns()) {
            Column ref = col.getRefColumn();
            if (ref == null)
                continue;
            MetaTable refTable = ref.getTable();
            if (refTable == null)
                continue;
            String refTableName = normalize(refTable.getTableName());
            String refColumnName = normalize(ref.getName());
            String columnName = refTableName + "." + refColumnName;
            columns.add(columnName);
            tables.add(refTableName);
        }
        for (ViewJoin vj : view.getJoins()) {
            ViewColumn vc1 = vj.getColumn1();
            ViewColumn vc2 = vj.getColumn2();
            if (vc1 == null || vc2 == null)
                continue;
            String vc1Name = toViewColumnName(vc1);
            String vc2Name = toViewColumnName(vc2);
            if (vc1Name == null || vc2Name == null)
                continue;
            joins.add(vc1Name + vj.getOperator() + vc2Name);
        }
        sql.append("CREATE OR REPLACE VIEW " + normalize(view.getTitle()) + " AS");
        sql.append("\n SELECT ");
        sql.append(Joiner.on(",").join(columns));
        sql.append("\n FROM ");
        sql.append(Joiner.on(",").join(tables));
        if (joins.size() > 0) {
            sql.append("\n WHERE ");
            sql.append(Joiner.on("\n    AND ").join(joins));
        }
        return sql.toString();
    }

    /*
     * Gets a full column name for a Column
     */
    private String toViewColumnName(ViewColumn viewColumn) {
        if (viewColumn == null || viewColumn.getRefColumn() == null)
            return null;
        Column vcColumn = viewColumn.getRefColumn();
        if (vcColumn == null)
            return null;
        MetaTable vcTable = viewColumn.getRefColumn().getTable();
        if (vcTable == null)
            return null;
        return normalize(vcTable.getTableName()) + "." + normalize(vcColumn.getName());
    }

    /*
     * SQL for creating a column definition.
     */
    private String toColumnSQL(Column col) {
        StringBuilder decl = new StringBuilder();
        decl.append(normalize(col.getName()) + " " + toSqlDatatype(col));
        if (col.getDescription() != null && col.getDescription().length() > 0) {
            String comment = col.getDescription();
            comment = escapeKeywords(dequote(comment));
            decl.append(" COMMENT '" + comment + "'");
        }
        return decl.toString();
    }

    /*
     * Creates a variable name.
     */
    private String toVariable(String column) {
        return "@" + column;
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

    /**
     * Simplifies an every complex SQL error message.
     *
     * @param message the message
     * @return the simplified message
     */
    public String simplifyErrorMessage(String message) {
        Pattern p = Pattern.compile("MESSAGE: *(.*\\n)", Pattern.MULTILINE);
        Matcher m = p.matcher(message);
        if (m.find() && m.groupCount() > 0) {
            message = m.group(1);
        }
        return message;
    }

    /**
     * Gets the SQL factory.
     *
     * @return the SQL factory
     */
    public static SQLFactory getSQLFactory() {
        return sqlManager;
    }

    /**
     * Creates USE SQL.
     *
     * @param name the database name
     * @return the USE SQL string
     */
    public String toUseSQL(String name) {
        return "use " + name;
    }

    /**
     * Escapes all keywords in the statement.
     *
     * @param statement the statement
     * @return the escaped statement
     */
    public String escapeKeywords(String statement) {
        String[] tokens = statement.split("(?<=[, ]+)|(?=[, ]+)");
        for (int i = 0; i < tokens.length; i++) {
            if (isKeyword(tokens[i])) {
                tokens[i] = "`" + tokens[i] + "`";
            }
        }
        return Joiner.on("").join(tokens);
    }

    /**
     * Checks if this is keyword.
     *
     * @param word the word
     * @return true, if is keyword
     */
    public boolean isKeyword(String word) {
        return Keywords.keywordSet.contains(word.toUpperCase());
    }
}
