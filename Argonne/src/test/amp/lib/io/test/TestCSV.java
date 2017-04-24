package lib.io.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;

import amp.lib.io.csv.CSVFile;
import amp.lib.io.csv.CSVFileManager;

public class TestCSV {

    @Test
    public void test() {
        CSVFileManager csvManager = CSVFileManager.getCSVFileManager();
        csvManager.readCSVFiles(new File("data"));
        List<CSVFile> csvs = csvManager.getAllCSVFiles();
        int noColumns = 0;
        int noRows = 0;
        for (CSVFile csv : csvs) {
            csv.parse();
            if (csv.getRows().isEmpty())
                noRows++;
            if (csv.getColumns().isEmpty())
                noColumns++;
        }
        assertEquals("Total CSV file count", 551, csvs.size());
        assertEquals("No-row file count", 101, noRows);
        assertEquals("No-column file count", 26, noColumns);
    }

}
