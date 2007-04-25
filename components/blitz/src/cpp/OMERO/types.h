/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_types_h
#define OMERO_types_h

#include <RTypes.h>
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
    CInt() : omero::RInt(true,0){}
    CInt(int value) : omero::RInt(false,value){}
    static CInt _NULL; 
  };
  typedef IceUtil::Handle<CInt> CIntPtr;

  // @CBool@
  class CBool : public omero::RBool {
  public:
    CBool() : omero::RBool(true,false){}
    CBool(bool value) : omero::RBool(false,value){}
    static CBool _NULL;
  };
  typedef IceUtil::Handle<CBool> CBoolPtr;

  // @CDouble@
  class CDouble : public omero::RDouble {
  public:
    CDouble() : omero::RDouble(true,0){}
    CDouble(double value) : omero::RDouble(false,value){}
    static CDouble _NULL;
  };
  typedef IceUtil::Handle<CDouble> CDoublePtr;

  // @CFloat@
  class CFloat : public omero::RFloat {
  public:
    CFloat() : omero::RFloat(true,0){}
    CFloat(float value) : omero::RFloat(false,value){}
    static CFloat _NULL;
  };
  typedef IceUtil::Handle<CFloat> CFloatPtr;

  // @CLong@
  class CLong : public omero::RLong {
  public:
    CLong() : omero::RLong(true,0){}
    CLong(long value) : omero::RLong(false,value){}
    static CLong _NULL;
  };
  typedef IceUtil::Handle<CLong> CLongPtr;

  // @CTime@
  class CTime : public omero::RTime {
  public:
    CTime() : omero::RTime(true,new omero::Time()){}
    CTime(omero::TimePtr value) : omero::RTime(false,value){}
    static CTime _NULL;
  };
  typedef IceUtil::Handle<CTime> CTimePtr;

  // @CString@
  class CString : public omero::RString {
  public:
    CString() : omero::RString(true,0){}
    CString(std::string value) : omero::RString(false,value){}
    static CString _NULL;
  };
  typedef IceUtil::Handle<CString> CStringPtr;

  // @CObject@
  class CObject : public omero::RObject {
  public:
    CObject() : omero::RObject(true,0){}
    CObject(omero::model::IObjectPtr value) : omero::RObject(false,value){}
    static CObject _NULL;
  };
  typedef IceUtil::Handle<CObject> CObjectPtr;
 
}

#define toString(Type)                                          \
std::ostream& operator<<(std::ostream& os, const Type type) {   \
  if (type->null) os << "null";                                 \
  else os << type->val ;                                        \
  return os; }                                                  \
// 
toString(omero::RIntPtr);
toString(omero::RBoolPtr);
toString(omero::RDoublePtr);
toString(omero::RFloatPtr);
toString(omero::RLongPtr);
toString(omero::RStringPtr);
//These need more work
//toString(omero::RObjectPtr);
//toString(omero::RTimePtr);

#endif // OMERO_types_h
