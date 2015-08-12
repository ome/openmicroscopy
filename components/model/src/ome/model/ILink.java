/*
 * ome.model.ILink
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.model;

/**
 * extension of {@link ome.model.IObject} for building object hierarchies. ILink
 * represents a many-to-many relationship between two classes that take part in
 * a containment relationship.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0
 * @since 3.0
 * @see ome.model.core.Image
 * @see ome.model.containers.Dataset
 * @see ome.model.containers.Project
 */
public interface ILink extends IObject {

    public IObject getParent();

    public void setParent(IObject parent);

    public IObject getChild();

    public void setChild(IObject child);

}
