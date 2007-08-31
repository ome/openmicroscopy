/*
 * omero.importer.engine.LoginHandler
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2007 Open Microscopy Environment
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

package omero.importer.engine;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.ejb.EJBAccessException;
import javax.naming.CommunicationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoginHandler
    implements IObservable
{ 
    /** Logger for this class. */
    private static Log  log    = LogFactory.getLog(LoginHandler.class);
    
    ArrayList<IObserver> observers = new ArrayList<IObserver>();
    
    // Use executor to ensure only one tryLogin() thread running at at time.
    Executor executor = Executors.newSingleThreadExecutor();
    
    public enum Message {DISCONNECTED, CONNECTING, CONNECTED,
        AUTH_ERROR, URL_ERROR, UNKNOWN_ERROR}
    
    private Message connectionStatus = Message.DISCONNECTED;

    private OMEROMetadataStore store;
    
    private String username;
    private String password;
    private String port;
    private String server;

    /**
     * Attempt a login, throwing exceptions and setting ConnectionStatus as 
     * needed. If you attempt tryLogin() with an existing Metadatastore in 
     * place, the old MetadataStore will be overwritten.
     * 
     * This method uses notifyObserver and sets the connectionStatus flag
     * (which can be called with getConnectionStatus()) for feedback.
     * 
     * @param username
     * @param password
     * @param port
     * @param server
     */
    public void login(String username, String password, String port, String server)
    {
        this.username = username;
        this.password = password;
        this.port = port;
        this.server = server;
        
        if (username == null) throw new NullPointerException("username cannot be null");
        if (password == null) throw new NullPointerException("password cannot be null");
        if (port == null) throw new NullPointerException("port cannot be null");
        if (server == null) throw new NullPointerException("server cannot be null");
        
        notifyObservers(Message.CONNECTING);
        
        executor.execute(
                new Runnable()
                {
                    public void run()
                    {
                        tryLogin();
                        notifyObservers(connectionStatus);
                    }
                });
    }
    
    /**
     * @return
     */
    private boolean tryLogin()
    {
        try
        {
            store = new OMEROMetadataStore(username, password, server, port);
            store.getProjects();
        } catch (EJBAccessException e)
        {
            connectionStatus = Message.AUTH_ERROR;
            log.debug(String.format("An authorization error has occured.", e));
        } catch (Throwable t)
        {
            if (t.getCause() instanceof CommunicationException)
            {
                connectionStatus = Message.URL_ERROR;
                log.debug(String.format("a host connection error has occured.", t));               
            } else
            {
            connectionStatus = Message.UNKNOWN_ERROR;
            t.printStackTrace();
            log.debug(String.format("An unknown error has occured in the login handler.", t));
            }
            return false;
        }
        connectionStatus = Message.CONNECTED;
        return true;
    }    
    

    public OMEROMetadataStore getMetadataStore()
    {
        return store;
    }

    public Message getConnectionStatus()
    {
        return connectionStatus;
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
