/*
 * $Id$
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

package ome.formats.importer;


//Import the GUI classes
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

//Import the Java classes
import java.util.*;

/**
 * A JOutlookBar provides a component that is similar to a JTabbedPane, but instead of maintaining
 * tabs, it uses Outlook-style bars to control the visible component
 */
public class JOutlookBar extends JPanel implements ActionListener
{
    private static final long serialVersionUID = 1L;

    DefaultListModel today = new DefaultListModel(); 
    DefaultListModel yesterday = new DefaultListModel();
    DefaultListModel thisWeek = new DefaultListModel();
    DefaultListModel lastWeek = new DefaultListModel();
    DefaultListModel thisMonth = new DefaultListModel();
    
    /**
     * The top panel: contains the buttons displayed on the top of the JOutlookBar
     */
    private JPanel topPanel = new JPanel( new GridLayout( 1, 1 ) );

    /**
     * The bottom panel: contains the buttons displayed on the bottom of the JOutlookBar
     */
    private JPanel bottomPanel = new JPanel( new GridLayout( 1, 1 ) );

    /**
     * A LinkedHashMap of bars: we use a linked hash map to preserve the order of the bars
     */
    private Map bars = new LinkedHashMap();

    /**
     * The currently visible bar (zero-based index)
     */
    private int visibleBar = 0;

    /**
     * A place-holder for the currently visible component
     */
    private JComponent visibleComponent = null;

    /**
     * Creates a new JOutlookBar; after which you should make repeated calls to
     * addBar() for each bar
     */
    public JOutlookBar()
    {
        this.setLayout( new BorderLayout() );
        this.add( topPanel, BorderLayout.NORTH );
        this.add( bottomPanel, BorderLayout.SOUTH );
    }

    /**
     * Adds the specified component to the JOutlookBar and sets the bar's name
     * 
     * @param  name      The name of the outlook bar
     * @param  componenet   The component to add to the bar
     */
    public void addBar( String name, JComponent component )
    {
        BarInfo barInfo = new BarInfo( name, component );
        barInfo.getButton().addActionListener( this );
        this.bars.put( name, barInfo );
        render();
    }

    /**
     * Adds the specified component to the JOutlookBar and sets the bar's name
     * 
     * @param  name      The name of the outlook bar
     * @param  icon      An icon to display in the outlook bar
     * @param  componenet   The component to add to the bar
     */
    public void addBar( String name, Icon icon, JComponent component )
    {
        BarInfo barInfo = new BarInfo( name, icon, component );
        barInfo.getButton().addActionListener( this );
        this.bars.put( name, barInfo );
        render();
    }

    /**
     * Removes the specified bar from the JOutlookBar
     * 
     * @param  name  The name of the bar to remove
     */
    public void removeBar( String name )
    {
        this.bars.remove( name );
        render();
    }

    /**
     * Returns the index of the currently visible bar (zero-based)
     * 
     * @return The index of the currently visible bar
     */
    public int getVisibleBar()
    {
        return this.visibleBar;
    }

    /**
     * Programmatically sets the currently visible bar; the visible bar
     * index must be in the range of 0 to size() - 1
     * 
     * @param  visibleBar   The zero-based index of the component to make visible
     */
    public void setVisibleBar( int visibleBar )
    {
        if( visibleBar >= 0 &&
                visibleBar <= this.bars.size() - 1 )
        {
            this.visibleBar = visibleBar;
            render();
        }
    }

    /**
     * Causes the outlook bar component to rebuild itself; this means that
     * it rebuilds the top and bottom panels of bars as well as making the
     * currently selected bar's panel visible
     */
    public void render()
    {
        // Compute how many bars we are going to have where
        int totalBars = this.bars.size();
        int topBars = this.visibleBar + 1;
        int bottomBars = totalBars - topBars;


        // Get an iterator to walk through out bars with
        Iterator itr = this.bars.keySet().iterator();


        // Render the top bars: remove all components, reset the GridLayout to
        // hold to correct number of bars, add the bars, and "validate" it to
        // cause it to re-layout its components
        this.topPanel.removeAll();
        GridLayout topLayout = ( GridLayout )this.topPanel.getLayout();
        topLayout.setRows( topBars );
        BarInfo barInfo = null;
        for( int i=0; i< topBars; i++ )
        {
            String barName = ( String )itr.next();
            barInfo = ( BarInfo )this.bars.get( barName );
            this.topPanel.add( barInfo.getButton() );
        }
        this.topPanel.validate();


        // Render the center component: remove the current component (if there
        // is one) and then put the visible component in the center of this panel
        if( this.visibleComponent != null )
        {
            this.remove( this.visibleComponent );
        }
        this.visibleComponent = barInfo.getComponent();
        this.add( visibleComponent, BorderLayout.CENTER );

        // Render the bottom bars: remove all components, reset the GridLayout to
        // hold to correct number of bars, add the bars, and "validate" it to
        // cause it to re-layout its components
        this.bottomPanel.removeAll();
        GridLayout bottomLayout = ( GridLayout )this.bottomPanel.getLayout();
        bottomLayout.setRows( bottomBars );
        for( int i=0; i<bottomBars; i++ )
        {
            String barName = ( String )itr.next();
            barInfo = ( BarInfo )this.bars.get( barName );
            this.bottomPanel.add( barInfo.getButton() );
        }
        this.bottomPanel.validate();


        // Validate all of our components: cause this container to re-layout its subcomponents
        this.validate();
    }

    /**
     * Invoked when one of our bars is selected
     */
    public void actionPerformed( ActionEvent e )
    {
        int currentBar = 0;
        for( Iterator i=this.bars.keySet().iterator(); i.hasNext(); )
        {
            String barName = ( String )i.next();
            BarInfo barInfo = ( BarInfo )this.bars.get( barName );
            if( barInfo.getButton() == e.getSource() )
            {
                // Found the selected button
                this.visibleBar = currentBar;
                render();
                return;
            }
            currentBar++;
        }
    }
    
    public JPanel getListPanel(JList list)
    {
        list.setCellRenderer(new ImportCellRenderer());
        list.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        
        JPanel panel = new JPanel( new BorderLayout() );
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().add(list);
        panel.add(scrollPane);
        
        // Add a listener for mouse clicks
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList)evt.getSource();
                if (evt.getClickCount() > 0) {          // Double-click
                    // Get item index
                    int index = list.locationToIndex(evt.getPoint());
                    if (index >= 0)
                    {
                        ImportEntry entry = (ImportEntry) list.getModel().getElementAt(index);
                        firePropertyChange("QUICK_HISTORY", -1, entry.importKey);
                    }
                }
            }
        });
        return panel;
    }

    public void updatePanelList(JList list, DefaultListModel mine, DefaultListModel theirs)
    {
        mine.clear();
        //System.err.println("listmodel size:" + theirs.size());
        for (int i =0; i < theirs.size(); i++)
        {
            //System.err.println(theirs.get(i));
            mine.addElement(theirs.get(i));
            //System.err.println(mine.size()); 
        }
        list.validate();
        this.visibleComponent.validate();

    }
    
    /**
     * Debug test...
     */
    public static void main( String[] args )
    {
        JFrame frame = new JFrame( "JOutlookBar Test" );
        JOutlookBar outlookBar = new JOutlookBar();
        outlookBar.setVisibleBar( 2 );
        frame.getContentPane().add( outlookBar );

        frame.setSize( 800, 600 );
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation( d.width / 2 - 400, d.height / 2 - 300 );
        frame.setVisible( true );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    }

    /**
     * Internal class that maintains information about individual Outlook bars;
     * specifically it maintains the following information:
     * 
     * name      The name of the bar
     * button     The associated JButton for the bar
     * component    The component maintained in the Outlook bar
     */
    class BarInfo
    {
        /**
         * The name of this bar
         */
        private String name;

        /**
         * The JButton that implements the Outlook bar itself
         */
        private JButton button;

        /**
         * The component that is the body of the Outlook bar
         */
        private JComponent component;

        /**
         * Creates a new BarInfo
         * 
         * @param  name    The name of the bar
         * @param  component  The component that is the body of the Outlook Bar
         */
        public BarInfo( String name, JComponent component )
        {
            this.name = name;
            this.component = component;
            this.button = new JButton( name );
            Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 3, 0);
            Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
            Border compoundBorder = BorderFactory.createCompoundBorder(etchedBorder, emptyBorder);
            this.button.setBorder(compoundBorder);
        }

        /**
         * Creates a new BarInfo
         * 
         * @param  name    The name of the bar
         * @param  icon    JButton icon
         * @param  component  The component that is the body of the Outlook Bar
         */
        public BarInfo( String name, Icon icon, JComponent component )
        {
            this.name = name;
            this.component = component;
            this.button = new JButton( name, icon );
            Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 3, 0);
            Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
            Border compoundBorder = BorderFactory.createCompoundBorder(etchedBorder, emptyBorder);
            this.button.setBorder(compoundBorder);
        }

        /**
         * Returns the name of the bar
         * 
         * @return The name of the bar
         */
        public String getName()
        {
            return this.name;
        }

        /**
         * Sets the name of the bar
         * 
         * @param  The name of the bar
         */
        public void setName( String name )
        {
            this.name = name;
        }

        /**
         * Returns the outlook bar JButton implementation
         * 
         * @return   The Outlook Bar JButton implementation
         */
        public JButton getButton()
        {
            return this.button;
        }

        /**
         * Returns the component that implements the body of this Outlook Bar
         * 
         * @return The component that implements the body of this Outlook Bar
         */
        public JComponent getComponent()
        {
            return this.component;
        }
    }
}

class ImportEntry {
    private final String title;
    private final String imagePath;
    private ImageIcon image;
    public final int importKey;

    public ImportEntry(String title, String imagePath, int importKey) {
        this.title = title;
        this.imagePath = imagePath;
        this.importKey = importKey;
    }

    public String getTitle() {
        return title;
    }
        
    public ImageIcon getImage() {
        if (imagePath != null)
        {
            java.net.URL imgURL = Main.class.getResource(imagePath);
            if (imgURL != null)
            {
               image = new ImageIcon(imgURL);
            } else
            {
                System.err.println("Couldn't find icon: " + imagePath);
            }
        }
        return image;
    }

    // Override standard toString method to give a useful result
    public String toString() {
        return title;
    }
}

class ImportCellRenderer extends JLabel implements ListCellRenderer {

    public ImportCellRenderer() {
        setOpaque(true);
        setIconTextGap(0);
    }

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        ImportEntry entry = (ImportEntry) value;
        setText(entry.getTitle());
        setIcon(entry.getImage());
        setFont(UIManager.getFont("TableCell.font"));
        
        if (isSelected) {
            //setToolTipText("test");
            setBackground(UIManager.getColor("Table.selectionBackground"));
            setForeground(Color.black);
        } else {
            setBackground(Color.white);
            setForeground(Color.black);
        }
        return this;
    }
}
