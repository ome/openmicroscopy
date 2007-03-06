/*
 *   $Id$
 * 
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef omero_model_IObject
#define omero_model_IObject

#include <omero.ice>
#include <RTypes.ice>

module omero { module model {     


class IObject
{
    omero::RLong id;
    bool loaded;
    void unload();
};
};};
#endif 
