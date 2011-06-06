set -e
set -u
set -x

./build.py clean
./build.py
./build.py test-compile
./build.py -f components/tools/OmeroJava/build.xml -Dtest.with.fail=false test
./build.py -f components/tools/OmeroJava/build.xml -Dtest.with.fail=false integration

cd examples
python ../target/scons/scons.py run_java=1 no_cpp=1
