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
 * http://trac.openmicroscopy.org.uk/omero/wiki/ExceptionHandling
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


  // SESSION EXCEPTIONS --------------------------------

  /**
   * Base session exception, though in the OMERO.blitz
   * implementation, all exceptions thrown by the Glacier2
   * must subclass CannotCreateSessionException. See below.
   */
  exception SessionException extends ServerError
    {

    };

  /**
   * Session has been removed. Either it was closed, or it
   * timed out and one "SessionTimeoutException" has already
   * been thrown.
   */
  exception RemovedSessionException extends SessionException
    {

    };

  /**
   * Session has timed out and will be removed.
   */
  exception SessionTimeoutException extends SessionException
    {

    };


  // SESSION EXCEPTIONS (Glacier2) ---------------------

  /**
   * Thrown when the information provided omero.createSession() or more
   * specifically Glacier2.RouterPrx.createSession() is incorrect. This
   * does -not- subclass from the omero.ServerError class because the
   * Ice Glacier2::SessionManager interface can only throw CCSEs.
   */
  exception AuthenticationException extends Glacier2::CannotCreateSessionException
    {

    };

  /**
   * Thrown when the password for a user has expried. Use: ISession.changeExpiredCredentials()
   * and login as guest. This does -not- subclass from the omero.ServerError class because the
   * Ice Glacier2::SessionManager interface can only throw CCSEs.
   */
  exception ExpiredCredentialException extends Glacier2::CannotCreateSessionException
    {

    };


  // OTHER SERVER EXCEPTIONS ------------------------------


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


  // DEPRECATED EXCEPTIONS


  /*
   * OMERO.blitz specific exception not thrown by OMERO.server.
   * Deprecated. This was a possible solution from before the
   * implementation of OmeroSessions. Now, omero.createSession()
   * will throw SessionException-specific subclasses. See above.
   */
  exception SessionCreationException extends Glacier2::CannotCreateSessionException
    {
    };

};

#endif // OMERO_ERROR_ICE
