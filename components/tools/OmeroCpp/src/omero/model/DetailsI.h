/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_MODEL_DETAILSI_H
#define OMERO_MODEL_DETAILSI_H

#include <omero/model/Details.h>
#include <omero/model/ExperimenterI.h>
#include <omero/model/ExperimenterGroupI.h>
#include <omero/model/EventI.h>
#include <omero/model/ExternalInfoI.h>
#include <omero/model/PermissionsI.h>
#include <Ice/Ice.h>
#include <iostream>
#include <string>
#include <vector>

namespace omero {

    namespace model {

	/*
	 * Simple implementation of the Details.ice
	 * type embedded in every OMERO.blitz type.
	 */
	class DetailsI : virtual public Details {

	protected:
	    virtual ~DetailsI(); // protected as outlined in Ice docs.

	public:
	    
	    DetailsI();

	    virtual omero::model::ExperimenterPtr getOwner(const Ice::Current& current = Ice::Current()) {
		return owner ;
	    }

	  virtual void setOwner(const omero::model::ExperimenterPtr& _owner, const Ice::Current& current = Ice::Current()) {
		owner = _owner ;

	  }

	  virtual omero::model::ExperimenterGroupPtr getGroup(const Ice::Current& current = Ice::Current()) {
		return group ;
	  }

	  virtual void setGroup(const omero::model::ExperimenterGroupPtr& _group, const Ice::Current& current = Ice::Current()) {
		group = _group ;

	  }

	  virtual omero::model::EventPtr getCreationEvent(const Ice::Current& current = Ice::Current()) {
		return creationEvent ;
	  }

	  virtual void setCreationEvent(const omero::model::EventPtr& _creationEvent, const Ice::Current& current = Ice::Current()) {
		creationEvent = _creationEvent ;

	  }

	  virtual omero::model::EventPtr getUpdateEvent(const Ice::Current& current = Ice::Current()) {
		return updateEvent ;
	  }

	  virtual void setUpdateEvent(const omero::model::EventPtr& _updateEvent, const Ice::Current& current = Ice::Current()) {
		updateEvent = _updateEvent ;

	  }

	  virtual omero::model::PermissionsPtr getPermissions(const Ice::Current& current = Ice::Current()) {
		return permissions ;
	  }

	  virtual void setPermissions(const omero::model::PermissionsPtr& _permissions, const Ice::Current& current = Ice::Current()) {
		permissions = _permissions ;

	  }

	  virtual omero::model::ExternalInfoPtr getExternalInfo(const Ice::Current& current = Ice::Current()) {
		return externalInfo ;
	  }

	  virtual void setExternalInfo(const omero::model::ExternalInfoPtr& _externalInfo, const Ice::Current& current = Ice::Current()) {
		externalInfo = _externalInfo ;

	  }

	};

	typedef IceUtil::Handle<DetailsI> DetailsIPtr;

  }
}
#endif // OMERO_MODEL_DETAILSI_H
