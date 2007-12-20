
export PYTHONPATH ?= "$(HOME)/root/lib/python2.4/site-packages"
export LD_LIBRARY_PATH ?= "$(HOME)/root/lib/:$(HOME)/root/lib64/"
export DYLD_LIBRARY_PATH ?= "$(HOME)/root/lib/"

restart: regrestart webrestart

start: regstart webstart

stop: regstop webstop

regrestart: regstop regstart

regstart:
	LD_LIBRARY_PATH=$(LD_LIBRARY_PATH) twistd -l log.reg --pidfile=pid.reg -y registry.py

regstop:
	ls pid.reg && cat pid.reg | xargs kill || echo "No pid.reg"

webrestart: stop start

webstart:
	twistd -l log.map --pidfile=pid.map -y generate.py

webstop:
	ls pid.map && cat pid.map | xargs kill || echo "No pid.map"

.PHONY: start stop restart regstart regstop regrestart webstart webstop webrestart

