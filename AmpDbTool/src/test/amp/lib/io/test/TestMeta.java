package lib.io.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import amp.lib.io.errors.ErrorEvent;
import amp.lib.io.errors.ErrorListener;
import amp.lib.io.errors.Report;
import amp.lib.io.meta.MetaTable;
import amp.lib.io.meta.MetaTable.ForeignKey;
import amp.lib.io.meta.MetaView;
import amp.lib.io.meta.MetaView.ViewColumn;
import amp.lib.io.meta.MetaView.ViewJoin;
import amp.lib.io.meta.MetadataFactory;

public class TestMeta implements ErrorListener {

    @Override
    public void errorOccurred(ErrorEvent event) {
        System.err.println(event);

    }

    @Test
    public void test() {
        int failures = 0;
        MetadataFactory mdm = MetadataFactory.getMetadataFactory();
        Report.addListener(this);

        mdm.readMetaFiles(new File("data"));
        for (MetaTable mt : mdm.getAllMetaTables()) {
            try {
                assertNotNull(mt.getIdentifier());
                assertNotNull(mt.getFile());
                assertNotNull(mt.getTitle());
                assertTrue(mt.getIdentifier() + ": no columns", mt.getAllColumns().size() > 0);
                for (ForeignKey fk : mt.getForeignKeys()) {
                    assertNotNull(fk + ": no primary key reference", fk.getReferenceColumn());
                }
                System.out.println(mt.dump());
            } catch (Exception e) {
                System.err.println(mt + ": " + e.getMessage());
                failures++;
            }
        }
        for (MetaView mv : mdm.getAllMetaViews()) {
            try {
                assertNotNull(mv.getIdentifier());
                assertNotNull(mv.getFile());
                assertNotNull(mv.getTitle());
                assertTrue(mv.getIdentifier() + ": no references", mv.getColumns().size() > 0);
                for (ViewColumn vc : mv.getColumns()) {
                    if (!vc.isAllColumns()) {
                        assertNotNull(vc + ": no reference column", vc.getRefColumn());
                    }
                }
                for (ViewJoin vj : mv.getJoins()) {
                    ViewColumn vc1 = vj.getColumn1();
                    ViewColumn vc2 = vj.getColumn2();
                    String op = vj.getOperator();
                    assertNotNull("Column 1", vc1);
                    assertNotNull("Column 2", vc2);
                    assertNotNull("Operator", op);
                    assertNotNull("Column 1 reference", vc1.getRefColumn());
                    assertNotNull("Column 2 reference", vc1.getRefColumn());
                    String ops = "= < > != <= >= ";
                    assertTrue("Operator not in " + ops, ops.contains(op + " "));
                }
                System.out.println(mv.dump());
            } catch (Exception e) {
                System.err.println(mv + ": " + e.getMessage());
                failures++;
            }
        }
        assertTrue(failures + " failures", failures == 17);
    }

}
