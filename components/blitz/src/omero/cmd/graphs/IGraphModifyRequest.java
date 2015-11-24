/*
 * Copyright 2013 Glencoe Software, Inc. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package omero.cmd.graphs;

import omero.cmd.IRequest;

/**
 * Extension of {@link IRequest} to allow copying {@link omero.cmd.GraphModify} objects
 * during pre-processing.
 * 
 * @since 5.0.0
 * @deprecated will be removed in OMERO 5.3, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.2/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public interface IGraphModifyRequest extends IRequest {

    /**
     * Produce a clone of this object. This may require loading an object
     * from the Spring context, and likely requires copying all serialized state.
     * In other words, the return value from this method should be equivalent
     * to what Ice will return post-deserialization.
     */
    IGraphModifyRequest copy();

}
