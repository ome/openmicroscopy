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
import omero.RType;
import omero.ServerError;
import omero.Time;
import omero.model.IObject;
import omero.util.IceMapper;

/**
 * Provides helper methods so that servant implementations need not extend a
 * particular {@link Class}.
 * 
 * @author josh
 * 
 */
public class UnregisterServantMessage extends ApplicationEvent {

    String key;
    Ice.Current curr;
    
    public UnregisterServantMessage(Object source, String serviceKey, Ice.Current current) {
        super(source);
        this.key = key;
        this.curr = current;
    }
    
    public String getServiceKey() {
        return this.key;
    }
    
    public Ice.Current getCurrent() {
        return this.curr;
    }
    
}