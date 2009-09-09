/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_SERVICESF_ICE
#define OMERO_SERVICESF_ICE

module omero {

    module api {
        interface RawFileStore;
        interface RawPixelsStore;
        interface RenderingEngine;
        interface ThumbnailStore;
    };

    module grid {
        interface SharedResources;
    };
};

#endif
