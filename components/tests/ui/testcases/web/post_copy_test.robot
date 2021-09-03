*** Settings ***
Documentation     Tests Copy and Paste actions.

Resource          ../../resources/config.txt
Resource          ../../resources/web/login.txt
Resource          ../../resources/web/tree.txt

Suite Setup         User "${USERNAME}" logs in with password "${PASSWORD}"

*** Test Cases ***

Test Copy Paste Dataset
    [Documentation]     Create 2 Projects and a Dataset. Copy and Paste the Dataset.

    Wait Until Keyword Succeeds             ${TIMEOUT}    ${INTERVAL}     Reload Page
    Select Experimenter
    ${p1id}=                                Create project      test copy-paste TO here
    ${p2id}=                                Create project      test copy-paste FROM here
    ${did}=                                 Create Dataset
    Wait Until Keyword Succeeds             ${TIMEOUT}    ${INTERVAL}     Click Element                          refreshButton
    Select Project By Id                    ${p2id}
    Wait Until Page Contains Element        xpath=//li[@data-id='${p2id}']//li[@data-id='${did}']
    Select Dataset By Id                    ${did}
    Click Element                           id=copyButton
    # Node Popup Menu Item Should Be Enabled  
    Select Project By Id                    ${p1id}
    Click Element                           id=pasteButton
    # Dataset should now be in BOTH Projects
    # Need to expand the Paste-To Project to reveal Dataset
    Select Project By Id                    ${p1id}
    Wait Until Page Contains Element        xpath=//li[@data-id='${p1id}']//li[@data-id='${did}']
    Wait Until Page Contains Element        xpath=//li[@data-id='${p2id}']//li[@data-id='${did}']

    #Delete Copied Dataset
    Select Dataset By Id                    ${did}
    Delete Container

    Select Project By Id                    ${p2id}
    Delete Container

    Select Project By Id                    ${p1id}
    Delete Container

Test Copy Paste Image
    [Documentation]     Copy Image from one dataset to another. Check if the link exists on both datasets.

    Wait Until Keyword Succeeds             ${TIMEOUT}    ${INTERVAL}     Reload Page
    Select Experimenter
    ${d1id}=                                Create dataset     test copy-paste TO here

    ${pId}    ${d2id}    ${imageId}         Select And Expand Project Dataset Image
    Click Element                           id=copyButton
    Select Dataset By Id                    ${d1id}
    Click Element                           id=pasteButton

    ${datasetNode}                          Wait For Dataset Node                     ${d1id}
    Click Node                              ${datasetNode}
    Wait Until Page Contains Element        xpath=//li[@data-id='${d1id}']//li[@data-id='${imageId}']
    Wait Until Page Contains Element        xpath=//li[@data-id='${d2id}']//li[@data-id='${imageId}']

    Select Dataset By Id                    ${d1id}
    Delete Container

Test Copy Paste Plate
    [Documentation]     Test copy pasting a plate into another screen. Check if the link exists on both the screens.
    
    Wait Until Keyword Succeeds             ${TIMEOUT}    ${INTERVAL}     Reload Page
    Select Experimenter
    ${s1id}=                                Create Screen      test Cut-Paste TO here1
    Wait Until Keyword Succeeds             ${TIMEOUT}    ${INTERVAL}     Click Element                    refreshButton

    ${s2id}=                                Select First Screen With Children
    ${plateId}=                             Select First Plate
    Click Element                           id=copyButton

    Select Screen By Id                     ${s1id}
    ${screenNode}                           Wait For Screen Node                                           ${s1id}
    Click Element                           id=pasteButton
    Click Node                              ${screenNode} 
    Wait Until Page Contains Element        xpath=//li[@data-id='${s1id}']//li[@data-id='${plateId}']
    Wait Until Page Contains Element        xpath=//li[@data-id='${s2id}']//li[@data-id='${plateId}']

    Click Node                              ${screenNode}
    Delete Container

[Teardown]    Close Browser
