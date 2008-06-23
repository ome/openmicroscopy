function [omerojavaService] = createOmeroJavaService(iceConfig, username, password)


omerojavaService = omerojava.service.OmeroJavaService(java.lang.String(iceConfig));
omerojavaService.createSession(username, password);
