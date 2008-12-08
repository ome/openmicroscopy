function openSharedWindow(url) {
    owindow = window.open(url, 'anew', config='height=600,width=850,left=50,top=50,toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,directories=no,status=no');
    if(!owindow.closed) owindow.focus();
}
