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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;



//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
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
	
	private static int 			ANY_DATE = 0;
	
	private static int 			LAST_TWO_WEEKS = 1; 
	
	private static int 			LAST_MONTH = 2; 
	
	private static int 			LAST_TWO_MONTHS = 3; 
	
	private static int 			ONE_YEAR = 4; 
	
	private static int 			TW0_YEARS = 5; 
	
	private static int 			RANGE = 6; 
	
	private static int 			MAX = 6;
	
	private static String[]	dateOptions;
	
	static {
		dateOptions = new String[MAX+1];
		dateOptions[ANY_DATE] = "Any date";
		dateOptions[LAST_TWO_WEEKS] = "Last two weeks";
		dateOptions[LAST_MONTH] = "Last 30 days";
		dateOptions[LAST_TWO_MONTHS] = "Last 60 days";
		dateOptions[ONE_YEAR] = "1 year";
		dateOptions[TW0_YEARS] = "2 years";
		dateOptions[RANGE] = "Specify date range (DD/MM/YYYY)";
	}

	/** Fields with the possible users. */
	private JTextField authors;
	
	/** Possible dates. */
	private JComboBox	dates;
	
	private TableLayout layout;

	private JPanel		searchPanel;
	
	/** The panel hosting the time fields. */
	private JPanel		rangePanel;
	
	/** The field hosting the possible tags. */
	private JTextField	tags;
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		authors = new JTextField(15);
		tags = new JTextField(15);
		dates = new JComboBox(dateOptions);
		dates.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				int index = dates.getSelectedIndex();
				if (index == RANGE) {
					if (rangePanel == null)
						rangePanel = buidlTimeRangePanel();
					layout.setRow(6, TableLayout.PREFERRED);
					searchPanel.add(rangePanel, "0, 6, 2, 6");
					
				} else {
					layout.setRow(6, 0);
					if (rangePanel != null)
						searchPanel.remove(rangePanel);
				}
				searchPanel.validate();
				searchPanel.repaint();
			}
		
		});
	}
	
	/** 
	 * Builds the panel hosting the time fields
	 * 
	 * @return See above;
	 */
	private JPanel buidlTimeRangePanel()
	{
		JPanel p = new JPanel();
		JTextField yearStart = new JTextField(4);
		JTextField monthStart = new JTextField(2);
		JTextField dayStart = new JTextField(2);
		JTextField yearEnd = new JTextField(4);
		JTextField monthEnd = new JTextField(2);
		JTextField dayEnd = new JTextField(2);
		p.add(UIUtilities.setTextFont("From"));
		JPanel from = new JPanel();
		from.add(dayStart);
		from.add(monthStart);
		from.add(yearStart);
		p.add(from);
		p.add(UIUtilities.setTextFont("To"));
		from = new JPanel();
		from.add(dayEnd);
		from.add(monthEnd);
		from.add(yearEnd);
		p.add(from);
		return  UIUtilities.buildComponentPanel(p);
	}
	
	/** 
	 * Builds the panel hosting the authors.
	 * 
	 * @return See above.
	 */
	private JPanel buildSearchPanel()
	{
		searchPanel= new JPanel();
		double[][] tl = {{TableLayout.PREFERRED, 10, TableLayout.PREFERRED}, //columns
				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5,
			     TableLayout.PREFERRED, 5, 0, 5, TableLayout.PREFERRED} }; //rows
		layout = new TableLayout(tl);
		searchPanel.setLayout(layout);
		searchPanel.add(UIUtilities.setTextFont("Authors"), "0, 0, l, c");
		searchPanel.add(authors, "2, 0, l, c");
		JLabel label = new JLabel(AUTHORS_EXAMPLE);
		label.setFont(FONT);
		searchPanel.add(label, "2, 2, l, c");
		searchPanel.add(UIUtilities.setTextFont("Date"), "0, 4, l, c");
		searchPanel.add(UIUtilities.buildComponentPanel(dates), "2, 4, l, c");
		searchPanel.add(UIUtilities.setTextFont("Tags"), "0, 8, l, c");
		searchPanel.add(tags, "2, 8, l, c");
		return UIUtilities.buildComponentPanel(searchPanel);
	}

	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		add(buildSearchPanel());
	}
	
	SearchPanel()
	{
		initComponents();
		buildGUI();
	}
	
}
