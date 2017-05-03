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

package amp.lib.io.meta;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

import amp.lib.io.MetadataObject;

public class MetaTable extends MetaObject {

    private String tableName = "<unknown>";
    private List<Column> allColumns = new ArrayList<>();
    private PrimaryKey primaryKey = null;
    private List<ForeignKey> foreignKeys = new ArrayList<>();

    public MetaTable(MetadataObject metaObject) {
        super(metaObject);
        createMetadata(metaObject);
    }

    public List<Column> getAllColumns() {
        return allColumns;
    }

    public Column getColumnByName(String columnReference) {
        for (Column col : getAllColumns()) {
            if (col.getName() != null && col.getName().equalsIgnoreCase(columnReference)) {
                return col;
            }
        }
        return null;
    }

    public List<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public String getTableName() {
        return tableName;
    }

    public void setForeignKeys(List<ForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return getIdentifier();
    }

    private void addColumn(Column metaCol) {
        allColumns.add(metaCol);
    }

    private void addForeignKey(ForeignKey fk) {
        foreignKeys.add(fk);

    }

    private void createMetadata(MetadataObject metadata) {
        setTableName(metadata.getTitle());
        MetadataObject.Column[] columnList = metadata.getTableSchema().getColumns();
        for (MetadataObject.Column metaCol : columnList) {
            Column col = new Column(this, metaCol);
            if (getAllColumns().contains(col)) {
                throw new MetaException(metadata, "duplicate column " + col);
            }
            addColumn(col);
        }

        String[] pkColumnNames = metadata.getTableSchema().getPrimaryKey();
        if (pkColumnNames != null && pkColumnNames.length > 0) {
            for (String pk : pkColumnNames) {
                if (pk == null || pk.trim().isEmpty()) {
                    throw new MetaException(metadata, "primary key column is null");
                }
                Column col = getColumnByName(pk);
                if (col == null) {
                    throw new MetaException(metadata, "primary key column " + pk + " is not defined");
                }
            }
            setPrimaryKey(new PrimaryKey(this, metadata));
        }

        amp.lib.io.MetadataObject.ForeignKey[] foreignKeys = metadata.getTableSchema().getForeignKeys();
        if (foreignKeys != null && foreignKeys.length > 0) {
            for (amp.lib.io.MetadataObject.ForeignKey fk : foreignKeys) {
                String ref = fk.getColumnReference();
                if (ref == null || ref.trim().isEmpty()) {
                    throw new MetaException(metadata, "foreign key column is null");
                }
                Column col = getColumnByName(ref);
                if (col == null) {
                    throw new MetaException(metadata, "foreign key column " + ref + " is not defined");
                }
                ForeignKey fkCol = new ForeignKey(this, fk);
                addForeignKey(fkCol);
            }
        }

    }

    public static class Column {
        public MetaTable table;
        public String name;
        public String titles;
        public String description;
        public String datatype;
        public int maxLength;
        public boolean required;

        public Column(MetaTable table, MetadataObject.Column metaCol) {
            this.table = table;
            setName(metaCol.getName());
            setDatatype(metaCol.getDatatype());
            setDescription(metaCol.getDescription());
            setMaxLength(metaCol.getMaxLength());
            setTitles(metaCol.getTitles());
            setRequired(metaCol.isRequired());
        }

        protected Column() {

        }

        public String dump() {
            StringBuilder sb = new StringBuilder();
            sb.append(this.getName());
            sb.append("," + this.getDatatype());
            sb.append(this.getMaxLength() == 0 ? "" : ("," + this.getMaxLength()));
            sb.append(this.isRequired() ? ",REQUIRED" : "");
            return sb.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (!(obj instanceof Column))
                return false;
            Column that = (Column) obj;
            return this.getName().equalsIgnoreCase(that.getName());
        }

        public String getDatatype() {
            return datatype;
        }

        public String getDescription() {
            return description;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public String getName() {
            return name;
        }

        public MetaTable getTable() {
            return table;
        }

        public String getTitles() {
            return titles;
        }

        @Override
        public int hashCode() {
            return table.hashCode() + getName().hashCode();
        }

        public boolean isRequired() {
            return required;
        }

        public void setDatatype(String datatype) {
            this.datatype = datatype;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setMaxLength(int maxLength) {
            this.maxLength = maxLength;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public void setTable(MetaTable table) {
            this.table = table;
        }

        public void setTitles(String titles) {
            this.titles = titles;
        }

        @Override
        public String toString() {
            String tableName = getTable() == null ? "<unknown>" : getTable().getTableName();
            String columnName = getName() == null ? "<unknown>" : getName();
            return tableName + "(" + columnName + ")";
        }
    }

    public static class ForeignKey {

        private String fkTableName;
        private MetaTable fkTable;

        private String fkColumnName;
        private Column fkColumn;

        private String refTableName;
        private MetaTable refTable;

        private String refColumnName;
        private Column refColumn;


        public MetaTable getFkTable() {
            return fkTable;
        }

        public ForeignKey(MetaTable table, amp.lib.io.MetadataObject.ForeignKey metaColumn) {
            this.fkTable = table;
            this.setFkTableName(table.getTableName());
            this.fkColumnName = metaColumn.getColumnReference();
            this.fkColumn = table.getColumnByName(fkColumnName);
            this.refTableName = metaColumn.getReference().getSchemaReference();
            this.refColumnName = metaColumn.getReference().getColumnReference();
        }

        public Column getFkColumn() {
            return fkColumn;
        }

        public String getFkTableName() {
            return fkTableName;
        }

        public Column getPkColumn() {
            return refColumn;
        }

        public Column getReferenceColumn() {
            if (refColumn == null) {
                MetaObject mo = MetadataFactory.getMetadataFactory().getMetadataByName(this.refTableName);
                if (mo == null) {
                    throw new MetaException(getFkTable(), "foreign key reference table " + refTableName + " is not defined");
                }
                if (!(mo instanceof MetaTable)) {
                    throw new MetaException(getFkTable(), "foreign key reference table " + refTableName + " is not to a table");
                }
                refTable = (MetaTable) mo;
                refColumn = refTable.getColumnByName(this.refColumnName);
                if (refColumn == null) {
                    throw new MetaException(getFkTable(), "foreign key reference column " + refColumnName + " is not defined");
                }
            }
            return refColumn;
        }

        public void setFkTableName(String fkTableName) {
            this.fkTableName = fkTableName;
        }

        public void setReferenceColumn(Column refColumn) {
            this.refColumn = refColumn;
            this.refTableName = refColumn.getTable().getTableName();
            this.refColumnName = refColumn.getName();
        }

        public void setReferenceColumn(String refTableName, String refColumnName) {
            this.refTableName = refTableName;
            this.refColumnName = refColumnName;
        }

        @Override
        public String toString() {
            return fkColumn + " REFERENCES (" + refColumn + ")";
        }
    }

    public static class PrimaryKey {
        MetaTable table;
        List<Column> pkColumns = new ArrayList<>();

        public PrimaryKey(MetaTable table, amp.lib.io.MetadataObject meta) {
            this.table = table;
            for (String pkColumnName : meta.getTableSchema().getPrimaryKey()) {
                if (pkColumnName != null && !pkColumnName.isEmpty()) {
                    Column pkColumn = table.getColumnByName(pkColumnName);
                    if (pkColumn == null) {
                        throw new MetaException(getTable(), "primary key column " + pkColumnName + " does not exist");
                    }
                    pkColumns.add(pkColumn);
                }
            }
        }

        public String toString() {
            return "(" + Joiner.on(",").join(pkColumns) + ")";
        }

        public List<Column> getPrimaryKeyColumns() {
            return pkColumns;
        }

        public MetaTable getTable() {
            return table;
        }

    }

}
