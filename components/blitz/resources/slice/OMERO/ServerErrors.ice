/*
 *   $Id$
 * 
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_ERROR_ICE
#define OMERO_ERROR_ICE

#include <Glacier2/Session.ice>

/*
 * Exceptions thrown by OMERO.blitz. These model closely the 
 * exceptions of OMERO.server. See the JavaDoc for the ome.conditions
 * package or: 
 *
 * http://cvs.openmicroscopy.org.uk/tiki/tiki-index.php?page=Omero+Exception+Handling
 */
module omero
{
  /*
   * Base exception. Equivalent to the ome.conditions.RootException.
   * RootException must be split into a ServerError and a ClientError
   * base-class since the two systems are more strictly split by the
   * Ice-runtime than is done in RMI/Java.
   */
  exception ServerError
    {
      string serverStackTrace;
      string serverExceptionClass;
      string message;
    };

  /*
   * OMERO.blitz specific exception not thrown by OMERO.server. 
   */
  exception SessionCreationException extends Glacier2::CannotCreateSessionException
    {
    };
    
  exception InternalException extends ServerError
    {
    };
    
  exception ResourceError extends ServerError
    {
    };
    
  exception DataAccessException extends ServerError
    {
    };
    
  exception OutOfServiceException extends ServerError
    {
    };
    
  exception SecurityViolation extends DataAccessException
    {
    };
    
  exception OptimisticLockException extends DataAccessException
    {
    };
    
  exception ApiUsageException extends DataAccessException
    {
    };
    
  exception ValidationException extends ApiUsageException
    {
    };

};

#endif // OMERO_ERROR_ICE
