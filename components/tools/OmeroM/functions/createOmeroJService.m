function [omerojService] = createOmeroJService(iceConfig, username, password)


omerojService = omeroj.service.OmeroJService(java.lang.String(iceConfig));
omerojService.createSession(username, password);
