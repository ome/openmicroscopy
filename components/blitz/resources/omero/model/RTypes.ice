/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_MODEL_RTYPES_ICE
#define OMERO_MODEL_RTYPES_ICE

#include <omero/model/IObject.ice>

//
// Simple type definitions used for remoting purposes.
// See README.ice for a description of the omero module.
//
module omero {

  /**
   * Wrapper for an {@link omero.model.IObject} instance.
   **/
  ["protected"] class RObject extends RType
  {
    omero::model::IObject val;
    // Here we don't want the pointer being altered
    omero::model::IObject getValue();
  };

};

#endif // OMERO_MODEL_RTYPES_ICE
