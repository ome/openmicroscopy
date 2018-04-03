/*
 * Copyright (c) 2018 University of Dundee. & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

$(function () {

    $('#fileupload').fileupload({
        singleFileUploads: false,
        add: function (e, data) {
            var html = "<li id='import_spinner' class='row'><div class='image'><img width='65px' height='65px' src='"+ WEBCLIENT.URLS.static_webgateway + "img/spinner.gif'></div></li>";
            $("#dataIcons").append(html);

            console.log("add", data);

            var pathNames = data.files.map(f => f.relativePath + f.name);
            console.log(pathNames);

            // Add pathNames to FormData based on import_form
            var formData = new FormData(document.getElementById("import_form"));
            for (var i=0; i<pathNames.length; i++){
                console.log(pathNames[i]);
                formData.append('pathNames', pathNames[i]);
            }
            data.formData = formData;

            data.submit();

            $("#import_info").show();
            $('#import_progress').css('width', '0');
            $('#import_status').text("Uploading...");
        },
        dataType: 'json',
        progressall: function (e, data) {
            // Progress of file upload....
            var progress = parseInt(data.loaded / data.total * 100, 10);
            console.log('progress', progress);
            $('#import_progress').css('width', progress + '%');
        },

        done: function (e, data) {
            console.log('done', e, data);
            // Upload is done...
            $('#import_status').text("Importing...");

            // Start pinging for import progress
            function importProgress() {
                console.log('importProgress...')
                $.getJSON(WEBCLIENT.URLS.webindex + 'import_progress/',
                    function(data){
                        console.log('data', data);
                        if (data.status == "in progress") {
                            setTimeout(importProgress, 500);
                        } else {
                            $("#import_spinner").remove();
                            $("#import_info").hide();

                            if (data.images) {
                                var html = data.images.map(
                                    function(i){
                                        return ("<li class='row' data-id='" + i.id + "' data-type='image'>" +
                                            "<div class='image'>" +
                                            "<a href='" + WEBCLIENT.URLS.webindex + "img_detail/" + i.id + "/'>" +
                                            "<img width='65px' height='65px' src='"+ WEBCLIENT.URLS.webindex + "render_thumbnail/" + i.id + "/'>" +
                                            "</a></div></li>");

                                    }).join("");
                                $("#dataIcons").append(html);
                            }
                        }
                    }
                );
            }
            importProgress();
        }
    });
});
