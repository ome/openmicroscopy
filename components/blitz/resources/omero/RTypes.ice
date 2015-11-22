/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_RTYPES_ICE
#define OMERO_RTYPES_ICE

//
// Simple type definitions used for remoting purposes.
// See README.ice for a description of the omero module.
//
module omero {

  /**
   * Simple base ["protected"] class. Essentially abstract.
   **/
  ["protected"] class RType
  {
    /**
     * Equals-like functionality for all RTypes. A return value
     * of 0 means they are equivalent and were almost certainly
     * created by the same constructor call, e.g.
     *
     * <pre>
     *   rbool(true).compare(rbool(true)) == 0
     * </pre>
     *
     * This method was originally added (Oct 2008) to force the
     * base RType class to be abstract in all languages.
     **/
    int compare(RType rhs);
  };

  /**
   * Boolean wrapper.
   **/
  ["protected"] class RBool extends RType
  {
    bool val;
    bool getValue();
  };


  /**
   * Double wrapper.
   **/
  ["protected"] class RDouble extends RType
  {
    double val;
    double getValue();
  };


  /**
   * Float wrapper.
   **/
  ["protected"] class RFloat extends RType
  {
    float val;
    float getValue();
  };


  /**
   * Integer wrapper.
   **/
  ["protected"] class RInt extends RType
  {
    int val;
    int getValue();
  };


  /**
   * Long Wrapper.
   **/
  ["protected"] class RLong extends RType
  {
    long val;
    long getValue();
  };


  /**
   * String wrapper.
   **/
  ["protected"] class RString extends RType
  {
    string val;
    string getValue();
  };

  /**
   * Extends RString and simply provides runtime
   * information to the server that this string
   * is intended as a "protected" class parameter. Used especially
   * by {@link omero.system.ParamMap} (omero/System.ice)
   * 
   * Usage:
   * <pre>
   *   omero::RClass c = ...; // from service
   *   if (!c.null && c.val.equals("Image")) { ... }
   * </pre>
   **/
  ["protected"] class RClass extends RString
  {
  };

  /**
   * A simple Time implementation. The long value is the number
   * of milliseconds since the epoch (January 1, 1970).
   **/
  ["protected"] class RTime extends RType
  {
    long val;
    long getValue();
  };

  // Collections

  /**
   * Simple sequence of {@link RType} instances. Note: when passing
   * an {@link RTypeSeq} over the wire, null sequence is maintained and
   * will be turned into an empty sequence. If nullability is
   * required, see the {@link RCollection} types.
   *
   * @see RCollection
   * @see RTypeDict
   */
  ["java:type:java.util.ArrayList<omero.RType>:java.util.List<omero.RType>"]
  sequence<RType> RTypeSeq;

  /**
   *
   **/
  ["java:type:java.util.ArrayList<java.util.List<omero.RType>>:java.util.List<java.util.List<omero.RType>>"]
  sequence<RTypeSeq> RTypeSeqSeq;

  /**
   * The collection "protected" classes permit the passing of sequences of all
   * other RTypes (including other collections) and it is itself nullable. The
   * allows for similar arguments to collections in languages with a unified
   * inheritance hierarchy (e.g., Java in which all "protected" classes extend
   * from java.lang.Object).
   *
   * Unlike the other rtypes which are used internally within the
   * {@link omero.model} classes, these types are mutable since they solely
   * pass through the
   *
   * This flexible mechanism is not used in all API calls because
   * the flexibility brings a performance penalty.
   **/
  ["protected"] class RCollection extends RType
  {
    RTypeSeq val;
    RTypeSeq getValue();
    int size();
    RType get(int index);
    void add(RType value);
    void addAll(RTypeSeq value);
  };

  /**
   * {@link RCollection} mapped to an array on the server of a type given
   * by a random member of the RTypeSeq. Only pass consistent arrays!
   * homogenous lists.
   **/
  ["protected"] class RArray extends RCollection
  {
  };

  /**
   * {@link RCollection} mapped to a java.util.List on the server
   **/
  ["protected"] class RList extends RCollection
  {
  };

  /**
   * {@link RCollection} mapped to a java.util.HashSet on the server
   **/
  ["protected"] class RSet extends RCollection
  {
  };

  /**
   * Simple dictionary of {@link RType} instances. Note: when passing
   * an RTypeDict over the wire, a null map will not be maintained and
   * will be turned into an empty map. If nullability is
   * required, see the {@link RMap} type.
   **/
  ["java:type:java.util.HashMap<String,omero.RType>"]
  dictionary<string,omero::RType> RTypeDict;

  /**
   * Similar to {@link RCollection}, the {@link RMap} class permits the passing
   * of a possible null {@link RTypeDict} where any other {@link RType} is
   * expected.
   *
   * @see RTypeDict
   * @see RCollection
   **/
  ["protected"] class RMap extends RType {
    RTypeDict val;
    RTypeDict getValue();
    int size();
    RType get(string key);
    void put(string key, RType value);
  };
};

#endif // OMERO_RTYPES_ICE
