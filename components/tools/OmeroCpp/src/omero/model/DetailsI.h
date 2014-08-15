/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_MODEL_DETAILSI_H
#define OMERO_MODEL_DETAILSI_H

#include <omero/client.h>
#include <omero/model/Details.h>
#include <omero/model/ExperimenterI.h>
#include <omero/model/ExperimenterGroupI.h>
#include <omero/model/EventI.h>
#include <omero/model/ExternalInfoI.h>
#include <omero/model/PermissionsI.h>
#include <Ice/Ice.h>
#include <IceUtil/Config.h>
#include <Ice/Handle.h>
#include <iostream>
#include <string>
#include <vector>

#ifndef OMERO_API
#   ifdef OMERO_API_EXPORTS
#       define OMERO_API ICE_DECLSPEC_EXPORT
#   else
#       define OMERO_API ICE_DECLSPEC_IMPORT
#   endif
#endif

namespace omero {
    namespace model {
	class DetailsI;
    }
}

namespace IceInternal {
  OMERO_API ::Ice::Object* upCast(::omero::model::DetailsI*);
}

namespace omero {

    namespace model {

	/*
	 * Simple implementation of the Details.ice
	 * type embedded in every OMERO.blitz type.
	 */

	typedef IceInternal::Handle<DetailsI> DetailsIPtr;

	class OMERO_API DetailsI : virtual public Details {

	protected:
	    virtual ~DetailsI(); // protected as outlined in Ice docs.
        
            // This must be stored as a raw pointer to prevent circular ref with client
            const omero::client* client;
            /*const*/ omero::api::ServiceFactoryPrx session;
	public:

          DetailsI(const omero::client* client = NULL);

          const omero::client* getClient() const;

          const omero::api::ServiceFactoryPrx getSession() const;

          /*const*/ omero::sys::EventContextPtr getEventContext() const;

          /*const*/ std::map<std::string, std::string> getCallContext() const;

          virtual omero::model::ExperimenterPtr getOwner(const Ice::Current& current = Ice::Current());

	  virtual void setOwner(const omero::model::ExperimenterPtr& _owner, const Ice::Current& current = Ice::Current());

	  virtual omero::model::ExperimenterGroupPtr getGroup(const Ice::Current& current = Ice::Current());

	  virtual void setGroup(const omero::model::ExperimenterGroupPtr& _group, const Ice::Current& current = Ice::Current());

	  virtual omero::model::EventPtr getCreationEvent(const Ice::Current& current = Ice::Current());

	  virtual void setCreationEvent(const omero::model::EventPtr& _creationEvent, const Ice::Current& current = Ice::Current());

	  virtual omero::model::EventPtr getUpdateEvent(const Ice::Current& current = Ice::Current());

	  virtual void setUpdateEvent(const omero::model::EventPtr& _updateEvent, const Ice::Current& current = Ice::Current());

	  virtual omero::model::PermissionsPtr getPermissions(const Ice::Current& current = Ice::Current());

	  virtual void setPermissions(const omero::model::PermissionsPtr& _permissions, const Ice::Current& current = Ice::Current());

	  virtual omero::model::ExternalInfoPtr getExternalInfo(const Ice::Current& current = Ice::Current());

	  virtual void setExternalInfo(const omero::model::ExternalInfoPtr& _externalInfo, const Ice::Current& current = Ice::Current());

	};

  }
}
#endif // OMERO_MODEL_DETAILSI_H
