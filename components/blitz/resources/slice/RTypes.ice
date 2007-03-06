/*
 *   $Id$
 * 
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_RTYPES_ICE
#define OMERO_RTYPES_ICE

#include <omero.ice>

module omero { 

  /*
   * Not nullable like the other RTypes.
   */
  class Time
  {
    long val;
  };

  class RType
  {
    bool null;
  };
  
  /*
   * Usage:
   *   omero::RBool b = ...; // from service
   *   if (!b.null && b.val) { ... }
   */
  class RBool extends RType
  {
    bool val;
  };
  
  
  /*
   * Usage:
   *   omero::RDouble d = ...; // from service
   *   if (!d.null && d.val < 0.0) { ... }
   */
  class RDouble extends RType
  {
    double val;
  };
  
  
  /*
   * Usage:
   *   omero::RFloat f = ...; // from service
   *   if (!f.null && d.val < 0.0) { ... }
   */
  class RFloat extends RType
  {
    float val;
  };
  
  
  /*
   * Usage:
   *   omero::RInt i = ...; // from service
   *   if (!i.null && i.val==0) { ... }
   */
  class RInt extends RType
  {
    int  val;
  };
  
  
  /*
   * Usage:
   *   omero::RLong l = ...; // from service
   *   if (!l.null && l.val==0) { ... }
   */
  class RLong extends RType
  {
    long val;
  };
  
  
  /*
   * Usage:
   *   omero::RString s = ...; // from service
   *   if (!s.null && s.val.equals("foo")) { ... }
   */
  class RString extends RType
  {
    string val;
  };
  
  /*
   * Usage:
   *   omero::RTime t = ...; // from service
   *   if (!t.null && t.val < System.currentMillis()) { ... }
   */
  class RTime extends RType
  {
    Time val;
  };

  /*
   * Usage:
   *   omero::RObject o = ...; // from service
   *   if (!o.null && o.val.isLoaded()) { ... }
   */
  class RObject extends RType
  {
    omero::model::IObject val;
  };


};

#endif // OMERO_RTYPES_ICE
    