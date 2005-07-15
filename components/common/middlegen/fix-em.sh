#!/bin/bash

perl -i -pe 's/2.0/3.0/g;s/assigned/native/g' *.hbm.xml
