/*
 *   $Id$
 * 
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_ERROR_ICE
#define OMERO_ERROR_ICE

module omero
{
    exception ServerError
    {
        string serverStackTrace;
        string serverExceptionClass;
        string message;
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