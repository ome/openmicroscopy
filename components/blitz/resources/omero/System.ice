/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_SYSTEM_ICE
#define OMERO_SYSTEM_ICE

#include <omero/fwd.ice>
#include <omero/Collections.ice>

/*
 * The omero::system module combines the ome.system and ome.parameters
 * packages from OMERO.server, and represent API arguments which are
 * neither model objects (omero.model.*.ice) nor RTypes (RTypes.ice).
 */
module omero {
  module sys {

    /*
     * Maps the ome.system.EventContext interface. Represents the
     * information known by the server security system about the
     * current user login.
     */
    class EventContext
    {
      long   shareId;
      long   sessionId;
      string sessionUuid;
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
      omero::model::Permissions umask;
    };

    /*
     *
     */
    class Filter
    {
      bool          unique;
      omero::RLong  ownerId;
      omero::RLong  groupId;
      omero::RInt   offset;
      omero::RInt   limit;
    };

    /*
     * Holder for all the parameters which can be taken to a query.
     */
    class Parameters
    {
      Filter theFilter;
      ParamMap map;
    };

    /*
     * Principal used for login, etc.
     */
    class Principal
    {
      string name;
      string group;
      string eventType;
      omero::model::Permissions umask;
    };

    /*
     * Server-constants used for determining particular groups and
     * users.
     */
    class Roles
    {
      // Root account
      long   rootId;
      string rootName;

      // System group (defines who is an "admin")
      long   systemGroupId;
      string systemGroupName;

      // The group which defines a "user". Any user not in the user
      // group is considered inactive.
      long   userGroupId;
      string userGroupName;

      // "guest" group. Can log in and use some methods.
      string guestGroupName;
    };

  };
};

#endif // OMERO_SYSTEM_ICE
