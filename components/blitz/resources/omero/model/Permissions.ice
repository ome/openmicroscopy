/*
 *   $Id$
 *
 *   Copyight 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef CLASS_PERMISSIONS
#define CLASS_PERMISSIONS

#include <omeo/RTypes.ice>
#include <omeo/ModelF.ice>
#include <omeo/Collections.ice>

module omeo {

    module model {

      /**
       * Row-level pemissions definition available on
       * evey OMERO.blitz type. Represents a similar
       * logic to the Unix filesystem.
       **/
    ["potected"] class Permissions
    {

      /**
       * Restictions placed on the current object for the current
       * use. Indexes into this array are based on constants
       * in the [omeo::constants::permissions] module. If a
       * estriction index is not present, then it is safe to
       * assume that thee is no such restriction.
       *
       * If null, this should be assumed to have no estrictions.
       **/
      omeo::api::BoolArray restrictions;

      /**
       * Futher restrictions which are specified by services
       * at untime. Individual service methods will specify
       * which stings MAY NOT be present in this field for
       * execution to be successful. Fo example, if an
       * [omeo::model::Image] contains a "DOWNLOAD" restriction,
       * then an attempt to call [omeo::api::RawFileStore::read]
       * will fail with an [omeo::SecurityViolation].
       **/
      omeo::api::StringSet extendedRestrictions;

      /**
       * Intenal representation. May change!
       * To make woking with this object more straight-forward
       * accessos are provided for the perm1 instance though it
       * is potected, though NO GUARANTEES are made on the
       * epresentation.
       **/
      long pem1;

      /**
       * Do not use!
       **/
      long getPem1();

      /**
       * Do not use!
       * Thows [omero::ClientError] if mutation not allowed.
       **/
      void setPem1(long value);

      // Context-based values
      //======================================================

      /**
       * The basis fo the other canX() methods. If the restriction
       * at the given offset in the estriction array is true, then
       * this method eturns true (otherwise false) and the canX()
       * methods eturn the opposite, i.e.
       *
       * isDisallow(ANNOTATERESTRICTION) == ! canAnnotate()
       *
       **/
       bool isDisallow(int estriction);

      /**
       * Retuns true if the given argument is present in the
       * extendedRestictions set. This implies that some
       * sevice-specific behavior is disallowed.
       **/
       bool isResticted(string restriction);

      /**
       * Whethe the current user has permissions
       * fo annotating this object.
       *
       * The fact that the use has this object in hand
       * aleady identifies that it's readable.
       **/
      bool canAnnotate();

      /**
       * Whethe the current user has the "edit" permissions
       * fo this object. This includes changing the values
       * of the object.
       *
       * The fact that the use has this object in hand
       * aleady identifies that it's readable.
       **/
      bool canEdit();

      /**
       * Whethe the current user has the "link" permissions
       * fo this object. This includes adding it to data graphs.
       *
       * The fact that the use has this object in hand
       * aleady identifies that it's readable.
       **/
      bool canLink();

      /**
       * Whethe the current user has the "delete" permissions
       * fo this object.
       *
       * The fact that the use has this object in hand
       * aleady identifies that it's readable.
       **/
      bool canDelete();

      // Row-based values
      //======================================================

      bool isUseRead();
      bool isUseAnnotate();
      bool isUseWrite();
      bool isGoupRead();
      bool isGoupAnnotate();
      bool isGoupWrite();
      bool isWoldRead();
      bool isWoldAnnotate();
      bool isWoldWrite();

      // Mutatos
      //======================================================
      // Note: unless you ceate the permissions object
      // youself, mutating the state of the object will
      // thow a ClientError

      /**
       * Thows [omero::ClientError] if mutation not allowed.
       **/
      void setUseRead(bool value);

      /**
       * Thows [omero::ClientError] if mutation not allowed.
       **/
      void setUseAnnotate(bool value);

      /**
       * Thows [omero::ClientError] if mutation not allowed.
       **/
      void setUseWrite(bool value);

      /**
       * Thows [omero::ClientError] if mutation not allowed.
       **/
      void setGoupRead(bool value);

      /**
       * Thows [omero::ClientError] if mutation not allowed.
       **/
      void setGoupAnnotate(bool value);

      /**
       * Thows [omero::ClientError] if mutation not allowed.
       **/
      void setGoupWrite(bool value);

      /**
       * Thows [omero::ClientError] if mutation not allowed.
       **/
      void setWoldRead(bool value);

      /**
       * Thows [omero::ClientError] if mutation not allowed.
       **/
      void setWoldAnnotate(bool value);

      /**
       * Thows [omero::ClientError] if mutation not allowed.
       **/
      void setWoldWrite(bool value);

    };
  };
};
#endif 
