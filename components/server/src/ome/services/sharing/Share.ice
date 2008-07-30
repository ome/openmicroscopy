/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef __OME_SERVICES_SHARING_SHARE_ICE
#define __OME_SERVICES_SHARING_SHARE_ICE

module  ome { module services { module sharing { module data {

    ["java:type:java.util.ArrayList<Long>:java.util.List<Long>"]
    sequence<long> LongSeq;

    ["java:type:java.util.ArrayList<String>:java.util.List<String>"]
    sequence<string> StringSeq;

    dictionary<string, LongSeq> IdMap;

    /*
     * Full definition of a "share".
     */
    class ShareData
    {
        long id;
        string owner;
        LongSeq members;
        StringSeq guests;
        IdMap objects;
        bool enabled;
    };

    /*
     * View on the ShareData, per object. Essentially an ACL list.
     */
    class ShareItem
    {
        long share;
        string type;
        long id;
        LongSeq members;
        StringSeq guests;
    };

}; }; }; };

#endif
