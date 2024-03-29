*** Settings ***
Documentation     Tests Drag and Drop actions.

Resource          ../../resources/config.txt
Resource          ../../resources/web/login.txt
Resource          ../../resources/web/tree.txt

Suite Setup         User "${USERNAME}" logs in with password "${PASSWORD}"

*** Test Cases ***

Test Drag And Drop Image

    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Set Selenium Speed                          .3
    Select Experimenter
    ${did}=                                     Select First Dataset

    ${pId}    ${datasetId}    ${imageId}        Select And Expand Project Dataset Image
    ${datasetNode}=                             Wait For Dataset Node                                             ${datasetId}    
    ${datasetNode1}=                            Wait For Dataset Node    ${did}

    Wait Until Element is Visible               xpath=//li[@id='${datasetNode}']//li[@data-id='${imageId}']/span
    Drag And Drop                               xpath=//li[@id='${datasetNode}']//li[@data-id='${imageId}']/span     xpath=//li[@id='${datasetNode1}']/span
    Select Dataset By Id                        ${did}
    Wait Until Element is Visible               xpath=//li[@id='${datasetNode1}']//li[@data-id='${imageId}'] 

    #Bring database state back to original
    Select Image By Id                          ${imageId}
    Wait Until Element is Visible               xpath=//li[@id='${datasetNode1}']//li[@data-id='${imageId}']/span
    Drag And Drop                               xpath=//li[@id='${datasetNode1}']//li[@data-id='${imageId}']/span    xpath=//li[@id='${datasetNode}']/span
    Select Dataset By Id                        ${datasetId}
    Wait Until Element is Visible               xpath=//li[@id='${datasetNode}']//li[@data-id='${imageId}']

Test Drag and Drop Orphaned Image
    
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Set Selenium Speed                          .3
    Select Experimenter
    ${did}=                                     Select First Dataset
    ${datasetNode}=                             Wait For Dataset Node    ${did}

    ${nodeId}=                                  Select Orphaned Images Section
    ${imageId}=                                 Select First Orphaned Image
    ${imageNode}=                               Wait For Image Node     ${imageId}

    Wait Until Element is Visible               xpath=//li[@id='${nodeId}']//li[@id='${imageNode}']/span
    Drag And Drop                               xpath=//li[@id='${nodeId}']//li[@id='${imageNode}']/span    xpath=//li[@id='${datasetNode}']/span
    Select Dataset By Id                        ${did}
    Dataset Should Contain Image                ${imageId}     ${did} 

Test Drag and Drop Orphaned Dataset
       
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Set Selenium Speed                          .3 
    Select Experimenter                            
    ${pid}=                                     Select First Project With Children
    Select Experimenter                         
    ${datasetId}                                Create Dataset
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Click Element    refreshButton 

    ${projectNode}                              Wait For Project Node    ${pid}
    ${datasetNode}                              Wait For Dataset Node    ${datasetId}                           

    Click Node                                  ${datasetNode}
    Wait Until Element is Visible               xpath=//li[@id='${datasetNode}']/span
    Drag And Drop                               xpath=//li[@id='${datasetNode}']/span    xpath=//li[@id='${projectNode}']/span
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Click Element    refreshButton
    ${projectNode}                              Wait For Project Node    ${pid}
    Click Node                                  ${projectNode}
    Wait Until Element is Visible               xpath=//li[@id='${projectNode}']//li[@data-id='${datasetId}']
    Select Dataset By Id                        ${datasetId}
    Delete Container

Test Drag and Drop Plate
    
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Set Selenium Speed                          .3
    Select Experimenter
    ${screenId}                                 Create screen                 Screen Plate Drag And Drop
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Click Element    refreshButton

    ${screenId1}                                Select First Screen With Children
    ${plateId}                                  Select First Plate
    ${screenNode1}                              Wait For Screen Node    ${screenId1}
    ${screenNode}                               Wait For Screen Node    ${screenId}

    Wait Until Element is Visible               xpath=//li[@id='${screenNode1}']//li[@data-id='${plateId}']/span
    Drag And Drop                               xpath=//li[@id='${screenNode1}']//li[@data-id='${plateId}']/span                             xpath=//li[@id='${screenNode}']/span
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Click Element    refreshButton
    
    ${screenNode}                               Wait For Screen Node    ${screenId}
    Click Node                                  ${screenNode}
    Wait Until Element is Visible               xpath=//li[@id='${screenNode}']//li[@data-id='${plateId}']
    Select Screen By Id                         ${screenId}
    Delete Container

Test Drag and Drop Dataset On Experimenter
    [Documentation]                             This test will currently fail on 5.1 Series

    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Set Selenium Speed                          .3
    Select Experimenter
    ${pid}                                      Create Project    
    ${did}=                                     Create Dataset
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Click Element    refreshButton

    ${projectNode}=                             Wait For Project Node    ${pid}
    Click Node                                  ${projectNode}
    ${datasetNode}                              Wait For Dataset Node    ${did}

    Wait Until Element is Visible               xpath=//li[@id='${projectNode}']//li[@id='${datasetNode}']
    Drag And Drop                               xpath=//li[@id='${datasetNode}']/span    xpath=//li[@id='${treeRootId}']/span
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Select Experimenter
    ${datasetNode}                              Wait For Dataset Node    ${did}
    Wait Until Element is Visible               xpath=//li[@id='${treeRootId}']/ul/li[@id='${datasetNode}']

    Select Dataset By Id                        ${did}
    Delete Container

    Select Project By Id                        ${pid}
    Delete Container

Test Drag and Drop Image on Experimenter
    
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Set Selenium Speed                          .3
    Select Experimenter
    ${pid}                                      Create Project
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Click Element    refreshButton

    ${projectId}    ${datasetid}    ${imageId}  Select And Expand Project Dataset Image
    ${imageNode}=                               Wait For Image Node    ${imageId}
    #Drag Image on Experimenter
    Drag And Drop                               xpath=//li[@id='${imageNode}']/span  xpath=//li[@id='${treeRootId}']/span
    ${nodeId}=                                  Select Orphaned Images Section
    Page Should Not Contain Element             xpath=//li[@id='${nodeId}']//li[@id='${imageNode}']
    Select Project By Id                        ${pid}
    Delete Container

Test Drag and Drop Project on Invalid Targets

    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Set Selenium Speed                          .3
    Select Experimenter
    ${pid}                                      Create Project
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Click Element    refreshButton

    Select Experimenter
    ${screenId}                                 Create Screen
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Click Element    refreshButton

    ${plateId}                                  Select First Plate No Load

    ${projectNode}                              Wait For Project Node          ${pid}

    ${projectId}    ${datasetid}    ${imageId}  Select And Expand Project Dataset Image
    ${projectNode1}                             Wait For Project Node           ${projectId}

    #Drag Project on Project
    Drag And Drop                               xpath=//li[@id='${projectNode}']/span    xpath=//li[@id='${projectNode1}']/span
    Page Should Not Contain Element             xpath=//li[@id='${projectNode1}']//li[@id='${projectNode}']

    #Drag Project on Dataset
    ${datasetNode1}=                            Wait For Dataset Node    ${datasetid}
    Drag And Drop                               xpath=//li[@id='${projectNode}']/span    xpath=//li[@id='${datasetNode1}']/span
    Page Should Not Contain Element             xpath=//li[@id='${datasetNode1}']//li[@id='${projectNode}'] 

    #Drag Project on Image
    ${imageNode1}=                              Wait For Image Node      ${imageId}
    Drag And Drop                               xpath=//li[@id='${projectNode}']/span    xpath=//li[@id='${imageNode1}']/span
    Page Should Not Contain Element             xpath=//li[@id='${imageNode1}']//li[@id='${projectNode}']  

    #Drag Project on Screen
    ${screenNode1}                              Wait For Screen Node      ${screenId}
    Drag And Drop                               xpath=//li[@id='${projectNode}']/span    xpath=//li[@id='${screenNode1}']/span
    Page Should Not Contain Element             xpath=//li[@id='${screenNode1}']//li[@id='${projectNode}']

    #Drag Project on Plate
    ${plateNode1}                               Wait For Plate Node       ${plateId}
    Drag And Drop                               xpath=//li[@id='${projectNode}']/span    xpath=//li[@id='${plateNode1}']/span
    Page Should Not Contain Element             xpath=//li[@id='${plateNode1}']//li[@id='${projectNode}']

    Select Project By Id                        ${pid}
    Delete Container
    Select Screen By id                         ${screenId}
    Delete Container

Test Drag and Drop Dataset on Invalid Targets

    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Set Selenium Speed                          .3
    Select Experimenter
    ${did}=                                     Create Dataset
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Click Element    refreshButton

    Select Experimenter
    ${screenId}                                 Create Screen
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Click Element    refreshButton

    ${plateId}                                  Select First Plate No Load
    
    ${datasetNode}                              Wait For Dataset Node          ${did}

    ${projectId}    ${datasetid}    ${imageId}  Select And Expand Project Dataset Image

    #Drag Dataset on Dataset
    ${datasetNode1}=                            Wait For Dataset Node      ${datasetid}
    Drag And Drop                               xpath=//li[@id='${datasetNode}']/span  xpath=//li[@id='${datasetNode1}']/span
    Page Should Not Contain Element             xpath=//li[@id='${datasetNode1}']//li[@id='${datasetNode}']

    #Drag Dataset on Image
    ${imageNode1}=                              Wait For Image Node         ${imageId}
    Drag And Drop                               xpath=//li[@id='${datasetNode}']/span   xpath=//li[@id='${imageNode1}']/span
    Page Should Not Contain Element             xpath=//li[@id='${imageNode1}']//li[@id='${datasetNode}'] 

    #Drag Dataset on Screen
    ${screenNode1}                              Wait For Screen Node         ${screenId}
    Drag And Drop                               xpath=//li[@id='${datasetNode}']/span    xpath=//li[@id='${screenNode1}']/span
    Page Should Not Contain Element             xpath=//li[@id='${screenNode1}']//li[@id='${datasetNode}']

    #Drag Dataset on Plate
    ${plateNode1}                               Wait For Plate Node          ${plateId}
    Drag And Drop                               xpath=//li[@id='${datasetNode}']/span    xpath=//li[@id='${plateNode1}']/span
    Page Should Not Contain Element             xpath=//li[@id='${plateNode1}']//li[@id='${datasetNode}']

    Select Dataset By Id                        ${did}
    Delete Container
    Select Screen By id                         ${screenId}
    Delete Container

Test Drag and Drop Screen on Invalid Targets

    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Set Selenium Speed                          .3
    Select Experimenter
    ${screenId}                                 Create Screen
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Click Element    refreshButton
    
    Select Experimenter
    ${screenId1}                                Create Screen
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Click Element    refreshButton

    Select Experimenter
    ${plateId}                                  Select First Plate No Load

    ${screenNode}                               Wait For Screen Node           ${screenId}

    #Drag Screen on Screen
    ${screenNode1}                              Wait For Screen Node           ${screenId1}

    Drag And Drop                               xpath=//li[@id='${screenNode}']/span    xpath=//li[@id='${screenNode1}']/span
    Page Should Not Contain Element             xpath=//li[@id='${screenNode1}']//li[@id='${screenNode}']

    #Drag Screen on Plate
    ${plateNode1}                               Wait For Plate Node             ${plateId}

    Drag And Drop                               xpath=//li[@id='${screenNode}']/span  xpath=//li[@id='${plateNode1}']/span
    Page Should Not Contain Element             xpath=//li[@id='${plateNode1}']//li[@id='${screenNode}']

    ${projectId}    ${datasetid}    ${imageId}  Select And Expand Project Dataset Image

    #Drag Screen on Project
    ${projectNode}                              Wait For Project Node          ${projectId}
    Drag And Drop                               xpath=//li[@id='${screenNode}']/span  xpath=//li[@id='${projectNode}']/span
    Page Should Not Contain Element             xpath=//li[@id='${projectNode}']//li[@id='${screenNode}']

    #Drag Screen on Dataset
    ${datasetNode}=                             Wait For Dataset Node      ${datasetid}
    Drag And Drop                               xpath=//li[@id='${screenNode}']/span    xpath=//li[@id='${datasetNode}']/span
    Page Should Not Contain Element             xpath=//li[@id='${datasetNode}']//li[@id='${screenNode}']

    #Drag Screen on Image
    ${imageNode1}=                               Wait For Image Node            ${imageId}
    Drag And Drop                                xpath=//li[@id='${screenNode}']/span  xpath=//li[@id='${imageNode1}']/span
    Page Should Not Contain Element              xpath=//li[@id='${imageNode1}']//li[@id='${screenNode}']

    Select Screen By id                          ${screenId}
    Delete Container
    Select Screen By id                          ${screenId1}
    Delete Container

Test Drag and Drop Plate on Invalid Targets

    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Set Selenium Speed                          .3
    Select Experimenter
    ${plateId}                                  Select First Plate No Load
    
    ${plateNode}                                Wait For Plate Node            ${plateId}

    ${projectId}    ${datasetid}    ${imageId}  Select And Expand Project Dataset Image
    ${projectNode}                              Wait For Project Node          ${projectId}
    
    #Drag Plate on Project
    Drag And Drop                               xpath=//li[@id='${plateNode}']/span  xpath=//li[@id='${projectNode}']/span
    Page Should Not Contain Element             xpath=//li[@id='${projectNode}']//li[@id='${plateNode}']

    #Drag Plate on Dataset
    ${datasetNode}=                             Wait For Dataset Node      ${datasetid}
    Drag And Drop                               xpath=//li[@id='${plateNode}']/span  xpath=//li[@id='${datasetNode}']/span
    Page Should Not Contain Element             xpath=//li[@id='${datasetNode}']//li[@id='${plateNode}']

    #Drag Plate on Image
    ${imageNode1}=                              Wait For Image Node         ${imageId}
    Drag And Drop                               xpath=//li[@id='${plateNode}']/span  xpath=//li[@id='${imageNode1}']/span
    Page Should Not Contain Element             xpath=//li[@id='${imageNode1}']//li[@id='${plateNode}']

Test Drag and Drop Image on Invalid Targets

    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Set Selenium Speed                          .3
    Select Experimenter
    ${screenId}                                 Create Screen
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Click Element    refreshButton

    Select Experimenter
    ${pId}                                      Create Project
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Click Element    refreshButton

    ${projectId}    ${datasetid}    ${imageId}  Select And Expand Project Dataset Image

    ${imageNode}=                               Wait For Image Node            ${imageId}

    #Drag Image on Screen
    ${screenNode}                               Wait For Screen Node           ${screenId}
    
    Drag And Drop                               xpath=//li[@id='${imageNode}']/span    xpath=//li[@id='${screenNode}']/span
    Page Should Not Contain Element             xpath=//li[@id='${screenNode}']//li[@id='${imageNode}']

    ${plateId}                                  Select First Plate No Load
    ${plateNode}                                Wait For Plate Node            ${plateId}

    #Drag Image on Plate
    Drag And Drop                               xpath=//li[@id='${imageNode}']/span    xpath=//li[@id='${plateNode}']/span
    Page Should Not Contain Element             xpath=//li[@id='${plateNode}']//li[@id='${imageNode}']

    ${projectNode}                              Wait For Project Node          ${pId}
    
    #Drag Image on Project
    Drag And Drop                               xpath=//li[@id='${imageNode}']/span   xpath=//li[@id='${projectNode}']/span
    Page Should Not Contain Element             xpath=//li[@id='${projectNode}']//li[@id='${imageNode}']

    Select Screen By id                         ${screenId}
    Delete Container

    Select Project By id                         ${pId}
    Delete Container

[Teardown]    Close Browser
