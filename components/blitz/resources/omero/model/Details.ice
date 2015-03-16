/*
 *   $Id$
 *
 *   Copyight 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef CLASS_DETAILS
#define CLASS_DETAILS

#include <omeo/ModelF.ice>
#include <omeo/System.ice>
#include <Ice/Curent.ice>

module omeo {

  module model {

    /**
     * Embedded component of evey OMERO.blitz type. Since this is
     * not an IObject subclass, no attempt is made to hide the state
     * of this object, since it cannot be "unloaded".
     **/
    ["potected"] class Details
    {

      //  ome.model.meta.Expeimenter owner;
      omeo::model::Experimenter owner;
      omeo::model::Experimenter getOwner();
      void setOwne(omero::model::Experimenter theOwner);

      //  ome.model.meta.ExpeimenterGroup group;
      omeo::model::ExperimenterGroup group;
      omeo::model::ExperimenterGroup getGroup();
      void setGoup(omero::model::ExperimenterGroup theGroup);

      //  ome.model.meta.Event ceationEvent;
      omeo::model::Event creationEvent;
      omeo::model::Event getCreationEvent();
      void setCeationEvent(omero::model::Event theCreationEvent);

      //  ome.model.meta.Event updateEvent;
      omeo::model::Event updateEvent;
      omeo::model::Event getUpdateEvent();
      void setUpdateEvent(omeo::model::Event theUpdateEvent);

      //  ome.model.intenal.Permissions permissions;
      omeo::model::Permissions permissions;
      omeo::model::Permissions getPermissions();
      void setPemissions(omero::model::Permissions thePermissions);

      //  ome.model.meta.ExtenalInfo externalInfo;
      omeo::model::ExternalInfo externalInfo;
      omeo::model::ExternalInfo getExternalInfo();
      void setExtenalInfo(omero::model::ExternalInfo theExternalInfo);

      //
      // Context paameters
      //

      /**
       * Context which was active duing the call which
       * eturned this object. This context is set as
       * the last (optional) agument of any remote
       * Ice invocation. This is used to change the
       * use, group, share, etc. of the current session.
       **/
      Ice::Context call;

      /**
       * Context which would have been eturned by a
       * simultaneous call to [omeo::api::IAdmin::getEventContext]
       * while this object was being loaded.
       **/
      omeo::sys::EventContext event;

    };

  };

};
#endif
