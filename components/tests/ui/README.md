User Interface Scripts
======================

These scripts, and the implementations of the keywords they use,
specify automated operation of OMERO clients. This allows acceptance
testing of user interface behavior and detection of regressions.

Read about Robot Framework at
http://code.google.com/p/robotframework/

Before installing please visit 
https://code.google.com/p/robotframework/wiki/Installation

Requirements:
 * Install firefox version 21. It does not work with more recent versions
 * Install Python. Mininum is 2.5. Python 3.x not supported.

If you are planning to run the OMERO.insight and OMERO.web tests, 
you will have to run robotframework with python and with jython.
In that case it is easier to install robotframework using the 
setup script provided.

Setting up
----------
If you wish to set-up data, i.e. create a Project, dataset and
import 3 images in the dataset, you first need to run

./build.py test-compile
then
./build.py -f components/tests/ui/build.xml setup-db.

If you need more images, run the second command again. The images will be 
imported in the dataset created the first time.


OMERO.insight
-------------

Install Jython (2.5.4rc1) and within it use easy_install to install docutils and
robotframework. Or, if you can get pip working, even better.
Recommended to download the installer jar. See
http://www.jython.org/downloads.html

If you use Eclipse, then for developing test scripts you may find the
Swing Explorer plug-in helpful; read http://www.swingexplorer.com/

Read about SwingLibrary at
http://github.com/robotframework/SwingLibrary

Note that the convention in Insight is to name Swing components for
the kind of component that they are. For instance, the first window to
pop up is the "server window" and it includes a "username field" and a
"config server button" and similar.

The supplied SwingLibrary JAR has been adjusted by the script from
http://github.com/Rethought/swinglibrary_package so that it
incorporates a later revision of Abbot that corrects an issue with
Java 7. Those wishing to repeat the adjustment on a Mac may wish to
review the adjustment script and use homebrew to install wget and GNU
sed: when the script runs correctly, no errors should be glimpsed.

To actually run a tests in a given folder,

Replace ${omero.version} by the current version e.g. 4.4.8-DEV-ice34

export OMERO_CHECKOUT=~/src/openmicroscopy
CLASSPATH="$OMERO_CHECKOUT"/components/insight/OUT/app/libs/*:"$OMERO_CHECKOUT"/components/insight/OUT/dist/omero.insight.jar:"$OMERO_CHECKOUT"/lib/repository/swinglibrary-1.6.0a.jar:"$OMERO_CHECKOUT"/target/repository/java-ui-libraries-${omero.version} jybot "$OMERO_CHECKOUT"/components/tests/ui/testcases/insight/menus


To run all the insight tests, from the top level

./build.py -f components/tests/ui/build.xml ui-test-insight

To run the tests in a given folder under testcases/insight e.g.
./build.py -f components/tests/ui/build.xml ui-test-insight -DFOLDER=icons

The output of the tests can be found under 
components/tests/ui/target/reports/insight/*. One output directory will be created for each testcases directory.


To run the tests in a given test e.g.
./build.py -f components/tests/ui/build.xml ui-test-insight -DTEST=menus/context-menus.txt

The output of the test can be found under 
components/tests/ui/target/reports/insight/menus
i.e. The parent directory of the test itself.

OMERO.web
---------

After installing the robotframework,  you need to install the
robotframework-selenium2library, we are not planning to use Selenium1

If you have pip install, you can install the library with the following command

pip install robotframework-selenium2library

Read http://github.com/rtomac/robotframework-selenium2library

By default, the tests are run using the default browser i.e. firefox
If you want to run the tests on googlechrome, you need to install the chrome driver.
See https://code.google.com/p/chromedriver/downloads/list

If you are running the tests on Mac OS X, you can install install with the following command

brew install chromedriver


To run all the web tests on both firefox and chrom, from the top level

./build.py -f components/tests/ui/build.xml ui-test-web

To run all the web tests on firefox (respectively chrome) only

./build.py -f components/tests/ui/build.xml web-firefox

The output of the tests can be found under
components/tests/ui/target/reports/web/firefox

./build.py -f components/tests/ui/build.xml web-chrome

The output of the tests can be found under
components/tests/ui/target/reports/web/chrome


To run a single test under testcases/web

./build.py -f components/tests/ui/build.xml web-firefox -DTEST=tree.txt