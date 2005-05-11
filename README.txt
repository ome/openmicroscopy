
-------------------------------------------------
   OMERO - OME Read-Only Web App
-------------------------------------------------

OMERO provides a query server which can be deployed
in multiple servlet servers (Tomcat, JBoss, etc.).
It directly accesses the Postgres DB of an existing
OME installation and returns OME Objects using 
the Hessian libraries from caucho.com. 


Note: OME_SRC = top directory containing 
      /components /lib project.xml etc.


TO WORK WITH THE OMERO REPOSITORY:

(1) Initialize Maven:

    set JAVA_HOME  ( or export JAVA_HOME)   = ....
    set MAVEN_HOME ( or export MAVEN_HOME ) = OME_SRC/lib/maven/ 
    
    run: (*.bat also available)
    
    	OME_SRC/lib/maven/bin/install_repo.sh HOME_DIR/.maven/repository
	
    cd OME_SRC/lib/hibernate and run:
    	
	fix_hibernate.sh HOME_DIR/.maven/repository

    Note: these may be soon automatized in the build script.

(2) Do necessary configuration:

    [A] See build.properties.example for what can be set at the top level. 
    These values can be set in HOME_DIR/build.properties, 
    OME_SRC/build.properties, in the top level of any component, or on the 
    command-line with -Dkey=value

    [B] Of most importance, however, are the JDBC connection settings in:
    OME_SRC/components/war/src/conf/spring.properties


(3) Install OMERO:
   
   Run OME_SRC/lib/maven/bin/maven. Copy the war-file produced under
   OME_SRC/components/war/srv/target to your servlet container. Enjoy!

   (Alternatively, follow the instructions below.)


-- INSTALLATION for TOMCAT --
(2) In home directory or omero install directory, 
    add "build.properties" file with the properties:
  
      maven.tomcat.username=josh
      maven.tomcat.password=moore

   and possibly

      maven.tomcat.host...TODO

   This file should _not_ be put under revision control.

(3) Run:
   maven 


-- INSTALLATION for JBOSS -
(2)
(3)


#########################
# The Client
#########################
