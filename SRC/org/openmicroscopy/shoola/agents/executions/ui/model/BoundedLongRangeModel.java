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
* A range model based on longs, instead of ints. This code is similar
* to the DefaultBoundedRangeModel found in the Java SDK, but it differs in
* implementation internals and provides a more-stripped down api.
* 
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
	
    private EventListenerList listeners = new EventListenerList();

    private transient ChangeEvent changeEvent = null;


	private long start = 0;
    private long end = 0;
    private long min = 0;
    private long max = 100;
    private boolean adjusting = false;

	
	public BoundedLongRangeModel() {
	}	
	
	public BoundedLongRangeModel(long min,long max) {
        if (max >= min) {
            this.start = min;
            this.end = max;
            this.min = min;
            this.max = max;
        }
        else {
            throw new IllegalArgumentException("invalid range properties");
        }
    }
    
    public long getStart() {
    		return start;
    }
	
	public long getEnd() {
		return end;
	}
	
	public long getMinimum() {
		return min;
	}
	
	public long getMaximum() {
		return max;
	}
	
	public boolean isInRange(long val) {
		if (val >= start && val <= end)
			return true;
		else
			return false;
	}
	
    public void setStart(long n) {
    		if (n < min)
    			n=min;
        update(n,end, adjusting);
    }
    
    public void setEnd(long n) {
    		if (n > max)
    			n =max;
    		update(start, n, adjusting);
    }
    
    public void offset(long dx) {
    		long newStart = start+dx;
    		long newEnd = end+dx;
    		if (newStart < min)
    			newStart = min;
    		if (newEnd > max)
    			newEnd = max;
    		update(newStart,newEnd,adjusting);
    }

    public void setAdjusting(boolean b) {
        update(start, end, b);
    }

    public void reset() {
    		update(min,max,false);
    }
    
    
    private void update(long newStart, long newEnd, boolean adjusting)
    {
    		if (newStart < min)
    			newStart = min;
    		
    		if (newEnd > max)
    			newEnd = max;
    
        if (newEnd < newStart) 
            newEnd = newStart;

        boolean changed =
            (newStart != start) ||
            (newEnd != end) ||
            (this.adjusting != adjusting);

        if (changed) {
            start = newStart;
            end = newEnd;
            this.adjusting = adjusting;

            fireChange();
        }
    }


    public void addListener(ChangeListener l) {
        listeners.add(ChangeListener.class, l);
    }
    

    private void fireChange() 
    {
        Object[] targets = listeners.getListenerList();
        for (int i = targets.length - 2; i >= 0; i -=2 ) {
            if (targets[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener)targets[i+1]).stateChanged(changeEvent);
            }          
        }
    }   
}
