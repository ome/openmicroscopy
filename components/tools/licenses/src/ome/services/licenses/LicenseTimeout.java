/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.licenses;

/**
 * (CLIENT) Exception thrown on a regular method call when a previously valid
 * license has timed out as interpreted by the {@link LicenseStore}.
 * The {@link LicenseStore} may attempt to re-acquire a license
 * transparently for the user, in which case this exception 
 * {@link #isFailedReacquisition() specifies} that no licenses were 
 * available.
 * 
 * @author Josh Moore, josh.moore @ gmx.de
 * @since  3.0-RC1
 */
public class LicenseTimeout extends LicenseException {

    private static final long serialVersionUID = 2061806055561108993L;
    
    Boolean reacquireFailed = Boolean.FALSE;

    public LicenseTimeout(String msg) {
        super(msg);
    }

    public LicenseTimeout(String msg, boolean failedReacquisition) {
	super(msg);
	reacquireFailed = Boolean.valueOf(failedReacquisition);
    }

    /**
     * Returns {@link Boolean#TRUE} if a reacquisition was attempted
     * and failed, {@link Boolean#FALSE} otherwise.
     */
    public Boolean isFailedReacquisition() {
	return reacquireFailed;
    }

}
