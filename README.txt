
-------------------------------------------------
   OMERO - OME Read-Only Web App
-------------------------------------------------

OMERO provides a query server which can be deployed
in various servlet servers (Tomcat, JBoss, etc.).
It directly accesses the Postgres DB of an existing
OME installation and returns OME Objects using 
the Hessian libraries from caucho.com. 


Note: OME_SRC = top directory containing 
      /components /lib project.xml etc.


TO WORK WITH THE OMERO REPOSITORY:

(1) Initialize environment: DB, Java, server, Maven

    swith to a user with access to the database (alternatively create an account)
    install Java and a Servlet Container (Tomcat, Jetty, Resin, or JBoss et al.)
    
    set JAVA_HOME  ( or export JAVA_HOME)   = ....
    set MAVEN_HOME ( or export MAVEN_HOME ) = OME_SRC/lib/maven/ 
    
    run: (*.bat also available)
    
    	OME_SRC/lib/maven/bin/install_repo.sh HOME_DIR/.maven/repository
	
    Note: you will need to run this _even if_ you already have maven 
          installed. It takes care of some missing libraries that are required 
	  for the installation.

    TODO: this Note can be removed as soon as we have a working OME-maven repository ! 

(2) Do necessary configuration:

    There are three components in omero:
    	components/jars/model
	components/jars/client
	components/wars/srv

    [A] See build.properties.example in each of these directories (if available). 
    These values can also be set in HOME_DIR/build.properties, 
    OME_SRC/build.properties, or on the maven command-line with -Dkey=value

    [B] Of most importance, however, are the JDBC connection settings in:
    OME_SRC/components/war/src/conf/spring.properties


(3) Install OMERO:
   
   Run OME_SRC/lib/maven/bin/maven. Copy the war-file produced under
   OME_SRC/components/war/srv/target to your servlet container. Enjoy!

   (Alternatively, follow the instructions below.)


-- INSTALLATION for TOMCAT --
(2) In home directory or omero install directory, 
    add "build.properties" file with the properties:
  
      maven.tomcat.username=you
      maven.tomcat.password=secret

   and possibly

      maven.tomcat.host...TODO

   This file should _not_ be put under revision control.

(3) Run:
   maven 

(4) _NOW_ you can run the test cases in client (before this there was no
    server)

-- INSTALLATION for JBOSS -
(2)
(3)


#########################
# The Client
#########################
