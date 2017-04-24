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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents metadata about an analysis table to be created or populated.
 * Information includes: - Ordinary columns, with datatype and other decorators
 * - Primary key column - Foreign key columns
 * 
 * @author brucejtaylor333
 *
 */
public class Metadata {

    private static Map<String, Metadata> metadataMap = new HashMap<>();

    private Metafile file = null;

    public String tableName;
    private List<Column> columns = new ArrayList<>();

    private PrimaryKeyColumn primaryKey;

    private List<ForeignKeyColumn> foreignKeys = new ArrayList<>();
    public String title = "";
    private String identifier = "";

    private Metadata() {
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append(getIdentifier());
        sb.append("[");
        sb.append("Table=" + getTableName() + ",");
        sb.append("Title=" + getTitle() + ",");
        sb.append("Columns={");
        for (Column c : getColumns()) {
            sb.append("(" + c.dump() + "),");
        }
        sb.append("}");
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Metadata))
            return false;
        return getIdentifier().equals(((Metadata) obj).getIdentifier());
    }

    public Column getColumnByName(String columnReference) {
        for (Column col : getColumns()) {
            if (col.getName() != null && col.getName().equalsIgnoreCase(columnReference)) {
                return col;
            }
        }
        return null;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public Metafile getFile() {
        return file;
    }

    public List<ForeignKeyColumn> getForeignKeys() {
        return foreignKeys;
    }

    public String getIdentifier() {
        return identifier;
    }

    public PrimaryKeyColumn getPrimaryKey() {
        return primaryKey;
    }

    public String getTableName() {
        return tableName;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
    }

    public void setFile(Metafile metafile) {
        this.file = metafile;

    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;

    }

    public void setPrimaryKey(PrimaryKeyColumn primaryKey) {
        this.primaryKey = primaryKey;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return getIdentifier();
    }

    public static List<Metadata> getAllMetadata() {
        List<Metadata> metaList = new ArrayList<>();
        metaList.addAll(metadataMap.values());
        return metaList;
    }

    public static Metadata getMetadata(String identifier) {
        if (!metadataMap.containsKey(identifier)) {
            Metadata m = new Metadata();
            m.setIdentifier(identifier);
            metadataMap.put(identifier, m);
        }
        return metadataMap.get(identifier);
    }

    public static class Column {
        private Metadata table;
        public String name;
        public String titles;
        public String description;
        public String datatype;
        public int maxLength;
        public boolean required;

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

        public Metadata getTable() {
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

        public void setTable(Metadata table) {
            this.table = table;
        }

        public void setTitles(String titles) {
            this.titles = titles;
        }

        @Override
        public String toString() {
            return table.toString() + "." + getName();
        }
    }

    public static class ForeignKeyColumn extends Column {

        private String pkIdentifier;

        private String pkColumnName;
        public PrimaryKeyColumn pkColumn;

        public ForeignKeyColumn(String pkIdentifier, String pkColumnName) {
            this.pkIdentifier = pkIdentifier;
            this.pkColumnName = pkColumnName;
        }

        @Override
        public String dump() {
            String fkTable = null;
            String fkName = null;
            try {
                PrimaryKeyColumn c = this.getColumnReference();
                fkName = c.getName();
                fkTable = c.getTable().getTableName();
            } catch (Exception e) {
                fkName = "<unknown>";
            }
            return super.dump() + ",FOREIGN=" + fkTable + "." + fkName;
        }

        public PrimaryKeyColumn getColumnReference() {
            if (pkColumn == null) {
                Metadata pkTable = Metadata.getMetadata(pkIdentifier);
                Column c = pkTable.getColumnByName(pkColumnName);
                if (c == null) {
                    throw new RuntimeException(pkTable + "." + pkColumnName + " is not a defined column");
                } else if (!(c instanceof PrimaryKeyColumn)) {
                    throw new RuntimeException(pkTable + "." + pkColumnName + " is not a primary key column");
                }
                pkColumn = (PrimaryKeyColumn) c;
            }
            return pkColumn;
        }

        public void setColumnReference(PrimaryKeyColumn columnReference) {
            this.pkColumn = columnReference;
        }
    }

    public static class PrimaryKeyColumn extends Column {

        @Override
        public String dump() {
            return super.dump() + ",PRIMARY";
        }

    }

}
