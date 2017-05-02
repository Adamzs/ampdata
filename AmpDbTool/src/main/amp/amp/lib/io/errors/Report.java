package amp.lib.io.errors;

import java.util.ArrayList;
import java.util.List;

import amp.lib.io.errors.ErrorEvent.Severity;
import amp.lib.io.meta.MetaObject;

public class Report {
    private static List<ErrorListener> listeners = new ArrayList<ErrorListener>();

    public static void addListener(ErrorListener listener) {
        listeners.add(listener);
    }

    public static void error(MetaObject source, String message) {
        report(new ErrorEvent(source, message, Severity.ERROR));
    }

    public static void error(String message) {
        report(new ErrorEvent(null, message, Severity.ERROR));
    }

    public static void info(MetaObject source, String message) {
        report(new ErrorEvent(source, message, Severity.INFO));
    }

    public static void info(String message) {
        report(new ErrorEvent(null, message, Severity.INFO));
    }

    public static void report(ErrorEvent event) {
        for (ErrorListener listener : listeners) {
            listener.errorOccurred(event);
        }
    }

    public static void warning(MetaObject source, String message) {
        report(new ErrorEvent(source, message, Severity.WARNING));
    }

    public static void warning(String message) {
        report(new ErrorEvent(null, message, Severity.WARNING));
    }
}
