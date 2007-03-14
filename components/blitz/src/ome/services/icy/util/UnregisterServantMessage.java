/*
 *   $Id$
 *
 *   Copyright (c) 2007 Glencoe Software Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.icy.util;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.context.ApplicationEvent;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import ome.api.ServiceInterface;
import ome.system.OmeroContext;
import ome.util.Filterable;
import ome.util.messages.InternalMessage;
import omero.RType;
import omero.ServerError;
import omero.Time;
import omero.model.IObject;
import omero.util.IceMapper;

/**
 * @author josh
 * 
 */
public class UnregisterServantMessage extends InternalMessage {

    private static final long serialVersionUID = 3409582093802L;

    String key;
    Ice.Current curr;
    
    public UnregisterServantMessage(Object source, String serviceKey, Ice.Current current) {
        super(source);
        this.key = serviceKey;
        this.curr = current;
    }
    
    public String getServiceKey() {
        return this.key;
    }
    
    public Ice.Current getCurrent() {
        return this.curr;
    }
    
}