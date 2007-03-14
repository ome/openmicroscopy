package ome.services.icy.util;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.context.ApplicationEvent;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import ome.api.ServiceInterface;
import ome.system.OmeroContext;
import ome.system.Principal;
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
public class CreateSessionMessage extends AbstractSessionMessage {

    private static final long serialVersionUID = 6132548299119420025L;

    public CreateSessionMessage(Object source, String sessionId, Principal principal) {
        super(source, sessionId, principal);
    }
    
}