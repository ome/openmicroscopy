*** Settings ***
Documentation     Tests Cut and Paste actions.

Resource          ../../resources/config.txt
Resource          ../../resources/web/login.txt
Resource          ../../resources/web/tree.txt

Suite Setup         User "${USERNAME}" logs in with password "${PASSWORD}"

*** Test Cases ***

Test Cut Paste Image
    [Documentation]     Cut an existing Image to /remove/ it to Orphaned and Paste it back

    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Select Experimenter
    ${pId}    ${datasetId}    ${imageId}        Select And Expand Project Dataset Image
    Wait Until Element is Visible               xpath=//li[@data-id='${datasetId}']//li[@data-id='${imageId}']
    Click Element                               id=cutButton
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Click Element                    refreshButton

    ${nodeId}=                                  Select Orphaned Images Section
    Wait Until Page Contains Element            xpath=//li[@id='${nodeId}']
    Wait Until Page Contains Element            xpath=//li[@id='${nodeId}']//li[@data-id='${imageId}']
    # Dataset should be selected, paste back
    Select Dataset By Id                        ${datasetId}    
    Click Element                               id=pasteButton
    Dataset Should Contain Image                ${imageId}     ${datasetId}

Test Cut Paste Orphaned Image
    [Documentation]    Cut an orphaned Image and paste in back into a Dataset.
    
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Select Experimenter
    ${nodeId}                                   Select Orphaned Images Section
    ${imageId}                                  Select First Orphaned Image    
    Click Element                               id=cutButton
    Page Should Contain Element                 xpath=//li[@id='${nodeId}']//li[@data-id='${imageId}']
    ${dId}                                      Select First Dataset
    Click Element                               id=pasteButton    
    Dataset Should Contain Image                ${imageId}     ${dId}
    Page Should Not Contain Element             xpath=//li[@id='${nodeId}']//li[@data-id='${imageId}']

Test Cut Paste Dataset
    [Documentation]     Create 2 Projects and a Dataset. Cut and Paste the Dataset.

    Wait Until Keyword Succeeds             ${TIMEOUT}    ${INTERVAL}     Reload Page
    Select Experimenter
    ${p1id}=                                Create project      test Cut-Paste TO here
    ${p2id}=                                Create project      test Cut-Paste FROM here
    ${did}=                                 Create Dataset
    Wait Until Keyword Succeeds             ${TIMEOUT}    ${INTERVAL}     Click Element                                   refreshButton
    # Check hierarchy
    Select Project By Id                    ${p2id}
    Wait Until Element Is Visible           xpath=//li[@data-id='${p2id}']/ul/li[@data-id='${did}']     ${WAIT}
    Select Dataset By Id                    ${did}
    # Wait until metadata panel loads
    Wait Until Page Contains                Dataset ID:          30
    Click Element                           id=cutButton
    # POST a /move/ action - wait for Orphaned Dataset
    Wait Until Element Is Visible           xpath=//div[@id='dataTree']/ul/li/ul/li[@data-id='${did}']  ${WAIT}
    Wait Until Element Is Visible           xpath=//li[@id='${treeRootId}']/ul/li[@data-id='${did}']    ${WAIT}
    Select Project By Id                    ${p1id}
    Click Element                           id=pasteButton
    # Another /move/ to different Project - Dataset should NOT be in first Project
    # Need to expand the Paste-To Project to reveal Dataset
    ${projectNode1}                         Wait For Project Node                                       ${p1id}
    ${projectNode2}                         Wait For Project Node                                       ${p2id}
    Select Project By Id                    ${p1id}
    Wait Until Element Is Visible           xpath=//li[@id='${projectNode1}']/ul/li[@data-id='${did}']     ${WAIT}
    Page Should Not Contain Element         xpath=//li[@id='${projectNode2}']/ul/li[@data-id='${did}']

    Select Project By Id                    ${p1id}
    Delete Container

    Select Project By Id                    ${p2id}
    Delete Container

Test Cut Paste Plate
    [Documentation]    Cut a Plate and paste it onto another screen

    Wait Until Keyword Succeeds             ${TIMEOUT}    ${INTERVAL}     Reload Page
    Select Experimenter
    ${s1id}=                                Create Screen      test Cut-Paste TO here
    Wait Until Keyword Succeeds             ${TIMEOUT}    ${INTERVAL}     Click Element                    refreshButton

    ${s2id}=                                Select First Screen With Children
    ${screenNode}                           Wait For Screen Node                 ${s2id}
    ${plateId}=                             Select First Plate
    Click Element                           id=cutButton    
    
    Wait Until Keyword Succeeds             ${TIMEOUT}    ${INTERVAL}   Page Should Not Contain Element    xpath=//li[@id='${screenNode}']//li[@data-id='${plateId}']
    Select Screen By Id                     ${s1id}
    ${screenNode1}                          Wait For Screen Node                                           ${s1id}
    Click Element                           id=pasteButton
    Click Node                              ${screenNode1}                     
    Wait Until Page Contains Element        xpath=//li[@id='${screenNode1}']//li[@data-id='${plateId}']

    # Cut and paste Plate to original Screen
    Select First Plate
    Click Element                           id=cutButton
    Click Node                              ${screenNode}
    Click Element                           id=pasteButton

    # Delete new Screen
    Select Screen By Id                     ${s1id}
    Delete Container

[Teardown]    Close Browser
