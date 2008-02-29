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
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import layout.TableLayout;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.TitledLineBorder;
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
	implements ActionListener
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
		
	}
	
	/**
	 * Lays out the URL annotation.
	 * 
	 * @return See above.
	 */
	private JPanel layoutURL()
	{
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("URL"));
		TableLayout layout = new TableLayout();
		p.setLayout(layout);
		double[] columns = {TableLayout.PREFERRED, 5, TableLayout.PREFERRED};
		layout.setColumn(columns);
		for (int j = 0; j < 2*urlComponents.size()-1; j++) {
			if (j%2 == 0) layout.insertRow(j, TableLayout.PREFERRED);
			else layout.insertRow(j, 5);
		}
		Iterator i = urlComponents.keySet().iterator();
		int index;
		URLAnnotationData url;
		String s;
		IconManager icons = IconManager.getInstance();
		Icon icon = icons.getIcon(IconManager.REMOVE);
		JButton button;
		labels = new HashMap<JLabel, URLAnnotationData>();
		while (i.hasNext()) {
			index = (Integer) i.next();
			url = urlComponents.get(index);
			if (model.isCurrentUserOwner(url)) {
				s = "2, "+index+", f, c";
				button = new JButton(icon);
				button.setBorder(null);
				button.setToolTipText("Remove");
				button.setActionCommand(""+index);
				button.addActionListener(this);
				p.add(button, s);
			}
			s = "0, "+index+", f, c";
			JLabel label = new JLabel(UIUtilities.formatURL(url.getURL()));
			label.setToolTipText("Added by "+model.formatToolTip(url));
			labels.put(label, url);
			label.addMouseListener(new MouseAdapter() {
			
				public void mouseReleased(MouseEvent e) {
					JLabel l = (JLabel) e.getSource();
					URLAnnotationData url = labels.get(l);
					if (url != null)
						browse(url.getURL());
				}
			
			});
			p.add(label, s);
		}
		return p;
	}
	
	/** Adds a new url area only if the previously added one has been used. */
	private void addURLArea()
	{
		JTextField area;
		Iterator i;
		addedContent.removeAll();
		if (areas.size() == 0) {
			area = new JTextField();
			UIUtilities.setTextAreaDefault(area);
			areas.add(area);
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
			if (!empty) {
				area = new JTextField();
				UIUtilities.setTextAreaDefault(area);
				areas.add(area);
			}
		}
		
		TableLayout layout = new TableLayout();
		addedContent.setLayout(layout);
		double[] columns = {TableLayout.PREFERRED, 5, TableLayout.FILL};
		layout.setColumn(columns);
		for (int j = 0; j < 2*areas.size()-1; j++) {
			if (j%2 == 0) layout.insertRow(j, TableLayout.PREFERRED);
			else layout.insertRow(j, 5);
		}
		int index = 0;
		String s;
		i = areas.iterator();
		while (i.hasNext()) {
			area = (JTextField) i.next();
			s = "0, "+index+", f, c";
			addedContent.add(UIUtilities.setTextFont("URL: "), s);
			s = "2, "+index+", f, c";
			addedContent.add(area, s);
			index = index+2;
		}
		addedContent.revalidate();
	}
	
	/**
	 * Lays out the components used to add new <code>URL</code>s.
	 * 
	 * @return See above.
	 */
	private JPanel layoutAddContent()
	{
		 JPanel content = new JPanel();
		 double[][] tl = {{TableLayout.PREFERRED, 5, TableLayout.FILL}, //columns
				 {TableLayout.PREFERRED, 60} }; //rows
		 TableLayout layout = new TableLayout(tl);
		 content.setLayout(layout);
		 content.add(addButton, "0, 0, f, c");
		 JScrollPane pane = new JScrollPane(addedContent);
		 pane.setOpaque(false);
		 pane.setBorder(null);
		 content.add(pane, "2, 0, 2, 1");
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
		int n = model.getUrlsCount()-toRemove.size();
		title = TITLE+LEFT+n+RIGHT;
		Border border = new TitledLineBorder(title, getBackground());
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
		add(layoutAddContent());
		revalidate();
	}
	
	/**
	 * Overridden to set the title of the component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return title; }

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
			if (url != null) toRemove.add(url);
			buildUI();
		}
	}
	
}
