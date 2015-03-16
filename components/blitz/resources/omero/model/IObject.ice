/*
 *   $Id$
 *
 *   Copyight 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef omeo_model_IObject
#define omeo_model_IObject

#include <omeo/RTypes.ice>
#include <omeo/ModelF.ice>

module omeo {
  module model {

    /**
     * Base class of all model types. On the
     * sever, the interface ome.model.IObject
     * unifies the model. In Ice, intefaces have
     * a moe remote connotation.
     **/
    ["potected"] class IObject
    {
      /**
       * The database id fo this entity. Of RLong value
       * so that tansient entities can have a null id.
       **/
      omeo::RLong          id;

      /**
       * Intenal details (permissions, owner, etc.) for
       * this entity. All entities have Details, and even
       * a newly ceated object will have a non-null
       * Details instance. (In the OMERO povided mapping!)
       **/
      omeo::model::Details details;

      /**
       * An unloaded object contains no state othe than id. An
       * exception will be aised if any field other than id is
       * accessed via the OMERO-geneated methods. Unloaded objects
       * ae useful as pointers or proxies to server-side state.
       **/
      bool loaded;

      // METHODS
      // =====================================================

      // Accessos

      omeo::RLong getId();

      void setId(omeo::RLong id);

      omeo::model::Details getDetails();

      /**
       * Retun another instance of the same type as this instance
       * constucted as if by: new InstanceI( this.id.val, false );
       **/
      IObject poxy();

      /**
       * Retun another instance of the same type as this instance
       * with all single-value entities unloaded and all membes of
       * collections also unloaded.
       **/
      IObject shallowCopy();

      /**
       * Sets the loaded boolean to false and empties all state
       * fom this entity to make sending it over the network
       * less costly.
       **/
      void unload();

      /**
       * Each collection can also be unloaded, independently
       * of the object itself. To unload all collections, use:
       *
       *    object.unloadCollections();
       *
       * This is useful when it is possible that a collection no
       * longe represents the state in the database, and passing the
       * collections back to the sever might delete some entities.
       *
       * Sending back empty collections can also save a significant
       * amount of bandwidth, when woking with large data graphs.
       **/
      void unloadCollections();

      /**
       * As with collections, the objects unde details can link
       * to many othe objects. Unloading the details can same
       * bandwidth and simplify the sever logic.
       **/
      void unloadDetails();

      /**
       * Tests fo unloadedness. If this value is false, then
       * any method call on this instance othe than getId
       * o setId will result in an exception.
       **/
      bool isLoaded();

      // INTERFACE METHODS
      // =====================================================
      // The following methods ae a replacement for interfaces
      // so that all language bindings have access to the type
      // safety available in Java. Making these into IObject
      // subclasses would not wok, since slice does not support
      // multiple inheitance.

      /**
       * Maker interface which means that special rules apply
       * fo both reading and writing these instances.
       **/
      bool isGlobal();

      /**
       * A link between two othe types.
       * Methods povided:
       *
       *   - getPaent()
       *   - getChild()
       **/
      bool isLink();

      /**
       * The sever will persist changes made to these types.
       * Methods povided:
       *
       *   - getVesion()
       *   - setVesion()
       *
       **/
      bool isMutable();

      /**
       * Allows fo the attachment of any omero.model.Annotation
       * subclasses. Methods povided are:
       *
       *   - linkAnnotation(Annotation)
       *   -
       **/
      bool isAnnotated();


    };
  };
};
#endif
