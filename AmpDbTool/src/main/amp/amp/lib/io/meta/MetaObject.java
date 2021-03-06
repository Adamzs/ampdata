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
 * The Class MetaObject.
 */
public abstract class MetaObject {

    public String title = "";

    private String identifier = "";

    private File file = null;
    private boolean valid = true;

	private Boolean autoindex = false;

    /**
     * Instantiates a new meta object.
     * @param metaObject the meta object
     */
    public MetaObject(MetadataObject metaObject) {
        super();
        this.setTitle(metaObject.getTitle());
        this.setIdentifier(metaObject.getId());
        if (metaObject.addindex != null) {
        	this.setAutoIndex(metaObject.addindex.equalsIgnoreCase("TRUE"));
        }
    }

    protected MetaObject() {
        super();
    }

    /**
     * Debugging data for the MetaObject
     * @return debugging data
     */
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

    /**
     * Gets the file.
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * Gets the identifier.
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the title.
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
    }

    /**
     * Is metaObject valid for futher processing?
     * @return validity state
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Sets the file.
     * @param metafile the metafile
     */
    public void setFile(File metafile) {
        this.file = metafile;

    }
    
    public void setAutoIndex(Boolean addindex) {
    	this.autoindex = addindex;
    }

    /**
     * Sets the identifier.
     * @param identifier the identifier
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;

    }

    /**
     * Sets the title.
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Marks metaObject as valid/invalid for further processing
     * @param valid validity state
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Boolean isAutoIndex() {
    	return this.autoindex;
    }
    
    @Override
    public String toString() {
        if (identifier != null)
            return identifier;
        if (file != null)
            return file.getPath();
        return "<unknown>";
    }

}
