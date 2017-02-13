/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_COLLECTIONS_ICE
#define OMERO_COLLECTIONS_ICE

#include <omero/ModelF.ice>
#include <omero/RTypes.ice>
#include <omero/System.ice>
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

    module api {

        // Forward definition (used in sequences)

        dictionary<string, omero::model::Annotation> SearchMetadata;

        //
        // Primitive Lists
        //

        ["java:type:java.util.ArrayList<String>:java.util.List<String>"]
            sequence<string> StringSet;

        ["java:type:java.util.ArrayList<Long>:java.util.List<Long>"]
            sequence<long> LongList;

        ["java:type:java.util.ArrayList<Integer>:java.util.List<Integer>"]
            sequence<int> IntegerList;

        //
        // Object lists
        //

        ["java:type:java.util.ArrayList"]
            sequence<SearchMetadata> SearchMetadataList;

        ["java:type:java.util.ArrayList<omero.model.Experimenter>:java.util.List<omero.model.Experimenter>"]
            sequence<omero::model::Experimenter> ExperimenterList;

        ["java:type:java.util.ArrayList<omero.model.ExperimenterGroup>:java.util.List<omero.model.ExperimenterGroup>"]
            sequence<omero::model::ExperimenterGroup> ExperimenterGroupList;

        ["java:type:java.util.ArrayList<omero.model.Event>:java.util.List<omero.model.Event>"]
            sequence<omero::model::Event> EventList;

        ["java:type:java.util.ArrayList<omero.model.EventLog>:java.util.List<omero.model.EventLog>"]
            sequence<omero::model::EventLog> EventLogList;

        ["java:type:java.util.ArrayList<omero.model.Annotation>:java.util.List<omero.model.Annotation>"]
            sequence<omero::model::Annotation> AnnotationList;

        ["java:type:java.util.ArrayList<omero.model.Session>:java.util.List<omero.model.Session>"]
            sequence<omero::model::Session> SessionList;

        ["java:type:java.util.ArrayList<omero.model.IObject>:java.util.List<omero.model.IObject>"]
            sequence<omero::model::IObject> IObjectList;

        ["java:type:java.util.ArrayList<omero.model.Project>:java.util.List<omero.model.Project>"]
            sequence<omero::model::Project> ProjectList;

        ["java:type:java.util.ArrayList<omero.model.Dataset>:java.util.List<omero.model.Dataset>"]
            sequence<omero::model::Dataset> DatasetList;

        ["java:type:java.util.ArrayList<omero.model.Image>:java.util.List<omero.model.Image>"]
            sequence<omero::model::Image> ImageList;

        ["java:type:java.util.ArrayList<omero.model.LogicalChannel>:java.util.List<omero.model.LogicalChannel>"]
            sequence<omero::model::LogicalChannel> LogicalChannelList;

        ["java:type:java.util.ArrayList<omero.model.OriginalFile>:java.util.List<omero.model.OriginalFile>"]
            sequence<omero::model::OriginalFile> OriginalFileList;

        ["java:type:java.util.ArrayList<omero.model.Pixels>:java.util.List<omero.model.Pixels>"]
            sequence<omero::model::Pixels> PixelsList;

        ["java:type:java.util.ArrayList<omero.model.PixelsType>:java.util.List<omero.model.PixelsType>"]
            sequence<omero::model::PixelsType> PixelsTypeList;

        ["java:type:java.util.ArrayList<omero.model.Roi>:java.util.List<omero.model.Roi>"]
            sequence<omero::model::Roi> RoiList;

        ["java:type:java.util.ArrayList<omero.model.ScriptJob>:java.util.List<omero.model.ScriptJob>"]
            sequence<omero::model::ScriptJob> ScriptJobList;

        ["java:type:java.util.ArrayList<omero.model.Shape>:java.util.List<omero.model.Shape>"]
            sequence<omero::model::Shape> ShapeList;

        ["java:type:java.util.ArrayList<omero.model.ChecksumAlgorithm>:java.util.List<omero.model.ChecksumAlgorithm>"]
            sequence<omero::model::ChecksumAlgorithm> ChecksumAlgorithmList;

        ["java:type:java.util.ArrayList<omero.model.NamedValue>:java.util.List<omero.model.NamedValue>"]
        sequence<omero::model::NamedValue> NamedValueList;

        ["java:type:java.util.ArrayList<omero.sys.EventContext>:java.util.List<omero.sys.EventContext>"]
        sequence<omero::sys::EventContext> EventContextList;

        // Arrays

        sequence<bool> BoolArray;
        sequence<byte> ByteArray;
        sequence<short> ShortArray;
        sequence<int> IntegerArray;
        sequence<long> LongArray;
        sequence<float> FloatArray;
        sequence<double> DoubleArray;
        sequence<string> StringArray;
        sequence<ByteArray> ByteArrayArray;
        sequence<ShortArray> ShortArrayArray;
        sequence<IntegerArray> IntegerArrayArray;
        sequence<IntegerArrayArray> IntegerArrayArrayArray;
        sequence<LongArray> LongArrayArray;
        sequence<FloatArray> FloatArrayArray;
        sequence<FloatArrayArray> FloatArrayArrayArray;
        sequence<DoubleArray> DoubleArrayArray;
        sequence<DoubleArrayArray> DoubleArrayArrayArray;
        sequence<StringArray> StringArrayArray;
        sequence<RTypeDict> RTypeDictArray;

        // Dictionaries

        dictionary<long,   string>                     LongStringMap;
        dictionary<long,   int>                        LongIntMap;
        dictionary<long,   ByteArray>                  LongByteArrayMap;
        dictionary<long,   omero::model::Pixels>       LongPixelsMap;
        dictionary<int,    string>                     IntStringMap;
        dictionary<string, omero::RType>               StringRTypeMap;
        dictionary<string, omero::model::Experimenter> UserMap;
        dictionary<string, omero::model::OriginalFile> OriginalFileMap;
        dictionary<string, string>                     StringStringMap;
        dictionary<string, omero::RString>             StringRStringMap;
        dictionary<string, StringArray>                StringStringArrayMap;
        dictionary<string, long>                       StringLongMap;
        dictionary<string, int>                        StringIntMap;

        // if using to store owner and group ID, use first=owner, second=group
        struct LongPair {
          long first;
          long second;
        };

        dictionary<LongPair, long>                     LongPairLongMap;
        dictionary<LongPair, int>                      LongPairIntMap;
        dictionary<LongPair, StringLongMap>            LongPairToStringLongMap;
        dictionary<LongPair, StringIntMap>             LongPairToStringIntMap;

        // Multimaps (dictionaries with sequence values)

        dictionary<string, Ice::LongSeq>               IdListMap;
        dictionary<string, LongList>                   StringLongListMap;
        dictionary<bool,   LongList>                   BooleanLongListMap;
        dictionary<bool,   omero::sys::LongList>       BooleanIdListMap;
        dictionary<string, IObjectList>                IObjectListMap;
        dictionary<long,   IObjectList>                LongIObjectListMap;
        dictionary<string, ShapeList>                  StringShapeListMap;
        dictionary<long,   ShapeList>                  LongShapeListMap;
        dictionary<int,    ShapeList>                  IntShapeListMap;
        dictionary<long,   AnnotationList>             LongAnnotationListMap;
        dictionary<long,   BooleanLongListMap>         IdBooleanLongListMapMap;

    };

};

#endif
