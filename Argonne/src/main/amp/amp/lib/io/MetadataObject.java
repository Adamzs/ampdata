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

package amp.lib.io;

public class MetadataObject {

    public static class TableSchema {
        public Column[] columns;
        public String primaryKey;
        public ForeignKey[] foreignKeys;
        public Column[] getColumns() {
            return columns;
        }
        public void setColumns(Column[] columns) {
            this.columns = columns;
        }
        public String getPrimaryKey() {
            return primaryKey;
        }
        public void setPrimaryKey(String primaryKey) {
            this.primaryKey = primaryKey;
        }
        public ForeignKey[] getForeignKeys() {
            return foreignKeys;
        }
        public void setForeignKeys(ForeignKey[] foreignKeys) {
            this.foreignKeys = foreignKeys;
        }
        
    }
    
    public static class Column {
        public String name;
        public String titles;
        public String description;
        public String datatype;
        public int maxLength;
        public boolean required;
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getTitles() {
            return titles;
        }
        public void setTitles(String titles) {
            this.titles = titles;
        }
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
        public String getDatatype() {
            return datatype;
        }
        public void setDatatype(String datatype) {
            this.datatype = datatype;
        }
        public int getMaxLength() {
            return maxLength;
        }
        public void setMaxLength(int maxLength) {
            this.maxLength = maxLength;
        }
        public boolean isRequired() {
            return required;
        }
        public void setRequired(boolean required) {
            this.required = required;
        }
    }
    
    public static class ForeignKey {
        public String columnReference;
        public Reference reference;
        public String getColumnReference() {
            return columnReference;
        }
        public void setColumnReference(String columnReference) {
            this.columnReference = columnReference;
        }
        public Reference getReference() {
            return reference;
        }
        public void setReference(Reference reference) {
            this.reference = reference;
        }
    }
    
    public static class Reference {
        public String schemaReference;
        public String columnReference;
        public String getSchemaReference() {
            return schemaReference;
        }
        public void setSchemaReference(String schemaReference) {
            this.schemaReference = schemaReference;
        }
        public String getColumnReference() {
            return columnReference;
        }
        public void setColumnReference(String columnReference) {
            this.columnReference = columnReference;
        }
    }
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getScenario() {
        return scenario;
    }
    public void setScenario(String scenario) {
        this.scenario = scenario;
    }
    public String getClassname() {
        return classname;
    }
    public void setClassname(String classname) {
        this.classname = classname;
    }
    public String getAmpversion() {
        return ampversion;
    }
    public void setAmpversion(String ampversion) {
        this.ampversion = ampversion;
    }
    public TableSchema getTableSchema() {
        return tableSchema;
    }
    public void setTableSchema(TableSchema tableSchema) {
        this.tableSchema = tableSchema;
    }
    public String id;
    public String title;
    public String scenario;
    public String classname;
    public String ampversion;
    public TableSchema tableSchema;



    
}
