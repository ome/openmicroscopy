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

% Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
% All rights reserved.
%
% This program is free software; you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation; either version 2 of the License, or
% (at your option) any later version.
%
% This program is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License along
% with this program; if not, write to the Free Software Foundation, Inc.,
% 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

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
