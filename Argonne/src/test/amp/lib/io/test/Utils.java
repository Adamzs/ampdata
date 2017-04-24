package lib.io.test;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class Utils {

    private static String[] exclusions = {
                    "fedResults", "adHoc", "lockResults", "midas_templates", "report"
    };

    public static boolean exclude(File f) {
        for (String ex : exclusions) {
            if (f.getPath().contains(ex)) {
                return true;
            }
        }
        return false;
    }

    public static List<File> findFiles(File dir) {
        List<File> csvs = new ArrayList<>();
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                csvs.addAll(findFiles(f));
            } else if (isCSV(f) && !exclude(f)) {
                csvs.add(f);
            }
        }
        return csvs;
    }

    public static boolean isCSV(File f) {
        return f.getName().endsWith(".csv");
    }

    public static boolean isEqual(Set<String> values1, Set<String> values2) {
        return isSubset(values1, values2) && isSubset(values2, values1);
    }

    public static boolean isSubset(Set<String> values1, Set<String> values2) {
        for (String s : values1) {
            if (!values2.contains(s))
                return false;
        }
        return true;
    }

    public static Map<String, HashSet<String>> mapColumnValues(List<File> csvFiles) {
        Map<String, HashSet<String>> columnValues = new TreeMap<>();

        for (File f : csvFiles) {
            try {
                CSVParser p = new CSVParser(new FileReader(f), CSVFormat.EXCEL.withFirstRecordAsHeader());
                Map<String, Integer> headers = p.getHeaderMap();
                for (CSVRecord record : p.getRecords()) {
                    for (String header : headers.keySet()) {
                        String key = f.getPath() + ":" + header;
                        if (record.isSet(header)) {
                            String value = record.get(header);
                            if (!Utils.possibleKey(value)) {
                                columnValues.remove(key);
                                break;
                            }
                            if (!columnValues.containsKey(key)) {
                                columnValues.put(key, new HashSet<>());
                            }
                            columnValues.get(key).add(value);
                        }
                    }
                    p.close();
                }
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }
        return columnValues;
    }

    public static String normalizeHeaderName(String header) {
        return header.toUpperCase().replaceAll("[ _$]", "");
    }

    public static boolean possibleKey(String value) {
        if (value == null)
            return false;
        value = value.trim();
        if (value.length() <= 1)
            return false;
        if (value.matches("[-0-9\\.]+"))
            return false;
        if (value.matches("true|false"))
            return false;
        return true;
    }

}
