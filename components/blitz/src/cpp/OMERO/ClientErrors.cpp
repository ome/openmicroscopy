/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <OMERO/ClientErrors.h>

namespace omero {

  ClientError::ClientError(const char* file, int line, const char* msg) :
    std::exception(), _line(line), _file(file), _msg(msg){}

  ClientError::~ClientError() throw(){}

  const char* ClientError::name() const throw()
  {
    return "ClientError";
  }

  const char* ClientError::file() const throw()
  {
    return _file;
  }

  const char* ClientError::what() const throw()
  {
    return _msg;
  }

  int ClientError::line() const throw()
  {
    return _line;
  }

  UnloadedEntityException::UnloadedEntityException(const char* file, int line, const char* msg) :
    ClientError(file,line,msg){}

  UnloadedCollectionException::UnloadedCollectionException(const char* file, int line, const char* msg) :
    ClientError(file,line,msg){}

}

std::ostream& operator<<(std::ostream& os, const omero::ClientError& ex) 
{
  os << ex.name() << " in File " << ex.file() << ", line " << ex.line();
  os << std::endl << ex.what() << std::endl;
  return os;
}
