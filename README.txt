
-------------------------------------------------
   OMERO - OME Read-Only Web App
-------------------------------------------------

OMERO provides a query server which can be deployed
in various servlet servers (Tomcat, JBoss, etc.).
It directly accesses the Postgres DB of an existing
OME installation and returns OME Objects using 
the Hessian libraries from caucho.com. 

For information on installation and developing omero,
please see the files under docs/ .

The contents of this directory ("OMERO_HOME") are:

 README.txt:	
	this file

 components:	
	directory containing artifacts for the build

 project.xml:	
	a Maven Project Object Model (POM) defining the build

 project.properties:
	environment variables for the build (global)

 build.properties:
	environment variables for a single user/installation

 log4j.properties:
	logging configuration for all components

 docs:
	documentation. Currently of important are

         * BUILDING.txt  if you've checked out from source, and
         * INSTALL.txt   for running the server.

 lib:
	libraries necessary for build including maven itself.





TODO: LICENCES.

