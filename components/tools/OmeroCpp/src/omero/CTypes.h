/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CTYPES_H
#define OMERO_CTYPES_H

#include <omero/RTypes.h>
#include <string>
#include <iosfwd>

/**
 * Previously (before ticket:710) the CTYpe helper classes were intended to make working with the
 * omero::RType subclasses simpler. However, due to garbage collections issues they were
 * sometimes prematurely collected. The previous class definitions have been replaced by typedefs
 * so earlier code will still compile. These typedefs should be considered DEPRECATED.
 *
 * Please use the omero::RTypes directly.
 * See omero/RTypes.ice for more information.
 */
namespace omero {

  typedef RInt CInt;
  typedef RIntPtr CIntPtr;

  typedef RBool CBool;
  typedef RBoolPtr CBoolPtr;

  typedef RDouble CDouble;
  typedef RDoublePtr CDoublePtr;

  typedef RFloat CFloat;
  typedef RFloatPtr CFloatPtr;

  typedef RLong CLong;
  typedef RLongPtr CLongPtr;

  typedef RTime CTime;
  typedef RTimePtr CTimePtr;

  typedef RString CString;
  typedef RStringPtr CStringPtr;

  typedef RObject CObject; 
  typedef RObjectPtr CObjectPtr;

  typedef RArray CArray;
  typedef RArrayPtr CArrayPtr;

  typedef RList CList;
  typedef RListPtr CListPtr;

  typedef RSet CSet;
  typedef RSetPtr CSetPtr;

}

#define TypetoStringDecl(Type)                                          \
std::ostream& operator<<(std::ostream& os, const Type type);
// 
TypetoStringDecl(omero::RIntPtr);
TypetoStringDecl(omero::RBoolPtr);
TypetoStringDecl(omero::RDoublePtr);
TypetoStringDecl(omero::RFloatPtr);
TypetoStringDecl(omero::RLongPtr);
TypetoStringDecl(omero::RStringPtr);
//These need more work
//TypetoStringDecl(omero::RObjectPtr);
//TypetoStringDecl(omero::RTimePtr);

#endif // OMERO_CTYPES_H
