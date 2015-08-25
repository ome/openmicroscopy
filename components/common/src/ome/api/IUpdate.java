/*
 * ome.api.IUpdate
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

import java.util.Collection;
import java.util.List;

import ome.annotations.Validate;
import ome.conditions.ValidationException;
import ome.model.IObject;

/**
 * Provides methods for directly updating object graphs. IUpdate is the lowest
 * level (level-1) interface which may make changes (INSERT, UPDATE, DELETE) to
 * the database. All other methods of changing the database may leave it in an
 * inconsistent state.
 * 
 * <p>
 * All the save* methods act recursively on the entire object graph, replacing
 * placeholders and details where necessary, and then "merging" the final graph.
 * This means that the objects that are passed into IUpdate.save* methods are
 * copied over to new instances which are then returned. The original objects
 * <b>should be discarded</b>.
 * </p>
 * 
 * <p>{@link #saveAndReturnIds(IObject[])} behaves slightly differently in that
 * it does <em>not</em> handle object modifications. The graph of objects
 * passed in can consist <em>ONLY</em> if either newly created objects without
 * ids or of unloaded objects with ids. <em>Note:</em> The ids of the saved values
 * may not be in order. This is caused by persistence-by-transitivity. Hibernate
 * may detect an item later in the array if they are interconnected and therefore
 * choose to save it first.
 * </p>
 * 
 * <p>
 * All methods throw {@link ome.conditions.ValidationException} if the input
 * objects do not pass validation, and
 * {@link ome.conditions.OptimisticLockException} if the version of a given has
 * already been incremented.
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 3.0
 * @since OMERO3.0
 * @see ome.util.Validation
 * @see "ome.logic.UpdateImpl"
 * @see ome.model.internal.Details
 */
public interface IUpdate extends ServiceInterface {

    /** Logic differs from other methods. See class description
     * @see ome.api.IUpdate */
    List<Long> saveAndReturnIds(IObject[] objects);
    
    /** @see ome.api.IUpdate */
    void saveCollection(@Validate(IObject.class)
    Collection<IObject> graph);

    /** @see ome.api.IUpdate */
    void saveObject(IObject graph);

    /** @see ome.api.IUpdate */
    void saveArray(IObject[] graph);

    /** @see ome.api.IUpdate */
    <T extends IObject> T saveAndReturnObject(T graph);

    /** @see ome.api.IUpdate */
    IObject[] saveAndReturnArray(IObject[] graph);

    /**
     * Deletes a single entity. Unlike the other IUpdate methods, deleteObject
     * does not propagate to related entities (e.g. foreign key relationships)
     * and so calls to deleteObject must be properly ordered.
     *
     * For example, if you would like to delete a FileAnnotation along with
     * the linked OriginalFile, it is necessary to first call
     * deleteObject(OriginalFile) and then deleteObject(FileAnnotation).
     *
     * Instead, you may look to use the more advanced method provided in
     * {@link ome.api.IDelete} which provide support for deleting entire
     * graphs of objects in the correct order.
     *
     * @param row
     *            a persistent {@link IObject} to be deleted.
     * @throws ValidationException
     *             if the row is locked, has foreign key constraints, or is
     *             otherwise marked un-deletable.
     */
    void deleteObject(IObject row) throws ValidationException;

    /**
     * Initiates full-text indexing for the given object. This may have to wait
     * for the current {@code ome.services.fulltext.FullTextThread} to finish.
     * Can only be executed by an admin. Other users must wait for the
     * background {@link Thread} to complete.
     * 
     * @param row
     *            a persistent {@link IObject} to be deleted
     * @throws ValidationException
     *             if the object does not exist or is nul
     */
    void indexObject(IObject row) throws ValidationException;
}
