$(document).ready(function() 
    {
        var flag_l = true

        $('#swapTree').click(function() { 
            if (flag_l) {
                $("#left_panel").hide();
                $("#swapTree").html('<img tabindex="0" src="/webclient/static/images/tree/spacer.gif" class="collapsed-left" id="lhid_trayhandle_icon_left">');
                flag_l = false;
            } else {
                $("#left_panel").show();
                $("#swapTree").html('<img tabindex="0" src="/webclient/static/images/tree/spacer.gif" class="expanded-left" id="lhid_trayhandle_icon_left">');
                flag_l = true;
            }
        });
        
        var flag_r = false;

        $('#swapMeta').click(function() { 
            if (flag_r) {                        
                $("#right_panel").hide();
                $("#swapMeta").html('<img tabindex="0" src="/webclient/static/images/tree/spacer.gif" class="expanded-right" id="lhid_trayhandle_icon_right">'); 
                flag_r = false;                        
            } else {
                $("#right_panel").css("width", "370px").show();
                $("#swapMeta").html('<img tabindex="0" src="/webclient/static/images/tree/spacer.gif" class="collapsed-right" id="lhid_trayhandle_icon_right">'); 
                flag_r = true; 
            }
        });
        
})