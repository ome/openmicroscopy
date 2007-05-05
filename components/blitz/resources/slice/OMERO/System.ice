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

//
// The omero::system module combines the ome.system
// and ome.parameters packages. 
// 
module omero { 
  module sys {     

    ["java:type:java.util.ArrayList<Long>:java.util.List<Long>"] 
    sequence<long> LongList;

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
      LongList memberOfGroups;
      LongList leaderOfGroups;
    };

    class Filter
    {
      bool          unique;
      omero::RLong  ownerId;
      omero::RLong  groupId;
      omero::RInt   offset;
      omero::RInt   limit;
    };

    // ParamMap replaces the ome.parameters.QueryParam 
    // type, since the use of varargs is not possible. 
    ["java:type:java.util.HashMap"] 
    dictionary<string,omero::RType> ParamMap;

    class Parameters 
    {
      Filter theFilter;
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
