/** 
 * A static utility class for accessing browser-neutral event properties and
 * methods.
 */
function EventUtils() {
	throw 'RuntimeException: EventUtils is a static utility class and may not be instantiated';
}

EventUtils.addEventListener = function(target, type, callback, captures) {
	var result = true;
	if (target.addEventListener) {
		target.addEventListener(type, callback, captures);
	} else if (target.attachEvent) {
		result = target.attachEvent('on' + type, callback, captures);
	} else {
		// IE 5 Mac and some others
		target['on'+type] = callback;
	}

	return result;
}

EventUtils.findTarget = function(e, allowTextNodes) {
    var target;
	if (window.event) {
		target = window.event.srcElement;
	} else if (e) {
		target = e.target;
	} else { 
		// we can't find it, just use window
		target = window;
	}

	if (!allowTextNodes && target.nodeType == 3) {
		target = target.parentNode;
	}

	return target;
}

/**
 * @return {x, y}
 */
EventUtils.getMousePosition = function(e) {
	var posx = 0;
	var posy = 0;
	if (!e)
	{
		e = window.event;
	}

	if (e.pageX || e.pageY)
	{
		posx = e.pageX;
		posy = e.pageY;
	}
	else if (e.clientX || e.clientY)
	{
		posx = e.clientX + document.body.scrollLeft;
		posy = e.clientY + document.body.scrollTop;
	}

	return { x : posx, y : posy };
}

function TextSelectionEvent(selectedText, mousePosition) {
	this.selectedText = selectedText;
	this.x = mousePosition.x;
	this.y = mousePosition.y;
}
