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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries
import org.jdesktop.swingx.JXDatePicker;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.TreeComponent;
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
	implements PropertyChangeListener
{

	/** The title of the search UI component. */
	private static final String		SEARCH_TITLE = "Search For";
	
	/** The title of the user UI component. */
	private static final String		USER_TITLE = "Users";
	
	/** The title of the date UI component. */
	private static final String		DATE_TITLE = "Date";
	
	/** The title of the scope UI component. */
	private static final String		SCOPE_TITLE = "Scope";
	
	/** The title of the type UI component. */
	private static final String		TYPE_TITLE = "Type";
	
	/** Wild card used. */
	private static final String		WILD_CARD = 
									"(* = any string, ? = any character)";
	
	/** Search Tip. */
	private static final String		SEARCH_TIP = "Tip: Use these options to " +
			"look for an exact phrase or to exclude certain words.";
	
	/** Users Tip. */
	private static final String		USERS_TIP = "Tip: Add a minus in front " +
			"of a name to exclude the user.";
	
	/** The type of font used for the tips. */
	private static final int		TIP_FONT_TYPE = Font.ITALIC;
	
	/** The size of font used for the tips. */
	private static final int		TIP_FONT_SIZE = 10;
	
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
	
	/** The tooltip of the calendar button. */
	private static final String		DATE_TOOLTIP = "Bring up a calendar.";
	
	/** 
	 * Bound property indicating that the date has changed.
	 * JCalendar should have a static field.
	 */
	private static final String		DATE_CALENDAR_PROPERTY = "date";
	
	/** Possible time options. */
	private static String[]		dateOptions;
	
	/** The maximum number of results returned. */
	private static String[]		numberOfResults;
	
	/** The maximum number of results returned. */
	private static String[]		fileFormats;
	
	static {
		dateOptions = new String[SearchContext.MAX+1];
		dateOptions[SearchContext.ANY_DATE] = "Any date";
		dateOptions[SearchContext.LAST_TWO_WEEKS] = "Last two weeks";
		dateOptions[SearchContext.LAST_MONTH] = "Last 30 days";
		dateOptions[SearchContext.LAST_TWO_MONTHS] = "Last 60 days";
		dateOptions[SearchContext.ONE_YEAR] = "1 year";
		dateOptions[SearchContext.RANGE] = "Specify date range " +
								"("+UIUtilities.DATE_FORMAT.toUpperCase()+")";
		numberOfResults = new String[SearchContext.MAX_RESULTS+1];
		numberOfResults[SearchContext.LEVEL_ONE] = 
									SearchContext.LEVEL_ONE_VALUE+" results";
		numberOfResults[SearchContext.LEVEL_TWO] = 
			SearchContext.LEVEL_TWO_VALUE+" results";
		numberOfResults[SearchContext.LEVEL_THREE] = 
			SearchContext.LEVEL_THREE_VALUE+" results";
		numberOfResults[SearchContext.LEVEL_FOUR] = 
			SearchContext.LEVEL_FOUR_VALUE+" results";
		
		fileFormats = new String[SearchContext.MAX_FORMAT+1];
		fileFormats[SearchContext.ALL_FORMATS] = "All formats";
		fileFormats[SearchContext.HTML] = "HTML (.htm, .html)";
		fileFormats[SearchContext.PDF] = "Adobe PDF (.pdf)";
		fileFormats[SearchContext.EXCEL] = "Microsoft Excel (.xls)";
		fileFormats[SearchContext.POWER_POINT] = "Microsoft PowerPoint (.ppt)";
		fileFormats[SearchContext.WORD] = "Microsoft Word (.doc)";
		fileFormats[SearchContext.XML] = "RSS/XML (.xml)";
		fileFormats[SearchContext.TEXT] = "Text Format (.txt)";
	}

	/** If checked the case is taken into account. */ 
	private JCheckBox				caseSensitive;
	
	/** The button used to display the available owners. */
	private JButton					ownerButton;
	
	/** The button used to display the available annotators. */
	private JButton					annotatorButton;
	
	/** Fields with the possible users. */
	private JTextField 				usersAsOwner;
	
	/** Fields with the possible users. */
	private JTextField 				usersAsAnnotator;
	
	/** Possible dates. */
	private JComboBox				dates;
	
	/** Possible results. */
	//private JComboBox				results;
	
	/** The terms to search for. */
	private JTextField				allTermsArea;
	
	/** The terms to search for. */
	private JTextField				exactPhraseArea;
	
	/** The terms to search for. */
	private JTextField				atLeastTermsArea;
	
	/** The terms to search for. */
	private JTextField				withoutTermsArea;
	
	/** Date used to specify the beginning of the time interval. */
	//private JDateChooser			fromDate;
	private JXDatePicker			fromDate;
	
	/** Date used to specify the ending of the time interval. */
	//private JDateChooser			toDate;
	private JXDatePicker			toDate;
	
	/** Reference to the model .*/
	private SearchComponent 		model;
	
	/** Items used to defined the scope of the search. */
	private Map<Integer, JCheckBox>	scopes;
	
	/** Items used to defined the scope of the search. */
	private Map<Integer, JCheckBox>	types;
	
	/** Button to only retrieve the current user's data. */
	private JCheckBox				currentUserAsOwner;
	
	/** Button to only retrieve the current user and selected users' data. */
	private JCheckBox				othersAsOwner;

	/** Button to only retrieve the current user's data. */
	private JCheckBox				currentUserAsAnnotator;
	
	/** Button to only retrieve the current user and selected users' data. */
	private JCheckBox				othersAsAnnotator;
	
	/** The possible textual areas. */
	private Map<JTextField, JLabel>	areas;
	
	/** Button to bring up the tooltips for help. */
	private JButton					helpButton;
	
	/** Button indicating that the time entered is the creation time. */
	private JRadioButton			creationTime;
	
	/** Button indicating that the time entered is the time of update. */
	private JRadioButton			updatedTime;
	
	/** The possible file formats. */
	private JComboBox				formats;
	
	/**
	 * Creates a date picker.
	 * 
	 * @return See above.
	 */
	private JXDatePicker createDatePicker()
	{
		String[] dateFormats = new String[1];
		dateFormats[0] = UIUtilities.DATE_FORMAT;
		JXDatePicker picker = new JXDatePicker();
		
		picker.setToolTipText(DATE_TOOLTIP);
		//picker.getEditor().setEditable(false);
		//picker.setEditable(false);
		picker.setFormats(dateFormats);
		return picker;
	}
	
	/** Initializes the components composing the display.  */
	private void initComponents()
	{
		scopes = new HashMap<Integer, JCheckBox>(model.getNodes().size());
		types = new HashMap<Integer, JCheckBox>(model.getTypes().size());
		IconManager icons = IconManager.getInstance();
 		fromDate = createDatePicker();
		toDate = createDatePicker();

		allTermsArea = new JTextField(20);
		exactPhraseArea = new JTextField(20);
		atLeastTermsArea = new JTextField(20);
		withoutTermsArea = new JTextField(20);
	
		usersAsOwner = new JTextField(20);
		usersAsAnnotator = new JTextField(20);
		usersAsAnnotator.setEditable(false);
		usersAsOwner.setEditable(false);
		
		dates = new JComboBox(dateOptions);
		dates.addActionListener(model);
		dates.setActionCommand(""+SearchComponent.DATE);
		ownerButton = new JButton(icons.getIcon(IconManager.OWNER));
		ownerButton.setToolTipText("Select the owner of the objects.");
		UIUtilities.unifiedButtonLookAndFeel(ownerButton);
		ownerButton.addActionListener(model);
		ownerButton.setActionCommand(""+SearchComponent.OWNER);
		ownerButton.setEnabled(false);
		usersAsOwner.setEnabled(false);
		annotatorButton = new JButton(icons.getIcon(IconManager.OWNER));
		annotatorButton.setToolTipText("Select the users who annotated the " +
										"objects.");
		annotatorButton.setEnabled(false);
		usersAsAnnotator.setEnabled(false);
		UIUtilities.unifiedButtonLookAndFeel(annotatorButton);
		annotatorButton.addActionListener(model);
		annotatorButton.setActionCommand(""+SearchComponent.ANNOTATOR);
		
		currentUserAsOwner = new JCheckBox("Me");
		currentUserAsOwner.setSelected(true);
		currentUserAsAnnotator = new JCheckBox("Me");
		currentUserAsAnnotator.setSelected(true);
		othersAsOwner = new JCheckBox("Others");
		othersAsAnnotator = new JCheckBox("Others");
		othersAsOwner.addChangeListener(new ChangeListener() {
		
			public void stateChanged(ChangeEvent e) {
				ownerButton.setEnabled(othersAsOwner.isSelected());
				usersAsOwner.setEnabled(othersAsOwner.isSelected());
			}
		});
		
		othersAsAnnotator.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent e) {
				annotatorButton.setEnabled(othersAsAnnotator.isSelected());
				usersAsAnnotator.setEnabled(othersAsAnnotator.isSelected());
			}
		});
		
		caseSensitive = new JCheckBox("Case sensitive");
		areas = new LinkedHashMap<JTextField, JLabel>();
		areas.put(atLeastTermsArea, new JLabel(AT_LEAST_WORDS));
		areas.put(allTermsArea, new JLabel(ALL_WORDS));
		areas.put(exactPhraseArea, new JLabel(EXACT_WORDS));
		areas.put(withoutTermsArea, new JLabel(WITHOUT_WORDS));
		//results = new JComboBox(numberOfResults);
		helpButton = new JButton(icons.getIcon(IconManager.HELP));
		helpButton.setToolTipText("Advanced search Tips.");
		UIUtilities.unifiedButtonLookAndFeel(helpButton);
		helpButton.addActionListener(model);
		helpButton.setActionCommand(""+SearchComponent.HELP);
		formats = new JComboBox(fileFormats);
		formats.setEnabled(false);
		ButtonGroup group = new ButtonGroup();
		creationTime = new JRadioButton("Created");
		creationTime.setSelected(true);
		updatedTime = new JRadioButton("Updated");
		group.add(creationTime);
		group.add(updatedTime);
		
		SearchContext ctx = model.getSearchContext();
		if (ctx == null) return;
		if (ctx.getTimeType() == SearchContext.UPDATED_TIME)
			updatedTime.setSelected(true);
		List<Integer> l = ctx.getOwnerSearchContext();
		if (l != null) {
			othersAsOwner.setSelected(l.contains(SearchContext.OTHERS));
			currentUserAsOwner.setSelected(
					l.contains(SearchContext.CURRENT_USER));
		}
		
		l = ctx.getAnnotatorSearchContext();
		if (l != null) {
			othersAsAnnotator.setSelected(l.contains(SearchContext.OTHERS));
			currentUserAsAnnotator.setSelected(
					l.contains(SearchContext.CURRENT_USER));
		}
		int dateIndex = ctx.getDateIndex();
		if (dateIndex != -1) dates.setSelectedIndex(dateIndex);
		String[] none = ctx.getNone();
		String v = "";
		for (int i = 0; i < none.length; i++) {
			v += SearchUtil.QUOTE_SEPARATOR+none[i]+SearchUtil.QUOTE_SEPARATOR;
			if (i != (none.length-1))
				v += SearchUtil.SPACE_SEPARATOR;
		}
		withoutTermsArea.setText(v);
		String[] must = ctx.getMust();
		v = "";
		for (int i = 0; i < must.length; i++) {
			v += SearchUtil.QUOTE_SEPARATOR+must[i]+SearchUtil.QUOTE_SEPARATOR;
			if (i != (none.length-1))
				v += SearchUtil.SPACE_SEPARATOR;
		}
		allTermsArea.setText(v);
		String[] some = ctx.getSome();
		v = "";
		for (int i = 0; i < some.length; i++) {
			v += SearchUtil.QUOTE_SEPARATOR+some[i]+SearchUtil.QUOTE_SEPARATOR;
			if (i != (none.length-1))
				v += SearchUtil.SPACE_SEPARATOR;
		}
		atLeastTermsArea.setText(v);
		
		//initialize
		setDateIndex();
	}
	
	/**
	 * Sets the name of the passed user in the specified <code>TextField</code>.
	 * 
	 * @param name	The value to set.
	 * @param field	The UI component to handle.
	 */
	private void setUserString (String name, JTextField field)
	{
		/*
		String text = field.getText();
		String[] values = text.split(SearchUtil.COMMA_SEPARATOR);
		String n;
		String v = "";
		boolean exist = false;
		int l = values.length;
		for (int i = 0; i < l; i++) {
			n = values[i].trim();
			if (n.length() > 0) {
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
		field.setText(v);
		*/
		List<String> values = SearchUtil.splitTerms(field.getText(), 
											SearchUtil.QUOTE_SEPARATOR);
		String v = "";
		int n = values.size();
		if (n == 0)
			v = SearchUtil.QUOTE_SEPARATOR+name+SearchUtil.QUOTE_SEPARATOR;
		else {
			Iterator i = values.iterator();
			String value;
			boolean exist = false;
			int index = 0;
			while (i.hasNext()) {
				value = (String) i.next();
				if (!value.equals(name)) {
					v += SearchUtil.QUOTE_SEPARATOR+value
					+SearchUtil.QUOTE_SEPARATOR;
					if (index < n)
						v += SearchUtil.SPACE_SEPARATOR;
				}
				index++;
			}
			if (!exist) 
				v += SearchUtil.QUOTE_SEPARATOR+name+SearchUtil.QUOTE_SEPARATOR;
		}
		field.setText(v);
	}
	
	/**
	 * Retrieves the users to exclude.
	 * 
	 * @param field The text field to handle.
	 * @return See above.
	 */
	private List<String> getExcludedUsers(JTextField field)
	{
		String text = field.getText();
		if (text == null || text.length() == 0) return null;
		if (!text.contains(SearchUtil.MINUS_SEPARATOR)) return null;
		List<String> l = SearchUtil.splitTerms(text, 
											SearchUtil.QUOTE_SEPARATOR);
		int index = 0;
		Iterator i;
		List<String> excluded = new ArrayList<String>();
		if (l != null) {
			i = l.iterator();
			String value;
			String nextItem;
			while (i.hasNext()) {
				value = (String) i.next();
				if (SearchUtil.MINUS_SEPARATOR.equals(value)) {
					nextItem = l.get(index+1);
					if (nextItem != null)
						excluded.add(nextItem);
				}
				index++;
			}
		}
		return excluded;
	}
	
	/**
	 * Retrieves the users to exclude.
	 * 
	 * @param field The text field to handle.
	 * @return See above.
	 */
	private List<String> getUsers(JTextField field)
	{
		String text = field.getText();
		if (text == null || text.length() == 0) return null;
		List<String> l = SearchUtil.splitTerms(text, 
										SearchUtil.QUOTE_SEPARATOR);
		Iterator i;
		List<String> terms = new ArrayList<String>();
		if (l != null) {
			i = l.iterator();
			String value;
			while (i.hasNext()) {
				value = (String) i.next();
				if (!SearchUtil.MINUS_SEPARATOR.equals(value))
					terms.add(value);
			}
		}
		return terms;
	}
	
	/** 
	 * Builds the panel hosting the time fields.
	 * 
	 * @return See above;
	 */
	private JPanel buildTimeRange()
	{
		JPanel p = new JPanel();
		p.add(UIUtilities.setTextFont("From: "));
		p.add(fromDate);
		p.add(UIUtilities.setTextFont("To: "));
		p.add(toDate);
		return p;
	}
	
	/** 
	 * Builds the panel hosting the user selection.
	 * 
	 * @param user The user checkbox to lay out.
	 * @param others The others checkbox to lay out.
	 * @return See above.
	 */
	public JPanel buildUserSelection(JCheckBox user, JCheckBox others)
	{
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        p.add(user, c);
		p.add(others, c);
		return UIUtilities.buildComponentPanel(p);
	}
	
	/** 
	 * Builds and lays out the component displaying the various options.
	 * 
	 * @return See above.
	 */
	private JPanel buildScope()
	{
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
        //p.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        //c.insets = new Insets(3, 3, 3, 3);
        List<SearchObject> nodes = model.getNodes();
		SearchObject n;
		int m = nodes.size();
		JCheckBox box;
		c.weightx = 1.0;
		List<Integer> ctxNodes = null;
		SearchContext ctx = model.getSearchContext();
		if (ctx != null) ctxNodes = ctx.getContext();
		if (ctxNodes == null) {
			for (int i = 0; i < m; i++) {
				n = nodes.get(i);
				box = new JCheckBox(n.getDescription());
				if (i == 0) box.setSelected(true);
				if (i%2 == 0) c.gridy++;
				
				p.add(box, c);
				scopes.put(n.getIndex(), box);
			}
		} else {
			for (int i = 0; i < m; i++) {
				n = nodes.get(i);
				box = new JCheckBox(n.getDescription());
				box.setSelected(ctxNodes.contains(n.getIndex()));
				if (i%2 == 0) c.gridy++;
				
				p.add(box, c);
				scopes.put(n.getIndex(), box);
			}
		}
		
		UIUtilities.setBoldTitledBorder(SCOPE_TITLE, p);
		return p;
	}
	
	/** 
	 * Builds and lays out the component displaying the various types.
	 * 
	 * @return See above.
	 */
	private JPanel buildType()
	{
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
        //p.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 3, 3, 3);
        List<SearchObject> nodes = model.getTypes();
        List<Integer> ctxNodes = null;
		SearchContext ctx = model.getSearchContext();
		if (ctx != null) ctxNodes = ctx.getType();
        
		SearchObject n;
		int m = nodes.size();
		JCheckBox box;
		c.weightx = 1.0;
		
		
		if (ctxNodes == null) {
			for (int i = 0; i < m; i++) {
				n = nodes.get(i);
				box = new JCheckBox(n.getDescription());
				if (i == 0) box.setSelected(true);
				
				p.add(box, c);
				types.put(n.getIndex(), box);
			}
		} else {
			for (int i = 0; i < m; i++) {
				n = nodes.get(i);
				box = new JCheckBox(n.getDescription());
				box.setSelected(ctxNodes.contains(n.getIndex()));
				
				p.add(box, c);
				types.put(n.getIndex(), box);
			}
		}
		
		UIUtilities.setBoldTitledBorder(TYPE_TITLE, p);
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
		UIUtilities.setBoldTitledBorder(SEARCH_TITLE, searchFor);
		JPanel p = new JPanel();
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
        ++c.gridy;
        c.gridx = 0;
        c.weightx = 0.0; 
        p.add(UIUtilities.setTextFont(SEARCH_TIP, TIP_FONT_TYPE, 
        							TIP_FONT_SIZE), c); 
        searchFor.add(UIUtilities.buildComponentPanel(p));
		return searchFor;
	}
	
	/**
	 * Lays out the specified text field and button.
	 * 
	 * @param box		The box to lay out.
	 * @param field		The field to lay out.
	 * @param button	The button to lay out.
	 * @return See above.
	 */
	private JPanel buildUserSelection(JCheckBox box, JTextField field, 
										JButton button)
	{
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridy = 1;
        p.add(box, c);
        p.add(field, c);
		p.add(button, c);
		return p;
	}
	
	/**
	 * Builds the UI component hosting the users to search for.
	 * 
	 * @return See above.
	 */
	private JPanel buildUsers()
	{	
		JPanel usersPanel= new JPanel();
		UIUtilities.setBoldTitledBorder(USER_TITLE, usersPanel);
		usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.Y_AXIS));
		JPanel content = new JPanel();
		content.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridy = 0;
        content.add(UIUtilities.setTextFont("Owned by "), c);
        c.gridx = 1;
        content.add(currentUserAsOwner, c);
		c.gridy++;
		c.ipady = 10;
		content.add(buildUserSelection(othersAsOwner, usersAsOwner, 
										ownerButton), c);
		
		c.gridx = 0;
		c.gridy++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		c.ipady = 5;
		content.add(new JSeparator(), c);
		c.ipady = 0;
		c.gridwidth = 1;
		c.gridy++;
		content.add(UIUtilities.setTextFont("Annotated by "), c);
        c.gridx = 1;
        content.add(currentUserAsAnnotator, c);
		c.gridy++;
		content.add(buildUserSelection(othersAsAnnotator, usersAsAnnotator, 
				annotatorButton), c);

		usersPanel.add(UIUtilities.buildComponentPanel(content));
		return usersPanel;
	}
	
	/**
	 * Builds the UI component hosting the time interval.
	 * 
	 * @return See above.
	 */
	private JPanel buildDate()
	{
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
		content.add(creationTime);
		content.add(updatedTime);
		
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridy = 0;
        c.ipady = 10;
        p.add(content, c);
        c.ipady = 0;
        c.gridy++;
        p.add(dates, c);
        c.gridy++;
		p.add(buildTimeRange(), c);
		JPanel panel = UIUtilities.buildComponentPanel(p);
		UIUtilities.setBoldTitledBorder(DATE_TITLE, panel);
		return panel;
	}

	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		TreeComponent tree = new TreeComponent();
		tree.insertNode(buildSearchFor(), 
							UIUtilities.buildCollapsePanel(SEARCH_TITLE));
		tree.insertNode(buildType(), 
						UIUtilities.buildCollapsePanel(TYPE_TITLE));
		tree.insertNode(buildScope(), 
				UIUtilities.buildCollapsePanel(SCOPE_TITLE));
		tree.insertNode(buildUsers(), 
							UIUtilities.buildCollapsePanel(USER_TITLE));
		tree.insertNode(buildDate(), 
									UIUtilities.buildCollapsePanel(DATE_TITLE));
		tree.addPropertyChangeListener(this);
		content.add(tree);
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(content);
		setDateIndex();
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
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
		if (fromDate.getEditor() != null);
			fromDate.getEditor().setEnabled(false);
		toDate.setEnabled(index == SearchContext.RANGE);
		if (toDate.getEditor() != null);
			toDate.getEditor().setEnabled(false);
	}
	
	/**
	 * Returns the currently selected time index.
	 * 
	 * @return See above.
	 */
	int getSelectedDate() { return dates.getSelectedIndex(); }
	
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
	List<Integer> getOwnerSearchContext()
	{
		List<Integer> context = new ArrayList<Integer>();
		if (currentUserAsOwner.isSelected())
			context.add(SearchContext.CURRENT_USER);
		if (othersAsOwner.isSelected())
			context.add(SearchContext.OTHERS);
		return context;
	}
	
	/**
	 * Returns the context of the search for users.
	 * 
	 * @return See above.
	 */
	List<Integer> getAnnotatorSearchContext()
	{
		List<Integer> context = new ArrayList<Integer>();
		if (currentUserAsAnnotator.isSelected())
			context.add(SearchContext.CURRENT_USER);
		if (othersAsAnnotator.isSelected())
			context.add(SearchContext.OTHERS);
		return context;
	}
	
	/**
	 * Returns the collection of the users who own the objects.
	 * 
	 * @return See above.
	 */
	List<String> getOwners() { return getUsers(usersAsOwner); }
	
	/**
	 * Returns the collection of the users who annotated the objects.
	 * 
	 * @return See above.
	 */
	List<String> getAnnotators() { return getUsers(usersAsAnnotator); }
	
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
	void setOwnerString(String name)
	{
		setUserString(name, usersAsOwner);
	}
	
	/**
	 * Sets the name of the selected user.
	 * 
	 * @param name The string to set.
	 */
	void setAnnotatorString(String name)
	{
		setUserString(name, usersAsAnnotator);
	}
	
	/**
	 * Returns the type of files to search for.
	 * 
	 * @return See above.
	 */
	int getAttachment() { return formats.getSelectedIndex(); }
	
	/**
	 * Returns the index of the time.
	 * 
	 * @return See above.
	 */
	int getTimeIndex()
	{
		if (creationTime.isSelected()) return SearchContext.CREATION_TIME;
		if (updatedTime.isSelected()) return SearchContext.UPDATED_TIME;
		return -1;
	}

	/**
	 * Returns the collection of excluded users.
	 * 
	 * @return See above.
	 */
	List<String> getExcludedAnnotators()
	{
		return getExcludedUsers(usersAsAnnotator);
	}
	
	/**
	 * Returns the collection of excluded users.
	 * 
	 * @return See above.
	 */
	List<String> getExcludedOwners()
	{
		return getExcludedUsers(usersAsOwner);
	}

	/**
	 * Reacts to property fired by a {@link TreeComponentNode}.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (TreeComponent.EXPANDED_PROPERTY.equals(name)) {
			model.pack();
		} else if (DATE_CALENDAR_PROPERTY.equals(name)) {
			//if (evt.getNewValue() != null) 
				//toDate.setDate(new Date());
		}
	}
	
}
