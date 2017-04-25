package amp.lib.io.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import com.google.common.base.Joiner;

import amp.lib.io.errors.ErrorEvent;
import amp.lib.io.errors.ErrorEventListener;
import amp.lib.io.errors.ErrorReporter;
import amp.lib.io.meta.Metadata;
import amp.lib.io.meta.Metadata.Column;
import amp.lib.io.meta.Metadata.ForeignKeyColumn;
import amp.lib.io.meta.Metadata.PrimaryKeyColumn;

public class SQLManager implements ErrorReporter, ErrorEventListener {

    private static SQLManager sqlManager = new SQLManager();

    @SuppressWarnings("unchecked")
    private static final Map<String, String> dataTypeMap = MapUtils.putAll(new HashMap<String, String>(), new String[][] {
                    {
                                    "string", "VARCHAR(50)"
                    }, {
                                    "integer", "INT(11)"
                    }, {
                                    "long", "INT(11)"
                    }, {
                                    "float", "DECIMAL(15,0)"
                    }, {
                                    "boolean", "TINYINT(1)"
                    }, {
                                    "double", "DECIMAL(15,0)"
                    }
    });

    private List<ErrorEventListener> listeners = new ArrayList<>();

    @Override
    public void addErrorEventListener(ErrorEventListener listener) {
        listeners.add(listener);

    }

    @Override
    public void errorOccurred(ErrorEvent event) {
        reportError(event);

    }

    public String getDatatypeString(Column c) {
        String sqlType = dataTypeMap.get(c.getDatatype());
        if (sqlType == null) {
            throw new RuntimeException("Unknown datatype:" + c.getDatatype());
        } else if (c.getMaxLength() > 0) {
            sqlType = "VARCHAR(" + c.getMaxLength() + ")";
        }
        return sqlType;
    }

    @Override
    public void reportError(ErrorEvent event) {
        for (ErrorEventListener l : listeners) {
            l.errorOccurred(event);
        }
    }

    public String toDeleteAllSql(Metadata meta) {
        return "DELETE FROM " + normalize(meta.getTableName()) + " WHERE(1=1)";
    }

    public String toDropTableSQL(String tableName) {
        return "DROP TABLE IF EXISTS " + normalize(tableName);
    }

    public String toInsertSQL(Metadata metadata, List<String> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO " + metadata.getTableName() + "(");
        for (Column col : metadata.getColumns()) {
            sb.append(this.equals(col.getName()));
            sb.append(",");
        }
        if (sb.length() > 0)
            sb.setLength(sb.length() - 1);
        sb.append(") VALUES (");
        for (String value : data) {
            sb.append("'");
            sb.append(value == null ? "null" : value);
            sb.append("'");
            sb.append(",");
        }
        if (sb.length() > 0)
            sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public String toLoadSql(Metadata meta) {
        String metaFilePath = meta.getFile().getFile().getAbsolutePath();
        String csvFilePath = metaFilePath.replaceAll(".meta", "");
        StringBuilder sql = new StringBuilder();
        csvFilePath = csvFilePath.replace("\\", "/");
        sql.append("LOAD DATA LOCAL INFILE '" + csvFilePath + "'");
        sql.append(" INTO TABLE " + normalize(meta.getTableName()));
        sql.append(" COLUMNS TERMINATED BY ','");
        sql.append(" OPTIONALLY ENCLOSED BY '\"'");
        sql.append(" IGNORE 1 ROWS");
        return sql.toString();

    }

    public String toReferentialIntegrity(boolean state) {
        return "SET @@foreign_key_checks = " + (state ? "1" : "0");
    }

    /**
     * Returns the SQL needed to create a table for the given MetadataObject
     * 
     * @param meta
     *            the object to create
     * @return the creation SQL
     */
    public List<String> toTableSQL(Metadata meta) {
        List<String> sql = new ArrayList<>();
        if (meta.getColumns().size() == 0) {
            throw new RuntimeException(meta + ": no columns defined");
        }

        sql.add(toDropTableSQL(meta.getTableName()));

        StringBuffer create = new StringBuffer();
        create.append("CREATE TABLE IF NOT EXISTS " + normalize(meta.getTableName()));
        List<String> columnDecls = new ArrayList<>();
        for (Column col : meta.getColumns()) {
            String decl = toPlainColumn(col);
            columnDecls.add(decl);
        }
        if (meta.getPrimaryKey() != null) {
            columnDecls.add(toPrimaryKey(meta.getPrimaryKey()));
        }
        for (ForeignKeyColumn fk : meta.getForeignKeys()) {
            columnDecls.add(toForeignKey(fk));
        }
        create.append(" (\n   ");
        create.append(Joiner.on(",\n   ").join(columnDecls));
        create.append("\n)");
        create.append(" ENGINE = INNODB");
        sql.add(create.toString());

        for (Column col : meta.getColumns()) {
            if (col instanceof PrimaryKeyColumn) {
                sql.addAll(toPrimaryIndex((PrimaryKeyColumn) col));
            } else if (col instanceof ForeignKeyColumn) {
                sql.addAll(toForeignIndex((ForeignKeyColumn) col));
            }
        }
        return sql;
    }

    private String deblank(String text) {
        return text.replaceAll(" +", "");
    }

    private String dequote(String text) {
        return text.replaceAll("['\"`]", " ");
    }

    @SuppressWarnings("unused")
    private void newLine(StringBuffer command) {
        command.append("\n   ");
    }

    private String normalize(String text) {
        text = dequote(text);
        text = deblank(text);
        text = Keywords.escapeKeywords(text);
        return text;
    }

    private List<String> toForeignIndex(ForeignKeyColumn col) {
        return toIndex(col);
    }

    private String toForeignKey(ForeignKeyColumn col) {
        PrimaryKeyColumn ref = col.getColumnReference();
        String constraint = "CONSTRAINT FOREIGN KEY(" + normalize(col.getName()) + ") " + "REFERENCES " + normalize(ref.getTable().getTableName()) + "(" + normalize(ref.getName()) + ")";
        return constraint;
    }

    private List<String> toIndex(Column col) {
        List<String> sql = new ArrayList<>();
        sql.add("CREATE INDEX " + toIndexName(col) + " ON " + normalize(col.getTable().getTableName()) + "(" + normalize(col.getName()) + ")");
        return sql;
    }

    private String toIndexName(Column col) {
        return normalize(col.getTable().getTableName() + "_" + col.getName() + "_IDX");
    }

    private String toPlainColumn(Column col) {
        StringBuilder decl = new StringBuilder();
        decl.append(normalize(col.getName()) + " " + getDatatypeString(col));
        if (col.getDescription() != null && col.getDescription().length() > 0) {
            String comment = col.getDescription();
            comment = Keywords.escapeKeywords(dequote(comment));
            decl.append(" COMMENT '" + comment + "'");
        }
        return decl.toString();
    }

    private List<String> toPrimaryIndex(PrimaryKeyColumn col) {
        return toIndex(col);
    }

    private String toPrimaryKey(PrimaryKeyColumn col) {
        return "CONSTRAINT PRIMARY KEY(" + normalize(col.getName()) + ")";
    }

    public static SQLManager getSQLManager() {
        return sqlManager;
    }
}
