package amp.lib.io.meta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import amp.lib.io.MetadataObject;
import amp.lib.io.errors.ErrorEvent;
import amp.lib.io.errors.ErrorEventListener;
import amp.lib.io.errors.ErrorReporter;
import amp.lib.io.meta.Metadata.PrimaryKeyColumn;

public class Metafile implements ErrorReporter {

    private File file;
    private Metadata metadata;
    private MetadataObject metaObject;

    private List<ErrorEventListener> listeners = new ArrayList<>();

    public Metafile(File file) {
        this.file = file;
    }

    @Override
    public void addErrorEventListener(ErrorEventListener listener) {
        listeners.add(listener);

    }

    public File getFile() {
        return file;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public MetadataObject getMetadataObject() {
        return metaObject;
    }

    public void parseMetaFile() {
        JsonFactory factory = new JsonFactory();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            JsonParser jp = factory.createParser(file);
            metaObject = objectMapper.readValue(jp, MetadataObject.class);
            jp.close();
            createMetadata();
        } catch (IOException e) {
            this.reportError(new ErrorEvent(this, e.getMessage()));
        }
    }

    @Override
    public void reportError(ErrorEvent event) {
        for (ErrorEventListener l : listeners) {
            l.errorOccurred(event);
        }
    }

    @Override
    public String toString() {
        return file.getPath();
    }

    private void createMetadata() {
        metadata = Metadata.getMetadata(metaObject.getId());
        metadata.setFile(this);
        metadata.setTableName(metaObject.getTitle());
        metadata.setIdentifier(metaObject.id);
        metadata.setTableName(metaObject.title);
        MetadataObject.Column[] columnList = metaObject.getTableSchema().getColumns();
        if (columnList == null || columnList.length == 0) {
            this.reportError(new ErrorEvent(this, metaObject.getTitle() + ": no columns defined"));
        }
        for (MetadataObject.Column objCol : columnList) {
            Metadata.Column metaCol = null;
            if (isPk(objCol, metaObject)) {
                PrimaryKeyColumn pk = new Metadata.PrimaryKeyColumn();
                metadata.setPrimaryKey(pk);
                metaCol = pk;
            } else if (isFk(objCol, metaObject)) {
                MetadataObject.ForeignKey fk = toFk(objCol, metaObject);
                MetadataObject.Reference fkRef = fk.getReference();
                Metadata.ForeignKeyColumn fkCol = new Metadata.ForeignKeyColumn(fkRef.getSchemaReference(), fkRef.getColumnReference());
                metadata.getForeignKeys().add(fkCol);
                metaCol = fkCol;
            } else {
                metaCol = new Metadata.Column();
            }
            metaCol.setTable(metadata);
            metaCol.setName(objCol.getName());
            metaCol.setDatatype(objCol.getDatatype());
            metaCol.setDescription(objCol.getDescription());
            metaCol.setMaxLength(objCol.getMaxLength());
            metaCol.setTitles(objCol.getTitles());
            metaCol.setRequired(objCol.isRequired());

            if (metadata.getColumns().contains(metaCol)) {
                this.reportError(new ErrorEvent(this, ": duplicate column " + metaCol));
            }
            metadata.getColumns().add(metaCol);
        }

    }

    private boolean isFk(MetadataObject.Column objCol, MetadataObject obj) {
        for (MetadataObject.ForeignKey col : obj.getTableSchema().foreignKeys) {
            if (objCol.getName().equalsIgnoreCase(col.getColumnReference())) {
                return true;
            }
        }
        return false;
    }

    private boolean isPk(MetadataObject.Column objCol, MetadataObject obj) {
        if (objCol.getName().equals(obj.getTableSchema().getPrimaryKey())) {
            return true;
        }
        return false;
    }

    private MetadataObject.ForeignKey toFk(MetadataObject.Column objCol, MetadataObject obj) {
        for (MetadataObject.ForeignKey col : obj.getTableSchema().foreignKeys) {
            if (objCol.getName().equalsIgnoreCase(col.getColumnReference())) {
                return col;
            }
        }
        return null;
    }

}
