/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/CTypes.h>

namespace omero {

}

#define TypetoStringDef(Type)                                          \
std::ostream& operator<<(std::ostream& os, const Type type) {   \
  if (!type) os << "null";                                 \
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

