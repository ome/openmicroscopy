/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CLIENT_ERRORS_H
#define OMERO_CLIENT_ERRORS_H

#include <ostream>
#include <iostream>
#include <exception>

/*
 * Client-side exceptions thrown, especially by the generated
 * model entities. 
 */
namespace omero {

  class ClientError : public std::exception
  {
  protected:
    int _line;
    const char* _file;
    const char* _msg;
  public:
    ClientError(const char*, int, const char* message);
    virtual ~ClientError() throw();
    virtual const char* name() const throw();
    virtual const char* file() const throw();
    virtual int line() const throw();
    virtual char const* what() const throw();
  };

  /*
   * Thrown if an object is unloaded (see loaded field) and any 
   * method which is expecting valid state is called. (The id 
   * of an unloaded object will always be sent by the server.)
   */
  class UnloadedEntityException : public ClientError
  {
  public:
    UnloadedEntityException(const char*, int, const char* message);
  };

  /*
   * Thrown if a collection is unloaded (see collectionNameLoaded fields)
   * and any method which is expecting a valid collection is called.
   */
  class UnloadedCollectionException : public ClientError
  {
  public:
    UnloadedCollectionException(const char*, int, const char* message);
  };

}

std::ostream& operator<<(std::ostream&, const omero::ClientError&);

#endif // OMERO_CLIENT_ERRORS_H
