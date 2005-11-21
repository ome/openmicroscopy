/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.CBSearchTabView
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.hiviewer.clipboard;



//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.Colors;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.FindRegExCmd;

import pojos.DataObject;

/** 
 * The <code>Search</code> panel.
 *
 * @author  Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@comnputing.dundee.ac.uk
 *              </a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class CBSearchTabView
    extends ClipBoardTab
{

    /** The default size of the coloured panel. */
    private static final int    SQUARE_SIZE = 15;
    
    /** The default border color. */
    private static final Color  BORDER_COLOR = Color.BLACK;
    
    /** Identifies that the search is performed at the title level. */
    private static final int    IN_TITLE = 0;
    
    /** Identifies that the search is performed at the annotation level. */
    private static final int    IN_ANNOTATION = 1;
    
    /** Identifies that the search is performed  in annotation and title. */
    private static final int    IN_T_AND_A = 2;
    
    /** Indicates to retrieved all annotated {@link DataObject}s. */
    static final int            ALL_ANNOTATED = 3;
     
    /** The number of search options. */
    private static final int    MAX = 3;
    
    /** Search level options. */
    private static String[] searchChoices;
    
    static {
        searchChoices = new String[MAX+1];
        searchChoices[IN_TITLE] = "in title";
        searchChoices[IN_ANNOTATION] = "in annotation";
        searchChoices[IN_T_AND_A] = "in title or annotation";
        searchChoices[ALL_ANNOTATED] = "annotated";
    }
    
    /** The search button. */
    JButton                     searchButton;
    
    /** The clear button. */
    JButton                     clearButton;
    
    /** The combo box displaying the search levels. */
    JComboBox                   searchType;
    
    /** The text field hosting the regular expression. */
    private JTextField          searchString;
    
    /** The panel hosting the tree displaying the result. */
    private JPanel              treeHolder;
    
    /** The panel hosting the search controls. */
    private JPanel              searchPanel;
    
    /** The panel hosting the legend. */
    private JPanel              legendPanel;
    
    /** The results tree. */
    private SearchResultsPane   pane;
    
    /** Initializes the UI components. */
    private void initComponents()
    {
        searchButton = new JButton("Search");
        clearButton = new JButton("Clear");
        searchString = new JTextField();
        searchType = new JComboBox(searchChoices);
        setComponentsEnabled(false);
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        // griddy constraints
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        
        add(buildSearchPanel(), c);

        // add legend panel
        c.gridy = 1;
        add(buildLegendPanel(), c);
        
        // add tree panel
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = GridBagConstraints.REMAINDER;
        add(buildTreePanel(), c);
    }
    
    /**
     * Builds the search panel.
     * 
     * @return See below.
     */
    private JPanel buildSearchPanel()
    {
        searchPanel = new JPanel();
        searchPanel.setBorder(new TitledBorder("--"));
        searchPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        // package buttons into a panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(searchButton);
        buttonPanel.add(clearButton);
                
        // griddy constraints
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(searchString, c);
        
        // add buttons panel
        c.gridy = 1;
        c.fill = GridBagConstraints.NONE;
        searchPanel.add(buttonPanel, c);
        
        // add drop down
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(searchType, c);
        
        return searchPanel;
    }

    
    /**
     * Builds the legend panel.
     * 
     * @return See below.
     */
    private JPanel buildLegendPanel()
    {
        legendPanel = new JPanel();
        legendPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        // griddy constraints
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.NONE;
        
        // one
        c.insets = new Insets(1, 1, 1, 1);
        Colors colors = Colors.getInstance();
        legendPanel.add(buildLegend(colors.getColor(Colors.TITLE_BAR_HIGHLIGHT), 
                        "Currently selected"), c);
        
        // two
        c.gridx = 1;
        legendPanel.add(buildLegend(colors.getColor(Colors.REGEX_ANNOTATION),
                        "Found in annotation"), c);
        
        // three
        c.gridx = 0;
        c.gridy = 1;
        legendPanel.add(buildLegend(colors.getColor(Colors.REGEX_TITLE), 
                        "Found in title"), c);
           
        // four
        c.gridx = 1;
        c.gridy = 1;
        legendPanel.add(buildLegend(
                        colors.getColor(Colors.REGEX_TITLE_AND_ANNOTATION),
                        "Found in either"), c);

        //five
        c.gridx = 0;
        c.gridy = 2;
        legendPanel.add(buildLegend(colors.getColor(Colors.ANNOTATED),
                        "Found in annotated"), c);
        return legendPanel;
    }
    
    /**
     * Returns panel with coloured square and title.
     * 
     * @param color The selected color.
     * @param title The related title
     * @return See below.
     */
    private JPanel buildLegend(Color color, String title)
    {
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JPanel legendColour = new JPanel();
        legendColour.setSize(SQUARE_SIZE, SQUARE_SIZE);
        legendColour.setBorder(new LineBorder(BORDER_COLOR));
        legendColour.setBackground(color);
        JLabel legendTitle = new JLabel(title);    
        p.add(legendColour);
        p.add(legendTitle);
        return p;
    }
    
    /**
     * Creates the panel hosting the tree of results. 
     * 
     * @return See below.
     */
    private JPanel buildTreePanel()
    {
        treeHolder = new JPanel();
        treeHolder.setBorder(new TitledBorder("Tree of search results"));
        treeHolder.setLayout(new BorderLayout());        
        return treeHolder;
    }
    
    /**
     * Sets the buttons enabled. 
     * 
     * @param b The boolean flag.
     */
    private void setComponentsEnabled(boolean b)
    {
        searchButton.setEnabled(b);
        clearButton.setEnabled(b);
        searchType.setEnabled(b);
    }
    
    private Dimension getControlsDimension()
    {
        Dimension ds = searchPanel.getPreferredSize();
        Dimension dl = legendPanel.getPreferredSize();
        return new Dimension(ds.width+dl.width+1, ds.height+dl.height+1);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model The <code>ClipBoardModel</code> Model.
     * @param view The <code>ClipBoardUI</code> hosting this sub-component.
     * @param controller The <code>ClipBoardControl</code> control.
     */
    CBSearchTabView(ClipBoardModel model, ClipBoardUI view, ClipBoardControl
                    controller)
    {
        super(model, view, controller);
        initComponents();
        new CBSearchTabViewMng(this);
        buildGUI();
    }
    
    /** 
     * Clears the previous results.
     */
    void clearSearchValue()
    {
       searchString.setText(null);
       treeHolder.removeAll();
       treeHolder.repaint();
       pane = null;
    }
    
    /** 
     * Returns the regular expression.
     * 
     * @return See above.
     */
    String getSearchValue() { return searchString.getText(); }
    
    /**
     * Returns the selected searching type.
     * 
     * @return See above.
     */
    int getSearchType()
    {
        int i = searchType.getSelectedIndex();
        switch (i) {
            case IN_ANNOTATION:
                return FindRegExCmd.IN_ANNOTATION;
            case CBSearchTabView.IN_T_AND_A:
                return FindRegExCmd.IN_T_AND_A;
            case CBSearchTabView.IN_TITLE:
            default:
                return FindRegExCmd.IN_TITLE;
        }
    }
    
    /**
     * Displays the results of the search action.
     * 
     * @param foundNodes The set of nodes to display.
     */
    void setSearchResults(Set foundNodes)
    {
        pane = new SearchResultsPane(this, foundNodes);
        pane.addPropertyChangeListener(SearchResultsPane.LOCALIZE_IMAGE_DISPLAY,
                                        controller);
        treeHolder.removeAll();
        treeHolder.add(new JScrollPane(pane), BorderLayout.CENTER);
        treeHolder.revalidate();
    }
    
    /**
     * Returns the popupMenu.
     * 
     * @return See below.
     */
    TreePopupMenu getPopupMenu() { return view.getPopupMenu(); }
    
    /**
     * Returns the currently selected data object.
     * 
     * @return See above.
     */
    DataObject getDataObject()
    {
        if (pane != null) return pane.getDataObject();
        return null;
    }
    
    /**
     * Updates the UI components when a node is selected in the
     * <code>Browser</code>.
     */
    protected void onDisplayChange(ImageDisplay selectedDisplay)
    {
        //TODO: Need to define a strategy to perform a search when user selects 
        //a new node.
        if (model.getPaneIndex() != ClipBoard.SEARCH_PANEL) return;
        if (selectedDisplay == null || selectedDisplay instanceof ImageNode) {
            setComponentsEnabled(false);
            searchPanel.setBorder(new TitledBorder(" -- ")); 
            return;
        }
        String title = "";
        //Same approach as cmd.
        if (selectedDisplay.getParentDisplay() == null) { //root
            title = "all";
        } else {
            if (!(selectedDisplay instanceof ImageNode))
                title = selectedDisplay.getTitle();
        }
        setComponentsEnabled(true);
        searchPanel.setBorder(new TitledBorder("Search '"+title+"' for: ")); 
    }
    
    /** Overriden to size the tree hosting the result. */
    public void setBounds(int x, int y, int w, int h)
    {
        Rectangle r = view.getVisibleRect();
        Dimension dTree = treeHolder.getSize();
        int hTab = view.getTabPaneHeight();
        int hDiff = hTab-dTree.height;
        if (hDiff > 0) {
            Dimension dC = getControlsDimension();
            Dimension d = new Dimension(dTree.width-10, r.height-hDiff-10);
            if (hTab < r.height) treeHolder.setPreferredSize(d);
            else if (dTree.height > dC.height) treeHolder.setPreferredSize(d);
        }
        super.setBounds(x, y, w, h);
    }
    
}
