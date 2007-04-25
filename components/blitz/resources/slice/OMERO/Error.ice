/*
 *   $Id$
 * 
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_ERROR_ICE
#define OMERO_ERROR_ICE

#include <Glacier2/Session.ice>

module omero
{
    exception ServerError
    {
        string serverStackTrace;
        string serverExceptionClass;
        string message;
    };

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

    
    local exception ClientError
    {
      string message;
    };

    local exception UnloadedEntityException extends ClientError
    {
    };

    local exception UnloadedCollectionException extends ClientError
    {
    };

};

#endif // OMERO_ERROR_ICE