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

set(GEN_DIR gen)
file(MAKE_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/${GEN_DIR})

include_directories(src gen)

include_directories("${PROJECT_SOURCE_DIR}/components/blitz/generated"
                    "${CMAKE_CURRENT_SOURCE_DIR}/src"
                    "${CMAKE_CURRENT_BINARY_DIR}/${GEN_DIR}"
                    "${Ice_INCLUDE_DIRS}")

option(debug-ice-generation "Dump results of generated Ice source globbing" OFF)

# Find all static Ice slice definitions.
file(GLOB_RECURSE ICE_API_STATIC_SOURCES RELATIVE    "${PROJECT_SOURCE_DIR}"
     "${PROJECT_SOURCE_DIR}/components/blitz/resources/omero/api/*.ice")
file(GLOB_RECURSE ICE_MODEL_STATIC_SOURCES RELATIVE  "${PROJECT_SOURCE_DIR}"
     "${PROJECT_SOURCE_DIR}/components/blitz/resources/omero/model/*.ice")
file(GLOB_RECURSE ICE_COMMON_STATIC_SOURCES RELATIVE "${PROJECT_SOURCE_DIR}"
     "${PROJECT_SOURCE_DIR}/components/blitz/resources/*.ice")

# Move from common to api and model:
list(APPEND ICE_API_STATIC_SOURCES components/blitz/resources/omero/API.ice)
list(APPEND ICE_API_STATIC_SOURCES components/blitz/resources/omero/ServicesF.ice)
list(APPEND ICE_MODEL_STATIC_SOURCES components/blitz/resources/omero/ModelF.ice)

# Remove api and model files from the common files.
if(ICE_API_STATIC_SOURCES)
  list(REMOVE_ITEM ICE_COMMON_STATIC_SOURCES ${ICE_API_STATIC_SOURCES})
endif(ICE_API_STATIC_SOURCES)
if(ICE_MODEL_STATIC_SOURCES)
  list(REMOVE_ITEM ICE_COMMON_STATIC_SOURCES ${ICE_MODEL_STATIC_SOURCES})
endif(ICE_MODEL_STATIC_SOURCES)

# Remove client files from the common files.
set(ICE_CLIENT_STATIC_SOURCES)

if(ICE_CLIENT_STATIC_SOURCES)
  list(REMOVE_ITEM ICE_COMMON_STATIC_SOURCES ${ICE_CLIENT_STATIC_SOURCES})
endif(ICE_CLIENT_STATIC_SOURCES)

if(debug-ice-generation)
  message(STATUS "Static slice definitions (common): ${ICE_COMMON_STATIC_SOURCES}")
  message(STATUS "Static slice definitions (api):    ${ICE_API_STATIC_SOURCES}")
  message(STATUS "Static slice definitions (model):  ${ICE_MODEL_STATIC_SOURCES}")
  message(STATUS "Static slice definitions (client): ${ICE_CLIENT_STATIC_SOURCES}")
endif(debug-ice-generation)

# Find all generated Ice slice definitions.
file(GLOB_RECURSE ICE_API_GENERATED_SOURCES RELATIVE     "${PROJECT_SOURCE_DIR}" "${PROJECT_SOURCE_DIR}/components/blitz/generated/omero/api/*.ice")
file(GLOB_RECURSE ICE_MODEL_GENERATED_SOURCES RELATIVE   "${PROJECT_SOURCE_DIR}" "${PROJECT_SOURCE_DIR}/components/blitz/generated/omero/model/*.ice")
file(GLOB_RECURSE ICE_COMMON_GENERATED_SOURCES RELATIVE  "${PROJECT_SOURCE_DIR}" "${PROJECT_SOURCE_DIR}/components/blitz/generated/*.ice")

# Remove api and model files from the common files.
if(ICE_API_GENERATED_SOURCES)
  list(REMOVE_ITEM ICE_COMMON_GENERATED_SOURCES ${ICE_API_GENERATED_SOURCES})
endif(ICE_API_GENERATED_SOURCES)
if(ICE_MODEL_GENERATED_SOURCES)
  list(REMOVE_ITEM ICE_COMMON_GENERATED_SOURCES ${ICE_MODEL_GENERATED_SOURCES})
endif(ICE_MODEL_GENERATED_SOURCES)

# Remove client files from the common files.
set(ICE_CLIENT_GENERATED_SOURCES)

if(ICE_CLIENT_GENERATED_SOURCES)
  list(REMOVE_ITEM ICE_COMMON_GENERATED_SOURCES ${ICE_CLIENT_GENERATED_SOURCES})
endif(ICE_CLIENT_GENERATED_SOURCES)

if(debug-ice-generation)
  message(STATUS "Generated slice definitions (common): ${ICE_COMMON_GENERATED_SOURCES}")
  message(STATUS "Generated slice definitions (api):    ${ICE_API_GENERATED_SOURCES}")
  message(STATUS "Generated slice definitions (model):  ${ICE_MODEL_GENERATED_SOURCES}")
  message(STATUS "Generated slice definitions (client): ${ICE_CLIENT_GENERATED_SOURCES}")
endif(debug-ice-generation)

# Find all generated C++ sources and headers.
file(GLOB_RECURSE OMERO_API_GENERATED_HEADERS RELATIVE   "${PROJECT_SOURCE_DIR}" "${PROJECT_SOURCE_DIR}/components/blitz/generated/omero/api/*.h")
file(GLOB_RECURSE OMERO_API_GENERATED_SOURCES RELATIVE   "${PROJECT_SOURCE_DIR}" "${PROJECT_SOURCE_DIR}/components/blitz/generated/omero/api/*.cpp")
file(GLOB_RECURSE OMERO_MODEL_GENERATED_HEADERS RELATIVE "${PROJECT_SOURCE_DIR}" "${PROJECT_SOURCE_DIR}/components/blitz/generated/omero/model/*.h")
file(GLOB_RECURSE OMERO_MODEL_GENERATED_SOURCES RELATIVE "${PROJECT_SOURCE_DIR}" "${PROJECT_SOURCE_DIR}/components/blitz/generated/omero/model/*.cpp")
file(GLOB_RECURSE OMERO_COMMON_GENERATED_HEADERS RELATIVE "${PROJECT_SOURCE_DIR}" "${PROJECT_SOURCE_DIR}/components/blitz/generated/*.h")
file(GLOB_RECURSE OMERO_COMMON_GENERATED_SOURCES RELATIVE "${PROJECT_SOURCE_DIR}" "${PROJECT_SOURCE_DIR}/components/blitz/generated/*.cpp")

# Remove api and model files from the common files.
if(OMERO_API_GENERATED_HEADERS)
  list(REMOVE_ITEM OMERO_COMMON_GENERATED_HEADERS ${OMERO_API_GENERATED_HEADERS})
endif(OMERO_API_GENERATED_HEADERS)
if(OMERO_MODEL_GENERATED_HEADERS)
  list(REMOVE_ITEM OMERO_COMMON_GENERATED_HEADERS ${OMERO_MODEL_GENERATED_HEADERS})
endif(OMERO_MODEL_GENERATED_HEADERS)
if(OMERO_API_GENERATED_SOURCES)
  list(REMOVE_ITEM OMERO_COMMON_GENERATED_SOURCES ${OMERO_API_GENERATED_SOURCES})
endif(OMERO_API_GENERATED_SOURCES)
if(OMERO_MODEL_GENERATED_SOURCES)
  list(REMOVE_ITEM OMERO_COMMON_GENERATED_SOURCES ${OMERO_MODEL_GENERATED_SOURCES})
endif(OMERO_MODEL_GENERATED_SOURCES)

if(debug-ice-generation)
  message(STATUS "Generated C++ headers (common): ${OMERO_COMMON_GENERATED_HEADERS}")
  message(STATUS "Generated C++ sources (common): ${OMERO_COMMON_GENERATED_SOURCES}")
  message(STATUS "Generated C++ headers (api):    ${OMERO_API_GENERATED_HEADERS}")
  message(STATUS "Generated C++ sources (api):    ${OMERO_API_GENERATED_SOURCES}")
  message(STATUS "Generated C++ headers (model):  ${OMERO_MODEL_GENERATED_HEADERS}")
  message(STATUS "Generated C++ sources (model):  ${OMERO_MODEL_GENERATED_SOURCES}")
  message(STATUS "Generated C++ headers (client): ${OMERO_CLIENT_GENERATED_HEADERS}")
  message(STATUS "Generated C++ sources (client): ${OMERO_CLIENT_GENERATED_SOURCES}")
endif(debug-ice-generation)

# Remove client files from the common files.
set(OMERO_CLIENT_GENERATED_HEADERS)
set(OMERO_CLIENT_GENERATED_SOURCES)

if(OMERO_CLIENT_GENERATED_HEADERS)
  list(REMOVE_ITEM OMERO_COMMON_GENERATED_HEADERS ${OMERO_CLIENT_GENERATED_HEADERS})
endif(OMERO_CLIENT_GENERATED_HEADERS)
if(OMERO_CLIENT_GENERATED_SOURCES)
  list(REMOVE_ITEM OMERO_COMMON_GENERATED_SOURCES ${OMERO_CLIENT_GENERATED_SOURCES})
endif(OMERO_CLIENT_GENERATED_SOURCES)

list(APPEND SLICEOPTS "-I${PROJECT_SOURCE_DIR}/components/blitz/generated" "-I${PROJECT_SOURCE_DIR}/components/blitz/resources")
foreach(slicedir ${Ice_SLICE_DIRS})
  list(APPEND SLICEOPTS "-I${slicedir}")
endforeach(slicedir)
if(WIN32)
  list(APPEND SLICEOPTS --dll-export OMERO_ICE)
endif(WIN32)

set(omero_includedir "${CMAKE_INSTALL_FULL_INCLUDEDIR}/omero")

# Generate C++ headers and sources from ICE definitions with slice2cpp
# Install generated headers.
function(icegen in outsrc outhdr)
  foreach(icesrc ${${in}})
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
      list(APPEND output_sources ${GEN_DIR}/${ICECPP})
      list(APPEND output_headers ${GEN_DIR}/${ICEHPP})
      add_custom_command(
        OUTPUT ${GEN_DIR}/${ICECPP} ${GEN_DIR}/${ICEHPP}
        WORKING_DIRECTORY gen
        COMMAND ${CMAKE_COMMAND} -E make_directory "${ICE_PATH}"
        COMMAND "${Ice_SLICE2CPP_EXECUTABLE}" ${SLICEOPTS} ${incdir} "--output-dir=${ICE_PATH}" "${PROJECT_SOURCE_DIR}/${icesrc}"
        DEPENDS "${PROJECT_SOURCE_DIR}/${icesrc}")
      install(FILES ${CMAKE_CURRENT_BINARY_DIR}/${GEN_DIR}/${ICEHPP}
              DESTINATION "${CMAKE_INSTALL_FULL_INCLUDEDIR}/${ICE_PATH}")
    endif("${ICE_PATH}" STREQUAL ".")
  endforeach(icesrc)
  set(${outsrc} ${output_sources} PARENT_SCOPE)
  set(${outhdr} ${output_headers} PARENT_SCOPE)
endfunction(icegen in out)

icegen(ICE_COMMON_STATIC_SOURCES OMERO_COMMON_STATIC_ICE_SOURCES OMERO_COMMON_STATIC_ICE_HEADERS)
icegen(ICE_COMMON_GENERATED_SOURCES OMERO_COMMON_GENERATED_ICE_SOURCES OMERO_COMMON_GENERATED_ICE_HEADERS)
icegen(ICE_API_STATIC_SOURCES OMERO_API_STATIC_ICE_SOURCES OMERO_API_STATIC_ICE_HEADERS)
icegen(ICE_API_GENERATED_SOURCES OMERO_API_GENERATED_ICE_SOURCES OMERO_API_GENERATED_ICE_HEADERS)
icegen(ICE_MODEL_STATIC_SOURCES OMERO_MODEL_STATIC_ICE_SOURCES OMERO_MODEL_STATIC_ICE_HEADERS)
icegen(ICE_MODEL_GENERATED_SOURCES OMERO_MODEL_GENERATED_ICE_SOURCES OMERO_MODEL_GENERATED_ICE_HEADERS)
icegen(ICE_CLIENT_STATIC_SOURCES OMERO_CLIENT_STATIC_ICE_SOURCES OMERO_CLIENT_STATIC_ICE_HEADERS)
icegen(ICE_CLIENT_GENERATED_SOURCES OMERO_CLIENT_GENERATED_ICE_SOURCES OMERO_CLIENT_GENERATED_ICE_HEADERS)

# Install generated headers and compute absolute path.
function(abspath_headers in out)
  list(APPEND output_headers ${${out}})
  foreach(genhdr ${${in}})
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

    list(APPEND output_headers "${PROJECT_SOURCE_DIR}/${genhdr}")
  endforeach(genhdr)
  set(${out} ${output_headers} PARENT_SCOPE)
endfunction(abspath_headers in out)

abspath_headers(OMERO_API_GENERATED_HEADERS OMERO_API_GENERATED_HEADERS_ABS)
abspath_headers(OMERO_MODEL_GENERATED_HEADERS OMERO_MODEL_GENERATED_HEADERS_ABS)
abspath_headers(OMERO_COMMON_GENERATED_HEADERS OMERO_COMMON_GENERATED_HEADERS_ABS)

# Absolute path for generated sources
function(abspath_sources in out)
  list(APPEND output_sources ${${out}})
  foreach(gensrc ${${in}})
    list(APPEND output_sources "${PROJECT_SOURCE_DIR}/${gensrc}")
  endforeach(gensrc)
  set(${out} ${output_sources} PARENT_SCOPE)
endfunction(abspath_sources)

abspath_sources(OMERO_API_GENERATED_SOURCES OMERO_API_GENERATED_SOURCES_ABS)
abspath_sources(OMERO_MODEL_GENERATED_SOURCES OMERO_MODEL_GENERATED_SOURCES_ABS)
abspath_sources(OMERO_COMMON_GENERATED_SOURCES OMERO_COMMON_GENERATED_SOURCES_ABS)

set(OMERO_ICE_HEADERS
    ${OMERO_COMMON_STATIC_ICE_HEADERS}
    ${OMERO_COMMON_GENERATED_ICE_HEADERS}
    ${OMERO_MODEL_STATIC_ICE_HEADERS}
    ${OMERO_MODEL_GENERATED_ICE_HEADERS}
    ${OMERO_API_STATIC_ICE_HEADERS}
    ${OMERO_API_GENERATED_ICE_HEADERS}
    ${OMERO_CLIENT_STATIC_ICE_HEADERS}
    ${OMERO_CLIENT_GENERATED_ICE_HEADERS})
set(OMERO_ICE_SOURCES
    ${OMERO_COMMON_STATIC_ICE_SOURCES}
    ${OMERO_COMMON_GENERATED_ICE_SOURCES}
    ${OMERO_MODEL_STATIC_ICE_SOURCES}
    ${OMERO_MODEL_GENERATED_ICE_SOURCES}
    ${OMERO_API_STATIC_ICE_SOURCES}
    ${OMERO_API_GENERATED_ICE_SOURCES}
    ${OMERO_CLIENT_STATIC_ICE_SOURCES}
    ${OMERO_CLIENT_GENERATED_ICE_SOURCES})

add_library(omero-ice SHARED
            ${OMERO_ICE_HEADERS} ${OMERO_ICE_SOURCES})
target_link_libraries(omero-ice ${Ice_LIBRARIES})
set_target_properties(omero-ice PROPERTIES
                      VERSION ${OME_VERSION_SHORT})

if(MSVC)
  set_target_properties(omero-ice PROPERTIES
                        LINK_FLAGS "/INCREMENTAL:NO"
                        DEFINE_SYMBOL "OMERO_ICE_EXPORTS")
endif(MSVC)

set(OMERO_COMMON_STATIC_HEADERS
    src/omero/ClientErrors.h
    src/omero/IceNoWarnPop.h
    src/omero/IceNoWarnPush.h
    src/omero/ObjectFactoryRegistrar.h
    src/omero/RTypesI.h
    src/omero/templates.h)

set(OMERO_COMMON_STATIC_SYS_HEADERS
    src/omero/sys/ParametersI.h)

set(OMERO_COMMON_STATIC_SOURCES
    src/omero/ClientErrors.cpp
    src/omero/ObjectFactoryRegistrar.cpp
    src/omero/RTypesI.cpp
    src/omero/sys/ParametersI.cpp)

set(OMERO_COMMON_HEADERS
    ${OMERO_COMMON_STATIC_HEADERS}
    ${OMERO_COMMON_STATIC_SYS_HEADERS}
    ${OMERO_COMMON_GENERATED_HEADERS_ABS})
set(OMERO_COMMON_SOURCES
    ${OMERO_COMMON_STATIC_SOURCES}
    ${OMERO_COMMON_GENERATED_SOURCES_ABS})


set(OMERO_MODEL_STATIC_MODEL_HEADERS
    src/omero/model/DetailsI.h
    src/omero/model/ObjectFactory.h
    src/omero/model/PermissionsI.h
    src/omero/model/ElectricPotentialI.h
    src/omero/model/FrequencyI.h
    src/omero/model/LengthI.h
    src/omero/model/PowerI.h
    src/omero/model/PressureI.h
    src/omero/model/TemperatureI.h
    src/omero/model/TimeI.h)

set(OMERO_MODEL_STATIC_SOURCES
    src/omero/model/DetailsI.cpp
    src/omero/model/PermissionsI.cpp
    src/omero/model/ElectricPotentialI.cpp
    src/omero/model/FrequencyI.cpp
    src/omero/model/LengthI.cpp
    src/omero/model/PowerI.cpp
    src/omero/model/PressureI.cpp
    src/omero/model/TemperatureI.cpp
    src/omero/model/TimeI.cpp)

set(OMERO_MODEL_HEADERS
    ${OMERO_MODEL_STATIC_MODEL_HEADERS}
    ${OMERO_MODEL_GENERATED_HEADERS_ABS})
set(OMERO_MODEL_SOURCES
    ${OMERO_MODEL_STATIC_SOURCES}
    ${OMERO_MODEL_GENERATED_SOURCES_ABS})


set(OMERO_API_STATIC_HEADERS)
set(OMERO_API_STATIC_SOURCES)

set(OMERO_API_HEADERS
    ${OMERO_API_STATIC_HEADERS}
    ${OMERO_API_GENERATED_HEADERS_ABS})
set(OMERO_API_SOURCES
    ${OMERO_API_STATIC_SOURCES}
    ${OMERO_API_GENERATED_SOURCES_ABS})


set(OMERO_CLIENT_STATIC_HEADERS
    src/omero/all.h
    src/omero/callbacks.h
    src/omero/client.h
    src/omero/clientF.h
    src/omero/conversions.h
    src/omero/min.h
    src/omero/ObjectFactory.h)

set(OMERO_CLIENT_STATIC_UTIL_HEADERS
    src/omero/util/concurrency.h
    src/omero/util/tiles.h
    src/omero/util/uuid.h)

set(OMERO_CLIENT_STATIC_SOURCES
    src/omero/callbacks.cpp
    src/omero/client.cpp
    src/omero/clientF.cpp
    src/omero/ObjectFactory.cpp
    src/omero/util/concurrency.cpp
    src/omero/conversions.cpp
    src/omero/util/tiles.cpp
    src/omero/util/uuid.cpp)

set(OMERO_CLIENT_HEADERS
    ${OMERO_CLIENT_STATIC_HEADERS}
    ${OMERO_CLIENT_STATIC_UTIL_HEADERS}
    ${OMERO_CLIENT_GENERATED_HEADERS_ABS})
set(OMERO_CLIENT_SOURCES
    ${OMERO_CLIENT_STATIC_SOURCES}
    ${OMERO_CLIENT_GENERATED_SOURCES_ABS})

add_library(omero-client SHARED
            ${OMERO_COMMON_HEADERS} ${OMERO_COMMON_SOURCES}
            ${OMERO_MODEL_HEADERS} ${OMERO_MODEL_SOURCES}
            ${OMERO_API_HEADERS} ${OMERO_API_SOURCES}
            ${OMERO_CLIENT_HEADERS} ${OMERO_CLIENT_SOURCES})
target_link_libraries(omero-client omero-ice ${Ice_LIBRARIES})
set_target_properties(omero-client PROPERTIES
                      VERSION ${OME_VERSION_SHORT})

if(MSVC)
  set_target_properties(omero-client PROPERTIES
                        LINK_FLAGS "/INCREMENTAL:NO"
                        DEFINE_SYMBOL "OMERO_CLIENT_EXPORTS")
endif(MSVC)

set_property(TARGET omero-ice omero-client PROPERTY FOLDER "Libraries")

if(debug-ice-generation)
  message(STATUS "omero-client (headers): ${OMERO_COMMON_HEADERS} ${OMERO_MODEL_HEADERS} ${OMERO_API_HEADERS} ${OMERO_CLIENT_HEADERS}")
  message(STATUS "omero-client (sources): ${OMERO_COMMON_SOURCES} ${OMERO_MODEL_SOURCES} ${OMERO_API_SOURCES} ${OMERO_CLIENT_SOURCES}")
endif(debug-ice-generation)

add_custom_target(gensrc
                  DEPENDS ${OMERO_COMMON_HEADERS} ${OMERO_COMMON_SOURCES}
                          ${OMERO_MODEL_HEADERS} ${OMERO_MODEL_SOURCES}
                          ${OMERO_API_HEADERS} ${OMERO_API_SOURCES}
                          ${OMERO_CLIENT_HEADERS} ${OMERO_CLIENT_SOURCES})

set_property(TARGET gensrc PROPERTY FOLDER "Ice code generation")

install(TARGETS omero-ice omero-client
        LIBRARY DESTINATION ${CMAKE_INSTALL_FULL_LIBDIR}
        ARCHIVE DESTINATION ${CMAKE_INSTALL_FULL_LIBDIR}
        RUNTIME DESTINATION ${CMAKE_INSTALL_FULL_BINDIR})

install(FILES ${OMERO_COMMON_STATIC_HEADERS}
              ${OMERO_CLIENT_STATIC_HEADERS}
        DESTINATION ${omero_includedir})
install(FILES ${OMERO_MODEL_STATIC_MODEL_HEADERS}
        DESTINATION ${omero_includedir}/model)
install(FILES ${OMERO_COMMON_STATIC_SYS_HEADERS}
        DESTINATION ${omero_includedir}/sys)
install(FILES ${OMERO_CLIENT_STATIC_UTIL_HEADERS}
        DESTINATION ${omero_includedir}/util)

set(LIBRARY_PREFIX OMERO_ICE)
set(LIBRARY_NAME omero-ice)
set(LIBRARY_HEADER omero/RTypes.h)
configure_file(${PROJECT_SOURCE_DIR}/components/bioformats/cpp/cmake/TemplateConfig.cmake.in
               ${CMAKE_CURRENT_BINARY_DIR}/omero-ice-config.cmake)
install(FILES ${CMAKE_CURRENT_BINARY_DIR}/omero-ice-config.cmake
        DESTINATION ${CMAKE_INSTALL_FULL_LIBDIR}/cmake)

set(LIBRARY_PREFIX OMERO_CLIENT)
set(LIBRARY_NAME omero-client)
set(LIBRARY_HEADER omero/client.h)
configure_file(${PROJECT_SOURCE_DIR}/components/bioformats/cpp/cmake/TemplateConfig.cmake.in
               ${CMAKE_CURRENT_BINARY_DIR}/omero-client-config.cmake)
install(FILES ${CMAKE_CURRENT_BINARY_DIR}/omero-client-config.cmake
        DESTINATION ${CMAKE_INSTALL_FULL_LIBDIR}/cmake)

foreach(flag -Wno-switch-default -Wno-redundant-decls -Wno-overloaded-virtual -Wno-old-style-cast -Wno-deprecated-declarations)
  string(REPLACE "/" "_" flag_var "${flag}")
  set(test_cxx_flag "CXX_FLAG${flag_var}")
  CHECK_CXX_COMPILER_FLAG(${flag} "${test_cxx_flag}")
  if (${test_cxx_flag})
     set(ICE_FLAGS "${ICE_FLAGS} ${flag}")
  endif (${test_cxx_flag})
endforeach(flag ${test_flags})

set_source_files_properties(
    ${OMERO_ICE_SOURCES}
    PROPERTIES
    COMPILE_FLAGS "${ICE_FLAGS}")

# Last so that variables are propagated
add_subdirectory(test)
add_subdirectory(utils)
