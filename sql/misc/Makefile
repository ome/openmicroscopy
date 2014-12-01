#
# Copyright 2010 Glencoe Software, Inc. All rights reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

#
# This makefile is used to test database upgrades. It
# provides various targets for the upgrade script developer.
#
# Several databases will be created via psql:
#
#  PREV:     A database created from the previous db scripts
#  FRESH:    A database created directly from the db script
#  SIMPLE:   The PREV database upgraded with the generated script
#  UPGRADED: The PREV database upgraded with our upgrade script
#
# The default target ($TARGET/$UPGRADED.diff) will compare the
# old schema against an upgraded version of itself.
#

# Versions
PREVDBVERS ?= OMERO5.0
PREVDBPATCH ?= 0
CURRDBVERS ?= OMERO5.1DEV
CURRDBPATCH ?= 16
PREV ?= $(PREVDBVERS)__$(PREVDBPATCH)
CURR ?= $(CURRDBVERS)__$(CURRDBPATCH)

# Tools
OMERO_SOURCE ?= ../..
OMERO_HOME ?= $(OMERO_SOURCE)/dist
OMERO_EXE ?= $(OMERO_HOME)/bin/omero
DIFF_JAR ?= "apgdiff-2.3.jar"
DIFF_CMD ?= java -jar $(DIFF_JAR) --ignore-start-with
PSQL_HOST ?= localhost
PSQL_USER ?= omero
PSQL_CMD ?= env PGOPTIONS=--client-min-messages=warning psql -h $(PSQL_HOST) -U $(PSQL_USER) -q -v ON_ERROR_STOP=1 --pset pager=off
PSQL_CREATE ?= createdb -h $(PSQL_HOST) -U $(PSQL_USER)
PSQL_DROP ?= dropdb -h $(PSQL_HOST) -U $(PSQL_USER)
PSQL_DUMP ?= pg_dump -h $(PSQL_HOST) -U $(PSQL_USER)

# Naming
PREFIX=
TARGET ?= target
FRESH ?= FRESH-$(CURR)
SIMPLE ?= SIMPLE-$(CURR)
UPGRADED ?= UPGRADED-$(CURR)
TESTS ?= TESTS-$(CURR)

# Directories and files
DIR ?= $(OMERO_SOURCE)/sql/psql/$(CURR)/
SCRIPTDIR ?= $(DIR)
SCRIPT ?= $(PREV).sql
PYSCRIPT ?= $(DIR)/t/$(PREV).py
FIXTURE ?= $(DIR)/t/$(PREV).sql
TEST ?= $(DIR)/t/$(PREV).test

HAS_FIXTURE = test -e $(FIXTURE)
HAS_TEST = test -e $(TEST)

##################################################
# Functions: $(call name,var1,var2) ##############
##################################################

# Diff tries to produce SQL which can be added to $(SCRIPT)
# in order to make the two versions approach one another
MAKE_DIFF = $(DIFF_CMD) $(TARGET)/$(2).schema $(TARGET)/$(1).schema > $(TARGET)/$(2).diff

# Now assuming that users have installed plpgsql centrally
#   (grep "CREATE PROCEDURAL LANGUAGE plpgsql" $(TARGET)/$(1).sql ||\
#   createlang plpgsql $(PREFIX)$(1)) &&\

MAKE_DB = \
	($(PSQL_DROP) $(PREFIX)$(1) || echo 'DNE') &&\
	$(PSQL_CREATE) $(PREFIX)$(1) &&\
	$(PSQL_CMD) $(PREFIX)$(1) -f $(TARGET)/$(1).sql

##################################################
# Upgraded version ###############################
##################################################

$(TARGET)/$(UPGRADED).diff: $(TARGET)/$(FRESH).schema $(TARGET)/$(UPGRADED).schema
	$(call MAKE_DIFF,$(FRESH),$(UPGRADED))

$(TARGET)/$(UPGRADED).schema: $(TARGET)/$(UPGRADED).db
	(cd $(SCRIPTDIR) && $(PSQL_CMD) $(PREFIX)$(UPGRADED) -f $(SCRIPT))
	$(HAS_FIXTURE) && $(HAS_TEST) && $(PSQL_CMD) $(PREFIX)$(UPGRADED) -f $(TEST) || echo No test
	$(PSQL_DUMP) -s $(PREFIX)$(UPGRADED) > $(TARGET)/$(UPGRADED).schema

$(TARGET)/$(UPGRADED).db: $(TARGET)/$(UPGRADED).sql
	$(call MAKE_DB,$(UPGRADED))
	touch $(TARGET)/$(UPGRADED).db

$(TARGET)/$(UPGRADED).sql: $(TARGET)/$(PREV).sql
	$(HAS_FIXTURE) &&\
	cp $(DIR)/t/$(PREV).sql $(TARGET)/$(UPGRADED).sql ||\
	cp $(TARGET)/$(PREV).sql $(TARGET)/$(UPGRADED).sql

##################################################
# Fresh version ##################################
##################################################

$(TARGET)/$(SIMPLE).diff: $(TARGET)/$(FRESH).diff
	cp $(TARGET)/$(PREV).sql $(TARGET)/$(SIMPLE).sql
	$(call MAKE_DB,$(SIMPLE))
	$(call MAKE_DIFF,$(SIMPLE),$(FRESH))
	$(PSQL_CMD) $(PREFIX)$(UPGRADED) -f $(TARGET)/$(SIMPLE).diff

$(TARGET)/$(FRESH).diff: $(TARGET)/$(PREV).schema $(TARGET)/$(FRESH).schema
	$(call MAKE_DIFF,$(PREV),$(FRESH))

$(TARGET)/$(FRESH).schema: $(TARGET)/$(FRESH).db
	$(PSQL_DUMP) -s $(PREFIX)$(FRESH) > $(TARGET)/$(FRESH).schema

$(TARGET)/$(FRESH).db: $(TARGET)/$(FRESH).sql
	$(call MAKE_DB,$(FRESH))
	touch $(TARGET)/$(FRESH).db

$(TARGET)/$(FRESH).sql:
	mkdir -p $(TARGET)
	$(OMERO_EXE) db script -f $(TARGET)/$(FRESH).sql "$(CURRDBVERS)" "$(CURRDBPATCH)" ome


##################################################
# Previous version ###############################
##################################################

$(TARGET)/$(PREV).schema: $(TARGET)/$(PREV).sql
	$(call MAKE_DB,$(PREV))
	$(PSQL_DUMP) -s $(PREFIX)$(PREV) > $(TARGET)/$(PREV).schema

$(TARGET)/$(PREV).sql:
	mkdir -p $(TARGET)
	$(OMERO_EXE) db script -f $(TARGET)/$(PREV).sql $(PREVDBVERS) $(PREVDBPATCH) ome


#########################################
# PHONY targets #########################
#########################################

simple: $(TARGET)/$(SIMPLE).diff

freshbuild: clean build $(TARGET)/$(FRESH).diff

upgradedbuild: clean build $(TARGET)/$(UPGRADED).diff

build:
	cd $(OMERO_SOURCE) && ./build.py build-schema build-dist

clean:
	rm -f $(TARGET)/*.sql $(TARGET)/*.schema $(TARGET)/*.diff

rmdb:
	rm $(TARGET)/$(UPGRADED).db

rmdiff:
	rm $(TARGET)/$(UPGRADED).diff

rmsql:
	rm $(TARGET)/$(UPGRADED).sql

diff: $(TARGET)/$(UPGRADED).diff
	less -G $(TARGET)/$(UPGRADED).diff

vi:
	vi $(SCRIPTDIR)/$(SCRIPT)

schema:
	vi $(DIR)/schema.sql

data:
	vi $(DIR)/data.sql

views:
	vi $(DIR)/views.sql

fixture:
	vi $(FIXTURE)

pyscript:
	vi $(PYSCRIPT)

pyrun:
	python $(PYSCRIPT)
	$(PSQL_DUMP) $(PREFIX)$(PREV) > $(FIXTURE)

test:
	vi $(TEST)

psql:
	psql $(PREFIX)$(UPGRADED)

prev: $(TARGET)/$(PREV).schema
	psql $(PREFIX)$(PREV)

fresh: $(TARGET)/$(FRESH).schema
	psql $(PREFIX)$(FRESH)

.PHONY: fresh upgraded build clean rmdb rmdiff simple diff vi schema data views psql prev test fixture run pyscript
