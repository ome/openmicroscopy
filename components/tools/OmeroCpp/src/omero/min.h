/*
 *
 *  Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *  Use is subject to license terms supplied in LICENSE.txt
 */

#if !defined(OMERO_IMPORT_STYLE) || OMERO_IMPORT_STYLE == 'A'
    // Here we allow the defining of style
    // only if we are not being called by
    // all.h. If so, we assume that its
    // ifdef directive will prevent this
    // from being called multiple times.
    #if !defined(OMERO_IMPORT_STYLE)
        #define OMERO_IMPORT_STYLE 'M'
    #endif
#include <omero/IceNoWarnPush.h>
#include<omero/API.h>
#include<omero/ServicesF.h>
#include<omero/Constants.h>
#include<omero/cmd/API.h>
#include <omero/IceNoWarnPop.h>
#include<omero/RTypesI.h>
#include<omero/model/NamedValue.h>
#endif
