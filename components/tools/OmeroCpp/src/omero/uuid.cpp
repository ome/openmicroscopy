/*
 * UUID abstraction.
 */

#include <omero/uuid.h>

#include <IceUtil/Config.h>
#if ICE_INT_VERSION / 100 >= 305
#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_generators.hpp>
#include <boost/uuid/uuid_io.hpp>
#else
#include <IceUtil/UUID.h>
#endif

std::string
generate_uuid ()
{
#if ICE_INT_VERSION / 100 >= 305
  static boost::uuids::random_generator uuidgen;
  return boost::uuids::to_string(uuidgen());
#else
  return IceUtil::generateUUID();
#endif
}
