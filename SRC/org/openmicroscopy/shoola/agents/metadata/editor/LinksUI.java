/*
 * org.openmicroscopy.shoola.agents.metadata.editor.LinksUI 
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
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.TitledLineBorder;
import pojos.AnnotationData;
import pojos.URLAnnotationData;

/** 
 * UI component displaying the collection of urls.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class LinksUI
	extends AnnotationUI
	implements ActionListener, DocumentListener, PropertyChangeListener
{
	
	/** The title associated to this component. */
	private static final String TITLE = "Links ";
	
	/** Action id indicating to add new file. */
	private static final String	ADD_NEW_ACTION = "addNew";
	
	/** Action id indicating to add new file. */
	private static final String	ADD_UPLOADED_ACTION = "addUploaded";
	
	/** Collection of key/value pairs used to remove annotations. */
	private Map<Integer, URLAnnotationData> urlComponents;
	
	/** Collection of key/value pairs used to remove annotations. */
	private Map<JLabel, URLAnnotationData> labels;
	
	/** Collection of urls to add. */
	private Set<URLAnnotationData>			toRemove;
	
	/** Collection of urls to unlink. */
	private Set<URLAnnotationData>			toAdd;
	
	/** Button to add a new URL. */
	private JButton							addButton;
	
	/** The field where to enter the url. */
	private List<JTextField>				areas;
	
	/** The UI component hosting the areas. */
	private JPanel							addedContent;
	
	/** The component hosting the final result. */
	private JPanel 							content;
	
	/** The selection menu. */ 
	private JPopupMenu						selectionMenu;
	
	/** Initializes the UI components. */
	private void initComponents()
	{
		toAdd = new HashSet<URLAnnotationData>();
		addedContent = new JPanel();
		areas = new ArrayList<JTextField>();
		addButton = new JButton("Add...");
		addButton.setToolTipText("Add a new URL.");
		addButton.addMouseListener(new MouseAdapter() {
			
			public void mouseReleased(MouseEvent e) {
				Point p = e.getPoint();
				createSelectionMenu().show(addButton, p.x, p.y);
			}
		
		});
	}
	
	/**
	 * Creates the selection menu.
	 * 
	 * @return See above.
	 */
	private JPopupMenu createSelectionMenu()
	{
		if (selectionMenu != null) return selectionMenu;
		selectionMenu = new JPopupMenu();
		IconManager icons = IconManager.getInstance();
		JMenuItem item = new JMenuItem("New URL");
		item.setIcon(icons.getIcon(IconManager.UPLOAD));
		item.setToolTipText("Attach a new URL.");
		item.addActionListener(this);
		item.setActionCommand(""+ADD_NEW_ACTION);
		selectionMenu.add(item);
		item = new JMenuItem("Existing URL");
		item.setIcon(icons.getIcon(IconManager.DOWNLOAD));
		item.setToolTipText("Attach an existing URL.");
		item.addActionListener(this);
		item.setActionCommand(""+ADD_UPLOADED_ACTION);
		selectionMenu.add(item);
		return selectionMenu;
	}
	
	/**
	 * Browses the specified url.
	 * 
	 * @param url The url to browse.
	 */
	private void browse(String url)
	{
		MetadataViewerAgent.getRegistry().getTaskBar().openURL(url);
	}
	
	/**
	 * Lays out the URL annotation.
	 * 
	 * @return See above.
	 */
	private JPanel layoutURL()
	{
		JPanel p = new JPanel();
		Collection urls = model.getUrls();
		Iterator i;
		URLAnnotationData url;
		int index = 0;

		urlComponents = new HashMap<Integer, URLAnnotationData>();
		if (urls != null) {
			i = urls.iterator();
			while (i.hasNext()) {
				url = (URLAnnotationData) i.next();
				if (!toRemove.contains(url)) {
					urlComponents.put(index, url);
					index++;
				}
			}
		}
		
		i = toAdd.iterator();
		while (i.hasNext()) {
			url = (URLAnnotationData) i.next();
			urlComponents.put(index, url);
			index++;
		}
		
		i = urlComponents.keySet().iterator();
		index = 0;
		IconManager icons = IconManager.getInstance();
		Icon icon = icons.getIcon(IconManager.REMOVE);
		JButton button;
		labels = new HashMap<JLabel, URLAnnotationData>();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 0;
		JLabel label;
		c.weightx = 0;
		if (urlComponents.size() == 0) return p;
		p.add(UIUtilities.setTextFont("Url: "), c);
		while (i.hasNext()) {
			c.gridx = 1;
			c.weightx = 0;
			index = (Integer) i.next();
			url = urlComponents.get(index);
			label = new JLabel(UIUtilities.formatURL(url.getURL()));
			label.setToolTipText("Added: "+model.formatDate(url));
			labels.put(label, url);
			label.addMouseListener(new MouseAdapter() {
			
				public void mouseReleased(MouseEvent e) {
					JLabel l = (JLabel) e.getSource();
					URLAnnotationData url = labels.get(l);
					if (url != null) browse(url.getURL());
				}
				
				public void mouseEntered(MouseEvent e) {
					JLabel l = (JLabel) e.getSource();
					l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
			
			});
			p.add(label, c);
			c.gridx = 2;
			p.add(Box.createHorizontalStrut(5), c);
			if (model.isCurrentUserOwner(url)) {
				c.gridx = 3;
				button = new JButton(icon);
				button.setBorder(null);
				button.setToolTipText("Remove the link.");
				button.setActionCommand(""+index);
				button.addActionListener(this);
				p.add(button, c);
			}
			++c.gridy;
		}
		return UIUtilities.buildComponentPanel(p);
	}
	
	/** 
	 * Creates a component hosting the URL to enter.
	 * 
	 * @return See above.
	 */
	private JTextField createURLArea()
	{
		JTextField area = new JTextField();
		UIUtilities.setTextAreaDefault(area);
		area.getDocument().addDocumentListener(this);
		areas.add(area);
		return area;
	}
	
	/** Adds a new url area only if the previously added one has been used. */
	private void addURLArea()
	{
		JTextField area;
		Iterator i;
		addedContent.removeAll();
		if (areas.size() == 0) {
			area = createURLArea();
		} else {
			i = areas.iterator();
			String text;
			boolean empty = false;
			while (i.hasNext()) {
				area = (JTextField) i.next();
				text = area.getText();
				if (text != null && text.trim().length() == 0) {
					empty = true;
					break;
				}
			}
			if (!empty) area = createURLArea();
		}
		addedContent.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 0;
		
		i = areas.iterator();
		while (i.hasNext()) {
			c.gridx = 0;
			++c.gridy;
			c.weightx = 0;
			area = (JTextField) i.next();
			addedContent.add(UIUtilities.setTextFont("URL: "), c);
			c.gridx = 1;
			c.weightx = 1.0;
			addedContent.add(area, c);
		}
		content.revalidate();
	}
	
	/**
	 * Lays out the components used to add new <code>URL</code>s.
	 * 
	 * @return See above.
	 */
	private JPanel layoutAddContent()
	{
		 content = new JPanel();
		 content.setLayout(new GridBagLayout());
		 GridBagConstraints c = new GridBagConstraints();
		 c.anchor = GridBagConstraints.FIRST_LINE_START;
		 c.fill = GridBagConstraints.HORIZONTAL;
		 c.gridx = 0;
		 content.add(addButton, c);
		 c.weightx = 0.5;
		 JScrollPane pane = new JScrollPane(addedContent);
		 pane.setOpaque(false);
		 pane.setBorder(null);
		 c.gridx++;
		 content.add(pane, c);
		 return content;
	}
	
	/**
	 * Returns <code>true</code> if the file has already been added, 
	 * <code>false</code> otherwise.
	 * 
	 * @param data The value to check.
	 * @return See above.
	 */
	private boolean isURLAdded(URLAnnotationData data)
	{
		if (data == null) return true;
		Iterator<URLAnnotationData> i = toAdd.iterator();
		URLAnnotationData f;
		while (i.hasNext()) {
			f = i.next();
			if (f.getId() == data.getId())
				return true;
		}
		return false;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	LinksUI(EditorModel model)
	{
		super(model);
		toRemove = new HashSet<URLAnnotationData>();
		title = TITLE;
		initComponents();
		TitledLineBorder border = new TitledLineBorder(title, getBackground());
		//setBorder(border);
		UIUtilities.setBoldTitledBorder(title, this);
		getCollapseComponent().setBorder(border);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(layoutAddContent());
	}
	
	/** Shows the collection of existing tags. */
	void showSelectionWizard()
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		Collection l = model.getExistingURLs();
		if (l == null) return;
		List<Object> r = new ArrayList<Object>();
		Collection urls = model.getUrls();
		Iterator i;
		Set<Long> ids = new HashSet<Long>();
		AnnotationData data;
		if (urls != null) {
			i = urls.iterator();
			while (i.hasNext()) {
				data = (AnnotationData) i.next();
				if (!toRemove.contains(data)) 
					ids.add(data.getId());
			}
		}
		
		if (l.size() > 0) {
			i = l.iterator();
			while (i.hasNext()) {
				data = (AnnotationData) i.next();
				if (!ids.contains(data.getId()))
					r.add(data);
			}
		}
		
		Registry reg = MetadataViewerAgent.getRegistry();
		if (r.size() == 0) {
			UserNotifier un = reg.getUserNotifier();
			un.notifyInfo("Existing URLs", "No Urls found.");
			return;
		}
		SelectionWizard wizard = new SelectionWizard(
										reg.getTaskBar().getFrame(), r);
		IconManager icons = IconManager.getInstance();
		wizard.setTitle("Upload URls Selection", "Select Urls already " +
				"updloaded to the server", 
				icons.getIcon(IconManager.URL_48));
		wizard.addPropertyChangeListener(this);
		UIUtilities.centerAndShow(wizard);
	}
	
	/**
	 * Overridden to lay out the links.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		removeAll();
		addedContent.removeAll();
		int n = model.getUrlsCount()-toRemove.size();
		title = TITLE+LEFT+n+RIGHT;
		TitledLineBorder border = new TitledLineBorder(title, getBackground());
		//setBorder(border);
		UIUtilities.setBoldTitledBorder(title, this);
		getCollapseComponent().setBorder(border);
		add(layoutURL());
		add(Box.createVerticalStrut(5));
		add(layoutAddContent());
		revalidate();
	}
	
	/**
	 * Overridden to set the title of the component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return title; }

	/**
	 * Returns the collection of urls to remove.
	 * @see AnnotationUI#getAnnotationToRemove()
	 */
	protected List<AnnotationData> getAnnotationToRemove()
	{
		Iterator i = toRemove.iterator();
		List<AnnotationData> l = new ArrayList<AnnotationData>();
		while (i.hasNext()) 
			l.add((AnnotationData) i.next());
		return l;
	}

	/**
	 * Creates a URL annotation.
	 * 
	 * @param value The passed value.
	 * @return See above.
	 */
	private URLAnnotationData createAnnotation(String value)
	{
		if (value == null) return null;
		LogMessage msg;
		String[] URLS = new String[1];
		URLS[0] = "http";
		try {
			return new URLAnnotationData(value);
		} catch (Exception e) {
			if (!value.contains(URLAnnotationData.HTTP) && 
					!value.contains(URLAnnotationData.HTTPS)) {
				value = URLAnnotationData.HTTP+"://"+value;
				try {
					return new URLAnnotationData(value);
				} catch (Exception ex) {
					msg = new LogMessage();
					msg.print("URL Creation");
					msg.print(ex);
					DataBrowserAgent.getRegistry().getLogger().error(this, msg);
					return null;
				}
			}
			msg = new LogMessage();
			msg.print("URL Creation");
			msg.print(e);
			DataBrowserAgent.getRegistry().getLogger().error(this, msg);
		}
		return null;
	}
	
	/**
	 * Returns the collection of urls to add.
	 * @see AnnotationUI#getAnnotationToSave()
	 */
	protected List<AnnotationData> getAnnotationToSave()
	{
		List<AnnotationData> l = new ArrayList<AnnotationData>(); 
		Iterator i = areas.iterator();
		JTextField area;
		String value;
		URLAnnotationData data;
		UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
		while (i.hasNext()) {
			area = (JTextField) i.next();
			value = area.getText();
			if (value != null) {
				value = value.trim();
				if (value.length() > 0) {
					data = createAnnotation(value);
					if (data != null) l.add(data);
					else {
						un.notifyInfo("URL", "The url entered is not valid.");
					}
				}
			}
		}
		if (toAdd.size() > 0)
			l.addAll(toAdd);
		return l;
	}
	
	/**
	 * Returns <code>true</code> if annotation to save.
	 * @see AnnotationUI#hasDataToSave()
	 */
	protected boolean hasDataToSave()
	{
		if (getAnnotationToRemove().size() > 0) return true;
		if (toAdd.size() > 0) return true;
		List<String> l = new ArrayList<String>(); 
		Iterator i = areas.iterator();
		JTextField area;
		String value;
		while (i.hasNext()) {
			area = (JTextField) i.next();
			value = area.getText();
			if (value != null) {
				
				value = value.trim();
				if (value.length() > 0)
					l.add(value);
			} 
		}
		if (l.size() > 0) return true;
		return false;
	}
	
	/**
	 * Clears the data to save.
	 * @see AnnotationUI#clearData()
	 */
	protected void clearData()
	{
		areas.clear();
		toRemove.clear();
		toAdd.clear();
	}
	
	/**
	 * Clears the UI.
	 * @see AnnotationUI#clearDisplay()
	 */
	protected void clearDisplay() 
	{
		removeAll();
		clearData();
	}
	
	/**
	 * Adds the selected annotation to the collection of elements to remove.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		String s = e.getActionCommand();
		if (ADD_NEW_ACTION.equals(s)) {
			addURLArea();
		} else if (ADD_UPLOADED_ACTION.equals(s)) {
			if (model.getExistingAttachments() == null) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				model.loadExistingUrls();
			} else showSelectionWizard();
		} else {
			int index = Integer.parseInt(s);
			URLAnnotationData url = urlComponents.get(index);
			if (url != null) {
				if (!toAdd.contains(url))
					toRemove.add(url);
				else toAdd.remove(url);
				firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
						Boolean.TRUE);
			}
			buildUI();
		}
	}

	/**
	 * Adds the selected <code>URL</code>s to the list of links to save.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (SelectionWizard.SELECTED_ITEMS_PROPERTY.equals(name)) {
			Collection l = (Collection) evt.getNewValue();
			if (l == null || l.size() == 0) return;
			Iterator i = l.iterator();
			URLAnnotationData data;
	    	while (i.hasNext()) {
	    		data = (URLAnnotationData) i.next();
	    		if (!isURLAdded(data))
	    			toAdd.add(data);
	    	}
	    	buildUI();
	    	firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
					Boolean.TRUE);
		}
	}
	
	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
						Boolean.TRUE);
	}

	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
							Boolean.TRUE);
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
}
