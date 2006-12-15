/*
 * ome.rules.drools.TimeRule
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.rules.drools;

//Java imports
import java.util.Date;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.spi.KnowledgeHelper;
import org.drools.spring.metadata.annotation.java.Condition;
import org.drools.spring.metadata.annotation.java.Consequence;
import org.drools.spring.metadata.annotation.java.Fact;
import org.drools.spring.metadata.annotation.java.Rule;

//Application-internal dependencies

/**
 * resets all time values to be no later than the current time.
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since OMERO 1.0
 */
@Rule
public class TimeRule {

    private static Log log = LogFactory.getLog(TimeRule.class);
    
    @Condition
    public boolean timeIsInFuture(@Fact("date") Date date) {
    	return date.after(new Date(System.currentTimeMillis()));
	}

	@Consequence
	public void resetTime(@Fact("date") Date date, KnowledgeHelper kh){
		date.setTime(System.currentTimeMillis());
	}
}