/*
 * ome.formats.importer.History
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
package ome.formats.importer;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

/**
 * @author Brian W. Loranger
 *
 */
public class HistoryDB implements IObservable
{
    ArrayList<IObserver> observers = new ArrayList<IObserver>();
    
    public SimpleDateFormat day = new SimpleDateFormat("MMM d, ''yy");
    public SimpleDateFormat hour = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat sqlDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private Connection conn;
    
    public Connection getConnection()
    {
        return conn;
    }
    
    private HistoryDB() throws SQLException, ClassNotFoundException
    {
        String saveDirectory = System.getProperty("user.home") + File.separator + "omero";

        if (!new File(saveDirectory).exists()) {
            new File(saveDirectory).mkdir();
        }
        
        // Load the HSQL Database Engine JDBC driver
        Class.forName("org.hsqldb.jdbcDriver");
        
        // Connect to the database
        conn = DriverManager.getConnection(
                "jdbc:hsqldb:file:" + saveDirectory + File.separator + "history",  // filenames
                "sa",                   // username
                "");                    // password
        try 
        {        
            // Create the import history table - updated when import button clicked
            update( "CREATE TABLE import_table ( " +
                    "uID INT IDENTITY, " +
                    "experimenterID BIGINT, " +
                    "date DATETIME, " +
                    "status VARCHAR(64)" +
                    " )" ); 
            
            // Create the file history table - updated with each file import
            update( "CREATE TABLE file_table ( " +
                    "uID INT IDENTITY, " +
                    "importID BIGINT, " +
                    "experimenterID BIGINT, " +
                    "rowNum INT, " +
                    "filename VARCHAR(256), " +
                    "projectID BIGINT, " +
                    "datasetID BIGINT, " +
                    "date DATETIME, " +
                    "status VARCHAR(64)" +
                    " )" );
           
        } catch (SQLException ex2) { 
            //ex2.printStackTrace(); 
        } //ignore SQL error if table already exists
    } // History()

    public static synchronized HistoryDB getHistoryDB()
    {
        if (ref == null) 
        try
        {
            ref = new HistoryDB();
        } catch (Exception e)
        {
            JOptionPane.showMessageDialog(null,
                    "We were not able to connect to the history DB.\n" +
                    "Make sure you do not have a second importer\n" +
                    "running and try again.\n\n" +
                    "Click OK to exit.",
                    "Warning",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();    // could not start db
            System.exit(0);

        }
        return ref;
    }
    
    private static HistoryDB ref;
    
    public void shutdown() throws SQLException {

        Statement st = conn.createStatement();

        // db writes out to files and performs clean shuts down
        // otherwise there will be an unclean shutdown
        // when program ends
        st.execute("SHUTDOWN");
        conn.close();    // if there are no other open connection
    }
    
    public ResultSet getImportResults(HistoryDB db, String table, Long experimenterID)
    {
        if (experimenterID == -1)
        {
            try
            {
                return getQueryResults("SELECT * FROM " + table);
            } catch (SQLException e)
            { e.printStackTrace(); }
            return null;            
        } else
        {
            try
            {
                return getQueryResults("SELECT * FROM " + table + " WHERE ExperimenterID = " + experimenterID);
            } catch (SQLException e)
            { e.printStackTrace(); }
            return null;
        }
    }

    public String stripGarbage(String s) {  
        String good =
            " .-_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String result = "";
        for ( int i = 0; i < s.length(); i++ ) {
            if ( good.indexOf(s.charAt(i)) >= 0 )
                result += s.charAt(i);
        }
        return result;
    }

    public ResultSet getFileResults(HistoryDB db, String table, int importID, 
            Long experimenterID, String string, boolean done, boolean failed, boolean invalid, 
            boolean pending, Date from, Date to)
    {
        String fromString = null, toString = null;
        if (string == null) string = "";
        string = stripGarbage(string);
        
        String queryString = "SELECT * FROM " + table  + " WHERE ExperimenterID = " + experimenterID +
            " AND filename like '%" + string + "%'";

        if (done)
            queryString = queryString + " AND (status = 'done'";
        else
            queryString = queryString + " AND (status != 'done'";
        
        if (failed)
            if (done)
                queryString = queryString + " OR status = 'failed'";
            else
                queryString = queryString + " AND status = 'failed'";
        else
                queryString = queryString + " AND status != 'failed'";


        if (invalid)
            if (done || failed)
                queryString = queryString + " OR status = 'invalid'";
            else
                queryString = queryString + " AND status = 'invalid'";
        else
                queryString = queryString + " AND status != 'invalid'";

        if (pending)
            if (done || failed || invalid)
                queryString = queryString + " OR status = 'pending')";
            else
                queryString = queryString + " AND status = 'pending')";
        else
                queryString = queryString + " AND status != 'pending')";
        
        
        if (importID != -1)
            queryString = queryString + " AND importID = " + importID;
        
        if (from != null)
        {
            fromString = sqlDateFormat.format(from);
            //System.err.println(from + ", " + fromString);
            queryString = queryString + " AND date >= '" + fromString + "'";
        }
        
        if (to != null)
        {
            toString = sqlDateFormat.format(getDaysBefore(to, 1));
            queryString = queryString + " AND date <= '" + toString + "'";
        }

        try
        {
            return getQueryResults(queryString);
        } catch (SQLException e)
        { 
            e.printStackTrace(); 
        }
        return null;
    }
    
    public boolean wipeUserHistory(Long experimenterID)
    {   
        if (experimenterID == -1)
        {
            try
            {
                update("DELETE * FROM import_table");
                update("DELETE * FROM file_table");
            } catch (SQLException e)
            { return false; }
            return true;            
        } else
        {
            try
            {
                update("DELETE FROM import_table WHERE experimenterID = " + experimenterID);
                update("DELETE FROM file_table WHERE experimenterID = " + experimenterID);
            } catch (SQLException e)
            {
                e.printStackTrace();
                return false; 
            }
            return true;
        }        
    }
    
    public int insertImportHistory(long experimenterID, String status) 
    throws SQLException
    {
        
        return update(
                "INSERT INTO import_table(experimenterID, date, status) " +
                "VALUES(" + 
                    experimenterID + ", " +
                    "'" + sqlDateTimeFormat.format(new Date()) + "', " +
                    "'" + status + "'" + 
                    " )");
    } // insertHistory()
 
    public synchronized ResultSet getGeneratedKeys() throws SQLException {
        Statement st = null;

        st = conn.createStatement();    // statements

        String sql = "CALL IDENTITY()"; 
        
        PreparedStatement ps = conn.prepareCall(sql); 
         
        ResultSet rs = st.executeQuery(sql); 
        
        st.close();
        
        return rs;
    }

    public int getLastKey() throws SQLException {
        ResultSet rs = getGeneratedKeys();
        rs.next(); 
        return rs.getInt(1);
    }
    
    public int insertFileHistory(long importID, long experimenterID, int rowNum, 
            String filename, long projectID, long datasetID, String status) 
    throws SQLException
    {
        return update(
                "INSERT INTO file_table(importID, experimenterID, rowNum, filename, " +
                "projectID, datasetID, date, status) " +
                "VALUES(" + 
                    importID + ", " +
                    experimenterID + ", " +
                    rowNum + ", " +
                    "'" + filename + "', " +
                    projectID + ", " +
                    datasetID + ", " +
                    "'" + sqlDateTimeFormat.format(new Date()) + "', " +
                    "'" + status + "'" + 
                    " )");
    } // insertHistory()

    public int updateImportStatus(int id, String status) throws SQLException
    {
        int result = update("UPDATE import_table SET status = '" + status + "' WHERE uID = " + id);
        //System.err.println("observers" + observers.size());
        notifyObservers("QUICKBAR_UPDATE");
        return result;
    } // updateHistoryStatus()

    public int updateFileStatus(int id, int rowNum, String status) throws SQLException
    {
        return update("UPDATE file_table SET status = '" + status + "' WHERE importID = " + id + 
                " AND rowNum = " + rowNum);       
    } // updateHistoryStatus()
    
    //use for SQL commands CREATE, DROP, INSERT and UPDATE
    public synchronized int update(String expression) throws SQLException {

        //System.err.println(expression);
        
        Statement st = null;

        st = conn.createStatement();    // statements

        int i = st.executeUpdate(expression);    // run the query

        if (i == -1) {
            System.out.println("db error : " + expression);
        }

        st.close();
        
        return i;
    } // Update()
    
    public synchronized ResultSet getQueryResults(String expression) throws SQLException {
        Statement st = null;
        ResultSet rs = null;

        //System.err.println(expression);
        
        st = conn.createStatement();         // statement objects can be reused with

        // repeated calls to execute but we
        // choose to make a new one each time
        return st.executeQuery(expression);    // run the query
    }    

    public DefaultListModel getImportListByDate(Date start, Date end)
    {
        ResultSet rs;
        try
        {
            rs = getQueryResults("SELECT * FROM import_table" + 
                    " WHERE date BETWEEN '" + sqlDateFormat.format(end) + 
                    "' AND '" + sqlDateFormat.format(start) + "'");

            String icon;
            DefaultListModel list = new DefaultListModel();
            for (; rs.next(); ) {
                if (rs.getString("status").equals("complete"))
                    icon = "gfx/import_done_16.png";
                else
                    icon = "gfx/warning_msg16.png";
                String dayString = day.format(rs.getObject("date"));
                String hourString = hour.format(rs.getObject("date"));

                if (day.format(new Date()).equals(dayString))
                    dayString = "Today";

                if (day.format(getYesterday()).equals(dayString))
                {
                    dayString = "Yesterday";
                }

                ImportEntry entry = new ImportEntry(dayString + " " + hourString, icon, rs.getInt("uID"));
                list.addElement(entry);
            }
            return list;
        } catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public Date getYesterday() {
        return getDayBefore(new Date());
    }
 
    public Date getDayBefore(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }
  
    // days should be negative
    public Date getDaysBefore(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    // Observable methods
    
    public boolean addObserver(IObserver object)
    {
        return observers.add(object);
    }
    
    public boolean deleteObserver(IObserver object)
    {
        return observers.remove(object);
        
    }

    public void notifyObservers(Object message)
    {
        for (IObserver observer:observers)
        {
            observer.update(this, message);
        }
    }
}
