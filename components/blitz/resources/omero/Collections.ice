/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_COLLECTIONS_ICE
#define OMERO_COLLECTIONS_ICE

#include <omero/RTypes.ice>
#include <Ice/BuiltinSequences.ice>

/*
 * Defines various sequences and dictionaries used throughout
 * the OMERO API. Defining all of these in one central location
 * increases reuse and keeps the library sizes as small as possible.
 *
 * Some collections cannot be defined here since some types are not
 * yet defined.
 */
module omero {

    /*
     * Some collections were initially defined under omero::sys
     */
    module sys {

        ["java:type:java.util.ArrayList<Long>:java.util.List<Long>"]
            sequence<long> LongList;

        ["java:type:java.util.ArrayList<Integer>:java.util.List<Integer>"]
            sequence<int> IntList;

        ["java:type:java.util.HashMap<Long,Long>:java.util.Map<Long,Long>"]
            dictionary<long, long> CountMap;

        /*
         * ParamMap replaces the ome.parameters.QueryParam
         * type, since the use of varargs is not possible.
         */
        ["java:type:java.util.HashMap"]
            dictionary<string,omero::RType> ParamMap;

        /*
         * IdByteMap is used by the ThumbnailService for the multiple thumbnail
         * retrieval methods.
         */
        ["java:type:java.util.HashMap"]
            dictionary<long,Ice::ByteSeq> IdByteMap;

    };

    module api {

        // Forward definition (used in sequences)

        dictionary<string, omero::model::Annotation> SearchMetadata;

        // Lists


        ["java:type:java.util.ArrayList"]
            sequence<SearchMetadata> SearchMetadataList;

        ["java:type:java.util.ArrayList<omero.model.Experimenter>:java.util.List<omero.model.Experimenter>"]
            sequence<omero::model::Experimenter> ExperimenterList;

        ["java:type:java.util.ArrayList<omero.model.ExperimenterGroup>:java.util.List<omero.model.ExperimenterGroup>"]
            sequence<omero::model::ExperimenterGroup> ExperimenterGroupList;

        ["java:type:java.util.ArrayList<omero.model.Annotation>:java.util.List<omero.model.Annotation>"]
            sequence<omero::model::Annotation> AnnotationList;

        ["java:type:java.util.ArrayList<omero.model.Session>:java.util.List<omero.model.Session>"]
            sequence<omero::model::Session> SessionList;

        ["java:type:java.util.ArrayList<String>:java.util.List<String>"]
            sequence<string> StringSet;

        ["java:type:java.util.ArrayList<omero.model.IObject>:java.util.List<omero.model.IObject>"]
            sequence<omero::model::IObject> IObjectList;

        ["java:type:java.util.ArrayList<omero.model.Project>:java.util.List<omero.model.Project>"]
            sequence<omero::model::Project> ProjectList;

        ["java:type:java.util.ArrayList<omero.model.Dataset>:java.util.List<omero.model.Dataset>"]
            sequence<omero::model::Dataset> DatasetList;

        ["java:type:java.util.ArrayList<omero.model.Image>:java.util.List<omero.model.Image>"]
            sequence<omero::model::Image> ImageList;

        ["java:type:java.util.ArrayList<omero.model.Pixels>:java.util.List<omero.model.Pixels>"]
            sequence<omero::model::Pixels> PixelsList;

        ["java:type:java.util.ArrayList<omero.model.PixelsType>:java.util.List<omero.model.PixelsType>"]
            sequence<omero::model::PixelsType> PixelsTypeList;

        ["java:type:java.util.ArrayList<Long>:java.util.List<Long>"]
            sequence<long> LongList;

        ["java:type:java.util.ArrayList<Integer>:java.util.List<Integer>"]
            sequence<int> IntegerList;

        // Arrays

        sequence<byte> ByteArray;
        sequence<short> ShortArray;
        sequence<int> IntegerArray;
       	sequence<long> LongArray;
		sequence<double> DoubleArray;
      	sequence<ByteArray> ByteArrayArray;
		sequence<ShortArray> ShortArrayArray;
	  	sequence<IntegerArray> IntegerArrayArray;
        sequence<IntegerArrayArray> IntegerArrayArrayArray;
        sequence<LongArray> LongArrayArray;
		sequence<DoubleArray> DoubleArrayArray;
        sequence<DoubleArrayArray> DoubleArrayArrayArray;

        // Dictionaries

        dictionary<bool, omero::sys::LongList> BooleanIdListMap;
        dictionary<long, string> LongStringMap;
        dictionary<long, ByteArray> LongByteArrayMap;
        dictionary<long, omero::model::Pixels> LongPixelsMap;
        dictionary<string, omero::RType> StringRTypeMap;
        dictionary<string, omero::model::Experimenter> UserMap;
        dictionary<string, IObjectList> IObjectListMap;
        dictionary<string, Ice::LongSeq> IdListMap;

        // Redundant or too specifically named definitions. Should possibly be deprecated.

        dictionary<long, string> ScriptIDNameMap;
        dictionary<long, IObjectList> AnnotationMap;

    };

};

#endif
