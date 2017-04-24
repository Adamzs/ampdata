package amp.lib.io.populate;

import java.util.ArrayList;
import java.util.List;

import amp.lib.io.db.Database;
import amp.lib.io.db.SQLManager;
import amp.lib.io.errors.ErrorEvent;
import amp.lib.io.errors.ErrorEventListener;
import amp.lib.io.errors.ErrorReporter;
import amp.lib.io.meta.Metadata;

public class PopulateManager implements ErrorReporter {
    private static PopulateManager populateManager = new PopulateManager();

    private List<ErrorEventListener> listeners = new ArrayList<>();

    private PopulateManager() {
    }

    @Override
    public void addErrorEventListener(ErrorEventListener listener) {
        listeners.add(listener);
    }

    public void populateData(Database db, List<Metadata> metadata) {
        SQLManager sqlm = SQLManager.getSQLManager();
        String refOff = sqlm.toReferentialIntegrity(false);
        db.execute(refOff);
        for (Metadata meta : metadata) {
            importCSVFile(db, meta);
        }
        String refOn = sqlm.toReferentialIntegrity(true);
        db.execute(refOn);
    }

    @Override
    public void reportError(ErrorEvent event) {
        for (ErrorEventListener l : listeners) {
            l.errorOccurred(event);
        }
    }

    private void importCSVFile(Database db, Metadata meta) {
        SQLManager sqlm = SQLManager.getSQLManager();
        try {
            String clearSql = sqlm.toDeleteAllSql(meta);
            reportError(new ErrorEvent(this, clearSql, ErrorEvent.Severity.INFO));
            db.execute(clearSql);
            String loadSql = sqlm.toLoadSql(meta);
            reportError(new ErrorEvent(this, loadSql, ErrorEvent.Severity.INFO));
            db.execute(loadSql);
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && message.contains("underlying")) {
                message = message.replaceAll("\n", " ");
                message = message.replaceAll(":.*MESSAGE:", ":");
                message = message.replaceAll("STACKTRACE.*\\z", "");
            }
            reportError(new ErrorEvent(this, message));
        }
    }

    public static PopulateManager getPopulateManager() {
        return populateManager;
    }
}
