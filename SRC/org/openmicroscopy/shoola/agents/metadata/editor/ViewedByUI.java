/*
 * org.openmicroscopy.shoola.agents.metadata.editor.ViewedByUI 
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;


//Third-party libraries
import org.jdesktop.swingx.JXBusyLabel;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.env.data.util.ViewedByDef;
import org.openmicroscopy.shoola.util.ui.RatingComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.AnnotationData;
import pojos.ImageData;
import pojos.RatingAnnotationData;

/** 
 * Displays the thumbnails and rating as seen by users.
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
class ViewedByUI 
	extends AnnotationUI
	implements ActionListener
{
    
	/** The title associated to this component. */
	private static final String 	TITLE = "Viewed by ";
	
	/** The dimension of the scroll pane. */
	private static final Dimension 	SCROLL_SIZE = new Dimension (100, 150);
	
	/** Indicates to lay out the nodes as a list. */
	private static final int		LIST_VIEW = 0;
	
	/** Indicates to lay out the nodes as a grid. */
	private static final int		GRID_VIEW = 1;
	
	/** The tool bar displayed when the component is expanded. */
	private JToolBar			displayBar;
	
	/** Button indicating to display the nodes in a list. */
	private JToggleButton		listView;
	
	/** Button indicating to display the nodes in a grid. */
	private JToggleButton		gridView;
	
	/** One of the constants defined by this class. */
	private int					layoutIndex;
	
	/** The UI displaying the thumbnail as a grid. */
	private JPanel				gridPane;
	
	/** The UI displaying the thumbnail as a list. */
	private JPanel				listPane;

	/** Flag indicating if the node is expanded or not. */
	private boolean				expanded;
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		layoutIndex = LIST_VIEW;
		IconManager icons = IconManager.getInstance();
		listView = new JToggleButton(icons.getIcon(IconManager.LIST_VIEW));
		listView.setToolTipText("View as List.");
		listView.setActionCommand(""+LIST_VIEW);
		listView.addActionListener(this);
		gridView = new JToggleButton(icons.getIcon(IconManager.GRID_VIEW));
		gridView.setToolTipText("View as Gid");
		gridView.setActionCommand(""+GRID_VIEW);
		gridView.addActionListener(this);
		listView.setSelected(true);
		ButtonGroup group = new ButtonGroup();
		group.add(listView);
		group.add(gridView);
		displayBar = new JToolBar();
		displayBar.setBorder(null);
		displayBar.setFloatable(false);
		displayBar.add(UIUtilities.setTextFont("Display: "));
		displayBar.add(Box.createHorizontalStrut(5));
		displayBar.add(listView);
		displayBar.add(gridView);
		expanded = false;
	}
	
	/** 
	 * Builds an element of the list pane.
	 * 
	 * @param img	The thumbnail to display.
	 * @param id	The id of the user.
	 * @return See above.
	 */
	private JPanel buildListItem(BufferedImage img, long id)
	{
		JPanel p = new JPanel();
		ViewedByDef def = model.getViewedDef(id);
		p.add(new ThumbnailCanvas(model, img, def));
		
		String name = model.formatOwner(def.getExperimenter());
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.add(new JLabel(name));
		p.add(content);
		return UIUtilities.buildComponentPanel(p, 0, 0);
	}
	
	/** 
	 * Builds an element of the grid pane.
	 * 
	 * @param img	The thumbnail to display.
	 * @param id	The id of the user.
	 * @return See above.
	 */
	private JPanel buildGridItem(BufferedImage img, long id)
	{
		JPanel p = new JPanel();

		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
	    c.gridy = 0;
		ViewedByDef def = model.getViewedDef(id);
		p.add(new ThumbnailCanvas(model, img, def), c);
		c.gridy++;
		String name = model.formatOwner(def.getExperimenter());
		Collection ratings = def.getRatings();
		int value = 0;
		if (ratings != null && ratings.size() != 0) {
			Iterator i = ratings.iterator();
			while (i.hasNext()) 
				value = ((RatingAnnotationData) i.next()).getRating();
		}
		RatingComponent rate = new RatingComponent(value, 
								RatingComponent.MEDIUM_SIZE, false);
		
		JPanel content = new JPanel();
		content.setLayout(new GridBagLayout());
		GridBagConstraints cg = new GridBagConstraints();
		cg.gridx = 0;
	    cg.gridy = 0;
		int width = img.getWidth();
		int w = content.getFontMetrics(content.getFont()).charWidth('m');
		if (name.length()*w > width) {
			String[] values = name.split(" ");
			for (int i = 0; i < values.length; i++) {
				cg.gridy++;
				content.add(new JLabel(values[i]), cg);
			}
		} else content.add(new JLabel(name), cg);
		cg.gridy++;
		content.add(rate, cg);
		p.add(content, c);
		return UIUtilities.buildComponentPanel(p);
	}
	
	/** 
	 * Builds and lays out the nodes as a list. 
	 * 
	 * @return See above.
	 */
	private JPanel layoutList()
	{
		listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
		Map<Long, BufferedImage> thumbnails = model.getThumbnails();
		Set set = thumbnails.entrySet();
		Entry entry;
		Iterator i = set.iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			listPane.add(buildListItem((BufferedImage) entry.getValue(), 
					(Long) entry.getKey()));
		}
		return UIUtilities.buildComponentPanel(listPane);
	}
	
	/** 
	 * Builds and lays out the nodes as a grid. 
	 * 
	 * @return See above.
	 */
	private JPanel layoutGrid()
	{
		gridPane = new JPanel();
		gridPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		Map<Long, BufferedImage> thumbnails = model.getThumbnails();
		List<JPanel> thumbs = new ArrayList<JPanel>(thumbnails.size());
		Set set = thumbnails.entrySet();
		Entry entry;
		Iterator it = set.iterator();
		while (it.hasNext()) {
			entry = (Entry) it.next();
			thumbs.add(buildGridItem((BufferedImage) entry.getValue(), 
					(Long) entry.getKey()));
		}
		int n = (thumbs.size()/3)+1;
	    it = thumbs.iterator();
	    c.gridx = 0;
	    c.gridy = 0;
	    c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
	    JPanel comp;
	    for (int i = 0; i < 3; ++i) {
	    	c.gridx = 0;
            for (int j = 0; j < n; ++j) {
                if (!it.hasNext()) //Done, less than n^2 children.
                    break;  //Go to finally
                comp = (JPanel) it.next();
                c.gridx += j;
                c.gridy += i;
                gridPane.add(comp, c);
            }
        }    
		
		return UIUtilities.buildComponentPanel(gridPane);
	}
	
	/** 
	 * Builds the loading component.
	 * 
	 * @return See above.
	 */
	private JPanel buildLoadingPane()
	{
		JPanel p = new JPanel();
		JXBusyLabel label = new JXBusyLabel();
		label.setEnabled(true);
		label.setBusy(true);
	    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        //p.setBorder(BorderFactory.createEtchedBorder());
        p.add(new JLabel("Loading..."));
        p.add(UIUtilities.buildComponentPanelRight(label));
		return p;
	}
	
	/**
	 * Builds and lays out a component displaying a message.
	 * 
	 * @return See above.
	 */
	private JPanel buildEmptyPane()
	{
		JPanel p = new JPanel();
	    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel("No views available."));
		return p;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	ViewedByUI(EditorModel model)
	{
		super(model);
		title = TITLE;
		initComponents();
	}
	
	/**
	 * Sets to <code>true</code> if the node is expanded, 
	 * <code>false</code> otherwise.
	 * 
	 * @param expanded The value to set.
	 */
	void setExpanded(boolean expanded) { this.expanded = expanded; }
	
	/**
	 * Returns <code>true</code> if the node is expanded, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isExpanded() { return expanded; }
	
	/**
	 * Overridden to lay out the nodes depending on the selected layout index.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		title = TITLE+LEFT+model.getViewedByCount()+RIGHT;
		removeAll();
		if (model.getRefObject() instanceof ImageData) {
			if (model.isThumbnailsLoaded()) {
				if (model.getThumbnails().size() == 0) {
					add(buildEmptyPane(), Component.CENTER_ALIGNMENT);
				} else {
					//add(UIUtilities.buildComponentPanelRight(displayBar), 
					//					Component.RIGHT_ALIGNMENT);
					JScrollPane pane;
					switch (layoutIndex) {
						case LIST_VIEW:
							pane = new JScrollPane(layoutList()); 
							pane.setPreferredSize(SCROLL_SIZE);
							add(pane, Component.CENTER_ALIGNMENT);
							break;
						case GRID_VIEW:
							pane = new JScrollPane(layoutGrid()); 
							pane.setPreferredSize(SCROLL_SIZE);
							add(pane, Component.CENTER_ALIGNMENT);
					}
				}
			} else {
				if (expanded)
					add(buildLoadingPane(), Component.CENTER_ALIGNMENT);
			}
		}
		revalidate();
		repaint();
	}
	
	/**
	 * Overridden to set the title of the component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return title; }

	/**
	 * Returns <code>null</code> because data are not editable.
	 * @see AnnotationUI#getAnnotationToRemove()
	 */
	protected List<AnnotationData> getAnnotationToRemove() { return null; }

	/**
	 * Returns <code>null</code> because data are not editable.
	 * @see AnnotationUI#getAnnotationToSave()
	 */
	protected List<AnnotationData> getAnnotationToSave() { return null; }
	
	/**
	 * Returns <code>false</code> because data are not editable.
	 * @see AnnotationUI#hasDataToSave()
	 */
	protected boolean hasDataToSave() { return false; }
	
	/**
	 * Clears the UI.
	 * @see AnnotationUI#clearDisplay()
	 */
	protected void clearDisplay()
	{ 
		removeAll(); 
		revalidate();
	}
	
	/**
	 * Clears the data to save.
	 * @see AnnotationUI#clearData()
	 */
	protected void clearData() {}
	
	/**
	 * Sets the title of the component.
	 * @see AnnotationUI#setComponentTitle()
	 */
	protected void setComponentTitle() {}
	
	/**
	 * Modifies the layout of the thumbnails.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			default:
			case LIST_VIEW:
				layoutIndex = LIST_VIEW;
				break;
			case GRID_VIEW:
				layoutIndex = GRID_VIEW;
		}
		buildUI();
	}

}
