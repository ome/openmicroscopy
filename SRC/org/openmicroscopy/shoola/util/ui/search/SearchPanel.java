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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

//Third-party libraries
import com.sun.corba.se.pept.transport.OutboundConnectionCache;
import com.toedter.calendar.JDateChooser;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.OutlookBar;
import org.openmicroscopy.shoola.util.ui.UIUtilities;



/** 
 * The Component hosting the various fields used to collect the 
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

	/** Wild card used. */
	private static final String		WILD_CARD = 
									"(* = any string, ? = any character)";
	
	/** Description of the {@link #allTermsArea}. */
	private static final String		ALL_WORDS = 
										"<html><b>All</b> the words</html>";
	
	/** Description of the {@link #exactPhraseArea}. */
	private static final String		EXACT_WORDS = 
										"<html><b>Exact</b> phrase</html>";
	
	/** Description of the {@link #atLeastTermsArea}. */
	private static final String		AT_LEAST_WORDS = 
								"<html><b>At least one</b> of the words</html>";
	
	/** Description of the {@link #withoutTermsArea}. */
	private static final String		WITHOUT_WORDS = 
										"<html><b>Without</b> the words</html>";
	
	/** The preferred size of the calendar popup. */
	private static final Dimension	CALENDAR_SIZE = new Dimension(250, 200);
	
	/** The selected date format. */
	private static final String		DATE_FORMAT = "YYYY/MM/DD";//"MM/dd/yy";
	
	/** The tooltip of the calendar button. */
	private static final String		DATE_TOOLTIP = "Bring up a calendar.";
	
	/** Possible time options. */
	private static String[]		dateOptions;
	
	/** The maximum number of results returned. */
	private static String[]		numberOfResults;
	
	static {
		dateOptions = new String[SearchContext.MAX+1];
		dateOptions[SearchContext.ANY_DATE] = "Any date";
		dateOptions[SearchContext.LAST_TWO_WEEKS] = "Last two weeks";
		dateOptions[SearchContext.LAST_MONTH] = "Last 30 days";
		dateOptions[SearchContext.LAST_TWO_MONTHS] = "Last 60 days";
		dateOptions[SearchContext.ONE_YEAR] = "1 year";
		dateOptions[SearchContext.RANGE] = "Specify date range " +
											"("+DATE_FORMAT+")";
		numberOfResults = new String[SearchContext.MAX_RESULTS+1];
		numberOfResults[SearchContext.LEVEL_ONE] = 
									SearchContext.LEVEL_ONE_VALUE+" results";
		numberOfResults[SearchContext.LEVEL_TWO] = 
			SearchContext.LEVEL_TWO_VALUE+" results";
		numberOfResults[SearchContext.LEVEL_THREE] = 
			SearchContext.LEVEL_THREE_VALUE+" results";
		numberOfResults[SearchContext.LEVEL_FOUR] = 
			SearchContext.LEVEL_FOUR_VALUE+" results";
	}

	/** If checked the case is taken into account. */ 
	private JCheckBox				caseSensitive;
	
	/** The button used to display the available users. */
	private JButton					userButton;
	
	/** Fields with the possible users. */
	private JTextField 				authors;
	
	/** Possible dates. */
	private JComboBox				dates;
	
	/** Possible results. */
	private JComboBox				results;
	
	/** The terms to search for. */
	private JTextField				allTermsArea;
	
	/** The terms to search for. */
	private JTextField				exactPhraseArea;
	
	/** The terms to search for. */
	private JTextField				atLeastTermsArea;
	
	/** The terms to search for. */
	private JTextField				withoutTermsArea;
	
	/** Date used to specify the beginning of the time interval. */
	private JDateChooser			fromDate;
	
	/** Date used to specify the ending of the time interval. */
	private JDateChooser			toDate;
	
	/** Reference to the model .*/
	private SearchComponent 		model;
	
	/** Items used to defined the scope of the search. */
	private Map<Integer, JCheckBox>	scopes;
	
	/** Items used to defined the scope of the search. */
	private Map<Integer, JCheckBox>	types;
	
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
	
	/** The possible textual areas. */
	private Map<JTextField, JLabel>	areas;
	
	/** Button to bring up the tooltips for help. */
	private JButton					helpButton;
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		scopes = new HashMap<Integer, JCheckBox>(model.getNodes().size());
		types = new HashMap<Integer, JCheckBox>(model.getTypes().size());
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
		allTermsArea = new JTextField(20);
		exactPhraseArea = new JTextField(20);
		atLeastTermsArea = new JTextField(20);
		withoutTermsArea = new JTextField(20);
	
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
		areas = new LinkedHashMap<JTextField, JLabel>();
		areas.put(atLeastTermsArea, new JLabel(AT_LEAST_WORDS));
		areas.put(allTermsArea, new JLabel(ALL_WORDS));
		areas.put(exactPhraseArea, new JLabel(EXACT_WORDS));
		areas.put(withoutTermsArea, new JLabel(WITHOUT_WORDS));
		results = new JComboBox(numberOfResults);
		helpButton = new JButton(icons.getIcon(IconManager.HELP));
		helpButton.setToolTipText("Advanced search Tips.s");
		UIUtilities.unifiedButtonLookAndFeel(helpButton);
		helpButton.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				showHelp();
			}
		
		});
	}
	
	/** Brings up the Help dialog. */
	private void showHelp()
	{
		SearchHelp helpDialog = new SearchHelp((JFrame) model.getOwner());
		UIUtilities.centerAndShow(helpDialog);
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
		p.setLayout(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 3, 3, 3);
        List<SearchObject> nodes = model.getNodes();
		SearchObject n;
		int m = nodes.size();
		JCheckBox box;
		c.weightx = 1.0;
		for (int i = 0; i < m; i++) {
			n = nodes.get(i);
			box = new JCheckBox(n.getDescription());
			if (i == 0) box.setSelected(true);
			if (i%2 == 0) c.gridy++;
			
			p.add(box, c);
			scopes.put(n.getIndex(), box);
		}
		TitledBorder border = new TitledBorder("Scope");
		border.setTitleFont(p.getFont().deriveFont(Font.BOLD));
		p.setBorder(border);
		return p;
	}
	
	/** 
	 * Builds and lays out the component displaying the various types.
	 * 
	 * @return See above.
	 */
	private JPanel buildTypePanel()
	{
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 3, 3, 3);
        List<SearchObject> nodes = model.getTypes();
		SearchObject n;
		int m = nodes.size();
		JCheckBox box;
		c.weightx = 1.0;
		for (int i = 0; i < m; i++) {
			n = nodes.get(i);
			box = new JCheckBox(n.getDescription());
			if (i == 0) box.setSelected(true);
			
			p.add(box, c);
			types.put(n.getIndex(), box);
		}
		TitledBorder border = new TitledBorder("Type");
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
		//initBorder("Search For", searchFor);
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		//p.add(UIUtilities.setTextFont(WILD_CARD, Font.ITALIC, 10));
		p.add(results);
		p.add(helpButton);
		searchFor.add(UIUtilities.buildComponentPanelRight(p));
		
		//searchFor.add(UIUtilities.buildComponentPanel(p));
		p = new JPanel();
		p.setLayout(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 3, 3, 3);
       
        Iterator i = areas.keySet().iterator();
        JLabel label;
        JTextField area;
        while (i.hasNext()) {
            ++c.gridy;
            c.gridx = 0;
            area = (JTextField) i.next();
            label = areas.get(area);
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.weightx = 0.0;  
            p.add(label, c);
            label.setLabelFor(area);
            c.gridx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            p.add(area, c);  
        }
        searchFor.add(UIUtilities.buildComponentPanel(p));
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
		searchPanel.add(buildTypePanel());
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
	 * Returns the scope of the search.
	 * 
	 * @return See above.
	 */
	List<Integer> getType()
	{
		List<Integer> list = new ArrayList<Integer>();
		Iterator i = types.keySet().iterator();
		JCheckBox box;
		Integer key;
		while (i.hasNext()) {
			key = (Integer) i.next();
			box = types.get(key);
			if (box.isSelected())
				list.add(key);
		}
		return list;
	}
	
	
	/**
	 * Returns the terms that may be in the document.
	 * 
	 * @return See above.
	 */
	String[] getSome()
	{
		String text = atLeastTermsArea.getText();
		List<String> l = 
			SearchUtil.splitTerms(text, SearchUtil.QUOTE_SEPARATOR);
		String[] terms = null;
		if (l.size() > 0) {
			//terms = (String[]) l.toArray(new String[] {});
		} else {
			l = SearchUtil.splitTerms(text, SearchUtil.SPACE_SEPARATOR);
			//if (l.size() > 0)
			//	terms = (String[]) l.toArray(new String[] {});
		}
		text = exactPhraseArea.getText();
		if (text != null) {
			l.addAll(SearchUtil.splitTerms(text, SearchUtil.QUOTE_SEPARATOR));
		}
		if (l.size() > 0)
			terms = (String[]) l.toArray(new String[] {});
		return terms;
	}
	
	/**
	 * Returns the terms that may be in the document.
	 * 
	 * @return See above.
	 */
	String[] getMust()
	{
		String text = atLeastTermsArea.getText();
		List l = SearchUtil.splitTerms(text, SearchUtil.QUOTE_SEPARATOR);
		String[] terms = null;
		if (l.size() > 0) {
			terms = (String[]) l.toArray(new String[] {});
			
		} else {
			l = SearchUtil.splitTerms(text, SearchUtil.SPACE_SEPARATOR);
			if (l.size() > 0)
				terms = (String[]) l.toArray(new String[] {});
		}
		return terms;
	}
	
	/**
	 * Returns the terms that may be in the document.
	 * 
	 * @return See above.
	 */
	String[] getNone()
	{
		String text = withoutTermsArea.getText();
		List l = SearchUtil.splitTerms(text, SearchUtil.QUOTE_SEPARATOR);
		String[] terms = null;
		if (l.size() > 0) {
			terms = (String[]) l.toArray(new String[] {});
			
		} else {
			l = SearchUtil.splitTerms(text, SearchUtil.SPACE_SEPARATOR);
			if (l.size() > 0)
				terms = (String[]) l.toArray(new String[] {});
		}
		return terms;
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
									SearchUtil.COMMA_SEPARATOR);
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
		if (areas != null) {
			Iterator i = areas.keySet().iterator();
			while (i.hasNext()) {
				((JTextField) i.next()).requestFocus();
				break;
			}
		}
	}
	
	/**
	 * Sets the name of the selected user.
	 * 
	 * @param name The string to set.
	 */
	void setUserString(String name)
	{
		String text = authors.getText();
		String[] values = text.split(SearchUtil.COMMA_SEPARATOR);
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
						v += SearchUtil.COMMA_SEPARATOR
								+SearchUtil.SPACE_SEPARATOR;
					exist = true;
				} else v += SearchUtil.COMMA_SEPARATOR
							+SearchUtil.SPACE_SEPARATOR;
			}
		}
		if (!exist) v += name;
		else v = v.substring(0, text.length()-1);
		authors.setText(v);
	}
	
}
