
REM To prevent needing a restart, this script uses the
REM current user to run the service.

ECHO "Using current user for service: %USERDOMAIN%\%USERNAME%"
SET /P PASSWORD=Password:

cd %~dp0\..
python build.py
cd dist

python bin\omero admin stop

set OMERO_CONFIG=quickstart
dropdb -U postgres %OMERO_CONFIG%
createdb -U postgres %OMERO_CONFIG%
createlang -U postgres plpgsql %OMERO_CONFIG%
python bin\omero db script OMERO4.1 0 ome
psql -U postgres -f OMERO4.1__0.sql %OMERO_CONFIG%

set PYTHONPATH=%PYTHONPATH%;lib\python
python lib\python\omero\install\win_set_path.py

REM Required because of environment-less service
python bin\omero config def %OMERO_CONFIG%


mkdir data
python bin\omero config set omero.data.dir %CD%\data

python bin\omero config set omero.windows.user %USERDOMAIN%\%USERNAME%
python bin\omero config set omero.windows.pass "%PASSWORD%"
python bin\omero config set omero.db.user postgres
python bin\omero config set omero.db.name %OMERO_CONFIG%
python bin\omero admin start
