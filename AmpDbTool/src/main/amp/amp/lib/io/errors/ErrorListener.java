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

/**
 * The listener interface for receiving error events. The class that is interested in processing a
 * error event implements this interface, and the object created with that class is registered with
 * a component using the component's <code>addErrorListener<code> method. When the error event
 * occurs, that object's appropriate method is invoked.
 * @see ErrorEvent
 */
public interface ErrorListener {

    /**
     * Error occurred: handle appropriately.
     * @param event the event that occurred
     */
    public void errorOccurred(ErrorEvent event);
}
