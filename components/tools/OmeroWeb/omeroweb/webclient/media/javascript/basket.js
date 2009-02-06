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
                alert("Internal server error. Cannot add to basket.")
            }
        });
        
        var calculateCartTotal = function(total)
        {
            $('#cartTotal').html(total);
        };
    }
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

function copyToClipboard (productType, productId) {
    if (productId == null) {
        alert("No object selected.")
    } else {
        $.ajax({
            type: "POST",
            url: "/webclient/clipboard/", //this.href,
            data: "action=copy&productId="+productId+"&productType="+productType,
            contentType:'html',
            success: function(responce){
                alert(responce);
            },
            error: function(responce) {
                alert("Internal server error. Could not copy to clipboard.")
            }
        });
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