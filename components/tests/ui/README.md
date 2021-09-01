User Interface Scripts
======================

These scripts, and the implementations of the keywords they use,
specify automated operation of OMERO clients. This allows acceptance
testing of user interface behavior and detection of regressions.

**Requirements**:
 * [Robot Framework](http://robotframework.org)
 * Python 2.x and [pip](https://pip.pypa.io/en/stable/) (Python 3.x is not supported)
 * one or both of these web browsers (Web tests)
    + Firefox - [requires geckodriver](#web-browser-drivers)
    + Chrome - [requires chromedriver](#web-browser-drivers)
 * robotframework-selenium2library (Web tests)

Before installing please visit
https://github.com/robotframework/robotframework/blob/master/INSTALL.rst

Testing frameworks
------------------

#### [selenium](http://github.com/rtomac/robotframework-selenium2library)

Selenium runs tests _one at a time (recommended way to run tests for now)_.
You can install the library with the following:

```
pip install robotframework-selenium2library
```

#### [pabot](https://github.com/mkorpela/pabot/)

Pabot is a relativily new library for running robot tests and allows them to run in _parallel_.
This can significantly reduce testing times, however not all tests pass and there could be
unexpected results. To install robotframework-pabot:

``` 
pip install robotframework-pabot
```

Web browser drivers
-------------------

By default, the tests are run using the default browser i.e. Firefox.
If you are running the tests on Mac OS X, you can install with the following

```
brew install geckodriver
```

NB: If this takes too long, and manually installing geckodriver
from [releases](https://github.com/mozilla/geckodriver/releases)
doesn't work, try using Chrome.

To run the tests on Chrome, you need to install the chromedriver.
See https://sites.google.com/a/chromium.org/chromedriver/downloads
Download the correct driver for your version of Chrome and save it to
a directory on your `$PATH`.

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
ICE_CONFIG=$(pwd)/components/tests/ui/resources/robot_ice.config omero --path components/tests/ui/plugins robot config > components/tests/ui/resources/config.txt
```

Note this command will create the Robot configuration file using the
configuration properties of the server as well as the Ice configuration file
read from the `ICE_CONFIG` environment variable.


OMERO.web
---------

To run the tests locally, you will first need to start OMERO.web, please see

https://docs.openmicroscopy.org/latest/omero/sysadmins/unix/install-web.html

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
./build.py -f components/tests/ui/build.xml web-browser
./build.py -f components/tests/ui/build.xml web-browser -DBROWSER=chrome
```

The output of the tests can be found repectively under

```
components/tests/ui/target/reports/web/firefox/
components/tests/ui/target/reports/web/chrome/
```

To run a single test under `testcases/web` in firefox, for example

```
./build.py -f components/tests/ui/build.xml web-browser -DTEST=annotate_test.txt
```

with the ouput being available under

```
components/tests/ui/target/reports/web/firefox/
```

It is possible to rerun tests that failed either in a different browser or the same browser.
By default, the tests will be first run in Firefox and rerun in Chrome.

To run all the web tests on Firefox then Chrome, use

```
./build.py -f components/tests/ui/build.xml web-browser-rerun
```

To run all the web tests on Chrome then Firefox, use

```
./build.py -f components/tests/ui/build.xml web-browser-rerun -DBROWSER=chrome -DTARGETBROWSER=firefox
```

It is often useful to merge the outputs of the run instead of aggregating the results.
To merge the outputs use

```
./build.py -f components/tests/ui/build.xml merge-results
```

To merge the outputs after running a `rerun` task first on Firefox use

```
./build.py -f components/tests/ui/build.xml merge-results-run
```

otherwise

```
./build.py -f components/tests/ui/build.xml merge-results-run -DBROWSER=chrome
```

To run all the web tests in parallel using robotframework-pabot on Firefox or Chrome, use

```
./build.py -f components/tests/ui/build.xml web-browser-pabot
./build.py -f components/tests/ui/build.xml web-browser-pabot -DBROWSER=chrome
```
