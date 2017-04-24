package lib.io.test;

import java.io.File;
import java.util.List;

import org.junit.Test;

import amp.lib.io.db.DBManager;
import amp.lib.io.db.SQLManager;
import amp.lib.io.errors.ErrorEvent;
import amp.lib.io.errors.ErrorEvent.Severity;
import amp.lib.io.errors.ErrorEventListener;
import amp.lib.io.meta.Metadata;
import amp.lib.io.meta.MetafileManager;
import amp.lib.io.populate.PopulateManager;

public class TestPopulate implements ErrorEventListener {

    @Override
    public void errorOccurred(ErrorEvent event) {
        if (event.getSeverity() == Severity.ERROR) {
            System.err.println(event.getInfo());
        } else {
            System.out.println(event.getInfo());
        }
    }

    @Test
    public void test() {
        DBManager dbm = DBManager.getDbManager();
        dbm.addErrorEventListener(this);
        MetafileManager mfh = MetafileManager.getMetafileHandler();
        mfh.addErrorEventListener(this);
        SQLManager sqlh = SQLManager.getSQLManager();
        sqlh.addErrorEventListener(this);
        PopulateManager pph = PopulateManager.getPopulateManager();
        pph.addErrorEventListener(this);

        dbm.openDatabase("Amp", "root", "root");
        mfh.readMetaFiles(new File("data"));
        List<Metadata> metadata = Metadata.getAllMetadata();
        pph.populateData(dbm.getDatabase(), metadata);
    }

}
