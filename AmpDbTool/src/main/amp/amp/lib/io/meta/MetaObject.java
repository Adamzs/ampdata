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

import java.io.File;

import amp.lib.io.MetadataObject;

/**
 * Represents metadata about an analysis table to be created or populated.
 * Information includes: - Ordinary columns, with datatype and other decorators
 * - Primary key column - Foreign key columns
 * 
 * @author brucejtaylor333
 *
 */
public abstract class MetaObject {

    boolean valid = true;
    public String title = "";

    private String identifier = "";

    private File file = null;

    public MetaObject(MetadataObject metaObject) {
        super();
        this.setTitle(metaObject.getTitle());
        this.setIdentifier(metaObject.getId());
    }

    protected MetaObject() {
        super();
    }

    public String dump() {
        return identifier;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof MetaObject))
            return false;
        return getIdentifier().equals(((MetaObject) obj).getIdentifier());
    }

    public File getFile() {
        return file;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getTitle() {
        return title;
    }

    public boolean getValid() {
        return valid;
    }

    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
    }

    public void setFile(File metafile) {
        this.file = metafile;

    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;

    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public String toString() {
        return getIdentifier();
    }

}
