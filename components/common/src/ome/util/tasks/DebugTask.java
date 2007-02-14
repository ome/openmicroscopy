/*
 * ome.util.tasks.SimpleTask
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.tasks;

// Java imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

// Third-party libraries

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.system.ServiceFactory;

/**
 * Task for debugging, which simply prints all properties passed to it on
 * standard out.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @see Task
 * @since 3.0-Beta2
 */
@RevisionDate("$Date: 2007-01-24 17:23:09 +0100 (Wed, 24 Jan 2007) $")
@RevisionNumber("$Revision: 1208 $")
public class DebugTask extends SimpleTask {

    /**
     * Sole constructor. Delegates to {@link SimpleTask}
     * 
     * @see SimpleTask#SimpleTask(ServiceFactory, Properties)
     */
    public DebugTask(ServiceFactory serviceFactory, Properties properties) {
        super(serviceFactory, properties);
    }

    /**
     * Does nothing.
     */
    @Override
    public void doTask() {
        super.doTask();

        Properties p = getProperties();
        Set keys = p.keySet();
        List<String> sortedList = new ArrayList(keys);
        Collections.sort(sortedList);
        for (String key : sortedList) {
            System.out.println(String.format("%s => %s", key, p
                    .getProperty(key)));
        }
    }
}
