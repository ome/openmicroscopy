/*
 *   $Id$
 * 
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 * 
 */

#ifndef CLASS_DETAILS
#define CLASS_DETAILS

#include <OMERO/fwd.ice>
#include <OMERO/IObject.ice>

module omero { 

  module model { 

    /*
     * Embedded component of every OMERO.blitz type. 
     */
    class Details
    {

      //  ome.model.meta.Experimenter owner;
      omero::model::Experimenter owner;

      //  ome.model.meta.ExperimenterGroup group;
      omero::model::ExperimenterGroup group;

      //  ome.model.meta.Event creationEvent;
      omero::model::Event creationEvent;

      //  ome.model.meta.Event updateEvent;
      omero::model::Event updateEvent;

      //  ome.model.internal.Permissions permissions;
      omero::model::Permissions permissions;

      //  ome.model.meta.ExternalInfo externalInfo;
      omero::model::ExternalInfo externalInfo;

    };

  }; 

};
#endif 
