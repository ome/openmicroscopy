
python build.py clean
python build.py build-all

export RELEASE=Os
python build.py build-cpp

python build.py -f components/tools/OmeroCpp/build.xml test
python build.py -f components/tools/OmeroCpp/build.xml integration

cd examples
python ../target/scons/scons.py run_cpp=1
