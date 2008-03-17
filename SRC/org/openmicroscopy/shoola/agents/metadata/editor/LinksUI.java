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
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
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
	implements ActionListener, DocumentListener
{
	
	/** The title associated to this component. */
	private static final String TITLE = "Links ";
	
	/** Action id indicating to add new url area. */
	private static final String	ADD_ACTION = "add";
	
	/** Collection of key/value pairs used to remove annotations. */
	private Map<Integer, URLAnnotationData> urlComponents;
	
	/** Collection of key/value pairs used to remove annotations. */
	private Map<JLabel, URLAnnotationData> labels;
	
	/** Collection of urls to unlink. */
	private Set<URLAnnotationData>			toRemove;
	
	/** Button to add a new URL. */
	private JButton							addButton;
	
	/** The field where to enter the url. */
	private List<JTextField>				areas;
	
	/** The UI component hosting the areas. */
	private JPanel							addedContent;
	
	/** The component hosting the final result. */
	private JPanel 							content;
	
	/** Initializes the UI components. */
	private void initComponents()
	{
		addedContent = new JPanel();
		areas = new ArrayList<JTextField>();
		addButton = new JButton("New...");
		addButton.setToolTipText("Add a new URL.");
		addButton.addActionListener(this);
		addButton.setActionCommand(ADD_ACTION);
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
		Iterator i = urlComponents.keySet().iterator();
		int index;
		URLAnnotationData url;
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
			
			if (model.isCurrentUserOwner(url)) {
				c.gridx = 2;
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
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	LinksUI(EditorModel model)
	{
		super(model);
		toRemove = new HashSet<URLAnnotationData>();
		title = TITLE;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		initComponents();
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
		IconManager icons = IconManager.getInstance();
		List<Image> imgs = new ArrayList<Image>();
		imgs.add(icons.getImageIcon(IconManager.URL).getImage());
		border.setImages(imgs);
		setBorder(border);
		getCollapseComponent().setBorder(border);
		if (n == 0) {
			add(layoutAddContent());
			revalidate();
			return;
		} 
		Collection urls = model.getUrls();
		Iterator i = urls.iterator();
		URLAnnotationData url;
		int index = 0;
		urlComponents = new HashMap<Integer, URLAnnotationData>();
		while (i.hasNext()) {
			url = (URLAnnotationData) i.next();
			if (!toRemove.contains(url)) {
				urlComponents.put(index, url);
				index++;
			}
		}
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
	 * Returns the collection of urls to add.
	 * @see AnnotationUI#getAnnotationToSave()
	 */
	protected List<AnnotationData> getAnnotationToSave()
	{
		List<AnnotationData> l = new ArrayList<AnnotationData>(); 
		Iterator i = areas.iterator();
		JTextField area;
		String value;
		while (i.hasNext()) {
			try {
				area = (JTextField) i.next();
				value = area.getText();
				if (value != null) {
					value = value.trim();
					if (value.length() > 0)
						l.add(new URLAnnotationData(value));
				}
				
			} catch (Exception e) {
				UserNotifier un = 
					MetadataViewerAgent.getRegistry().getUserNotifier();
				un.notifyInfo("New URL", "The URL entered does not " +
						"seem to be valid.");
			}
		}
		return l;
	}
	
	/**
	 * Returns <code>true</code> if annotation to save.
	 * @see AnnotationUI#hasDataToSave()
	 */
	protected boolean hasDataToSave()
	{
		if (getAnnotationToRemove().size() > 0) return true;
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
	}
	
	/**
	 * Clears the UI.
	 * @see AnnotationUI#clearDisplay()
	 */
	protected void clearDisplay() 
	{
		removeAll();
		areas.clear();
		toRemove.clear();
	}
	
	/**
	 * Adds the selected annotation to the collection of elements to remove.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		String s = e.getActionCommand();
		if (ADD_ACTION.equals(s)) {
			addURLArea();
		} else {
			int index = Integer.parseInt(s);
			URLAnnotationData url = urlComponents.get(index);
			if (url != null) {
				toRemove.add(url);
				firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
						Boolean.TRUE);
			}
			buildUI();
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
