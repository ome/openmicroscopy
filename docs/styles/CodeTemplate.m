% Copyright (C) 2012 University of Dundee & Open Microscopy Environment.
% All rights reserved.
%
% This program is free software; you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation; either version 2 of the License, or
% (at your option) any later version.
%
% This program is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License along
% with this program; if not, write to the Free Software Foundation, Inc.,
% 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

% REMEMBER:
% line length is 80 chars (may be more occasionally for long string literals)
% no hard-tabs; use soft-tabs of 4 spaces

classdef CodeTemplate <  MovieData
    % Put class description here.
    % Format descriptions using (basic) HTML as needed.
    % Use {@link target name} to reference fields and methods.
    % Use the "code" tag to enclose any symbol that is contained in the source
    % code and that is not to be referenced -- for example, a keyword or a
    % variable name.
    %
    % Excluding the author clause, which is optional, all other clauses
    % below are required. Just cut and paste.
    %
    % @author Your Name, user at example.com
    % @since OMERO-Beta4.4
    
    properties(Constant = true)
        
        % One-line description may have this style.
        A_CONSTANT = 1;
        
    end
    
    properties (GetAccess = private)
        
        % If you need more than one line to describe a member field, use
        % this common style.
        aMemberField;        
        anotherField;
        
    end
    
    methods (Access = public)
        
        function aMethod(self, param1, param2) %a space after commas 
            % Put single-line method description here.
            % Method signature is described following the order below:
            %
            % Input:
            %     param1 A tab before description.
            %     param2 A tab before description.
            % 
            % Output:
            %     Object A tab before description.
            
            k = 0; % a space between each symbol pair, no space before semicolon
            % optional line after vars declaration
            while (k < 10)    % a space between keywords and parentheses
                if (k == 3 || k == -1)  % a space between condition and operator
                    x = int64(k + 1);    % a space between cast and symbols
                    break;
                    
                else
                    k = methodX(par1, par2, par3); % a space after commas
                    
                end
                
                
                try
                    % some more code...
                    
                catch MException
                    
                    % exception handling code...
                end
                
                switch k
                    case 0
                        methodY();
                        methodZ();
                        break;
                    case 1
                        methodZ();   % never on the same line as the 'case' statement
                        break;
                    otherwise
                        % do something else
                end
                
                
            end
        end
    end
end
