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
package amp.lib.io.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import amp.lib.io.db.Database;
import amp.lib.io.errors.ErrorEvent;
import amp.lib.io.errors.ErrorEvent.Severity;
import amp.lib.io.errors.ErrorListener;
import amp.lib.io.errors.Report;
import amp.lib.io.meta.MetadataFactory;
import amp.lib.io.props.Properties;

/**
 * The main class for the AMP Database Tool User Interface.
 */
public class AMPDbTool implements Runnable, ActionListener, ErrorListener {
    private Properties props = Properties.getProperties();
    private ProjectMenu projectMenu = new ProjectMenu();
    private DatabaseMenu databaseMenu = new DatabaseMenu();
    private JTextPane traceWindow = new JTextPane();
    private JScrollPane traceScroller = new JScrollPane();
    private JFrame ampDbToolFrame = new JFrame();
    private Database database;
    private MetadataFactory metaFactory;
    private File logPath;
    private Style redStyle = null;
    private Style blackStyle = null;
    private Style blueStyle = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(ProjectMenu.QUIT)) {
            quit();
        } else if (e.getActionCommand().equals(ProjectMenu.CLOSE)) {
            closeProject();
        } else if (e.getActionCommand().equals(ProjectMenu.OPEN)) {
            openProject();
        } else if (e.getActionCommand().equals(ProjectMenu.SAVE)) {
            saveOutput();
        } else if (e.getActionCommand().equals(DatabaseMenu.OPEN)) {
            openDatabase();
        } else if (e.getActionCommand().equals(DatabaseMenu.CREATE)) {
            createDatabase();
        } else if (e.getActionCommand().equals(DatabaseMenu.SCHEMA)) {
            createSchema();
        } else if (e.getActionCommand().equals(DatabaseMenu.SQL)) {
            executeCustomSQL();
        } else if (e.getActionCommand().equals(DatabaseMenu.DROP)) {
            dropDatabase();
        } else if (e.getActionCommand().equals(DatabaseMenu.POPULATE)) {
            populateDatabase();
        }

    }

    /**
     * Reports an error from the error listeners to the progress window.
     * @see amp.lib.io.errors.ErrorListener#errorOccurred(amp.lib.io.errors.ErrorEvent)
     */
    @Override
    public void errorOccurred(ErrorEvent event) {
        Style style = blackStyle;
        if (event.getSeverity() == Severity.ERROR) {
            style = redStyle;
        } else if (event.getSeverity() == Severity.INFO) {
            style = blueStyle;
        }
        StyledDocument doc = traceWindow.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), "\n" + event.toString(), style);
            traceWindow.setCaretPosition(doc.getLength());
            JScrollBar vertical = traceScroller.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
            traceScroller.paintAll(traceScroller.getGraphics());
        } catch (BadLocationException e) {
            // no-op
        }

    }

    @Override
    public void run() {
        database = Database.getDatabase();
        metaFactory = MetadataFactory.getMetadataFactory();

        Report.addListener(this);

        projectMenu.addActionListener(this);
        databaseMenu.addActionListener(this);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }

        traceWindow = new JTextPane();
        traceScroller = new JScrollPane(traceWindow);
        traceScroller.setAutoscrolls(true);
        traceScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        traceScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        redStyle = traceWindow.addStyle("Red", null);
        blackStyle = traceWindow.addStyle("Black", null);
        blueStyle = traceWindow.addStyle("Blue", null);
        StyleConstants.setForeground(redStyle, Color.red);
        StyleConstants.setForeground(blackStyle, Color.black);
        StyleConstants.setForeground(blueStyle, Color.blue);

        ampDbToolFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ampDbToolFrame.setJMenuBar(new JMenuBar());
        ampDbToolFrame.getJMenuBar().add(projectMenu);
        ampDbToolFrame.getJMenuBar().add(databaseMenu);
        ampDbToolFrame.getContentPane().setLayout(new BorderLayout());
        ampDbToolFrame.getContentPane().add(traceScroller, BorderLayout.CENTER);
        ampDbToolFrame.pack();
        ampDbToolFrame.setSize(1000, 1000);
        ampDbToolFrame.setVisible(true);
    }

    /*
     * Closes a project and resets all metadata.
     */
    private void closeProject() {
        metaFactory.clearAllMetadata();
        traceWindow.setText(null);
    }

    /*
     * Gathers info to create a new database, and creates it.
     */
    private void createDatabase() {
        String dbName = "";
        String dbUser = props.get(Properties.DB_USER);
        String dbPassword = props.get(Properties.DB_PASSWORD);
        CreateDatabasePanel createDialog = new CreateDatabasePanel(dbName, dbUser, dbPassword);
        int selected = JOptionPane.showConfirmDialog(ampDbToolFrame, createDialog, "Create Database", JOptionPane.OK_CANCEL_OPTION);
        if (selected == JOptionPane.OK_OPTION) {
            dbName = createDialog.getDatabaseName();
            dbUser = createDialog.getUser();
            dbPassword = createDialog.getPassword();
            database.createDatabase(dbName, dbUser, dbPassword);
        }
        props.set(Properties.DB_NAME, dbName);
        props.set(Properties.DB_USER, dbUser);
        props.set(Properties.DB_PASSWORD, dbPassword);
    }

    /*
     * Creates a new database schema in the open database from the project metadata.
     */
    private void createSchema() {
        if (isOpenProject() && isOpenDatabase()) {
            String dbName = database.getName();
            String dbUser = database.getUser();
            String dbPassword = database.getPassword();
            database.dropDatabase(dbName);
            database.createDatabase(dbName, dbUser, dbPassword);
            database.buildSchema(metaFactory.getAllMetadata());
        }
    }

    /*
     * Drops a database by name.
     */
    private void dropDatabase() {
        List<String> databases = database.getDatabaseSelection();
        DropDatabasePanel openDialog = new DropDatabasePanel(databases);
        int selected = JOptionPane.showConfirmDialog(ampDbToolFrame, openDialog, "Drop Database", JOptionPane.OK_CANCEL_OPTION);
        if (selected == JOptionPane.OK_OPTION) {
            String dbName = openDialog.getDatabaseName();
            database.dropDatabase(dbName);
        }
    }

    /*
     * Executes custom SQL from files.
     */
    private void executeCustomSQL() {
        if (isOpenDatabase()) {
            String sqlRoot = props.get(Properties.SQL_ROOT);
            OpenSQLPanel openDialog = new OpenSQLPanel(sqlRoot);
            int selected = JOptionPane.showConfirmDialog(ampDbToolFrame, openDialog, "Select SQL Views", JOptionPane.OK_CANCEL_OPTION);
            if (selected == JOptionPane.OK_OPTION) {
                List<File> sqlFiles = openDialog.getSqlFiles();
                if (sqlFiles.size() > 0) {
                    props.set(Properties.SQL_ROOT, openDialog.getSqlRoot());
                    database.buildSQL(sqlFiles);
                }
            }
        }
    }

    private boolean isFilterable(String line) {
        return line.startsWith("ERROR") || line.startsWith("INFO");
    }

    /*
     * Is the database open?
     */
    private boolean isOpenDatabase() {
        if (database == null || !database.isOpen()) {
            JOptionPane.showMessageDialog(ampDbToolFrame, "Open a database first");
            return false;
        }
        return true;
    }

    /*
     * Is the project open?
     */
    private boolean isOpenProject() {
        if (metaFactory.getAllMetadata().size() == 0) {
            JOptionPane.showMessageDialog(ampDbToolFrame, "Open a project first");
            return false;
        }
        return true;
    }

    /*
     * Get the database information and open it.
     */
    private void openDatabase() {
        List<String> databases = database.getDatabaseSelection();
        String dbName = props.get(Properties.DB_NAME);
        String dbUser = props.get(Properties.DB_USER);
        String dbPassword = props.get(Properties.DB_PASSWORD);
        OpenDatabasePanel openDialog = new OpenDatabasePanel(databases, dbName, dbUser, dbPassword);
        int selected = JOptionPane.showConfirmDialog(ampDbToolFrame, openDialog, "Select Database", JOptionPane.OK_CANCEL_OPTION);
        if (selected == JOptionPane.OK_OPTION) {
            dbName = openDialog.getDatabaseName();
            dbUser = openDialog.getUser();
            dbPassword = openDialog.getPassword();
            database.openDatabase(dbName, dbUser, dbPassword);
        }
        props.set(Properties.DB_NAME, dbName);
        props.set(Properties.DB_USER, dbUser);
        props.set(Properties.DB_PASSWORD, dbPassword);
    }

    /*
     * Get the project root directory and open it.
     */
    private void openProject() {
        closeProject();
        String projectRoot = props.get(Properties.PROJ_ROOT);
        OpenProjectPanel openDialog = new OpenProjectPanel(projectRoot);
        int selected = JOptionPane.showConfirmDialog(ampDbToolFrame, openDialog, "Select Project", JOptionPane.OK_CANCEL_OPTION);
        if (selected == JOptionPane.OK_OPTION) {
            logPath = new File(openDialog.getProjectPath());
            props.set(Properties.PROJ_ROOT, logPath.getAbsolutePath());
            metaFactory.readMetaFiles(logPath);
            Report.okay("INFO: Opened " + metaFactory.getAllMetadata().size() + " metadata files");
        }
    }

    /*
     * Populate the open database from the project CSV files.
     */
    private void populateDatabase() {
        if (isOpenProject() && isOpenDatabase()) {
            database.populateTables(metaFactory.getAllMetadata());
        }
    }

    /*
     * Quit the tool.
     */
    private void quit() {
        ampDbToolFrame.dispose();
        System.exit(0);
    }

    /*
     * Save the trace output to a file.
     */
    private void saveOutput() {
        String logPath = props.get(Properties.LOG_FILE);
        String logFilter = props.get(Properties.LOG_FILTER);
        CreateLogPanel logPanel = new CreateLogPanel(logPath, Boolean.parseBoolean(logFilter));
        int selected = JOptionPane.showConfirmDialog(ampDbToolFrame, logPanel, "Select Log File", JOptionPane.OK_CANCEL_OPTION);
        if (selected == JOptionPane.OK_OPTION && logPanel.getlogPath().length() > 0) {
            File logFile = new File(logPanel.getlogPath());
            boolean filterErrors = logPanel.getFilterErrors();
            String[] text = traceWindow.getText().split("\n");
            try (FileWriter logWriter = new FileWriter(logFile)) {
                for (String line : text) {
                    if (filterErrors && isFilterable(line)) {
                        // no-op
                    } else {
                        logWriter.write(line + "\n");
                    }
                }
            } catch (IOException e) {
                Report.error(e.getMessage());
            }
            props.set(Properties.LOG_FILE, logFile.getAbsolutePath());
            props.set(Properties.LOG_FILTER, Boolean.toString(filterErrors));
        }
    }

    /**
     * The entry point
     * @param args the arguments: not used.
     */
    public static void main(String[] args) {
        AMPDbTool ampDbTool = new AMPDbTool();
        SwingUtilities.invokeLater(ampDbTool);
    }

}
