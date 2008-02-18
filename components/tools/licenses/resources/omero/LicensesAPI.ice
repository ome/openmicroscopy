/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef LicensesAPI
#define LicensesAPI

#include <Ice/BuiltinSequences.ice>
#include <omero/ServerErrors.ice>

module omero {
  module constants {

    const string LICENSESERVICE = "omero.licenses.ILicense";

  };
  module licenses {

    exception NoAvailableLicenseException extends omero::SessionCreationException {

    };

    interface ILicense
    {
      Ice::ByteSeq acquireLicense() throws NoAvailableLicenseException, ServerError;
      long getAvailableLicenseCount() throws ServerError;
      long getLicenseTimeout() throws ServerError;
      long getTotalLicenseCount() throws ServerError;
      bool releaseLicense(Ice::ByteSeq token) throws ServerError;
      void resetLicenses() throws ServerError;
    };

  };
};

#endif // LicensesAPI
