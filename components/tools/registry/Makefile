
#export PYTHONPATH ?= "$(HOME)/root/lib/python2.4/site-packages"

restart: stop start

start:
	twistd -y registry.py

stop:
	kill `cat twistd.pid`

.PHONY: start stop restart
