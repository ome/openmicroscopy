$(document).ready(function() 
    {
        $('#swapTree').click(function() { 
            var flag_l = true;
            if($('img#lhid_trayhandle_icon_left').attr('class')=='collapsed-left') {
                flag_l = false
            }
            if (flag_l) {
                $("#left_panel").hide();
                $("#swapTree").html('<img tabindex="0" src="/static/common/image/spacer.gif" class="collapsed-left" id="lhid_trayhandle_icon_left">');
                flag_l = false;
            } else {
                $("#left_panel").show();
                $("#swapTree").html('<img tabindex="0" src="/static/common/image/spacer.gif" class="expanded-left" id="lhid_trayhandle_icon_left">');
                flag_l = true;
            }
        });

        $('#swapMeta').click(function() { 
            var flag_r = true;
            if($('img#lhid_trayhandle_icon_right').attr('class')=='expanded-right') {
                flag_r = false
            }
            if (flag_r) {                        
                $("#right_panel").hide();
                $("#swapMeta").html('<img tabindex="0" src="/static/common/image/spacer.gif" class="expanded-right" id="lhid_trayhandle_icon_right">'); 
                flag_r = false;                        
            } else {
                $("#right_panel").css("width", "370px").show();
                $("#swapMeta").html('<img tabindex="0" src="/static/common/image/spacer.gif" class="collapsed-right" id="lhid_trayhandle_icon_right">'); 
                flag_r = true; 
            }
        });
        
})