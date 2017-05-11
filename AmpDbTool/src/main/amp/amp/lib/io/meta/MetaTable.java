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
import amp.lib.io.errors.Report;

/**
 * All the metadata required to create and populate a database table.
 */
public class MetaTable extends MetaObject {

    private String tableName = "<unknown>";
    private List<Column> allColumns = new ArrayList<>();
    private PrimaryKey primaryKey = null;
    private List<ForeignKey> foreignKeys = new ArrayList<>();

    /**
     * Instantiates a new meta table.
     *
     * @param metaObject the meta object
     */
    public MetaTable(MetadataObject metaObject) {
        super(metaObject);
        createMetadata(metaObject);
    }

    /**
     * Gets all the columns.
     *
     * @return the all columns
     */
    public List<Column> getAllColumns() {
        return allColumns;
    }

    /**
     * Gets the column by name.
     *
     * @param columnName the column reference
     * @return the column by name
     */
    public Column getColumnByName(String columnName) {
        for (Column col : getAllColumns()) {
            if (col.getName() != null && col.getName().equalsIgnoreCase(columnName)) {
                return col;
            }
        }
        return null;
    }

    /**
     * Gets the foreign keys.
     *
     * @return the foreign keys
     */
    public List<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    /**
     * Gets the primary key.
     *
     * @return the primary key
     */
    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    /**
     * Gets the table name.
     *
     * @return the table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Sets the foreign keys.
     *
     * @param foreignKeys the new foreign keys
     */
    public void setForeignKeys(List<ForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    /**
     * Sets the primary key.
     *
     * @param primaryKey the new primary key
     */
    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    /**
     * Sets the table name.
     *
     * @param tableName the new table name
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return getIdentifier();
    }

    @Override
    public String dump() {
        //TODO
        return super.dump();
    }

    /*
     * Adds a new column to the list
     */
    private void addColumn(Column metaCol) {
        allColumns.add(metaCol);
    }

    /*
     * Adds a new foreign key to the list
     */
    private void addForeignKey(ForeignKey fk) {
        foreignKeys.add(fk);

    }

    /*
     * Builds all the metadata from the parsed MetadataObject.
     */
    private void createMetadata(MetadataObject metadata) {
        setTableName(metadata.getTitle());
        MetadataObject.Column[] columnList = metadata.getTableSchema().getColumns();
        for (MetadataObject.Column metaCol : columnList) {
            Column col = new Column(this, metaCol);
            if (getAllColumns().contains(col)) {
                setValid(false);
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

    /**
     * Represents a database table column.
     */
    public static class Column {
        private MetaTable table;
        private String name;
        private String titles;
        private String description;
        private String datatype;
        private int maxLength;
        private boolean required;

        /**
         * Instantiates a new column.
         *
         * @param table the table
         * @param metaCol the meta col
         */
        public Column(MetaTable table, MetadataObject.Column metaCol) {
            this.table = table;
            setName(metaCol.getName());
            setDatatype(metaCol.getDatatype());
            setDescription(metaCol.getDescription());
            setMaxLength(metaCol.getMaxLength());
            setTitles(metaCol.getTitles());
            setRequired(metaCol.isRequired());
        }

        /**
         * The internal data of the column
         *
         * @return the string
         */
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

        /**
         * Gets the datatype.
         *
         * @return the datatype
         */
        public String getDatatype() {
            return datatype;
        }

        /**
         * Gets the description.
         *
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Gets the max length.
         *
         * @return the max length
         */
        public int getMaxLength() {
            return maxLength;
        }

        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the table.
         *
         * @return the table
         */
        public MetaTable getTable() {
            return table;
        }

        /**
         * Gets the titles.
         *
         * @return the titles
         */
        public String getTitles() {
            return titles;
        }

        @Override
        public int hashCode() {
            return table.hashCode() + getName().hashCode();
        }

        /**
         * Checks if is required.
         *
         * @return true, if is required
         */
        public boolean isRequired() {
            return required;
        }

        /**
         * Sets the datatype.
         *
         * @param datatype the new datatype
         */
        public void setDatatype(String datatype) {
            this.datatype = datatype;
        }

        /**
         * Sets the description.
         *
         * @param description the new description
         */
        public void setDescription(String description) {
            this.description = description;
        }

        /**
         * Sets the max length.
         *
         * @param maxLength the new max length
         */
        public void setMaxLength(int maxLength) {
            this.maxLength = maxLength;
        }

        /**
         * Sets the name.
         *
         * @param name the new name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Sets the required.
         *
         * @param required the new required
         */
        public void setRequired(boolean required) {
            this.required = required;
        }

        /**
         * Sets the table.
         *
         * @param table the new table
         */
        public void setTable(MetaTable table) {
            this.table = table;
        }

        /**
         * Sets the titles.
         *
         * @param titles the new titles
         */
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

    /**
     * Represents a database foreign key.
     */
    public static class ForeignKey {

        private String fkTableName;
        private MetaTable fkTable;

        private String fkColumnName;
        private Column fkColumn;

        private String refTableName;
        private MetaTable refTable;

        private String refColumnName;
        private Column refColumn;


        /**
         * Gets the host table.
         *
         * @return the foreign key table
         */
        public MetaTable getFkTable() {
            return fkTable;
        }

        /**
         * Instantiates a new foreign key.
         *
         * @param table the table
         * @param metaColumn the meta column
         */
        public ForeignKey(MetaTable table, amp.lib.io.MetadataObject.ForeignKey metaColumn) {
            this.fkTable = table;
            this.setFkTableName(table.getTableName());
            this.fkColumnName = metaColumn.getColumnReference();
            this.fkColumn = table.getColumnByName(fkColumnName);
            this.refTableName = metaColumn.getReference().getSchemaReference();
            this.refColumnName = metaColumn.getReference().getColumnReference();
        }

        /**
         * Gets the foreign key column.
         *
         * @return the foreign key column
         */
        public Column getFkColumn() {
            return fkColumn;
        }

        /**
         * Gets the foreign key table name.
         *
         * @return the foreign key table name
         */
        public String getFkTableName() {
            return fkTableName;
        }

        /**
         * Gets the table of the reference
         * @return the reference table
         */
        public MetaTable getReferenceTable() {
            return getReferenceColumn().getTable();
        }

        /**
         * Gets the reference column.
         *
         * @return the reference column
         */
        public Column getReferenceColumn() {
            if (refColumn == null) {
                MetaObject mo = MetadataFactory.getMetadataFactory().getMetadataByIdentifier(this.refTableName);
                if (mo == null) {
                    Report.error(getFkTable(), "foreign key reference table " + refTableName + " is not defined");
                }
                else if (!(mo instanceof MetaTable)) {
                    Report.error(getFkTable(), "foreign key reference table " + refTableName + " is not to a table");
                }
                else {
                    refTable = (MetaTable) mo;
                    refColumn = refTable.getColumnByName(this.refColumnName);
                }
            }
            return refColumn;
        }

        /**
         * Sets the foreign key table name.
         *
         * @param fkTableName the new foreign key table name
         */
        public void setFkTableName(String fkTableName) {
            this.fkTableName = fkTableName;
        }

        /**
         * Sets the reference column.
         *
         * @param refColumn the new reference column
         */
        public void setReferenceColumn(Column refColumn) {
            this.refColumn = refColumn;
            this.refTableName = refColumn.getTable().getTableName();
            this.refColumnName = refColumn.getName();
        }

        /**
         * Sets the reference column.
         *
         * @param refTableName the ref table name
         * @param refColumnName the ref column name
         */
        public void setReferenceColumn(String refTableName, String refColumnName) {
            this.refTableName = refTableName;
            this.refColumnName = refColumnName;
        }

        @Override
        public String toString() {
            return fkColumn + " REFERENCES (" + refColumn + ")";
        }
    }

    /**
     * Represents a database primary key.
     */
    public static class PrimaryKey {
        private MetaTable table;
        private List<Column> pkColumns = new ArrayList<>();

        /**
         * Instantiates a new primary key.
         *
         * @param table the table
         * @param meta the meta
         */
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

        @Override
        public String toString() {
            return "(" + Joiner.on(",").join(pkColumns) + ")";
        }

        /**
         * Gets the primary key columns.
         *
         * @return the primary key columns
         */
        public List<Column> getPrimaryKeyColumns() {
            return pkColumns;
        }

        /**
         * Gets the table.
         *
         * @return the table
         */
        public MetaTable getTable() {
            return table;
        }

    }

}
