/*
 * UUID abstraction.
 */

#ifndef OMERO_UUID_H
#define OMERO_UUID_H

#include <omero/IceNoWarnPush.h>
#include <Ice/Ice.h>
#include <omero/IceNoWarnPop.h>
#include <string>

#ifndef OMERO_CLIENT
#   ifdef OMERO_CLIENT_EXPORTS
#       define OMERO_CLIENT ICE_DECLSPEC_EXPORT
#   else
#       define OMERO_CLIENT ICE_DECLSPEC_IMPORT
#   endif
#endif

namespace omero
{
  namespace util
  {

    /**
     * Generate a universally unique identifier (UUID).
     *
     * @returns a UUID as a string.
     */
    OMERO_CLIENT std::string
    generate_uuid ();

  }
}

#endif // OMERO_UUID_H
