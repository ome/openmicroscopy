/*
 * Windows portability fixes.
 */

#ifndef OMERO_INTERNAL_FIXES_H
#define OMERO_INTERNAL_FIXES_H

#ifdef _MSC_VER
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#undef PASCAL
#endif

#endif // OMERO_INTERNAL_FIXES_H
