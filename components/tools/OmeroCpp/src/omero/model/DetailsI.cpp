/*
 *   $Id$
 *
 *   Copyright 2007,2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/model/DetailsI.h>

::Ice::Object* IceInternal::upCast(::omero::model::DetailsI* p) { return p; }

namespace omero {

    namespace model {

        DetailsI::DetailsI(const omero::client* client):
          Details(),
          client(client) {
            if (client) {
                session = client->getSession();
            }
        }

        DetailsI::~DetailsI() {}

        const omero::client* DetailsI::getClient() const {
            return client;
        }

        const omero::api::ServiceFactoryPrx DetailsI::getSession() const {
              return session;
        }

        /*const*/ omero::sys::EventContextPtr DetailsI::getEventContext() const {
            return event;
        }

        /*const*/ std::map<std::string, std::string> DetailsI::getCallContext() const {
            return call;
        }

        omero::model::ExperimenterPtr DetailsI::getOwner(const Ice::Current& /*current */) {
            return owner ;
        }

        void DetailsI::setOwner(const omero::model::ExperimenterPtr& _owner, const Ice::Current& /*current */) {
              owner = _owner ;
        }

        omero::model::ExperimenterGroupPtr DetailsI::getGroup(const Ice::Current& /*current */) {
              return group ;
        }

        void DetailsI::setGroup(const omero::model::ExperimenterGroupPtr& _group, const Ice::Current& /*current */) {
              group = _group ;
        }

        omero::model::EventPtr DetailsI::getCreationEvent(const Ice::Current& /*current */) {
              return creationEvent ;
        }

        void DetailsI::setCreationEvent(const omero::model::EventPtr& _creationEvent, const Ice::Current& /*current */) {
              creationEvent = _creationEvent ;
        }

        omero::model::EventPtr DetailsI::getUpdateEvent(const Ice::Current& /*current */) {
              return updateEvent ;
        }

        void DetailsI::setUpdateEvent(const omero::model::EventPtr& _updateEvent, const Ice::Current& /*current */) {
              updateEvent = _updateEvent ;
        }

        omero::model::PermissionsPtr DetailsI::getPermissions(const Ice::Current& /*current */) {
              return permissions ;
        }

        void DetailsI::setPermissions(const omero::model::PermissionsPtr& _permissions, const Ice::Current& /*current */) {
              permissions = _permissions ;
        }

        omero::model::ExternalInfoPtr DetailsI::getExternalInfo(const Ice::Current& /*current */) {
              return externalInfo ;
        }

        void DetailsI::setExternalInfo(const omero::model::ExternalInfoPtr& _externalInfo, const Ice::Current& /*current */) {
              externalInfo = _externalInfo ;
        }

    }
}
