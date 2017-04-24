package amp.lib.io.csv;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import amp.lib.io.errors.ErrorEvent;
import amp.lib.io.errors.ErrorEventListener;
import amp.lib.io.errors.ErrorReporter;

public class CSVFileManager implements ErrorReporter {
    private static CSVFileManager CSVFileManager = new CSVFileManager();

    private static String[] exclusions = {
                    "fedResults", "adHoc", "lockResults", "midas_templates", "report"
    };

    private List<ErrorEventListener> listeners = new ArrayList<>();

    List<CSVFile> allCSVFiles = new ArrayList<>();

    private CSVFileManager() {
    }

    @Override
    public void addErrorEventListener(ErrorEventListener listener) {
        listeners.add(listener);

    }

    public List<CSVFile> getAllCSVFiles() {
        return allCSVFiles;
    }

    public CSVFile getCSVFileByIdentifier(String identifier) {
        for (CSVFile csv : getAllCSVFiles()) {
            if (csv.getCsvFile().getPath().endsWith(identifier)) {
                return csv;
            }
        }
        return null;
    }

    public List<CSVFile> readCSVFiles(File projectDirectory) {
        if (allCSVFiles.size() == 0) {
            for (File f : findFiles(projectDirectory)) {
                allCSVFiles.add(new CSVFile(f));
            }
        }
        return allCSVFiles;
    }

    @Override
    public void reportError(ErrorEvent event) {
        for (ErrorEventListener l : listeners) {
            l.errorOccurred(event);
        }
    }

    public static boolean exclude(File f) {
        for (String ex : exclusions) {
            if (f.getPath().contains(ex)) {
                return true;
            }
        }
        return false;
    }

    public static CSVFileManager getCSVFileManager() {
        return CSVFileManager;
    }

    public static boolean isCSV(File f) {
        return f.getName().endsWith(".csv");
    }

    private static List<File> findFiles(File dir) {
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

}
