client = omero.client();
sudoClient = omero.client();

try
    sf = client.createSession('root','ome');
    sessionSvc = sf.getSessionService();

    p = omero.sys.Principal();
    p.name = 'root'; % Can change to any user
    p.group = 'user';
    p.eventType = 'User';
    
    sudoSession = sessionSvc.createSessionWithTimeout( p, 3*60*1000 ); % 3 minutes to live
    
    sudoSf = sudoClient.joinSession( sudoSession.getUuid().getValue() );
    sudoAdminSvc = sudoSf.getAdminService();
    disp(sudoAdmin.Svc.getEventContext().userName);
    
catch ME
    
    sudoClient.closeSession();
    client.closeSession();

end
