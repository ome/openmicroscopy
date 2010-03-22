package ome.formats.importer.gui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportEvent;

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
        return result;
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
