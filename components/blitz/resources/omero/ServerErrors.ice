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
 *   |  |_ ConcurrentModification (data was changed)
 *   |  |_ OptimisticLockException (changed data conflicts)
 *   |  |_ LockTimeout (took too long to aquire lock)
 *   |  |_ TryAgain (took too long to aquire lock)
 *   |  \_ TooManyUsersException
 *   |     \_ DatabaseBusyException
 *   |
 *   |_ ApiUsageException (misuse of services)
 *   |   |_ OverUsageException (too much)
 *   |   |_ QueryException
 *   |   \_ ValidationException (bad data)
 *   |
 *   |_ SecurityViolation (some no-no)
 *   |   \_ GroupSecurityViolation
 *   |
 *   \_SessionException
 *      |_ RemovedSessionException (accessing a non-extant session)
 *      |_ SessionTimeoutException (session timed out; not yet removed)
 *      \_ ShutdownInProgress      (session on this server will most likely be destroyed)
 *
 *
 * However, the Ice runtime also has its own hierarchy (which we subclass in
 * some cases). The shown subclasses below are not exhaustive, but show those
 * which an application's exception handler may want to deal with.
 *
 *
 *  Ice::Exception
 *   |
 *   |_ Ice::UserException (super class of all application exceptions)
 *   |  |
 *   |  |_ Glacier2::CannotCreateSessionException (1 of 2 exceptions throwable by createSession)
 *   |  |   |_ omero::AuthenticationException (bad login)
 *   |  |   |_ omero::ExpiredCredentialException (old password)
 *   |  |   |_ omero::WrappedCreateSessionException (any other server error during createSession)
 *   |  |   \_ omero::licenses::NoAvailableLicensesException (see tools/licenses/resources/omero/LicensesAPI.ice)
 *   |  |
 *   |  \_ Glacier2::PermissionDeniedException (other of 2 exceptions throwable by createSession)
 *   |
 *   \_ Ice::LocalException (should generally be considered fatal. See exceptions below)
 *       |
 *       |_ Ice::ProtocolException (something went wrong on the wire. Wrong version?)
 *       |
 *       |_ Ice::RequestFailedException
 *       |   |_ ObjectNotExistException (Service timeout or similar?)
 *       |   \_ OperationNotExistException (Improper use of uncheckedCast?)
 *       |
 *       |_ Ice::UknownException (server threw an unexpected exception. Bug!)
 *       |
 *       \_ Ice::TimeoutException
 *           \_ Ice::ConnectTimeoutException (Couldn't establish a connection. Retry?)
 *
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

  /**
   * Server is in the progress of shutting down which will
   * typically lead to the current session being closed.
   */
  exception ShutdownInProgress extends SessionException
    {

    };


  // SESSION EXCEPTIONS (Glacier2) ---------------------

  /**
   * createSession() is a two-phase process. First, a PermissionsVerifier is
   * called which must return true; then a SessionManager is called to create
   * the session (ServiceFactory). If the PermissionsVerifier returns false,
   * then PermissionDeniedException will be thrown. This, however, cannot be
   * subclassed and so string parsing must be used.
   */

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

  /**
   * Thrown when any other server exception causes the session creation to fail.
   * Since working with the static information of Ice exceptions is not as easy
   * as with classes, here we use booleans to represent what has gone wrong.
   */
  exception WrappedCreateSessionException extends Glacier2::CannotCreateSessionException
    {
      bool    concurrency;
      long    backOff;    // Only used if ConcurrencyException
      string  type;       // Ice static type information
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
  exception ConcurrentModification extends ConcurrencyException
    {
    };

  /**
   * Too many simultaneous database users. This implies that a
   * connection to the database could not be acquired, no data
   * was saved or modifed. Clients may want to wait the given
   * backOff period, and retry.
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

  /**
   * Lock cannot be acquired and has timed out.
   */
  exception LockTimeout extends ConcurrencyException
    {
        int seconds; // Informational field on how long timeout was
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

  exception GroupSecurityViolation extends ServerError
    {
    };

  // OMEROFS

    /**
     * OmeroFSError
     *
     * Just one catch-all UserException for the present. It could be
     * subclassed to provide a finer grained level if necessary.
     *
     * It should be fitted into or subsumed within the above hierarchy
     **/
    exception OmeroFSError extends ServerError
      {
        string reason;
      };


};

#endif // OMERO_ERROR_ICE
