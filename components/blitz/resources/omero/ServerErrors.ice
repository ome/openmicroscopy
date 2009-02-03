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
 * Exceptions thrown by OMERO server components. Exceptions thrown client side
 * are available defined in each language binding separately. For more
 * information, see:
 *
 *   http://trac.openmicroscopy.org.uk/omero/wiki/ExceptionHandling
 *
 * OMERO Specific:
 * ===============
 *  ServerError (root server exception)
 *   |
 *   |_ InternalException (server bug)
 *   |
 *   |_ ResourceError (non-recoverable)
 *   |
 *   |_ ConcurrencyException (recoverable) 
 *   |  |_ OptimisticLockException (changed data)
 *   |  |_ TooManyUsersException
 *   |     \_ DatabaseBusyException
 *   |
 *   |_ ApiUsageException (misuse of services)
 *   |   |_ OverUsageException (too much)
 *   |   |_ QueryException
 *   |   \_ ValidationException (bad data)
 *   |
 *   |- SecurityViolation (some no-no)
 *   |
 *   \_SessionException
 *      |_RemovedSessionException (accessing a non-extant session)
 *      |_SessionTimeoutException (session timed out; not yet removed)
 * 
 * However, the Ice runtime also has its own hierarchy (which we subclass in
 * some cases);
 *
 *  Ice Exceptions
 *  ==============
 *  Glacier2::CannotCreateSessionException (only exception throwable by createSession)
 *   |_omero.AuthenticationException 
 *   \_omero.ExpiredCredentialException
 *
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


  /**
   * Programmer error. Ideally should not be thrown.
   */
  exception InternalException extends ServerError
    {
    };

  // RESOURCE

  /**
   * Unrecoverable error. The resource being accessed is not available.
   */
  exception ResourceError extends ServerError
    {
    };

  // CONCURRENCY

  /**
   * Recoverable error caused by simultaneous access of some form.
   */
  exception ConcurrencyException extends ServerError
    {
       long backOff; // Backoff in milliseconds
    };

  /**
   * Too many simultaneous database users.
   */
  exception DatabaseBusyException extends ConcurrencyException
    {
    };
    
  /**
   * Conflicting changes to the same piece of data.
   */
  exception OptimisticLockException extends ConcurrencyException
    {
    };

  // API USAGE

  exception ApiUsageException extends ServerError
    {
    };

  exception OverUsageException extends ApiUsageException
    {
    };
    
  exception QueryException extends ApiUsageException
    {
    };

  exception ValidationException extends ApiUsageException
    {
    };

  // SECURITY

  exception SecurityViolation extends ServerError
    {
    };


};

#endif // OMERO_ERROR_ICE
