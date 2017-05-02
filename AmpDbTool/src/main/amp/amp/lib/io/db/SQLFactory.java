package amp.lib.io.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;

import com.google.common.base.Joiner;

import amp.lib.io.meta.MetaTable;
import amp.lib.io.meta.MetaTable.Column;
import amp.lib.io.meta.MetaTable.ForeignKey;
import amp.lib.io.meta.MetaTable.PrimaryKey;
import amp.lib.io.meta.MetaView;
import amp.lib.io.meta.MetaView.ViewColumn;
import amp.lib.io.meta.MetaView.ViewJoin;

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

    public String deblank(String text) {
        return text.replaceAll(" +", "");
    }

    public String dequote(String text) {
        return text.replaceAll("['\"`]", " ");
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

    public String normalize(String text) {
        text = dequote(text);
        text = deblank(text);
        text = Keywords.escapeKeywords(text);
        return text;
    }

    public String toCountRowsSql(MetaTable meta) {
        return "SELECT COUNT(*) FROM " + normalize(meta.getTableName());
    }

    public String toDeleteAllSql(MetaTable meta) {
        return "DELETE FROM " + normalize(meta.getTableName()) + " WHERE(1=1)";
    }

    public String toDropIndexSQL(String indexName) {
        return "DROP INDEX " + indexName;
    }

    public String toDropTableSQL(MetaTable table) {
        return "DROP TABLE IF EXISTS " + normalize(table.getTableName());
    }

    public String toForeignIndexSQL(ForeignKey fk) {
        validateForeignKey(fk);
        Column fkRef = fk.getFkColumn();
        String fkColumnName = normalize(fkRef.getName());
        String fkTableName = normalize(fkRef.getTable().getTableName());
        String sql = "CREATE INDEX " + toIndexName(fk) + " ON " + fkTableName + "(" + fkColumnName + ")";
        return sql;
    }

    public String toForeignKeySQL(ForeignKey fk) {
        validateForeignKey(fk);
        Column pkRef = fk.getReferenceColumn();
        String pkTableName = normalize(pkRef.getTable().getTableName());
        String pkColumnName = normalize(pkRef.getName());
        Column fkRef = fk.getFkColumn();
        String fkColumnName = normalize(fkRef.getName());
        String fkTableName = normalize(fkRef.getTable().getTableName());
        String sql = "ALTER TABLE " + fkTableName + " ADD CONSTRAINT " + toIndexName(fk) + " FOREIGN KEY (" + fkColumnName + ")" + " REFERENCES " + pkTableName + "(" + pkColumnName + ")";
        return sql;
    }

    public String toIndexName(ForeignKey fk) {
        Column fkRef = fk.getFkColumn();
        String fkColumnName = fkRef.getName();
        String fkTableName = fkRef.getTable().getTableName();
        return "FK_" + deblank(fkTableName) + "_" + deblank(fkColumnName);
    }

    public String toIndexName(PrimaryKey pk) {
        return "PK_" + pk.getTable().getTableName();
    }

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

    public String metadataToCsvFile(MetaTable meta) {
        String metaFilePath = meta.getFile().getAbsolutePath();
        String csvFilePath = metaFilePath.replaceAll(".meta", "");
        return csvFilePath;
    }

    public String toPrimaryIndexSQL(PrimaryKey pk) {
        validatePrimaryKey(pk);
        String pkTableName = normalize(pk.getTable().getTableName());
        String indexName = normalize(toIndexName(pk));
        List<String> pkColumns = new ArrayList<>();
        for (Column pkColumn : pk.getPrimaryKeyColumns()) {
            pkColumns.add(normalize(pkColumn.getName()));
        }
        return "CREATE INDEX " + indexName + " ON " + pkTableName + "(" + Joiner.on(",").join(pkColumns) + ")";
    }

    public String toPrimaryKeySQL(PrimaryKey pk) {
        validatePrimaryKey(pk);
        String pkTableName = normalize(pk.getTable().getTableName());
        List<String> pkColumns = new ArrayList<>();
        for (Column pkColumn : pk.getPrimaryKeyColumns()) {
            pkColumns.add(normalize(pkColumn.getName()));
        }
        return "ALTER TABLE " + pkTableName + " ADD PRIMARY KEY(" + Joiner.on(",").join(pkColumns) + ")";
    }

    public String toReferentialIntegrity(boolean state) {
        return "SET @@foreign_key_checks = " + (state ? "1" : "0");
    }

    public String toTableSQL(MetaTable meta) {
        if (meta.getAllColumns().size() == 0) {
            throw new RuntimeException(meta + ": no columns defined");
        }
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
            String vc1Name = toColumnName(vc1);
            String vc2Name = toColumnName(vc2);
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

    private String toColumnName(ViewColumn viewColumn) {
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

    private String toColumnSQL(Column col) {
        StringBuilder decl = new StringBuilder();
        decl.append(normalize(col.getName()) + " " + getDatatypeString(col));
        if (col.getDescription() != null && col.getDescription().length() > 0) {
            String comment = col.getDescription();
            comment = Keywords.escapeKeywords(dequote(comment));
            decl.append(" COMMENT '" + comment + "'");
        }
        return decl.toString();
    }

    private String toVariable(String column) {
        return "@" + column;
    }

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

    public static SQLFactory getSQLManager() {
        return sqlManager;
    }
}
