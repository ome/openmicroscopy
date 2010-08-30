
// This script is used by the publications page for pagination, filtering, alternateRowColour
// and by 'entries' page for alternateRowColors only. 

jQuery.fn.alternateRowColors = function() {
  $('tbody tr', this).not('.filtered').filter(':odd')
    .removeClass('even').addClass('odd');
    $('tbody tr', this).not('.filtered').filter(':even')
        .removeClass('odd').addClass('even');
  return this;
};

$(document).ready(function() {
    
    // this is the only code for entries page
    $('table.alternateRows').alternateRowColors();
    
    // now for publications page....
    var $table = $('table.paginated');
    var filter_values = {};
    var filter_cols = [];
    
    var doFiltering = function() {
        // iterate through all data rows...
        $('tr:not(:has(th))', $table).each(function() {
            // for each column we have a filter value for...
            var showRow = true;
            for (var c=0; c< filter_cols.length; c++) {
                var cIndex = filter_cols[c];
                if (filter_values[cIndex].length > 0) {
                    var value = $('td', this).eq(cIndex).text();
                    var f = filter_values[cIndex].toLowerCase();
                    // check if the td text contains our filter value
                    if (value.toLowerCase().indexOf(f) == -1) {
                        showRow = false;
                    }
                }
            }
            if (showRow) {
                $(this).removeClass('filtered');
            } else {
                $(this).addClass('filtered');
            }
          });
        
        $table.trigger('page_controls');    // reset controls for pagination - set page = 0;
        $table.trigger('repaginate');
        $table.alternateRowColors();
    }
    
    // populate the list of filter_columns 
    // for each column (header)...
    $('th', $table).each(function(column) {
        var $field = $(this).children(".filter_field");
        // if we have any 'filter fields', save their values.
        if ($field.length > 0) {
            filter_cols.push(column);
            filter_values[column] = $field.attr('value');
            
            // bind the filtering to keyup of each filter field
            $field.keyup(function(event) {
                if (filter_values[column] != this.value) {
                    filter_values[column] = this.value;
                    doFiltering();
                }
            });
        }
    });
    // do filtering when page has loaded - in case there was any text. 
    doFiltering();
    
    
    var loadThumbs = function($tr) {
        var $img = $tr.find(".previewGif");
        var imgHref = $img.attr("id");
        var srcHref = $img.attr("src");
        if (imgHref != srcHref) {
            $img.attr('src', imgHref);
        }
    }
    
  $('table.paginated').each(function() {
    var currentPage = 0;
    var numPerPage = 20;
    var $table = $(this);
    $table.bind('repaginate', function() {
      $table.find('tbody tr').hide().not('.filtered')
          .slice(currentPage * numPerPage,
            (currentPage + 1) * numPerPage)
          .show().each(function() {
              loadThumbs($(this));
          })
    });
    
    var $pager = $('<div class="pager"></div>');
    
    $table.bind('page_controls', function() {
        currentPage = 0;
        var totalRows = $table.find('tbody tr').length;
        var numRows = $table.find('tbody tr').not('.filtered').length;
        var numPages = Math.ceil(numRows / numPerPage);
        //alert("page controls " + numRows + " pages " + numPages);
        $pager.empty();
        for (var page = 0; page < numPages; page++) {
          $('<span class="page-number"></span>').text(page + 1)
            .bind('click', {newPage: page}, function(event) {
              currentPage = event.data['newPage'];
              $table.trigger('repaginate');
              $(this).addClass('active')
                .siblings().removeClass('active');
            }).appendTo($pager).addClass('clickable');
        }
        $pager.append($('<span> Showing ' + numRows + ' / ' + totalRows + ' </span>'))
    });
    
    $table.trigger('page_controls');
    $pager.insertBefore($table)
        .find('span.page-number:first').click();
  });
  
  // load the EMDB entry details for a publication when the link is clicked
    $(".emdb_link").click(function(event) {
        var $link = $(this);
        var jsonLink = $link.attr('href');
        // Django will generate a link for the first entry - use this as template for other links
        var exampleLink = $link.siblings(".entry_link").attr('href');
        var exampleImageLink = $link.siblings(".image_link").attr('href');
        var exampleId = $link.siblings(".entry_link").text();
        var $entries_pane = $link.siblings(".entries_pane");     // we will display links here
        
        // json format is {"entryId": "Description", "1006": "Virus Molecule"}
        $.getJSON(jsonLink, function(data) {
            var html = "<table width='100%'>";
            $.each(data, function(entryId) {
                var eLink = exampleLink.replace(exampleId, entryId);
                var imgLink = exampleImageLink.replace(exampleId, entryId);
                html += "<tr><td><a href='"+ eLink + "'><img src='" + imgLink + "' class='previewGif' /></a></td>" + 
                        "<td><a href='"+ eLink + "'>" + entryId + "</a> " + data[entryId] + "</td></tr>";
            });
            html += "</table>";
            $entries_pane.append(html);
        });
        return false;
    })
});