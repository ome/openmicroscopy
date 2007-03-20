package ome.services.icy.util;

import ome.services.icy.fire.SessionPrincipal;

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