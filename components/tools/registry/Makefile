
export PYTHONPATH ?= "$(HOME)/root/lib/python2.4/site-packages"
export LD_LIBRARY_PATH ?= "$(HOME)/root/lib/"
export DYLD_LIBRARY_PATH ?= "$(HOME)/root/lib/"

restart: regrestart webrestart

start: regstart webstart

stop: regstop webstop

regrestart: regstop regstart

regstart:
	twistd -l log.reg --pidfile=pid.reg -y registry.py

regstop:
	ls pid.reg && cat pid.reg | xargs kill || echo "No pid.reg"

webrestart: stop start

webstart:
	twistd -l log.map --pidfile=pid.map -y generate.py

webstop:
	ls pid.map && cat pid.map | xargs kill || echo "No pid.map"

.PHONY: start stop restart regstart regstop regrestart webstart webstop webrestart

