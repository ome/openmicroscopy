function [omerojavaService] = createOmeroJavaService(iceConfig, username, password)

client = omero.client(java.lang.String(iceConfig))
sf = client.createSession(username, password)
omerojavaService = sf.createGateway()
