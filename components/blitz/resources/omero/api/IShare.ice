/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_ISHARE_ICE
#define OMERO_API_ISHARE_ICE

#include <omero/ModelF.ice>
#include <omero/ServicesF.ice>
#include <omero/Collections.ice>
["deprecate:IShare is deprecated."]
module omero {

    module api {

        /**
         * Provides method for sharing - collaboration process for images,
         * datasets, projects.
         **/
        ["ami", "amd"] interface IShare extends ServiceInterface
            {
                /**
                 * Turns on the access control lists attached to the given
                 * share for the current session. Warning: this will slow down
                 * the execution of the current session for all database
                 * reads. Writing to the database will not be allowed. If
                 * share does not exist or is not accessible (non-members) or
                 * is disabled, then an {@link ValidationException} is thrown.
                 */
                idempotent void activate(long shareId) throws ServerError;

                /**
                 * Turns off the access control lists with the current share.
                 */
                idempotent void deactivate() throws ServerError;

                /**
                 * Gets a share as a {@link omero.model.Session} with all
                 * related: {@link omero.model.Annotation} comments,
                 * {@link omero.model.Experimenter} members, fully loaded.
                 * Unlike the other methods on this interface, if the
                 * sessionId is unknown, does not throw a
                 * {@link omero.ValidationException}.
                 *
                 * @return a {@link omero.model.Session} with id and
                 *         {@link omero.model.Details} set or null.
                 *         The owner in the Details object is the true owner,
                 *         and the group in the Details has all member users
                 *         linked. {@link omero.model.Annotation} instances
                 *         of the share are linked to the
                 *         {@link omero.model.Session}. Missing is a list of
                 *         share guests.
                 */
                idempotent omero::model::Share getShare(long shareId) throws ServerError;

                /**
                 * Returns a map from share id to the count of total members
                 * (including the owner). This is represented by
                 * {@link omero.model.ShareMember} links.
                 *
                 * @param shareIds Not null.
                 * @return Map with all ids present.
                 * @throws ValidationException if a given share does not exist
                 */
                idempotent omero::sys::CountMap getMemberCount(omero::sys::LongList shareIds) throws ServerError;

                /**
                 * Gets all owned shares for the current
                 * {@link omero.model.Experimenter}.
                 *
                 * @param onlyActive
                 *            if true, then only shares which can be used for
                 *            login will be returned. All <i>draft</i> shares
                 *            (see {@link #createShare}) and closed shares (see
                 *            {@link #closeShare}) will be filtered.
                 * @return set of shares. Never null. May be empty.
                 */
                idempotent SessionList getOwnShares(bool active) throws ServerError;

                /**
                 * Gets all shares where current
                 * {@link omero.model.Experimenter} is a member.
                 *
                 * @param onlyActive
                 *            if true, then only shares which can be used for
                 *            login will be returned. All <i>draft</i> shares
                 *            (see {@link #createShare}) and closed shares (see
                 *            {@link #closeShare}) will be filtered.
                 * @return set of shares. Never null. May be empty.
                 */
                idempotent SessionList getMemberShares(bool active) throws ServerError;

                /**
                 * Gets all shares owned by the given
                 * {@link omero.model.Experimenter}.
                 *
                 * @param onlyActive
                 *            if true, then only shares which can be used for
                 *            login will be returned. All <i>draft</i> shares
                 *            (see {@link #createShare}) and closed shares (see
                 *            {@link #closeShare}) will be filtered.
                 * @return set of shares. Never null. May be empty.
                 */
                idempotent SessionList getSharesOwnedBy(omero::model::Experimenter user, bool active) throws ServerError;

                /**
                 * Gets all shares where given
                 * {@link omero.model.Experimenter} is a member.
                 *
                 * @param onlyActive
                 *            if true, then only shares which can be used for
                 *            login will be returned. All <i>draft</i> shares
                 *            (see {@link #createShare}) and closed shares (see
                 *            {@link #closeShare}) will be filtered.
                 * @return set of shares. Never null. May be empty.
                 */
                idempotent SessionList getMemberSharesFor(omero::model::Experimenter user, bool active) throws ServerError;

                /**
                 * Looks up all {@link omero.model.IObject} items belong to the
                 * {@link omero.model.Session} share.
                 *
                 * @return list of objects. Not null. Probably not empty.
                 */
                idempotent IObjectList getContents(long shareId) throws ServerError;

                /**
                 * Returns a range of items from the share.
                 *
                 * @see #getContents
                 */
                idempotent IObjectList getContentSubList(long shareId, int start, int finish) throws ServerError;

                /**
                 * Returns the number of items in the share.
                 */
                idempotent int getContentSize(long shareId) throws ServerError;

                /**
                 * Returns the contents of the share keyed by type.
                 */
                idempotent IdListMap getContentMap(long shareId) throws ServerError;

                /**
                 * Creates {@link omero.model.Session} share with all related:
                 * {@link omero.model.IObject} itmes,
                 * {@link omero.model.Experimenter} members, and guests.
                 *
                 * @param enabled
                 *            if true, then the share is immediately available
                 *            for use. If false, then the share is in draft
                 *            state. All methods on this interface will work
                 *            for shares <em>except</em> {@link #activate}.
                 *            Similarly, the share password cannot be used by
                 *            guests to login.
                 */
                long createShare(string description,
                                 omero::RTime expiration,
                                 IObjectList items,
                                 ExperimenterList exps,
                                 StringSet guests,
                                 bool enabled) throws ServerError;
                idempotent void setDescription(long shareId, string description) throws ServerError;
                idempotent void setExpiration(long shareId, omero::RTime expiration) throws ServerError;
                idempotent void setActive(long shareId, bool active) throws ServerError;

                /**
                 * Closes {@link omero.model.Session} share. No further logins
                 * will be possible and all getters (e.g.
                 * {@link #getMemberShares}, {@link #getOwnShares}, ...) will
                 * filter these results if {@code onlyActive} is true.
                 */
                void closeShare(long shareId) throws ServerError;

                /**
                 * Adds new {@link omero.model.IObject} items to
                 * {@link omero.model.Session} share. Conceptually calls
                 * {@link #addObjects} for every argument passed, but the
                 * graphs will be merged.
                 */
                void addObjects(long shareId, IObjectList iobjects) throws ServerError;

                /**
                 * Adds new {@link omero.model.IObject} item to
                 * {@link omero.model.Session} share. The entire object graph
                 * with the exception of all Details will be loaded into the
                 * share. If you would like to load a single object, then pass
                 * an unloaded reference.
                 */
                void addObject(long shareId, omero::model::IObject iobject) throws ServerError;

                /**
                 * Remove existing items from the share.
                 */
                void removeObjects(long shareId, IObjectList iobjects) throws ServerError;

                /**
                 * Removes existing {@link omero.model.IObject} object from the
                 * {@link omero.model.Session} share.
                 */
                void removeObject(long shareId, omero::model::IObject iobject) throws ServerError;

                /**
                 * Returns a map from share id to comment count.
                 *
                 * @param shareIds Not null.
                 * @return Map with all ids present and 0 if no count exists.
                 * @throws ValidationException if a given share does not exist
                 */
                idempotent omero::sys::CountMap getCommentCount(omero::sys::LongList shareIds) throws ServerError;

                /**
                 * Looks up all {@link omero.model.Annotation} comments which
                 * belong to the {@link omero.model.Session} share.
                 *
                 * @return list of Annotation
                 */
                idempotent AnnotationList getComments(long shareId) throws ServerError;

                /**
                 * Creates {@link omero.model.TextAnnotation} comment for
                 * {@link omero.model.Session} share.
                 */
                omero::model::TextAnnotation addComment(long shareId, string comment) throws ServerError;

                /**
                 * Creates {@link omero.model.TextAnnotation} comment which
                 * replies to an existing comment.
                 *
                 * @return the new {@link omero.model.TextAnnotation}
                 */
                omero::model::TextAnnotation addReply(long shareId,
                                                      string comment,
                                                      omero::model::TextAnnotation replyTo) throws ServerError;

                /**
                 * Deletes {@link omero.model.Annotation} comment from the
                 * database.
                 */
                void deleteComment(omero::model::Annotation comment) throws ServerError;

                /**
                 * Get all {@link omero.model.Experimenter} users who are a
                 * member of the share.
                 */
                idempotent ExperimenterList getAllMembers(long shareId) throws ServerError;

                /**
                 * Get the email addresses for all share guests.
                 */
                idempotent StringSet getAllGuests(long shareId) throws ServerError;

                /**
                 * Get a single set containing the
                 * {@link omero.model.Experimenter#getOmeName} login names
                 * of the users as well email addresses for guests.
                 *
                 * @param shareId
                 * @return a {@link StringSet} containing the login of all
                 *         users
                 * @throws ValidationException
                 *         if there is a conflict between email addresses and
                 *         user names.
                 */
                idempotent StringSet getAllUsers(long shareId) throws ValidationException, ServerError;

                /**
                 * Adds {@link omero.model.Experimenter} experimenters to
                 * {@link omero.model.Session} share.
                 */
                void addUsers(long shareId, ExperimenterList exps) throws ServerError;

                /**
                 * Adds guest email addresses to the share.
                 */
                void addGuests(long shareId, StringSet emailAddresses) throws ServerError;

                /**
                 * Removes {@link omero.model.Experimenter} experimenters from
                 * {@link omero.model.Session} share.
                 */
                void removeUsers(long shareId, ExperimenterList exps) throws ServerError;

                /**
                 * Removes guest email addresses from the share.
                 */
                void removeGuests(long shareId, StringSet emailAddresses) throws ServerError;

                /**
                 * Adds {@link omero.model.Experimenter} experimenter to
                 * {@link omero.model.Session} share.
                 */
                void addUser(long shareId, omero::model::Experimenter exp) throws ServerError;

                /**
                 * Adds guest email address to the share.
                 */
                void addGuest(long shareId, string emailAddress) throws ServerError;

                /**
                 * Removes {@link omero.model.Experimenter} experimenter from
                 * {@link omero.model.Session} share.
                 */
                void removeUser(long shareId, omero::model::Experimenter exp) throws ServerError;

                /**
                 * Removes guest email address from share.
                 */
                void removeGuest(long shareId, string emailAddress) throws ServerError;

                // Under construction
                /**
                 * Gets actual active connections to
                 * {@link omero.model.Session} share.
                 *
                 * @return map of experimenter and IP address
                 */
                idempotent UserMap getActiveConnections(long shareId) throws ServerError;

                /**
                 * Gets previous connections to
                 * {@link omero.model.Session} share.
                 *
                 * @return map of experimenter and IP address
                 */
                idempotent UserMap getPastConnections(long shareId) throws ServerError;

                /**
                 * Makes the connection invalid for
                 * {@link omero.model.Session} share for specified user.
                 */
                void invalidateConnection(long shareId, omero::model::Experimenter exp) throws ServerError;

                /**
                 * Gets events for {@link omero.model.Session} share per
                 * {@link omero.model.Experimenter} experimenter for period of
                 * time.
                 * @return List of events
                 */
                idempotent IObjectList getEvents(long shareId, omero::model::Experimenter exp, omero::RTime from, omero::RTime to) throws ServerError;

                /**
                 * Notifies via email selected members of share.
                 */
                void notifyMembersOfShare(long shareId, string subject, string message, bool html) throws ServerError;
            };

    };
};

#endif
