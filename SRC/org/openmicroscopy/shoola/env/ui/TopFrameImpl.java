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

//Java imports
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.config.IconFactory;
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 * Implements the {@link TopFrame} interface
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

public class TopFrameImpl 
    extends JFrame 
    implements TopFrame, ActionListener
{
	/** Constant used to size and positions the topFrame. */
	static final int		INSET = 100;
	
	/** 
	 * ID to handle action command and position the menu Item 
	 * in the connectMenu.
	 */ 
	static final int        OMEDS = TopFrame.OMEDS;
	static final int        OMEIS = TopFrame.OMEIS;
	
	/** Action command ID. */ 
	static final int        EXIT = 10;
	
	/** the 4 available menus. */    
    private JMenu           fileMenu, viewMenu, helpMenu, connectMenu;
    
	/** The application internal desktop. */ 
    private JDesktopPane    desktop;
    
    /** Reference to the singleton {@link Container}. */ 
    private Container       container;
    
    /**
     * Creates a new Instance of {@link TopFrameImpl}
     *
     * @param container Reference to the singleton {@link Container}. 
     */ 
    public TopFrameImpl(Container container)
    {
        super("Open Microscopy Environment");
        this.container = container;
        
        //make sure we have nice window decorations
        JFrame.setDefaultLookAndFeelDecorated(true);  
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
        //ome icon.
		IconFactory factory = (IconFactory) 
			container.getRegistry().lookup("/resources/icons/DefaultFactory");
		ImageIcon omeIcon = (ImageIcon) factory.getIcon("OME16.png");
		setIconImage(omeIcon.getImage());
        setJMenuBar(createMenuBar());
        desktop = new JDesktopPane();
		getContentPane().add(desktop);
    }
    
	/** Implemented as specified by {@link TopFrame}. */     
    public void addToDesktop(Component c, int position)
    {
        desktop.add(c, new Integer(position));
    }
    
	/** Implemented as specified by {@link TopFrame}.*/ 
    public void removeFromDesktop(Component c)
    {
        desktop.remove(c);
        c.setVisible(false);
    }
    
	/** Implemented as specified by {@link TopFrame}.*/ 
    public void addToMenu(int menuType, JMenuItem menuItem)
    {
        JMenu menu = retrieveMenu(menuType);
        if (menuType == FILE) addToMenuFile(menuItem);
        else menu.add(menuItem);
    }
    
	/** Implemented as specified by {@link TopFrame}. */ 
    public void removeFromMenu(int menuType, JMenuItem item)
    {
        JMenu menu = retrieveMenu(menuType);
        menu.remove(item);
    }
    
	/** Implemented as specified by {@link TopFrame}. */ 
    public JMenuItem getItemFromMenu(int menuType, int itemPosition) 
    {
    	JMenuItem item = null;
    	JMenu menu = retrieveMenu(menuType);
    	if (0 <= itemPosition && itemPosition < menu.getItemCount()) 
    		item = menu.getItem(itemPosition);
    	return item;
    }
    
	/** Implemented as specified by {@link TopFrame}. */ 
    public JFrame getFrame()
    {
    	return this;
    }
    
	/** 
	 * Handles the <code>EXIT<code> event fired by the fileMenu.
	 * 
	 * Required by the ActionListener interface. 
	 */ 
    public void actionPerformed(ActionEvent e)
    {
        try {
            int     cmd = Integer.parseInt(e.getActionCommand());
            // just in case we need to handle other events
            switch (cmd) {
                case EXIT:
                  //TODO: do something
                  break;
                case OMEDS:
                	connectToOMEDS();
                	break;
                case OMEIS:
					connectToOMEIS();
                    break;
                default: break;
            }        
        } catch(NumberFormatException nfe) {//impossible if IDs are set correctly 
                throw nfe;  //just to be on the safe side...
        }
    }
    
	/** Connect to OMEDS. */
	private void connectToOMEDS()
	{
		LoginOMEDS loginDS = new LoginOMEDS(container);
		showLogin(loginDS);
	}
    
	/** Connect to OMEIS. */
	private void connectToOMEIS()
	{
		LoginOMEIS loginIS = new LoginOMEIS(container);
		showLogin(loginIS);
	}
    
	/**
	* Pops up the top frame window.
	*/
    public void open()
    {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(INSET, INSET, screenSize.width  - INSET*2,
				  screenSize.height - INSET*2);
    	setVisible(true);
    }
    
	/** 
	 * Sizes, centers and brings up the specified login dialog.
	 *
	 * @param   editor	The editor dialog.
	 */
	private void showLogin(JDialog editor)
	{
		//editor.pack();
		Registry registry = container.getRegistry();
		JFrame topFrame = (JFrame) registry.getTopFrame().getFrame();
		Rectangle   tfB = topFrame.getBounds(), 
					psB = editor.getBounds();
		int         offsetX = (tfB.width-psB.width)/2, 
					offsetY = (tfB.height-psB.height)/2;
		if (offsetX < 0)   offsetX = 0;
		if (offsetY < 0)   offsetY = 0;
		editor.setLocation(tfB.x+offsetX, tfB.y+offsetY);
		editor.setVisible(true);
	}

	/** 
	* Adds the specified menuItem to the container at the position n-1. 
	* 
	* @param menuItem       menuItem to add.
	*/
    private void addToMenuFile(JMenuItem menuItem)
    {
        JComponent popMenu = fileMenu.getPopupMenu();
        Component[] list = popMenu.getComponents();
        Component lastOne = list[list.length-1];
        fileMenu.add(menuItem);
        fileMenu.add(lastOne);
    }
    
	/** Initializes the 4 menus and add them to the menuBar. */
    private JMenuBar createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar(); 
        createFileMenu();
        createConnectMenu();
        viewMenu = new JMenu("View");
        helpMenu = new JMenu("Help");
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(connectMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }
    
	/** Initializes the fileMenu. */
    private void createFileMenu()
    {
        fileMenu = new JMenu("File");
        JMenuItem menuItem = new JMenuItem("Exit");
        menuItem.setActionCommand(""+EXIT);
        menuItem.addActionListener(this);
        fileMenu.add(menuItem);
    }
    
	/** Initializes the connectMenu. */
	private void createConnectMenu()
	{
		connectMenu = new JMenu("Connect");
		JMenuItem menuItem = new JMenuItem("OMEDS");
		menuItem.setActionCommand(""+OMEDS);
		menuItem.setEnabled(false);
		menuItem.addActionListener(this);
		connectMenu.add(menuItem);
		menuItem = new JMenuItem("OMEIS");
		menuItem.setEnabled(false);
		menuItem.setActionCommand(""+OMEIS);
		menuItem.addActionListener(this);
		connectMenu.add(menuItem);
	}
	
	/** 
	* Retrieves the specified menu. 
	* 
	* @param  int  menuType.
	* @return the above mentioned. 
	*/    
    private JMenu retrieveMenu(int menuType)
    {
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
                case CONNECT:
                	menu = connectMenu;
            }// end switch  
        } catch(NumberFormatException nfe) {//impossible if IDs are set correctly 
                throw nfe;  //just to be on the safe side...
        }    
        return menu;
    }
    
}
