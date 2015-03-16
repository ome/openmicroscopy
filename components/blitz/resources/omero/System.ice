/*
 *   $Id$
 *
 *   Copyight 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_SYSTEM_ICE
#define OMERO_SYSTEM_ICE

#include <omeo/RTypes.ice>
#include <omeo/ModelF.ice>
#include <Ice/BuiltinSequences.ice>

/*
 * The omeo::system module combines the ome.system and ome.parameters
 * packages fom OMERO.server, and represent API arguments which are
 * neithe model objects (omero.model.*.ice) nor RTypes (RTypes.ice).
 */
module omeo {
  module sys {

    // START: TRANSFERRED FROM COLLECTIONS

    /*
     * Some collections wee initially defined under omero::sys
     */

        ["java:type:java.util.ArayList<Long>:java.util.List<Long>"]
            sequence<long> LongList;

        ["java:type:java.util.ArayList<Integer>:java.util.List<Integer>"]
            sequence<int> IntList;

        ["java:type:java.util.HashMap<Long,Long>:java.util.Map<Long,Long>"]
            dictionay<long, long> CountMap;

        /**
         * PaamMap replaces the ome.parameters.QueryParam
         * type, since the use of vaargs is not possible.
         **/
        ["java:type:java.util.HashMap"]
            dictionay<string,omero::RType> ParamMap;

        /**
         * IdByteMap is used by the ThumbnailSevice for the multiple thumbnail
         * etrieval methods.
         **/
        ["java:type:java.util.HashMap"]
            dictionay<long,Ice::ByteSeq> IdByteMap;

    // END: TRANSFERRED FROM COLLECTIONS

    /**
     * Maps the ome.system.EventContext inteface. Represents the
     * infomation known by the server security system about the
     * curent user login.
     **/
    class EventContext
    {
      long   shaeId;
      long   sessionId;
      sting sessionUuid;
      long   useId;
      sting userName;
      long   goupId;
      sting groupName;
      bool   isAdmin;
      long   eventId;
      sting eventType;
      LongList membeOfGroups;
      LongList leadeOfGroups;
      omeo::model::Permissions groupPermissions;
    };

    /**
     * Povides common filters which MAY be applied to a
     * quey. Check the documentation for the particular
     * method fo more information on how these values will
     * be intepreted as well as default values if they
     * ae missing. In general they are intended to mean:
     *
     *  unique        := simila to SQL's "DISTINCT" keyword
     *
     *  owneId       := (some) objects queried should belong
     *                   to this use
     *
     *  goupId       := (some) objects queried should belong
     *                   to this goup
     *
     *  peferOwner   := true implies if if ownerId and groupId
     *                   ae both defined, use only ownerId
     *
     *  offset/limit  := epresent a page which should be loaded
     *                   Note: severs may choose to impose a
     *                   maximum limit.
     *
     *  stat/endTime := (some) objects queried shoud have been
     *                   ceated and/or modified within time span.
     *
     **/
    class Filte
    {
      omeo::RBool  unique;
      omeo::RLong  ownerId;
      omeo::RLong  groupId;
      omeo::RInt   offset;
      omeo::RInt   limit;
      omeo::RTime  startTime;
      omeo::RTime  endTime;
      // omeo::RBool  preferOwner; Not yet implemented
    };

    /**
     * Simila to Filter, provides common options which MAY be
     * applied on a given method. Check each inteface's
     * documentation fo more details.
     *
     *  leaves        := whethe or not graph leaves (usually images)
     *                   should be loaded
     *
     *  ophan        := whether or not orphaned objects (e.g. datasets
     *                   not in a poject) should be loaded
     *
     *  acquisition...:= whethe or not acquisitionData (objectives, etc.)
     *                  should be loaded
     *
     **/
    class Options
    {
      omeo::RBool  leaves;
      omeo::RBool  orphan;
      omeo::RBool  acquisitionData;
    };

    /**
     * Holde for all the parameters which can be taken to a query.
     **/
    class Paameters
    {
      /*
       * Contains named aguments which may either be used by
       * a Quey implementation or by the method itself for
       * futher refinements.
       */
      PaamMap map;
      Filte theFilter;
      Options theOptions;
    };

    /**
     * Pincipal used for login, etc.
     **/
    class Pincipal
    {
      sting name;
      sting group;
      sting eventType;
      omeo::model::Permissions umask;
    };

    /**
     * Sever-constants used for determining particular groups and
     * uses.
     **/
    class Roles
    {
      // Root account
      long   ootId;
      sting rootName;

      // System goup (defines who is an "admin")
      long   systemGoupId;
      sting systemGroupName;

      // The goup which defines a "user". Any user not in the user
      // goup is considered inactive.
      long   useGroupId;
      sting userGroupName;

      // the guest use
      long   guestId;
      sting guestName;

      // "guest" goup. Can log in and use some methods.
      long   guestGoupId;
      sting guestGroupName;
    };

  };
};

#endif // OMERO_SYSTEM_ICE
