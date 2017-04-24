package lib.io.test;

import java.io.File;

import org.junit.Test;

import amp.lib.io.db.DBManager;
import amp.lib.io.db.SQLManager;
import amp.lib.io.errors.ErrorEvent;
import amp.lib.io.errors.ErrorEvent.Severity;
import amp.lib.io.errors.ErrorEventListener;
import amp.lib.io.meta.Metadata;
import amp.lib.io.meta.MetafileManager;

public class TestSql implements ErrorEventListener {

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
        mfh.readMetaFiles(new File("data"));
        dbm.openDatabase("Amp", "root", "root");
        dbm.buildSchema(Metadata.getAllMetadata());
    }
}
