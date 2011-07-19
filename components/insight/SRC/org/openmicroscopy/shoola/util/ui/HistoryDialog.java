/*
 * org.openmicroscopy.shoola.util.ui.HistoryDialog
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;

//Third-party libraries
import info.clearthought.layout.TableLayout;

//Application-internal dependencies

/** 
 * Basic pop-up menu displaying collection of objects.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class HistoryDialog
    extends JPopupMenu    
{
    
    /** Bound property indicating that an item is selected in the list. */
    public static final String SELECTION_PROPERTY ="selection";
    
    /** The maximum height of the menu. */
    private static final int	MAX_HEIGHT = 200;
    
    /** The list hosting the data. */
    private JList       			history;
    
    /** The list hosting the second collection of data. */
    private JList					secondaryHistory;
    
    /** The width of the component. */
    private int         			width;
    
    /** The data to display. */
    private Object[]    			data;

    /** The original array of items to display. */
    private Object[]				originalData;
    
    /** The data to display. */
    private Object[]    			secondaryData;

    /** The original array of items to display. */
    private Object[]				secondaryOriginalData;
    
    /** Helper reference to the font metrics of a {@link JList}. */
    private FontMetrics 			metrics;
    
    /** Flag indicating to take into account or not the case sensitivity. */
    private boolean					caseSensitive;
    
    /** Computes and sets the pop up size. */
    private void determinePopupSize()
    {
    	int  height = metrics.getHeight()*data.length+10;
        if (secondaryHistory != null) 
        	height += metrics.getHeight()*secondaryData.length+10;
        if (height > MAX_HEIGHT) height = MAX_HEIGHT;
        setPopupSize(new Dimension(width, height));
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        JPanel list = new JPanel();
        list.setOpaque(!list.isOpaque());
        if (secondaryHistory == null) {
        	list.setLayout(new BorderLayout());
            list.add(history);
        } else {
        	double[][] tl = {{TableLayout.FILL}, //columns
     				{TableLayout.PREFERRED, TableLayout.PREFERRED, 
        			TableLayout.PREFERRED} }; //rows
        	list.setLayout(new TableLayout(tl));
        	list.add(history, "0, 0");
        	list.add(new JSeparator(), "0, 1");
        	list.add(secondaryHistory, "0, 2");
        }
        metrics = getFontMetrics(history.getFont());
        determinePopupSize();
        add(new JScrollPane(list));
    }
    
    /** Initializes the components. */
    private void initComponents()
    {
    	caseSensitive = false;
        history = new JList(data);
        history.addMouseListener(new MouseAdapter() {
		
			/**
			 * Fires a property with the selected object.
			 * @see MouseAdapter#mousePressed(MouseEvent)
			 */
			public void mousePressed(MouseEvent e) {
				ListSelectionModel model = history.getSelectionModel();
		    	if (model.isSelectionEmpty()) return;
		    	int index = history.getSelectedIndex();
		    	firePropertyChange(SELECTION_PROPERTY, null, data[index]);
                setVisible(false);
			}
		
		});
        if (secondaryData == null || secondaryData.length == 0) return;
        secondaryHistory = new JList(secondaryData);
        secondaryHistory.addMouseListener(new MouseAdapter() {
		
        	/**
			 * Fires a property with the selected object.
			 * @see MouseAdapter#mousePressed(MouseEvent)
			 */
			public void mousePressed(MouseEvent e) {
				ListSelectionModel model = secondaryHistory.getSelectionModel();
		    	if (model.isSelectionEmpty()) return;
		    	int index = secondaryHistory.getSelectedIndex();
		    	firePropertyChange(SELECTION_PROPERTY, null, 
		    						secondaryData[index]);
                setVisible(false);
			}
		
		});
    }
    
    /**
     * Creates a new instance.
     * 
     * @param data  The data to display.
     * @param width The width of the component.
     */
    public HistoryDialog(Object[] data, int width)
    {
    	this.originalData = data;
        this.data = data;
        this.width = width;
        initComponents();
        buildGUI();
    }
    
    /**
     * Creates a new instance.
     * 
     * @param data  		The data to display.
     * @param secondaryData	The second array of data to display.
     * @param width 		The width of the component.
     */
    public HistoryDialog(Object[] data, Object[] secondaryData, int width)
    {
    	this.originalData = data;
        this.data = data;
        this.width = width;
        this.secondaryData = secondaryData;
        secondaryOriginalData = secondaryData;
        initComponents();
        buildGUI();
    }
    
    /**
     * Sets the renderer for the various list.
     * 
     * @param rnd The value to set.
     */
    public void setListCellRenderer(DefaultListCellRenderer rnd)
    {
    	if (rnd != null) history.setCellRenderer(rnd);
    }
    
    /**
     * Sets the renderer for the various list.
     * 
     * @param rndMain 	The value to set.
     * @param rnd 		The value to set.
     */
    public void setListCellRenderer(DefaultListCellRenderer rndMain,
    								DefaultListCellRenderer rnd)
    {
    	if (rndMain != null) history.setCellRenderer(rndMain);
    	if (rnd != null && secondaryHistory != null) 
    		secondaryHistory.setCellRenderer(rnd);
    }
    
    /**
     * Returns the selected object or <code>null</code>
     * and hides the menu.
     * 
     * @return See above.
     */
    public Object getSelectedTextValue()
    {
    	ListSelectionModel model = history.getSelectionModel();
    	if (!model.isSelectionEmpty()) {
    		int index = history.getSelectedIndex();
    		setVisible(false);
        	return data[index];
    	}
        // Now we try the secondary list
        if (secondaryHistory != null) {
        	 model = secondaryHistory.getSelectionModel();
        	 if (!model.isSelectionEmpty()) {
        		 int index = secondaryHistory.getSelectedIndex();
        		 setVisible(false);
        		 return secondaryData[index];
         	}
        }
        return null;
    }
    
    /**
     * Selects an item in the list if one of the elements starts with
     * the passed string. Returns <code>true</code> if an item is selected,
     * <code>false</code> otherwise.
     * 
     * @param v The value to handle.
     * @return See above.
     */
    public boolean setSelectedTextValue(String v)
    {
    	if (v == null || v.length() == 0) return false;
    	if (originalData == null) return false;
    	String value;
    	if (!caseSensitive) v = v.toLowerCase();
    	List<Object> l = new ArrayList<Object>();
    	for (int i = 0; i < originalData.length; i++) {
			value = originalData[i].toString();
			if (!caseSensitive) value = value.toLowerCase();
			if (value.startsWith(v)) {
				l.add(originalData[i]);
			}
		}
    	if (secondaryHistory != null) {
    		List<Object> sl = new ArrayList<Object>();
        	for (int i = 0; i < secondaryOriginalData.length; i++) {
    			value = secondaryOriginalData[i].toString();
    			if (!caseSensitive) value = value.toLowerCase();
    			if (value.startsWith(v)) {
    				sl.add(secondaryOriginalData[i]);
    			}
    		}
        	int n = sl.size();
        	int m = l.size();
    		if (n == 0 && m == 0) {
    			resetListData();
    			return false;
    		}
    		else if (n == 0 && m > 0) {
    			data = new Object[m];
            	Iterator j = l.iterator();
            	int index = 0;
            	while (j.hasNext()) {
        			data[index] = j.next();
        			index++;
        		}
            	history.setListData(data);
            	history.setSelectedValue(data[0], true);
            	secondaryData = new Object[n];
            	secondaryHistory.setListData(secondaryData);
            	determinePopupSize();
            	return true;
    		} else if (n > 0 && m == 0) {
    			secondaryData = new Object[n];
            	Iterator j = sl.iterator();
            	int index = 0;
            	while (j.hasNext()) {
            		secondaryData[index] = j.next();
        			index++;
        		}
            	secondaryHistory.setListData(secondaryData);
            	secondaryHistory.setSelectedValue(secondaryData[0], true);
            	data = new Object[m];
            	history.setListData(data);
            	determinePopupSize();
            	return true;
    		} else if (n > 0 && m > 0) {
    			data = new Object[m];
            	Iterator j = l.iterator();
            	int index = 0;
            	while (j.hasNext()) {
        			data[index] = j.next();
        			index++;
        		}
            	history.setListData(data);
            	history.setSelectedValue(data[0], true);
            	secondaryData = new Object[n];
            	j = sl.iterator();
            	index = 0;
            	while (j.hasNext()) {
            		secondaryData[index] = j.next();
        			index++;
        		}
            	secondaryHistory.setListData(secondaryData);
            	determinePopupSize();
            	return true;
    		}
    	} else {
    		if (l.size() == 0) {
    			resetListData();
    			return false;
    		}
    		data = new Object[l.size()];
        	Iterator j = l.iterator();
        	int index = 0;
        	while (j.hasNext()) {
    			data[index] = j.next();
    			index++;
    		}
        	history.setListData(data);
        	history.setSelectedValue(data[0], true);
        	determinePopupSize();
        	return true;
    	}
    	return false;
    }
    
    /**
     * Sets the selected value. 
     * 
     * @param increase 	Pass <code>true</code> to increase the index,
     * 					<code>false</code> to decrease the index.
     */
    public void setSelectedIndex(boolean increase)
    {
    	ListSelectionModel model = history.getSelectionModel();
    	int m = data.length;
    	if (secondaryHistory == null) {
    		//if (!model.isSelectionEmpty() && m > 0) {
    		if (m > 0) {
        		int index;
        		if (model.isSelectionEmpty()) index = 0;
        		else index = history.getSelectedIndex();
            	Object r = null;
            	int l = m-1;
            	if (increase) {
            		if (index < l) index += 1;
            		else index = 0;
            	} else {
            		if (index > 0) index -= 1;
            		else index = l;
            	}
            	r = data[index];
            	if (r != null) history.setSelectedValue(r, true);
        	}
    	} else {
    		int n = secondaryData.length;
    		if (n == 0 && m == 0) return;
    		if (n == 0 && m > 0) {
    			if (!model.isSelectionEmpty()) {
            		int index = history.getSelectedIndex();
                	Object r = null;
                	int l = m-1;
                	if (increase) {
                		if (index < l) index += 1;
                		else index = 0;
                	} else {
                		if (index > 0) index -= 1;
                		else index = l;
                	}
                	r = data[index];
                	if (r != null) history.setSelectedValue(r, true);
            	}
    		} else if (n > 0 && m == 0) {
    			model = secondaryHistory.getSelectionModel();
    			if (!model.isSelectionEmpty()) {
            		int index = secondaryHistory.getSelectedIndex();
                	Object r = null;
                	int l = n-1;
                	if (increase) {
                		if (index < l) index += 1;
                		else index = 0;
                	} else {
                		if (index > 0) index -= 1;
                		else index = l;
                	}
                	r = secondaryData[index];
                	if (r != null) secondaryHistory.setSelectedValue(r, true);
            	}
    		} else if (n > 0 && m > 0) {
    			ListSelectionModel msh = secondaryHistory.getSelectionModel();
    			int index;
    			if (!model.isSelectionEmpty()) {
            		index = history.getSelectedIndex();
                	int l = m-1;
                	if (increase) {
                		if (index < l) {
                			index += 1;
                			history.setSelectedValue(data[index], true);
                		} else { //end 
                			model.clearSelection();
                			index = 0;
                			secondaryHistory.setSelectedValue(
                					secondaryData[0], true);
                		}
                	} else {
                		if (index > 0) {
                			index -= 1;
                			history.setSelectedValue(data[index], true);
                		} else {
                			model.clearSelection();
                			index = n-1;
                			secondaryHistory.setSelectedValue(
                					secondaryData[index], true);
                		}
                	}
            	} else {
            		if (msh.isSelectionEmpty()) 
            			return;
            		int l = n-1;
            		index = secondaryHistory.getSelectedIndex();
                	if (increase) {
                		if (index < l) {
                			index += 1;
                			secondaryHistory.setSelectedValue(
                					secondaryData[index], true);
                		} else { //end 
                			index = 0;
                			msh.clearSelection();
                			history.setSelectedValue(data[0], true);
                		}
                	} else {
                		if (index > 0) {
                			index -= 1;
                			secondaryHistory.setSelectedValue(
                					secondaryData[index], true);
                		} else {
                			index = m-1;
                			secondaryHistory.clearSelection();
                			history.setSelectedValue(data[index], true);
                			
                		}
                	}
            	}
    		}
    	}
    }

    /** Resets the original list of data. */
	public void resetListData()
	{
		if (originalData == null || originalData.length == 0) return;
		data = originalData;
		history.setListData(originalData);
    	history.setSelectedValue(originalData[0], true);
    	if (secondaryHistory != null) {
    		secondaryData = secondaryOriginalData;
    		secondaryHistory.setListData(secondaryOriginalData);
    	}
    	determinePopupSize();
	}
    
}
