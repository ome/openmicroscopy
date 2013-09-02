# Source this file to set environment variables used by the OMERO VM
# install scripts inside the guest.

export OMERO_JOB="${OMERO_JOB:-OMERO-stable-ice34}"
export OMERO_PREFIX="${OMERO_PREFIX:-/home/omero/OMERO.server}"

export OMERO_DB_NAME="${OMERO_DB_NAME:-omero}"
export OMERO_DB_USER="${OMERO_DB_USER:-omero}"
export OMERO_DB_PASS="${OMERO_DB_PASS:-omero}"

export OMERO_DATA_DIR="${OMERO_DATA_DIR:-/home/omero/OMERO.data}"
export OMERO_ROOT_PASS="${OMERO_ROOT_PASS:-omero}"

export OMERO_WEB_PORT="${OMERO_WEB_PORT:-8080}"

export ENABLE_OMERO_NO_PROCESSOR_FIX="${ENABLE_OMERO_NO_PROCESSOR_FIX:-1}"

