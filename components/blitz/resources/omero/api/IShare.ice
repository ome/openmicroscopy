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
         * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IShare.html">IShare.html<a/>
         **/
        ["ami", "amd"] interface IShare extends ServiceInterface
            {
                void activate(long shareId) throws ServerError;
                void deactivate() throws ServerError;
                omero::model::Share getShare(long shareId) throws ServerError;
                omero::sys::CountMap getMemberCount(omero::sys::LongList shareIds) throws ServerError;
                SessionList getOwnShares(bool active) throws ServerError;
                SessionList getMemberShares(bool active) throws ServerError;
                SessionList getSharesOwnedBy(omero::model::Experimenter user, bool active) throws ServerError;
                SessionList getMemberSharesFor(omero::model::Experimenter user, bool active) throws ServerError;
                IObjectList getContents(long shareId) throws ServerError;
                IObjectList getContentSubList(long shareId, int start, int finish) throws ServerError;
                int getContentSize(long shareId) throws ServerError;
                IdListMap getContentMap(long shareId) throws ServerError;

                long createShare(string description,
                                 omero::RTime expiration,
                                 IObjectList items,
                                 ExperimenterList exps,
                                 StringSet guests,
                                 bool enabled) throws ServerError;
                void setDescription(long shareId, string description) throws ServerError;
                void setExpiration(long shareId, omero::RTime expiration) throws ServerError;
                void setActive(long shareId, bool active) throws ServerError;
                void closeShare(long shareId) throws ServerError;

                void addObjects(long shareId, IObjectList iobjects) throws ServerError;
                void addObject(long shareId, omero::model::IObject iobject) throws ServerError;
                void removeObjects(long shareId, IObjectList iobjects) throws ServerError;
                void removeObject(long shareId, omero::model::IObject iobject) throws ServerError;

                omero::sys::CountMap getCommentCount(omero::sys::LongList shareIds) throws ServerError;
                AnnotationList getComments(long shareId) throws ServerError;
                omero::model::TextAnnotation addComment(long shareId, string comment) throws ServerError;
                omero::model::TextAnnotation addReply(long shareId,
                                                      string comment,
                                                      omero::model::TextAnnotation replyTo) throws ServerError;
                void deleteComment(omero::model::Annotation comment) throws ServerError;

                ExperimenterList getAllMembers(long shareId) throws ServerError;
                StringSet getAllGuests(long shareId) throws ServerError;
                StringSet getAllUsers(long shareId) throws ValidationException, ServerError;
                void addUsers(long shareId, ExperimenterList exps) throws ServerError;
                void addGuests(long shareId, StringSet emailAddresses) throws ServerError;
                void removeUsers(long shareId, ExperimenterList exps) throws ServerError;
                void removeGuests(long shareId, StringSet emailAddresses) throws ServerError;
                void addUser(long shareId, omero::model::Experimenter exp) throws ServerError;
                void addGuest(long shareId, string emailAddress) throws ServerError;
                void removeUser(long shareId, omero::model::Experimenter exp) throws ServerError;
                void removeGuest(long shareId, string emailAddress) throws ServerError;

                // Under construction
                UserMap getActiveConnections(long shareId) throws ServerError;
                UserMap getPastConnections(long shareId) throws ServerError;
                void invalidateConnection(long shareId, omero::model::Experimenter exp) throws ServerError;
                IObjectList getEvents(long shareId, omero::model::Experimenter exp, omero::RTime from, omero::RTime to) throws ServerError;
            };

    };
};

#endif
