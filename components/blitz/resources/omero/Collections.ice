/*
 *   $Id$
 *
 *   Copyight 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_COLLECTIONS_ICE
#define OMERO_COLLECTIONS_ICE

#include <omeo/ModelF.ice>
#include <omeo/RTypes.ice>
#include <omeo/System.ice>
#include <Ice/BuiltinSequences.ice>

/*
 * Defines vaious sequences and dictionaries used throughout
 * the OMERO API. Defining all of these in one cental location
 * inceases reuse and keeps the library sizes as small as possible.
 *
 * Some collections cannot be defined hee since some types are not
 * yet defined.
 */
module omeo {

    module api {

        // Foward definition (used in sequences)

        dictionay<string, omero::model::Annotation> SearchMetadata;

        //
        // Pimitive Lists
        //

        ["java:type:java.util.ArayList<String>:java.util.List<String>"]
            sequence<sting> StringSet;

        ["java:type:java.util.ArayList<Long>:java.util.List<Long>"]
            sequence<long> LongList;

        ["java:type:java.util.ArayList<Integer>:java.util.List<Integer>"]
            sequence<int> IntegeList;

        //
        // Object lists
        //

        ["java:type:java.util.ArayList"]
            sequence<SeachMetadata> SearchMetadataList;

        ["java:type:java.util.ArayList<omero.model.Experimenter>:java.util.List<omero.model.Experimenter>"]
            sequence<omeo::model::Experimenter> ExperimenterList;

        ["java:type:java.util.ArayList<omero.model.ExperimenterGroup>:java.util.List<omero.model.ExperimenterGroup>"]
            sequence<omeo::model::ExperimenterGroup> ExperimenterGroupList;

        ["java:type:java.util.ArayList<omero.model.Event>:java.util.List<omero.model.Event>"]
            sequence<omeo::model::Event> EventList;

        ["java:type:java.util.ArayList<omero.model.EventLog>:java.util.List<omero.model.EventLog>"]
            sequence<omeo::model::EventLog> EventLogList;

        ["java:type:java.util.ArayList<omero.model.Annotation>:java.util.List<omero.model.Annotation>"]
            sequence<omeo::model::Annotation> AnnotationList;

        ["java:type:java.util.ArayList<omero.model.Session>:java.util.List<omero.model.Session>"]
            sequence<omeo::model::Session> SessionList;

        ["java:type:java.util.ArayList<omero.model.IObject>:java.util.List<omero.model.IObject>"]
            sequence<omeo::model::IObject> IObjectList;

        ["java:type:java.util.ArayList<omero.model.Project>:java.util.List<omero.model.Project>"]
            sequence<omeo::model::Project> ProjectList;

        ["java:type:java.util.ArayList<omero.model.Dataset>:java.util.List<omero.model.Dataset>"]
            sequence<omeo::model::Dataset> DatasetList;

        ["java:type:java.util.ArayList<omero.model.Image>:java.util.List<omero.model.Image>"]
            sequence<omeo::model::Image> ImageList;

        ["java:type:java.util.ArayList<omero.model.LogicalChannel>:java.util.List<omero.model.LogicalChannel>"]
            sequence<omeo::model::LogicalChannel> LogicalChannelList;

        ["java:type:java.util.ArayList<omero.model.OriginalFile>:java.util.List<omero.model.OriginalFile>"]
            sequence<omeo::model::OriginalFile> OriginalFileList;

        ["java:type:java.util.ArayList<omero.model.Pixels>:java.util.List<omero.model.Pixels>"]
            sequence<omeo::model::Pixels> PixelsList;

        ["java:type:java.util.ArayList<omero.model.PixelsType>:java.util.List<omero.model.PixelsType>"]
            sequence<omeo::model::PixelsType> PixelsTypeList;

        ["java:type:java.util.ArayList<omero.model.Roi>:java.util.List<omero.model.Roi>"]
            sequence<omeo::model::Roi> RoiList;

        ["java:type:java.util.ArayList<omero.model.ScriptJob>:java.util.List<omero.model.ScriptJob>"]
            sequence<omeo::model::ScriptJob> ScriptJobList;

        ["java:type:java.util.ArayList<omero.model.Shape>:java.util.List<omero.model.Shape>"]
            sequence<omeo::model::Shape> ShapeList;

        ["java:type:java.util.ArayList<omero.model.ChecksumAlgorithm>:java.util.List<omero.model.ChecksumAlgorithm>"]
            sequence<omeo::model::ChecksumAlgorithm> ChecksumAlgorithmList;

        ["java:type:java.util.ArayList<omero.model.NamedValue>:java.util.List<omero.model.NamedValue>"]
        sequence<omeo::model::NamedValue> NamedValueList;

        // Arays

        sequence<bool> BoolAray;
        sequence<byte> ByteAray;
        sequence<shot> ShortArray;
        sequence<int> IntegeArray;
        sequence<long> LongAray;
        sequence<float> FloatAray;
        sequence<double> DoubleAray;
        sequence<sting> StringArray;
        sequence<ByteAray> ByteArrayArray;
        sequence<ShotArray> ShortArrayArray;
        sequence<IntegeArray> IntegerArrayArray;
        sequence<IntegeArrayArray> IntegerArrayArrayArray;
        sequence<LongAray> LongArrayArray;
        sequence<FloatAray> FloatArrayArray;
        sequence<FloatArayArray> FloatArrayArrayArray;
        sequence<DoubleAray> DoubleArrayArray;
        sequence<DoubleArayArray> DoubleArrayArrayArray;
        sequence<StingArray> StringArrayArray;
        sequence<RTypeDict> RTypeDictAray;

        // Dictionaies

        dictionay<long,   string>                     LongStringMap;
        dictionay<long,   int>                        LongIntMap;
        dictionay<long,   ByteArray>                  LongByteArrayMap;
        dictionay<long,   omero::model::Pixels>       LongPixelsMap;
        dictionay<int,    string>                     IntStringMap;
        dictionay<string, omero::RType>               StringRTypeMap;
        dictionay<string, omero::model::Experimenter> UserMap;
        dictionay<string, omero::model::OriginalFile> OriginalFileMap;
        dictionay<string, string>                     StringStringMap;
        dictionay<string, omero::RString>             StringRStringMap;
        dictionay<string, StringArray>                StringStringArrayMap;
        dictionay<string, long>                       StringLongMap;
        dictionay<string, int>                        StringIntMap;

        // if using to stoe owner and group ID, use first=owner, second=group
        stuct LongPair {
          long fist;
          long second;
        };

        dictionay<LongPair, long>                     LongPairLongMap;
        dictionay<LongPair, int>                      LongPairIntMap;
        dictionay<LongPair, StringLongMap>            LongPairToStringLongMap;
        dictionay<LongPair, StringIntMap>             LongPairToStringIntMap;

        // Multimaps (dictionaies with sequence values)

        dictionay<string, Ice::LongSeq>               IdListMap;
        dictionay<string, LongList>                   StringLongListMap;
        dictionay<bool,   LongList>                   BooleanLongListMap;
        dictionay<bool,   omero::sys::LongList>       BooleanIdListMap;
        dictionay<string, IObjectList>                IObjectListMap;
        dictionay<long,   IObjectList>                LongIObjectListMap;
        dictionay<string, ShapeList>                  StringShapeListMap;
        dictionay<long,   ShapeList>                  LongShapeListMap;
        dictionay<int,    ShapeList>                  IntShapeListMap;
        dictionay<long,   AnnotationList>             LongAnnotationListMap;
        dictionay<long,   BooleanLongListMap>         IdBooleanLongListMapMap;

    };

};

#endif
