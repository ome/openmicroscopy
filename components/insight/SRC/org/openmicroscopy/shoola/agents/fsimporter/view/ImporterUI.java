/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.ImporterUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.fsimporter.view;


//Java imports
import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

//Third-party libraries
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.actions.GroupSelectionAction;
import org.openmicroscopy.shoola.agents.fsimporter.actions.ImporterAction;
import org.openmicroscopy.shoola.agents.fsimporter.actions.PersonalManagementAction;
import org.openmicroscopy.shoola.agents.fsimporter.chooser.ImportDialog;
import org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponent;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.ui.ClosableTabbedPane;
import org.openmicroscopy.shoola.util.ui.ClosableTabbedPaneComponent;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ExperimenterData;

/** 
 * The {@link Importer}'s View. Displays the on-going import and the finished
 * ones.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class ImporterUI 
	extends TopWindow
{

	/** The window's title. */
	private static final String TITLE = "Import Data";
	
	/** The text displayed to notify the user to refresh. */
	private static final String	REFRESH_TXT = "New containers added. " +
			"Please Refresh";
	
	/** Identifies the style of the document.*/
	private static final String STYLE = "StyleName";
	
	/** The maximum number of characters in the debug text.*/
	private static final int 	MAX_CHAR = 2000;
	
	/** Reference to the model. */
	private ImporterModel	model;
	
	/** Reference to the control. */
	private ImporterControl	controller;
	
	/** The total of imports. */
	private int total;
	
	/** The component displaying the various imports. */
	private ClosableTabbedPane tabs;
	
	/** Flag used to indicate that the component has been displayed or not. */
	private boolean initialized;
	
	/** The identifier of the UI element. */
	private int uiElementID;
	
	/** Keeps track of the imports. */
	private Map<Integer, ImporterUIElement> uiElements;
	
	/** The controls bar. */
	private JComponent controlsBar;

	/** The component indicating to refresh the containers view.*/
	private JXLabel messageLabel;
	
	/** The menu displaying the groups the user is a member of. */
    private JPopupMenu	personalMenu;
    
    /** The debug text.*/
    private JTextPane	debugTextPane;
	
	/**
	 * Creates the component hosting the debug text.
	 * 
	 * @return See above.
	 */
	private JComponent createDebugTab()
	{
		debugTextPane = new JTextPane();
		debugTextPane.setEditable(false);
		StyledDocument doc = (StyledDocument) debugTextPane.getDocument();

		Style style = doc.addStyle(STYLE, null);
		StyleConstants.setForeground(style, Color.black);
		StyleConstants.setFontFamily(style, "SansSerif");
		StyleConstants.setFontSize(style, 12);
		StyleConstants.setBold(style, false);

		JScrollPane sp = new JScrollPane(debugTextPane);
		sp.getVerticalScrollBar().addAdjustmentListener(
				new AdjustmentListener()
				{
					public void adjustmentValueChanged(AdjustmentEvent e)
					{
						try {
							debugTextPane.setCaretPosition(
									debugTextPane.getDocument().getLength());
						} catch (IllegalArgumentException ex) {
							//
						}
					}
				}
		);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(sp, BorderLayout.CENTER);
		return panel;
	}
	
    /**
     * Sets the defaults of the specified menu item.
     * 
     * @param item The menu item.
     */
    private void initMenuItem(JMenuItem item)
    {
        item.setBorder(null);
        //item.setFont((Font) ImporterAgent.getRegistry().lookup(
        //              "/resources/fonts/Labels"));
    }
    
    /**
     * Brings up the <code>ManagePopupMenu</code>on top of the specified
     * component at the specified location.
     * 
     * @param c The component that requested the po-pup menu.
     * @param p The point at which to display the menu, relative to the
     *            <code>component</code>'s coordinates.
     */
    private void showPersonalMenu(Component c, Point p)
    {
    	if (p == null) return;
        if (c == null) throw new IllegalArgumentException("No component.");
        //if (p == null) throw new IllegalArgumentException("No point.");
        //if (personalMenu == null) {
        	personalMenu = new JPopupMenu();
        	personalMenu.setBorder(
        			BorderFactory.createBevelBorder(BevelBorder.RAISED));
        	List<GroupSelectionAction> l = controller.getUserGroupAction();
        	Iterator<GroupSelectionAction> i = l.iterator();
        	GroupSelectionAction a;
        	JCheckBoxMenuItem item;
        	ButtonGroup buttonGroup = new ButtonGroup();
        	ExperimenterData exp = ImporterAgent.getUserDetails();
        	long id = exp.getDefaultGroup().getId();
        	while (i.hasNext()) {
				a = i.next();
				item = new JCheckBoxMenuItem(a);
				item.setEnabled(true);
				item.setSelected(a.isSameGroup(id));
				initMenuItem(item);
				buttonGroup.add(item);
				personalMenu.add(item);
			}
        //}
        personalMenu.show(c, p.x, p.y);
    }
    
	/**
	 * Builds and lays out the controls.
	 * 
	 * @return See above.
	 */
	private JPanel buildControls()
	{
		JPanel p = new JPanel();
		p.add(new JButton(controller.getAction(ImporterControl.CANCEL_BUTTON)));
		p.add(Box.createHorizontalStrut(5));
		if (!model.isMaster()) {
			p.add(new JButton(controller.getAction(
					ImporterControl.CLOSE_BUTTON)));
			p.add(Box.createHorizontalStrut(5));
		}
		p.add(new JButton(controller.getAction(ImporterControl.RETRY_BUTTON)));
		p.add(Box.createHorizontalStrut(5));
		p.add(new JButton(controller.getAction(ImporterControl.SEND_BUTTON)));
		return UIUtilities.buildComponentPanelRight(p);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		Container container = getContentPane();
		container.setLayout(new BorderLayout(0, 0));
		
		IconManager icons = IconManager.getInstance();
		TitlePanel tp = new TitlePanel(TITLE, "", "Select data to import " +
				"and monitor imports.", 
				icons.getIcon(IconManager.IMPORT_48));
		JXPanel p = new JXPanel();
		JXPanel lp = new JXPanel();
		lp.setLayout(new FlowLayout(FlowLayout.LEFT));
		lp.add(messageLabel);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(tp);
		p.add(lp);
		p.setBackgroundPainter(tp.getBackgroundPainter());
		lp.setBackgroundPainter(tp.getBackgroundPainter());
		container.add(p, BorderLayout.NORTH);
		container.add(tabs, BorderLayout.CENTER);
		container.add(controlsBar, BorderLayout.SOUTH);
	}
	
	/** Displays the window. */
	private void display()
	{
		if (initialized) return;
		initialized = true;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(6*(screenSize.width/10), 8*(screenSize.height/10));
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		messageLabel = new JXLabel();
		//IconManager icons = IconManager.getInstance();
		//messageLabel.setIcon(icons.getIcon(IconManager.REFRESH));
		messageLabel.setText(REFRESH_TXT);
		messageLabel.setVisible(false);
		messageLabel.setFont(messageLabel.getFont().deriveFont(Font.BOLD));
		controlsBar = buildControls();
		controlsBar.setVisible(false);
		uiElementID = 0;
		uiElements = new LinkedHashMap<Integer, ImporterUIElement>();
		tabs = new ClosableTabbedPane(JTabbedPane.TOP, 
				JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.setAlignmentX(LEFT_ALIGNMENT);
		tabs.addPropertyChangeListener(controller);
		tabs.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent e) {
				controlsBar.setVisible(tabs.getSelectedIndex() != 0);
				controller.getAction(
						ImporterControl.RETRY_BUTTON).setEnabled(
							hasFailuresToReimport());
				controller.getAction(
						ImporterControl.SEND_BUTTON).setEnabled(
								hasSelectedFailuresToSend());
			}
		});
	}
	
	 /**
     * Helper method to create the <code>File</code> menu.
     * 
     * @return See above.
     */
    private JMenu createFileMenu()
    {
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.add(new JMenuItem(controller.getAction(ImporterControl.LOG_OFF)));
        menu.add(new JMenuItem(controller.getAction(ImporterControl.EXIT)));
        return menu;
    }
    
	/**
     * Creates the menu bar.
     * 
     * @return The menu bar.
     */
    private JMenuBar createMenuBar()
    {
    	TaskBar tb = ImporterAgent.getRegistry().getTaskBar();
    	JMenuBar bar = tb.getTaskBarMenuBar();
    	if (!model.isMaster()) return bar;
    	JMenu[] existingMenus = new JMenu[bar.getMenuCount()];
    	for (int i = 0; i < existingMenus.length; i++) {
    		existingMenus[i] = bar.getMenu(i);
    	}
    	
 		bar.removeAll();
 		bar.add(createFileMenu());
 		for (int i = 0; i < existingMenus.length; i++) {
			bar.add(existingMenus[i]);
		}
    	return bar;
    }
    
	/** Creates a new instance. */
	ImporterUI()
	{
		super(TITLE);
	}

	/**
     * Links this View to its Controller.
     * 
     * @param model 		The Model.
     * @param controller	The Controller.
     */
	void initialize(ImporterModel model, ImporterControl controller)
	{
		this.model = model;
		this.controller = controller;
		total = 1;
		initComponents();
		setJMenuBar(createMenuBar());
		buildGUI();
	}
	
	/**
	 * Displays or hides the message indicating to refresh the location view.
	 * 
	 * @param show Pass <code>true</code> to show, <code>false</code> to hide.
	 */
	void showRefreshMessage(boolean show) { messageLabel.setVisible(show); }

	/** 
	 * Adds the chooser to the tab.
	 * 
	 * @param chooser The component to add.
	 */
	void addComponent(ImportDialog chooser)
	{
		if (chooser == null) return;
		if (model.isMaster()) chooser.addToolBar(buildToolBar());
		tabs.insertTab("Select Data to Import", null, chooser, "", 0);
		//if in debug mode insert the debug section
		Boolean b = (Boolean) 
			ImporterAgent.getRegistry().lookup("/options/Debug");
		if (b != null && b.booleanValue()) {
			IconManager icons = IconManager.getInstance();
			ClosableTabbedPaneComponent c = new ClosableTabbedPaneComponent(1, 
					"Debug Text", icons.getIcon(IconManager.DEBUG));
			c.setCloseVisible(false);
			c.setClosable(false);
			double[][] tl = {{TableLayout.FILL}, {TableLayout.FILL}};
			c.setLayout(new TableLayout(tl));
			c.add(createDebugTab(), "0, 0");
			tabs.insertClosableComponent(c);
		}
		selectChooser();
	}
	
	/** Indicates to the select the import chooser. */
	void selectChooser() { tabs.setSelectedIndex(0); }
	
	/**
	 * Returns the collection of files that could not be imported.
	 * 
	 * @return See above.
	 */
	List<FileImportComponent> getMarkedFiles()
	{
		Component[] comps = tabs.getComponents();
		if (comps == null || comps.length == 0) return null;
		List<FileImportComponent> list = new ArrayList<FileImportComponent>();
		List<FileImportComponent> l;
		ImporterUIElement element = getSelectedPane();
		l = element.getMarkedFiles();
		if (l != null && l.size() > 0)
			list.addAll(l);
		/*
		for (int i = 0; i < comps.length; i++) {
			if (comps[i] instanceof ImporterUIElement) {
				element = (ImporterUIElement) comps[i];
				l = element.getMarkedFiles();
				if (l != null && l.size() > 0)
					list.addAll(l);
			}
		}
		*/
		return list;
	}
	
	/**
	 * Adds the component to the display.
	 * 
	 * @param object The component to add.
	 */
	ImporterUIElement addImporterElement(ImportableObject object)
	{
		if (object == null) return null;
		int n = tabs.getComponentCount();
		String title = "Import #"+total;
		ImporterUIElement element = new ImporterUIElement(controller,
				uiElementID, n, title, object);
		//IconManager icons = IconManager.getInstance();
		tabs.insertTab(title, element.getImportIcon(), element, "", total);
		total++;
		uiElements.put(uiElementID, element);
		uiElementID++;
		if (!isVisible()) {
			setVisible(true);
			display();
		}
		return element;
	}
	
	/**
	 * Modifies the icon corresponding to the specified component when 
	 * the import has ended.
	 * 
	 * @param element The element to handle.
	 */
	void onImportEnded(ImporterUIElement element)
	{
		if (element == null) return;
		element.onImportEnded();
		if (tabs.getSelectedComponent() == element) {
			tabs.setIconAt(tabs.getSelectedIndex(), element.getImportIcon());
		} else {
			Component[] components = tabs.getComponents();
			int index = -1;
			for (int i = 0; i < components.length; i++) {
				if (components[i] == element) {
					index = i;
				}
			}
			if (index >= 0) 
				tabs.setIconAt(index, element.getImportIcon());
			
		}
	}
	
	/**
	 * Sets the selected pane when the import start.
	 * 
	 * @param element The element to select.
	 * @param startImport Pass <code>true</code> to start the import, 
	 * 					  <code>false</code> otherwise.
	 */
	void setSelectedPane(ImporterUIElement element, boolean startImport)
	{
		int n = tabs.getComponentCount();
		if (n == 0 || element == null) return;
		if (tabs.getSelectedComponent() == element) return;
		Component[] components = tabs.getComponents();
		for (int i = 0; i < components.length; i++) {
			if (components[i] == element) {
				tabs.setSelectedComponent(element);
			}
		}
		if (startImport) {
			//tabs.setIconAt(index, busyIcon);
			element.startImport();
		}
	}
	
	/**
	 * Returns the selected pane.
	 * 
	 * @return See above.
	 */
	ImporterUIElement getSelectedPane()
	{
		if (tabs.getSelectedIndex() > 0)
			return (ImporterUIElement) tabs.getSelectedComponent();
		return null;
	}
	
	/**
	 * Returns the UI element corresponding to the passed index.
	 * 
	 * @param index The identifier of the component
	 * @return See above.
	 */
	ImporterUIElement getUIElement(int index)
	{
		return uiElements.get(index);
	}

	/**
	 * Returns the first element with data to import.
	 * 
	 * @return See above.
	 */
	ImporterUIElement getElementToStartImportFor()
	{
		if (uiElements.size() == 0) return null;
		Iterator<ImporterUIElement> i = uiElements.values().iterator();
		ImporterUIElement element;
		while (i.hasNext()) {
			element = i.next();
			if (!element.isDone())
				return element;
		}
		return null;
	}
	
	/**
	 * Removes the specified element. Returns the element or <code>null</code>.
	 * 
	 * @param object The object to remove.
	 * @return See above.
	 */
	ImporterUIElement removeImportElement(Object object)
	{
		Iterator<ImporterUIElement> i = uiElements.values().iterator();
		ImporterUIElement element;
		ImporterUIElement found = null;
		while (i.hasNext()) {
			element = i.next();
			if (element == object) {
				found = element;
				break;
			}
		}
		if (found != null) uiElements.remove(found.getID());
		return found;
	}

	/**
	 * Returns the elements with data to import.
	 * 
	 * @return See above.
	 */
	Collection<ImporterUIElement> getImportElements()
	{
		return uiElements.values();
	}
	
	/**
	 * Returns <code>true</code> if errors to send, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasFailuresToSend()
	{
		/*
		Iterator<ImporterUIElement> i = uiElements.values().iterator();
		while (i.hasNext()) {
			if (i.next().hasFailuresToSend())
				return true;
		}
		return false;
		*/
		
		return hasSelectedFailuresToSend();
	}
	
	
    /**
     * Brings up the menu on top of the specified component at 
     * the specified location.
     * 
     * @param menuID    The id of the menu.
     * @param c         The component that requested the pop-up menu.
     * @param p         The point at which to display the menu, relative to the
     *                  <code>component</code>'s coordinates.
     */
    void showMenu(int menuID, Component c, Point p)
    {
        switch (menuID) {
            case Importer.PERSONAL_MENU:
            	showPersonalMenu(c, p);
        }  
    }
	
	/**
	 * Returns <code>true</code> if the agent is the entry point
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isMaster() { return model.isMaster(); }
	
	/** 
	 * Adds the text to the debug pane.
	 * 
	 * @param text The text to display.
	 */
	void appendDebugText(String text)
	{
		if (debugTextPane == null) return;
		StyledDocument doc = (StyledDocument) debugTextPane.getDocument();
		try {
			doc.insertString(doc.getLength(), text, doc.getStyle(STYLE));
			if (doc.getLength() > MAX_CHAR)
				doc.remove(0, doc.getLength()-MAX_CHAR);
		} catch (Exception e) {
			//ignore
		}
	}
	
    /** 
     * Builds the toolbar when the importer is the entry point.
     * 
     * @return See above.
     */
    JComponent buildToolBar()
    {
        Set set = ImporterAgent.getAvailableUserGroups();
        if (set == null || set.size() == 0) return null;
        
    	ImporterAction a = controller.getAction(ImporterControl.GROUP_BUTTON);
    	a.setEnabled(set.size() > 1);
    	JButton b = new JButton(a);
    	UIUtilities.unifiedButtonLookAndFeel(b);
        b.addMouseListener((PersonalManagementAction) a);
        return b;
    }
    
    /**
     * Returns <code>true</code> if the selected pane has failures to send,
     * <code>false/code> otherwise.
     * 
     * @return See above.
     */
    boolean hasSelectedFailuresToSend()
    {
    	ImporterUIElement pane = getSelectedPane();
    	if (pane == null) return false;
    	return pane.hasFailuresToSend();
    }
    
    /**
	 * Returns the collection of files that could not be imported.
	 * 
	 * @return See above.
	 */
	List<FileImportComponent> getFilesToReimport()
	{
		ImporterUIElement pane = getSelectedPane();
    	if (pane == null) return null;
    	return pane.getFilesToReimport();
	}
	
	/**
	 * Returns <code>true</code> if file to re-import, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasFailuresToReimport()
	{
		ImporterUIElement element = getSelectedPane();
		if (element == null) return false;
		return element.hasFailuresToReimport();
	}

}
