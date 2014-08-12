#.rst:
# FindIce
# -------
#
# Find the ZeroC Internet Communication Engine (ICE) programs,
# libraries and datafiles.
#
# Use this module by invoking find_package with the form::
#
#   find_package(Ice
#     [version] [EXACT]      # Minimum or EXACT version e.g. 3.5.1
#     [REQUIRED])            # Fail with error if Ice is not found
#
# This module reports information about the Ice installation in
# several variables.  General variables::
#
#   Ice_VERSION - Ice release version
#   Ice_FOUND - true if the main programs and libraries were found
#
# Ice programs are reported in::
#
#   SLICE2CPP_EXECUTABLE - path to slice2cpp executable
#   SLICE2CS_EXECUTABLE - path to slice2cs executable
#   SLICE2FREEZEJ_EXECUTABLE - path to slice2freezej executable
#   SLICE2FREEZE_EXECUTABLE - path to slice2freeze executable
#   SLICE2HTML_EXECUTABLE - path to slice2html executable
#   SLICE2JAVA_EXECUTABLE - path to slice2java executable
#   SLICE2PHP_EXECUTABLE - path to slice2php executable
#   SLICE2PY_EXECUTABLE - path to slice2py executable
#   SLICE2RB_EXECUTABLE - path to slice2rb executable
#
# Ice libraries are reported in::
#
#   FREEZE_LIBRARY - Freeze library
#   GLACIER2_LIBRARY - Glacier2 library
#   ICE_LIBRARY - Ice library
#   ICEBOX_LIBRARY - IceBox library
#   ICEDB_LIBRARY - IceDB library
#   ICEGRID_LIBRARY - IceGrid library
#   ICEPATCH2_LIBRARY - IcePatch library
#   ICESSL_LIBRARY - IceSSL library
#   ICESTORM_LIBRARY - IceStorm library
#   ICEUTIL_LIBRARY - IceUtil library
#   ICEXML_LIBRARY - IceXML library
#   SLICE_LIBRARY - Slice library
#
# Ice directories for C++ programs, includes and slice includes and libraries
# are reported in::
#
#   ICE_BINARY_DIR - the directory containing the Ice programs
#   ICE_INCLUDE_DIR - the directory containing the Ice headers
#   ICE_SLICE_DIR - the directory containing the Ice slice interface definitions
#   ICE_LIBRARY_DIR - the directory containing the Ice libraries
#
# This module reads hints about search results from variables::
#
#   ICE_HOME - the root of the Ice installation
#   ICE_BINARY_DIR - the directory containing the Ice programs
#   ICE_INCLUDE_DIR - the directory containing the Ice headers
#   ICE_SLICE_DIR - the directory containing the Ice slice interface definitions
#   ICE_LIBRARY_DIR - the directory containing the Ice libraries
#
# The environment variable :envvar:`ICE_HOME` may also be used, unless
# overridden by setting the ICE_HOME variable.
#
# .. note::
#
#   These variables are not all required to be set, and in most cases
#   will not require setting at all unless multiple Ice versions are
#   available and a specific version is required.  On Windows,
#   ICE_HOME is usually sufficient since the package is contained in a
#   single directory.  On Unix, the programs, headers and libraries
#   will usually be in standard locations, but ICE_SLICE_DIR might not
#   be automatically detected.  All the other variables are defaulted
#   using ICE_HOME, if set.  It's possible to set ICE_HOME and
#   selectively specify alternative locations for the other
#   components; this might be required for e.g. newer versions of
#   Visual Studio if the heuristics are not sufficient to identify the
#   correct programs and libraries.
#
# Other variables one may set to control this module are::
#
#   ICE_DEBUG - Set to ON to enable debug output from FindIce.

#=============================================================================
# Written by Roger Leigh <rleigh@codelibre.net>
# Copyright 2014 University of Dundee
#=============================================================================
#
# Distributed under the OSI-approved BSD License (the "License");
# see accompanying file Copyright.txt for details.
#
# This software is distributed WITHOUT ANY WARRANTY; without even the
# implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# See the License for more information.
#=============================================================================
# (To distribute this file outside of CMake, substitute the full
#  License text for the above reference.)

set(ICE_HOME NOTFOUND
    CACHE PATH "Location of the Ice installation")
mark_as_advanced(FORCE ICE_HOME)

set(ICE_BINARY_DIR NOTFOUND
    CACHE PATH "Location of the Ice programs")
mark_as_advanced(FORCE ICE_BINARY_DIR)

set(ICE_INCLUDE_DIR NOTFOUND
    CACHE PATH "Location of the Ice headers")
mark_as_advanced(FORCE ICE_INCLUDE_DIR)

set(ICE_SLICE_DIR NOTFOUND
    CACHE PATH "Location of the Ice slice interface definitions")
mark_as_advanced(FORCE ICE_SLICE_DIR)

set(ICE_LIBRARY_DIR NOTFOUND
    CACHE PATH "Location of the Ice libraries")
mark_as_advanced(FORCE ICE_LIBRARY_DIR)

function(_Ice_FIND)
  # Set up search paths, taking compiler into account.  Search ICE_HOME,
  # with ICE_HOME in the environment as a fallback
  if(ICE_HOME)
    list(APPEND ice_roots "${ICE_HOME}")
  endif(ICE_HOME)
  if(EXISTS "$ENV{ICE_HOME}")
    file(TO_CMAKE_PATH "$ENV{ICE_HOME}" NATIVE_PATH)
    list(APPEND ice_roots "${NATIVE_PATH}")
  endif(EXISTS "$ENV{ICE_HOME}")

  if(CMAKE_SIZEOF_VOID_P EQUAL 8)
    # 64-bit path suffix
    set(_x64 "/x64")
    # 64-bit library directory
    set(_lib64 "lib64")
  endif(CMAKE_SIZEOF_VOID_P EQUAL 8)

  foreach(root ${ice_roots})
    # For compatibility with ZeroC Windows builds.
    if(MSVC)
      # Versions prior to VS 10.0 don't use vcnnn subdirectories.
      # VS 10.0
      if((MSVC_VERSION EQUAL 1600) OR (MSVC_VERSION GREATER 1600 AND MSVC_VERSION LESS 1700))
        list(APPEND ice_binary_paths "${root}/bin/vc100${_x64}")
        list(APPEND ice_library_paths "${root}/lib/vc100${_x64}")
        list(APPEND ice_binary_paths "${root}/bin/vc100")
        list(APPEND ice_library_paths "${root}/lib/vc100")
      endif((MSVC_VERSION EQUAL 1600) OR (MSVC_VERSION GREATER 1600 AND MSVC_VERSION LESS 1700))
      # VS 11.0
      if((MSVC_VERSION EQUAL 1700) OR (MSVC_VERSION GREATER 1700 AND MSVC_VERSION LESS 1800))
        list(APPEND ice_binary_paths "${root}/bin/vc110${_x64}")
        list(APPEND ice_library_paths "${root}/lib/vc110${_x64}")
        list(APPEND ice_binary_paths "${root}/bin/vc110")
        list(APPEND ice_library_paths "${root}/lib/vc110")
      endif((MSVC_VERSION EQUAL 1700) OR (MSVC_VERSION GREATER 1700 AND MSVC_VERSION LESS 1800))
      # VS 12.0
      if((MSVC_VERSION EQUAL 1800) OR (MSVC_VERSION GREATER 1800 AND MSVC_VERSION LESS 1900))
        list(APPEND ice_binary_paths "${root}/bin/vc120${_x64}")
        list(APPEND ice_library_paths "${root}/lib/vc120${_x64}")
        list(APPEND ice_binary_paths "${root}/bin/vc120")
        list(APPEND ice_library_paths "${root}/lib/vc120")
      endif((MSVC_VERSION EQUAL 1800) OR (MSVC_VERSION GREATER 1800 AND MSVC_VERSION LESS 1900))
    endif(MSVC)
    # Generic 64-bit directories
    list(APPEND ice_binary_paths "${root}/bin${_x64}")
    list(APPEND ice_library_paths "${root}/${_lib64}")
    list(APPEND ice_library_paths "${root}/lib${_x64}")
    # Generic 64-bit or 32-bit directories
    list(APPEND ice_binary_paths "${root}/bin")
    list(APPEND ice_include_paths "${root}/include")
    # Common directories
    list(APPEND ice_library_paths "${root}/lib")
    list(APPEND ice_slice_paths "${root}/slice")
  endforeach(root ${ice_roots})

  # On Windows, look in standard install locations.  Different versions
  # of Ice install in different places and support different compiler
  # versions.  Look only in the locations compatible with the compiler
  # in use.  For newer versions which this hardcoded logic can't
  # support, ICE_HOME and/or the other configuration options must be
  # used, in which case the above logic will be used instead.
  if(MSVC_VERSION)
    set(_x86 "(x86)")
    if (CMAKE_SIZEOF_VOID_P MATCHES 8)
      set (program_files_path "$ENV{ProgramFiles${_x86}}/ZeroC")
    else (CMAKE_SIZEOF_VOID_P MATCHES 8)
      set (program_files_path "$ENV{ProgramFiles}/ZeroC")
    endif (CMAKE_SIZEOF_VOID_P MATCHES 8)
    file(TO_CMAKE_PATH "${program_files_path}" program_files_path)

    # VS 8.0
    if((MSVC_VERSION EQUAL 1400) OR (MSVC_VERSION GREATER 1400 AND MSVC_VERSION LESS 1500))
      # 3.3.1
      list(APPEND ice_binary_paths "C:/Ice-3.3.1/bin${_x64}")
      list(APPEND ice_library_paths "C:/Ice-3.3.1/lib${_x64}")
      list(APPEND ice_binary_paths "C:/Ice-3.3.1/bin")
      list(APPEND ice_library_paths "C:/Ice-3.3.1/lib")
      list(APPEND ice_include_paths "C:/Ice-3.3.1/include")
      list(APPEND ice_slice_paths "C:/Ice-3.3.1/slice")
      # 3.3.0
      list(APPEND ice_binary_paths "C:/Ice-3.3.0/bin${_x64}")
      list(APPEND ice_library_paths "C:/Ice-3.3.0/lib${_x64}")
      list(APPEND ice_binary_paths "C:/Ice-3.3.0/bin")
      list(APPEND ice_library_paths "C:/Ice-3.3.0/lib")
      list(APPEND ice_include_paths "C:/Ice-3.3.0/include")
      list(APPEND ice_slice_paths "C:/Ice-3.3.0/slice")
    endif((MSVC_VERSION EQUAL 1400) OR (MSVC_VERSION GREATER 1400 AND MSVC_VERSION LESS 1500))

    # VS 9.0
    if((MSVC_VERSION EQUAL 1500) OR (MSVC_VERSION GREATER 1500 AND MSVC_VERSION LESS 1600))
      # 3.4.2
      list(APPEND ice_binary_paths "${program_files_path}/Ice-3.4.2/bin${_x64}")
      list(APPEND ice_library_paths "${program_files_path}/Ice-3.4.2/lib${_x64}")
      list(APPEND ice_binary_paths "${program_files_path}/Ice-3.4.2/bin")
      list(APPEND ice_library_paths "${program_files_path}/Ice-3.4.2/lib")
      list(APPEND ice_include_paths "C:/Ice-3.4.2/include")
      list(APPEND ice_slice_paths "C:/Ice-3.4.2/slice")
      # 3.4.1
      list(APPEND ice_binary_paths "${program_files_path}/Ice-3.4.1/bin${_x64}")
      list(APPEND ice_library_paths "${program_files_path}/Ice-3.4.1/lib${_x64}")
      list(APPEND ice_binary_paths "${program_files_path}/Ice-3.4.1/bin")
      list(APPEND ice_library_paths "${program_files_path}/Ice-3.4.1/lib")
      list(APPEND ice_include_paths "C:/Ice-3.4.1/include")
      list(APPEND ice_slice_paths "C:/Ice-3.4.1/slice")
      # 3.4.0
      list(APPEND ice_binary_paths "${program_files_path}/Ice-3.4.0/bin${_x64}")
      list(APPEND ice_library_paths "${program_files_path}/Ice-3.4.0/lib${_x64}")
      list(APPEND ice_binary_paths "${program_files_path}/Ice-3.4.0/bin")
      list(APPEND ice_library_paths "${program_files_path}/Ice-3.4.0/lib")
      list(APPEND ice_include_paths "C:/Ice-3.4.0/include")
      list(APPEND ice_slice_paths "C:/Ice-3.4.0/slice")
      # 3.3.1
      list(APPEND ice_binary_paths "C:/Ice-3.3.1-VC90/bin${_x64}")
      list(APPEND ice_library_paths "C:/Ice-3.3.1-VC90/lib${_x64}")
      list(APPEND ice_binary_paths "C:/Ice-3.3.1-VC90/bin")
      list(APPEND ice_library_paths "C:/Ice-3.3.1-VC90/lib")
      list(APPEND ice_include_paths "C:/Ice-3.3.1-VC90/include")
      list(APPEND ice_slice_paths "C:/Ice-3.3.1-VC90/slice")
      # 3.3.0
      list(APPEND ice_binary_paths "C:/Ice-3.3.0-VC90/bin${_x64}")
      list(APPEND ice_library_paths "C:/Ice-3.3.0-VC90/lib${_x64}")
      list(APPEND ice_binary_paths "C:/Ice-3.3.0-VC90/bin")
      list(APPEND ice_library_paths "C:/Ice-3.3.0-VC90/lib")
      list(APPEND ice_include_paths "C:/Ice-3.3.0-VC90/include")
      list(APPEND ice_slice_paths "C:/Ice-3.3.0-VC90/slice")
    endif((MSVC_VERSION EQUAL 1500) OR (MSVC_VERSION GREATER 1500 AND MSVC_VERSION LESS 1600))

    # VS 10.0
    if((MSVC_VERSION EQUAL 1600) OR (MSVC_VERSION GREATER 1600 AND MSVC_VERSION LESS 1700))
      # 3.5.1
      list(APPEND ice_binary_paths "${program_files_path}/Ice-3.5.1/bin${_x64}")
      list(APPEND ice_library_paths "${program_files_path}/Ice-3.5.1/lib${_x64}")
      list(APPEND ice_binary_paths "${program_files_path}/Ice-3.5.1/bin")
      list(APPEND ice_library_paths "${program_files_path}/Ice-3.5.1/lib")
      list(APPEND ice_include_paths "${program_files_path}/Ice-3.5.1/include")
      list(APPEND ice_slice_paths "${program_files_path}/Ice-3.5.1/slice")
      # 3.5.0
      list(APPEND ice_binary_paths "${program_files_path}/Ice-3.5.0/bin${_x64}")
      list(APPEND ice_library_paths "${program_files_path}/Ice-3.5.0/lib${_x64}")
      list(APPEND ice_binary_paths "${program_files_path}/Ice-3.5.0/bin")
      list(APPEND ice_library_paths "${program_files_path}/Ice-3.5.0/lib")
      list(APPEND ice_include_paths "${program_files_path}/Ice-3.5.0/include")
      list(APPEND ice_slice_paths "${program_files_path}/Ice-3.5.0/slice")
      # 3.4.2
      list(APPEND ice_binary_paths "${program_files_path}/Ice-3.4.2/bin/vc100${_x64}")
      list(APPEND ice_library_paths "${program_files_path}/Ice-3.4.2/lib/vc100${_x64}")
      list(APPEND ice_binary_paths "${program_files_path}/Ice-3.4.2/bin/vc100")
      list(APPEND ice_library_paths "${program_files_path}/Ice-3.4.2/lib/vc100")
      list(APPEND ice_include_paths "${program_files_path}/Ice-3.4.2/include")
      list(APPEND ice_slice_paths "${program_files_path}/Ice-3.4.2/slice")
      # 3.4.1
      list(APPEND ice_binary_paths "${program_files_path}/Ice-3.4.1/bin/vc100${_x64}")
      list(APPEND ice_library_paths "${program_files_path}/Ice-3.4.1/lib/vc100${_x64}")
      list(APPEND ice_binary_paths "${program_files_path}/Ice-3.4.1/bin/vc100")
      list(APPEND ice_library_paths "${program_files_path}/Ice-3.4.1/lib/vc100")
      list(APPEND ice_include_paths "${program_files_path}/Ice-3.4.1/include")
      list(APPEND ice_slice_paths "${program_files_path}/Ice-3.4.1/slice")
    endif((MSVC_VERSION EQUAL 1600) OR (MSVC_VERSION GREATER 1600 AND MSVC_VERSION LESS 1700))

    # VS 11.0
    if((MSVC_VERSION EQUAL 1700) OR (MSVC_VERSION GREATER 1700 AND MSVC_VERSION LESS 1800))
      # 3.5.1
      list(APPEND ice_binary_paths "${program_files_path}/Ice-3.5.1/bin/vc110${_x64}")
      list(APPEND ice_library_paths "${program_files_path}/Ice-3.5.1/lib/vc110${_x64}")
      list(APPEND ice_binary_paths "${program_files_path}/Ice-3.5.1/bin/vc110")
      list(APPEND ice_library_paths "${program_files_path}/Ice-3.5.1/lib/vc110")
      list(APPEND ice_include_paths "${program_files_path}/Ice-3.5.1/include")
      list(APPEND ice_slice_paths "${program_files_path}/Ice-3.5.1/slice")
      # 3.5.0
      list(APPEND ice_binary_paths "${program_files_path}/Ice-3.5.0/bin/vc110${_x64}")
      list(APPEND ice_library_paths "${program_files_path}/Ice-3.5.0/lib/vc110${_x64}")
      list(APPEND ice_binary_paths "${program_files_path}/Ice-3.5.0/bin/vc110")
      list(APPEND ice_library_paths "${program_files_path}/Ice-3.5.0/lib/vc110")
      list(APPEND ice_include_paths "${program_files_path}/Ice-3.5.0/include")
      list(APPEND ice_slice_paths "${program_files_path}/Ice-3.5.0/slice")
    endif((MSVC_VERSION EQUAL 1700) OR (MSVC_VERSION GREATER 1700 AND MSVC_VERSION LESS 1800))

    # VS 12.0
    if((MSVC_VERSION EQUAL 1800) OR (MSVC_VERSION GREATER 1800 AND MSVC_VERSION LESS 1900))
      # 3.5.1
      list(APPEND ice_binary_paths "${program_files_path}/Ice-3.5.1/bin/vc120${_x64}")
      list(APPEND ice_library_paths "${program_files_path}/Ice-3.5.1/lib/vc120${_x64}")
      list(APPEND ice_binary_paths "${program_files_path}/Ice-3.5.1/bin/vc120")
      list(APPEND ice_library_paths "${program_files_path}/Ice-3.5.1/lib/vc120")
      list(APPEND ice_include_paths "${program_files_path}/Ice-3.5.1/include")
      list(APPEND ice_slice_paths "${program_files_path}/Ice-3.5.1/slice")
    endif((MSVC_VERSION EQUAL 1800) OR (MSVC_VERSION GREATER 1800 AND MSVC_VERSION LESS 1900))
  else(MSVC_VERSION)
    set(ice_locations
        /opt/Ice
        /opt/Ice-3
        /opt/Ice-3.5
        /opt/Ice-3.5.1
        /opt/Ice-3.5.0
        /opt/Ice-3.4
        /opt/Ice-3.4.2
        /opt/Ice-3.4.1
        /opt/Ice-3.4.0
        /opt/Ice-3.3
        /opt/Ice-3.3.1
        /opt/Ice-3.3.0)

    foreach(path ${ice_locations})
      # Prefer 64-bit variants if present and using a 64-bit compiler
      list(APPEND ice_binary_paths "${path}/bin${_x64}")
      list(APPEND ice_binary_paths "${path}/lib${_x64}")
      list(APPEND ice_binary_paths "${path}/${_lib64}")
      list(APPEND ice_binary_paths "${path}/bin")
      list(APPEND ice_binary_paths "${path}/lib")
      list(APPEND ice_binary_paths "${path}/include")
      list(APPEND ice_binary_paths "${path}/slice")
    endforeach(path)
  endif(MSVC_VERSION)

  if(ICE_DEBUG)
    message(STATUS "--------FindIce.cmake search debug--------")
    message(STATUS "ICE binary path search order: ${ice_binary_paths}")
    message(STATUS "ICE include path search order: ${ice_include_paths}")
    message(STATUS "ICE slice path search order: ${ice_slice_paths}")
    message(STATUS "ICE library path search order: ${ice_library_paths}")
    message(STATUS "----------------")
  endif(ICE_DEBUG)

  set(ice_programs
      slice2cpp
      slice2cs
      slice2freezej
      slice2freeze
      slice2html
      slice2java
      slice2php
      slice2py
      slice2rb)

  set(ice_libraries
      Freeze
      Glacier2
      Ice
      IceBox
      IceDB
      IceGrid
      IcePatch2
      IceSSL
      IceStorm
      IceUtil
      IceXML
      Slice)

  # Find all Ice programs
  foreach(program ${ice_programs})
    string(TOUPPER "${program}" program_upcase)
    set(program_var "${program_upcase}_EXECUTABLE")
    find_program("${program_var}" "${program}"
      PATHS "${ICE_BINARY_DIR}"
            ${ice_binary_paths}
      DOC "Ice slice translator")
    mark_as_advanced(program_var)
    set("${program_var}" "${${program_var}}" PARENT_SCOPE)
    if(NOT FOUND_ICE_BINARY_DIR)
      get_filename_component(FOUND_ICE_BINARY_DIR "${${program_var}}" PATH)
    endif(NOT FOUND_ICE_BINARY_DIR)
  endforeach(program ${ice_programs})
  set(ICE_BINARY_DIR "${FOUND_ICE_BINARY_DIR}" PARENT_SCOPE)

  # Get version.
  if(SLICE2CPP_EXECUTABLE)
    # Execute in C locale for safety
    set(_Ice_SAVED_LC_ALL "$ENV{LC_ALL}")
    set(ENV{LC_ALL} C)

    execute_process(COMMAND ${SLICE2CPP_EXECUTABLE} --version
      ERROR_VARIABLE Ice_VERSION_SLICE2CPP_FULL
      ERROR_STRIP_TRAILING_WHITESPACE)

    # restore the previous LC_ALL
    set(ENV{LC_ALL} ${_Ice_SAVED_LC_ALL})

    # Make short version
    string(REGEX REPLACE "^(.*)\\.[^.]*$" "\\1" Ice_VERSION_SLICE2CPP_SHORT "${Ice_VERSION_SLICE2CPP_FULL}")
    set(ICE_VERSION "${Ice_VERSION_SLICE2CPP_FULL}" PARENT_SCOPE)
  endif(SLICE2CPP_EXECUTABLE)

  # Find include directory
  find_path(ICE_INCLUDE_DIR
            NAMES "Ice/Ice.h"
            PATHS  "${ICE_INCLUDE_DIR}"
                   ${ice_include_paths})
  set(ICE_INCLUDE_DIR "${ICE_INCLUDE_DIR}" PARENT_SCOPE)

  # Find slice directory
  find_path(ICE_SLICE_DIR
            NAMES "Ice/Connection.ice"
            PATHS "${ICE_SLICE_DIR}"
                  ${ice_slice_paths}
                  "/usr/local/share/Ice-${Ice_VERSION_SLICE2CPP_FULL}/slice"
                  "/usr/local/share/Ice-${Ice_VERSION_SLICE2CPP_SHORT}/slice"
                  "/usr/local/share/Ice/slice"
                  "/usr/share/Ice-${Ice_VERSION_SLICE2CPP_FULL}/slice"
                  "/usr/share/Ice-${Ice_VERSION_SLICE2CPP_SHORT}/slice"
                  "/usr/share/Ice/slice"
            NO_DEFAULT_PATH)
  set(ICE_SLICE_DIR "${ICE_SLICE_DIR}" PARENT_SCOPE)

  # Find all Ice libraries
  foreach(library ${ice_libraries})
    string(TOUPPER "${library}" library_upcase)
    set(library_var "${library_upcase}_LIBRARY")
    find_library("${library_var}" "${library}"
      PATHS
        "${ICE_LIBRARY_DIR}"
        ${ice_library_paths}
      HINT
        "${ICE_HOME}/lib"
      DOC "Ice slice translator")
    mark_as_advanced(library_var)
    set("${library_var}" "${${library_var}}" PARENT_SCOPE)
    if(NOT FOUND_ICE_LIBRARY_DIR)
      get_filename_component(FOUND_ICE_LIBRARY_DIR "${${library_var}}" PATH)
    endif(NOT FOUND_ICE_LIBRARY_DIR)
  endforeach(library ${ice_libraries})
  set(ICE_LIBRARY_DIR "${FOUND_ICE_LIBRARY_DIR}" PARENT_SCOPE)
endfunction(_Ice_FIND)

_Ice_FIND()

if(ICE_DEBUG)
  message(STATUS "--------FindIce.cmake results debug--------")
  message(STATUS "ICE_VERSION number: ${ICE_VERSION}")
  message(STATUS "ICE_HOME directory: ${ICE_HOME}")
  message(STATUS "ICE_BINARY_DIR directory: ${ICE_BINARY_DIR}")
  message(STATUS "ICE_INCLUDE_DIR directory: ${ICE_INCLUDE_DIR}")
  message(STATUS "ICE_SLICE_DIR directory: ${ICE_SLICE_DIR}")
  message(STATUS "ICE_LIBRARY_DIR directory: ${ICE_LIBRARY_DIR}")
  message(STATUS "slice2cpp executable: ${SLICE2CPP_EXECUTABLE}")
  message(STATUS "slice2cs executable: ${SLICE2CS_EXECUTABLE}")
  message(STATUS "slice2freezej executable: ${SLICE2FREEZEJ_EXECUTABLE}")
  message(STATUS "slice2freeze executable: ${SLICE2FREEZE_EXECUTABLE}")
  message(STATUS "slice2html executable: ${SLICE2HTML_EXECUTABLE}")
  message(STATUS "slice2java executable: ${SLICE2JAVA_EXECUTABLE}")
  message(STATUS "slice2php executable: ${SLICE2PHP_EXECUTABLE}")
  message(STATUS "slice2py executable: ${SLICE2PY_EXECUTABLE}")
  message(STATUS "slice2rb executable: ${SLICE2RB_EXECUTABLE}")
  message(STATUS "Freeze library: ${FREEZE_LIBRARY}")
  message(STATUS "Glacier2 library: ${GLACIER2_LIBRARY}")
  message(STATUS "Ice library: ${ICE_LIBRARY}")
  message(STATUS "IceBox library: ${ICEBOX_LIBRARY}")
  message(STATUS "IceDB library: ${ICEDB_LIBRARY}")
  message(STATUS "IceGrid library: ${ICEGRID_LIBRARY}")
  message(STATUS "IcePatch2 library: ${ICEPATCH2_LIBRARY}")
  message(STATUS "IceSSL library: ${ICESSL_LIBRARY}")
  message(STATUS "IceStorm library: ${ICESTORM_LIBRARY}")
  message(STATUS "IceUtil library: ${ICEUTIL_LIBRARY}")
  message(STATUS "IceXML library: ${ICEXML_LIBRARY}")
  message(STATUS "Slice library: ${SLICE_LIBRARY}")
  message(STATUS "----------------")
endif(ICE_DEBUG)

include(FindPackageHandleStandardArgs)
FIND_PACKAGE_HANDLE_STANDARD_ARGS(Ice
                                  REQUIRED_VARS SLICE2CPP_EXECUTABLE
                                                ICE_INCLUDE_DIR
                                                ICE_SLICE_DIR
                                                ICE_LIBRARY
                                  VERSION_VAR ICE_VERSION)

unset(ICE_VERSION)
