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
#include <Ice/Config.h>
#include <iostream>
#include <string>
#include <vector>

namespace omero { namespace model {

/*
 * Simple implementation of the Details.ice
 * type embedded in every OMERO.blitz type.
 */
class DetailsI : virtual public Details { 

protected:
    ~DetailsI(); // protected as outlined in Ice docs.

public:

    DetailsI();
 
    omero::model::ExperimenterPtr getOwner() {
        return  owner ;
    }
    
    void setOwner(omero::model::ExperimenterPtr _owner) {
        owner =  _owner ;
         
    }
 
    omero::model::ExperimenterGroupPtr getGroup() {
        return  group ;
    }
    
    void setGroup(omero::model::ExperimenterGroupPtr _group) {
        group =  _group ;
         
    }
 
    omero::model::EventPtr getCreationEvent() {
        return  creationEvent ;
    }
    
    void setCreationEvent(omero::model::EventPtr _creationEvent) {
        creationEvent =  _creationEvent ;
         
    }
 
    omero::model::EventPtr getUpdateEvent() {
        return  updateEvent ;
    }
    
    void setUpdateEvent(omero::model::EventPtr _updateEvent) {
        updateEvent =  _updateEvent ;
         
    }
 
    omero::model::PermissionsPtr getPermissions() {
        return  permissions ;
    }
    
    void setPermissions(omero::model::PermissionsPtr _permissions) {
        permissions =  _permissions ;
         
    }
 
    omero::model::ExternalInfoPtr getExternalInfo() {
        return  externalInfo ;
    }
    
    void setExternalInfo(omero::model::ExternalInfoPtr _externalInfo) {
        externalInfo =  _externalInfo ;
         
    }
 
  };

  typedef IceUtil::Handle<DetailsI> DetailsIPtr;

 }
}
#endif // OMERO_MODEL_DETAILSI_H
 
