/*
 * org.openmicroscopy.shoola.agents.dataBrowser.util.FilteringDialog 
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
package org.openmicroscopy.shoola.agents.dataBrowser.util;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;


//Third-party libraries
import layout.TableLayout;
import org.jdesktop.swingx.JXDatePicker;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import org.openmicroscopy.shoola.util.ui.RatingComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.search.SearchUtil;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;

/** 
 * Modal dialog used to filter data.
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
public class FilteringDialog 
	extends JDialog
	implements ActionListener
{

	/** Bound property indicating to filter the data. */
	public static final String	FILTER_PROPERTY = "filter";
	
	/** The title of the dialog. */
	private static final String TITLE = "Filtering and Searching";
	
	/** Action id indicating to close the dialog. */
	private static final int	CANCEL = 0;
	
	/** Action id indicating to filter the data. */
	private static final int	FILTER = 1;
	
	/** ID to filter by higher rate. */
	private static final int	HIGHER_RATING = 0;
	
	/** ID to filter by lower rate. */
	private static final int	LOWER_RATING = 1;
	
	/** ID to filter by the exact rate. */
	private static final int	EXACT_RATING = 2;
	
	/** The maximum number of options. */
	private static final int	MAX = 2;
	
	/** Store the rating options. */
	private static final String[]	RATING;
	
	static {
		RATING = new String[MAX+1];
		RATING[HIGHER_RATING] = "greater or equal to";
		RATING[LOWER_RATING] = "lower or equal to";
		RATING[EXACT_RATING] = "equal to";
	}
	
	/** The component to select the rating. */
	private JCheckBox		ratingBox;
	
	/** The component to select the comments. */
	private JCheckBox		commentsBox;
	
	/** The component to select the tags. */
	private JCheckBox		tagsBox;
	
	/** The component to select a time interval. */
	private JCheckBox		calendarBox;
	
	/** Used to select one the rating options. */
	private JComboBox		ratingOptions;
	
	/** Date used to specify the beginning of the time interval. */
	private JXDatePicker	fromDate;
	
	/** Date used to specify the ending of the time interval. */
	private JXDatePicker	toDate;
	
	/** The rating component. */
	private RatingComponent	rating;
	
	/** The field area. */
	private JTextField		area;
	
	/** Button to close the dialog. */
	private JButton			cancelButton;
	
	/** Button to filter the data. */
	private JButton			filterButton;
	
	/** Sets the properties of the dialog. */
	private void setProperties()
	{
		setTitle(TITLE);
		setModal(true);
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		ratingBox = new JCheckBox("Rating:");
		ratingBox.setSelected(true);
		calendarBox = new JCheckBox("Calendar:");
		commentsBox = new JCheckBox("Comments");
		tagsBox = new JCheckBox("Tags");
		ratingOptions = new JComboBox(RATING);
		rating = new RatingComponent(5, RatingComponent.HIGH_SIZE);
		fromDate = UIUtilities.createDatePicker();
		toDate = UIUtilities.createDatePicker();
		area = new JTextField();
		area.setColumns(15);
		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText("Close the dialog.");
		cancelButton.setActionCommand(""+CANCEL);
		cancelButton.addActionListener(this);
		filterButton = new JButton("Filter");
		filterButton.setToolTipText("Filter the data.");
		filterButton.setActionCommand(""+FILTER);
		filterButton.addActionListener(this);
		getRootPane().setDefaultButton(filterButton);
	}

	/** Organizes the filtering parameters. */
	private void filter()
	{
		FilterContext context = new FilterContext();
		boolean filter = false;
		if (ratingBox.isSelected()) {
			int index = -1;
			switch (ratingOptions.getSelectedIndex()) {
				case HIGHER_RATING:
					index = FilterContext.HIGHER;
					break;
				case LOWER_RATING:
					index = FilterContext.LOWER;
					break;
				case EXACT_RATING:
					index = FilterContext.EQUAL;
			};
			filter = true;
			context.setRate(index, rating.getCurrentValue());
		}
		if (calendarBox.isSelected()) {
			Date d = fromDate.getDate();
			Timestamp start = null;
			if (d != null) start =  new Timestamp(d.getTime());
			Timestamp end = null;
			d = toDate.getDate();
			if (d != null) end =  new Timestamp(d.getTime());
			context.setTimeInterval(start, end);
			filter = true;
		}
		if (tagsBox.isSelected()) {
			context.addAnnotationType(TagAnnotationData.class);
			filter = true;
		}
		if (commentsBox.isSelected()) {
			context.addAnnotationType(TextualAnnotationData.class);
			filter = true;
		}
		List<String> l = SearchUtil.splitTerms(area.getText(), 
				SearchUtil.COMMA_SEPARATOR);
		context.setTerms(l);
		//Get the text to filter by
		if (filter)
			firePropertyChange(FILTER_PROPERTY, null, context);
		setVisible(false);
	}
	
	/**
	 * Builds and lays out the components used to select the rating level.
	 * 
	 * @return See above.
	 */
	private JPanel buildRatingPane()
	{
		JPanel p = new JPanel();
		p.add(ratingBox);
		p.add(ratingOptions);
		p.add(rating);
		return UIUtilities.buildComponentPanel(p, 0, 0);
	}
	
	/**
	 * Builds and lays out the components used to select a time range.
	 * 
	 * @return See above.
	 */
	private JPanel buildCalendarPane()
	{
		JPanel p = new JPanel();
		p.add(calendarBox);
		p.add(UIUtilities.setTextFont("From: "));
		p.add(fromDate);
		p.add(UIUtilities.setTextFont("To: "));
		p.add(toDate);
		return UIUtilities.buildComponentPanel(p, 0, 0);
	}
	
	/**
	 * Builds the component displaying the area.
	 * 
	 * @return See above.
	 */
	private JPanel buildAreaPane()
	{
		//TODO: Add intersection and union option
		return UIUtilities.buildComponentPanelRight(area);
	}
	
	/** 
	 * Builds the selection component.
	 * 
	 * @return See above.
	 */
	private JPanel buildSelectionPane()
	{
		JPanel p = new JPanel();
		double[][] size = {{TableLayout.FILL}, 
				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5,
				TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, 
				TableLayout.PREFERRED, 5}};
		p.setLayout(new TableLayout(size));
		int i = 0;
		p.add(buildAreaPane(), "0, "+i);
		i++;
		p.add(new JSeparator(JSeparator.HORIZONTAL), "0, "+i);
		i++;
		p.add(buildRatingPane(), "0, "+i);
		i++;
		p.add(new JSeparator(JSeparator.HORIZONTAL), "0, "+i);
		i++;
		p.add(buildCalendarPane(), "0, "+i);
		i++;
		p.add(new JSeparator(JSeparator.HORIZONTAL), "0, "+i);
		i++;
		p.add(UIUtilities.buildComponentPanel(tagsBox), "0, "+i);
		i++;
		p.add(new JSeparator(JSeparator.HORIZONTAL), "0, "+i);
		i++;
		p.add(UIUtilities.buildComponentPanel(commentsBox), "0, "+i);
		i++;
		p.add(new JSeparator(JSeparator.HORIZONTAL), "0, "+i);
		return p;
	}
	
	/**
	 * Builds and lays out the tool bar.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar()
	{
		JPanel p = new JPanel();
		p.add(filterButton);
		p.add(Box.createHorizontalStrut(5));
		p.add(cancelButton);
		return UIUtilities.buildComponentPanelRight(p);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		//IconManager icons = IconManager.getInstance();
		//TitlePanel tp = new TitlePanel(TITLE, "foo", 
		//				icons.getIcon(IconManager.FILTERING_48));
		Container c = getContentPane();
		//c.add(tp, BorderLayout.NORTH);
		c.add(buildSelectionPane(), BorderLayout.CENTER);
		c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the frma.ce
	 */
	public FilteringDialog(JFrame owner)
	{
		super(owner);
		setProperties();
		initComponents();
		buildGUI();
		pack();
	}

	/**
	 * Reacts to various controls.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CANCEL:
				setVisible(false);
				break;
			case FILTER:
				filter();
		}
	}
	
}
