/*
 * ome.util.ReverseModelMapper
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

// Java imports
import java.util.IdentityHashMap;
import java.util.Map;

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Application-internal dependencies
import ome.api.ModelBased;
import ome.model.IObject;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public class ReverseModelMapper { // extends ContextFilter {

    protected static Log log = LogFactory.getLog(ModelMapper.class);

    protected Map target2model = new IdentityHashMap();

    public IObject map(ModelBased source) {

        if (source == null) {

            return null;

        } else if (target2model.containsKey(source)) {

            return (IObject) target2model.get(source);

        } else {

            IObject model = source.newIObject();
            target2model.put(source, model);
            source.fillIObject(model, this);
            return model;

        }
    }

}