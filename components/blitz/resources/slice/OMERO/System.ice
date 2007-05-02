/*
 *   $Id$
 * 
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_SYSTEM_ICE
#define OMERO_SYSTEM_ICE

#include <OMERO/fwd.ice>
#include <OMERO/RTypes.ice>

module omero { 
  module sys {     

    ["java:type:java.util.ArrayList"] 
    sequence<long> ListOfLongs;

    class EventContext
    {
      long   userId;
      string userName;
      long   groupId;
      string groupName;
      bool   isAdmin;
      bool   isReadOnly;
      long   eventId;
      string eventType;
      ListOfLongs memberOfGroups;
      ListOfLongs leaderOfGroups;
    };

    class Filter
    {
      bool          unique;
      omero::RLong  ownerId;
      omero::RLong  groupId;
      omero::RInt   offset;
      omero::RInt   limit;
    };

    enum Type { 
      longType, intType, boolType, floatType,
      doubleType, stringType, classType, objectType, timeType
    };

    class QueryParam
    {
      string name;
      Type   paramType;
      long   longVal;
      int    intVal;
      bool   boolVal;
      float  floatVal;
      double doubleVal;
      string stringVal;
      string classVal;
      omero::RObject objectVal;
      omero::RTime timeVal;
    };

    ["java:type:java.util.HashMap"] 
    dictionary<string,QueryParam> ParamMap;

    class Parameters 
    {
      Filter filt;
      ParamMap map;
    };

    class Roles
    {
      long   rootId;
      string rootName;
      long   systemGroupId;
      string systemGroupName;
      long   userGroupId;
      string userGroupName;
    };

  };
};

#endif // OMERO_SYSTEM_ICE
