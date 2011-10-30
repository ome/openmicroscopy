/*
 * ome.formats.importer.gui.GuiCommonElements
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.Version;
import ome.formats.importer.util.BareBonesBrowserLaunch;
import ome.formats.importer.util.IniFileLoader;
import ome.formats.importer.util.LogAppenderProxy;
import ome.formats.importer.util.ErrorHandler.EXCEPTION_EVENT;
import ome.formats.importer.util.ErrorHandler.FILE_EXCEPTION;
import ome.formats.importer.util.ErrorHandler.INTERNAL_EXCEPTION;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.openmicroscopy.shoola.util.ui.MacOSMenuHandler;
import org.openmicroscopy.shoola.util.ui.login.LoginCredentials;
import org.openmicroscopy.shoola.util.ui.login.ScreenLogin;
import org.openmicroscopy.shoola.util.ui.login.ScreenLogo;

/**
 * @author Brian W. Loranger
 */
public class GuiImporter extends JFrame 
implements  ActionListener, WindowListener, IObserver, PropertyChangeListener, 
WindowStateListener, WindowFocusListener
{
    private static final String show_log_file = "show_log_file_location";

    /**
     * Due to the static initialization required by {@link LogAppenderProxy}
     * no logging should be performed before {@link LogAppenderProxy#configure(ImportConfig)}
     * is called.
     */
    private static Log          log     = LogFactory.getLog(GuiImporter.class);

    // -- Constants --
    private final static boolean useSplashScreenAbout   = false;
    static boolean USE_QUAQUA = false;


    public final static String TITLE            = "OMERO.importer";

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
    public static final String CONFIG_ICON = "gfx/nuvola_configure16.png";
    public static final String ERROR_ICON_ANIM = "gfx/warning_msg16_anim.gif";
    public static final String ERROR_ICON = "gfx/warning_msg16.png";
    public static final String LOGFILE_ICON = "gfx/nuvola_output16.png";
    public static final String FORUM_ICON = "gfx/nuvola_chat16.png";

    private ImportConfig         config;
    private ErrorHandler         errorHandler;
    private FileQueueHandler     fileQueueHandler;
    private StatusBar            statusBar;

    private LoginHandler         loginHandler;
    private HistoryHandler       historyHandler;
    private HistoryTable         historyTable;

    private JMenuBar            menubar;
    private JMenu               fileMenu;
    private JMenuItem           options;
    private JMenuItem           fileQuit;
    private JMenuItem           login;
    private JMenu               helpMenu;
    private JMenuItem           helpComment;
    private JMenuItem			helpForums;
    private JMenuItem           helpHome;
    private JMenuItem           helpAbout;

    private Boolean              loggedIn;

    private JTextPane           outputTextPane;
    private JTextPane           debugTextPane;
    private JPanel              historyPanel;
    private JPanel              errorPanel;

    JTabbedPane         tPane;
    final int historyTabIndex = 1;

    private boolean errors_pending = false; // used to change error icon on tab
	private boolean error_notification = false; // used if unsent errors on quit

    private ScheduledExecutorService scanEx = Executors.newScheduledThreadPool(1);
    private ScheduledExecutorService importEx = Executors.newScheduledThreadPool(1);
    
    private Rectangle bounds;

    /**
     * Main entry class for the application
     * 
     * @param config - ImportConfig file
     */
    public GuiImporter(ImportConfig config)
    {
        //super(TITLE);
    	
        //javax.swing.ToolTipManager.sharedInstance().setDismissDelay(0);

        this.setConfig(config);
        this.bounds = config.getUIBounds();
        
        Level level = org.apache.log4j.Level.toLevel(config.getDebugLevel());
        LogAppender.setLoggingLevel(level);

        historyHandler = new HistoryHandler(this);
        setHistoryTable(historyHandler.table);

        // Add a shutdown hook for when app closes
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                log.debug("Running shutdown hook.");
                shutdown();
            }
        });

        // Set app defaults
        setTitle(config.getAppTitle());
        setIconImage(GuiCommonElements.getImageIcon(GuiImporter.ICON).getImage());
        setPreferredSize(new Dimension(bounds.width, bounds.height));
        setSize(bounds.width, bounds.height);
        setLocation(bounds.x, bounds.y);      
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        pack();

        addWindowListener(this);

        // capture move info
        addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent evt) {
                bounds = getBounds();
            }
        });

        // capture resize info
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                bounds = getBounds();               
            }
        });

        // menu bar
        menubar = new JMenuBar();
        fileMenu = new JMenu("File");
        menubar.add(fileMenu);
        
        login = new JMenuItem("Login to the server...", GuiCommonElements.getImageIcon(LOGIN_ICON));
        login.setActionCommand("login");
        login.addActionListener(this);        
        fileMenu.add(login);
        options = new JMenuItem("Options...", GuiCommonElements.getImageIcon(CONFIG_ICON));
        options.setActionCommand("options");
        options.addActionListener(this);        
        fileMenu.add(options);
        fileQuit = new JMenuItem("Quit", GuiCommonElements.getImageIcon(QUIT_ICON));
        fileQuit.setActionCommand("quit");
        fileQuit.addActionListener(this);
        fileMenu.add(fileQuit);
        helpMenu = new JMenu("Help");
        menubar.add(helpMenu);
        helpComment = new JMenuItem("Send a Comment...", GuiCommonElements.getImageIcon(COMMENT_ICON));
        helpComment.setActionCommand("comment");
        helpComment.addActionListener(this);
        helpHome = new JMenuItem("Visit Importer Homepage...", GuiCommonElements.getImageIcon(HOME_ICON));
        helpHome.setActionCommand("home");
        helpHome.addActionListener(this);
        helpForums = new JMenuItem("Visit the OMERO Forums...", GuiCommonElements.getImageIcon(FORUM_ICON));
        helpForums.setActionCommand("forums");
        helpForums.addActionListener(this);
        helpAbout = new JMenuItem("About the Importer...", GuiCommonElements.getImageIcon(ABOUT_ICON));
        helpAbout.setActionCommand("about");
        helpAbout.addActionListener(this);
        helpMenu.add(helpComment);
        helpMenu.add(helpHome);
        helpMenu.add(helpForums);
        // Help --> Show log file location...
        JMenuItem helpShowLog = new JMenuItem("Show log file location...", GuiCommonElements.getImageIcon(LOGFILE_ICON));
        helpShowLog.setActionCommand(show_log_file);
        helpShowLog.addActionListener(this);
        helpMenu.add(helpShowLog);
        helpMenu.add(helpAbout);
        // Help --> About
        setJMenuBar(menubar);

        // tabbed panes
        tPane = new JTabbedPane();
        tPane.setOpaque(false); // content panes must be opaque

        // file chooser pane
        JPanel filePanel = new JPanel(new BorderLayout());

        setStatusBar(new StatusBar());
        getStatusBar().setStatusIcon("gfx/server_disconn16.png",
        "Server disconnected.");
        getStatusBar().setProgress(false, 0, "");
        this.getContentPane().add(getStatusBar(), BorderLayout.SOUTH);

        // The file chooser sub-pane
        setFileQueueHandler(new FileQueueHandler(scanEx, importEx, this, config));
        //splitPane.setResizeWeight(0.5);

        filePanel.add(getFileQueueHandler(), BorderLayout.CENTER);
        tPane.addTab("File Chooser", GuiCommonElements.getImageIcon(CHOOSER_ICON), filePanel,
        "Add and delete images here to the import queue.");
        tPane.setMnemonicAt(0, KeyEvent.VK_1);

        // history pane
        historyPanel = new JPanel();
        historyPanel.setOpaque(false);
        historyPanel.setLayout(new BorderLayout());

        tPane.addTab("Import History", GuiCommonElements.getImageIcon(HISTORY_ICON), historyPanel,
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
                		try {
                			outputTextPane.setCaretPosition(outputTextPane.getDocument().getLength());
                		} catch (IllegalArgumentException e1) {
                			log.error("Error setting cursor:" + e1);
                		}
                	}
                }
        );

        outputPanel.add(outputScrollPane, BorderLayout.CENTER);

        tPane.addTab("Output Text", GuiCommonElements.getImageIcon(OUTPUT_ICON), outputPanel,
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
                    	try {
                        debugTextPane.setCaretPosition(debugTextPane.getDocument().getLength());
                    	} catch (IllegalArgumentException e1) {
                    		log.error("Error setting cursor:" + e1);
                    	}
                    }
                }
        );

        debugPanel.add(debugScrollPane, BorderLayout.CENTER);

        tPane.addTab("Debug Text", GuiCommonElements.getImageIcon(BUG_ICON), debugPanel,
        "Debug messages are displayed here.");
        tPane.setMnemonicAt(0, KeyEvent.VK_3);

        // Error Pane
        errorPanel = new JPanel();
        errorPanel.setOpaque(false);
        errorPanel.setLayout(new BorderLayout());

        tPane.addTab("Import Errors", GuiCommonElements.getImageIcon(ERROR_ICON), errorPanel,
        "Import errors are displayed here.");
        tPane.setMnemonicAt(0, KeyEvent.VK_5);

        tPane.setSelectedIndex(0);

        if (getHistoryTable().db.historyEnabled == false)
        	tPane.setEnabledAt(historyTabIndex,false);
        
        // Add the tabbed pane to this panel.
        add(tPane);

        this.setVisible(false);

        historyPanel.add(historyHandler, BorderLayout.CENTER);
        tPane.setEnabledAt(historyTabIndex,false);

        setLoginHandler(new LoginHandler(this, getHistoryTable()));

        LogAppender.getInstance().setTextArea(debugTextPane);
        appendToOutputLn("> Starting the importer (revision "
                + getPrintableKeyword(Version.revision) + ").");
        appendToOutputLn("> Build date: " + getPrintableKeyword(Version.revisionDate));
        appendToOutputLn("> Release date: " + Version.releaseDate);

        // TODO : should this be a third executor?
        setErrorHandler(new ErrorHandler(importEx, config));
        getErrorHandler().addObserver(this);
        errorPanel.add(getErrorHandler(), BorderLayout.CENTER);
        
        macMenuFix();

        //displayLoginDialog(this, true);
    }

    /**
     * Check if the history table is enabled, disabling the history tab 
     * if it is not.
     */
    void checkHistoryEnable() {
        tPane.addMouseListener(new MouseAdapter() {
        	
        	public void mouseClicked(MouseEvent e) {
        		
            	JTabbedPane pane = (JTabbedPane) e.getSource();
        		
            	boolean userDisabled = config.getUserDisableHistory() | config.getStaticDisableHistory();
            	
        		if (pane.indexAtLocation(e.getX(), e.getY()) == historyTabIndex 
        				&& getHistoryTable().db.historyEnabled == false)
				{
					if (HistoryDB.alertOnce == false && !userDisabled)
					{
						JOptionPane.showMessageDialog(null,
								"For some reason we are not able to connect to the remote\n" +
								"history service (this most likely means the server does\n" +
								"not have this feature installed). In the meantime, you will\n" +
								"still be able to use the importer, however the history tab's\n" +
								"functionality will not be enabled.",
								"Warning",
								JOptionPane.ERROR_MESSAGE);
						HistoryDB.alertOnce = true;
					}        		
				}
        	}
        });
	}

	/**
     * save ini file and gui settings on exist
     */
    protected void shutdown()
    {
        log.debug("Shutdown called");
                
        importEx.shutdown();
        scanEx.shutdown();

        // How do I know an import is running here and how do I cancel it?
        waitOnExecutor("Import", importEx, 60);
        waitOnExecutor("Scanning", scanEx, 60);
  
        try {
            getLoginHandler().logout();
        } catch (Exception e) {
            log.warn("Exception on metadatastore.logout()", e);
        }

        // Get and save the UI window placement
        try {
            getConfig().setUIBounds(bounds);
        } finally {
            getConfig().saveAll();
            getConfig().saveGui();
        }
    }

    /**
     * @param msg
     * @param ex
     * @param secs
     */
    private void waitOnExecutor(String msg, ScheduledExecutorService ex,
            int secs) {
        try {
            boolean terminated = importEx.awaitTermination(secs,
                    TimeUnit.SECONDS);
            if (!terminated) {
                log.error(msg + " still running!");
            }
        } catch (Exception e) {
            log.warn("Exception on awaitTermination of " + msg, e);
        }
    }

    /**
     * Fixes menu issues with the about this app quit functions on mac
     */
    private void macMenuFix()
    {
        try {

            MacOSMenuHandler handler = new MacOSMenuHandler(this);

            handler.initialize();

            addPropertyChangeListener(this);

        } catch (Throwable e) {}
    }

    /**
     * This method appends data to the output window
     * 
     * @param s - text to append
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

            doc.insertString(doc.getLength(), s, style);

            //trim the document size so it doesn't grow to big
            int maxChars = 200000;
            if (doc.getLength() > maxChars)
                doc.remove(0, doc.getLength() - maxChars);

            //outputTextPane.setDocument(doc);
        } catch (BadLocationException e) {}
    }

    /**
     * Append to the output window and add a line return
     * 
     * @param s - text to append
     */
    public void appendToOutputLn(String s)
    {
        appendToOutput(s + "\n");
    }

    /**
     * This method appends data to the output window.
     * 
     * @param s - string to append
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
           
            doc.insertString(doc.getLength(), s, style);

            //trim the document size so it doesn't grow to big
            int maxChars = 200000;
            if (doc.getLength() > maxChars)
                doc.remove(0, doc.getLength() - maxChars);

            //debugTextPane.setDocument(doc);
        } catch (BadLocationException e) {}
    }

    /**
     * Append to the output window and add a line return
     * 
     * @param s - string to append
     */
    public void appendToDebugLn(String s)
    {
        appendToDebug(s + "\n");
    }

    /**
     * Handle action events
     * 
     * @param event
     */
    public void actionPerformed(ActionEvent event)
    {
        String cmd = event.getActionCommand();

        if ("login".equals(cmd))
        {
            if (getLoggedIn() == true)
            {
                logout();
                getLoginHandler().logout();
                setLoginHandler(null);
                showLogoutMessage();
            } else {
                HistoryTable table = null;
                if (historyHandler != null) {
                    table = historyHandler.table;
                }
                setLoginHandler(new LoginHandler(this, table, true, false));
                //loginHandler.displayLogin(false);
            }
        } else if ("quit".equals(cmd)) {
        	String message = null;
        	if(error_notification == true)
        		message = "Do you really want to close the application?\n" +
                "Doing so will cancel any running imports.\n\n" +
                "NOTE: You still have unsent error messages!";
        	
            if (GuiCommonElements.quitConfirmed(this, message) == true)
            {
                try {
                    // Save login screen groups
                    ScreenLogin.registerGroup(getLoginHandler().getMetadataStore().mapUserGroups());
                } catch (Exception e) {
                    log.warn("Exception on ScreenLogin.registerGroup()", e);
                }
                
            	//if (loggedIn)
            	//	loginHandler.logout();
                System.exit(0);
            }
        } else if ("options".equals(cmd)) {
           final OptionsDialog dialog = new OptionsDialog(config, this, "Import", true);
        }
        else if ("about".equals(cmd))
        {
            // HACK - JOptionPane prevents shutdown on dispose
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            About.show(this, getConfig(), useSplashScreenAbout);
        }
        else if ("comment".equals(cmd))
        {
            new CommentMessenger(this, "OMERO.importer Comment Dialog", getConfig(), true, false);
        }
        else if ("home".equals(cmd))
        {
            BareBonesBrowserLaunch.openURL(getConfig().getHomeUrl());
        }
        else if ("forums".equals(cmd))
        {
            BareBonesBrowserLaunch.openURL(getConfig().getForumUrl());
        }
        else if (show_log_file.equals(cmd))
        {
            File path = new File(getConfig().getUserSettingsDirectory());
            try
            {
                String url = path.toURI().toURL().toString();
                url = url.replaceAll("^file:/", "file:///");
                BareBonesBrowserLaunch.openURL(url);
            }
            catch (MalformedURLException ex)
            {
                log.error("Error while transforming URL for: " 
                        + path.getAbsolutePath(), ex);
            }
        }

    }

    private void showLogoutMessage() {
		JOptionPane.showMessageDialog(this,
				"You have been logged out of the importer.\n" +
				"Choose 'login' from the file menu to continue.",
				"Warning",
				JOptionPane.WARNING_MESSAGE);
	}

	/**
     * This function strips out the unwanted sections of the keywords
     * used for the version number and build time variables, leaving
     * only the stuff we want.
     *         
     * @param keyword
     * @return parsed String
     */
    public static String getPrintableKeyword(String keyword)
    {
        int begin = keyword.indexOf(" ") + 1;
        int end = keyword.lastIndexOf(" ");
        return keyword.substring(begin, end);
    }

    /**
     * Toggles wait cursor.
     * 
     * @param wait
     */
    public void waitCursor(boolean wait)
    {
        setCursor(wait ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : null);
    }

    /**
     * Toggle the import login/logout menu item
     * 
     * @param toggle - boolean toggle (true is logged in)
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

    private void logout()
    {
    	setImportEnabled(false);
        setLoggedIn(false);
        tPane.setEnabledAt(historyTabIndex,false);
        appendToOutputLn("> Logged out.");
        getStatusBar().setStatusIcon("gfx/server_disconn16.png", "Logged out.");
        getFileQueueHandler().enableImports(false);
        
        try {
            // Save login screen groups
            ScreenLogin.registerGroup(getLoginHandler().getMetadataStore().mapUserGroups());
        } catch (Exception e) {
            log.warn("Exception on ScreenLogin.registerGroup()", e);
        }
    }
    
    /**
     * @return
     */
    public static Point getSplashLocation()
    {
        return null;
        //return splashLocation;
    }

    /* (non-Javadoc)
     * @see ome.formats.importer.IObserver#update(ome.formats.importer.IObservable, ome.formats.importer.ImportEvent)
     */
    public void update(IObservable importLibrary, ImportEvent event)
    {

    	// Keep alive has failed, call logout
    	if (event instanceof ImportEvent.LOGGED_OUT)
    	{
    		logout();
    		showLogoutMessage();
    	}
    	    	
        if (event instanceof ImportEvent.LOADING_IMAGE)
        {
            ImportEvent.LOADING_IMAGE ev = (ImportEvent.LOADING_IMAGE) event;

            getStatusBar().setProgress(true, -1, "Loading file " + ev.numDone + " of " + ev.total);
            appendToOutput("> [" + ev.index + "] Loading image \"" + ev.shortName + "\"...\n");
            getStatusBar().setStatusIcon("gfx/import_icon_16.png", "Prepping file \"" + ev.shortName);
        }

        else if (event instanceof ImportEvent.LOADED_IMAGE)
        {
            ImportEvent.LOADED_IMAGE ev = (ImportEvent.LOADED_IMAGE) event;

            getStatusBar().setProgress(true, -1, "Analyzing file " + ev.numDone + " of " + ev.total);
            appendToOutput(" Succesfully loaded.\n");
            appendToOutput("> [" + ev.index + "] Importing metadata for " + "image \"" + ev.shortName + "\"... ");
            getStatusBar().setStatusIcon("gfx/import_icon_16.png", "Analyzing the metadata for file \"" + ev.shortName);            
        }

        else if (event instanceof ImportEvent.BEGIN_SAVE_TO_DB) {
            ImportEvent.BEGIN_SAVE_TO_DB ev = (ImportEvent.BEGIN_SAVE_TO_DB) event;
            appendToOutput("> [" + ev.index + "] Saving metadata for " + "image \"" + ev.filename + "\"... ");
            getStatusBar().setStatusIcon("gfx/import_icon_16.png", "Saving metadata for file \"" + ev.filename);
        }
        
        else if (event instanceof ImportEvent.DATASET_STORED)
        {
            ImportEvent.DATASET_STORED ev = (ImportEvent.DATASET_STORED) event;

            int num = ev.numDone;
            int tot = ev.total;
            int pro = num - 1;
            appendToOutputLn("Successfully stored to "+ev.target.getClass().getSimpleName()+" \"" + 
                    ev.filename + "\" with id \"" + ev.target.getId().getValue() + "\".");
            appendToOutputLn("> [" + ev.series + "] Importing pixel data for " + "image \"" + ev.filename + "\"... ");
            getStatusBar().setProgress(true, 0, "Importing file " + num + " of " + tot);
            getStatusBar().setProgressValue(pro);
            getStatusBar().setStatusIcon("gfx/import_icon_16.png", "Importing the pixel data for file \"" + ev.filename);
            appendToOutput("> Importing plane: ");
        }

        else if (event instanceof ImportEvent.DATA_STORED)
        {
            ImportEvent.DATA_STORED ev = (ImportEvent.DATA_STORED) event;

            appendToOutputLn("> Successfully stored with pixels id \"" + ev.pixId + "\".");
            appendToOutputLn("> [" + ev.filename + "] Image imported successfully!");
        }

        else if (event instanceof FILE_EXCEPTION)
        {
            FILE_EXCEPTION ev = (FILE_EXCEPTION) event;
            if (IOException.class.isAssignableFrom(ev.exception.getClass())) {

                final JOptionPane optionPane = new JOptionPane( 
                        "The importer cannot retrieve one of your images in a timely manner.\n" +
                        "The file in question is:\n'" + ev.filename + "'\n\n" +
                        "There are a number of reasons you may see this error:\n" +
                        " - The file has been deleted.\n" +
                        " - There was a networking error retrieving a remotely saved file.\n" +
                        " - An archived file has not been fully retrieved from backup.\n\n" +
                        "The importer should now continue with the remainer of your imports.\n",
                        JOptionPane.ERROR_MESSAGE);

                final JDialog dialog = new JDialog(this, "IO Error");
                dialog.setAlwaysOnTop(true);
                dialog.setContentPane(optionPane);
                dialog.pack();
                dialog.setVisible(true);
                optionPane.addPropertyChangeListener(
                        new PropertyChangeListener() {
                            public void propertyChange(PropertyChangeEvent e) {
                                String prop = e.getPropertyName();

                                if (dialog.isVisible() 
                                 && (e.getSource() == optionPane)
                                 && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                                    dialog.dispose();
                                }
                            }
                        });
            }
        }

        else if (event instanceof EXCEPTION_EVENT)
        {
            EXCEPTION_EVENT ev = (EXCEPTION_EVENT) event;
            log.error("EXCEPTION_EVENT", ev.exception);
        }

        else if (event instanceof INTERNAL_EXCEPTION)
        {
            INTERNAL_EXCEPTION e = (INTERNAL_EXCEPTION) event;
            log.error("INTERNAL_EXCEPTION", e.exception);

            // What else should we do here? Why are EXCEPTION_EVENTs being
            // handled here?
        }

        else if (event instanceof ImportEvent.ERRORS_PENDING)
        {
            tPane.setIconAt(4, GuiCommonElements.getImageIcon(ERROR_ICON_ANIM));
            errors_pending  = true;
            error_notification  = true;
        }

        else if (event instanceof ImportEvent.ERRORS_COMPLETE)
        {
            tPane.setIconAt(4, GuiCommonElements.getImageIcon(ERROR_ICON));
            error_notification  = false;
        }

        else if (event instanceof ImportEvent.ERRORS_COMPLETE)
        {
            tPane.setIconAt(4, GuiCommonElements.getImageIcon(ERROR_ICON));
            error_notification  = false;
        }
        
        else if (event instanceof ImportEvent.ERRORS_FAILED)
        {
            sendingErrorsFailed(this);
        }
        
        else if (event instanceof ImportEvent.IMPORT_QUEUE_DONE && errors_pending == true)
        {
            errors_pending = false;
            importErrorsCollected(this); 
        }

    }

    /**
     * Display errors in import dialog 
     * 
     * @param frame - parent frame
     */
    private void importErrorsCollected(Component frame)
    {
        final JOptionPane optionPane = new JOptionPane("\nYour import has produced one or more errors, "
                + "\nvisit the 'Import Errors' tab for details.", JOptionPane.WARNING_MESSAGE);
        final JDialog errorDialog = new JDialog(this, "Errors Collected", false);
        errorDialog.setContentPane(optionPane);

        optionPane.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent e) {
                        String prop = e.getPropertyName();

                        if (errorDialog.isVisible() 
                                && (e.getSource() == optionPane)
                                && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                            errorDialog.dispose();
                        }
                    }
                });

        errorDialog.toFront();
        errorDialog.pack();
        errorDialog.setLocationRelativeTo(frame);
        errorDialog.setVisible(true);
    }

    /**
     * Display errors in candidates dialog
     * 
     * @param frame - parent frame
     */
    public void candidateErrorsCollected(Component frame)
    {
        errors_pending = false;
        final JOptionPane optionPane = new JOptionPane("\nAdding these files to the queue has produced one or more errors and some"
                + "\n files will not be displayed on the queue. View the 'Import Errors' tab for details.", JOptionPane.WARNING_MESSAGE);
        final JDialog errorDialog = new JDialog(this, "Errors Collected", true);
        errorDialog.setContentPane(optionPane);

        optionPane.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent e) {
                        String prop = e.getPropertyName();

                        if (errorDialog.isVisible() 
                                && (e.getSource() == optionPane)
                                && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                            errorDialog.dispose();
                        }
                    }
                });

        errorDialog.toFront();
        errorDialog.pack();
        errorDialog.setLocationRelativeTo(frame);
        errorDialog.setVisible(true);
    }

    /**
     * Display failed sending errors dialog
     * 
     * @param frame - parent frame
     */
    public void sendingErrorsFailed(Component frame)
    {
        final JOptionPane optionPane = new JOptionPane("\nDue to an error we were not able to send your error messages." +
        		"\nto our feedback server. Please try again.", JOptionPane.WARNING_MESSAGE);
        
        final JDialog failedDialog = new JDialog(this, "Feedback Failed!", true);
        
        failedDialog.setContentPane(optionPane);

        optionPane.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent e) {
                        String prop = e.getPropertyName();

                        if (failedDialog.isVisible() 
                                && (e.getSource() == optionPane)
                                && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                            failedDialog.dispose();
                        }
                    }
                });

        failedDialog.toFront();
        failedDialog.pack();
        failedDialog.setLocationRelativeTo(frame);
        failedDialog.setVisible(true);
    }
        
	private static void LoggingDisabledNotification() {
		JOptionPane.showMessageDialog(null,
				"The importer was unable to access its local log file\n" +
				"which is normally located in your home directory under\n" +
				" the sub-directory omero/logs.\n\n" +
				"The importer will continue to operate normally, but will\n" +
				"be unable to record any information to this file.\n",
				"Warning",
				JOptionPane.ERROR_MESSAGE);
    }
    
    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String name = evt.getPropertyName();
        if (ScreenLogin.LOGIN_PROPERTY.equals(name)) {
            LoginCredentials lc = (LoginCredentials) evt.getNewValue();
            if (lc != null) login(lc);
        } else if (ScreenLogin.QUIT_PROPERTY.equals(name) || name.equals("quitpplication")) {
            if (GuiCommonElements.quitConfirmed(this, null) == true)
            {
                System.exit(0);
            }
        } else if (ScreenLogin.TO_FRONT_PROPERTY.equals(name) || 
                ScreenLogo.MOVE_FRONT_PROPERTY.equals(name)) {
            //updateView();
        } else if (name.equals("aboutApplication"))
        {
            // HACK - JOptionPane prevents shutdown on dispose
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            About.show(this, getConfig(), useSplashScreenAbout);
        }


    }

    /**
     * Log in using loging credentials supplied
     * @param lc - login credentials
     */
    public void login(LoginCredentials lc) {}
    
    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
     */
    public void windowClosing(WindowEvent e)  
    {
    	String message = null;
    	if(error_notification == true)
    		message = "Do you really want to quit?\n" +
            "Doing so will cancel any running imports.\n\n" +
            "NOTE: You still have unsent error messages!";
    	
        if (GuiCommonElements.quitConfirmed(this, null) == true)
        {
            try {
                // Save login screen groups
                ScreenLogin.registerGroup(getLoginHandler().getMetadataStore().mapUserGroups());
            } catch (Exception ex) {
                log.warn("Exception on ScreenLogin.registerGroup()", ex);
            }
            
            System.exit(0);
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
     */
    public void windowActivated(WindowEvent e)  {}
    
    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
     */
    public void windowClosed(WindowEvent e)  {}
    
    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
     */
    public void windowDeactivated(WindowEvent e)  {}
    
    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
     */
    public void windowDeiconified(WindowEvent e)  {}
    
    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
     */
    public void windowIconified(WindowEvent e)  {}
    
    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
     */
    public void windowOpened(WindowEvent e) {}
    
    /* (non-Javadoc)
     * @see java.awt.event.WindowStateListener#windowStateChanged(java.awt.event.WindowEvent)
     */
    public void windowStateChanged(WindowEvent arg0) {}
    
    /* (non-Javadoc)
     * @see java.awt.event.WindowFocusListener#windowGainedFocus(java.awt.event.WindowEvent)
     */
    public void windowGainedFocus(WindowEvent arg0) {}
    
    /* (non-Javadoc)
     * @see java.awt.event.WindowFocusListener#windowLostFocus(java.awt.event.WindowEvent)
     */
    public void windowLostFocus(WindowEvent arg0) {}
    
	/**
	 * @param historyTable the historyTable to set
	 */
	public void setHistoryTable(HistoryTable historyTable) {
		this.historyTable = historyTable;
	}

	/**
	 * @return the historyTable
	 */
	public HistoryTable getHistoryTable() {
		return historyTable;
	}

	/**
	 * @param loginHandler the loginHandler to set
	 */
	public void setLoginHandler(LoginHandler loginHandler) {
		this.loginHandler = loginHandler;
	}

	/**
	 * @return the loginHandler
	 */
	public LoginHandler getLoginHandler() {
		return loginHandler;
	}

	/**
	 * @param loggedIn the loggedIn to set
	 */
	public void setLoggedIn(Boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	/**
	 * @return the loggedIn
	 */
	public Boolean getLoggedIn() {
		return loggedIn;
	}

	/**
	 * @param statusBar the statusBar to set
	 */
	public void setStatusBar(StatusBar statusBar) {
		this.statusBar = statusBar;
	}

	/**
	 * @return the statusBar
	 */
	public StatusBar getStatusBar() {
		return statusBar;
	}

	/**
	 * @param errorHandler the errorHandler to set
	 */
	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	/**
	 * @return the errorHandler
	 */
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	/**
	 * @param config the config to set
	 */
	public void setConfig(ImportConfig config) {
		this.config = config;
	}

	/**
	 * @return the config
	 */
	public ImportConfig getConfig() {
		return config;
	}

	/**
	 * @param fileQueueHandler the fileQueueHandler to set
	 */
	public void setFileQueueHandler(FileQueueHandler fileQueueHandler) {
		this.fileQueueHandler = fileQueueHandler;
	}

	/**
	 * @return the fileQueueHandler
	 */
	public FileQueueHandler getFileQueueHandler() {
		return fileQueueHandler;
	}
    
    /**
     * Start up the application, display the main window and the login dialog.
     *            
     * @param args 
     */
	public static void main(String[] args)
	{  
		try {
			LogAppenderProxy.configure(new File(IniFileLoader.LOGFILE));
		} catch (Exception e) {
			GuiImporter.LoggingDisabledNotification();
		}
		ImportConfig config = new ImportConfig(args.length > 0 ? new File(args[0]) : null);
		config.configureDebug(null); // Uses ini
        
        config.loadAll();
        config.loadGui();
        USE_QUAQUA = config.getUseQuaqua();
        
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
        
        /* Alternative GTK file chooser
        if ("GTK look and feel".equals(UIManager.getLookAndFeel().getName())) {
            UIManager.put("FileChooserUI", "eu.kostia.gtkjfilechooser.ui.GtkFileChooserUI");
          }
         */

        new GuiImporter(config);
    }
}