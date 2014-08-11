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

if("${PROJECT_SOURCE_DIR}" STREQUAL "${PROJECT_BINARY_DIR}")
  message(FATAL_ERROR "In-tree builds are not supported; please run cmake from a separate build directory.")
endif("${PROJECT_SOURCE_DIR}" STREQUAL "${PROJECT_BINARY_DIR}")

enable_language(CXX)

include(CheckIncludeFileCXX)
include(CheckCXXCompilerFlag)
include(CheckCXXSourceCompiles)

include(components/bioformats/cpp/cmake/GNUInstallDirs.cmake)
include(components/bioformats/cpp/cmake/CompilerChecks.cmake)
include(components/bioformats/cpp/cmake/GTest.cmake)

find_package(Threads REQUIRED)

# Unit tests
find_package(GTest)
set(BUILD_TESTS OFF)
if(GTEST_FOUND)
  set(BUILD_TESTS ON)
endif(GTEST_FOUND)
option(test "Enable unit tests (requires gtest)" ${BUILD_TESTS})
set(BUILD_TESTS ${test})


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

add_subdirectory(components/tools/OmeroCpp)
add_subdirectory(examples)
