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
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Brian W. Loranger
 */

public class Main extends JFrame implements ActionListener, WindowListener
{
    private static final long   serialVersionUID = 1228000122345370913L;

    /** The data of the last release date. */
    public static String        releaseDate      
         = "2006-11-10 10:40:46 +0000 (Fri, 10 Nov 2006)";

    /** The repository revision. */
    public static String        revision  = "$LastChangedRevision$";

    /** The data of the last repository revision. */
    public static String        revisionDate     
         = "$LastChangedDate$";

    
    /** Logger for this class. */
    @SuppressWarnings("unused")
    private static Log          log     = LogFactory.getLog(Main.class);

    // -- Constants --

    private final static String TITLE            = "OMERO Importer";
    public final static String splash           = "gfx/Splash.png";
    private final static boolean useSplashScreenAbout   = false;
     
    private final static int width = 980;
    private final static int height = 580;
    
    public LoginHandler        loginHandler;

    public StatusBar            statusBar;

    private JMenu               fileMenu;

    private JMenu               helpMenu;
  
    private JMenuItem           login;
    
    public Boolean             loggedIn;

    private JTextPane           outputTextPane;

    private JTextPane           debugTextPane;

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
    public Main()
    {
        super(TITLE);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());
        setContentPane(pane);
        setPreferredSize(new Dimension(width, height)); // default size
        pack();
        setLocationRelativeTo(null);
        addWindowListener(this);

        // menu bar
        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);
        fileMenu = new JMenu("File");
        menubar.add(fileMenu);
        login = new JMenuItem("Login...");
        login.setActionCommand("login");
        login.addActionListener(this);        
        fileMenu.add(login);
        JMenuItem fileExit = new JMenuItem("Exit");
        fileExit.setActionCommand("exit");
        fileExit.addActionListener(this);
        fileMenu.add(fileExit);
        helpMenu = new JMenu("Help");
        menubar.add(helpMenu);
        JMenuItem helpComment = new JMenuItem("Send Comment...");
        helpComment.setActionCommand("comment");
        helpComment.addActionListener(this);
        JMenuItem helpAbout = new JMenuItem("About...");
        helpAbout.setActionCommand("about");
        helpAbout.addActionListener(this);
        helpMenu.add(helpComment);
        helpMenu.add(helpAbout);


        // tabbed panes
        JTabbedPane tPane = new JTabbedPane();
        tPane.setOpaque(false); // content panes must be opaque

        // file chooser pane
        JPanel filePanel = new JPanel(new BorderLayout());

        // The file chooser sub-pane
        FileQueueHandler fileQueueHandler = new FileQueueHandler(this);
        //splitPane.setResizeWeight(0.5);

        filePanel.add(fileQueueHandler, BorderLayout.CENTER);
        tPane.addTab("File Viewer", null, filePanel,
        "Add and delete images here to the import queue.");
        tPane.setMnemonicAt(0, KeyEvent.VK_1);
        
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

        tPane.addTab("Output Text", null, outputPanel,
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

        tPane.addTab("Debug Text", null, debugPanel,
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

        this.setVisible(true);

        LogAppender.getInstance().setTextArea(debugTextPane);
        appendToOutputLn("> Starting the importer (revision "
                + getPrintableKeyword(revision) + ").");
        appendToOutputLn("> Build date: " + getPrintableKeyword(revisionDate));
        appendToOutputLn("> Release date: " + releaseDate);
        
        loginHandler = new LoginHandler(this);;
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

            doc.insertString(doc.getLength(), s, style);
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
        try
        {
            StyledDocument doc = (StyledDocument) debugTextPane.getDocument();
            Style style = doc.addStyle("StyleName", null);
            StyleConstants.setForeground(style, Color.black);
            StyleConstants.setFontFamily(style, "SansSerif");
            StyleConstants.setFontSize(style, 12);
            StyleConstants.setBold(style, false);

            doc.insertString(doc.getLength(), s, style);
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
                loginHandler = new LoginHandler(this);
                //store = loginHandler.getMetadataStore();
                //loginHandler.tryLogin(this);
                //store = loginHandler.getMetadataStore();
            }
        } else if ("exit".equals(cmd)) {
            if (quitConfirmed(this) == true)
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
            new CommentMessenger(this, "Comment Dialog", true);
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
        if (toggle == true) login.setText("Logout...");
        else login.setText("Login...");
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

    private boolean quitConfirmed(JFrame frame) {
        String s1 = "Quit";
        String s2 = "Cancel";
        Object[] options = {s1, s2};
        int n = JOptionPane.showOptionDialog(frame,
                "Do you really want to quit?\n" +
                "Doing so will cancel any running imports.",
                "Quit Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                s1);
        if (n == JOptionPane.YES_OPTION) {
            return true;
        } else {
            return false;
        }
    }

    public void windowClosing(WindowEvent e)  
    {
        if (quitConfirmed(this) == true)
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

        if (laf.equals("apple.laf.AquaLookAndFeel"))
        {
            System.setProperty("Quaqua.design", "panther");
            
            try {
                UIManager.setLookAndFeel(
                    "ch.randelshofer.quaqua.QuaquaLookAndFeel"
                );
           } catch (Exception e) { System.err.println(laf + " not supported.");}
        } else {
            try {
                UIManager.setLookAndFeel(laf);
            } catch (Exception e) 
            { System.err.println(laf + " not supported."); }
        }
        
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    new Main();
                }
            });
        } catch (InterruptedException e) {
            // Ignore: If this exception occurs, we return too early, which
            // makes the splash window go away too early.
            // Nothing to worry about. Maybe we should write a log message.
        } catch (InvocationTargetException e) {
            // Error: Startup has failed badly. 
            // We can not continue running our application.
            InternalError error = new InternalError();
            error.initCause(e);
            throw error;
        }
    }
}