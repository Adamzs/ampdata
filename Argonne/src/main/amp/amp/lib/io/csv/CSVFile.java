package amp.lib.io.csv;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CSVFile {

    private File file;
    private List<String> columnList = new ArrayList<>();
    private List<List<String>> rows = new ArrayList<>();

    public CSVFile() {
    }

    public CSVFile(File file) {
        this.file = file;
    }

    public CSVFile(List<String> columnNames) {
        this();
        this.columnList = columnNames;
    }

    public List<String> addRow() {
        List<String> row = Collections.nCopies(columnList.size(), null);
        rows.add(row);
        return row;
    }

    public List<String> getColumns() {
        return columnList;
    }

    public File getCsvFile() {
        return file;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public String getValue(int row, int column) {
        return rows.get(row).get(column);
    }

    public String getValue(int row, String column) {
        return rows.get(row).get(columnList.indexOf(column));
    }

    public void parse() {
        try {
            if (rows.isEmpty()) {
                CSVParser p = new CSVParser(new FileReader(file), CSVFormat.EXCEL.withFirstRecordAsHeader());
                Map<String, Integer> headers = p.getHeaderMap();
                for (String header : headers.keySet()) {
                    columnList.add(header);
                }
                for (CSVRecord record : p.getRecords()) {
                    ArrayList<String> row = new ArrayList<String>();
                    rows.add(row);
                    for (int col = 0; col < headers.size(); col++) {
                        String value = col < record.size() ? record.get(col) : null;
                        row.set(columnList.indexOf(col), value);
                    }
                }
                p.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setValue(int row, int column, String value) {
        rows.get(row).set(column, value);
    }

    public void setValue(int row, String column, String value) {
        rows.get(row).set(columnList.indexOf(column), value);
    }

    @Override
    public String toString() {
        return "CSVFile [" + file.getPath() + "]";
    }

}
