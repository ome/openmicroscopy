// function to handle submission of the form and opening results window...
jQuery.fn.runScript = function() {
    var $form = $(this);
    var scriptName = $("#scriptForm .scriptName").text();
    var newWindow=window.open('','','height=250,width=700,right=50');
    newWindow.document.write('<html><title>Results</title>');
    newWindow.document.write('<h3>Running ' + scriptName + '</h3>');
    newWindow.document.write('<p>Waiting for results...</p>');
    newWindow.document.write('</html>');
    newWindow.document.close();
    if (window.focus) {newWindow.focus()}
      
    var postData = $form.serialize();
    var actionUrl = $form.attr('action');
    $.post(actionUrl, postData, function(resultsUrl) {
        // set the url of the results window - will only update when script completes
        newWindow.location = resultsUrl;
    });
    return false;
};