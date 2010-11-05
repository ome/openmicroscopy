./build.py clean
./build.py
./build.py -f components/tools/OmeroJava/build.xml -Dtest.with.fail=true test
./build.py -f components/tools/OmeroJava/build.xml -Dtest.with.fail=true integration

cd examples
python ../scons/scons.py builddir=$TEST run_java=1
