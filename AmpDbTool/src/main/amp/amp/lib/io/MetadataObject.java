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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class MetadataObject {

    public String id;
    public String title;
    public String scenario;

    public String classname;

    public String ampversion;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public TableSchema tableSchema;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public ViewSchema viewSchema;

    public MetadataObject() {
        super();
    }
    
    @Override
    public String toString() {
        return (id != null) ? id : super.toString();
    }

    public String getAmpversion() {
        return ampversion;
    }

    public String getClassname() {
        return classname;
    }

    public String getId() {
        return id;
    }

    public String getScenario() {
        return scenario;
    }

    public TableSchema getTableSchema() {
        return tableSchema;
    }

    public String getTitle() {
        return title;
    }

    public ViewSchema getViewSchema() {
        return viewSchema;
    }

    public void setAmpversion(String ampversion) {
        this.ampversion = ampversion;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public void setTableSchema(TableSchema tableSchema) {
        this.tableSchema = tableSchema;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setViewSchema(ViewSchema viewSchema) {
        this.viewSchema = viewSchema;
    }

    public static class Column {
        public String name;
        public String titles;
        public String description;
        public String datatype;
        public int maxLength;
        public boolean required;

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

        public String getTitles() {
            return titles;
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

        public void setTitles(String titles) {
            this.titles = titles;
        }
    }

    public static class ForeignKey {
        public String columnReference;
        public Reference reference;

        public String getColumnReference() {
            return columnReference;
        }

        public Reference getReference() {
            return reference;
        }

        public void setColumnReference(String columnReference) {
            this.columnReference = columnReference;
        }

        public void setReference(Reference reference) {
            this.reference = reference;
        }
    }

    public static class Join {
        public Reference column1;
        public Reference column2;
        public String operator;

        public Join() {

        }

        public Reference getColumn1() {
            return column1;
        }

        public Reference getColumn2() {
            return column2;
        }

        public String getOperator() {
            return operator == null ? "=" : operator;
        }

        public void setColumn1(Reference column1) {
            this.column1 = column1;
        }

        public void setColumn2(Reference column2) {
            this.column2 = column2;
        }

        public void setOperator(String operator) {
            this.operator = operator == null ? "=" : operator;
        }
    }

    public static class Reference {
        public String schemaReference;
        public String columnReference;

        public String getColumnReference() {
            return columnReference;
        }

        public String getSchemaReference() {
            return schemaReference;
        }

        public void setColumnReference(String columnReference) {
            this.columnReference = columnReference;
        }

        public void setSchemaReference(String schemaReference) {
            this.schemaReference = schemaReference;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TableSchema {

        public Column[] columns;
        public String[] primaryKey;
        public ForeignKey[] foreignKeys;

        public Column[] getColumns() {
            return columns;
        }

        public ForeignKey[] getForeignKeys() {
            return foreignKeys;
        }

        public String[] getPrimaryKey() {
            return primaryKey;
        }

        public void setColumns(Column[] columns) {
            this.columns = columns;
        }

        public void setForeignKeys(ForeignKey[] foreignKeys) {
            this.foreignKeys = foreignKeys;
        }

        public void setPrimaryKey(String[] primaryKey) {
            this.primaryKey = primaryKey;
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ViewSchema {
        private Reference[] references;
        private Join[] joins;

        public ViewSchema() {

        }

        public Join[] getJoins() {
            return joins;
        }

        public Reference[] getReferences() {
            return references;
        }

        public void setJoins(Join[] joins) {
            this.joins = joins;
        }

        public void setReferences(Reference[] references) {
            this.references = references;
        }
    }

}
