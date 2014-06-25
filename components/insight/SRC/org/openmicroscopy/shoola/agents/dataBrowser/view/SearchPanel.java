/*
 * org.openmicroscopy.shoola.util.ui.search.SearchPanel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;


//Java imports
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;



//Third-party libraries
import org.jdesktop.swingx.JXDatePicker;
import org.openmicroscopy.shoola.agents.util.finder.FinderFactory;
import org.openmicroscopy.shoola.env.LookupNames;
//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.SeparatorPane;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.search.ExperimenterContext;
import org.openmicroscopy.shoola.util.ui.search.GroupContext;
import org.openmicroscopy.shoola.util.ui.search.SearchContext;
import org.openmicroscopy.shoola.util.ui.search.SearchObject;
import org.openmicroscopy.shoola.util.ui.search.SearchUtil;

import pojos.ExperimenterData;

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
public class SearchPanel
	extends JPanel
{	
	
	/** The title of the type UI component. */
	private static final String		TYPE_TITLE = "Type";
	
	/** The number of columns of the search areas. */
	private static final int		AREA_COLUMNS = 12;
	
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

	/** Possible dates. */
	private JComboBox				dates;
	
	/** The terms to search for. */
	private JTextField				fullTextArea;
	
	/** Date used to specify the beginning of the time interval. */
	private JXDatePicker			fromDate;
	
	/** Date used to specify the ending of the time interval. */
	private JXDatePicker			toDate;
	
	private JButton clearDate;
	
	/** Reference to the model .*/
	private SearchComponent 		model;
	
	/** Items used to defined the scope of the search (Name, Description, ...). */
	private Map<Integer, JCheckBox>	scopes;
	
	/** Items used to defined the scope of the search. (Images, Projects, Datasets, ...). */
	private Map<Integer, JCheckBox>	types;
	
	/** Button to bring up the tooltips for help. */
	private JButton					helpBasicButton;
	
	/** The component used to perform a basic search. */
	private JPanel					basicSearchComp;
	
	/** The component hosting either the advanced or basic search component. */
	private JPanel 					searchFor;
	
	/** The collection of buttons to add. */
	private List<JButton>			controls;
	
	/** The box displaying the groups.*/
	private JComboBox groupsBox;
	
	/** The box displaying the users.*/
        private JComboBox usersBox;
	
	/**
	 * Creates a <code>JComboBox</code> with the available groups.
	 * 
	 * @return See above.
	 */
	private JComboBox createGroupBox()
	{
		List<GroupContext> groups = model.getGroups();
		Object[] values = new Object[groups.size()+1];
		values[0] = new GroupContext("All groups", GroupContext.ALL_GROUPS_ID);
		int j = 1;
		Iterator<GroupContext> i = groups.iterator();
		while (i.hasNext()) {
			values[j] = i.next();
			j++;
		}
		return new JComboBox(values);
	}
	
	/** Initializes the components composing the display.  */
	private void initComponents()
	{
	        usersBox = new JComboBox();
		groupsBox = createGroupBox();
		scopes = new HashMap<Integer, JCheckBox>(model.getNodes().size());
		types = new HashMap<Integer, JCheckBox>(model.getTypes().size());
		IconManager icons = IconManager.getInstance();
 		fromDate = UIUtilities.createDatePicker(false);
 		fromDate.setBackground(UIUtilities.BACKGROUND_COLOR);
		toDate = UIUtilities.createDatePicker(false);
		toDate.setBackground(UIUtilities.BACKGROUND_COLOR);
		
		clearDate = new JButton(icons.getIcon(IconManager.CLOSE));
		clearDate.setToolTipText("Reset the dates");
		UIUtilities.unifiedButtonLookAndFeel(clearDate);
		clearDate.setBackground(UIUtilities.BACKGROUND_COLOR);
		clearDate.setActionCommand(""+SearchComponent.RESET_DATE);
		clearDate.addActionListener(model);
		
		fullTextArea = new JTextField(AREA_COLUMNS);
		fullTextArea.addKeyListener(new KeyAdapter() {

            /** Finds the phrase. */
            public void keyPressed(KeyEvent e)
            {
            	Object source = e.getSource();
            	if (source != fullTextArea) return;
            	switch (e.getKeyCode()) {
					case KeyEvent.VK_ENTER:
						model.search();
				}
            }
        });
		
		dates = new JComboBox(dateOptions);
		dates.setBackground(UIUtilities.BACKGROUND_COLOR);
		dates.addActionListener(model);
		dates.setActionCommand(""+SearchComponent.DATE);
		
		helpBasicButton = new JButton(icons.getIcon(IconManager.HELP));
		helpBasicButton.setToolTipText("Search Tips.");
		helpBasicButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		UIUtilities.unifiedButtonLookAndFeel(helpBasicButton);
		helpBasicButton.addActionListener(model);
		helpBasicButton.setActionCommand(""+SearchComponent.HELP);

		
		SearchContext ctx = model.getSearchContext();
		if (ctx == null) return;

		int dateIndex = ctx.getDateIndex();
		if (dateIndex != -1) dates.setSelectedIndex(dateIndex);
	}
	
	/**
	 * Resets the date fields
	 */ 
	public void resetDate() {
	    toDate.setDate(null);
	    fromDate.setDate(null);
	}
	
	/** 
	 * Builds the panel hosting the time fields.
	 * 
	 * @return See above;
	 */
	private JPanel buildTimeRange()
	{
		JPanel p = new JPanel();
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		p.add(UIUtilities.setTextFont("From: "));
		p.add(fromDate);
		p.add(UIUtilities.setTextFont("To: "));
		p.add(toDate);
		p.add(clearDate);
		return p;
	}
	
	/** 
	 * Builds and lays out the component displaying the various options.
	 * 
	 * @return See above.
	 */
	private JPanel buildFields()
	{	
            List<SearchObject> nodes = model.getNodes();
            SearchObject n;
            int m = nodes.size();
            JCheckBox box;
            JPanel p = new JPanel();
            p.setBackground(UIUtilities.BACKGROUND_COLOR);
            GridBagConstraints c = new GridBagConstraints();
            c.weightx = 1.0;
            c.gridy = 1;
            List<Integer> ctxNodes = null;
            SearchContext ctx = model.getSearchContext();
            if (ctx != null)
                ctxNodes = ctx.getContext();
            if (ctxNodes == null) {
                for (int i = 0; i < m; i++) {
                    n = nodes.get(i);
                    box = new JCheckBox(n.getDescription());
                    box.setBackground(UIUtilities.BACKGROUND_COLOR);
    
                    if (i % 2 == 0) {
                        c.gridy++;
                    }
    
                    p.add(box, c);
                    scopes.put(n.getIndex(), box);
                }
            } else {
                for (int i = 0; i < m; i++) {
                    n = nodes.get(i);
                    box = new JCheckBox(n.getDescription());
                    box.setBackground(UIUtilities.BACKGROUND_COLOR);
                    box.setSelected(ctxNodes.contains(n.getIndex()));
                    
                    if (i % 2 == 0)
                        c.gridy++;
    
                    p.add(box, c);
                    scopes.put(n.getIndex(), box);
                }
            }
            c.gridy++;
            UIUtilities.setBoldTitledBorder("Restrict by Field", p);
            return p;
	}

	/** 
         * Builds and lays out the component displaying the various options.
         * 
         * @return See above.
         */
        private JPanel buildScope()
        {
            JPanel p = new JPanel();
            p.setBackground(UIUtilities.BACKGROUND_COLOR);
            p.setLayout(new GridBagLayout());
            
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            
            p.add(new JLabel("Groups:"), c);
            c.gridx = 1;
            p.add(groupsBox, c);
            
            groupsBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    updateUsersBox();
                }
            });
            
            c.gridx = 0;
            c.gridy++;
            
            p.add(new JLabel("Data owned by:"), c);
            c.gridx = 1;
            p.add(usersBox, c);
            
            UIUtilities.setBoldTitledBorder("Scope", p);
            
            return p;
        }
        
        /**
         * Updates the content of the usersBox depending on the current
         * groupsBox selection
         */
        private void updateUsersBox() {
            updateUsersBox(false);
        }
        
        /**
         * Updates the content of the usersBox depending on the current
         * groupsBox selection
         * @param reset If <code>true</code> reset the selection to 'all'
         */
        private void updateUsersBox(boolean reset) {
            ExperimenterContext me = new ExperimenterContext("Me", getUserDetails().getId());
            ExperimenterContext all = new ExperimenterContext("Anyone", ExperimenterContext.ALL_EXPERIMENTERS_ID);
            ExperimenterContext selected = (usersBox.getSelectedIndex() != -1 && !reset) ? (ExperimenterContext) usersBox.getSelectedItem() : null;
    
            usersBox.removeAllItems();
    
            // the users to present in the combobox
            List<ExperimenterContext> items = new ArrayList<ExperimenterContext>();
            
            // always add 'me' and 'all'
            items.add(me);
            items.add(all);
    
            // gather the users from the GroupContexts
            if (groupsBox.getSelectedIndex() > -1) {
                GroupContext groupContext = (GroupContext) groupsBox.getSelectedItem();
                
                List<GroupContext> groupContexts = new ArrayList<GroupContext>();
                if (groupContext.getId()==GroupContext.ALL_GROUPS_ID) {
                    // users from all GroupContexts must be added
                    for(int i=0; i<groupsBox.getItemCount(); i++) {
                        groupContext = (GroupContext) groupsBox.getItemAt(i);
                        if(groupContext.getId()==GroupContext.ALL_GROUPS_ID) 
                            continue;
                        groupContexts.add(groupContext);
                    }
                }
                else {
                    // just users from the selected GroupContext must be added
                    groupContexts.add(groupContext);
                }
                
                for (GroupContext gc : groupContexts) {
                    for (ExperimenterContext exp : gc.getExperimenters()) {
                        if (!items.contains(exp)) {
                            items.add(exp);
                        }
                    }
                }
            }
    
            for (ExperimenterContext item : items) {
                usersBox.addItem(item);
            }
    
            // restore the previous selection if there was any
            if (selected != null && items.contains(selected)) {
                usersBox.setSelectedItem(selected);
            } else {
                usersBox.setSelectedItem(me);
            }
        }
        
        
        
	/** 
	 * Builds and lays out the component displaying the various types.
	 * 
	 * @return See above.
	 */
	private JPanel buildType()
	{
            JPanel p = new JPanel();
            p.setBackground(UIUtilities.BACKGROUND_COLOR);
            p.setLayout(new GridBagLayout());
            // p.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(3, 3, 3, 3);
            List<SearchObject> nodes = model.getTypes();
            List<Integer> ctxNodes = null;
            SearchContext ctx = model.getSearchContext();
            if (ctx != null)
                ctxNodes = ctx.getType();
    
            SearchObject n;
            int m = nodes.size();
            JCheckBox box;
            c.weightx = 1.0;
    
            if (ctxNodes == null) {
                for (int i = 0; i < m; i++) {
                    n = nodes.get(i);
                    box = new JCheckBox(n.getDescription());
                    box.setBackground(UIUtilities.BACKGROUND_COLOR);
                    box.setSelected(true);
                    p.add(box, c);
                    if (i % 2 == 0)
                        c.gridy++;
                    types.put(n.getIndex(), box);
                }
            } else {
                for (int i = 0; i < m; i++) {
                    n = nodes.get(i);
                    box = new JCheckBox(n.getDescription());
                    box.setBackground(UIUtilities.BACKGROUND_COLOR);
                    box.setSelected(ctxNodes.contains(n.getIndex()));
                    p.add(box, c);
                    if (i % 2 == 0)
                        c.gridy++;
                    types.put(n.getIndex(), box);
                }
            }
    
            UIUtilities.setBoldTitledBorder(TYPE_TITLE, p);
            return p;
	}

	/**
	 * Builds and lays out the Basic search component.
	 * 
	 * @return See above.
	 */
	private JPanel buildBasicSearchComp()
	{
		basicSearchComp = new JPanel();
		basicSearchComp.setBackground(UIUtilities.BACKGROUND_COLOR);
		UIUtilities.setBoldTitledBorder("Search", basicSearchComp);
		basicSearchComp.setLayout(new BoxLayout(basicSearchComp, 
				BoxLayout.Y_AXIS));
		basicSearchComp.add(fullTextArea);
		JToolBar bar = new JToolBar();
		bar.setBackground(UIUtilities.BACKGROUND_COLOR);
		bar.setFloatable(false);
		bar.setRollover(true);
		bar.setBorder(null);
		bar.add(helpBasicButton);
		if (controls != null && controls.size() > 0) {
			Iterator<JButton> i = controls.iterator();
			while (i.hasNext()) {
				bar.add(i.next());
			}
		}
		JPanel p = UIUtilities.buildComponentPanel(bar);
		p.setBackground(UIUtilities.BACKGROUND_COLOR);

		basicSearchComp.add(p);
		return basicSearchComp;
	}
	
	/**
	 * Builds the UI component hosting the terms to search for.
	 * 
	 * @return See above.
	 */
	private JPanel buildSearchFor()
	{
		if (searchFor == null) {
			searchFor = new JPanel();
		    searchFor.setBackground(UIUtilities.BACKGROUND_COLOR);
			searchFor.setLayout(new BoxLayout(searchFor, BoxLayout.Y_AXIS));
		}
		searchFor.removeAll();
		searchFor.add(buildBasicSearchComp());
		return searchFor;
	}
	
	/**
	 * Builds the UI component hosting the time interval.
	 * 
	 * @return See above.
	 */
	private JPanel buildDate()
	{
		
            JPanel p = new JPanel();
            p.setBackground(UIUtilities.BACKGROUND_COLOR);
            p.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
    
            p.add(buildTimeRange(), c);
    
            UIUtilities.setBoldTitledBorder("Date", p); 
            return p;
	}

	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setBorder(null);
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		setBackground(UIUtilities.BACKGROUND_COLOR);
		
		add(buildSearchFor(), c);//, "0, 0");
		
		SeparatorPane sep = new SeparatorPane();
		sep.setBackground(UIUtilities.BACKGROUND_COLOR);
		c.gridy++;
		add(sep, c);//, "0, 1");
		
		JPanel typePanel = buildType();
                c.gridy++;
                add(typePanel, c);//, "0, 2")
                
                JPanel fieldsPanel = buildFields();
		c.gridy++;
		add(fieldsPanel, c);//, "0, 2");
		
                JPanel scopePanel = buildScope();
                c.gridy++;
                add(scopePanel, c);//, "0, 2");
		
		JPanel datePanel = buildDate();
		c.gridy++;
		add(datePanel, c);//, "0, 4");

		updateUsersBox();
	}

	/** 
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 * @param controls The collection of buttons to add next to the help etc.
	 */
	SearchPanel(SearchComponent model, List<JButton> controls)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.model = model;
		this.controls = controls;
		initComponents();
		buildGUI();
	}

	/** Resets.*/
	public void reset()
	{
	        updateUsersBox(true);
	        
	        resetDate();
	        
	        setTerms(Collections.<String> emptyList());
	        
	        for(Entry<Integer, JCheckBox> scope : scopes.entrySet()) {
	            scope.getValue().setSelected(false);
	        }
	        
	        for(Entry<Integer, JCheckBox> type : types.entrySet()) {
                    type.getValue().setSelected(true);
                }
	        
		validate();
		repaint();
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
	 * Sets the values to add. 
	 * 
	 * @param values The values to add.
	 */
	void setTerms(List<String> terms)
	{
		if (terms == null || terms.size() == 0) return;
		StringBuffer text = new StringBuffer();
		Iterator<String> i = terms.iterator();
		while (i.hasNext()) {
			text.append(i.next());
			text.append(SearchUtil.SPACE_SEPARATOR);
		}
			
		fullTextArea.setText(text.toString());
	}
	
	/**
	 * Returns the terms that may be in the document.
	 * 
	 * @return See above.
	 */
	String[] getQueryTerms()
	{
		String text;
			text = fullTextArea.getText();
			if (text != null && text.trim().length() > 0) {
				List<String> l = SearchUtil.splitTerms(text);
				if (l.size() > 0) 
					return l.toArray(new String[] {});
			}
		
		List<String> l = SearchUtil.splitTerms(text);

		if (l.size() > 0)
			return l.toArray(new String[] {});
		return null;
	}
	
	/**
         * Returns the current user's details.
         * 
         * @return See above.
         */
        private ExperimenterData getUserDetails()
        { 
                return (ExperimenterData) FinderFactory.getRegistry().lookup(
                                LookupNames.CURRENT_USER_DETAILS);
        }
	
	
	long getGroupId() {
	    long result = GroupContext.ALL_GROUPS_ID;
	    if(groupsBox.getSelectedIndex()>=0) {
	        GroupContext g = (GroupContext) groupsBox.getSelectedItem();
	        result = g.getId();
	    }
	    return result;
	}
	
	long getUserId() {
            long result = -1;
            if(usersBox.getSelectedIndex()>=0) {
                ExperimenterContext u = (ExperimenterContext) usersBox.getSelectedItem();
                result = u.getId()==ExperimenterContext.ALL_EXPERIMENTERS_ID ? -1 : u.getId();
            }
            return result;
        }
}
