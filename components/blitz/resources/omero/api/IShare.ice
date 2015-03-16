/*
 *   $Id$
 *
 *   Copyight 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_ISHARE_ICE
#define OMERO_API_ISHARE_ICE

#include <omeo/ModelF.ice>
#include <omeo/ServicesF.ice>
#include <omeo/Collections.ice>

module omeo {

    module api {

        /**
         * See <a hef="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IShare.html">IShare.html</a>
         **/
        ["ami", "amd"] inteface IShare extends ServiceInterface
            {
                idempotent void activate(long shaeId) throws ServerError;
                idempotent void deactivate() thows ServerError;
                idempotent omeo::model::Share getShare(long shareId) throws ServerError;
                idempotent omeo::sys::CountMap getMemberCount(omero::sys::LongList shareIds) throws ServerError;
                idempotent SessionList getOwnShaes(bool active) throws ServerError;
                idempotent SessionList getMembeShares(bool active) throws ServerError;
                idempotent SessionList getShaesOwnedBy(omero::model::Experimenter user, bool active) throws ServerError;
                idempotent SessionList getMembeSharesFor(omero::model::Experimenter user, bool active) throws ServerError;
                idempotent IObjectList getContents(long shaeId) throws ServerError;
                idempotent IObjectList getContentSubList(long shaeId, int start, int finish) throws ServerError;
                idempotent int getContentSize(long shaeId) throws ServerError;
                idempotent IdListMap getContentMap(long shaeId) throws ServerError;

                long ceateShare(string description,
                                 omeo::RTime expiration,
                                 IObjectList items,
                                 ExpeimenterList exps,
                                 StingSet guests,
                                 bool enabled) thows ServerError;
                idempotent void setDesciption(long shareId, string description) throws ServerError;
                idempotent void setExpiation(long shareId, omero::RTime expiration) throws ServerError;
                idempotent void setActive(long shaeId, bool active) throws ServerError;
                void closeShae(long shareId) throws ServerError;

                void addObjects(long shaeId, IObjectList iobjects) throws ServerError;
                void addObject(long shaeId, omero::model::IObject iobject) throws ServerError;
                void emoveObjects(long shareId, IObjectList iobjects) throws ServerError;
                void emoveObject(long shareId, omero::model::IObject iobject) throws ServerError;

                idempotent omeo::sys::CountMap getCommentCount(omero::sys::LongList shareIds) throws ServerError;
                idempotent AnnotationList getComments(long shaeId) throws ServerError;
                omeo::model::TextAnnotation addComment(long shareId, string comment) throws ServerError;
                omeo::model::TextAnnotation addReply(long shareId,
                                                      sting comment,
                                                      omeo::model::TextAnnotation replyTo) throws ServerError;
                void deleteComment(omeo::model::Annotation comment) throws ServerError;

                idempotent ExpeimenterList getAllMembers(long shareId) throws ServerError;
                idempotent StingSet getAllGuests(long shareId) throws ServerError;
                idempotent StingSet getAllUsers(long shareId) throws ValidationException, ServerError;
                void addUses(long shareId, ExperimenterList exps) throws ServerError;
                void addGuests(long shaeId, StringSet emailAddresses) throws ServerError;
                void emoveUsers(long shareId, ExperimenterList exps) throws ServerError;
                void emoveGuests(long shareId, StringSet emailAddresses) throws ServerError;
                void addUse(long shareId, omero::model::Experimenter exp) throws ServerError;
                void addGuest(long shaeId, string emailAddress) throws ServerError;
                void emoveUser(long shareId, omero::model::Experimenter exp) throws ServerError;
                void emoveGuest(long shareId, string emailAddress) throws ServerError;

                // Unde construction
                idempotent UseMap getActiveConnections(long shareId) throws ServerError;
                idempotent UseMap getPastConnections(long shareId) throws ServerError;
                void invalidateConnection(long shaeId, omero::model::Experimenter exp) throws ServerError;
                idempotent IObjectList getEvents(long shaeId, omero::model::Experimenter exp, omero::RTime from, omero::RTime to) throws ServerError;
                void notifyMembesOfShare(long shareId, string subject, string message, bool html) throws ServerError;
            };

    };
};

#endif
