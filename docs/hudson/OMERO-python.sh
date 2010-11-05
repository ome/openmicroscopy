set -e
set -u
set -x

ulimit -n 8192
ulimit -a

#
# Run tests
#
./build.py -f components/tools/OmeroPy/build.xml -Dtest.with.fail=true test
./build.py -f components/tools/OmeroPy/build.xml -Dtest.with.fail=true integration

cd examples
python ../scons/scons.py builddir=$TEST run_py=1
