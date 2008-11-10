/*
 * ome.formats.testclient.TestClient
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import ome.formats.importer.util.Actions;
import ome.formats.importer.util.BareBonesBrowserLaunch;
import ome.formats.importer.util.GuiCommonElements;
import ome.formats.importer.util.IniFileLoader;
import ome.system.UpgradeCheck;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmicroscopy.shoola.util.ui.MacOSMenuHandler;
import org.openmicroscopy.shoola.util.ui.login.LoginCredentials;
import org.openmicroscopy.shoola.util.ui.login.ScreenLogin;
import org.openmicroscopy.shoola.util.ui.login.ScreenLogo;

/**
 * @author Brian W. Loranger
 */

public class Main extends JFrame 
    implements  ActionListener, WindowListener, IObserver, PropertyChangeListener, 
                WindowStateListener, WindowFocusListener
{
    private static final long   serialVersionUID = 1228000122345370913L;
    
    IniFileLoader   ini;
    
    public static String        dbVersion = "300";
    
    public static String versionNumber = "Unknown";
    
    /** The data of the last release date. */
    public static String        releaseDate      
         = "2008-06-16 16:18:13 +0100 (Mon, 16 Jun 2008)";

    /** The repository revision. */
    public static String        revision  = "$LastChangedRevision: 2524 $";

    /** The data of the last repository revision. */
    public static String        revisionDate     
         = "$LastChangedDate: 2008-06-23 10:29:21 +0100 (Mon, 23 Jun 2008) $";

    /** Logger for this class. */
    @SuppressWarnings("unused")
    private static Log          log     = LogFactory.getLog(Main.class);
   
    // -- Constants --
    private final static boolean useSplashScreenAbout   = false;
    private static boolean USE_QUAQUA = true;
    
    
    //public final static String TITLE            = "OMERO.importer";
    
    public final static String splash           = "gfx/importer_splash.png";
    public static final String ICON = "gfx/icon.png";
    public static final String QUIT_ICON = "gfx/nuvola_exit16.png";
    public static final String LOGIN_ICON = "gfx/nuvola_login16.png";
    public static final String COMMENT_ICON = "gfx/nuvola_sendcomment16.png";
    public static final String HOME_ICON = "gfx/nuvola_home16.png";
    public static final String ABOUT_ICON = "gfx/nuvola_about16.png";
    public static final String HISTORY_ICON = "gfx/nuvola_history16.png";
    public static final String CHOOSER_ICON = "gfx/nuvola_chooser16.png";
    public static final String OUTPUT_ICON = "gfx/nuvola_output16.png";
    public static final String BUG_ICON = "gfx/nuvola_bug16.png";
    
    public LoginHandler         loginHandler;
    public FileQueueHandler     fileQueueHandler;
    public static HistoryDB     db;
    public StatusBar            statusBar;
    private GuiCommonElements   gui;
    private static final    String HOME_URL = 
    	"http://trac.openmicroscopy.org.uk/shoola/wiki/OmeroInsightGettingStarted";
    
    public ScreenLogin         view;
    public ScreenLogo          viewTop;

    private JMenuBar            menubar;
    private JMenu               fileMenu;
    private JMenuItem           fileQuit;
    private JMenuItem           login;
    private JMenu               helpMenu;
    private JMenuItem           helpComment;
    private JMenuItem           helpHome;
    private JMenuItem           helpAbout;
    
    public Boolean              loggedIn;

    private JTextPane           outputTextPane;
    private JTextPane           debugTextPane;
    private JPanel              historyPanel;
    
    private JTabbedPane         tPane;

    @SuppressWarnings("unused")
    private String              username;
    @SuppressWarnings("unused")
    private String              password;
    @SuppressWarnings("unused")
    private String              server;
    @SuppressWarnings("unused")
    private String              port;
  
    
    /**
     * Main entry class for the application
     */
    public Main(String[] args)
    {
        //super(TITLE);
        
        isUpgradeRequired();
        
        gui = new GuiCommonElements();
        
        // Load up the main ini file
        ini = IniFileLoader.getIniFileLoader(args);
        
        // Add a shutdown hook for when app closes
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { shutdown(); }
        });
        
        // Set app defaults
        setTitle(ini.getAppTitle());
        setIconImage(gui.getImageIcon(Main.ICON).getImage());
        setPreferredSize(new Dimension(gui.bounds.width, gui.bounds.height));
        setSize(gui.bounds.width, gui.bounds.height);
        setLocation(gui.bounds.x, gui.bounds.y);      
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();

        addWindowListener(this);

        // capture move info
        addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent evt) {
                gui.bounds = getBounds();
            }
        });

        // capture resize info
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                gui.bounds = getBounds();               
            }
        });
        
        // menu bar
        menubar = new JMenuBar();
        fileMenu = new JMenu("File");
        menubar.add(fileMenu);
        login = new JMenuItem("Login to the server...", gui.getImageIcon(LOGIN_ICON));
        login.setActionCommand("login");
        login.addActionListener(this);        
        fileMenu.add(login);
        fileQuit = new JMenuItem("Quit", gui.getImageIcon(QUIT_ICON));
        fileQuit.setActionCommand("quit");
        fileQuit.addActionListener(this);
        fileMenu.add(fileQuit);
        helpMenu = new JMenu("Help");
        menubar.add(helpMenu);
        helpComment = new JMenuItem("Send a Comment...", gui.getImageIcon(COMMENT_ICON));
        helpComment.setActionCommand("comment");
        helpComment.addActionListener(this);
        helpHome = new JMenuItem("Visit Importer Homepage...", gui.getImageIcon(HOME_ICON));
        helpHome.setActionCommand("home");
        helpHome.addActionListener(this);
        helpAbout = new JMenuItem("About the Importer...", gui.getImageIcon(ABOUT_ICON));
        helpAbout.setActionCommand("about");
        helpAbout.addActionListener(this);
        helpMenu.add(helpComment);
        helpMenu.add(helpHome);
        helpMenu.add(helpAbout);
        setJMenuBar(menubar);
      
        // tabbed panes
        tPane = new JTabbedPane();
        tPane.setOpaque(false); // content panes must be opaque

        // file chooser pane
        JPanel filePanel = new JPanel(new BorderLayout());

        // The file chooser sub-pane
        fileQueueHandler = new FileQueueHandler(this);
        //splitPane.setResizeWeight(0.5);

        filePanel.add(fileQueueHandler, BorderLayout.CENTER);
        tPane.addTab("File Chooser", gui.getImageIcon(CHOOSER_ICON), filePanel,
        "Add and delete images here to the import queue.");
        tPane.setMnemonicAt(0, KeyEvent.VK_1);

        // history pane
        historyPanel = new JPanel();
        historyPanel.setOpaque(false);
        historyPanel.setLayout(new BorderLayout());
        
        tPane.addTab("Import History", gui.getImageIcon(HISTORY_ICON), historyPanel,
                "Import history is displayed here.");
        tPane.setMnemonicAt(0, KeyEvent.VK_4);
       
        // output text pane
        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BorderLayout());
        outputTextPane = new JTextPane();
        outputTextPane.setEditable(false);

        JScrollPane outputScrollPane = new JScrollPane();
        outputScrollPane.getViewport().add(outputTextPane);
        
        outputScrollPane.getVerticalScrollBar().addAdjustmentListener(
                new AdjustmentListener()
        {
            public void adjustmentValueChanged(AdjustmentEvent e)
            {
                outputTextPane.setCaretPosition(outputTextPane.getDocument().
                        getLength());
            }
        }
        );

        outputPanel.add(outputScrollPane, BorderLayout.CENTER);

        tPane.addTab("Output Text", gui.getImageIcon(OUTPUT_ICON), outputPanel,
                "Standard output text goes here.");
        tPane.setMnemonicAt(0, KeyEvent.VK_2);


        // debug pane
        JPanel debugPanel = new JPanel();
        debugPanel.setLayout(new BorderLayout());
        debugTextPane = new JTextPane();
        debugTextPane.setEditable(false);

        JScrollPane debugScrollPane = new JScrollPane();
        debugScrollPane.getViewport().add(debugTextPane);

        debugScrollPane.getVerticalScrollBar().addAdjustmentListener(
                new AdjustmentListener()
        {
            public void adjustmentValueChanged(AdjustmentEvent e)
            {
                debugTextPane.setCaretPosition(debugTextPane.getDocument().
                        getLength());
            }
        }
        );

        debugPanel.add(debugScrollPane, BorderLayout.CENTER);

        tPane.addTab("Debug Text", gui.getImageIcon(BUG_ICON), debugPanel,
                "Debug messages are displayed here.");
        tPane.setMnemonicAt(0, KeyEvent.VK_3);

        tPane.setSelectedIndex(0);
        
        // Add the tabbed pane to this panel.
        add(tPane);
        
        statusBar = new StatusBar();
        statusBar.setStatusIcon("gfx/server_disconn16.png",
                "Server disconnected.");
        statusBar.setProgress(false, 0, "");
        this.getContentPane().add(statusBar, BorderLayout.SOUTH);

        this.setVisible(false);

        loginHandler = LoginHandler.getLoginHandler(this, false, false);
        
        LogAppender.getInstance().setTextArea(debugTextPane);
        appendToOutputLn("> Starting the importer (revision "
                + getPrintableKeyword(revision) + ").");
        appendToOutputLn("> Build date: " + getPrintableKeyword(revisionDate));
        appendToOutputLn("> Release date: " + releaseDate);
        
        //System.err.println(isUpgradeRequired());
        
        HistoryHandler historyHandler = HistoryHandler.getHistoryHandler();
        historyPanel.add(historyHandler, BorderLayout.CENTER);
        
        macMenuFix();
        
        //displayLoginDialog(this, true);
    }

    // Check online to see if this is the current version
    boolean isUpgradeRequired()
    {
        ResourceBundle bundle = ResourceBundle.getBundle("omero");
        String version = bundle.getString("omero.version");
        String url = bundle.getString("omero.upgrades.url");
        UpgradeCheck check = new UpgradeCheck(url, version, "importer"); 
        check.run();
        return check.isUpgradeNeeded();
    }
    
    // save ini file and gui settings on exist
    protected void shutdown()
    {
        // Get and save the UI window placement      
        ini.setUIBounds(gui.getUIBounds());
        ini.flushPreferences();
    }
    
    /* Fixes menu issues with the about this app quit functions on mac */
    private void macMenuFix()
    {
        try {

            MacOSMenuHandler handler = new MacOSMenuHandler(this);

            handler.initialize();

            addPropertyChangeListener(this);

        } catch (Throwable e) {}
    }
    
    public boolean displayLoginDialog(Object viewer, boolean modal, boolean displayTop)
    {   
        Image img = Toolkit.getDefaultToolkit().createImage(ICON);
        view = new ScreenLogin(ini.getAppTitle(), gui.getImageIcon("gfx/login_background.png"), img,
                ini.getVersionNumber());
        view.showConnectionSpeed(false);
        viewTop = new ScreenLogo(ini.getAppTitle(), gui.getImageIcon(splash), img);
        viewTop.setStatusVisible(false);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension d = viewTop.getExtendedSize();
        Dimension dlogin = view.getPreferredSize();
        Rectangle r;
        int totalHeight;
        if (displayTop)
        {
            totalHeight = d.height+dlogin.height;
            viewTop.setBounds((screenSize.width-d.width)/2, 
                    (screenSize.height-totalHeight)/2, 
                    d.width, viewTop.getSize().height);
            r = viewTop.getBounds();
            
            viewTop.addPropertyChangeListener((PropertyChangeListener) viewer);
            viewTop.addWindowStateListener((WindowStateListener) viewer);
            viewTop.addWindowFocusListener((WindowFocusListener) viewer); 
            view.setBounds(r.x, r.y+d.height, dlogin.width, dlogin.height);
       } else {
            totalHeight = dlogin.height;
            view.setBounds((screenSize.width-d.width)/2,
                    (screenSize.height-totalHeight)/2, 
                    dlogin.width, dlogin.height);
            view.setQuitButtonText("Canel");
        }
        view.addPropertyChangeListener((PropertyChangeListener) viewer);
        view.addWindowStateListener((WindowStateListener) viewer);
        view.addWindowFocusListener((WindowFocusListener) viewer);
        view.setAlwaysOnTop(false);
        
        
        viewTop.setVisible(displayTop);
        view.setVisible(true);
        
        return true;
    }
    
    /**
     * @param s This method appends data to the output window.
     */
    public void appendToOutput(String s)
    {
        try
        {
            StyledDocument doc = (StyledDocument) outputTextPane.getDocument();
            Style style = doc.addStyle("StyleName", null);
            StyleConstants.setForeground(style, Color.black);
            StyleConstants.setFontFamily(style, "SansSerif");
            StyleConstants.setFontSize(style, 12);
            StyleConstants.setBold(style, false);

            // set to blank before update, this will speed up inserts by 3
            //StyledDocument blank = new DefaultStyledDocument();
            //outputTextPane.setDocument(blank);
            doc.insertString(doc.getLength(), s, style);
            
            //trim the document size so it doesn't grow to big
            int maxChars = 200000;
            if (doc.getLength() > maxChars)
                doc.remove(0, doc.getLength() - maxChars);
            
            //outputTextPane.setDocument(doc);
        } catch (BadLocationException e) {}
    }

    /**
     * @param s Append to the output window and add a line return
     */
    public void appendToOutputLn(String s)
    {
        appendToOutput(s + "\n");
    }

    /**
     * @param s This method appends data to the output window.
     */
    public void appendToDebug(String s)
    {
        log.debug(s);
        try
        {          
            StyledDocument doc = (StyledDocument) debugTextPane.getDocument();
            
            Style style = doc.addStyle("StyleName", null);
            StyleConstants.setForeground(style, Color.black);
            StyleConstants.setFontFamily(style, "SansSerif");
            StyleConstants.setFontSize(style, 12);
            StyleConstants.setBold(style, false);

            // set to blank before update, this will speed up inserts by 3
            //StyledDocument blank = new DefaultStyledDocument();
            //debugTextPane.setDocument(blank);
            doc.insertString(doc.getLength(), s, style);
            
            //trim the document size so it doesn't grow to big
            int maxChars = 200000;
            if (doc.getLength() > maxChars)
                doc.remove(0, doc.getLength() - maxChars);
            
            //debugTextPane.setDocument(doc);
        } catch (BadLocationException e) {}
    }

    /**
     * @param s Append to the output window and add a line return
     */
    public void appendToDebugLn(String s)
    {
        appendToDebug(s + "\n");
    }
    
    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();

        if ("login".equals(cmd))
        {
            if (loggedIn == true)
            {
                setImportEnabled(false);
                loggedIn = false;
                appendToOutputLn("> Logged out.");
                statusBar.setStatusIcon("gfx/server_disconn16.png", "Logged out.");
                loginHandler = null;
            } else 
            {                
                loginHandler = LoginHandler.getLoginHandler(this, true, true);
                loginHandler.displayLogin(false);
                db = HistoryDB.getHistoryDB();
            }
        } else if ("quit".equals(cmd)) {
            if (gui.quitConfirmed(this) == true)
            {
                System.exit(0);
            }
        }
        else if ("about".equals(cmd))
        {
            // HACK - JOptionPane prevents shutdown on dispose
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            About.show(this.getContentPane(), useSplashScreenAbout);
        }
        else if ("comment".equals(cmd))
        {
            new CommentMessenger(this, "OMERO.importer Comment Dialog", true);
        }
        else if ("home".equals(cmd))
        	{
        		BareBonesBrowserLaunch.openURL(HOME_URL);
        	}
    }

    /**
     * @param keyword
     * @return This function strips out the unwanted sections of the keywords
     *         used for the version number and build time variables, leaving
     *         only the stuff we want.
     */
    public static String getPrintableKeyword(String keyword)
    {
        int begin = keyword.indexOf(" ") + 1;
        int end = keyword.lastIndexOf(" ");
        return keyword.substring(begin, end);
    }

    /** Toggles wait cursor. */
    public void waitCursor(boolean wait)
    {
        setCursor(wait ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : null);
    }

    /**
     * @param toggle boolean toggle for the import menu
     */
    public void setImportEnabled(boolean toggle)
    {
        if (toggle == true) login.setText("Logout of the server...");
        else login.setText("Login to the server...");
    }

    /**
     * only allow the exit menu option
     */
    public void onlyAllowExit()
    {
        fileMenu.setEnabled(true);
        helpMenu.setEnabled(true);
    }

    /**
     * @param toggle Enable all menu options
     */
    public void enableMenus(boolean toggle)
    {
        fileMenu.setEnabled(toggle);
        helpMenu.setEnabled(toggle);
    }

    // Getters and Setters

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setPort(String port)
    {
        this.port = port;
    }

    public void setServer(String server)
    {
        this.server = server;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void windowClosing(WindowEvent e)  
    {
        if (gui.quitConfirmed(this) == true)
        {
            System.exit(0);
        }
    }

    public void windowActivated(WindowEvent e)  {}
    public void windowClosed(WindowEvent e)  {}
    public void windowDeactivated(WindowEvent e)  {}
    public void windowDeiconified(WindowEvent e)  {}
    public void windowIconified(WindowEvent e)  {}
    public void windowOpened(WindowEvent e) {}

    /**
     * @param args Start up the application, display the main window and the
     *            login dialog.
     */
    public static void main(String[] args)
    {  

        String laf = UIManager.getSystemLookAndFeelClassName() ;

        //laf = "ch.randelshofer.quaqua.QuaquaLookAndFeel";
        //laf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        //laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        //laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        //laf = "javax.swing.plaf.metal.MetalLookAndFeel";

        if (laf.equals("apple.laf.AquaLookAndFeel") && USE_QUAQUA)
        {
            System.setProperty("Quaqua.design", "panther");
            
            try {
                UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
           } catch (Exception e) { System.err.println(laf + " not supported.");}
        } else {
            try {
                UIManager.setLookAndFeel(laf);
            } catch (Exception e) 
           { System.err.println(laf + " not supported."); }
        }
        
        new Main(args);
    }

    public static Point getSplashLocation()
    {
        return null;
        //return splashLocation;
    }

    public void update(IObservable importLibrary, Object message, Object[] args)
    {
        if (message == Actions.LOADING_IMAGE)
        {
            statusBar.setProgress(true, -1, "Loading file " + args[2] + " of " + args[3]);
            appendToOutput("> [" + args[1] + "] Loading image \"" + args[0] + "\"...\n");
            statusBar.setStatusIcon("gfx/import_icon_16.png", "Prepping file \"" + 
                    args[0] + "\" (file " + args[2] + " of " + args[3] + " imports)");
        }
        if (message == Actions.LOADED_IMAGE)
        {
            statusBar.setProgress(true, -1, "Analyzing file " + args[2] + " of " + args[3]);
            appendToOutput(" Succesfully loaded.\n");
            appendToOutput("> [" + args[1] + "] Importing metadata for " + "image \"" + args[0] + "\"... ");
            statusBar.setStatusIcon("gfx/import_icon_16.png", "Analyzing the metadata for file \"" + 
                    args[0] + "\" (file " + args[2] + " of " + args[3] + " imports)");            
        }
        
        if (message == Actions.DATASET_STORED)
        {
            appendToOutputLn("Successfully stored to dataset \"" + args[4] + "\" with id \"" + args[5] + "\".");
            appendToOutputLn("> [" + args[1] + "] Importing pixel data for " + "image \"" + args[0] + "\"... ");
            statusBar.setProgress(true, 0, "Importing file " + args[2] + " of " + args[3]);
            statusBar.setProgressValue((Integer)args[2] - 1);
            statusBar.setStatusIcon("gfx/import_icon_16.png", "Importing the plane data for file \"" +
                    args[0] + "\" (file " + args[2] + " of " + args[3] + " imports)");
            appendToOutput("> Importing plane: ");
        }
        
        if (message == Actions.DATA_STORED)
        {
            appendToOutputLn("> Successfully stored with pixels id \"" + args[5] + "\".");
            appendToOutputLn("> [" + args[1] + "] Image imported successfully!");
        }
        
        if (message == Actions.IO_EXCEPTION)
        {
            final JOptionPane optionPane = new JOptionPane( 
                    "The importer cannot retrieve one of your images in a timely manner.\n" +
                    "The file in question is:\n'" + args[0] + "'\n\n" +
                    "There are a number of reasons you may see this error:\n" +
                    " - The file has been deleted.\n" +
                    " - There was a networking error retrieving a remotely saved file.\n" +
                    " - An archived file has not been fully retrieved from backup.\n\n" +
                    "The importer will now try to continue with the remainer of your imports.\n",
                    JOptionPane.ERROR_MESSAGE);
            
            final JDialog dialog = new JDialog(this, "IO Error");
            dialog.setAlwaysOnTop(true);
            dialog.setContentPane(optionPane);
            dialog.pack();
            dialog.setVisible(true);
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        String name = evt.getPropertyName();
        if (ScreenLogin.LOGIN_PROPERTY.equals(name)) {
            LoginCredentials lc = (LoginCredentials) evt.getNewValue();
            if (lc != null) login(lc);
        } else if (ScreenLogin.QUIT_PROPERTY.equals(name) || name.equals("quitpplication")) {
            if (gui.quitConfirmed(this) == true)
            {
                System.exit(0);
            }
        } else if (ScreenLogin.TO_FRONT_PROPERTY.equals(name) || 
                ScreenLogo.MOVE_FRONT_PROPERTY.equals(name)) {
            //updateView();
        } else if (name.equals("aboutApplication"))
        {
            // HACK - JOptionPane prevents shutdown on dispose
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            About.show(this.getContentPane(), useSplashScreenAbout);
        }
        
        
    }

    public void login(LoginCredentials lc) {}
    public void windowStateChanged(WindowEvent arg0) {}
    public void windowGainedFocus(WindowEvent arg0) {}
    public void windowLostFocus(WindowEvent arg0) {}
}