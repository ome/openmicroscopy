set -e
set -u
set -x

ulimit -n 8192
ulimit -a

#
# Run tests
#
./build.py clean
./build.py
./build.py -f components/tools/OmeroFS/build.xml  -Dtest.with.fail=false test
./build.py -f components/tools/OmeroFS/build.xml  -Dtest.with.fail=false integration

