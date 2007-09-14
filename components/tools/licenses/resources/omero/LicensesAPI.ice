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
      Ice::ByteSeq acquireLicense() throws NoAvailableLicenseException;
      long getAvailableLicenseCount();
      long getLicenseTimeout();
      long getTotalLicenseCount();
      bool releaseLicense(Ice::ByteSeq token);
      void resetLicenses() throws ServerError;
    };

  };
};

#endif // LicensesAPI
