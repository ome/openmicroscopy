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
  class CInt : omero::RInt {
  public:
    CInt() : omero::RInt(true,0){}
    CInt(int value) : omero::RInt(false,value){}
    static Cint NULL; 
  }
  typedef IceUtil::Handle<CInt> CIntPtr;

  // @CBool@
  class CBool : omero::RBool {
  public:
    CBool() : omero::RBool(true,false){}
    CBool(bool value) : omero::RBool(false,value){}
    static CBool NULL;
  }
  typedef IceUtil::Handle<CBool> CBoolPtr;

  // @CDouble@
  class CDouble : omero::RDouble {
  public:
    CDouble() : omero::RDouble(true,0){}
    CDouble(double value) : omero::RDouble(false,value){}
    static CDouble NULL;
  }
  typedef IceUtil::Handle<CDouble> CDoublePtr;

  // @CFloat@
  class CFloat : omero::RFloat {
  public:
    CFloat() : omero::RFloat(true,0){}
    CFloat(float value) : omero::RFloat(false,value){}
    static CFloat NULL;
  }
  typedef IceUtil::Handle<CFloat> CFloatPtr;

  // @CLong@
  class CLong : omero::RLong {
  public:
    CLong() : omero::RLong(true,0){}
    CLong(long value) : omero::RLong(false,value){}
    static CLong NULL;
  }
  typedef IceUtil::Handle<CLong> CLongPtr;

  // @CTime@
  class CTime : omero::RTime {
  public:
    CTime() : omero::RTime(true,omero::Time()){}
    CTime(omero::Time value) : omero::RTime(false,value){}
    static CTime NULL;
  }
  typedef IceUtil::Handle<CTime> CTimePtr;

  // @CString@
  class CString : omero::RString {
  public:
    CString() : omero::RString(true,0){}
    CString(string value) : omero::RString(false,value){}
    static CString NULL;
  }
  typedef IceUtil::Handle<CString> CStringPtr;

  // @CObject@
  class CObject : omero::RObject {
  public:
    CObject() : omero::RObject(true,0){}
    CObject(int value) : omero::RObject(false,value){}
    static CObject NULL;
  }
  typedef IceUtil::Handle<CObject> CObjectPtr;
 
}

#end // OMERO_types_h
