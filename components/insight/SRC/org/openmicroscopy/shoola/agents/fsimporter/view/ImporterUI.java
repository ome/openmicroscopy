/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.ImporterUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.fsimporter.view;


//Java imports
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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
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
import info.clearthought.layout.TableLayout;

import org.apache.commons.collections.CollectionUtils;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.actions.GroupSelectionAction;
import org.openmicroscopy.shoola.agents.fsimporter.chooser.ImportDialog;
import org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponent;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.ui.ClosableTabbedPane;
import org.openmicroscopy.shoola.util.ui.ClosableTabbedPaneComponent;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
class ImporterUI extends TopWindow
{

	/** The text displayed in the header of the dialog.*/
	private static final String TEXT_TITLE_DESCRIPTION =
			"Select data to import and monitor imports.";
	
	/** The window's title. */
	private static final String TEXT_TITLE = "Import Data";
	
	/** The text displayed to notify the user to refresh. */
	private static final String	TEXT_REFRESH =
			"New containers added. Please Refresh";
	
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
    	if (p == null)
    		return;
    	
        if (c == null)
        	throw new IllegalArgumentException("No component.");
        
    	personalMenu = new JPopupMenu();
    	personalMenu.setBorder(
    			BorderFactory.createBevelBorder(BevelBorder.RAISED));
    	List<GroupSelectionAction> l = controller.getUserGroupAction();
    	Iterator<GroupSelectionAction> i = l.iterator();
    	GroupSelectionAction a;
    	JCheckBoxMenuItem item;
    	ButtonGroup buttonGroup = new ButtonGroup();
    	long id = model.getGroupId();
    	while (i.hasNext()) {
			a = i.next();
			item = new JCheckBoxMenuItem(a);
			item.setEnabled(true);
			item.setSelected(a.isSameGroup(id));
			initMenuItem(item);
			buttonGroup.add(item);
			personalMenu.add(item);
		}
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
		p.add(new JButton(controller.getAction(ImporterControl.RETRY_BUTTON)));
		p.add(Box.createHorizontalStrut(5));
		p.add(new JButton(controller.getAction(ImporterControl.SEND_BUTTON)));
		p.add(Box.createHorizontalStrut(5));
		p.add(new JButton(controller.getAction(ImporterControl.CANCEL_BUTTON)));
		p.add(Box.createHorizontalStrut(5));
		if (!model.isMaster()) {
			p.add(new JButton(controller.getAction(
					ImporterControl.CLOSE_BUTTON)));
		}
		return UIUtilities.buildComponentPanelRight(p);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		Container container = getContentPane();
		container.setLayout(new BorderLayout(0, 0));
		
		IconManager icons = IconManager.getInstance();
		TitlePanel tp = new TitlePanel(TEXT_TITLE, "", TEXT_TITLE_DESCRIPTION, 
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
		messageLabel.setText(TEXT_REFRESH);
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
							hasFailuresToReupload());
				controller.getAction(
						ImporterControl.SEND_BUTTON).setEnabled(
								hasFailuresToSend());
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
 			if (i != TaskBar.FILE_MENU) bar.add(existingMenus[i]);
		}
    	return bar;
    }
    
    /** Packs the window and resizes it if the screen is too small. */
	private void packWindow()
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension size = getSize();
		int width = (int) (screenSize.width);
		int height = (int) (screenSize.height);
		int w = size.width-10;
		int h = size.height-10;
		boolean reset = false;
		if (w > width) {
			reset = true;
		}
		if (h > height) {
			reset = true;
			h = height;
		} 
		if (reset) {
			setSize(h, h);
		}
	}
	
	/** Creates a new instance. */
	ImporterUI()
	{
		super(TEXT_TITLE);
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
		setName("importer window");
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
		tabs.insertTab("Select Data to Import", null, chooser, "", 0);
		//if in debug mode insert the debug section
		Boolean debugEnabled = (Boolean) 
			ImporterAgent.getRegistry().lookup("/options/Debug");
		if (debugEnabled != null && debugEnabled.booleanValue()) {
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
		pack();
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
		if(element == null)
			return null;
		
		l = element.getMarkedFiles();
		if (l != null && l.size() > 0)
			list.addAll(l);
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
		ImporterUIElement element = new ImporterUIElement(controller, model,
				this, uiElementID, n, title, object);
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
	
	/** Resets the import.*/
	void reset()
	{
		int n = tabs.getTabCount();
		for (int i = 1; i < n; i++) {
			tabs.remove(i);
		}
		uiElements.clear();
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
		int index = -1;
		for (int i = 0; i < components.length; i++) {
			if (components[i] == element) {
				index = i;
				tabs.setSelectedComponent(element);
			}
		}
		if (startImport) {
			Icon icon = element.startImport(tabs);
			if (index >=0) tabs.setIconAt(index, icon);
		}
	}
	
	/**
	 * Returns the selected pane.
	 * 
	 * @return See above.
	 */
	ImporterUIElement getSelectedPane()
	{
		if (tabs.getSelectedIndex() > 0 && tabs.getSelectedComponent() instanceof ImporterUIElement)
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
			if (!element.hasStarted())
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
	 * Returns <code>true</code> if there is an on-going import for a given
	 * group or one schedule.
	 *
	 * @return See above.
	 */
	boolean hasOnGoingImportFor(SecurityContext ctx)
	{
	    Collection<ImporterUIElement> values = uiElements.values();
	    Iterator<ImporterUIElement> i = values.iterator();
	    ImporterUIElement elt;
	    ImportableObject data;
	    List<ImportableFile> files;
	    ImportableFile f;
	    Iterator<ImportableFile> j;
	    while (i.hasNext()) {
            elt = i.next();
            data = elt.getData();
            files = data.getFiles();
            if (CollectionUtils.isNotEmpty(files) && !elt.isDone()) {
                j = files.iterator();
                while (j.hasNext()) {
                    f = j.next();
                    if (f.getGroup().getId() == ctx.getGroupID()) {
                        return true;
                    }
                }
            }
        }
	    return false;
	}
    
	/**
	 * Returns <code>true</code> if errors to send, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasFailuresToSend()
	{
		ImporterUIElement element = getSelectedPane();
		if (element == null) return false;
		return element.hasFailuresToSend();
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
	 * Returns the collection of files that could not be imported.
	 * 
	 * @return See above.
	 */
	List<FileImportComponent> getFilesToReimport()
	{
		ImporterUIElement pane = getSelectedPane();
    	if (pane == null) return null;
    	return pane.getFilesToReupload();
	}

	/**
	 * Returns <code>true</code> if file to re-upload, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasFailuresToReupload()
	{
		ImporterUIElement element = getSelectedPane();
		if (element == null) return false;
		return element.hasFailuresToReupload();
	}

	/** 
	 * Overridden to the set the location of the {@link ImViewer}.
	 * @see TopWindow#setOnScreen() 
	 */
	public void setOnScreen()
	{
		packWindow();
		UIUtilities.centerAndShow(this);
	}

}