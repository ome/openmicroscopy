/*
 * org.openmicroscopy.shoola.env.ui.ActivityResultDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.ui;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;


//Third-party libraries
import info.clearthought.layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Dialog displaying the results.
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
class ActivityResultDialog 
	extends JDialog
	implements ActionListener, PropertyChangeListener
{
	
	/** Identifier indicating to close the dialog. */
	private static final int CLOSE = 0;
	
	/** Reference to the activity. */
	private ActivityComponent activity;
	
	/** The result to handle. */
	private Object result;
	
	/** Button to close the dialog. */
	private JButton closeButton;
	
	/** Initializes the component. */
	private void initComponents()
	{
		closeButton = new JButton("Close");
		closeButton.setToolTipText("Close the dialog.");
		closeButton.addActionListener(this);
		closeButton.setActionCommand(""+CLOSE);
	}
	
	/**
	 * Builds and lays out the controls.
	 * 
	 * @return See above.
	 */
	private JPanel buildStatusBar()
	{
		JPanel p = new JPanel();
		p.add(closeButton);
		JPanel bar = UIUtilities.buildComponentPanelRight(p);
		bar.setBorder(new LineBorder(Color.LIGHT_GRAY));
		return bar;
	}
	
	/** Lays out the result. */
	private JPanel layoutResult()
	{
		JPanel p = new JPanel();
		p.setBackground(UIUtilities.BACKGROUND);
		Iterator i;
		TableLayout layout = new TableLayout();
		p.setLayout(layout);
		int index = 0;
		Object r;
		ActivityResultRow c;
		if (result instanceof Collection) {
			double[] size = {TableLayout.FILL};
			layout.setColumn(size);
			i = ((Collection) result).iterator();
			while (i.hasNext()) {
				layout.insertRow(index, TableLayout.PREFERRED);
				r = i.next();
				c = new ActivityResultRow(r, activity);
				p.add(c, "0, "+index+", FULL, CENTER");
				if (index%2 == 0)
					c.setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
				else 
					c.setBackground(UIUtilities.BACKGROUND_COLOUR_ODD);
				index++;
			}
		} else if (result instanceof Map) {
			Entry entry;
			JLabel l;
			String key;
			double[] size = {TableLayout.PREFERRED, 5, TableLayout.FILL};
			layout.setColumn(size);
			JPanel pp, empty;
			i = ((Map) result).entrySet().iterator();
			Dimension d;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				key = (String) entry.getKey();
				layout.insertRow(index, TableLayout.PREFERRED);
				
				c = new ActivityResultRow(key, entry.getValue(), activity);
				c.addPropertyChangeListener(this);
				empty = new JPanel();
				pp = UIUtilities.buildComponentPanel(
						UIUtilities.setTextFont(key+": "), 0, 0);
				d = c.getPreferredSize();
				d = new Dimension(empty.getPreferredSize().width, d.height);
				empty.setPreferredSize(d);
				empty.setSize(d);
				d = new Dimension(pp.getPreferredSize().width, d.height);
				pp.setPreferredSize(d);
				pp.setSize(d);
				if (index%2 == 0) {
					pp.setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
					c.setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
					empty.setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
				} else {
					pp.setBackground(UIUtilities.BACKGROUND_COLOUR_ODD);
					c.setBackground(UIUtilities.BACKGROUND_COLOUR_ODD);
					empty.setBackground(UIUtilities.BACKGROUND_COLOUR_ODD);
				}
				p.add(pp, "0, "+index+", FULL, CENTER");
				p.add(empty, "1, "+index+", FULL, CENTER");
				p.add(c, "2, "+index+", FULL, CENTER");
				index++;
			}
		}
		return p;
	}
	
	/** 
	 * Builds and lays out the UI. 
	 * 
	 * @param index One of the constants defined by this class.
	 */
	private void buildGUI()
	{
		Container c = getContentPane();
		String title = "Results";
		String text = "Follow the results returned.";
		
		if (result instanceof Map) {
			Map m = (Map) result;
			if (m.containsKey(ActivityComponent.STD_ERR)) {
				title = "Errors";
				text = "Follow the errors returned.";
			}
		}
		if (activity instanceof DeleteActivity) {
			title = "Errors";
			text = "Follow the errors returned.";
		}
		TitlePanel tp = new TitlePanel(title, text, IconManager.getResults());
		c.setBackground(UIUtilities.BACKGROUND_COLOR);
		JScrollPane pane = new JScrollPane(layoutResult());
		pane.getViewport().setBackground(UIUtilities.BACKGROUND_COLOR);
		pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		pane.setBackground(UIUtilities.BACKGROUND_COLOR);
		pane.setOpaque(false);
		pane.setBorder(new LineBorder(Color.LIGHT_GRAY));
		c.add(tp, BorderLayout.NORTH);
		c.add(pane, BorderLayout.CENTER);
		c.add(buildStatusBar(), BorderLayout.SOUTH);
	}
	
	/** Closes and disposes. */
	private void close()
	{
		setVisible(false);
		dispose();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of this dialog.
	 * @param activity The activity of reference.
	 * @param result The result to handle.
	 */
	ActivityResultDialog(JFrame owner, ActivityComponent activity, Object 
			result)
	{
		this(owner, activity, result, -1);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of this dialog.
	 * @param activity The activity of reference.
	 * @param result The result to handle.
	 * @param index One of the constants defined by this class.
	 */
	ActivityResultDialog(JFrame owner, ActivityComponent activity, Object 
			result, int index)
	{
		super(owner);
		if (activity == null) 
			throw new IllegalArgumentException("No activity to handle.");
		if (result == null)
			throw new IllegalArgumentException("No result to handle.");
		this.result = result;
		this.activity = activity;
		initComponents();
		buildGUI();
		pack();
	}

	/**
	 * Closes the dialog.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CLOSE:
				close();
				break;
		}
	}

	/** 
	 * Listens to the property indicating that an action to download 
	 * or view the object.
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (ActivityResultRow.ACTION_PROPERTY.equals(name)) close();
	}
	
}
