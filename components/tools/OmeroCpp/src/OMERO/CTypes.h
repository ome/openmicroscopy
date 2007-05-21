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
 *  i->setName(new omero::CString("foo"));
 *  i->setDescription(new omero::CString());
 *
 * See OMERO/RTypes.ice for more information.
 */
namespace omero {

  class CInt : public omero::RInt {
  public:
    CInt();
    CInt(int value);
  };
  typedef IceUtil::Handle<CInt> CIntPtr;

  class CBool : public omero::RBool {
  public:
    CBool();
    CBool(bool value);
  };
  typedef IceUtil::Handle<CBool> CBoolPtr;

  class CDouble : public omero::RDouble {
  public:
    CDouble();
    CDouble(double value);
  };
  typedef IceUtil::Handle<CDouble> CDoublePtr;

  class CFloat : public omero::RFloat {
  public:
    CFloat();
    CFloat(float value);
  };
  typedef IceUtil::Handle<CFloat> CFloatPtr;

  class CLong : public omero::RLong {
  public:
    CLong();
    CLong(long value);
  };
  typedef IceUtil::Handle<CLong> CLongPtr;

  class CTime : public omero::RTime {
  public:
    CTime();
    CTime(omero::TimePtr value);
  };
  typedef IceUtil::Handle<CTime> CTimePtr;

  class CString : public omero::RString {
  public:
    CString();
    CString(std::string value);
  };
  typedef IceUtil::Handle<CString> CStringPtr;

  class CClass : public omero::RClass {
  public:
    CClass();
    CClass(std::string value);
  };
  typedef IceUtil::Handle<CString> CStringPtr;

  class CObject : public omero::RObject {
  public:
    CObject();
    CObject(omero::model::IObjectPtr value);
  };
  typedef IceUtil::Handle<CObject> CObjectPtr;

  class CArray : public omero::RArray {
  public:
    CArray();
    CArray(omero::RTypeSeq value);
  };
  typedef IceUtil::Handle<CArray> CArrayPtr;

  class CList : public omero::RList {
  public:
    CList();
    CList(omero::RTypeSeq value);
  };
  typedef IceUtil::Handle<CList> CListPtr;

  class CSet : public omero::RSet {
  public:
    CSet();
    CSet(omero::RTypeSeq value);
  };
  typedef IceUtil::Handle<CSet> CSetPtr;

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
