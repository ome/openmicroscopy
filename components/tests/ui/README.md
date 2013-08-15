User Interface Scripts
======================

These scripts, and the implementations of the keywords they use,
specify automated operation of OMERO clients. This allows acceptance
testing of user interface behavior and detection of regressions.

Read about Robot Framework at
http://code.google.com/p/robotframework/


OMERO.insight
-------------

Install Jython and within it use easy_install to install docutils and
robotframework. Or, if you can get pip working, even better.

If you use Eclipse, then for developing test scripts you may find the
Swing Explorer plug-in helpful; read http://www.swingexplorer.com/

Read about SwingLibrary at
http://github.com/robotframework/SwingLibrary

The supplied SwingLibrary JAR has been adjusted by the script from
http://github.com/Rethought/swinglibrary_package so that it
incorporates a later revision of Abbot that corrects an issue with
Java 7. Those wishing to repeat the adjustment on a Mac may wish to
review the adjustment script and use homebrew to install wget and GNU
sed: when the script runs correctly, no errors should be glimpsed.

To actually run a test script,

export OMERO_CHECKOUT=~/src/openmicroscopy
cd "$OMERO_CHECKOUT"/components/insight/OUT/app/
CLASSPATH=compiled/:libs/'*':"$OMERO_CHECKOUT"/lib/repository/swinglibrary-1.6.0a.jar:"$OMERO_CHECKOUT"/target/repository/ui-scripts-4.4.8-DEV-ice33.jar jybot "$OMERO_CHECKOUT"/components/ui-scripts/resources/scripts/insight/experiments/test-11210.rest

This horror will be addressed in due course.


OMERO.web
---------

Coming soon.

Read http://github.com/rtomac/robotframework-selenium2library
