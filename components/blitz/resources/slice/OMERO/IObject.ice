/*
 *   $Id$
 * 
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef omero_model_IObject
#define omero_model_IObject

#include <OMERO/fwd.ice>
#include <OMERO/RTypes.ice>

module omero { module model {     


class IObject
{
    omero::RLong          id;
    omero::model::Details details;
    bool loaded;
    void unload();
};
};};
#endif 
