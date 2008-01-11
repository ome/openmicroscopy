/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

import java.util.List;

import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.model.containers.Dataset;
import ome.model.core.Image;

/**
 * Provides simplifed methods for deleting instances from the database. Various
 * policies may exist for delete and so it is important to consult the
 * implementation documentation to know what types will be deleted by force
 * (using admin privileges) or alternatively throw an exception due to
 * constraint violations.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public interface IDelete extends ServiceInterface {

    /**
     * Returns all entities which would prevent the given {@link Image} id from
     * being deleted. The force boolean determines whether or not the user's
     * other collections should also be removed in order to facilitate the
     * delete.
     *
     * See the documentation of the specific implementation for more
     * information.
     *
     * @param id
     *            of the {@link Image} to be deleted.
     * @param force
     *            return value will be included
     * @return unloaded entity list
     */
    public List<IObject> checkImageDelete(long id, boolean force);

    /**
     * Returns all entities which would be deleted by a call to
     * {@link #deleteImage(long, boolean)}.
     *
     * See the documentation of the specific implementation for more
     * information.
     *
     * @param id
     * @param force
     * @return unloaded entity list
     */
    public List<IObject> verifyImageDelete(long id, boolean force);

    /**
     * Deletes an {@link Image} and all related (subordinate) metadata as
     * defined by the implementation. This method calls
     * {@link #checkImageDelete(long, boolean)} and throws a
     * {@link ConstraintViolation} exception with the results if they are not
     * empty; then it forcibly deletes all objects returned by
     * {@link #verifyImageDelete(long, boolean)}
     *
     * @param id
     *            id of the {@link Image} to be deleted
     * @param force
     *            {@link Image} will be removed even if contained in other
     *            {@link Dataset datasets}. If false, a
     *            {@link ConstraintViolation} will be raised.
     * @throws ValidationException
     *             throws an exception if there is any unexpected object which
     *             prevents this object from being deleted, i.e. not in
     *             {@link #checkImageDelete(long, boolean)} (check first)
     * @throws ApiUsageException
     *             if the object has constraints. Use
     *             {@link #checkImageDelete(long, boolean)} first in order to
     *             verify that there are no constraints.
     * @throws ValidationException
     *             If the {@link Image} does not belong to the current user.
     */
    public void deleteImage(long id, boolean force) throws SecurityViolation,
            ValidationException, ApiUsageException;

}
