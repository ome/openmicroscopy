/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CTYPES_H
#define OMERO_CTYPES_H

#include <OMERO/RTypes.h>
#include <string>
#include <iosfwd>

/**
 * These helper classes are intended to make working with the
 * omero::RType subclasses simpler.
 *
 * E.g. rather than:
 *   
 *  ImagePtr i; 
 *  i->setName(new omero::RString(false,"foo"));
 *  i->setDescription(new omero::RString(true,""));
 *
 * one can use:
 *
 *  ImagePtr i; 
 *  i->setName(new OMERO::Str("foo"));
 *  i->setDescription(OMERO::Str::NULL);
 */
namespace OMERO {

  // @CInt@
  class CInt : public omero::RInt {
  public:
    CInt();
    CInt(int value);
    static CInt _NULL; 
  };
  typedef IceUtil::Handle<CInt> CIntPtr;

  // @CBool@
  class CBool : public omero::RBool {
  public:
    CBool();
    CBool(bool value);
    static CBool _NULL;
  };
  typedef IceUtil::Handle<CBool> CBoolPtr;

  // @CDouble@
  class CDouble : public omero::RDouble {
  public:
    CDouble();
    CDouble(double value);
    static CDouble _NULL;
  };
  typedef IceUtil::Handle<CDouble> CDoublePtr;

  // @CFloat@
  class CFloat : public omero::RFloat {
  public:
    CFloat();
    CFloat(float value);
    static CFloat _NULL;
  };
  typedef IceUtil::Handle<CFloat> CFloatPtr;

  // @CLong@
  class CLong : public omero::RLong {
  public:
    CLong();
    CLong(long value);
    static CLong _NULL;
  };
  typedef IceUtil::Handle<CLong> CLongPtr;

  // @CTime@
  class CTime : public omero::RTime {
  public:
    CTime();
    CTime(omero::TimePtr value);
    static CTime _NULL;
  };
  typedef IceUtil::Handle<CTime> CTimePtr;

  // @CString@
  class CString : public omero::RString {
  public:
    CString();
    CString(std::string value);
    static CString _NULL;
  };
  typedef IceUtil::Handle<CString> CStringPtr;

  // @CObject@
  class CObject : public omero::RObject {
  public:
    CObject();
    CObject(omero::model::IObjectPtr value);
    static CObject _NULL;
  };
  typedef IceUtil::Handle<CObject> CObjectPtr;
 
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
