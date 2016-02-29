import React from 'react';
import ReactDOM from 'react-dom';
import CentrePanel from './CentrePanel';

function renderCentrePanel(jstree, selected) {
    console.log("renderCentrePanel...");
    ReactDOM.render(
        <CentrePanel
            jstree={jstree}
            selected={selected}/>,
        document.getElementById('content_details')
    );
}

export default renderCentrePanel;