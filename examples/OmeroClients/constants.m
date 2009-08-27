sz=omero.constants.MESSAGESIZEMAX.value;
to=omero.constants.CONNECTTIMEOUT.value/1000;
disp(sprintf('By default, no method call can pass more than %d kb',sz));
disp(sprintf('By default, client.createSession() will wait %d seconds for a connection', to));
