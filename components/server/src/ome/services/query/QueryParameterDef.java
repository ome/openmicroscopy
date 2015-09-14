/*
 * ome.services.query.QueryParameterDef
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
import ome.parameters.Parameters;
import ome.parameters.QueryParameter;

/**
 * definition of a slot into which a {@link ome.parameters.QueryParameter} must
 * fit. These are typically defined statically in
 * {@link ome.services.query.Query} subclasses and collected into
 * {@link ome.services.query.Definitions} which get passed to the super
 * {@link ome.services.query.Query#Query(Definitions, Parameters) Query constructor.}
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since OMERO 3.0
 */
public class QueryParameterDef {

    /**
     * name of this parameter. Will be compared to all
     * {@link ome.parameters.QueryParameter query parameters} with an equal
     * {@link QueryParameter#name name}.
     */
    final public String name;

    /**
     * type of this parameter. Will restrict what values can be assigned to
     * {@link QueryParameter#value}
     */
    final public Class type;

    /**
     * whether or not this {@link QueryParameter} can be omitted or its
     * {@link QueryParameter#value value} null.
     */
    final public boolean optional;

    /**
     * main constructor. Provides all three fields, none of which can be null.
     */
    public QueryParameterDef(String name, Class type, boolean optional) {
        if (name == null) {
            throw new ApiUsageException("Name cannot be null.");
        }

        if (type == null) {
            throw new ApiUsageException("Type cannot be null.");
        }

        this.name = name;
        this.type = type;
        this.optional = optional;
    }

    /**
     * validation method called by {@link Query#checkParameters()}. Subclasses
     * should be <em>very</em> careful to call super.errorIfInvalid.
     * 
     * @param parameter
     *            Parameter with a matching name to be validated.
     */
    public void errorIfInvalid(QueryParameter parameter) {
        // If paramter is null, skip the rest
        if (parameter == null) {
            if (!this.optional) {
                throw new ApiUsageException(
                        "Non-optional parameter cannot be null.");
            }

            // If the names don't match, there is a problem.
        } else if (parameter.name == null || !parameter.name.equals(this.name)) {
            throw new ApiUsageException(String.format(
                    "Parameter name does not match: %s != %s ", this.name,
                    parameter.name));

            // If parameter.type is null, skip the rest.
        } else if (parameter.type == null) {
            if (!this.optional) {
                throw new ApiUsageException(
                        "Non-optional parameter type cannot be null.");
            }

            // If value is null, skip the rest
        } else if (parameter.value == null) {
            if (!this.optional) {
                throw new ApiUsageException("Non-optional parameter "
                        + this.name + " may not be null.");
            }

        } else {
            // Fields are non-null, check them.
            if (!this.type.isAssignableFrom(parameter.type)) {
                throw new ApiUsageException(String.format(
                        " Type of parameter %s doesn't match: %s != %s", name,
                        this.type, parameter.type));
            }

            if (!this.optional && Collection.class.isAssignableFrom(this.type)
                    && ((Collection) parameter.value).size() < 1) {
                throw new ApiUsageException(
                        "Non-optional collections may not be empty.");
            }

        }

    }

}

// ~ Simple short-cuts
// =========================================================================

class AlgorithmQueryParameterDef extends QueryParameterDef {
    public AlgorithmQueryParameterDef() {
        super(Parameters.ALGORITHM, String.class, false);
    }
}

class ClassQueryParameterDef extends QueryParameterDef {
    public ClassQueryParameterDef() {
        super(Parameters.CLASS, Class.class, false);
    }
}

class IdsQueryParameterDef extends CollectionQueryParameterDef {
    public IdsQueryParameterDef() {
        super(Parameters.IDS, false, Long.class);
    }
}
