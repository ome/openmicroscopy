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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;

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
	static final int				INSET = 100;
	
	/** 
	 * ID to handle action command and position the menu Item 
	 * in the connectMenu.
	 */ 
	static final int        		OMEDS = TopFrame.OMEDS;
	static final int        		OMEIS = TopFrame.OMEIS;
	
	/** Action command ID. */ 
	private static final int        EXIT_APP = 10;
	private static final int		HELPME = 11;
	
	private static final Dimension	BAR_SEPARATOR = new Dimension(15, 0);
	
	/** the 4 available menus. */    
    private JMenu           		fileMenu, viewMenu, helpMenu, connectMenu;
    
    /** The availabel toolBar. */
    private JToolBar				fileToolBar, viewToolBar, connectToolBar, 
    								helpToolBar;
    						
	/** The application internal desktop. */ 
    private JDesktopPane    		desktop;
    
    /** Reference to the singleton {@link Container}. */ 
    private Container      			container;
    
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
		IconManager im = IconManager.getInstance(container.getRegistry());
		ImageIcon omeIcon = (ImageIcon) im.getIcon(IconManager.OME);
		setIconImage(omeIcon.getImage());
        setJMenuBar(createMenuBar());
        desktop = new JDesktopPane();
		getContentPane().add(createToolBar(), BorderLayout.NORTH);
		getContentPane().add(desktop, BorderLayout.CENTER);
    }
    
	/** Implemented as specified by {@link TopFrame}. */  
    public void addToToolBar(int tbType, Component c)
    {
    	JToolBar tb = retrieveToolBar(tbType);
    	tb.add(c);
    	tb.addSeparator();
    }
    
	/** Implemented as specified by {@link TopFrame}. */  
	public void removeFromToolBar(int tbType, Component c)
	{
		JToolBar tb = retrieveToolBar(tbType);
		tb.remove(c);
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
    public JFrame getFrame() { return this; }
    
	/** Implemented as specified by {@link TopFrame}. */ 
    public void deiconifyFrame(JInternalFrame frame)
    {
		desktop.getDesktopManager().deiconifyFrame(frame);
    }

	/** 
	 * Handles the <code>EXIT<code> event fired by the fileMenu.
	 * 
	 * Required by the ActionListener interface. 
	 */ 
    public void actionPerformed(ActionEvent e)
    {
        try {
            int cmd = Integer.parseInt(e.getActionCommand());
            // just in case we need to handle other events
            switch (cmd) {
                case EXIT_APP:
					System.exit(0);	//TODO: shut down container.
                  	break;
                case OMEDS:
                	connectToOMEDS();	break;
                case OMEIS:
					connectToOMEIS(); break;
                case HELPME:
                	help(); break;
                default: break;
            }        
        } catch(NumberFormatException nfe) {
                throw nfe;  //just to be on the safe side...
        }
    }
    
	/** Post an event to connect to OMEDS. */
	private void connectToOMEDS()
	{
		ServiceActivationRequest request = new ServiceActivationRequest(
									ServiceActivationRequest.DATA_SERVICES);
		container.getRegistry().getEventBus().post(request);
	}
    
	/** Connect to OMEIS. */
	private void connectToOMEIS()
	{
		//LoginOMEIS loginIS = new LoginOMEIS(container);
		//showLogin(loginIS);
		//TODO: post an event
	}
    
    private void help()
    {
    }
    
	/** Pops up the top frame window. */
    public void open()
    {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(INSET, INSET, screenSize.width  - INSET*2,
				  screenSize.height - INSET*2);
    	setVisible(true);
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
        viewMenu = new JMenu("Window");
        helpMenu = new JMenu("Help");
        menuBar.add(fileMenu);
        menuBar.add(connectMenu);
		menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }
    
	/** Initializes the fileMenu. */
    private void createFileMenu()
    {
        fileMenu = new JMenu("File");
        JMenuItem menuItem = new JMenuItem("Exit");
        menuItem.setActionCommand(""+EXIT_APP);
        menuItem.addActionListener(this);
        fileMenu.add(menuItem);
    }
    
	/** Initializes the connectMenu. */
	private void createConnectMenu()
	{
		connectMenu = new JMenu("Connect");
		JMenuItem menuItem = new JMenuItem("OMEDS");
		menuItem.setActionCommand(""+OMEDS);
		menuItem.addActionListener(this);
		connectMenu.add(menuItem);
		menuItem = new JMenuItem("OMEIS");
		menuItem.setEnabled(false);
		menuItem.setActionCommand(""+OMEIS);
		menuItem.addActionListener(this);
		connectMenu.add(menuItem);
	}
	
	/** Create the 4 toolBars contained in the main one. */
	private JToolBar createToolBar()
	{
		JToolBar bar = new JToolBar();
		viewToolBar = new JToolBar();
		viewToolBar.setFloatable(false);
		createHelpToolBar();
		createFileToolBar();
		createConnectToolBar();
		bar.add(fileToolBar);
		bar.addSeparator(BAR_SEPARATOR);
		bar.add(connectToolBar);
		bar.addSeparator(BAR_SEPARATOR);
		bar.add(viewToolBar);
		bar.addSeparator(BAR_SEPARATOR);
		bar.add(helpToolBar);
		return bar;
	}
	
	/** Create the <code>help</code> toolbar. */
	private void createHelpToolBar()
	{
		helpToolBar = new JToolBar();
		helpToolBar.setFloatable(false);
		IconManager im = IconManager.getInstance(container.getRegistry());
		JButton help = new JButton(im.getIcon(IconManager.HELP));
		help.setToolTipText(
				UIFactory.formatToolTipText("Please help me."));
		help.setActionCommand(""+HELPME);
		help.addActionListener(this);		
		helpToolBar.add(help);
		helpToolBar.addSeparator();
	}
	
	/** Create the <code>file</code> toolbar. */
	private void createFileToolBar()
	{
		fileToolBar = new JToolBar();
		fileToolBar.setFloatable(false);
		IconManager im = IconManager.getInstance(container.getRegistry());
		JButton exit = new JButton(im.getIcon(IconManager.EXIT));
		exit.setToolTipText(
			UIFactory.formatToolTipText("Exit the application."));
		exit.setActionCommand(""+EXIT_APP);
		exit.addActionListener(this);
		fileToolBar.add(exit);
		fileToolBar.addSeparator();	
	}
	
	/** Create the <code>connect</code> toolbar. */
	private void createConnectToolBar()
	{
		connectToolBar = new JToolBar();
		connectToolBar.setFloatable(false);
		IconManager im = IconManager.getInstance(container.getRegistry());
		JButton connectDS = new JButton(im.getIcon(IconManager.CONNECT_DS));
		connectDS.setActionCommand(""+OMEDS);
		connectDS.addActionListener(this);
		connectDS.setToolTipText(
			UIFactory.formatToolTipText("Connect to OME DataService."));
		JButton connectIS = new JButton(im.getIcon(IconManager.CONNECT_IS));
		connectIS.setToolTipText(
			UIFactory.formatToolTipText("Connect to OME ImageService."));
		connectIS.setActionCommand(""+OMEIS);
		connectIS.addActionListener(this);	
		connectToolBar.add(connectDS);
		connectToolBar.addSeparator();
		connectToolBar.add(connectIS);
		connectToolBar.addSeparator();
	}
	
	/** 
	 * Retrieves the specified menu. 
	 * 
	 * @param  menuType		menu ID.
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
        } catch(NumberFormatException nfe) {
                throw nfe;  //just to be on the safe side...
        }    
        return menu;
    }
    
	/** 
	 * Retrieves the specified toolBar. 
	 * 
	 * @param  tbType  toolbar ID.
	 * @return the above mentioned. 
	 */  
    private JToolBar retrieveToolBar(int tbType)
    {
    	JToolBar tb = null;
		try {
			switch (tbType) {
				case FILE_TB:
					tb = fileToolBar; break;
				case VIEW_TB:
					tb = viewToolBar; break;
				case HELP_TB:
					tb = viewToolBar; break;
				case CONNECT:
					tb = connectToolBar;
			}// end switch  
		} catch(NumberFormatException nfe) {
			throw nfe;  //just to be on the safe side...
		}    
    	return tb;
    }
    
}
