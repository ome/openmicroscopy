/*
 *   $Id$
 * 
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_RTYPES_ICE
#define OMERO_RTYPES_ICE

#include <OMERO/fwd.ice>

/*
 * Simple type definitions used for remoting purposes.
 *
 * RType-subclasses permit both the passing of null values to
 * OMERO.blitz, since the Ice protocol maps null values to default
 * (the empty string, 0.0, etc.), and a simple implementation of an
 * "Any" value.
 *
 * Usage (C++):
 *   
 *    omero::RBoolPtr b1 = new omero::RBool(true);
 *    omero::RBoolPtr b2 = someObjPtr->getBool();
 *    if (b2 && b2.val) { ... };
 *    // the first test, checks if the pointer is null
 *
 * Usage (Java):
 *    omero.RBool b1 = new omero.RBool(true);
 *    omero.RBool b2 = someObj.getBool();
 *    if (b2!=null && b2.val) { ... };
 *    // no operator overloading; check for null directly.
 *
 */
module omero { 

  /*
   * Simple base class. Essentially abstract. 
   */
  class RType
  {
  };

  /*
   */
  class RBool extends RType
  {
    bool val;
  };
  
  
  /*
   */
  class RDouble extends RType
  {
    double val;
  };
  
  
  /*
   */
  class RFloat extends RType
  {
    float val;
  };
  
  
  /*
   */
  class RInt extends RType
  {
    int  val;
  };
  
  
  /*
   */
  class RLong extends RType
  {
    long val;
  };
  
  
  /*
   */
  class RString extends RType
  {
    string val;
  };

  /*
   * Extends RString and simply provides runtime
   * information to the server that this string
   * is intended as a class parameter. Used especially
   * by omero::system::ParamMap (OMERO/System.ice) 
   *
   * Usage:
   *   omero::RClass c = ...; // from service
   *   if (!c.null && c.val.equals("Image")) { ... }
   */
  class RClass extends RString
  {
  };

  

  /* A simple Time implementation. The long value is the number
   * of milliseconds since the epoch (January 1, 1970). 
   */
  class RTime extends RType
  {
    long val;
  };
  
  /*
   */
  class RObject extends RType
  {
    omero::model::IObject val;
  };

  // Collections

  ["java:type:java.util.ArrayList<RType>:java.util.List<RType>"] 
  sequence<RType> RTypeSeq;

  /*
   * The collection classes permit the passing of sequences of all
   * other RTypes (including other collections) and it is itself
   * nullable. The allows for similar arguments to collections in
   * languages with a unified inheritance hierarchy (e.g., Java in
   * which all classes extend from java.lang.Object).
   *
   * This flexible mechanism is not used in all API calls because
   * the flexibility brings a performance penalty.
   */ 
  class RCollection extends RType
  {
    RTypeSeq val;
  };

  // Mapped to an array on the server of a type given 
  // by a random member of the RTypeSeq. Only pass
  // homogenous lists.
  class RArray extends RCollection
  {
  };

  // Mapped to a java.util.List on the server
  class RList extends RCollection
  {
  };

  // Mapped to a java.util.HashSet on the server
  class RSet extends RCollection
  {
  };
};

#endif // OMERO_RTYPES_ICE

