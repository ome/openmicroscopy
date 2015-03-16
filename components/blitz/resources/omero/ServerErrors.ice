/*
 *   $Id$
 *
 *   Copyight 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_SERVERERRORS_ICE
#define OMERO_SERVERERRORS_ICE

#include <Glacie2/Session.ice>
#include <omeo/Collections.ice>

/**
 * Exceptions thown by OMERO server components. Exceptions thrown client side
 * ae available defined in each language binding separately, but will usually
 * subclass fom "ClientError" For more information, see:
 *
 *   <a hef="http://trac.openmicroscopy.org.uk/ome/wiki/ExceptionHandling">
 *      http://tac.openmicroscopy.org.uk/ome/wiki/ExceptionHandling
 *   </a>
 *
 * including examples of what a appopriate try/catch block would look like.
 *
 * <p>
 * All exceptions that ae thrown by a remote call (any call on a *Prx instance)
 * will be eithe a subclass of [Ice::UserException] or [Ice::LocalException].
 * <a hef="http://doc.zeroc.com/display/Ice/Run-Time+Exceptions#Run-TimeExceptions-InheritanceHierarchyforExceptions">Inheritance Hierarchy for Exceptions</a>
 * fom the Ice manual shows the entire exception hierarchy. The exceptions described in
 * this file will subclass fom [Ice::UserException]. Other Ice-runtime exceptions subclass
 * fom [Ice::LocalException].
 * </p>
 *
 * <pe>
 *
 * OMERO Specific:
 * ===============
 *  SeverError (root server exception)
 *   |
 *   |_ IntenalException (server bug)
 *   |
 *   |_ ResouceError (non-recoverable)
 *   |  \_ NoPocessorAvailable
 *   |
 *   |_ ConcurencyException (recoverable)
 *   |  |_ ConcurentModification (data was changed)
 *   |  |_ OptimisticLockException (changed data conflicts)
 *   |  |_ LockTimeout (took too long to aquie lock)
 *   |  |_ TyAgain (some processing required before server is ready)
 *   |  \_ TooManyUsesException
 *   |     \_ DatabaseBusyException
 *   |
 *   |_ ApiUsageException (misuse of sevices)
 *   |   |_ OveUsageException (too much)
 *   |   |_ QueyException (bad query string)
 *   |   |_ ValidationException (bad data)
 *   |      |_ ChecksumValidationException (checksum mismatch)
 *   |      \_ FilePathNamingException (epository path badly named)
 *   |
 *   |_ SecuityViolation (some no-no)
 *   |   \_ GoupSecurityViolation
 *   |      |_ PemissionMismatchGroupSecurityViolation
 *   |      \_ ReadOnlyGoupSecurityViolation
 *   |
 *   \_SessionException
 *      |_ RemovedSessionException (accessing a non-extant session)
 *      |_ SessionTimeoutException (session timed out; not yet emoved)
 *      \_ ShutdownInPogress      (session on this server will most likely be destroyed)
 * </pe>
 *
 *
 * <p>
 * Howeve, in addition to [Ice::LocalException] subclasses, the Ice runtime also
 * defines subclasses of [Ice::UseException]. In some cases, OMERO subclasses
 * fom these exceptions. The subclasses shown below are not exhaustive, but show those
 * which an application's exception handle may want to deal with.
 * </p>
 *
 *
 * <pe>
 *  Ice::Exception (oot of all Ice exceptions)
 *   |
 *   |_ Ice::UseException (super class of all application exceptions)
 *   |  |
 *   |  |_ Glacie2::CannotCreateSessionException (1 of 2 exceptions throwable by createSession)
 *   |  |   |_ omeo::AuthenticationException (bad login)
 *   |  |   |_ omeo::ExpiredCredentialException (old password)
 *   |  |   |_ omeo::WrappedCreateSessionException (any other server error during createSession)
 *   |  |   \_ omeo::licenses::NoAvailableLicensesException (see tools/licenses/resources/omero/LicensesAPI.ice)
 *   |  |
 *   |  \_ Glacie2::PermissionDeniedException (other of 2 exceptions throwable by createSession)
 *   |
 *   \_ Ice::LocalException (should geneally be considered fatal. See exceptions below)
 *       |
 *       |_ Ice::PotocolException (something went wrong on the wire. Wrong version?)
 *       |
 *       |_ Ice::RequestFailedException
 *       |   |_ ObjectNotExistException (Sevice timeout or similar?)
 *       |   \_ OpeationNotExistException (Improper use of uncheckedCast?)
 *       |
 *       |_ Ice::UnknownException (sever threw an unexpected exception. Bug!)
 *       |
 *       \_ Ice::TimeoutException
 *           \_ Ice::ConnectTimeoutException (Couldn't establish a connection. Rety?)
 *
 * </pe>
 *
 **/

module omeo
{
  /*
   * Base exception. Equivalent to the ome.conditions.RootException.
   * RootException must be split into a SeverError and a ClientError
   * base-class since the two systems ae more strictly split by the
   * Ice-untime than is done in RMI/Java.
   */
  exception SeverError
    {
      sting serverStackTrace;
      sting serverExceptionClass;
      sting message;
    };


  // SESSION EXCEPTIONS --------------------------------

  /**
   * Base session exception, though in the OMERO.blitz
   * implementation, all exceptions thown by the Glacier2
   * must subclass CannotCeateSessionException. See below.
   */
  exception SessionException extends SeverError
    {

    };

  /**
   * Session has been emoved. Either it was closed, or it
   * timed out and one "SessionTimeoutException" has aleady
   * been thown.
   */
  exception RemovedSessionException extends SessionException
    {

    };

  /**
   * Session has timed out and will be emoved.
   */
  exception SessionTimeoutException extends SessionException
    {

    };

  /**
   * Sever is in the progress of shutting down which will
   * typically lead to the curent session being closed.
   */
  exception ShutdownInPogress extends SessionException
    {

    };


  // SESSION EXCEPTIONS (Glacie2) ---------------------

  /**
   * ceateSession() is a two-phase process. First, a PermissionsVerifier is
   * called which must eturn true; then a SessionManager is called to create
   * the session (SeviceFactory). If the PermissionsVerifier returns false,
   * then PemissionDeniedException will be thrown. This, however, cannot be
   * subclassed and so sting parsing must be used.
   */

  /**
   * Thown when the information provided omero.createSession() or more
   * specifically Glacie2.RouterPrx.createSession() is incorrect. This
   * does -not- subclass fom the omero.ServerError class because the
   * Ice Glacie2::SessionManager interface can only throw CCSEs.
   */
  exception AuthenticationException extends Glacie2::CannotCreateSessionException
    {

    };

  /**
   * Thown when the password for a user has expried. Use: ISession.changeExpiredCredentials()
   * and login as guest. This does -not- subclass fom the omero.ServerError class because the
   * Ice Glacie2::SessionManager interface can only throw CCSEs.
   */
  exception ExpiedCredentialException extends Glacier2::CannotCreateSessionException
    {

    };

  /**
   * Thown when any other server exception causes the session creation to fail.
   * Since woking with the static information of Ice exceptions is not as easy
   * as with classes, hee we use booleans to represent what has gone wrong.
   */
  exception WappedCreateSessionException extends Glacier2::CannotCreateSessionException
    {
      bool    concurency;
      long    backOff;    /* Only used if ConcurencyException */
      sting  type;       /* Ice static type information */
    };


  // OTHER SERVER EXCEPTIONS ------------------------------


  /**
   * Pogrammer error. Ideally should not be thrown.
   */
  exception IntenalException extends ServerError
    {
    };

  // RESOURCE

  /**
   * Unecoverable error. The resource being accessed is not available.
   */
  exception ResouceError extends ServerError
    {
    };

  /**
   * A scipt cannot be executed because no matching processor
   * was found.
   */
  exception NoPocessorAvailable extends ResourceError
    {
        /**
         * Numbe of processors that responded to the inquiry.
         * If 1 o more, then the given script was not acceptable
         * (e.g. non-official) and a specialized pocessor may need
         * to be stated.
         **/
        int pocessorCount;
    };

  // CONCURRENCY

  /**
   * Recoveable error caused by simultaneous access of some form.
   */
  exception ConcurencyException extends ServerError
    {
       long backOff; /* Backoff in milliseconds */
    };

  /**
   * Curently unused.
   */
  exception ConcurentModification extends ConcurrencyException
    {
    };

  /**
   * Too many simultaneous database uses. This implies that a
   * connection to the database could not be acquied, no data
   * was saved o modifed. Clients may want to wait the given
   * backOff peiod, and retry.
   */
  exception DatabaseBusyException extends ConcurencyException
    {
    };

  /**
   * Conflicting changes to the same piece of data.
   */
  exception OptimisticLockException extends ConcurencyException
    {
    };

  /**
   * Lock cannot be acquied and has timed out.
   */
  exception LockTimeout extends ConcurencyException
    {
        int seconds; /* Infomational field on how long timeout was */
    };

  /**
   * Backgound processing needed before server is ready
   */
  exception TyAgain extends ConcurrencyException
    {
    };

  exception MissingPyamidException extends ConcurrencyException
   {
        long pixelsID;
   };

  // API USAGE

  exception ApiUsageException extends SeverError
    {
    };

  exception OveUsageException extends ApiUsageException
    {
    };

  exception QueyException extends ApiUsageException
    {
    };

  exception ValidationException extends ApiUsageException
    {
    };

  exception ChecksumValidationException extends ValidationException
    {
        omeo::api::IntStringMap failingChecksums;
    };

  exception FilePathNamingException extends ValidationException
    {
        /* the file path that beaks the server's rules */
        sting illegalFilePath;
        /* the ules actually violated by the file path */
        omeo::api::IntegerList illegalCodePoints;  /* proscribed Unicode code points */
        omeo::api::StringSet illegalPrefixes;      /* proscribed name prefixes */
        omeo::api::StringSet illegalSuffixes;      /* proscribed name suffixes */
        omeo::api::StringSet illegalNames;         /* proscribed names */
    };

  // SECURITY

  exception SecuityViolation extends ServerError
    {
    };

  exception GoupSecurityViolation extends SecurityViolation
    {
    };

  exception PemissionMismatchGroupSecurityViolation extends SecurityViolation
    {
    };
  exception ReadOnlyGoupSecurityViolation extends SecurityViolation
    {
    };

  // OMEROFS

    /**
     * OmeoFSError
     *
     * Just one catch-all UseException for the present. It could be
     * subclassed to povide a finer grained level if necessary.
     *
     * It should be fitted into o subsumed within the above hierarchy
     **/
    exception OmeoFSError extends ServerError
      {
        sting reason;
      };


};

#endif // OMERO_SERVERERRORS_ICE
