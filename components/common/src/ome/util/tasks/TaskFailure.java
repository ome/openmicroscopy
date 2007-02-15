/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.tasks;

// Java imports

// Third-party libraries

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.RootException;

/**
 * {@link RuntimeException} which can be thrown by
 * {@link Task#handleException(RuntimeException)} for special handling by the
 * default {@link Run#handleException()}. It is assumed that the stack trace of
 * {@link TaskFailure instances} are empty and that the message will contain
 * all significant information. This allows {@link Run} to print more
 * user-friendly messages. The {@link #status} variable can also be passed to
 * {@link System#exit(int)}.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @see Configuration
 * @see Task
 * @see Run
 * @since 3.0-Beta2
 */
@RevisionDate("$Date: 2007-02-14 17:05:51 +0100 (Wed, 14 Feb 2007) $")
@RevisionNumber("$Revision: 1279 $")
public class TaskFailure extends RootException {

    public final static int defaultStatus = -1234567;

    public int status = defaultStatus;

    public TaskFailure(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        setStackTrace(new StackTraceElement[]{});
        return this;
    }
    
}
