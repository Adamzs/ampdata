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
package amp.lib.io.meta;

/**
 * Thrown when something goes wrong with metadata parsing or validation.
 */
@SuppressWarnings("serial")
public class MetaException extends RuntimeException {

    private Object source;

    private String message;

    /**
     * Instantiates a new meta exception.
     * @param source the source
     * @param message the message
     */
    public MetaException(Object source, String message) {
        this.source = source;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * Gets the source.
     * @return the source
     */
    public Object getSource() {
        return source;
    }

    /**
     * Sets the message.
     * @param message the new message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets the source.
     * @param source the new source
     */
    public void setSource(Object source) {
        this.source = source;
    }

    @Override
    public String toString() {
        if (source == null) {
            return message;
        } else {
            return source.toString() + ": " + message;
        }
    }
}
