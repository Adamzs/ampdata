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

import amp.lib.io.MetadataObject;
import amp.lib.io.MetadataObject.Join;
import amp.lib.io.MetadataObject.Reference;
import amp.lib.io.MetadataObject.ViewSchema;
import amp.lib.io.meta.MetaTable.Column;

/**
 * Represents a database view created from the metadata.
 */
public class MetaView extends MetaObject {
    private List<ViewColumn> columns = new ArrayList<>();

    private List<ViewJoin> joins = new ArrayList<>();

    /**
     * Instantiates a new meta view.
     *
     * @param metaObject the meta object
     */
    public MetaView(MetadataObject metaObject) {
        super(metaObject);
        ViewSchema viewSchema = metaObject.getViewSchema();
        for (Reference ref : viewSchema.getReferences()) {
            columns.add(new ViewColumn(ref));
        }
        for (Join join : viewSchema.getJoins()) {
            joins.add(new ViewJoin(join));
        }
    }

    /**
     * Gets the columns.
     *
     * @return the columns
     */
    public List<ViewColumn> getColumns() {
        return columns;
    }

    /**
     * Gets the joins.
     *
     * @return the joins
     */
    public List<ViewJoin> getJoins() {
        return joins;
    }

    /**
     * Sets the columns.
     *
     * @param columns the new columns
     */
    public void setColumns(List<ViewColumn> columns) {
        this.columns = columns;
    }

    /**
     * Sets the joins.
     *
     * @param joins the new joins
     */
    public void setJoins(List<ViewJoin> joins) {
        this.joins = joins;
    }

    /**
     * Represents a column in a view.
     */
    public class ViewColumn {
        private String tableName;
        private String columnName;
        private Column refColumn;

        /**
         * Instantiates a new view column.
         *
         * @param ref the ref
         */
        public ViewColumn(Reference ref) {
            this.tableName = ref.getSchemaReference();
            this.columnName = ref.getColumnReference();
        }

        /**
         * Gets the ref column.
         *
         * @return the ref column
         */
        public Column getRefColumn() {
            if (refColumn == null) {
                MetadataFactory mdm = MetadataFactory.getMetadataFactory();
                MetaObject mo = mdm.getMetadataByIdentifier(tableName);
                if (mo == null) {
                    throw new RuntimeException("view reference " + tableName + " does not exist");
                }
                if (!(mo instanceof MetaTable)) {
                    throw new RuntimeException("view reference " + tableName + " is not a table");
                }
                if (!columnName.equals("*")) {
                    MetaTable mt = (MetaTable) mo;
                    refColumn = mt.getColumnByName(columnName);
                    if (refColumn == null) {
                        throw new RuntimeException("view reference " + tableName + "(" + columnName + ") does not exist");
                    }
                }
            }
            return refColumn;
        }

        /**
         * Checks if is all columns.
         *
         * @return true, if is all columns
         */
        public boolean isAllColumns() {
            return columnName.equals("*");
        }

        @Override
        public String toString() {
            return tableName + "." + columnName;
        }
    }

    /**
     * Represents a join clause in a view definition.
     */
    public class ViewJoin {
        private ViewColumn column1;
        private ViewColumn column2;
        private String operator;

        /**
         * Instantiates a new view join.
         *
         * @param join the join
         */
        public ViewJoin(Join join) {
            this.column1 = new ViewColumn(join.getColumn1());
            this.column2 = new ViewColumn(join.getColumn2());
            this.operator = join.getOperator();
        }

        /**
         * Gets the column 1.
         *
         * @return the column 1
         */
        public ViewColumn getColumn1() {
            return column1;
        }

        /**
         * Gets the column 2.
         *
         * @return the column 2
         */
        public ViewColumn getColumn2() {
            return column2;
        }

        /**
         * Gets the operator.
         *
         * @return the operator
         */
        public String getOperator() {
            return operator;
        }

        /**
         * Sets the column 1.
         *
         * @param column1 the new column 1
         */
        public void setColumn1(ViewColumn column1) {
            this.column1 = column1;
        }

        /**
         * Sets the column 2.
         *
         * @param column2 the new column 2
         */
        public void setColumn2(ViewColumn column2) {
            this.column2 = column2;
        }

        /**
         * Sets the operator.
         *
         * @param operator the new operator
         */
        public void setOperator(String operator) {
            this.operator = operator;
        }

        @Override
        public String toString() {
            return "JOIN( " + column1 + operator + column2 + ")";
        }
    }

}
