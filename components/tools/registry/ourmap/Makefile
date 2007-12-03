export PYTHONPATH ?= $(HOME)/root/lib/python2.4/site-packages/
export LD_LIBRARY_PATH ?= $(HOME)/root/lib/

restart: stop start

start:
	twistd -y mapparse.py

stop:
	kill `cat twistd.pid`

.PHONY: start stop restart

