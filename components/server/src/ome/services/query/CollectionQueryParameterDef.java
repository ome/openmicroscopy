/*
 * ome.services.query.CollectionQueryParameterDef
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

package ome.services.query;

import java.util.Collection;

import ome.conditions.ApiUsageException;
import ome.parameters.QueryParameter;

/**
 * extension of {@link ome.services.query.QueryParameterDef} which restricts the
 * {@link ome.services.query.QueryParameterDef#type type} to a
 * {@link java.util.Collection}, and specifies the element types of that
 * Collection. Also overrides validation to check that type.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since OMERO 3.0
 */
public class CollectionQueryParameterDef extends QueryParameterDef {

    public Class elementType;

    public CollectionQueryParameterDef(String name, boolean optional,
            Class elementType) {
        super(name, Collection.class, optional);
        this.elementType = elementType;

    }

    @Override
    /**
     * adds Collection-element tests after calling super.errorIfInvalid();
     */
    public void errorIfInvalid(QueryParameter parameter) {
        super.errorIfInvalid(parameter);

        if (!optional && ((Collection) parameter.value).size() < 1) {
            throw new ApiUsageException(
                    "Requried collection parameters may not be empty.");
        }

        if (parameter.value != null) {
            for (Object element : (Collection) parameter.value) {

                if (element == null) {
                    throw new ApiUsageException(
                            "Null elements are not allowed "
                                    + "in parameter collections");
                }

                if (!elementType.isAssignableFrom(element.getClass())) {
                    throw new ApiUsageException("Elements of type "
                            + element.getClass().getName()
                            + " are not allowed in collections of type "
                            + elementType.getName());
                }
            }
        }

    }

}
