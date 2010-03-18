function isCheckedById(name) { 
    var checked = $("input[name='"+name+"']:checked").length; 
    if (checked == 0) { return false; } else { return true; } 
}

var calculateCartTotal = function(total)
{
    $('#cartTotal').html(total);
};

function manyAddToBasket() {     
    if (!isCheckedById("image")) {//&& !isCheckedById("dataset") && !isCheckedById("plate")) {
        alert ("Please select at least one image. Currently you cannot add other objects to basket."); 
    } else { 
        manyToBasket($("input[type='checkbox']:checked"));
    }
};

function toBasket (productType, productId) {
    if (productId == null) {
        alert("No object selected.")
    } else {
        $.ajax({
            type: "POST",
            url: "/webclient/basket/update/", //this.href,
            data: "action=add&productId="+productId+"&productType="+productType,
            contentType:'html',
            success: function(responce){
                if(responce.match(/(Error: ([a-z][A-Z]+))/gi)) {
                    alert(responce)
                } else {
                    calculateCartTotal(responce);
                }
            },
            error: function(responce) {
                alert(responce)
                alert("Internal server error. Cannot add to basket.")
            }
        });
    }
};

function manyToBasket (productArray) { 
    var productListQuery = "action=addmany";
    productArray.each(function() {
        if(this.checked) {
            productListQuery += "&"+this.name+"="+this.id;
        }
    });
    
    $.ajax({
        type: "POST",
        url: "/webclient/basket/update/", //this.href,
        data: productListQuery,
        contentType:'html',
        success: function(responce){
            if(responce.match(/(Error: ([a-z][A-Z]+))/gi)) {
                alert(responce)
            } else {
                calculateCartTotal(responce);
            }
        },
        error: function(responce) {
            alert("Internal server error. Cannot add to basket.")
        }
    });
};

function fromBasket (productType, productId) {
    if (productId == null) {
        alert("No object selected.")
    } else {
        $.ajax({
            type: "POST",
            url: "/webclient/basket/update/", //this.href,
            data: "action=del&productId="+productId+"&productType="+productType,
            contentType:'html',
            cache:false,
            success: function(responce){
                if(responce.match(/(Error: ([a-z][A-Z]+))/gi)) {
                    alert(responce)
                } else {
                    calculateCartTotal(responce);
                    var rm = "#"+productType+"-"+productId
                    $(rm).remove()
                }
            },
            error: function(responce) {
                alert("Internal server error. Cannot remove from basket.")
            }
        });
        
        var calculateCartTotal = function(total)
        {
            if (total == 0) {
                window.location = "/webclient/basket/";
            } else {
                $('#cartTotal').html(total);
            }
        };
    }
}


function manyUnlink(parent) { 
    if (!isCheckedById("dataset") && !isCheckedById("image") && !isCheckedById("plate")) {
        alert ("Please select at least one object"); 
    } else { 
        unlink($("input[type='checkbox']:checked"), parent);
    }
};

function selectAll() {
    $("INPUT[type='checkbox']").attr('checked', $('#checkAllAuto').is(':checked'));   
}

function unlink (productArray, parent) { 
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

function manyCopyToClipboard() { 
    if (isCheckedById("project") || isCheckedById("screen")) {
        alert ("You can only copy datasets, images or plates. Please uncheck projects and screens."); 
    } else if (!isCheckedById("dataset") && !isCheckedById("plate") && !isCheckedById("image")) {
        alert ("Please select at least one dataset, image or plate."); 
    } else if (isCheckedById("dataset") && isCheckedById("plate")) {
        alert ("Please select only datasets, images or plates."); 
    } else { 
        copyToClipboard($("input[type='checkbox']:checked"));
    }
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

function changeView(view) { 
    var rel = $("div#content_details").attr('rel');
    if(rel=='orphaned') {
        $("div#content_details").html('<p>Loading data... please wait <img src="/webclient/static/images/tree/spinner.gif"/></p>');
        $("div#content_details").load('/webclient/load_data/orphaned/?view='+view);
    } else {
        $("div#content_details").html('<p>Loading data... please wait <img src="/webclient/static/images/tree/spinner.gif"/></p>');
        $("div#content_details").load('/webclient/load_data/dataset/'+rel+'/?view='+view);
    }
    return false;
};

function openPopup(url) {
    owindow = window.open(url, 'anew', config='height=600,width=850,left=50,top=50,toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,directories=no,status=no');
    if(!owindow.closed) owindow.focus();
    return false;
}


function saveMetadata (image_id, metadata_type, metadata_value) {
    if (image_id == null) {
        alert("No image selected.")
    } else {
        $($('#id_'+metadata_type).parent()).append('<img src="../images/tree/spinner.gif"/>');
        $.ajax({
            type: "POST",
            url: "/webclient/metadata/image/"+image_id+"/", //this.href,
            data: "matadataType="+metadata_type+"&metadataValue="+metadata_value,
            contentType:'html',
            cache:false,
            success: function(responce){
                $($('#id_'+metadata_type).parent().find('img')).remove()
            },
            error: function(responce) {
                $($('#id_'+metadata_type).parent().find('img')).remove()
                alert("Cannot save new value for '"+metadata_type+"'.")
            }
        });
    }
}
