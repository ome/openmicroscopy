/*
 * ome.rules.drools.AllObjectsRule
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.spi.KnowledgeHelper;
import org.drools.spring.metadata.annotation.java.Condition;
import org.drools.spring.metadata.annotation.java.Consequence;
import org.drools.spring.metadata.annotation.java.Fact;
import org.drools.spring.metadata.annotation.java.Rule;

@Rule
public class AllObjects {
    private static Log log = LogFactory.getLog(AllObjects.class);

    @Condition
    public boolean objectAsserted(@Fact("object")
    Object obj) {
        return true;
    }

    @Consequence
    public void consequence(@Fact("object")
    Object obj, KnowledgeHelper kh) {
        if (log.isDebugEnabled()) {
            log.debug("here i am" + obj); // TODO walk the graph!
        }
    }
}
