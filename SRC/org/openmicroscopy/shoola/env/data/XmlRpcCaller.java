/*
 * org.openmicroscopy.shoola.env.data.XmlRpcCaller
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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
 * Written by:    Douglas Creager <dcreager@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */




package org.openmicroscopy.shoola.env.data;

import java.net.URL;
import java.util.*;
import java.io.*;
import org.apache.xmlrpc.*;

public class XmlRpcCaller
{
    public static final String  NULL_REFERENCE = ">>OBJ:NULL";

    public static boolean TRACE_CALLS = false;
    public static boolean USE_LITE_CLIENT = true;

    private XmlRpcClient  xmlrpc;
    private Vector        vparams = new Vector();
    private String        sessionKey = null;

    private File  traceFilename;
    private PrintWriter  traceFile;

    public XmlRpcCaller(URL url)
    {
        try
        {
            xmlrpc = createClient(url);
            XmlRpc.setKeepAlive(false);
        } catch (Exception e) {
            xmlrpc = null;
            System.err.println(e);
        }
    }

    protected XmlRpcClient createClient(URL url)
    {
        if (USE_LITE_CLIENT)
            return new XmlRpcClientLite(url);
        else
            return new XmlRpcClient(url);
    }

    public void login(String username, String password)
    {
        synchronized(this)
        {
            if (sessionKey == null)
            {
                if (TRACE_CALLS)
                {
                    try
                    {
                        traceFilename = File.createTempFile("xmlrpc-",".trc");
                        traceFile = new PrintWriter(
                            new BufferedWriter(new FileWriter(traceFilename)),
                            true);
                        System.err.println("Using trace file "+traceFilename);
                    } catch (IOException e) {
                        System.err.println("Could not create trace file!");
                    }
                    traceFile.println("Login "+username+" ***");
                }

                vparams.addElement(username);
                vparams.addElement(password);
                sessionKey = invoke("createSession").toString();

                if (sessionKey == null || sessionKey.equals(""))
                    throw new RemoteException("Could not log in");
            }
        }
    }

    public void logout()
    {
        synchronized(this)
        {
            if (sessionKey != null)
            {
                vparams.addElement(sessionKey);
                invoke("closeSession");
                sessionKey = null;

                if (TRACE_CALLS)
                {
                    traceFile.println("Logout");
                    traceFile.close();

                    traceFilename = null;
                    traceFile = null;
                }
            }
        }
    }

    public String getSessionKey() { return sessionKey; }

    protected void addParameter(Object object)
    {
        synchronized(this)
        {
            vparams.add(encodeObject(object));
        }
    }

    private Object invoke(String method)
    {
        synchronized(this)
        {
            try
            {
                if (TRACE_CALLS)
                {
                    traceFile.print(method+"(");
                    for (int i = 0; i < vparams.size(); i++)
                    {
                        traceFile.print(vparams.elementAt(i)+",");
                    }
                    traceFile.println(")");
                }

                Object retval = xmlrpc.execute(method,vparams);
                return retval;
            } catch (Exception e) {
                if (TRACE_CALLS)
                {
                    traceFile.println("execute exception ("+e.getClass()+
                                      "): "+e.getMessage());
                    e.printStackTrace(traceFile);
                }
                throw new RemoteException(e.getMessage());
            } finally {
                vparams.clear();
            }
        }
    }

    public Object invoke(String method, Object[] params)
    {
        synchronized(this)
        {
            if (sessionKey == null)
                throw new IllegalArgumentException("Have not logged in");

            addParameter(sessionKey);
            if (params != null)
            {
                for (int i = 0; i < params.length; i++)
                    addParameter(params[i]);
            }
            return invoke(method);
        }
    }

    public Object dispatch(String method)
    {
        return dispatch(method,(Object[]) null);
    }

    public Object dispatch(String method,
                           Object param1)
    {
        return dispatch(method,new Object[] {param1});
    }

    public Object dispatch(String method, Object[] params)
    {
        synchronized(this)
        {
            if (sessionKey == null)
                throw new IllegalArgumentException("Have not logged in");

            addParameter(sessionKey);
            addParameter(method);
            if (params != null)
            {
                for (int i = 0; i < params.length; i++)
                    addParameter(params[i]);
            }
            return invoke("dispatch");
        }
    }

    protected Object encodeObject(Object object)
    {
        if (object == null)
            return NULL_REFERENCE;
        else if (object instanceof List) {
            List list = new Vector();
            Iterator it = ((List) object).iterator();
            while (it.hasNext())
                list.add(encodeObject(it.next()));
            return list;
        } else if (object instanceof Map) {
            Map map = new Hashtable();
            Iterator it = ((Map) object).keySet().iterator();
            while (it.hasNext())
            {
                Object key = it.next();
                Object value = ((Map) object).get(key);
                map.put(encodeObject(key),encodeObject(value));
            }
            return map;
        } else {
            return object;
        }
    }

}
