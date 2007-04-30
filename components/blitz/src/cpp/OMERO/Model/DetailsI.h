/*
 *   $Id$
 * 
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 * 
 */

#include <OMERO/ModelI.h>
#include <Ice/Config.h>
#include <iostream>
#include <string>
#include <vector>

#ifndef DETAILSI_H
#define DETAILSI_H

namespace omero { namespace model {

class DetailsI : public Details { 

protected:
    ~DetailsI(); // protected as outlined in docs.

public:

   /**
    * Default no-args constructor which manages the proper "loaded"
    * status of all {@link Collection}s by manually initializing them all
    * to an empty {@link Collection} of the approrpriate type.
    */
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
 }
}
#endif // DETAILSI_H
 
