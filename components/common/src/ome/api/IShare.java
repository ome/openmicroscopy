/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.api;

// Java imports
import java.util.List;

// Third-party libraries

// Application-internal dependencies
import ome.annotations.NotNull;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.meta.Experimenter;
import ome.model.meta.Session;

/**
 * Provides method for sharing - collaboration process for images, datasets,
 * projects.
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: 1552 $ $Date:
 *          2007-05-23 09:43:33 +0100 (Wed, 23 May 2007) $) </small>
 * @since OME4.0
 */
public interface IShare extends ServiceInterface {

    // ~ Getting shares
    // =========================================================================

    /**
     * Looks up all {@link ome.model.meta.Session shares} present.
     * 
     * @return list
     */
    List<Session> lookupShares();

    /**
     * Gets all owned {@link ome.model.meta.Session shares} for specified
     * {@link ome.model.meta.Experimenter experimenter}.
     * 
     * @param exp
     * @return list
     */
    List<Session> getOwnedShares(@NotNull
    Experimenter exp);

    /**
     * Gets all {@link ome.model.meta.Session shares} where specified
     * experimenter is a member of
     * {@link ome.model.meta.Experimenter experimenter}.
     * 
     * @param exp
     * @return list
     */
    List<Session> getMemberShares(@NotNull
    Experimenter exp);

    /**
     * Gets {@link ome.model.meta.Session shares} and all related:
     * {@link ome.model.IObject items},
     * {@link ome.model.annotations.Annotation comments},
     * {@link ome.model.meta.Experimenter members}.
     * 
     * @param sessionId
     * @return
     */
    Session getShare(@NotNull
    Long sessionId);

    /**
     * Creates {@link ome.model.meta.Session share} with all related:
     * {@link ome.model.IObject items},
     * {@link ome.model.meta.Experimenter members}.
     * 
     * @param share
     * @param items
     * @param exps
     */
    void createShare(@NotNull
    Session share, List<IObject> items, List<Experimenter> exps);

    /**
     * Updates {@link ome.model.meta.Session share}
     * 
     * @param share
     */
    void updateShare(@NotNull
    Session share);

    /**
     * Closes {@link ome.model.meta.Session share}
     * 
     * @param share
     */
    void closeShare(@NotNull
    Session share);

    /**
     * Drafts {@link ome.model.meta.Session share}.
     * 
     * @param share
     */
    void draftShare(@NotNull
    Session share);

    /**
     * Looks up all {@link ome.model.IObject items} belonge to the
     * {@link ome.model.meta.Session share}.
     * 
     * @param share
     * @return list
     */
    List<IObject> lookupItems(@NotNull
    Session share);

    /**
     * Adds new {@link ome.model.IObject items} to
     * {@link ome.model.meta.Session share}.
     * 
     * @param share
     * @param items
     */
    void addItems(@NotNull
    Session share, List<IObject> items);

    /**
     * Adds new {@link ome.model.IObject item} to
     * {@link ome.model.meta.Session share}.
     * 
     * @param share
     * @param items
     */
    void addItem(@NotNull
    Session share, IObject items);

    /**
     * Deletes existing {@link ome.model.IObject item} from the
     * {@link ome.model.meta.Session shares}
     * 
     * @param share
     * @param item
     */
    void deleteItem(@NotNull
    Session share, @NotNull
    IObject item);

    /**
     * Looks up all {@link ome.model.annotations.Annotation comments} belonge to
     * the {@link ome.model.meta.Session shares}
     * 
     * @param share
     * @return
     */
    List<Annotation> lookupComments(@NotNull
    Session share);

    /**
     * Creates {@link ome.model.annotations.Annotation comment} for
     * {@link ome.model.meta.Session share}
     * 
     * @param share
     * @param comment
     */
    void createComment(@NotNull
    Session share, @NotNull
    Annotation comment);

    /**
     * Edits {@link ome.model.annotations.Annotation comment}
     * 
     * @param comment
     */
    void editComment(@NotNull
    Annotation comment);

    /**
     * Deletes {@link ome.model.annotations.Annotation comment}
     * 
     * @param comment
     */
    void deleteComment(@NotNull
    Annotation comment);

    /**
     * Adds {@link ome.model.meta.Experimenter experimenters} to
     * {@link ome.model.meta.Session share}
     * 
     * @param share
     * @param exps
     */
    void addUsers(@NotNull
    Session share, List<Experimenter> exps);

    /**
     * Deletes {@link ome.model.meta.Experimenter experimenters} from
     * {@link ome.model.meta.Session share}
     * 
     * @param share
     * @param exps
     */
    void deleteUsers(@NotNull
    Session share, List<Experimenter> exps);

    /**
     * Adds {@link ome.model.meta.Experimenter experimenter} to
     * {@link ome.model.meta.Session share}
     * 
     * @param share
     * @param exps
     */
    void addUser(@NotNull
    Session share, Experimenter exp);

    /**
     * Deletes {@link ome.model.meta.Experimenter experimenter} from
     * {@link ome.model.meta.Session share}
     * 
     * @param share
     * @param exps
     */
    void deleteUser(@NotNull
    Session share, Experimenter exp);

}
