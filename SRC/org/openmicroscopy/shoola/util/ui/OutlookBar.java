/*
 * org.openmicroscopy.shoola.util.ui.OutlookBar 
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
package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * Provides a component that is similar to a <code>JTabbedPane</code>, but 
 * instead of maintaining tabs, it uses Outlook-style bars to control the 
 * visible component.
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
public class OutlookBar 
	extends JPanel
	implements ActionListener
{

	/** Contains the buttons displayed at the top of the bar */
    private JPanel 							topPanel;

    /** Contains the buttons displayed at the bottom of the bar */
    private JPanel							bottomPanel;

    /** Map used to preserve the order of the bar. */
    private Map<Integer, OutlookBarComponent>	bars;
    
    /** The currently visible bar. */
    private int 							visibleBar;
    
    /** The currently visible component. */
    private JComponent						visibleComponent;
   
    /** Initializes the components. */
    private void initComponents()
    {
    	bars = new LinkedHashMap<Integer, OutlookBarComponent>();
    	topPanel = new JPanel(new GridLayout(1, 1));
    	bottomPanel = new JPanel(new GridLayout(1, 1));
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	 setLayout(new BorderLayout());
         add(topPanel, BorderLayout.NORTH);
         add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /** Rebuilds the bar. */
    protected void rebuild()
    {
    	//Determine the numbers of bars.
    	int totalBars = bars.size();
        int topBars = visibleBar+1;
        int bottomBars = totalBars-topBars;

        Iterator i = bars.keySet().iterator();
        topPanel.removeAll();
        GridLayout topLayout = (GridLayout) topPanel.getLayout();
        topLayout.setRows(topBars);
        OutlookBarComponent bar = null;
        for (int j = 0; j < topBars; j++) {
        	bar = bars.get(i.next());
            topPanel.add(bar.getButton());
        }
        topPanel.validate();

        if (visibleComponent != null)
            remove(visibleComponent);
        
        visibleComponent = bar.getComponent();
        add(visibleComponent, BorderLayout.CENTER);

        // Render the bottom bars: remove all components, reset the GridLayout 
        // to hold to correct number of bars, add the bars, and "validate" it to
        // cause it to re-layout its components
        bottomPanel.removeAll();
        GridLayout bottomLayout = (GridLayout) bottomPanel.getLayout();
        bottomLayout.setRows(bottomBars);
        for( int j = 0; j < bottomBars; j++ ) {
        	bar = bars.get(i.next());
            bottomPanel.add(bar.getButton());
        }
        bottomPanel.validate();
        validate();
    }
    
    /** Creates a new instance, */
    public OutlookBar()
    {
    	initComponents();
    	buildGUI();
    }
    
    /**
     * Adds the passed component to the bar.
     * 
     * @param name		The name of the bar.
     * @param component	The component ot add. Mustn't be <code>null</code>.
     */
    public void addBar(String name, JComponent component)
    {
    	if (component == null)
    		throw new NullPointerException("No component specified.");
    	addBar(name, component, null);
    }
    
    /**
     * Adds the passed component to the bar.
     * 
     * @param name		The name of the bar.
     * @param component	The component ot add. Mustn't be <code>null</code>.
     * @param icon		The icon added to the bar.
     */
    public void addBar(String name, JComponent component, Icon icon)
    {
    	int i = bars.size();
    	OutlookBarComponent bar = new OutlookBarComponent(name, component, 
    													icon, i);
    	bar.getButton().addActionListener(this);
    	bar.setCurrent(i == 0);
    	bars.put(i, bar); 
    	rebuild();
    }
    
    /**
     * Removes the bar specified by the passed component.
     * 
     * @param index The index of the bar to remove.
     */
    public void removeBar(int index)
    {
    	OutlookBarComponent bar = bars.get(index);
    	if (bar == null) return;
    	bars.remove(bar);
    	if (bar.getIndex() == visibleBar) visibleBar = 0;
    	rebuild();
    }
    
    /**
     * Sets the visible bar.
     * 
     * @param index The UI index of the visible bar. If the index is not valid
     * 				the default bar i.e. the first one is selected.
     */
    public void setVisibleBar(int index)
    {
    	if (index >= 0 && index < bars.size())
    		visibleBar = index;
    	else visibleBar = 0;
    	rebuild();
    }

    /** 
     * Sets the selected component.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
	public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();
		int currentBar = 0;
		Iterator i = bars.keySet().iterator();
		OutlookBarComponent bar;
		while (i.hasNext()) {
			bar = bars.get(i.next());
			if (src == bar.getButton()) {
				visibleBar = currentBar;
				rebuild();
				break;
			}
			currentBar++;
		}
	}
    
}
