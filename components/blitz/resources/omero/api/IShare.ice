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

module omero {

    module api {

        /**
         * See <a href="http://downloads.openmicroscopy.org/latest/omero5.1/api/ome/api/IShare.html">IShare.html</a>
         **/
        ["ami", "amd"] interface IShare extends ServiceInterface
            {
                idempotent void activate(long shareId) throws ServerError;
                idempotent void deactivate() throws ServerError;
                idempotent omero::model::Share getShare(long shareId) throws ServerError;
                idempotent omero::sys::CountMap getMemberCount(omero::sys::LongList shareIds) throws ServerError;
                idempotent SessionList getOwnShares(bool active) throws ServerError;
                idempotent SessionList getMemberShares(bool active) throws ServerError;
                idempotent SessionList getSharesOwnedBy(omero::model::Experimenter user, bool active) throws ServerError;
                idempotent SessionList getMemberSharesFor(omero::model::Experimenter user, bool active) throws ServerError;
                idempotent IObjectList getContents(long shareId) throws ServerError;
                idempotent IObjectList getContentSubList(long shareId, int start, int finish) throws ServerError;
                idempotent int getContentSize(long shareId) throws ServerError;
                idempotent IdListMap getContentMap(long shareId) throws ServerError;

                long createShare(string description,
                                 omero::RTime expiration,
                                 IObjectList items,
                                 ExperimenterList exps,
                                 StringSet guests,
                                 bool enabled) throws ServerError;
                idempotent void setDescription(long shareId, string description) throws ServerError;
                idempotent void setExpiration(long shareId, omero::RTime expiration) throws ServerError;
                idempotent void setActive(long shareId, bool active) throws ServerError;
                void closeShare(long shareId) throws ServerError;

                void addObjects(long shareId, IObjectList iobjects) throws ServerError;
                void addObject(long shareId, omero::model::IObject iobject) throws ServerError;
                void removeObjects(long shareId, IObjectList iobjects) throws ServerError;
                void removeObject(long shareId, omero::model::IObject iobject) throws ServerError;

                idempotent omero::sys::CountMap getCommentCount(omero::sys::LongList shareIds) throws ServerError;
                idempotent AnnotationList getComments(long shareId) throws ServerError;
                omero::model::TextAnnotation addComment(long shareId, string comment) throws ServerError;
                omero::model::TextAnnotation addReply(long shareId,
                                                      string comment,
                                                      omero::model::TextAnnotation replyTo) throws ServerError;
                void deleteComment(omero::model::Annotation comment) throws ServerError;

                idempotent ExperimenterList getAllMembers(long shareId) throws ServerError;
                idempotent StringSet getAllGuests(long shareId) throws ServerError;
                idempotent StringSet getAllUsers(long shareId) throws ValidationException, ServerError;
                void addUsers(long shareId, ExperimenterList exps) throws ServerError;
                void addGuests(long shareId, StringSet emailAddresses) throws ServerError;
                void removeUsers(long shareId, ExperimenterList exps) throws ServerError;
                void removeGuests(long shareId, StringSet emailAddresses) throws ServerError;
                void addUser(long shareId, omero::model::Experimenter exp) throws ServerError;
                void addGuest(long shareId, string emailAddress) throws ServerError;
                void removeUser(long shareId, omero::model::Experimenter exp) throws ServerError;
                void removeGuest(long shareId, string emailAddress) throws ServerError;

                // Under construction
                idempotent UserMap getActiveConnections(long shareId) throws ServerError;
                idempotent UserMap getPastConnections(long shareId) throws ServerError;
                void invalidateConnection(long shareId, omero::model::Experimenter exp) throws ServerError;
                idempotent IObjectList getEvents(long shareId, omero::model::Experimenter exp, omero::RTime from, omero::RTime to) throws ServerError;
                void notifyMembersOfShare(long shareId, string subject, string message, bool html) throws ServerError;
            };

    };
};

#endif
