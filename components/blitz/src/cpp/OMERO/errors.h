/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <ostream>
#include <iostream>
#include <exception>

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

  class UnloadedEntityException : public ClientError
  {
  public:
    UnloadedEntityException(const char*, int, const char* message);
  };

  class UnloadedCollectionException : public ClientError
  {
  public:
    UnloadedCollectionException(const char*, int, const char* message);
  };
  
}

std::ostream& operator<<(std::ostream&, const omero::ClientError&);
  
