Validator

This is a TurboGears (http://www.turbogears.org) project. It can be
started by running the start-validator.py script.

The normal command to start it is:
python start-validator.py

This project was generated from a standard TurboGears template.

The following file have been edited/added
-----------------------------------------
./:
README.txt
dev.cfg - This file sets default directories and the server port.

./schema:
This folder has copies of the schema files that are used for validation. It's location can be changed in the file "dev.cfg".
ome-2007-07.xsd
ome-fc-tiff.xsd
ome-fc.xsd

./uploads:
This folder is used as the temporary store for the files being generated. It's location can be changed in the file "dev.cfg". The user the validator is running as needs to have write access to this folder.

./validator:
OmeValidator.py - main validation code
controllers.py - handles upload, file listing, and results
release.py - variables setting release information

./validator/static/css:
style.css

./validator/static/images:
ome_trac_back_top.png
ome_xml_trac_banner.png

./validator/templates: - page templates
fileslist.kid - list of files to validate
master.kid - used by other page templates
result.kid - results of a validation
validator.kid - main upload for validation page


The following files are as generated
------------------------------------

./:
sample-prod.cfg
setup.py
start-validator.py*
test.cfg

./Validator.egg-info:
PKG-INFO
SOURCES.txt
dependency_links.txt
not-zip-safe
paster_plugins.txt
requires.txt
sqlobject.txt
top_level.txt

./validator:
__init__.py
json.py
model.py

./validator/templates:
__init__.py

./validator/config:
__init__.py
app.cfg
log.cfg

./validator/sqlobject-history:

./validator/tests:
__init__.py
test_controllers.py
test_model.py
