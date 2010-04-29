This directory contains OmeroPy scripts which use the
OmeroScripts API. Those under the omero/ subdirectory
will be automatically distributed with all binary
builds. The others are works-in-progress.

Scripts which would like to rely on other scripts can
use:

    import omero.<sub_dir>.<script_name>

For this to work, the official script in question must
be properly importable, i.e.:

    def run():
        client = omero.scripts.client(...)

    if __name__ == "__main__":
        run()

