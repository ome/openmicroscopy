/*
 *   $Id$
 * 
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef omero_model_IObject
#define omero_model_IObject

#include <omero/fwd.ice>
#include <omero/RTypes.ice>

module omero { 
  module model {     

    /*
     * Base class of all model types. On the
     * server, the interface ome.model.IObject 
     * unifies the model. In Ice, interfaces have
     * a more remote connotation.
     */
    class IObject
    {
      /*
       * The database id for this entity. Of RLong value
       * so that transient entities can have a null id. 
       */
      omero::RLong          id;

      /*
       * Internal details (permissions, owner, etc.) for
       * this entity. All entities have Details, and even
       * a newly created object will have a non-null
       * Details instance. (In the OMERO provided mapping!)
       */
      omero::model::Details details;

      /*
       * An unloaded object contains no state other than id. An
       * exception will be raised if any field other than id is
       * accessed via the OMERO-generated methods. Unloaded objects
       * are useful as pointers or proxies to server-side state.
       */
      bool loaded;

      // METHODS
      // =====================================================

      // Accessors

      omero::RLong getId();

      void setId(omero::RLong id);

      omero::model::Details getDetails();

      /*
       * Sets the loaded boolean to false and empties all state
       * from this entity to make sending it over the network
       * less costly.
       */
      void unload();

    };
  };
};
#endif
