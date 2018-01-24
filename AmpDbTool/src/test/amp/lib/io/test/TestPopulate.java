package lib.io.test;

import java.io.File;
import java.util.List;

import org.junit.Test;

import amp.lib.io.db.Database;
import amp.lib.io.errors.ErrorEvent;
import amp.lib.io.errors.ErrorEvent.Severity;
import amp.lib.io.errors.ErrorListener;
import amp.lib.io.errors.Report;
import amp.lib.io.meta.MetaObject;
import amp.lib.io.meta.MetadataFactory;

public class TestPopulate implements ErrorListener {

    @Override
    public void errorOccurred(ErrorEvent event) {
        System.out.println(event);
    }

    @Test
    public void test() {
        Database dbm = Database.getDatabase();
        Report.addListener(this);
        MetadataFactory mfh = MetadataFactory.getMetadataFactory();
        dbm.openDatabase("Amp", "root", "root");
        mfh.readMetaFiles(new File("data"));
        List<MetaObject> metadata = mfh.getAllMetadata();
        dbm.populateTables(metadata, "TestScenario");
    }

}
