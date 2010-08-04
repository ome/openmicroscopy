/*
 * ome.formats.importer.gui.History
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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
package ome.formats.importer.gui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportEvent;

/**
 * @author Brian W. Loranger
 *
 */
public abstract class HistoryTableAbstractDataSource implements IObservable, IHistoryTableDataSource 
{

    ArrayList<IObserver> observers = new ArrayList<IObserver>();
    
	// Calendar date methods
	
    /**
     * Return the date for yesterday
     * 
     * @return yesterday's Date
     */
    public Date getYesterday() 
    {
        return getDayBefore(new Date());
    }
 
    /**
     * Return the day before date given
     * 
     * @param date given
     * @return The day before date given
     */
    public Date getDayBefore(Date date) 
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }
  
    // days should be negative
    /**
     * Returns the date which is 'days' before 'date'
     * @param date
     * @param days
     * @return The Date X days before Date given 
     */
    public Date getDaysBefore(Date date, int days) 
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }
    
    /**
     * Only allows search to be conducted on 'legal' characters.
     * 
     * @param queryString - string passed in
     * @return - 'clean' string returned
     */
    public String stripIllegalSearchCharacters(String queryString) 
    {  
    	String good =
    		" .-_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    	String result = "";
    	for ( int i = 0; i < queryString.length(); i++ ) {
    		if ( good.indexOf(queryString.charAt(i)) >= 0 )
    			result += queryString.charAt(i);
    	}

    	StringBuffer buf = new StringBuffer();
    	for ( int i = 0; i < queryString.length(); i++ ) {
    		if ( good.indexOf(queryString.charAt(i)) >= 0 )
    			buf.append(queryString.charAt(i));
    	}
    	return buf.toString();
    }

    // Observable methods
    
    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#addObserver(ome.formats.importer.IObserver)
     */
    public boolean addObserver(IObserver object)
    {
        return observers.add(object);
    }
    
    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#deleteObserver(ome.formats.importer.IObserver)
     */
    public boolean deleteObserver(IObserver object)
    {
        return observers.remove(object);
    }

    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#notifyObservers(ome.formats.importer.ImportEvent)
     */
    public void notifyObservers(ImportEvent event)
    {
        for (IObserver observer:observers)
        {
            observer.update(this, event);
        }
    }
	
}
