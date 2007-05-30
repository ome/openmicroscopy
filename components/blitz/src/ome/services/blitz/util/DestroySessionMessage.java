package ome.services.blitz.util;

import ome.services.blitz.fire.SessionPrincipal;

/**
 * @author josh
 * 
 */
public class DestroySessionMessage extends AbstractSessionMessage {

    private static final long serialVersionUID = 7132548299119420025L;

    public DestroySessionMessage(Object source, String sessionId,
            SessionPrincipal principal) {
        super(source, sessionId, principal);
    }

}