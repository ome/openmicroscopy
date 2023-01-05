#!/usr/bin/env bash
# This script is used by a GitHub action raining daily.
# It checks if a new version of the "ome" dependencies
# is available and updates the value in etc/omero.properties

# Check the Java packages
dirs=("org/openmicroscopy/omero-blitz" "org/openmicroscopy/omero-common" "org/openmicroscopy/omero-gateway" "ome/OMEZarrReader")
for dir in "${dirs[@]}"
do
    : 
    values=(${dir//// })
    value=${values[${#values[@]}-1]}
    # get the latest version of the package
    repopath="https://artifacts.openmicroscopy.org/artifactory/ome.releases/${dir}"
    version=`curl -s ${repopath}/maven-metadata.xml | grep latest | sed "s/.*<latest>\([^<]*\)<\/latest>.*/\1/"`
    sed -i -e "s/versions.${value}=.*/versions.${value}=${version}/" etc/omero.properties
done

# Check the Python package.
packages=("omero-scripts")
for package in "${packages[@]}"
do
	:
    version=`curl -Ls https://pypi.org/pypi/$package/json | jq -r .info.version`
    sed -i -e "s/versions.${package}=.*/versions.${package}=${version}/" etc/omero.properties
done
if [ -f etc/omero.properties-e ]; then
    echo "file"
    rm -rf etc/omero.properties-e
fi
