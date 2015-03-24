


                                   README

                      OPEN MICROSCOPY ENVIRONMENT (OME)

                                 OMERO.insight







  OMERO.insight Project
  ---------------------

  The OMERO.insight Project is a sub-project of the Open Microscopy Environment
  Project (OME, http://www.openmicroscopy.org/) that focuses on delivering a
  client for the visualization and manipulation of both image data and metadata
  maintained at an OMERO server site.
  OMERO.insight is completely written in Java.



  Directory Contents
  ------------------

  This directory is the repository of the software artifacts of the
  OMERO.insight Project. Its contents are as follows:

    + build: This directory contains the tools to compile, run, test, and
          deliver the application.
    + config: Various configuration files required by the application to run.
    + launch: Launcher scripts and installation instructions bundled with the
          default application distribution file. Its sub-dirs contain further
          resources to build platform-specific distributions.
    + SRC: Contains the application source files.
    + TEST: The test code.
    + README: This file.



  Building OMERO.insight
  ----------------------

  The compilation, testing, launch, and delivery of the application are
  automated by means of an Ant (http://ant.apache.org/) build file, located
  under the build directory. In order to perform a build, all you need is
  a JDK -- version 1.6 or later. Move to the build directory and, from the 
  command line, enter:
  
      java build
  
  This will display the available targets to compile, run, test, and create a
  distribution bundle. Use the target you wish, for example:
  
     java build all
  
  Because all the tools needed to build the software are already included in
  the build directory, you don't need to have Ant on your machine.
  If you wish to use Ant instead, you can still do it by using the build.xml
  file under the build directory. However, there are some dependencies to
  satisfy before; these are clearly documented in the build.xml file itself.



  Developing OMERO.insight
  ------------------------

  See http://www.openmicroscopy.org/site/support/omero5/developers/index.html#insight



  Licensing
  ---------

  This software is licensed under the terms of the GNU General Public
  License (GPL), the full text of which can be found in /lib/licenses/gpl.txt or
  the top level LICENSE.txt. See /lib/licenses/README and
  http://www.openmicroscopy.org/site/about/licensing-attribution for further details.



  Copyright
  ---------

  Copyright (C) 2006-2014 University of Dundee. All rights reserved.


