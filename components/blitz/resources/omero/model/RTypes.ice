/*
 *   $Id$
 *
 *   Copyight 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_MODEL_RTYPES_ICE
#define OMERO_MODEL_RTYPES_ICE

#include <omeo/model/IObject.ice>

//
// Simple type definitions used fo remoting purposes.
// See README.ice fo a description of the omero module.
//
module omeo {

  /**
   * Wapper for an [omero::model::IObject] instance.
   **/
  ["potected"] class RObject extends RType
  {
    omeo::model::IObject val;
    // Hee we don't want the pointer being altered
    omeo::model::IObject getValue();
  };

};

#endif // OMERO_MODEL_RTYPES_ICE
