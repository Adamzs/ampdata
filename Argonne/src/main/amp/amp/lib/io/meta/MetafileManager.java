package amp.lib.io.meta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import amp.lib.io.errors.ErrorEvent;
import amp.lib.io.errors.ErrorEventListener;
import amp.lib.io.errors.ErrorReporter;

public class MetafileManager implements ErrorReporter, ErrorEventListener {

    private static MetafileManager handler = new MetafileManager();

    private List<ErrorEventListener> listeners = new ArrayList<>();

    private MetafileManager() {
    }

    @Override
    public void addErrorEventListener(ErrorEventListener listener) {
        listeners.add(listener);

    }

    @Override
    public void errorOccurred(ErrorEvent event) {
        this.reportError(event);
    }

    public List<Metafile> readMetaFiles(File dir) {
        List<Metafile> metas = new ArrayList<>();
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                metas.addAll(readMetaFiles(f));
            } else if (isMeta(f)) {
                try {
                    Metafile mf = new Metafile(f);
                    mf.addErrorEventListener(this);
                    mf.parseMetaFile();
                    metas.add(mf);
                } catch (Exception e) {
                    reportError(new ErrorEvent(this, e.getMessage()));
                }
            }
        }
        return metas;
    }

    @Override
    public void reportError(ErrorEvent event) {
        for (ErrorEventListener l : listeners) {
            l.errorOccurred(event);
        }
    }

    private boolean isMeta(File f) {
        return f.getName().endsWith(".meta");
    }

    public static MetafileManager getMetafileHandler() {
        return handler;
    }

}
