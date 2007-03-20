package ome.services.icy.util;

import ome.services.icy.fire.SessionPrincipal;

/**
 * @author josh
 * 
 */
public class CreateSessionMessage extends AbstractSessionMessage {

    private static final long serialVersionUID = 6132548299119420025L;

    public CreateSessionMessage(Object source, String sessionId,
            SessionPrincipal principal) {
        super(source, sessionId, principal);
    }

}