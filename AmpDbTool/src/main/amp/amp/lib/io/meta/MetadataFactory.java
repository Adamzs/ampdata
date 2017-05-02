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

public class MetadataFactory {

    private static MetadataFactory manager = new MetadataFactory();

    private static Map<String, MetaObject> metadataMap = new HashMap<>();

    private MetadataFactory() {
    }

    public void clearAllMetadata() {
        metadataMap.clear();
    }

    public List<MetaObject> getAllMetadata() {
        List<MetaObject> metaList = new ArrayList<>();
        metaList.addAll(metadataMap.values());
        return metaList;
    }

    public List<MetaTable> getAllMetaTables() {
        List<MetaTable> metaList = new ArrayList<>();
        for (MetaObject mo : getAllMetadata()) {
            if (mo instanceof MetaTable) {
                metaList.add((MetaTable) mo);
            }
        }
        return metaList;
    }

    public List<MetaView> getAllMetaViews() {
        List<MetaView> metaList = new ArrayList<>();
        for (MetaObject mo : getAllMetadata()) {
            if (mo instanceof MetaView) {
                metaList.add((MetaView) mo);
            }
        }
        return metaList;
    }

    public MetaObject getMetadataByName(String identifier) {
        return metadataMap.get(identifier);
    }

    public void readMetaFiles(File dir) {
        MetaObject mo = null;
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                readMetaFiles(f);
            } else if (isMeta(f)) {
                try {
                    mo = parseMetaFile(f);
                    metadataMap.put(mo.getIdentifier(), mo);
                } catch (Exception e) {
                    Report.error(mo, e.getMessage());
                }
            }
        }
    }

    private boolean isMeta(File f) {
        return f.getName().endsWith(".meta");
    }

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
                throw new RuntimeException("table and view definitions missing");
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        mo.setFile(file);
        return mo;
    }

    public static MetadataFactory getMetadataFactory() {
        return manager;
    }

}
