set -e
set -u
set -x

export OMERO_BUILD=r"$SVN_REVISION"-"$OMERO_BRANCH"-b"$BUILD_NUMBER"

./build.py clean
./build.py build-default
./build.py release-docs
./build.py release-zip

# Creating source release
cd ..
rm -rf target/svn-export
svn export . target/svn-export
cd target/svn-export
zip -r ../OMERO.source-r"$SVN_REVISION"-"$OMERO_BRANCH".zip .
