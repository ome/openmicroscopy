/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef CLASS_PERMISSIONS
#define CLASS_PERMISSIONS

#include <omero/RTypes.ice>
#include <omero/ModelF.ice>
#include <omero/Collections.ice>

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
       * Restrictions placed on the current object for the current
       * user. Indexes into this array are based on constants
       * in the [omero::constants::permissions] module. If a
       * restriction index is not present, then it is safe to
       * assume that there is no such restriction.
       *
       * If null, this should be assumed to have no restrictions.
       **/
      omero::api::BoolArray restrictions;

      /**
       * Further restrictions which are specified by services
       * at runtime. Individual service methods will specify
       * which strings MAY NOT be present in this field for
       * execution to be successful. For example, if an
       * [omero::model::Image] contains a "DOWNLOAD" restriction,
       * then an attempt to call [omero::api::RawFileStore::read]
       * will fail with an [omero::SecurityViolation].
       **/
      omero::api::StringSet extendedRestrictions;

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
       * The basis for the other canX() methods. If the restriction
       * at the given offset in the restriction array is true, then
       * this method returns true (otherwise false) and the canX()
       * methods return the opposite, i.e.
       *
       * isDisallow(ANNOTATERESTRICTION) == ! canAnnotate()
       *
       **/
       bool isDisallow(int restriction);

      /**
       * Returns true if the given argument is present in the
       * extendedRestrictions set. This implies that some
       * service-specific behavior is disallowed.
       **/
       bool isRestricted(string restriction);

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
       * of the object.
       *
       * The fact that the user has this object in hand
       * already identifies that it's readable.
       **/
      bool canEdit();

      /**
       * Whether the current user has the "link" permissions
       * for this object. This includes adding it to data graphs.
       *
       * The fact that the user has this object in hand
       * already identifies that it's readable.
       **/
      bool canLink();

      /**
       * Whether the current user has the "delete" permissions
       * for this object.
       *
       * The fact that the user has this object in hand
       * already identifies that it's readable.
       **/
      bool canDelete();

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
