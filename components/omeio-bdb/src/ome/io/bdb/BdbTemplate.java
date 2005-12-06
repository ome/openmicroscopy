/*
 * ome.io.bdb.BdbTemplate
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.io.bdb;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

// Java imports

// Third-party libraries

// Application-internal dependencies

/**
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public class BdbTemplate
{

    Database          db;

    OperationStatus[] status = new OperationStatus[1];

    public BdbTemplate(Database database)
    {
        db = database;
    }

    public OperationStatus getStatus()
    {
        return status[0];
    }

    public Object executeWithCursor(CursorCallback action)
    {
        Cursor cursor = null;
        try
        {
            cursor = db.openCursor(null, null);
            return action.doWithCursor(cursor, status);

        } catch (DatabaseException e)
        {
            throw new RuntimeException(
                    "Error while executing cursor callback.", e);
        } finally
        {
            try
            {
                if (cursor != null)
                {
                    cursor.close();
                }
            } catch (Exception e)
            {
                throw new RuntimeException("Closing cursor failed.", e); // FIXME
            } finally
            {
                cursor = null;
            }

        }
    }

    public Object executeWithPlaneCollector(final PlaneCollectorCallback action)
    {
        
        try { 
            List keys = (List) executeWithCursor(new CursorCallback(){
                public Object doWithCursor(Cursor cursor, OperationStatus[] status) throws DatabaseException {
                    
                    List keys = new ArrayList();
                    
                    DatabaseEntry key = new DatabaseEntry(ByteUtils.planeToKey(action.getId(),0,0,0));
                    DatabaseEntry data = new DatabaseEntry();
                    
                    cursor.getSearchKey(key,data,LockMode.DEFAULT);
                    long newId = ByteUtils.byteArrayToLong(key.getData(),0);
                    while (Long.valueOf(action.getId()).equals(Long.valueOf(newId))){
                        keys.add(key);
                        DatabaseEntry next = new DatabaseEntry();
                        cursor.getNext(next,data,LockMode.DEFAULT);
                        newId = ByteUtils.byteArrayToLong(key.getData(),0);
                    }
               
                    return keys;
               
                };
            });
        
            return action.doWithCollector(keys);
            
        } catch (DatabaseException e){
            throw new RuntimeException("Error while executing collector callback",e);
        }
    }

}
