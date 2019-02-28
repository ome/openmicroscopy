#!/usr/bin/env bash

set -e
set -u
set -x
apt-get update
apt-get install -y ant\
      python-pip python-tables python-virtualenv python-yaml python-jinja2 \
      zlib1g-dev python-pillow python-numpy python-sphinx \
      libssl-dev libbz2-dev libmcpp-dev libdb++-dev libdb-dev \
      zeroc-ice-all-dev

pip install --upgrade 'pip<10' setuptools scc

# TODO: unpin pip when possible
# openjdk:8 is "stretch" or Debian 9
pip install https://github.com/ome/zeroc-ice-py-debian9/releases/download/0.1.0/zeroc_ice-3.6.4-cp27-cp27mu-linux_x86_64.whl
