/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

#ifndef OMERO_SYSTEM_ICE
#define OMERO_SYSTEM_ICE

#include <omero/ModelF.ice>
#include <omero/SystemF.ice>
#include <omero/Collections.ice>

/*
 * The omero::system module combines the ome.system and ome.parameters
 * packages from OMERO.server, and represent API arguments which are
 * neither model objects (omero.model.*.ice) nor RTypes (RTypes.ice).
 */
module omero {
  module sys {

    /**
     * Maps the ome.system.EventContext interface. Represents the
     * information known by the server security system about the
     * current user login.
     **/
    class EventContext
    {
      long   shareId;
      long   sessionId;
      string sessionUuid;
      long   userId;
      string userName;
      omero::RLong   sudoerId;
      omero::RString sudoerName;
      long   groupId;
      string groupName;
      bool   isAdmin;
      omero::api::StringSet  adminPrivileges;
      long   eventId;
      string eventType;
      LongList memberOfGroups;
      LongList leaderOfGroups;
      omero::model::Permissions groupPermissions;
    };

    /**
     * Provides common filters which MAY be applied to a
     * query. Check the documentation for the particular
     * method for more information on how these values will
     * be interpreted as well as default values if they
     * are missing. In general they are intended to mean:
     *
     *  unique        := similar to SQL's ""DISTINCT"" keyword
     *
     *  ownerId       := (some) objects queried should belong
     *                   to this user
     *
     *  groupId       := (some) objects queried should belong
     *                   to this group
     *
     *  preferOwner   := true implies if if ownerId and groupId
     *                   are both defined, use only ownerId
     *
     *  offset/limit  := represent a page which should be loaded
     *                   Note: servers may choose to impose a
     *                   maximum limit.
     *
     *  start/endTime := (some) objects queried shoud have been
     *                   created and/or modified within time span.
     *
     **/
    class Filter
    {
      omero::RBool  unique;
      omero::RLong  ownerId;
      omero::RLong  groupId;
      omero::RInt   offset;
      omero::RInt   limit;
      omero::RTime  startTime;
      omero::RTime  endTime;
      // omero::RBool  preferOwner; Not yet implemented
    };

    /**
     * Similar to Filter, provides common options which MAY be
     * applied on a given method. Check each interface's
     * documentation for more details.
     *
     *  leaves        := whether or not graph leaves (usually images)
     *                   should be loaded
     *
     *  orphan        := whether or not orphaned objects (e.g. datasets
     *                   not in a project) should be loaded
     *
     *  acquisition...:= whether or not acquisitionData (objectives, etc.)
     *                  should be loaded
     *
     * cacheable      := whether or not the query is cacheable by Hibernate
     *                   (use with caution: caching may be counterproductive)
     **/
    class Options
    {
      omero::RBool  leaves;
      omero::RBool  orphan;
      omero::RBool  acquisitionData;
      ["deprecate:experimental: may be wholly removed in next major version"]
      omero::RBool  cacheable;
    };

    /**
     * Holder for all the parameters which can be taken to a query.
     **/
    class Parameters
    {
      /*
       * Contains named arguments which may either be used by
       * a Query implementation or by the method itself for
       * further refinements.
       */
      ParamMap map;
      Filter theFilter;
      Options theOptions;
    };

    /**
     * Principal used for login, etc.
     **/
    class Principal
    {
      string name;
      string group;
      string eventType;
      omero::model::Permissions umask;
    };

    /**
     * Server-constants used for determining particular groups and
     * users.
     **/
    class Roles
    {
      // Root account
      long   rootId;
      string rootName;

      // System group (defines who is an "admin")
      long   systemGroupId;
      string systemGroupName;

      // The group which defines a ""user"". Any user not in the user
      // group is considered inactive.
      long   userGroupId;
      string userGroupName;

      // the guest user
      long   guestId;
      string guestName;

      // ""guest"" group. Can log in and use some methods.
      long   guestGroupId;
      string guestGroupName;
    };

  };
};

#endif // OMERO_SYSTEM_ICE
