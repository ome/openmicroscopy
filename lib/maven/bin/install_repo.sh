#!/bin/sh
REPO_DIR=$1
if [ -z "$REPO_DIR" ]; then echo "usage: $0 [repository directory]"; exit; fi
if [ -z "$MAVEN_HOME" ]; then echo "MAVEN_HOME must be set"; exit; fi
if [ ! -f $REPO_DIR/dom4j/jars/dom4j-1.4-dev-8.jar ]; then 
  mkdir -p $REPO_DIR/dom4j/jars
  cp $MAVEN_HOME/lib/dom4j-1.4-dev-8.jar $REPO_DIR/dom4j/jars
fi
if [ ! -f $REPO_DIR/ant/jars/ant-1.5.3-1.jar ]; then 
  mkdir -p $REPO_DIR/ant/jars
  cp $MAVEN_HOME/lib/ant-1.5.3-1.jar $REPO_DIR/ant/jars
fi
if [ ! -f $REPO_DIR/ant/jars/ant-optional-1.5.3-1.jar ]; then 
  mkdir -p $REPO_DIR/ant/jars
  cp $MAVEN_HOME/lib/ant-optional-1.5.3-1.jar $REPO_DIR/ant/jars
fi
if [ ! -f $REPO_DIR/commons-betwixt/jars/commons-betwixt-1.0-beta-1.20030111.103454.jar ]; then 
  mkdir -p $REPO_DIR/commons-betwixt/jars
  cp $MAVEN_HOME/lib/commons-betwixt-1.0-beta-1.20030111.103454.jar $REPO_DIR/commons-betwixt/jars
fi
if [ ! -f $REPO_DIR/commons-digester/jars/commons-digester-1.4.1.jar ]; then 
  mkdir -p $REPO_DIR/commons-digester/jars
  cp $MAVEN_HOME/lib/commons-digester-1.4.1.jar $REPO_DIR/commons-digester/jars
fi
if [ ! -f $REPO_DIR/commons-jelly/jars/commons-jelly-20030902.160215.jar ]; then 
  mkdir -p $REPO_DIR/commons-jelly/jars
  cp $MAVEN_HOME/lib/commons-jelly-20030902.160215.jar $REPO_DIR/commons-jelly/jars
fi
if [ ! -f $REPO_DIR/commons-jelly/jars/commons-jelly-tags-ant-1.0.jar ]; then 
  mkdir -p $REPO_DIR/commons-jelly/jars
  cp $MAVEN_HOME/lib/commons-jelly-tags-ant-1.0.jar $REPO_DIR/commons-jelly/jars
fi
if [ ! -f $REPO_DIR/commons-jelly/jars/commons-jelly-tags-define-20030211.142932.jar ]; then 
  mkdir -p $REPO_DIR/commons-jelly/jars
  cp $MAVEN_HOME/lib/commons-jelly-tags-define-20030211.142932.jar $REPO_DIR/commons-jelly/jars
fi
if [ ! -f $REPO_DIR/commons-jelly/jars/commons-jelly-tags-util-20030211.141939.jar ]; then 
  mkdir -p $REPO_DIR/commons-jelly/jars
  cp $MAVEN_HOME/lib/commons-jelly-tags-util-20030211.141939.jar $REPO_DIR/commons-jelly/jars
fi
if [ ! -f $REPO_DIR/commons-jelly/jars/commons-jelly-tags-xml-20040613.030723.jar ]; then 
  mkdir -p $REPO_DIR/commons-jelly/jars
  cp $MAVEN_HOME/lib/commons-jelly-tags-xml-20040613.030723.jar $REPO_DIR/commons-jelly/jars
fi
if [ ! -f $REPO_DIR/commons-graph/jars/commons-graph-0.8.1.jar ]; then 
  mkdir -p $REPO_DIR/commons-graph/jars
  cp $MAVEN_HOME/lib/commons-graph-0.8.1.jar $REPO_DIR/commons-graph/jars
fi
if [ ! -f $REPO_DIR/commons-jexl/jars/commons-jexl-1.0-beta-1.jar ]; then 
  mkdir -p $REPO_DIR/commons-jexl/jars
  cp $MAVEN_HOME/lib/commons-jexl-1.0-beta-1.jar $REPO_DIR/commons-jexl/jars
fi
if [ ! -f $REPO_DIR/commons-logging/jars/commons-logging-1.0.3.jar ]; then 
  mkdir -p $REPO_DIR/commons-logging/jars
  cp $MAVEN_HOME/lib/commons-logging-1.0.3.jar $REPO_DIR/commons-logging/jars
fi
if [ ! -f $REPO_DIR/commons-httpclient/jars/commons-httpclient-2.0.jar ]; then 
  mkdir -p $REPO_DIR/commons-httpclient/jars
  cp $MAVEN_HOME/lib/commons-httpclient-2.0.jar $REPO_DIR/commons-httpclient/jars
fi
if [ ! -f $REPO_DIR/werkz/jars/werkz-20040426.222000.jar ]; then 
  mkdir -p $REPO_DIR/werkz/jars
  cp $MAVEN_HOME/lib/werkz-20040426.222000.jar $REPO_DIR/werkz/jars
fi
if [ ! -f $REPO_DIR/commons-beanutils/jars/commons-beanutils-1.6.1.jar ]; then 
  mkdir -p $REPO_DIR/commons-beanutils/jars
  cp $MAVEN_HOME/lib/commons-beanutils-1.6.1.jar $REPO_DIR/commons-beanutils/jars
fi
if [ ! -f $REPO_DIR/commons-cli/jars/commons-cli-1.0-beta-2.jar ]; then 
  mkdir -p $REPO_DIR/commons-cli/jars
  cp $MAVEN_HOME/lib/commons-cli-1.0-beta-2.jar $REPO_DIR/commons-cli/jars
fi
if [ ! -f $REPO_DIR/commons-collections/jars/commons-collections-2.1.jar ]; then 
  mkdir -p $REPO_DIR/commons-collections/jars
  cp $MAVEN_HOME/lib/commons-collections-2.1.jar $REPO_DIR/commons-collections/jars
fi
if [ ! -f $REPO_DIR/commons-grant/jars/commons-grant-1.0-beta-4.jar ]; then 
  mkdir -p $REPO_DIR/commons-grant/jars
  cp $MAVEN_HOME/lib/commons-grant-1.0-beta-4.jar $REPO_DIR/commons-grant/jars
fi
if [ ! -f $REPO_DIR/commons-io/jars/commons-io-20030203.000550.jar ]; then 
  mkdir -p $REPO_DIR/commons-io/jars
  cp $MAVEN_HOME/lib/commons-io-20030203.000550.jar $REPO_DIR/commons-io/jars
fi
if [ ! -f $REPO_DIR/commons-lang/jars/commons-lang-2.0.jar ]; then 
  mkdir -p $REPO_DIR/commons-lang/jars
  cp $MAVEN_HOME/lib/commons-lang-2.0.jar $REPO_DIR/commons-lang/jars
fi
if [ ! -f $REPO_DIR/forehead/jars/forehead-1.0-beta-5.jar ]; then 
  mkdir -p $REPO_DIR/forehead/jars
  cp $MAVEN_HOME/lib/forehead-1.0-beta-5.jar $REPO_DIR/forehead/jars
fi
if [ ! -f $REPO_DIR/log4j/jars/log4j-1.2.8.jar ]; then 
  mkdir -p $REPO_DIR/log4j/jars
  cp $MAVEN_HOME/lib/log4j-1.2.8.jar $REPO_DIR/log4j/jars
fi
if [ ! -f $REPO_DIR/which/jars/which-1.0.jar ]; then 
  mkdir -p $REPO_DIR/which/jars
  cp $MAVEN_HOME/lib/which-1.0.jar $REPO_DIR/which/jars
fi
if [ ! -f $REPO_DIR/xml-apis/jars/xml-apis-1.0.b2.jar ]; then 
  mkdir -p $REPO_DIR/xml-apis/jars
  cp $MAVEN_HOME/lib/endorsed/xml-apis-1.0.b2.jar $REPO_DIR/xml-apis/jars
fi
if [ ! -f $REPO_DIR/xerces/jars/xerces-2.4.0.jar ]; then 
  mkdir -p $REPO_DIR/xerces/jars
  cp $MAVEN_HOME/lib/endorsed/xerces-2.4.0.jar $REPO_DIR/xerces/jars
fi
if [ ! -f $REPO_DIR/plexus/jars/plexus-0.6.jar ]; then 
  mkdir -p $REPO_DIR/plexus/jars
  cp $MAVEN_HOME/lib/plexus-0.6.jar $REPO_DIR/plexus/jars
fi
if [ ! -f $REPO_DIR/maven/jars/maven-jelly-tags-1.0.1.jar ]; then 
  mkdir -p $REPO_DIR/maven/jars
  cp $MAVEN_HOME/lib/maven-jelly-tags-1.0.1.jar $REPO_DIR/maven/jars
fi
