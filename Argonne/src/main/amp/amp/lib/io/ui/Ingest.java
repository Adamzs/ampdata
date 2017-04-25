package amp.lib.io.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import amp.lib.io.db.DBManager;
import amp.lib.io.db.Database;
import amp.lib.io.db.SQLManager;
import amp.lib.io.errors.ErrorEvent;
import amp.lib.io.errors.ErrorEvent.Severity;
import amp.lib.io.errors.ErrorEventListener;
import amp.lib.io.meta.Metadata;
import amp.lib.io.meta.Metafile;
import amp.lib.io.meta.MetafileManager;
import amp.lib.io.populate.PopulateManager;
import amp.lib.io.props.Properties;

public class Ingest implements ActionListener, ErrorEventListener {
    FileMenu fileMenu = new FileMenu();
    DatabaseMenu databaseMenu = new DatabaseMenu();
    JTextPane traceWindow = new JTextPane();
    JScrollPane traceScroller = new JScrollPane();
    List<Metafile> metaFiles = new ArrayList<>();
    JFrame ingesterFrame = new JFrame();
    DBManager dbManager;
    MetafileManager mfManager;
    SQLManager sqlManager;
    PopulateManager popManager;
    Database db;
    String selection = null;
    File projectDirectory;

    private Style redStyle = null;

    private Style blackStyle = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(FileMenu.QUIT)) {
            quit();
        } else if (e.getActionCommand().equals(FileMenu.CLOSE)) {
            closeProject();
        } else if (e.getActionCommand().equals(FileMenu.OPEN)) {
            openProject();
        } else if (e.getActionCommand().equals(DatabaseMenu.OPEN)) {
            openDatabase();
        } else if (e.getActionCommand().equals(DatabaseMenu.CREATE)) {
            createDatabase();
        } else if (e.getActionCommand().equals(DatabaseMenu.SCHEMA)) {
            createSchema();
        } else if (e.getActionCommand().equals(DatabaseMenu.DROP)) {
            dropDatabase();
        } else if (e.getActionCommand().equals(DatabaseMenu.POPULATE)) {
            populateDatabase();
        }

    }

    @Override
    public void errorOccurred(ErrorEvent event) {
        String text = "\n--\n" + event.getInfo().toString();
        Style style = (event.getSeverity() == Severity.ERROR) ? redStyle : blackStyle;
        StyledDocument doc = traceWindow.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), text, style);
            traceWindow.setCaretPosition(doc.getLength());
            traceWindow.update(traceWindow.getGraphics());
            JScrollBar vertical = traceScroller.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
            traceScroller.update(traceScroller.getGraphics());
        } catch (BadLocationException e) {
            // no-op
        }

    }

    private void closeProject() {
        traceWindow.setText(null);
    }

    private void createDatabase() {
        Properties props = Properties.getProperties();
        String dbName = "";
        String dbUser = props.get(Properties.DB_USER);
        String dbPassword = props.get(Properties.DB_PASSWORD);
        CreateDatabasePanel createDialog = new CreateDatabasePanel(dbName, dbUser, dbPassword);
        int selected = JOptionPane.showConfirmDialog(ingesterFrame, createDialog, "Create Database", JOptionPane.OK_CANCEL_OPTION);
        if (selected == JOptionPane.OK_OPTION) {
            dbName = createDialog.getDatabaseName();
            dbUser = createDialog.getUser();
            dbPassword = createDialog.getPassword();
            db = dbManager.createDatabase(dbName, dbUser, dbPassword);
        }
        props.set(Properties.DB_NAME, dbName);
        props.set(Properties.DB_USER, dbUser);
        props.set(Properties.DB_PASSWORD, dbPassword);
    }

    private void createSchema() {
        if (isOpenProject() && isOpenDatabase()) {
            dbManager.buildSchema(Metadata.getAllMetadata());
        }
    }

    private void dropDatabase() {
        List<String> databases = dbManager.getDatabaseSelection();
        DropDatabasePanel openDialog = new DropDatabasePanel(databases);
        int selected = JOptionPane.showConfirmDialog(ingesterFrame, openDialog, "Drop Database", JOptionPane.OK_CANCEL_OPTION);
        if (selected == JOptionPane.OK_OPTION) {
            String dbName = openDialog.getDatabaseName();
            dbManager.dropDatabase(dbName);
        }
    }

    private boolean isOpenDatabase() {
        if (db == null) {
            JOptionPane.showMessageDialog(ingesterFrame, "Open a database first");
            return false;
        }
        return true;
    }

    private boolean isOpenProject() {
        if (metaFiles.size() == 0) {
            JOptionPane.showMessageDialog(ingesterFrame, "Open a project first");
            return false;
        }
        return true;
    }

    private void openDatabase() {
        List<String> databases = dbManager.getDatabaseSelection();
        Properties props = Properties.getProperties();
        String dbName = props.get(Properties.DB_NAME);
        String dbUser = props.get(Properties.DB_USER);
        String dbPassword = props.get(Properties.DB_PASSWORD);
        OpenDatabasePanel openDialog = new OpenDatabasePanel(databases, dbName, dbUser, dbPassword);
        int selected = JOptionPane.showConfirmDialog(ingesterFrame, openDialog, "Select Database", JOptionPane.OK_CANCEL_OPTION);
        if (selected == JOptionPane.OK_OPTION) {
            dbName = openDialog.getDatabaseName();
            dbUser = openDialog.getUser();
            dbPassword = openDialog.getPassword();
            db = dbManager.openDatabase(dbName, dbUser, dbPassword);
        }
        props.set(Properties.DB_NAME, dbName);
        props.set(Properties.DB_USER, dbUser);
        props.set(Properties.DB_PASSWORD, dbPassword);
    }

    private void openProject() {
        closeProject();
        Properties props = Properties.getProperties();
        String projectRoot = props.get(Properties.PROJ_ROOT);
        OpenProjectPanel openDialog = new OpenProjectPanel(projectRoot);
        int selected = JOptionPane.showConfirmDialog(ingesterFrame, openDialog, "Select Project", JOptionPane.OK_CANCEL_OPTION);
        if (selected == JOptionPane.OK_OPTION) {
            projectDirectory = new File(openDialog.getProjectPath());
            metaFiles = MetafileManager.getMetafileHandler().readMetaFiles(projectDirectory);
            Properties.getProperties().set(Properties.PROJ_ROOT, projectDirectory.getAbsolutePath());
            JOptionPane.showMessageDialog(ingesterFrame, "Import metadatas completed.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void populateDatabase() {
        if (isOpenProject() && isOpenDatabase()) {
            popManager.populateData(db, Metadata.getAllMetadata());
        }
    }

    private void quit() {
        ingesterFrame.dispose();
        System.exit(0);
    }

    protected void run() {
        dbManager = DBManager.getDbManager();
        sqlManager = SQLManager.getSQLManager();
        mfManager = MetafileManager.getMetafileHandler();
        popManager = PopulateManager.getPopulateManager();

        dbManager.addErrorEventListener(this);
        sqlManager.addErrorEventListener(this);
        mfManager.addErrorEventListener(this);
        popManager.addErrorEventListener(this);
        fileMenu.addActionListener(this);
        databaseMenu.addActionListener(this);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }

        traceWindow = new JTextPane();
        traceScroller = new JScrollPane(traceWindow);

        redStyle = traceWindow.addStyle("Red", null);
        blackStyle = traceWindow.addStyle("Black", null);
        StyleConstants.setForeground(redStyle, Color.red);
        StyleConstants.setForeground(blackStyle, Color.black);

        ingesterFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ingesterFrame.setJMenuBar(new JMenuBar());
        ingesterFrame.getJMenuBar().add(fileMenu);
        ingesterFrame.getJMenuBar().add(databaseMenu);
        ingesterFrame.getContentPane().setLayout(new BorderLayout());
        ingesterFrame.getContentPane().add(traceScroller, BorderLayout.CENTER);
        ingesterFrame.pack();
        ingesterFrame.setSize(1000, 1000);
        ingesterFrame.setVisible(true);
    }

    public static void main(String[] args) {
        Ingest ingester = new Ingest();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ingester.run();
            }
        });
    }

}
