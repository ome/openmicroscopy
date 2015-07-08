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

cmake_minimum_required(VERSION 2.8)

# Default policy is from 2.8.0
cmake_policy(VERSION 2.8.0)
# Set MacOSX @rpath usage globally.
if (POLICY CMP0042)
  cmake_policy(SET CMP0042 NEW)
endif(POLICY CMP0042)

project(openmicroscopy)

set_property(GLOBAL PROPERTY USE_FOLDERS ON)

set(OMERO_CPP_DIR components/tools/OmeroCpp)
set(OMERO_CPP_SOURCE_DIR ${PROJECT_SOURCE_DIR}/${OMERO_CPP_DIR})
set(OMERO_CPP_BINARY_DIR ${PROJECT_BINARY_DIR}/${OMERO_CPP_DIR})

include(${OMERO_CPP_DIR}/cmake/Version.cmake)

if("${PROJECT_SOURCE_DIR}" STREQUAL "${PROJECT_BINARY_DIR}")
  message(FATAL_ERROR "In-tree builds are not supported; please run cmake from a separate build directory.")
endif("${PROJECT_SOURCE_DIR}" STREQUAL "${PROJECT_BINARY_DIR}")

enable_language(CXX)

list(APPEND CMAKE_MODULE_PATH
     "${OMERO_CPP_SOURCE_DIR}/cmake")

include(CheckIncludeFileCXX)
include(CheckCXXCompilerFlag)
include(CheckCXXSourceCompiles)

include(GNUInstallDirs)
include(CompilerChecks)
include(GTest)

# For compatibility with scons and java build
if($ENV{SLICEPATH})
  set(ICE_SLICE_DIR "$ENV{SLICEPATH}")
endif($ENV{SLICEPATH})
# Find Ice programs, includes, slice definitions and libraries
find_package(Ice 3.4.0 REQUIRED COMPONENTS Ice IceUtil Glacier2)

find_package(Threads REQUIRED)

# Set Ice environment based upon the FindIce results (for build.py).
get_filename_component(ICE_BINARY_DIR "${Ice_SLICE2CPP_EXECUTABLE}" PATH)
file(TO_NATIVE_PATH "${ICE_BINARY_DIR}" NATIVE_ICE_BINARY_DIR)
if(WIN32)
  set(ENV{PATH} "${NATIVE_ICE_BINARY_DIR};$ENV{PATH}")
else(WIN32)
  set(ENV{PATH} "${NATIVE_ICE_BINARY_DIR}:$ENV{PATH}")
endif(WIN32)
if(Ice_HOME)
  file(TO_NATIVE_PATH "${Ice_HOME}" NATIVE_ICE_HOME)
  set(ENV{ICE_HOME} "${NATIVE_ICE_HOME}")
endif(Ice_HOME)
if(Ice_SLICE_DIRS)
  file(TO_NATIVE_PATH "${Ice_SLICE_DIRS}" NATIVE_ICE_SLICE_DIR)
  set(ENV{SLICEPATH} "${NATIVE_ICE_SLICE_DIR}")
endif(Ice_SLICE_DIRS)

# Run build.py to generate C++ and Ice sources.
if(NOT EXISTS ${PROJECT_SOURCE_DIR}/components/blitz/generated/omero/model/Image.ice)
  message(STATUS "Running build.py to generate C++ and Ice sources; this may take some time")
  execute_process(COMMAND python build.py build-default
                  RESULT_VARIABLE build_py_fail
                  WORKING_DIRECTORY "${PROJECT_SOURCE_DIR}"
                  OUTPUT_FILE "${PROJECT_BINARY_DIR}/CMakeFiles/OmeroBuildOutput.txt"
                  ERROR_FILE "${PROJECT_BINARY_DIR}/CMakeFiles/OmeroBuildError.txt")
  if(build_py_fail)
    # On failure we must clean to ensure that we don't leave the
    # sources half-generated since this just leads to build failures
    # later on.
    message(STATUS "Failed to run build.py to generate C++ and Ice sources; cleaning.")
    execute_process(COMMAND python build.py clean
                    RESULT_VARIABLE build_py_fail2
                    WORKING_DIRECTORY "${PROJECT_SOURCE_DIR}"
                    OUTPUT_FILE "${PROJECT_BINARY_DIR}/CMakeFiles/OmeroCleanOutput.txt"
                    ERROR_FILE "${PROJECT_BINARY_DIR}/CMakeFiles/OmeroCleanError.txt")
    message(FATAL_ERROR "Failed to run build.py to generate C++ and Ice sources.  See CMakeFiles/OmeroBuildOutput.txt and CMakeFiles/OmeroBuildError.txt")
  endif(build_py_fail)
else(NOT EXISTS ${PROJECT_SOURCE_DIR}/components/blitz/generated/omero/model/Image.ice)
  message(STATUS "C++ and Ice sources already generated; skipping running build.py")
endif(NOT EXISTS ${PROJECT_SOURCE_DIR}/components/blitz/generated/omero/model/Image.ice)

add_subdirectory(${OMERO_CPP_DIR})
add_subdirectory(examples)

set(LIBRARY_PREFIX OME)
configure_file(${OMERO_CPP_SOURCE_DIR}/cmake/TemplateShellConfig.cmake.in
               ${PROJECT_BINARY_DIR}/config @ONLY)
if(WIN32)
  configure_file(${OMERO_CPP_SOURCE_DIR}/cmake/TemplateCmdConfig.cmake.in
                 ${PROJECT_BINARY_DIR}/config.bat @ONLY)
endif()
