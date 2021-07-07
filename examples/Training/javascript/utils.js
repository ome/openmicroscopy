async function makeJSONRequest(method, url, data) {
  const options = {
    method: method, // *GET, POST, PUT, DELETE, etc.
    credentials: 'include', // include, *same-origin, omit
  }
  if (method == 'POST') {
    const csrf_token = getCookie('csrftoken');
    options.headers = { 'x-csrftoken': csrf_token }
  }
  if (data) {
    options.body = data;
  }
  // Do the fetch and check status...
  const response = await fetch(url, options);
  if (response.status != 200) {
    throw response;
  }
  // If OK, parse JSON response into native JavaScript objects
  return response.json();
}


function getCookie(name) {
  var cookieValue = null;
  if (document.cookie && document.cookie != '') {
    var cookies = document.cookie.split(';');
    for (var i = 0; i < cookies.length; i++) {
      var cookie = cookies[i].trim();
      // Does this cookie string begin with the name we want?
      if (cookie.substring(0, name.length + 1) == (name + '=')) {
        cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
        break;
      }
    }
  }
  return cookieValue;
}


function getParameterByName(name, url = window.location.href) {
  name = name.replace(/[\[\]]/g, '\\$&');
  var regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)'),
    results = regex.exec(url);
  if (!results) return null;
  if (!results[2]) return '';
  return decodeURIComponent(results[2].replace(/\+/g, ' '));
}
