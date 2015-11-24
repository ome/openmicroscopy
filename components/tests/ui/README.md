User Interface Scripts
======================

These scripts, and the implementations of the keywords they use,
specify automated operation of OMERO clients. This allows acceptance
testing of user interface behavior and detection of regressions.

Requirements:
 * Robot Framework
 * Python 2.5 or above, Python 3.x is not supported.
 * Jython 2.5 (Insight tests)
    + higher versions may not be supported, we use 2.5.4rc1
 * one or both of these web browsers (Web tests)
    + Firefox version 21. It may not work with some more recent versions.
    + Chrome
 * robotframework-selenium2library (Web tests)
 * chromedriver (Web tests using Chrome)

Read about Robot Framework at
http://code.google.com/p/robotframework/

Before installing please visit
https://code.google.com/p/robotframework/wiki/Installation

Note that the script jybot, required for the Insight tests, is not
installed by default when using pip install, see the above page for
more details.

Jython can be installed using the installer JAR at
http://www.jython.org/downloads.html. You may need to set `JYTHON_HOME`
and `JYTHONPATH` depending on your installation.

If you are planning to run the OMERO.insight and OMERO.web tests,
you will have to run robotframework with Python and with Jython.
In that case it is easier to install robotframework using the
`setup.py` scripts provided.

After installing the robotframework,  you need to install the
robotframework-selenium2library, we are not planning to use Selenium1.
See http://github.com/rtomac/robotframework-selenium2library

If you have pip installed, you can install the library with the following

```
pip install robotframework-selenium2library
```

By default, the tests are run using the default browser i.e. Firefox
If you want to run the tests on Chrome, you need to install the chromedriver.
See https://code.google.com/p/chromedriver/downloads/list

If you are running the tests on Mac OS X, you can install with the following

```
brew install chromedriver
```

Setting up
----------

If you wish to set-up the data required for the Robot test, you first need to
run the robot setup script (assuming the `ICE_CONFIG` environment variable is
properly configured) from the `dist` folder of the server:

```
cd dist/
bash ../components/tests/ui/robot_setup.sh
```

This command will create a `robot_ice.config` file containing the credentials
for the robot user. To run the Robot Framework tests, you will need a valid
configuration file under ``components/tests/ui/resources/config.txt``. To
generate this configuration file from a running server, you can use the CLI
robot plugin, e.g. for a  local server:

```
cd ../
mv dist/robot_ice.config components/tests/ui/resources/
ICE_CONFIG=$(pwd)/components/tests/ui/resources/robot_ice.config dist/bin/omero --path components/tests/ui/plugins robot config > components/tests/ui/resources/config.txt
```

Note this command will create the Robot configuration file using the
configuration properties of the server as well as the Ice configuration file
read from the `ICE_CONFIG` environment variable.

All components
--------------

To run the entire test suite for Insight, Web and CLI you should use

```
./build.py -f components/tests/ui/build.xml test-all
```

The results can then be aggregated using

```
./build.py -f components/tests/ui/build.xml aggregate-results
```

The aggregated output of the tests can be found under

```
components/tests/ui/target/reports/
```

OMERO.insight
-------------

If you use Eclipse, then for developing test scripts you may find the
Swing Explorer plug-in helpful; read http://www.swingexplorer.com/

Read about SwingLibrary at
http://github.com/robotframework/SwingLibrary

Note that the convention in Insight is to name Swing components for
the kind of component that they are. For instance, the first window to
pop up is the "server window" and it includes a "username field" and a
"config server button" and similar.

The version of the SwingLibrary library has an issue with Java 7. The script
from http://github.com/Rethought/swinglibrary_package provides an adjustement
to the JAR correcting this issue. Those  wishing to repeat the fix on a Mac
may wish to review the adjustment script and use Homebrew to install wget
and GNU sed: when the script runs correctly, no errors should be glimpsed.
In the long term, those maintaining our Insight testing framework should
periodically review
https://github.com/robotframework/SwingLibrary/issues?state=open and
watch issues like #41 and #47 whose resolution may suggest changes to
our code.

To run all the Insight tests, use

```
./build.py -f components/tests/ui/build.xml ui-test-insight
```

The output of the tests can be found under

```
components/tests/ui/target/reports/insight/
```

with one output directory created for each testcase directory.

To run the tests in a given directory, those under `testcases/insight/icons`
for example, use

```
./build.py -f components/tests/ui/build.xml ui-test-insight -DTEST=icons
```

The output of the tests can be found under

```
components/tests/ui/target/reports/insight/icons/
```

To run the tests in a given test

```
./build.py -f components/tests/ui/build.xml ui-test-insight -DTEST=menus/context-menus.txt
```

The output of the test can be found under

```
components/tests/ui/target/reports/insight/menus/
```

i.e. the parent directory of the test itself.

OMERO.web
---------

To run the tests locally, you will first need to start OMERO.web, please see

http://www.openmicroscopy.org/site/support/omero5.2/sysadmins/unix/install-web.html

or

http://www.openmicroscopy.org/site/support/omero5.2/sysadmins/windows/install-web.html

for more information.

To run all the web tests on both Firefox and Chrome, use

```
./build.py -f components/tests/ui/build.xml ui-test-web
```

The output of the tests can be found under

```
components/tests/ui/target/reports/web/firefox/
components/tests/ui/target/reports/web/chrome/
```

for each browser.

To run all the web tests on only Firefox or Chrome respectively, use

```
./build.py -f components/tests/ui/build.xml web-firefox
./build.py -f components/tests/ui/build.xml web-chrome
```

The output of the tests can be found repectively under

```
components/tests/ui/target/reports/web/firefox/
components/tests/ui/target/reports/web/chrome/
```

To run a single test under `testcases/web`, for example

```
./build.py -f components/tests/ui/build.xml web-firefox -DTEST=tree_test.txt
```

with the ouput being available under

```
components/tests/ui/target/reports/web/firefox/
```


OMERO CLI
---------

To run all the CLI tests, from the top level

```
./build.py -f components/tests/ui/build.xml test-cli
```

The output of the tests can be found under

```
components/tests/ui/target/reports/cli/
```
