/* 
 * org.openmicroscopy.shoola.agents.executions.ui.model.BoundedLongRangeModel
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

package org.openmicroscopy.shoola.agents.executions.ui.model;

//Java imports
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

//Third-party libraries

//Application-internal dependencies


/** 
* A range model based on longs, instead of ints. This code is an adaptation of 
* the DefaultBoundedRangeModel found in the Java SDK
*
* @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
* @version 2.2
* <small>
* (<b>Internal version:</b> $Revision$ $Date$)
* </small>
* @since OME2.2
*/

public class BoundedLongRangeModel {
	
    protected EventListenerList listenerList = new EventListenerList();

    protected transient ChangeEvent changeEvent = null;

	private static final float LABEL_FACTOR=0.2f;

	private long value = 0;
    private long extent = 0;
    private long min = 0;
    private long max = 100;
    private boolean isAdjusting = false;

	
	public BoundedLongRangeModel() {
	}	
	
	public BoundedLongRangeModel(long min,long max) {
        if (max >= min) {
            this.value = min;
            this.extent = max-min;
            this.min = min;
            this.max = max;
        }
        else {
            throw new IllegalArgumentException("invalid range properties");
        }
    }
    
    public long getValue() {
    		return value;
    }
	
	public long getMax() {
		return getValue()+getExtent();
	}
	
	public long getExtent() {
		return extent;
	}
	
	public long getMinimum() {
		return min;
	}
	
	public long getMaximum() {
		return max;
	}
	
	public boolean isInRange(long val) {
		if (val >= value && val <= (value+extent))
			return true;
		else
			return false;
	}
	
    public void setValue(long n) {
        long newValue = Math.max(n, min);
        if(newValue + extent > max) {
            newValue = max - extent; 
        }
        setProperties(newValue, extent, min, max, isAdjusting);
    }
    
    public void setExtent(long n) {
        long newExtent = Math.max(0, n);
        if(value + newExtent > max) {
            newExtent = max - value;
        }
        setProperties(value, newExtent, min, max, isAdjusting);
    }

    public void setMinimum(long n) {
        long newMax = Math.max(n, max);
        long newValue = Math.max(n, value);
        long newExtent = Math.min(newMax - newValue, extent);
        setProperties(newValue, newExtent, n, newMax, isAdjusting);
    }
    
    public void setMaximum(long n) {
        long newMin = Math.min(n, min);
        long newExtent = Math.min(n - newMin, extent);
        long newValue = Math.min(n - newExtent, value);
        setProperties(newValue, newExtent, newMin, n, isAdjusting);
    }

    public void setValueIsAdjusting(boolean b) {
        setProperties(value, extent, min, max, b);
    }

    public boolean getValueIsAdjusting() {
        return isAdjusting; 
    }

    public void setProperties(long newValue,long newEnd) {
    		setProperties(newValue,newEnd-newValue,min,max,false);
    }
    
    public void setProperties(long newValue, 
    		long newExtent, long newMin, long newMax, boolean adjusting)
    {
        if (newMin > newMax) {
            newMin = newMax;
        }
        
        if (newValue > newMax) {
            newMax = newValue;
        }
        
        if (newValue < newMin) {
            newMin = newValue;
        }

        if ((newExtent + newValue) > newMax) {
            newExtent = newMax - newValue;
        }
	
        if (newExtent < 0) {
            newExtent = 0;
        }

        boolean isChange =
            (newValue != value) ||
            (newExtent != extent) ||
            (newMin != min) ||
            (newMax != max) ||
            (adjusting != isAdjusting);

        if (isChange) {
            value = newValue;
            extent = newExtent;
            min = newMin;
            max = newMax;
            isAdjusting = adjusting;

            fireStateChanged();
        }
    }


    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }
    

    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }


    public ChangeListener[] getChangeListeners() {
        return (ChangeListener[])listenerList.getListeners(
                ChangeListener.class);
    }


    /** 
     * Runs each <code>ChangeListener</code>'s <code>stateChanged</code> method.
     * 
     * @see #setProperties
     * @see EventListenerList
     */
    protected void fireStateChanged() 
    {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -=2 ) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }          
        }
    }   


	public String getLowTickString() {
		long low = getValue();
		long high = getMax();
		long lowval =  (long) (low+ LABEL_FACTOR*(high-low));
		return Long.toString(lowval);
	}
	
	public String getHighTickString() {
		long low = getValue();
		long high = getMax();
		long highval =  (long) (low+(1-LABEL_FACTOR)*(high-low));
		return Long.toString(highval);
	}
}

