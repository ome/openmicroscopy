
python build.py build-cpp

export RELEASE=Os
python build.py build-cpp

python build.py -f components/tools/OmeroCpp/build.xml test
python build.py -f components/tools/OmeroCpp/build.xml integration

cd examples
python ../scons/scons.py run_cpp=1
