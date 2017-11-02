import omero.scripts as s
import uuid

uuid = str(uuid.uuid4())
print("I am the script named %s." % uuid)
client = s.client(uuid, "simple script")
