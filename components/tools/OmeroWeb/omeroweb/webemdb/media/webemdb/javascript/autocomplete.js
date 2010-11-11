$(document).ready(function() {
    var $searchField = $('#entry-text');
    $searchField.attr('autocomplete', 'off');
    var $autocomplete = $('<ul class="autocomplete"></ul>')
        .hide()
        .insertAfter($searchField);
    var autoComUrl = $('#autocompleteUrl').attr('href');
    
    // called when user clicks or uses up or down keys. 
    var setSelectedItem = function(item) {
        selectedItem = item;

        if (selectedItem === null) {
          $autocomplete.hide();
          return;
        }

        if (selectedItem < 0) {
          selectedItem = 0;
        }
        if (selectedItem >= $autocomplete.find('li').length) {
          selectedItem = $autocomplete.find('li').length - 1;
        }
        $autocomplete.find('li').removeClass('selected')
          .eq(selectedItem).addClass('selected');
        $autocomplete.show();
      };
    
    // called when the user hits enter, or clicks on a drop-down option
      var populateSearchField = function() {
          var entryId = $autocomplete.find('li').eq(selectedItem).text().substring(0,4)
          $searchField.val(entryId);
          setSelectedItem(null);
          var exampleEntryLink = $("#autocompleteEntryLink").attr('href');
          var link = exampleEntryLink.replace('1001', entryId);
          window.location = link;
        };
        
    // Handle 'Enter' to choose currently selected option
    $searchField.keypress(function(event) {
        if (event.keyCode == 13 && selectedItem !== null) {
          // User pressed enter key.
          populateSearchField();
          event.preventDefault();
        }
        // hide the autocomplete after we've lost focus
      }).blur(function(event) {
        setTimeout(function() {
          setSelectedItem(null);
        }, 250);
      });
        
    // Auto-complete functionality called on keyup....
    $searchField.keyup(function(event) {
        if (event.keyCode > 40 || event.keyCode == 8) {
          // Keys with codes 40 and below are special (enter, arrow keys, escape, etc.).
          // Key code 8 is backspace.
        $.ajax({
            'url': autoComUrl,
            'data': {'entry': $searchField.val()},
            'dataType': 'json',
            'type': 'GET',
            'success': function(data) {
                if (data.length) {
                    $autocomplete.empty();
                    $.each(data, function(index, term) {
                        $('<li></li>').text(term[0] + ":  " + term[1])
                            .appendTo($autocomplete)
                            .mouseover(function() {
                                setSelectedItem(index);
                            })
                            .click(populateSearchField);
                    });
                    setSelectedItem(0);
                }
                else {
                    setSelectedItem(null);
                }
            }
        });
        }
        else if (event.keyCode == 38 && 
                                     selectedItem !== null) {
          // User pressed up arrow.
          setSelectedItem(selectedItem - 1);
          event.preventDefault();
        }
        else if (event.keyCode == 40 && 
                                     selectedItem !== null) {
          // User pressed down arrow.
          setSelectedItem(selectedItem + 1);
          event.preventDefault();
        }

        else if (event.keyCode == 27 && selectedItem !== null) {
          // User pressed escape key.
          setSelectedItem(null);
        }
    })
});