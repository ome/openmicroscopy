/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef CLASS_DETAILS
#define CLASS_DETAILS

#include <omero/ModelF.ice>
#include <omero/System.ice>
#include <Ice/Current.ice>

module omero {

  module model {

    /**
     * Embedded component of every OMERO.blitz type. Since this is
     * not an IObject subclass, no attempt is made to hide the state
     * of this object, since it cannot be "unloaded".
     **/
    ["protected"] class Details
    {

      //  ome.model.meta.Experimenter owner;
      omero::model::Experimenter owner;
      omero::model::Experimenter getOwner();
      void setOwner(omero::model::Experimenter theOwner);

      //  ome.model.meta.ExperimenterGroup group;
      omero::model::ExperimenterGroup group;
      omero::model::ExperimenterGroup getGroup();
      void setGroup(omero::model::ExperimenterGroup theGroup);

      //  ome.model.meta.Event creationEvent;
      omero::model::Event creationEvent;
      omero::model::Event getCreationEvent();
      void setCreationEvent(omero::model::Event theCreationEvent);

      //  ome.model.meta.Event updateEvent;
      omero::model::Event updateEvent;
      omero::model::Event getUpdateEvent();
      void setUpdateEvent(omero::model::Event theUpdateEvent);

      //  ome.model.internal.Permissions permissions;
      omero::model::Permissions permissions;
      omero::model::Permissions getPermissions();
      void setPermissions(omero::model::Permissions thePermissions);

      //  ome.model.meta.ExternalInfo externalInfo;
      omero::model::ExternalInfo externalInfo;
      omero::model::ExternalInfo getExternalInfo();
      void setExternalInfo(omero::model::ExternalInfo theExternalInfo);

      //
      // Context parameters
      //

      /**
       * Context which was active during the call which
       * returned this object. This context is set as
       * the last (optional) argument of any remote
       * Ice invocation. This is used to change the
       * user, group, share, etc. of the current session.
       **/
      Ice::Context call;

      /**
       * Context which would have been returned by a
       * simultaneous call to {@link omero.api.IAdmin#getEventContext}
       * while this object was being loaded.
       **/
      omero::sys::EventContext event;

    };

  };

};
#endif
