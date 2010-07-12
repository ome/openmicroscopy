function [omerojavaService] = createOmeroJavaService(Hostname, username, password)

client = omero.client(java.lang.String(Hostname), 4064)
session = client.createSession(username, password)
omerojavaService = session.createGateway()
