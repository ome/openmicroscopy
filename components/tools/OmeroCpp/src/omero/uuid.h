/*
 * UUID abstraction.
 */

#ifndef OMERO_UUID_H
#define OMERO_UUID_H

#include <string>

namespace omero
{

  /**
   * Generate a universally unique identifier (UUID).
   *
   * @returns a UUID as a string.
   */
  std::string
  generate_uuid ();

}

#endif // OMERO_UUID_H
