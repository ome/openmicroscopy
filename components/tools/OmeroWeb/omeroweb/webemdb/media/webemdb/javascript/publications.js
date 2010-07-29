
// Finished code: Sorting and Paging

jQuery.fn.alternateRowColors = function() {
  $('tbody tr', this).not('.filtered').filter(':odd')
    .removeClass('even').addClass('odd');
    $('tbody tr', this).not('.filtered').filter(':even')
        .removeClass('odd').addClass('even');
  //$('tbody tr:even', this)
  //  .removeClass('odd').addClass('even');
  return this;
};

$(document).ready(function() {
    
    var $table = $('table.sortable');
    var filter_values = {};
    var filter_cols = [];
    // for each column (header)
    $('th', $table).each(function(column) {
        var $field = $(this).children(".filter_field");
        // if we have any 'filter fields'...
        if ($field.length > 0) {
            filter_cols.push(column);
            filter_values[column] = "";
            $field.keyup(function(event) {
                if (filter_values[column] != this.value) {
                    filter_values[column] = this.value;
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
            });
        }
    });
    
    // switch between table layout and 'flow layout' where everything in each row floats left. 
    $("#flowLayout").click(function() {
        $(".publication td").css('float', 'left');
        $(".tableHeader th").css('float', 'left');
    });
    $("#tableLayout").click(function() {
        $(".publication td").css('float', 'none');
        $(".tableHeader th").css('float', 'none');
    });
    
    
    // for our sortable table...
  $('table.sortable').each(function() {
    var $table = $(this);
    $table.alternateRowColors();
    // for each column (header)
    $('th', $table).each(function(column) {
      var $header = $(this);
      var findSortKey;
      // decide what we're going to sort on, based on class of th
      if ($header.is('.sort-alpha')) {
        findSortKey = function($cell) {
            // sort by the sort-key text within each cell, or text()
          return $cell.find('.sort-key').text().toUpperCase()
            + ' ' + $cell.text().toUpperCase();
        };
      }
      else if ($header.is('.sort-numeric')) {
        findSortKey = function($cell) {
          var key = $cell.text().replace(/^[^\d.]*/, '');
          key = parseFloat(key);
          return isNaN(key) ? 0 : key;
        };
      }
       
       // we've got a sort key - now for sorting...   
      if (findSortKey) {
        $header.addClass('clickable').hover(function() {
          $header.addClass('hover');
        }, function() {
          $header.removeClass('hover');
        }).children(".sort").click(function() {               // bind sorting to click of th
          var sortDirection = 1;
          if ($header.is('.sorted-asc')) {
            sortDirection = -1;
          }
          // get all the jquery rows as DOM objects...
          var rows = $table.find('tbody > tr').get();
          $.each(rows, function(index, row) {
            var $cell = $(row).children('td').eq(column);
            row.sortKey = findSortKey($cell);
          });
          // sort them
          rows.sort(function(a, b) {
            if (a.sortKey < b.sortKey) return -sortDirection;
            if (a.sortKey > b.sortKey) return sortDirection;
            return 0;
          });
          // add them back to table in order
          $.each(rows, function(index, row) {
            $table.children('tbody').append(row);
            row.sortKey = null;     // remove to prevent memory leaks
          });
          // set the class of the header wrt sort order
          $table.find('th').removeClass('sorted-asc')
            .removeClass('sorted-desc');
          if (sortDirection == 1) {
            $header.addClass('sorted-asc');
          }
          else {
            $header.addClass('sorted-desc');
          }
          // for each cell in the sorted col, add sorted class
          $table.find('td').removeClass('sorted')
            .filter(':nth-child(' + (column + 1) + ')')
            .addClass('sorted');
        // repaint the rows alternate colours and re-paginate 
          $table.alternateRowColors();
          $table.trigger('repaginate');
        });
      }
    });
  });
});

$(document).ready(function() {
    
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
});