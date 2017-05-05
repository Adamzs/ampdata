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
package amp.lib.io.errors;

import java.util.ArrayList;
import java.util.List;

import amp.lib.io.errors.ErrorEvent.Severity;
import amp.lib.io.meta.MetaObject;

/**
 * Static utility methods for using the error reporting mechanism
 */
public class Report {
    private static List<ErrorListener> listeners = new ArrayList<ErrorListener>();

    /**
     * Adds a listener.
     *
     * @param listener the listener
     */
    public static void addListener(ErrorListener listener) {
        listeners.add(listener);
    }

    /**
     * Error reporter.
     *
     * @param source the source
     * @param message the message
     */
    public static void error(MetaObject source, String message) {
        report(new ErrorEvent(source, message, Severity.ERROR));
    }

    /**
     * Error reporter.
     *
     * @param message the message
     */
    public static void error(String message) {
        report(new ErrorEvent(null, message, Severity.ERROR));
    }

    /**
     * Info reporter.
     *
     * @param source the source
     * @param message the message
     */
    public static void info(MetaObject source, String message) {
        report(new ErrorEvent(source, message, Severity.INFO));
    }

    /**
     * Info reporter.
     *
     * @param message the message
     */
    public static void info(String message) {
        report(new ErrorEvent(null, message, Severity.INFO));
    }

    /**
     * Report an event.
     *
     * @param event the event
     */
    public static void report(ErrorEvent event) {
        for (ErrorListener listener : listeners) {
            listener.errorOccurred(event);
        }
    }

    /**
     * Warning reporter.
     *
     * @param source the source
     * @param message the message
     */
    public static void warning(MetaObject source, String message) {
        report(new ErrorEvent(source, message, Severity.WARNING));
    }

    /**
     * Warning reporter.
     *
     * @param message the message
     */
    public static void warning(String message) {
        report(new ErrorEvent(null, message, Severity.WARNING));
    }
}
