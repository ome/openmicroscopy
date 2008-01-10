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
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;



//Third-party libraries
import layout.TableLayout;
import com.toedter.calendar.JDateChooser;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;



/** 
 * The Componnent hosting the various fields used to collect the 
 * context of the search.
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

	/** The preferred size of the calendar popup. */
	private static final Dimension	CALENDAR_SIZE = new Dimension(250, 200);
	
	/** The selected date format. */
	private static final String		DATE_FORMAT = "YYYY/MM/DD";//"MM/dd/yy";
	
	/** The tooltip of the calendar button. */
	private static final String		DATE_TOOLTIP = "Bring up a calendar.";
	
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

	/** If checked the case is taken into account. */ 
	private JCheckBox				caseSensitive;
	
	/** The button used to display the available users. */
	private JButton					userButton;
	
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
	
	/** Button to only retrieve the current user's data. */
	private JRadioButton			currentUser;
	
	/** Button to only retrieve the current user and selected users' data. */
	private JRadioButton			currentUserAndOthers;
	
	/** Button to only retrieve the current user and selected users' data. */
	private JRadioButton			others;
	
	/** Button to put <code>AND</code> between terms. */
	private JRadioButton			andBox;
	
	/** Button to put <code>OR</code> between terms. */
	private JRadioButton			orBox;
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		scopes = new HashMap<Integer, JCheckBox>(model.getNodes().size());
		IconManager icons = IconManager.getInstance();
		fromDate = new JDateChooser();
		JButton b = fromDate.getCalendarButton();
		b.setToolTipText(DATE_TOOLTIP);
		b.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		fromDate.setIcon(icons.getImageIcon(IconManager.CALENDAR));
		fromDate.setDateFormatString(DATE_FORMAT);
		fromDate.getJCalendar().setPreferredSize(CALENDAR_SIZE);
		toDate = new JDateChooser();
		toDate.getCalendarButton().setToolTipText(DATE_TOOLTIP);
		toDate.setDateFormatString(DATE_FORMAT);
		b = toDate.getCalendarButton();
		b.setToolTipText(DATE_TOOLTIP);
		b.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		toDate.setIcon(icons.getImageIcon(IconManager.CALENDAR));
		toDate.getJCalendar().setPreferredSize(CALENDAR_SIZE);
		fromDate.setEnabled(false);
		toDate.setEnabled(false);
		termsArea = new JTextField(20);
		authors = new JTextField(20);
		authors.setEditable(false);
		dates = new JComboBox(dateOptions);
		dates.addActionListener(model);
		dates.setActionCommand(""+SearchComponent.DATE);
		userButton = new JButton(icons.getIcon(IconManager.OWNER));
		userButton.setToolTipText("Select users");
		UIUtilities.unifiedButtonLookAndFeel(userButton);
		userButton.addActionListener(model);
		userButton.setActionCommand(""+SearchComponent.OWNER);
		
		currentUser = new JRadioButton("just me");
		currentUser.setSelected(true);
		currentUserAndOthers = new JRadioButton("me and others");
		others = new JRadioButton("just others");
		ButtonGroup group = new ButtonGroup();
		group.add(currentUser);
		group.add(currentUserAndOthers);
		group.add(others);
		group = new ButtonGroup();
		andBox = new JRadioButton("And");
		orBox = new JRadioButton("Or");
		orBox.setSelected(true);
		group.add(andBox);
		group.add(orBox);
		caseSensitive = new JCheckBox("Case sensitive");
	}
	
	/** 
	 * Displaying the term separator options.
	 * 
	 * @return See above.
	 */
	private JPanel buildAndOrPanel()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(andBox);
		p.add(Box.createRigidArea(SearchComponent.H_SPACER_SIZE));
		p.add(orBox);
		return UIUtilities.buildComponentPanel(p);
	}
	
	/** 
	 * Builds the panel hosting the time fields.
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
	 * Builds the panel hosting the user selection.
	 * 
	 * @return See above.
	 */
	public JPanel buildUserSelectionPanel()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(currentUser);
		p.add(currentUserAndOthers);
		p.add(others);
		return UIUtilities.buildComponentPanel(p);
	}
	
	/** 
	 * Builds and lays out the component displaying the various options.
	 * 
	 * @return See above.
	 */
	private JPanel buildScopePanel()
	{
		JPanel p = new JPanel();
		double[] tl = {TableLayout.PREFERRED, 10, TableLayout.PREFERRED}; //columns
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
		TitledBorder border = new TitledBorder("Scope");
		border.setTitleFont(p.getFont().deriveFont(Font.BOLD));
		p.setBorder(border);
		return p;
	}
	
	/**
	 * Builds the UI component hosting the terms to search for.
	 * 
	 * @return See above.
	 */
	private JPanel buildSearchFor()
	{
		JPanel searchFor = new JPanel();
		searchFor.setLayout(new BoxLayout(searchFor, BoxLayout.Y_AXIS));
		initBorder("Search For", searchFor);
		JPanel p = new JPanel();
		p.add(new JLabel("(* = any string, ? = any character)"));
		searchFor.add(UIUtilities.buildComponentPanel(p));
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(termsArea);
		p.add(caseSensitive);
		searchFor.add(UIUtilities.buildComponentPanel(p));
		searchFor.add(buildAndOrPanel());
		return searchFor;
	}
	
	/**
	 * Builds the UI component hosting the users to search for.
	 * 
	 * @return See above.
	 */
	private JPanel buildUsers()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		
		p.add(authors);
		p.add(userButton);
		JPanel usersPanel= new JPanel();
		initBorder("Users", usersPanel);
		usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.Y_AXIS));
		usersPanel.add(UIUtilities.buildComponentPanel(p));
		usersPanel.add(buildUserSelectionPanel());
		return usersPanel;
	}
	
	/**
	 * Builds the UI component hosting the time interval.
	 * 
	 * @return See above.
	 */
	private JPanel buildDate()
	{
		JPanel p = new JPanel();
		
		
		p.add(UIUtilities.buildComponentPanel(dates));
		JPanel datesPanel= new JPanel();
		datesPanel.setLayout(new BoxLayout(datesPanel, BoxLayout.Y_AXIS));
		initBorder("Date", datesPanel);
		datesPanel.add(UIUtilities.buildComponentPanel(p));
		//From
		datesPanel.add(buildTimeRangePanel());
		return datesPanel;
	}
	
	/**
	 * Formats and sets the title border of the passed component.
	 * 
	 * @param title The title.
	 * @param p		The component to handle.
	 */
	private void initBorder(String title, JComponent p)
	{
		TitledBorder border = new TitledBorder(title);
		border.setTitleFont(p.getFont().deriveFont(Font.BOLD));
		p.setBorder(border);
	}
	
	/** 
	 * Builds the panel hosting the authors.
	 * 
	 * @return See above.
	 */
	private JPanel buildSearchPanel()
	{
		JPanel searchPanel= new JPanel();
		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
		searchPanel.add(buildSearchFor());
		searchPanel.add(buildUsers());
		//From
		searchPanel.add(buildDate());
		//Context
		
		searchPanel.add(buildScopePanel());
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
	 * Returns <code>true</code> if the search is case sensitive,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isCaseSensitive() { return caseSensitive.isSelected(); }
	
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
		return SearchUtil.splitTerms(termsArea.getText(), 
				SearchUtil.SEARCH_SEPARATOR);
		/*
		List<String> l = new ArrayList<String>();
		String text = termsArea.getText();
		if (text == null) return l;
		text = text.trim();
		String value;
		String[] r = text.split(" ");
		for (int i = 0; i < r.length; i++) {
			value = r[i];
			if (value != null) {
				value.trim();
				if ( value.length() != 0) l.add(value);
			}
		}
		return l;
		*/
	}
	
	/**
	 * Returns the context of the search for users.
	 * 
	 * @return See above.
	 */
	int getUserSearchContext()
	{
		if (currentUser.isSelected()) return SearchContext.JUST_CURRENT_USER;
		if (currentUserAndOthers.isSelected()) 
			return SearchContext.CURRENT_USER_AND_OTHERS;
		return SearchContext.JUST_OTHERS;
	}
	
	/**
	 * Returns the collection of the possible users.
	 * 
	 * @return See above.
	 */
	List<String> getUsers()
	{
		if (getUserSearchContext() == SearchContext.JUST_CURRENT_USER)
			return new ArrayList<String>();
		return SearchUtil.splitTerms(authors.getText(), 
									SearchUtil.SEARCH_SEPARATOR);
	}
	
	/**
	 * Returns either <code>and</code> or <code>or</code>.
	 * 
	 * @return See above.
	 */
	String getSeparator()
	{
		if (orBox.isSelected()) return "or";
		return "and";
	}
	
	/** Indicates to set the focus on the search area. */
	void setFocusOnSearch()
	{
		if (termsArea != null) termsArea.requestFocus();
	}
	
	/**
	 * Sets the name of the selected user.
	 * 
	 * @param name The string to set.
	 */
	void setUserString(String name)
	{
		String text = authors.getText();
		String[] values = text.split(SearchUtil.SEARCH_SEPARATOR);
		String n;
		String v = "";
		boolean exist = false;
		int l = values.length;
		for (int i = 0; i < l; i++) {
			n = values[i].trim();
			if (n.length() >0) {
				v += n;
				if (name.equals(n)) {
					if (i != (l-1))
						v += SearchUtil.SEARCH_SEPARATOR
								+SearchUtil.NAME_SEPARATOR;
					exist = true;
				} else v += SearchUtil.SEARCH_SEPARATOR
							+SearchUtil.NAME_SEPARATOR;
			}
		}
		if (!exist) v += name;
		else v = v.substring(0, text.length()-1);
		authors.setText(v);
	}
	
}
