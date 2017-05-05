package lib.io.test;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;

import amp.lib.io.db.Database;
import amp.lib.io.db.SQLFactory;
import amp.lib.io.errors.ErrorEvent;
import amp.lib.io.errors.ErrorEvent.Severity;
import amp.lib.io.errors.ErrorListener;
import amp.lib.io.errors.Report;
import amp.lib.io.meta.MetadataFactory;

public class TestSchema implements ErrorListener {

    static Database dbm;
    static MetadataFactory mfh;
    static SQLFactory sqlh;
    static boolean initialized = false;

    @Override
    public void errorOccurred(ErrorEvent event) {
        if (event.getSeverity() == Severity.ERROR) {
            System.err.println(event + "\n--");
        } else {
            System.out.println(event.getMessage());
        }
    }

    public void initialize() {
        if (!initialized) {
            Report.addListener(this);
            dbm = Database.getDatabase();
            mfh = MetadataFactory.getMetadataFactory();
            sqlh = SQLFactory.getSQLFactory();
            initialized = true;
        }
    }

    @Test
    public void testSchema() {
        initialize();
        mfh.readMetaFiles(new File("data"));
        dbm.dropDatabase("Amp");
        dbm.createDatabase("Amp", "root", "root");
        dbm.buildSchema(mfh.getAllMetadata());
    }

    @Test
    public void testSQL() {
        initialize();
        dbm.openDatabase("Amp", "root", "root");
        dbm.buildSQL(Arrays.asList(new File[] { new File("sql") }));
    }
}
