/*
 *   Copyright 2017 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/RTypes.ice>
#include <Ice/BuiltinSequences.ice>

#ifndef OMERO_SYSTEMF_ICE
#define OMERO_SYSTEMF_ICE

/*
 * Defines various classes for more simplified imports.
 */
module omero {

    module sys {

        // START: TRANSFERRED FROM COLLECTIONS
        // Some collections were initially defined under omero::sys

        ["java:type:java.util.ArrayList<Long>:java.util.List<Long>"]
            sequence<long> LongList;

        ["java:type:java.util.ArrayList<Integer>:java.util.List<Integer>"]
            sequence<int> IntList;

        ["java:type:java.util.HashMap<Long,Long>:java.util.Map<Long,Long>"]
            dictionary<long, long> CountMap;

        /**
         * ParamMap replaces the ome.parameters.QueryParam
         * type, since the use of varargs is not possible.
         **/
        ["java:type:java.util.HashMap"]
            dictionary<string,omero::RType> ParamMap;

        /**
         * IdByteMap is used by the ThumbnailService for the multiple thumbnail
         * retrieval methods.
         **/
        ["java:type:java.util.HashMap"]
            dictionary<long,Ice::ByteSeq> IdByteMap;

        // END: TRANSFERRED FROM COLLECTIONS

        class EventContext;

    };

};

#endif
