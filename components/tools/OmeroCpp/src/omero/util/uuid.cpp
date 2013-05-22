/*
 * UUID abstraction.
 */

#include <omero/util/uuid.h>

#include <IceUtil/Config.h>
#include <IceUtil/UUID.h>

std::string
omero::util::generate_uuid ()
{
  return IceUtil::generateUUID();
}
