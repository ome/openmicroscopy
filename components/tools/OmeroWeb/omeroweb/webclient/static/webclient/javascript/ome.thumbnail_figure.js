//
// Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
// All rights reserved.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//

$(document).ready(function() {


    // Basically what we're doing here is supplementing the form fields
    // generated from the script itself to make a nier UI.
    // We use these controls to update the paramter fields themselves,
    // as well as updating the Figure preview.

    $("select[name=Tag_IDs]")
        .chosen({placeholder_text:'Choose one or more groups'})
        .change(function(evt, data) {
            console.log(data, data.deselected, data.selected);
        });

});