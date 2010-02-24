$(document).ready(function() 
{
    function isCheckedById(name) { 
        var checked = $("input[name='"+name+"']:checked").length; 
        if (checked == 0) { return false; } else { return true; } 
    }

    $("#addToBasket").click(function() {     
        if (!isCheckedById("image")) {//&& !isCheckedById("dataset") && !isCheckedById("plate")) {
            alert ("Please select at least one image. Currently you cannot add other objects to basket."); 
        } else { 
            manyToBasket($("input[type='checkbox']:checked"));
        }

    }); 

    $("#unlink").click(function() { 
        var parent = $("#unlink").attr('value');
        if (!isCheckedById("dataset") && !isCheckedById("image") && !isCheckedById("plate")) {
            alert ("Please select at least one object"); 
        } else { 
            manyUnlink($("input[type='checkbox']:checked"), parent);
        }
    });

    $("#copyToClipboard").click(function() { 
        if (isCheckedById("project") || isCheckedById("screen")) {
            alert ("You can only copy datasets, images or plates. Please uncheck projects and screens."); 
        } else if (!isCheckedById("dataset") && !isCheckedById("plate") && !isCheckedById("image")) {
            alert ("Please select at least one dataset, image or plate."); 
        } else if (isCheckedById("dataset") && isCheckedById("plate")) {
            alert ("Please select only datasets, images or plates."); 
        } else { 
            copyToClipboard($("input[type='checkbox']:checked"));
        }
    });

});

function selectAll() {
    $("INPUT[type='checkbox']").attr('checked', $('#checkAllAuto').is(':checked'));   
}

function manyUnlink (productArray, parent) { 
    var productListQuery = "parent="+parent;
    productArray.each(function() {
        if(this.checked) {
            productListQuery += "&"+this.name+"="+this.id;
        }
    });
    $.ajax({
        type: "POST",
        url: "/webclient/action/removemany/", //this.href,
        data: productListQuery,
        contentType:'html',
        success: function(responce){
            if(responce.match(/(Error: ([a-z][A-Z]+))/gi)) {
                alert(responce)
            } else {
                window.location.replace("");
            }
        },
        error: function(responce) {
            alert("Internal server error. Cannot add to basket.")
        }
    });
};

function copyToClipboard (productArray) {
    var productListQuery = "action=copy";
    if (productArray.length > 0 ) {
        productArray.each(function() {
            if(this.checked) {
                productListQuery += "&"+this.name+"="+this.id;
            }
        });
    } else {
        productListQuery += "&"+productArray.name+"="+productArray.id;
    }
    $.ajax({
        type: "POST",
        url: "/webclient/clipboard/", //this.href,
        data: productListQuery,
        contentType:'html',
        success: function(responce) {
            if(responce.match(/(Error: ([a-z][A-Z]+))/gi)) {
                alert(responce)
            } else {
                alert(responce)
            }
        },
        error: function(responce) {
            alert("Internal server error. Cannot add to basket.")
        }
    });
};

function treeCopyToClipboard(productType, productId) {
    if (productId == null) {
        alert("No object selected.");
    } else {
        input = $('<input type="checkbox" checked/>').attr('name', productType).attr('id', productId);
        copyToClipboard(input);
    }
};

function pasteFromClipboard (destinationType, destinationId, url) {
    if (destinationId == null) {
        alert("No object selected.")
    } else {
        $.ajax({
            type: "POST",
            url: "/webclient/clipboard/", //this.href,
            data: "action=paste&destinationId="+destinationId+"&destinationType="+destinationType,
            contentType:'html',
            success: function(responce){
                if(responce.match(/(Error: ([a-z][A-Z]+))/gi)) {
                    alert(responce)
                } else {
                    window.location = url
                }
            },
            error: function(responce) {
                alert("Internal server error. Could not be pasted.")
            }
        });
    }
};


function cleanClipboard (productType, productId) {
    if (productId == null) {
        alert("No object selected.")
    } else {
        $.ajax({
            type: "POST",
            url: "/webclient/clipboard/", //this.href,
            data: "action=clean",
            contentType:'html',
            success: function(responce){
                alert(responce);
            },
            error: function(responce) {
                alert(responce)
            }
        });
    }
};

