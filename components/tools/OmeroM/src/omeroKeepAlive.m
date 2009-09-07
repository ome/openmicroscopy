function keep_alive = omeroKeepAlive( omero_client )
% Create a Timer object which will periodically call "session.keepAlive([])"
%
%   keep_alive = omeroKeepAlive(c);
%
% If any invocation of the doKeepAlive callback fails, then execution will
% be terminated. To stop the timer manually, use:
%
%   stop(keep_alive);
%   delete(keep_alive);
%
% The Timer will be started before returning. On unloadOmero, all timers
% with the tag 'omeroKeepAlive' will be stopped and deleted to remove
% Java objects from the runtime.
%
% ----
%
% See: http://www.mathworks.com/access/helpdesk/help/techdoc/ref/timer.html
%
  function doKeepAlive(obj, event, string_arg)
    try
      omero_session = omero_client.getSession();
    catch
      %% disp('No session');
      return;
    end
    try
      omero_session.keepAlive([]);
      %% disp('Kept alive');
      return;
    catch EX
      %% disp('Lost connection');
      throw(EX);
    end
  end

  keep_alive = timer('Tag', 'omeroKeepAlive', 'TimerFcn', @doKeepAlive, 'StartDelay',10, 'ExecutionMode', 'fixedDelay', 'Period', 60);
  start(keep_alive);
end
