package ome.services.icy.util;

import ome.system.Principal;
import ome.util.messages.InternalMessage;

public abstract class AbstractSessionMessage extends InternalMessage {

    String id;
    
    Principal p;
    
    public AbstractSessionMessage(Object source, String sessionId, Principal principal) {
        super(source);
        this.id = sessionId;
        this.p = principal;
    }
    
    public String getSessionId() {
        return this.id;
    }

    public Principal getPrincipal() {
        return p;
    }


}