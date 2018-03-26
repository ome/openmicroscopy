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
            data.submit();

            $("#import_info").show();
            $('#import_progress').css('width', progress + '%');
            $('#import_status').text("Uploading...");
        },
        dataType: 'json',
        progressall: function (e, data) {
            var progress = parseInt(data.loaded / data.total * 100, 10);
            console.log('progress', progress);
            $('#import_progress').css('width', progress + '%');

            if (progress === 100) {
                $('#import_status').text("Importing...");
            }
        },

        done: function (e, data) {
            console.log('done', e, data);
            $("#import_spinner").remove();
            // var html = data.result.images.map(
            //     function(i){
            //         return ("<li class='row' data-id='" + i.id + "' data-type='image'>" +
            //             "<div class='image'>" +
            //             "<a href='" + WEBCLIENT.URLS.webindex + "img_detail/" + i.id + "/'>" +
            //             "<img width='65px' height='65px' src='"+ WEBCLIENT.URLS.webindex + "render_thumbnail/" + i.id + "/'>" +
            //             "</a></div></li>");

            //     }).join("");
            // $("#dataIcons").append(html);

            $("#import_info").hide();
        }
    });
});
