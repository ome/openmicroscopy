/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <OMERO/CTypes.h>

namespace omero {

  CInt::CInt() : omero::RInt(true,0){}
  CInt::CInt(int value) : omero::RInt(false,value){}

  CBool::CBool() : omero::RBool(true,false){}
  CBool::CBool(bool value) : omero::RBool(false,value){}

  CDouble::CDouble() : omero::RDouble(true,0){}
  CDouble::CDouble(double value) : omero::RDouble(false,value){}

  CFloat::CFloat() : omero::RFloat(true,0){}
  CFloat::CFloat(float value) : omero::RFloat(false,value){}

  CLong::CLong() : omero::RLong(true,0){}
  CLong::CLong(long value) : omero::RLong(false,value){}

  CTime::CTime() : omero::RTime(true,new omero::Time()){}
  CTime::CTime(omero::TimePtr value) : omero::RTime(false,value){}

  CString::CString() : omero::RString(true,0){}
  CString::CString(std::string value) : omero::RString(false,value){}

  CClass::CClass() : omero::RClass(true,0){}
  CClass::CClass(std::string value) : omero::RClass(false,value){}

  CObject::CObject() : omero::RObject(true,0){}
  CObject::CObject(omero::model::IObjectPtr value) : omero::RObject(false,value){}

  CArray::CArray() : omero::RArray(true,omero::RTypeSeq()){}
  CArray::CArray(omero::RTypeSeq value) : omero::RArray(false,value){}

  CList::CList() : omero::RList(true,omero::RTypeSeq()){}
  CList::CList(omero::RTypeSeq value) : omero::RList(false,value){}

  CSet::CSet() : omero::RSet(true,omero::RTypeSeq()){}
  CSet::CSet(omero::RTypeSeq value) : omero::RSet(false,value){}
 
}

#define TypetoStringDef(Type)                                          \
std::ostream& operator<<(std::ostream& os, const Type type) {   \
  if (type->null) os << "null";                                 \
  else os << type->val ;                                        \
  return os; }                                                  \
// 
TypetoStringDef(omero::RIntPtr);
TypetoStringDef(omero::RBoolPtr);
TypetoStringDef(omero::RDoublePtr);
TypetoStringDef(omero::RFloatPtr);
TypetoStringDef(omero::RLongPtr);
TypetoStringDef(omero::RStringPtr);
//These need more work
//TypetoStringDef(omero::RObjectPtr);
//TypetoStringDef(omero::RTimePtr);

