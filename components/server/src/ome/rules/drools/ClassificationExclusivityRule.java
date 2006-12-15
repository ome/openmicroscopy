/*
 * ome.rules.drools.ClassificationExclusivityRule
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

// Java imports
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Third-party libraries
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.CategoryGroupCategoryLink;
import ome.model.containers.CategoryImageLink;
import ome.model.core.Image;
import ome.util.ContextFilter;
import ome.util.Filterable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.spi.KnowledgeHelper;
import org.drools.spring.metadata.annotation.java.Condition;
import org.drools.spring.metadata.annotation.java.Consequence;
import org.drools.spring.metadata.annotation.java.Data;
import org.drools.spring.metadata.annotation.java.Fact;
import org.drools.spring.metadata.annotation.java.Rule;

// Application-internal dependencies

/**
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
@Rule
public class ClassificationExclusivityRule {

    private static Log log = LogFactory
            .getLog(ClassificationExclusivityRule.class);

    @Condition
    public boolean category(Category c) {
        List<CategoryImageLink> images = c.collectImageLinks(null);
        List<CategoryGroupCategoryLink> cgs = c.collectCategoryGroupLinks(null);

        // TODO

        return false; // TODO images.contains(i);
    }

    @Consequence
    public void die() {
        throw new RuntimeException(
                "Having the same image in TWO categories of the same category group is not allowed.");
    }
}