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

file(MAKE_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/gen)
set(GEN_DIR gen)

include_directories("${PROJECT_SOURCE_DIR}/components/blitz/generated"
                    "${CMAKE_CURRENT_SOURCE_DIR}/src"
                    "${CMAKE_CURRENT_BINARY_DIR}/gen")

file(GLOB_RECURSE ICE_STATIC_SOURCES RELATIVE "${PROJECT_SOURCE_DIR}" "${PROJECT_SOURCE_DIR}/components/blitz/resources/*.ice")
file(GLOB_RECURSE ICE_GENERATED_SOURCES RELATIVE "${PROJECT_SOURCE_DIR}" "${PROJECT_SOURCE_DIR}/components/blitz/generated/*.ice")
file(GLOB_RECURSE OMERO_CLIENT_GENERATED_HEADERS RELATIVE "${PROJECT_SOURCE_DIR}" "${PROJECT_SOURCE_DIR}/components/blitz/generated/*.h")
file(GLOB_RECURSE OMERO_CLIENT_GENERATED_SOURCES RELATIVE "${PROJECT_SOURCE_DIR}" "${PROJECT_SOURCE_DIR}/components/blitz/generated/*.cpp")

if(DEFINED ENV{SLICEPATH} AND EXISTS $ENV{SLICEPATH})
  set(ICEINCLUDE $ENV{SLICEPATH})
elseif(DEFINED ENV{ICE_HOME} AND EXISTS $ENV{ICE_HOME}/slice)
  set(ICEINCLUDE $ENV{ICE_HOME}/slice)
endif(DEFINED ENV{SLICEPATH} AND EXISTS $ENV{SLICEPATH})

set(SLICE2CPP slice2cpp)
if(DEFINED ENV{ICE_HOME} AND EXISTS $ENV{ICE_HOME}/bin/slice2cpp)
  set(SLICE2CPP $ENV{ICE_HOME}/bin/slice2cpp)
endif(DEFINED ENV{ICE_HOME} AND EXISTS $ENV{ICE_HOME}/bin/slice2cpp)
if(DEFINED ENV{SLICE2CPP})
  set(SLICE2CPP $ENV{SLICE2CPP})
endif(DEFINED ENV{SLICE2CPP})

list(APPEND SLICEOPTS "-I${PROJECT_SOURCE_DIR}/components/blitz/generated" "-I${PROJECT_SOURCE_DIR}/components/blitz/resources")
if(DEFINED ICEINCLUDE)
  list(APPEND SLICEOPTS "-I${ICEINCLUDE}")
endif(DEFINED ICEINCLUDE)
if(WIN32)
  list(APPEND SLICEOPTS --dll-export OMERO_API)
endif(WIN32)

set(omero_includedir "${CMAKE_INSTALL_FULL_INCLUDEDIR}/omero")

# Generate C++ headers and sources from ICE definitions with slice2cpp
# Install generated headers.
foreach(icesrc ${ICE_STATIC_SOURCES} ${ICE_GENERATED_SOURCES})
  string(REGEX REPLACE "^components/blitz/(resources|generated)/" "" ICEBASE ${icesrc})
  string(REGEX REPLACE "\\.ice$" ".cpp" ICECPP ${ICEBASE})
  string(REGEX REPLACE "\\.ice$" ".h" ICEHPP ${ICEBASE})
  get_filename_component(ICE_PATH ${ICECPP} PATH)
  if("${ICE_PATH}" STREQUAL "")
    set(ICE_PATH .)
  endif("${ICE_PATH}" STREQUAL "")

  if("${ICE_PATH}" STREQUAL ".")
    # README.ice is skipped.
    unset(incdir)
  else("${ICE_PATH}" STREQUAL ".")
    set(incdir "--include-dir=${ICE_PATH}")
    list(APPEND OMERO_CLIENT_ICE_SOURCES ${GEN_DIR}/${ICECPP})
    list(APPEND OMERO_CLIENT_ICE_HEADERS ${GEN_DIR}/${ICEHPP})
    add_custom_command(
      OUTPUT ${GEN_DIR}/${ICECPP} ${GEN_DIR}/${ICEHPP}
      WORKING_DIRECTORY gen
      COMMAND ${CMAKE_COMMAND} -E make_directory "${ICE_PATH}"
      COMMAND "${SLICE2CPP}" ${SLICEOPTS} ${incdir} "--output-dir=${ICE_PATH}" "${PROJECT_SOURCE_DIR}/${icesrc}"
      DEPENDS "${PROJECT_SOURCE_DIR}/${icesrc}")
    install(FILES ${CMAKE_CURRENT_BINARY_DIR}/${GEN_DIR}/${ICEHPP}
            DESTINATION "${CMAKE_INSTALL_FULL_INCLUDEDIR}/${ICE_PATH}")
  endif("${ICE_PATH}" STREQUAL ".")
endforeach(icesrc)

# Install generated headers and compute absolute path.
foreach(genhdr ${OMERO_CLIENT_GENERATED_HEADERS})
  string(REGEX REPLACE "^components/blitz/generated/" "" HDRBASE ${genhdr})
  get_filename_component(HDR_PATH ${HDRBASE} PATH)
  if("${HDR_PATH}" STREQUAL "")
    set(HDR_PATH .)
  endif("${HDR_PATH}" STREQUAL "")

  if("${HDR_PATH}" STREQUAL ".")
    # README.h is skipped.
  else("${HDR_PATH}" STREQUAL ".")
    install(FILES ${PROJECT_SOURCE_DIR}/${genhdr}
            DESTINATION "${CMAKE_INSTALL_FULL_INCLUDEDIR}/${HDR_PATH}")
  endif("${HDR_PATH}" STREQUAL ".")

  list(APPEND OMERO_CLIENT_GENERATED_HEADERS_ABS "${PROJECT_SOURCE_DIR}/${genhdr}")
endforeach(genhdr)

# Absolute path for generated sources
foreach(gensrc ${OMERO_CLIENT_GENERATED_SOURCES})
  list(APPEND OMERO_CLIENT_GENERATED_SOURCES_ABS "${PROJECT_SOURCE_DIR}/${gensrc}")
endforeach(gensrc)

set(OMERO_CLIENT_STATIC_HEADERS
    src/omero/all.h
    src/omero/callbacks.h
    src/omero/client.h
    src/omero/ClientErrors.h
    src/omero/clientF.h
    src/omero/min.h
    src/omero/ObjectFactoryRegistrar.h
    src/omero/RTypesI.h
    src/omero/templates.h)

set(OMERO_CLIENT_MODEL_STATIC_HEADERS
    src/omero/model/DetailsI.h
    src/omero/model/PermissionsI.h)

set(OMERO_CLIENT_SYS_STATIC_HEADERS
    src/omero/sys/ParametersI.h)

set(OMERO_CLIENT_UTIL_STATIC_HEADERS
    src/omero/util/concurrency.h
    src/omero/util/tiles.h
    src/omero/util/uuid.h)

set(OMERO_CLIENT_STATIC_SOURCES
    src/omero/callbacks.cpp
    src/omero/client.cpp
    src/omero/ClientErrors.cpp
    src/omero/clientF.cpp
    src/omero/model/DetailsI.cpp
    src/omero/model/PermissionsI.cpp
    src/omero/RTypesI.cpp
    src/omero/sys/ParametersI.cpp
    src/omero/util/concurrency.cpp
    src/omero/util/tiles.cpp
    src/omero/util/uuid.cpp)

set(OMERO_CLIENT_HEADERS
    ${OMERO_CLIENT_STATIC_HEADERS}
    ${OMERO_CLIENT_MODEL_STATIC_HEADERS}
    ${OMERO_CLIENT_SYS_STATIC_HEADERS}
    ${OMERO_CLIENT_UTIL_STATIC_HEADERS}
    ${OMERO_CLIENT_GENERATED_HEADERS_ABS}
    ${OMERO_CLIENT_ICE_HEADERS})
set(OMERO_CLIENT_SOURCES
    ${OMERO_CLIENT_STATIC_SOURCES}
    ${OMERO_CLIENT_GENERATED_SOURCES_ABS}
    ${OMERO_CLIENT_ICE_SOURCES})

include_directories(src gen)

add_library(omero-client SHARED ${OMERO_CLIENT_HEADERS} ${OMERO_CLIENT_SOURCES})
target_link_libraries(omero-client Ice IceUtil Glacier2)

install(TARGETS omero-client LIBRARY
        DESTINATION ${CMAKE_INSTALL_FULL_LIBDIR})

install(FILES ${OMERO_CLIENT_STATIC_HEADERS}
        DESTINATION ${omero_includedir})
install(FILES ${OMERO_CLIENT_MODEL_STATIC_HEADERS}
        DESTINATION ${omero_includedir}/model)
install(FILES ${OMERO_CLIENT_SYS_STATIC_HEADERS}
        DESTINATION ${omero_includedir}/sys)
install(FILES ${OMERO_CLIENT_UTIL_STATIC_HEADERS}
        DESTINATION ${omero_includedir}/util)

add_subdirectory(test)
add_subdirectory(utils)

#set(test_headers_SOURCES "")
#file(MAKE_DIRECTORY gen/test/headers)
#foreach(header ${MODEL_GENERATED_MODEL_HEADERS} ${MODEL_GENERATED_ENUM_HEADERS} ${OMERO_STATIC_HEADERS})
#  string(REPLACE "/" "_" genheader ${header})
#  string(REGEX REPLACE "\\.h$" ".cpp" genheader ${genheader})
#  string(REGEX REPLACE "^(gen|src)/" "" include ${header})
#  string(CONFIGURE "#include <@include@>" src)
#  file(WRITE "gen/test/headers/${genheader}" "${src}")
#  list(APPEND test_headers_SOURCES "gen/test/headers/${genheader}")
##  set(test_headers_SOURCES ${test_headers_SOURCES} PARENT_SCOPE )
#endforeach(header)
#add_executable(test/headers test/headers.cpp ${test_headers_SOURCES})
#target_link_libraries(test/headers omero-client)
