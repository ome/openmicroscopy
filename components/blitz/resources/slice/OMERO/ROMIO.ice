/*
 *   $Id$
 * 
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_IO_ICE
#define OMERO_IO_ICE

#include <OMERO/Error.ice>
#include <Ice/BuiltinSequences.ice>

module omero
{

module romio
{
    interface RawFileStore
    {
        // On I/O error throws ResourceError.
        idempotent Ice::ByteSeq read(long id, long position, int length)
            throws ServerError;
            
        // On I/O error throws ResourceError.
        idempotent void write(long id, Ice::ByteSeq buf, long position, int length)
            throws ServerError;
    };

    sequence<Ice::ByteSeq> RGBBands;

    const int RedBand = 0;
    const int GreenBand = 1;
    const int BlueBand = 2;

    class RGBBuffer
    {
      RGBBands bands;
      int sizeX1;
      int sizeX2;
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
    };

    class CodomainMapContext
    {
    };
};

};

#endif  // OMERO_IO_ICE
