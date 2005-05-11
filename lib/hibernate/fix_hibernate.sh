#!/bin/sh
REPO_DIR=$1
if [ -z "$REPO_DIR" ]; then echo "usage: $0 [repository directory]"; exit; fi
if [ -z "$MAVEN_HOME" ]; then echo "MAVEN_HOME must be set"; exit; fi
if [ ! -f $REPO_DIR/hibernate/jars/asm-hibernate.jar ]; then 
  mkdir -p $REPO_DIR/hibernate/jars
  cp asm-hibernate.jar $REPO_DIR/hibernate/jars
fi
if [ ! -f $REPO_DIR/hibernate/jars/jta-hibernate.jar ]; then 
  mkdir -p $REPO_DIR/hibernate/jars
  cp jta-hibernate.jar $REPO_DIR/hibernate/jars
fi
