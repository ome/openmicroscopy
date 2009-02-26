

#export PYTHONPATH      ?= $(HOME)/.hudson/root/lib/python2.4/site-packages:$(HOME)/.hudson/root/lib64/python2.4/site-packages:/usr/lib64/portage/pym
#export LD_LIBRARY_PATH ?= $(HOME)/.hudson/root/lib:$(HOME)/.hudson/root/lib64

restart: regrestart webrestart

start: regstart webstart

stop: regstop webstop

regrestart: regstop regstart

regstartold:
	export LD_LIBRARY_PATH=$(LD_LIBRARY_PATH) ; export PYTHONPATH=$(PYTHONPATH) ; \
	twistd -l log.reg --pidfile=pid.reg -y registry.py

regstart:
	twistd -l log.reg --pidfile=pid.reg -y registry.py

regstop:
	ls pid.reg && cat pid.reg | xargs kill || echo "No pid.reg"

webrestart: webstop webstart

webstart:
	twistd -l log.map --pidfile=pid.map -y generate.py

webstop:
	ls pid.map && cat pid.map | xargs kill || echo "No pid.map"

.PHONY: start stop restart regstart regstop regrestart webstart webstop webrestart

