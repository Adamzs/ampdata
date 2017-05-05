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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import amp.lib.io.MetadataObject;
import amp.lib.io.errors.Report;

/**
 * A factory for creating and managing Metadata objects.
 */
public class MetadataFactory {

    private static MetadataFactory manager = new MetadataFactory();

    private static Map<String, MetaObject> metadataMap = new HashMap<>();

    private MetadataFactory() {
    }

    /**
     * Clear all metadata.
     */
    public void clearAllMetadata() {
        metadataMap.clear();
    }

    /**
     * Gets the all metadata.
     *
     * @return the all metadata objects
     */
    public List<MetaObject> getAllMetadata() {
        List<MetaObject> metaList = new ArrayList<>();
        metaList.addAll(metadataMap.values());
        return metaList;
    }

    /**
     * Gets the all metaTables.
     *
     * @return the all meta tables
     */
    public List<MetaTable> getAllMetaTables() {
        List<MetaTable> metaList = new ArrayList<>();
        for (MetaObject mo : getAllMetadata()) {
            if (mo instanceof MetaTable) {
                metaList.add((MetaTable) mo);
            }
        }
        return metaList;
    }

    /**
     * Gets the all meta views.
     *
     * @return the all meta views
     */
    public List<MetaView> getAllMetaViews() {
        List<MetaView> metaList = new ArrayList<>();
        for (MetaObject mo : getAllMetadata()) {
            if (mo instanceof MetaView) {
                metaList.add((MetaView) mo);
            }
        }
        return metaList;
    }

    /**
     * Gets a MetaObject by identifier.
     *
     * @param identifier the identifier
     * @return the metadata or null if not found.
     */
    public MetaObject getMetadataByIdentifier(String identifier) {
        return metadataMap.get(identifier);
    }

    /**
     * Read and parse all the metadata files from the directory and sub-directories
     *
     * @param dir the root directory
     */
    public void readMetaFiles(File dir) {
        for (File f : dir.listFiles()) {
            MetaObject mo = null;
            if (f.isDirectory()) {
                readMetaFiles(f);
            } else if (isMeta(f)) {
                try {
                    mo = parseMetaFile(f);
                    metadataMap.put(mo.getIdentifier(), mo);
                } catch (MetaException e) {
                    Report.error(e.toString());
                }
            }
        }
    }

    /*
     * Is this is a metadata file
     */
    private boolean isMeta(File f) {
        return f.getName().endsWith(".meta");
    }

    /*
     * Parses and validates a metadata file into a MetaObject
     */
    private MetaObject parseMetaFile(File file) {
        MetaObject mo = null;
        JsonFactory factory = new JsonFactory();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JsonParser jp = factory.createParser(file);
            MetadataObject metaObject = objectMapper.readValue(jp, MetadataObject.class);
            jp.close();
            if (metaObject.getTableSchema() != null) {
                mo = new MetaTable(metaObject);
            } else if (metaObject.getViewSchema() != null) {
                mo = new MetaView(metaObject);
            } else {
                throw new MetaException(metaObject, "table and view definitions missing");
            }
        } catch (IOException e) {
            throw new MetaException(file, e.getMessage());
        }
        mo.setFile(file);
        return mo;
    }

    /**
     * Gets the metadata factory.
     *
     * @return the metadata factory
     */
    public static MetadataFactory getMetadataFactory() {
        return manager;
    }

}
