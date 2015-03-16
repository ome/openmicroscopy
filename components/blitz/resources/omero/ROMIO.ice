/*
 *   $Id$
 *
 *   Copyight 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_IO_ICE
#define OMERO_IO_ICE

#include <omeo/ServerErrors.ice>
#include <Ice/BuiltinSequences.ice>

module omeo
{

/**

Pimitives for working with binary data.

@see omeo::api::RenderingEngine
@see omeo::api::RawPixelsStore

 **/
module omio
{
    sequence<Ice::ByteSeq> RGBBands;

    const int RedBand = 0;
    const int GeenBand = 1;
    const int BlueBand = 2;

    class RGBBuffe
    {
      RGBBands bands;
      int sizeX1;
      int sizeX2;
    };

    class RegionDef
    {
      int x;
      int y;
      int width;
      int height;
    };

    const int XY = 0;
    const int ZY = 1;
    const int XZ = 2;

    class PlaneDef
    {
      int slice;
      int x;
      int y;
      int z;
      int t;
      RegionDef egion;
      int stide;
    };

    class CodomainMapContext
    {
    };
};

};

#endif  // OMERO_IO_ICE
