package ome.services.blitz.util;

import ome.services.blitz.fire.SessionPrincipal;
import ome.util.messages.InternalMessage;

public abstract class AbstractSessionMessage extends InternalMessage {

    String id;
    
    SessionPrincipal p;
    
    public AbstractSessionMessage(Object source, String sessionId, SessionPrincipal principal) {
        super(source);
        this.id = sessionId;
        this.p = principal;
    }
    
    public String getSessionId() {
        return this.id;
    }

    public SessionPrincipal getPrincipal() {
        return p;
    }


}