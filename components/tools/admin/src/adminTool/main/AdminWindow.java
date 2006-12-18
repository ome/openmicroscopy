/*
 * *.AdminMain 
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package src.adminTool.main;

// Java imports
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import src.adminTool.ui.messenger.CommentMessenger;

// Third-party libraries

// Application-internal dependencies

/**
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date$)
 *          </small>
 * @since OME2.2
 * 
 */
public class AdminWindow 
	extends JFrame 
	implements ActionListener 
{
    /**
     * 
     */
    private static final long serialVersionUID = 3490036241587183165L;

    private JMenuBar menuBar;

    private JMenu fileMenu;

    private JMenuItem loginMenu;

    private JMenuItem exit;

    private JMenu helpMenu;

    private JMenuItem help;

    private JMenuItem addComment;

    private MainPanel mainPanel;

    public AdminWindow() {
        setTitle("Omero Admin");
        setLocation(300, 100);
        setSize(840, 600);
        createMenu();
        mainPanel = new MainPanel(this);
        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);
        this.setVisible(true);
        mainPanel.startLogin();
    }

    public void createMenu() {
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        loginMenu = new JMenuItem("Login to Server");
        exit = new JMenuItem("Exit");
        fileMenu.add(loginMenu);
        fileMenu.add(new JSeparator());
        fileMenu.add(exit);
        menuBar.add(fileMenu);
        loginMenu.setActionCommand("login");
        loginMenu.addActionListener(this);
        exit.setActionCommand("exit");
        exit.addActionListener(this);
        helpMenu = new JMenu("Help");
        help = new JMenuItem("About AdminTool");
        addComment = new JMenuItem("Send Comment To Developers");
        helpMenu.add(addComment);
        addComment.addActionListener(this);
        addComment.setActionCommand("comment");
        menuBar.add(helpMenu);

        this.setJMenuBar(menuBar);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("login")) {
            mainPanel.startLogin();
     //       pack();
        }
        if (e.getActionCommand().equals("exit")) {
            this.dispose();
        }
        if (e.getActionCommand().equals("comment")) {
            CommentMessenger comments = new CommentMessenger(this,
                    "Send Comment to Developers", true);

        }
    }

}
