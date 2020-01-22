import os


def workdir(worker):
    home = os.path.expanduser("~")
    job = os.environ.get("JOB_NAME", "unknown_job")
    path = [home, "omero", "pytest", job, worker]
    return os.sep.join(path)


def pytest_configure(config):
    if not hasattr(config, 'slaveinput'):
        workerid = 'main'
        os.environ["OMERO_USERDIR"] = workdir(workerid)
