/*
 * ome.client.ThrowsConflictResolver
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.client;

// Java imports
import java.util.ConcurrentModificationException;

// Third-party libraries

// Application-internal dependencies
import ome.model.IObject;

/**
 * default strategy implementation which simply throws a
 * {@link java.util.ConcurrentModificationException} regardless of arguments.
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.more@gmx.de"> josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $ $Date: $)
 *          </small>
 * @since OME3.0
 */
public class ThrowsConflictResolver implements ConflictResolver {
    public final static String MESSAGE = "Version conflict discovered in session:";

    /**
     * strategy method that, in fact, doesn't resolve any conflicts. But simply
     * throws a {@link ConcurrentModificationException}
     * 
     * @param registeredVersion
     *            currently registered entity. Ignored.
     * @param possibleReplacement
     *            entity which is to be considered for replacement. Ignored.
     * @return does not return.
     * @throws ConcurrentModificationException.
     *             Always thrown.
     */
    public IObject resolveConflict(IObject registeredVersion,
            IObject possibleReplacement) throws ConcurrentModificationException {
        throw new ConcurrentModificationException(message(registeredVersion,
                possibleReplacement));
    }

    /** produces exception message based on the two inputs */
    protected String message(IObject registeredVersion,
            IObject possibleReplacement) {
        StringBuffer sb = new StringBuffer(MESSAGE.length() + 64);
        sb.append(MESSAGE);
        sb.append("\nregisteredVersion:\t");
        sb.append(registeredVersion);
        sb.append(" (hash=" + registeredVersion.hashCode() + ")");
        sb.append("\npossibleReplacement\t");
        sb.append(possibleReplacement);
        sb.append(" (hash=" + possibleReplacement.hashCode() + ")");
        return sb.toString();
    }
}
