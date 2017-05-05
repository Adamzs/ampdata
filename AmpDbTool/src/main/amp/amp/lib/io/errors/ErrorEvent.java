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

import amp.lib.io.meta.MetaObject;

/**
 * The ErrorEvent class has information about the error itself, and its MetaObject source.
 */
public class ErrorEvent {

    MetaObject source = null;
    String message = "";
    Severity severity = Severity.INFO;

    /**
     * Instantiates a new error event.
     *
     * @param source the source
     * @param message the message
     * @param severity the severity
     */
    public ErrorEvent(MetaObject source, String message, Severity severity) {
        this.source = source;
        this.message = message;
        this.severity = severity;
    }

    /**
     * Instantiates a new error event.
     *
     * @param message the message
     * @param severity the severity
     */
    public ErrorEvent(String message, Severity severity) {
        this(null, message, severity);
    }

    /**
     * Gets the error info.
     *
     * @return the info
     */
    public Object getMessage() {
        return message;
    }

    /**
     * Gets the severity.
     *
     * @return the severity
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Sets the error info.
     *
     * @param message the new info
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets the severity.
     *
     * @param severity the new severity
     */
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    @Override
    public String toString() {
        String severity = "";
        if (getSeverity() != Severity.INFO) {
            severity = getSeverity().toString() + ": ";
        }
        if (source == null) {
            return severity + message;
        } else {
            return severity + " " + source.getIdentifier() + ": " + message;
        }
    }

    /**
     * The severity of an error.
     */
    public enum Severity {
        INFO, WARNING, ERROR
    }
}
