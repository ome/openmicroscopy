
-------------------------------------------------
   OMERO - OME Remote Objects Server v.3.0.M2
-------------------------------------------------

.................................................
.WARNING: Omero is currently in development mode.
.         and should be considered BETA.        .
.................................................


For information on installation and developing omero,
please see the OMER Wiki and the development Trac at : 

  http://cvs.openmicroscopy.org.uk/tiki/
  http://trac.openmicroscopy.org.uk/

Especially:

* Getting Started:
  http://cvs.openmicroscopy.org.uk/tiki/tiki-index.php?page=Omero+Getting+Started

* Omero Design:
  http://cvs.openmicroscopy.org.uk/tiki/tiki-index.php?page=Omero+Design

* Development Roadmap:
  https://trac.openmicroscopy.org.uk/roadmap

The contents of this directory ("OMERO_HOME") are:

 README.txt:	
	this file

 components:	
	directory containing artifacts for the build

 pom.xml:	
	a Maven Project Object Model (POM) defining the 
	build dependencies

 omero.properties:
	property values for the build (global)

 hibernate.properties:
	property values specifically for Hibernate 

 log4j.properties:
	logging configuration for all components

 docs:
	documentation, license (LICENSE), and
        various example files.

 lib:
	libraries necessary for ant build including ant itself.


