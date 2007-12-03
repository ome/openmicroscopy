export PYTHONPATH ?= /tmp/geoip/lib/python2.4/site-packages/
export LD_LIBRARY_PATH ?= /tmp/geoip/lib/

restart: stop start

start:
	twistd -y mapparse.py

stop:
	kill `cat twistd.pid`

.PHONY: start stop restart

