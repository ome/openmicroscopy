slice2java --tie --output-dir=target/generated/src/ -Iresources/slice -Itarget/generated/resources -I$ICE_HOME/slice $*
slice2cpp --output-dir=src/cpp/generated -Iresources/slice -Itarget/generated/resources -I$ICE_HOME/slice $*
