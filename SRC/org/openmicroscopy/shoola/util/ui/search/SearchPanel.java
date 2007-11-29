/*
 * org.openmicroscopy.shoola.util.ui.search.SearchPanel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.search;


//Java imports
import java.awt.Font;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;



//Third-party libraries
import layout.TableLayout;
import com.toedter.calendar.JDateChooser;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;



/** 
 * 
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
class SearchPanel
	extends JPanel
{

	/** Font for progress bar label. */
	private static final Font	FONT = new Font("SansSerif", Font.ITALIC, 10);

	/** Example of authors. */
	private static final String	AUTHORS_EXAMPLE = "e.g. Swedlow";
	
	/** The selected date format. */
	private static final String	DATE_FORMAT = "MM/dd/yy";
	
	/** Possible time options. */
	private static String[]		dateOptions;
	
	static {
		dateOptions = new String[SearchContext.MAX+1];
		dateOptions[SearchContext.ANY_DATE] = "Any date";
		dateOptions[SearchContext.LAST_TWO_WEEKS] = "Last two weeks";
		dateOptions[SearchContext.LAST_MONTH] = "Last 30 days";
		dateOptions[SearchContext.LAST_TWO_MONTHS] = "Last 60 days";
		dateOptions[SearchContext.ONE_YEAR] = "1 year";
		dateOptions[SearchContext.RANGE] = "Specify date range " +
											"("+DATE_FORMAT+")";
	}

	/** Fields with the possible users. */
	private JTextField 				authors;
	
	/** Possible dates. */
	private JComboBox				dates;
	
	/** The terms to search for. */
	private JTextField				termsArea;
	
	/** Date used to specify the beginning of the time interval. */
	private JDateChooser			fromDate;
	
	/** Date used to specify the ending of the time interval. */
	private JDateChooser			toDate;
	
	/** Reference to the model .*/
	private SearchComponent 		model;
	
	/** Items used to defined the scope of the search. */
	private Map<Integer, JCheckBox>	scopes;
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		scopes = new HashMap<Integer, JCheckBox>(model.getNodes().size());
		IconManager icons = IconManager.getInstance();
		fromDate = new JDateChooser();
		fromDate.setIcon(icons.getImageIcon(IconManager.CALENDAR));
		fromDate.setDateFormatString(DATE_FORMAT);
		toDate = new JDateChooser();
		toDate.setDateFormatString(DATE_FORMAT);
		toDate.setIcon(icons.getImageIcon(IconManager.CALENDAR));
		fromDate.setEnabled(false);
		toDate.setEnabled(false);
		termsArea = new JTextField(20);
		authors = new JTextField(20);
		dates = new JComboBox(dateOptions);
		dates.addActionListener(model);
		dates.setActionCommand(""+SearchComponent.DATE);
	}
	
	/** 
	 * Builds the panel hosting the time fields
	 * 
	 * @return See above;
	 */
	private JPanel buildTimeRangePanel()
	{
		JPanel p = new JPanel();
		p.add(UIUtilities.setTextFont("From"));
		p.add(fromDate);
		p.add(UIUtilities.setTextFont("To"));
		p.add(toDate);
		return  UIUtilities.buildComponentPanel(p);
	}
	
	/** 
	 * Builds and lays out the component displaying the various options.
	 * 
	 * @return See above.
	 */
	private JPanel buildScopePanel()
	{
		JPanel p = new JPanel();
		double[] tl = {TableLayout.PREFERRED, 10, TableLayout.PREFERRED}; //rows
		TableLayout layout = new TableLayout();
		layout.setColumn(tl);
		
		List<SearchObject> nodes = model.getNodes();
		SearchObject n;
		
		
		int j = 0;
		int m = nodes.size();
		double[] rows;
		int size = 0;
		if (m%2 == 0) size = m-1;
		else size = m;
		rows = new double[size];
		for (int i = 0; i < rows.length; i++) {
			if (i%2 == 0) rows[i] = TableLayout.PREFERRED;
			else rows[i] = 5;
		}
		layout.setRow(rows);
		p.setLayout(layout);
		int row = -2;
		JCheckBox box;
		for (int i = 0; i < m; i++) {
			n = nodes.get(i);
			box = new JCheckBox(n.getDescription());
			if (i == 0) box.setSelected(true);
			if (i%2 == 0) {
				row = row+2;
				j = 0;
			} else j = 2;
			
			p.add(box, j+", "+row+", l, c");
			scopes.put(n.getIndex(), box);
		}
		p.setBorder(BorderFactory.createEtchedBorder());
		return p;
	}
	
	/** 
	 * Builds the panel hosting the authors.
	 * 
	 * @return See above.
	 */
	private JPanel buildSearchPanel()
	{
		//TODO: review layout
		JPanel searchPanel= new JPanel();
		double[][] tl = {{TableLayout.PREFERRED, 10, TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5,
			     TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, 
			     TableLayout.PREFERRED, 5, 
			     TableLayout.PREFERRED, TableLayout.PREFERRED} }; //rows
		searchPanel.setLayout(new TableLayout(tl));
		//Search For
		
		searchPanel.add(UIUtilities.setTextFont("Search For"), "0, 0, l, c");
		searchPanel.add(termsArea, "2, 0, l, c");
		
		//Author
		searchPanel.add(UIUtilities.setTextFont("Users"), "0, 2, l, c");
		searchPanel.add(authors, "2, 2, l, c");
		JLabel label = new JLabel(AUTHORS_EXAMPLE);
		label.setFont(FONT);
		searchPanel.add(label, "2, 4, l, c");
		
		//Date
		searchPanel.add(UIUtilities.setTextFont("Date"), "0, 6, l, c");
		searchPanel.add(UIUtilities.buildComponentPanel(dates), "2, 6, l, c");
		
		//From
		searchPanel.add(buildTimeRangePanel(), "0, 8, 2, 8");
		//Context
		searchPanel.add(UIUtilities.setTextFont("Scope"), "0, 10, l, c");
		searchPanel.add(buildScopePanel(), "0, 11, 2, 11");
		
		return UIUtilities.buildComponentPanel(searchPanel);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		add(buildSearchPanel());
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 *  @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	SearchPanel(SearchComponent model)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.model = model;
		initComponents();
		buildGUI();
	}

	/** Enables the date fields if the selected index is {@link #RANGE}. */
	void setDateIndex()
	{
		int index = dates.getSelectedIndex();
		fromDate.setEnabled(index == SearchContext.RANGE);
		if (fromDate.getDateEditor() != null);
			fromDate.getDateEditor().setEnabled(false);
		toDate.setEnabled(index == SearchContext.RANGE);
		if (toDate.getDateEditor() != null);
			toDate.getDateEditor().setEnabled(false);
	}
	
	/**
	 * Returns the currently selected time index.
	 * 
	 * @return See above.
	 */
	int getSelectedDate()
	{
		return dates.getSelectedIndex();
	}
	
	/**
	 * Returns the start time.
	 * 
	 * @return See above.
	 */
	Timestamp getFromDate()
	{
		Date d = fromDate.getDate();
		if (d == null) return null;
		return new Timestamp(d.getTime());
	}
	
	/**
	 * Returns the start time.
	 * 
	 * @return See above.
	 */
	Timestamp getToDate()
	{
		Date d = toDate.getDate();
		if (d == null) return null;
		return new Timestamp(d.getTime());
	}
	
	/** 
	 * Returns the scope of the search.
	 * 
	 * @return See above.
	 */
	List<Integer> getScope()
	{
		List<Integer> list = new ArrayList<Integer>();
		Iterator i = scopes.keySet().iterator();
		JCheckBox box;
		Integer key;
		while (i.hasNext()) {
			key = (Integer) i.next();
			box = scopes.get(key);
			if (box.isSelected())
				list.add(key);
		}
		return list;
	}
	
	/**
	 * Returns the terms to search for.
	 * 
	 * @return See above.
	 */
	List<String> getTerms() 
	{
		List<String> l = new ArrayList<String>();
		String text = termsArea.getText();
		if (text == null) return l;
		text = text.trim();
		String[] r = text.split(" ");
		for (int i = 0; i < r.length; i++)
			l.add(r[i].trim());
		return l;
	}
	
	/**
	 * Returns the collection of the possible users.
	 * 
	 * @return See above.
	 */
	List<String> getUsers()
	{
		List<String> l = new ArrayList<String>();
		String text = authors.getText();
		if (text == null) return l;
		text = text.trim();
		String[] r = text.split(" ");
		String value; 
		for (int i = 0; i < r.length; i++) {
			value = r[i];
			if (value != null) {
				value.trim();
				if ( value.length() != 0) l.add(value);
			}
		}
		return l;
	}
	
}
