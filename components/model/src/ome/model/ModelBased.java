/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.model;

import ome.model.IObject;
import ome.util.ModelMapper;
import ome.util.ReverseModelMapper;

/**
 * used for reverse mapping. Model Mapping is done by a single class
 * (subclassing {@link ome.util.ModelMapper}) which contains knowledge of what
 * {@link ome.model.IObject} classes become what target classes. This is done
 * because our domain objects cannot know about other types.
 * <p>
 * In reverse mapping, however, the burden of mapping can be placed on the
 * foreign (target) objects, which simplifies the process.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 2.5
 * @since 2.5
 * @see ome.util.ModelMapper
 * @see ome.util.ReverseModelMapper
 */
public interface ModelBased {
    
    public void copyObject(ome.util.Filterable model, ModelMapper mapper);
    public ome.util.Filterable fillObject(ReverseModelMapper mapper);
}
