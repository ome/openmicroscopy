/*
 * org.openmicroscopy.shoola.env.ui.TopFrameImpl
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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


package org.openmicroscopy.shoola.env.ui;

// Java imports
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/** 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * <b>Internal version:</b> $Revision$  $Date$
 * @version 2.2
 * @since OME2.2
 */

public class TopFrameImpl 
    extends JFrame 
    implements TopFrame, ActionListener {
        
/** To be used with the {@link #addToDesktop(Component, int) addToDesktop} method to position a
 * component on the bottommost layer of the application internal desktop.
*/
    public static int       DEFAULT_LAYER = 0;
/** To be used with the {@link #addToDesktop(Component, int) addToDesktop} method to position a
 * component on the palette layer of the application internal desktop.
 * The palette layer sits over the default layer. Useful for floating toolbars and palettes, so they
 * can be positioned above other components. 
*/
    public static int       PALETTE_LAYER = 1;
    
/*Action command ID */    
    static final int        FILE = 0;
    static final int        VIEW = 1;
    static final int        HELP = 2;
    static final int        EXIT = 3;
    
/** the 3 available menus */    
    private JMenu           fileMenu, viewMenu, helpMenu;
/** The application internal desktop. */ 
    private JDesktopPane    desktop;
    
    public TopFrameImpl() {
        super("Open Microscopy Environment");
        JFrame.setDefaultLookAndFeelDecorated(true);  //make sure we have nice window decorations
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // TODO add icon
        setJMenuBar(createMenuBar());
        desktop = new JDesktopPane();
    }
    
/** Implemented as specified by {@link UserNotifier}.
 */     
    public void addToDesktop(Component c, int position) {
        desktop.add(c, new Integer(position));
    }
    
/** Implemented as specified by {@link UserNotifier}.
 */ 
    public void removeFromDesktop(Component c) {
        desktop.remove(c);
        c.setVisible(false);
    }
    
/** Implemented as specified by {@link UserNotifier}.
 */ 
    public void addToMenu(int menuType, JMenuItem menuItem) {
        JMenu menu = retrieveMenu(menuType);
        if (menuType==FILE) addToMenuFile(menuItem);
        else menu.add(menuItem);
    }
    
/** Implemented as specified by {@linkUserNotifier}.
 */ 
    public void removeFromMenu(int menuType, JMenuItem item) {
        JMenu menu = retrieveMenu(menuType);
        menu.remove(item);
    }
    
/* handles the event EXIT fired by the fileMenu
 * 
 * Required by the ActionListener interface 
 */ 
    public void actionPerformed(ActionEvent e){
        try {
            int     cmd = Integer.parseInt(e.getActionCommand());
            // just in case we need to handle other events
            switch (cmd) {
                case EXIT:
                    // do something
            }        
        } catch(NumberFormatException nfe) {  //impossible if IDs are set correctly 
                throw nfe;  //just to be on the safe side...
        }
    }
    
    // TODO: display desktop
    public void open() {
    }
    
/* Add the specified menuItem to the container at the position n-1 
* 
* @param menuItem       menuItem to add
*/
    private void addToMenuFile(JMenuItem menuItem) {
        Container popMenu = fileMenu.getPopupMenu();
        Component[] list = popMenu.getComponents();
        Component lastOne = list[list.length-1];
        fileMenu.add(menuItem);
        fileMenu.add(lastOne);
    }
    
/* Initializes the 3 menus and add them to the menuBar */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar(); 
        createFileMenu();
        viewMenu = new JMenu("View");
        helpMenu = new JMenu("Help");
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }
    
/* Initializes the fileMenu */
    private void createFileMenu() {
        fileMenu = new JMenu("File");
        JMenuItem menuItem = new JMenuItem("Exit");
        menuItem.setActionCommand(""+EXIT);
        menuItem.addActionListener(this);
        fileMenu.add(menuItem);
    }

/* retrieves the specified menu 
* 
* @param  int  menuType
* @return the above mentioned 
*/    
    private JMenu retrieveMenu(int menuType) {
        JMenu menu = null;
        try {
            switch (menuType) {
                case FILE:
                    menu = fileMenu;
                    break;
                case VIEW:
                    menu = viewMenu;
                    break;
                case HELP:
                    menu = helpMenu;
            }// end switch  
        } catch(NumberFormatException nfe) {  //impossible if IDs are set correctly 
                throw nfe;  //just to be on the safe side...
        }    
        return menu;
    }
    
    
}
