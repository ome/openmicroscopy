/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.procs;

// Java imports
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Third-party libraries
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateCallback;

// Application-internal dependencies
import ome.parameters.Parameters;
import static ome.parameters.Parameters.*;
import ome.parameters.QueryParameter;

/**
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public interface ProcessCallback {

    void processFinished(Process proc);
    void processCancelled(Process proc);
    
}
