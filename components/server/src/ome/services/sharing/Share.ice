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

    class Obj
    {
        string type;
        long id;
    };

    ["java:type:java.util.ArrayList<ome.services.sharing.data.Obj>:java.util.List<ome.services.sharing.data.Obj>"]
    sequence<Obj> ObjSeq;

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
        long owner;
        LongSeq members;
        StringSeq guests;
        IdMap objectMap;
        ObjSeq objectList;

        bool enabled;
        long optlock;
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
