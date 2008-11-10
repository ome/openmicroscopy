/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_RTYPES_ICE
#define OMERO_RTYPES_ICE

#include <omero/ModelF.ice>

/*
 * Simple type definitions used for remoting purposes.
 *
 * RType-sub["protected"] classes permit both the passing of null values to
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
   * Simple base ["protected"] class. Essentially abstract.
   */
  ["protected"] class RType
  {
    /**
     * Equals-like functionality for all RTypes. A return value
     * of 0 means they are equivalent and were almost certainly
     * created by the same constructor call, e.g.
     *
     *   rbool(true).compare(rbool(true)) == 0
     *
     * This method was originally addd (Oct 2008) to force the
     * base RType class to be abstract in all languages.
     */
    int compare(RType rhs);
  };

  /*
   */
  ["protected"] class RBool extends RType
  {
    bool val;
    bool getValue();
  };


  /*
   */
  ["protected"] class RDouble extends RType
  {
    double val;
    double getValue();
  };


  /*
   */
  ["protected"] class RFloat extends RType
  {
    float val;
    float getValue();
  };


  /*
   */
  ["protected"] class RInt extends RType
  {
    int val;
    int getValue();
  };


  /*
   */
  ["protected"] class RLong extends RType
  {
    long val;
    long getValue();
  };


  /*
   */
  ["protected"] class RString extends RType
  {
    string val;
    string getValue();
  };

  /*
   * Extends RString and simply provides runtime
   * information to the server that this string
   * is intended as a ["protected"] class parameter. Used especially
   * by omero::system::ParamMap (omero/System.ice)
   *
   * Usage:
   *   omero::RClass c = ...; // from service
   *   if (!c.null && c.val.equals("Image")) { ... }
   */
  ["protected"] class RClass extends RString
  {
  };



  /* A simple Time implementation. The long value is the number
   * of milliseconds since the epoch (January 1, 1970).
   */
  ["protected"] class RTime extends RType
  {
    long val;
    long getValue();
  };

  /*
   */
  ["protected"] class RObject extends RType
  {
    omero::model::IObject val;
    // Here we don't want the pointer being altered
    omero::model::IObject getValue();
  };

  // Collections

  ["java:type:java.util.ArrayList<RType>:java.util.List<RType>"]
  sequence<RType> RTypeSeq;

  /*
   * The collection ["protected"] classes permit the passing of sequences of all
   * other RTypes (including other collections) and it is itself
   * nullable. The allows for similar arguments to collections in
   * languages with a unified inheritance hierarchy (e.g., Java in
   * which all ["protected"] classes extend from java.lang.Object).
   *
   * Unlike the other rtypes which are used internally within the
   * omero.model classes, these types are mutable since they solely
   * pass through the
   *
   * This flexible mechanism is not used in all API calls because
   * the flexibility brings a performance penalty.
   */
  ["protected"] class RCollection extends RType
  {
    RTypeSeq val;
    RTypeSeq getValue();
    int size();
    RType get(int index);
    void add(RType value);
    void addAll(RTypeSeq value);
  };

  // Mapped to an array on the server of a type given
  // by a random member of the RTypeSeq. Only pass
  // homogenous lists.
  ["protected"] class RArray extends RCollection
  {
  };

  // Mapped to a java.util.List on the server
  ["protected"] class RList extends RCollection
  {
  };

  // Mapped to a java.util.HashSet on the server
  ["protected"] class RSet extends RCollection
  {
  };

  ["java:type:java.util.HashMap<String,RType>"]
  dictionary<string,omero::RType> RTypeDict;

  ["protected"] class RMap extends RType {
    RTypeDict val;
    RTypeDict getValue();
    int size();
    RType get(string key);
    void put(string key, RType value);
  };
};

#endif // OMERO_RTYPES_ICE
