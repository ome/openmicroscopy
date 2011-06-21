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
./build.py -f components/tools/OmeroPy/build.xml -Dtest.with.fail=false test
./build.py -f components/tools/OmeroPy/build.xml -Dtest.with.fail=false integration

cd examples
python ../target/scons/scons.py run_py=1 no_cpp=1
