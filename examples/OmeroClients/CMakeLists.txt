# #%L
# OMERO C++ libraries (cmake build infrastructure)
# %%
# Copyright © 2006 - 2013 Open Microscopy Environment:
#   - Massachusetts Institute of Technology
#   - National Institutes of Health
#   - University of Dundee
#   - Board of Regents of the University of Wisconsin-Madison
#   - Glencoe Software, Inc.
# %%
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# 1. Redistributions of source code must retain the above copyright notice,
#    this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
#
# The views and conclusions contained in the software and documentation are
# those of the authors and should not be interpreted as representing official
# policies, either expressed or implied, of any organization.
# #L%

add_executable(clientpointer clientpointer.cpp)
target_link_libraries(clientpointer omero-client)

add_executable(collectionmethods collectionmethods.cpp)
target_link_libraries(collectionmethods omero-client)

add_executable(configuration configuration.cpp)
target_link_libraries(configuration omero-client)

add_executable(constants constants.cpp)
target_link_libraries(constants omero-client)

add_executable(constructors constructors.cpp)
target_link_libraries(constructors omero-client)

add_executable(details details.cpp)
target_link_libraries(details omero-client)

add_executable(enumerations enumerations.cpp)
target_link_libraries(enumerations omero-client)

add_executable(iterators iterators.cpp)
target_link_libraries(iterators omero-client)

add_executable(primitives primitives.cpp)
target_link_libraries(primitives omero-client)

add_executable(queries queries.cpp)
target_link_libraries(queries omero-client)

add_executable(rcollection rcollection.cpp)
target_link_libraries(rcollection omero-client)

add_executable(smartpointers smartpointers.cpp)
target_link_libraries(smartpointers omero-client)

add_executable(staticfields staticfields.cpp)
target_link_libraries(staticfields omero-client)

add_executable(sudo sudo.cpp)
target_link_libraries(sudo omero-client)

add_executable(unloaded unloaded.cpp)
target_link_libraries(unloaded omero-client)

add_executable(updates updates.cpp)
target_link_libraries(updates omero-client)

install(TARGETS clientpointer
                collectionmethods
                configuration
                constants
                constructors
                details
                enumerations
                iterators
                primitives
                queries
                rcollection
                smartpointers
                staticfields
                sudo
                unloaded
                updates
        RUNTIME
        DESTINATION "${omero_exampledir}"
        PERMISSIONS OWNER_READ OWNER_WRITE OWNER_EXECUTE
                    GROUP_READ GROUP_EXECUTE
                    WORLD_READ WORLD_EXECUTE
                    SETUID)
