/*
 * ome.io.bdb.Datastore
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

// Java imports
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.StatsConfig;

// Application-internal dependencies
import ome.api.IO;

/**
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public class Datastore implements IO
{

    private static Log        log    = LogFactory.getLog(Datastore.class);

    private String            path, name;

    private Environment       env;

    private EnvironmentConfig envCfg = new EnvironmentConfig();

    private Database          db;

    private DatabaseConfig    dbCfg  = new DatabaseConfig();

    public Datastore(String databasePath, String databaseName,
            boolean allowCreate, boolean readOnly)
    {
        path = databasePath;
        name = databaseName;
        envCfg.setAllowCreate(allowCreate);
        envCfg.setReadOnly(readOnly);
        // envCfg.setTransactional(true);
        dbCfg.setAllowCreate(envCfg.getAllowCreate());
        dbCfg.setReadOnly(envCfg.getReadOnly());
        // dbCfg.setTransactional(true);
    }

    public void init()
    {
        try
        {
            env = new Environment(new File(path), envCfg);
            db = env.openDatabase(null, name, dbCfg);
            db.preload(256000000,60); // FIXME (config)
        } catch (Exception e)
        {
            throw new RuntimeException("Error initializing "
                    + Datastore.class.getName(), e);
        }

        log.info("Opened BDB datastore " + this);

    }

    public void close()
    {
        try
        {

            if (db != null)
            {
                db.close();
                db = null;
            }

            if (env != null)
            {
                env.cleanLog();
                env.close();
                env = null;
            }
        } catch (Exception e)
        {
            throw new RuntimeException("Error closing "
                    + Datastore.class.getName(), e);
        }

        log.info("Closed BDB datastore " + this);

    }

    public long last()
    {

        Long result = (Long) new BdbTemplate(db)
                .executeWithCursor(new CursorCallback()
                {

                    public Object doWithCursor(Cursor cursor,
                            OperationStatus[] status) throws DatabaseException
                    {

                        DatabaseEntry key = new DatabaseEntry();
                        DatabaseEntry data = new DatabaseEntry();
                        status[0] = cursor.getPrev(key, data, LockMode.DEFAULT);

                        if (status[0] == OperationStatus.SUCCESS)
                        {
                            return Long.valueOf(ByteUtils.byteArrayToLong(key
                                    .getData(), 0));
                        } else
                        {
                            return Long.valueOf(0);
                        }

                    };
                });
        return result.longValue();
    }

    public String[] list()
    {
        return (String[]) new BdbTemplate(db)
                .executeWithCursor(new CursorCallback()
                {

                    public Object doWithCursor(Cursor cursor,
                            OperationStatus[] status) throws DatabaseException
                    {

                        List found = new ArrayList();
                        DatabaseEntry key = new DatabaseEntry();
                        DatabaseEntry data = new DatabaseEntry();

                        while (cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS)
                        {
                            found.add(ByteUtils.keyToPlane(key.getData()));
                        }

                        return (String[]) found
                                .toArray(new String[found.size()]);

                    };
                });
    }

    public void stats(PrintStream out){
        StatsConfig config = new StatsConfig();
        config.setClear(true);
        try {
            out.println(env.getStats(config));
        } catch (DatabaseException e){
            out.println("Error writing datastore stats.");
            e.printStackTrace(out);
        }
    }
    
    public void putPlane(byte[] plane, long id, int z, int c, int t)
    {
        DatabaseEntry key = new DatabaseEntry(ByteUtils.planeToKey(id, z, c, t));
        DatabaseEntry data = new DatabaseEntry(plane);

        putPlane(key, data);
    }

    public void putPlane(byte[] plane, long id, int z, int c, int t,
            int offset, int length)
    {
        DatabaseEntry key = new DatabaseEntry(ByteUtils.planeToKey(id, z, c, t));
        DatabaseEntry data = new DatabaseEntry(plane);
        data.setPartial(offset, length, true);

        putPlane(key, data);
    }

    void putPlane(DatabaseEntry key, DatabaseEntry data)
    {
        OperationStatus ret;

        try
        {
            ret = db.put(null, key, data);
            if (ret != OperationStatus.SUCCESS)
                throw new DatabaseException("Put failed. Status: "
                        + ret.toString());
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean planeExists(long id, int z, int c, int t)
    {
        DatabaseEntry key = new DatabaseEntry(ByteUtils.planeToKey(id, z, c, t));
        DatabaseEntry data = new DatabaseEntry();

        OperationStatus ret;

        try
        {
            ret = db.get(null, key, data, LockMode.DEFAULT);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        if (ret == OperationStatus.SUCCESS)
        {
            return true;
        } else if (ret == OperationStatus.NOTFOUND)
        {
            return false;
        } else
        {
            log.error("Key neither found nor not found. Status: "
                    + ret.toString());
            return false;
        }
    }

    public void deletePlane(long id, int z, int c, int t)
    {
        DatabaseEntry key = new DatabaseEntry(ByteUtils.planeToKey(id, z, c, t));

        OperationStatus ret;

        try
        {
            ret = db.delete(null, key);
            if (ret != OperationStatus.SUCCESS)
                throw new DatabaseException("Delete failed. Status: "
                        + ret.toString());
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    public byte[] getPlane(long id, int z, int c, int t)
    {
        DatabaseEntry key = new DatabaseEntry(ByteUtils.planeToKey(id, z, c, t));
        DatabaseEntry data = new DatabaseEntry();

        return getPlane(key, data);

    }

    public byte[] getPlane(long id, int z, int c, int t, int offset, int length)
    {
        DatabaseEntry key = new DatabaseEntry(ByteUtils.planeToKey(id, z, c, t));
        DatabaseEntry data = new DatabaseEntry();
        data.setPartial(offset, length, true);

        return getPlane(key, data);
    }

    byte[] getPlane(DatabaseEntry key, DatabaseEntry data)
    {

        OperationStatus ret;

        try
        {
            ret = db.get(null, key, data, LockMode.DEFAULT);
            if (ret != OperationStatus.SUCCESS)
                throw new DatabaseException("Get failed. Status: "
                        + ret.toString());

            return data.getData();

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    // PlaneCollectorCallback(Long,Integer,Integer,Integer)
    public boolean pixelsExist(final long id)
    {
        Boolean result = (Boolean) new BdbTemplate(db)
                .executeWithCursor(new CursorCallback()
                {

                    public Object doWithCursor(Cursor cursor,
                            OperationStatus[] status) throws DatabaseException
                    {

                        DatabaseEntry key = new DatabaseEntry(ByteUtils
                                .planeToKey(id, 0, 0, 0));
                        DatabaseEntry data = new DatabaseEntry();

                        status[0] = cursor.getSearchKey(key, data,
                                LockMode.DEFAULT);

                        if (status[0] == OperationStatus.SUCCESS)
                        {
                            long newId = ByteUtils.byteArrayToLong(key
                                    .getData(), 0);
                            return Boolean.valueOf(id == newId);
                        }

                        return Boolean.FALSE;

                    };
                });
        return result.booleanValue();
    }

    public byte[][][][] getPixels(long id)
    {
        return (byte[][][][]) new BdbTemplate(db).executeWithPlaneCollector(new PlaneCollectorCallback(id){
           public Object doWithCollector(List keys) throws DatabaseException
            {
               System.out.println(keys);
               return null;
            } 
        });
    }

    public void putPixels(byte[][][][] pixels, long id)
    {
        for (int t = 0; t < pixels.length; t++)
        {
            byte[][][] pixs_t = pixels[t];
            for (int c = 0; c < pixs_t.length; c++)
            {
                byte[][] pixs_tc = pixs_t[c];
                for (int z = 0; z < pixs_tc.length; z++)
                {
                    putPlane(pixs_tc[z], id, z, c, t);
                }
            }
        }
    }

    public void deletePixels(long id)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

}
