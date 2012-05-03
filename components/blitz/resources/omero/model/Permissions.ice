/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef CLASS_PERMISSIONS
#define CLASS_PERMISSIONS

#include <omero/model/IObject.ice>

module omero {

    module model {

      /**
       * Row-level permissions definition available on
       * every OMERO.blitz type. Represents a similar
       * logic to the Unix filesystem.
       **/
    ["protected"] class Permissions
    {

      /**
       * Flag calculated server-side which says that the
       * current user in the current context is not allowed
       * to annotate the current object (e.g. by linking
       * annotations or regions of interest).
       *
       * If this is set to true, then the disallowEdit will
       * also be set to true.
       *
       * The default value (false) will be used for newly
       * created objects stating that no security restrictions
       * are in place.
       **/
      bool disallowAnnotate;

      /**
       * Flag calculated server-side which says that the
       * current user in the current context is not allowed
       * to cross-link (e.g. images and datasets), modify,
       * or delete this data object.
       *
       * If this is set to true, disallowAnnotate may still be
       * false.
       *
       * The default value (false) will be used for newly
       * created objects stating that no security restrictions
       * are in place.
       **/
      bool disallowEdit;

      /**
       * Internal representation. May change!
       * To make working with this object more straight-forward
       * accessors are provided for the perm1 instance though it
       * is protected, though NO GUARANTEES are made on the
       * representation.
       **/
      long perm1;

      /**
       * Do not use!
       **/
      long getPerm1();

      /**
       * Do not use!
       * Throws [omero::ClientError] if mutation not allowed.
       **/
      void setPerm1(long value);

      // Context-based values
      //======================================================

      /**
       * Whether the current user has permissions
       * for annotating this object.
       *
       * The fact that the user has this object in hand
       * already identifies that it's readable.
       **/
      bool canAnnotate();

      /**
       * Whether the current user has the "edit" permissions
       * for this object. This includes changing the values
       * of the object, adding it to data graphs, and even
       * deleting it.
       *
       * The fact that the user has this object in hand
       * already identifies that it's readable.
       **/
      bool canEdit();

      // Row-based values
      //======================================================

      bool isUserRead();
      bool isUserAnnotate();
      bool isUserWrite();
      bool isGroupRead();
      bool isGroupAnnotate();
      bool isGroupWrite();
      bool isWorldRead();
      bool isWorldAnnotate();
      bool isWorldWrite();

      // Mutators
      //======================================================
      // Note: unless you create the permissions object
      // yourself, mutating the state of the object will
      // throw a ClientError

      /**
       * Throws [omero::ClientError] if mutation not allowed.
       **/
      void setUserRead(bool value);

      /**
       * Throws [omero::ClientError] if mutation not allowed.
       **/
      void setUserAnnotate(bool value);

      /**
       * Throws [omero::ClientError] if mutation not allowed.
       **/
      void setUserWrite(bool value);

      /**
       * Throws [omero::ClientError] if mutation not allowed.
       **/
      void setGroupRead(bool value);

      /**
       * Throws [omero::ClientError] if mutation not allowed.
       **/
      void setGroupAnnotate(bool value);

      /**
       * Throws [omero::ClientError] if mutation not allowed.
       **/
      void setGroupWrite(bool value);

      /**
       * Throws [omero::ClientError] if mutation not allowed.
       **/
      void setWorldRead(bool value);

      /**
       * Throws [omero::ClientError] if mutation not allowed.
       **/
      void setWorldAnnotate(bool value);

      /**
       * Throws [omero::ClientError] if mutation not allowed.
       **/
      void setWorldWrite(bool value);

    };
  };
};
#endif 
