*** Settings ***

Documentation     Tests the tree for creating new projects, datasets and screens.

Resource          ../../resources/config.txt
Resource          ../../resources/web/login.txt
Resource          ../../resources/web/tree.txt

Suite Setup         User "${USERNAME}" logs in with password "${PASSWORD}"

*** Variables ***

${treeRootId}                       j1_1
${CtxDatasetName}                   TestDataset
${CtxProjectName}                   TestProject
${CtxScreenName}                    TestScreen
${XPathNoData}                      xpath=//p[contains(@class, 'no_data')]
${orphaned}                         True
${notOrphaned}                      False

*** Keywords ***

Right Click Create P/D/S
    [Arguments]                             ${rootId}       ${optionText}       ${P/D/S Name}
    Open Context Menu                       xpath=//li[@id='${rootId}']/span
    Wait Until Element Is Visible           xpath=//ul[contains(@class, 'jstree-contextmenu')]//a[contains(text(), 'Create new')]
    Mouse Over                              xpath=//ul[contains(@class, 'jstree-contextmenu')]//a[contains(text(), 'Create new')]
    Click Element                           xpath=//ul[contains(@class, 'jstree-contextmenu')]//a[contains(text(), "${optionText}")]
    Input Text                              id=id_name      ${P/D/S Name}
    Click Dialog Button                     OK

P/D/S Should Be Clicked And Visible
    [Arguments]                             ${NodeName}
    Wait Until Page Contains Element        xpath=//span[contains(text(),'${NodeName}')][contains(@class, 'jstree-clicked')]
    Wait Until Page Contains Element        xpath=//div[contains(@class, 'data_heading')]//span[contains(text(),'${NodeName}')]

Check Right And Center Panels For Active Container
    [Arguments]                             ${containerOption}                  ${containerName}
    P/D/S Should Be Clicked And Visible     ${containerName}
    Wait Until Page Contains Element        xpath=//tr[contains(@class,'data_heading_id')]//th[contains(text(),'${containerOption} ID:')]
    ${Status}=                              Run Keyword and Return Status        Wait Until Page Contains Element   ${XPathNoData}
    Run Keyword If                          '${containerOption}'=='Dataset' and ${Status} == 'FAIL'                 Wait Until Page Contains Element    xpath=//ul[@id='dataIcons' and contains(@class,'ui-selectable')]
    ${newId}                                Get Text        css=tr.data_heading_id strong
    [Return]                                ${newId}

Create Project Using Icon and Check If It Exists
    ${projectId}=                           Create Project                      ${CtxProjectName}
    Project Should Exist                    ${projectId}
    Delete Container
    [Return]                                ${projectId}

Create Project Using Right Click and Check If It Exists
    [Arguments]                             ${RootId}                           ${ProjectName}
    Right Click Create P/D/S                ${RootId}       Project             ${ProjectName}
    ${projectId}=                           Check Right And Center Panels For Active Container                      Project             ${ProjectName}
    Project Should Exist                    ${projectId}
    Delete Container
    [Return]                                ${projectId}

Project Should Exist
    [Arguments]                             ${projectId}
    ${nodeId}=                              Wait for Project Node               ${projectId}
    Wait Until Page Contains Element        xpath=//li[@id='${treeRootId}']/ul/li[@id='${nodeId}']

Create Dataset Using Icon and Check If Orphaned
    [Arguments]                             ${isOrphaned}       ${deleteDataset}=True       ${projectId}=0
    # ${projectId}=                           Run Keyword If                          '${isOrphaned}' == '${notOrphaned}'  Get Text            css=tr.data_heading_id strong
    ${datasetId}=                           Create Dataset                          ${CtxDatasetName}
    Run Keyword If                          '${isOrphaned}' == '${orphaned}'        Dataset Exists and Is Orphaned      ${datasetId}
    Run Keyword If                          '${isOrphaned}' == '${notOrphaned}'     Dataset Exists and Is Not Orphaned  ${datasetId}        ${projectId}
    Run Keyword If                          ${deleteDataset}                        Delete Container
    [Return]                                ${datasetId}

Create Dataset Using Right Click and Check If Orphaned
    [Arguments]                             ${RootId}       ${DatasetName}      ${isOrphaned}       ${projectId}=0
    # ${projectId}=                           Run Keyword If                          '${isOrphaned}' == '${notOrphaned}'  Get Text            css=tr.data_heading_id strong
    Right Click Create P/D/S                ${RootId}       Dataset                 ${DatasetName}
    ${datasetId}=                           Check Right And Center Panels For Active Container                          Dataset             ${DatasetName}
    Run Keyword If                          '${isOrphaned}' == '${orphaned}'        Dataset Exists and Is Orphaned      ${datasetId}
    Run Keyword If                          '${isOrphaned}' == '${notOrphaned}'     Dataset Exists and Is Not Orphaned  ${datasetId}        ${projectId}
    [Return]                                ${datasetId}

Dataset Exists and Is Orphaned
    [Arguments]                             ${datasetId}
    ${nodeId}=                              Wait for Dataset Node               ${datasetId}
    Wait Until Page Contains Element        xpath=//li[@id='${treeRootId}']/ul/li[@id='${nodeId}']

Dataset Exists and Is Not Orphaned
    [Arguments]                             ${datasetId}                        ${projectId}
    ${datasetnodeId}=                       Wait for Dataset Node               ${datasetId}
    ${projectnodeId}=                       Wait for Project Node               ${projectId}
    Wait Until Page Contains Element        xpath=//li[@id='${treeRootId}']/ul/li[@id='${projectnodeId}']/ul/li[@id='${datasetnodeId}']

Create Screen Using Icon and Check If It Exists
    ${screenId}=                            Create Screen                       ${CtxScreenName}
    Screen Should Exist                     ${screenId}
    Delete Container
    [Return]                                ${screenId}

Create Screen Using Right Click and Check If It Exists
    [Arguments]                             ${RootId}                           ${ScreenName}
    Right Click Create P/D/S                ${RootId}       Screen              ${ScreenName}
    ${screenId}=                            Check Right And Center Panels For Active Container      Screen          ${ScreenName}
    Screen Should Exist                     ${screenId}
    Delete Container
    [Return]                                ${screenId}

Screen Should Exist
    [Arguments]                             ${screenId}
    ${nodeId}=                              Wait for Screen Node                ${screenId}
    Wait Until Page Contains Element        xpath=//li[@id='${treeRootId}']/ul/li[@id='${nodeId}']

*** Test Cases ***

Test Container Creation Enabled
    [Documentation]     Select User, Project, Dataset, Image and checks
    ...                 whether the toolbar/right-click menu options for
    ...                 creating various containers are enabled.

    Tree Should Be Visible
    # Full name may be truncated at start (right panel not wide enough)
    # just check last name
    Wait For Node To Be Visible                 ${LAST NAME}
    Wait For Node To Be Visible                 Orphaned Images
    Create Button Should Be Enabled             project
    Create Button Should Be Enabled             dataset
    Create Button Should Be Enabled             screen
    Node Popup Menu Item Should Be Enabled      Project    ${LAST NAME}
    Node Popup Menu Item Should Be Enabled      Dataset    ${LAST NAME}
    Node Popup Menu Item Should Be Enabled      Screen     ${LAST NAME}
    Node Popup Menu Item Should Be Disabled     Delete     ${LAST NAME}
    Select First Project With Children
    Create Button Should Be Enabled             project
    Create Button Should Be Enabled             dataset
    Create Button Should Be Enabled             screen
    Node Popup Menu Item Should Be Enabled      Project
    Node Popup Menu Item Should Be Enabled      Dataset
    Node Popup Menu Item Should Be Enabled      Screen
    Node Popup Menu Item Should Be Enabled      Delete
    Select First Dataset With Children
    Create Button Should Be Enabled             project
    Create Button Should Be Enabled             dataset
    Create Button Should Be Enabled             screen
    Node Popup Menu Item Should Be Enabled      Project
    Node Popup Menu Item Should Be Enabled      Dataset
    Node Popup Menu Item Should Be Enabled      Screen
    Node Popup Menu Item Should Be Enabled      Delete
    ${imageId}=                                 Select First Image
    Create Button Should Be Enabled             project
    Create Button Should Be Enabled             dataset
    Create Button Should Be Enabled             screen
    Node Popup Menu Item Should Be Enabled      Project
    Node Popup Menu Item Should Be Enabled      Dataset
    Node Popup Menu Item Should Be Enabled      Screen
    Node Popup Menu Item Should Be Enabled      Delete
    Select Orphaned Images Section
    Create Button Should Be Enabled             project
    Create Button Should Be Enabled             dataset
    Create Button Should Be Enabled             screen
    Node Popup Menu Item Should Be Enabled      Project
    Node Popup Menu Item Should Be Enabled      Dataset
    Node Popup Menu Item Should Be Enabled      Screen
    Node Popup Menu Item Should Be Disabled     Delete

    ${imageId}=                                 Select First Image
    Thumbnail Should Be Selected                ${imageId}
    ${imageNodeId}=                             Select Image By Id            ${imageId}
    # Using hot-key fails on jsTree 3 (worked OK previously)...
    # Focus                                       ${imageNodeId}
    # Key Down                                    40    '#${imageNodeId}'  # '40' is 'down' key    # Press Key    dataTree    /40
    # ... Use 'Click Next Thumbnail' instead
    Click Next Thumbnail
    Thumbnail Should Not Be Selected            ${imageId}
    Image Should Not Be Selected By Id          ${imageId}
    Click Thumbnail                             ${imageId}
    Thumbnail Should Be Selected                ${imageId}
    Image Should Be Selected By Id              ${imageId}

Test Create Project Using Icon

    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Select Experimenter
    Create Project Using Icon and Check If It Exists

    Select First Project
    Create Project Using Icon and Check If It Exists

    Select First Dataset
    Create Project Using Icon and Check If It Exists

    Select First Screen
    Create Project Using Icon and Check If It Exists

    Select And Expand Image
    Create Project Using Icon and Check If It Exists

    Select Orphaned Images Section
    Create Project Using Icon and Check If It Exists

Test Create Project Using Right Click

    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Select Experimenter
    Create Project Using Right Click and Check If It Exists     ${treeRootId}                   ${CtxProjectName}

    ${imageId}=                                                 Select And Expand Image
    ${treeId}=                                                  Wait For Image Node             ${imageId}
    Create Project Using Right Click and Check If It Exists     ${treeId}                       ${CtxProjectName}

    ${screenId}=                                                Select First Screen
    ${treeId}=                                                  Wait For Screen Node            ${screenId}
    Create Project Using Right Click and Check If It Exists     ${treeId}                       ${CtxProjectName}

    ${treeId}=                                                  Select Orphaned Images Section
    Create Project Using Right Click and Check If It Exists     ${treeId}                       ${CtxProjectName}

    ${imageId}=                                                 Select First Orphaned Image
    ${treeId}=                                                  Wait For Image Node             ${imageId}
    Create Project Using Right Click and Check If It Exists     ${treeId}                       ${CtxProjectName}

Test Create Dataset Using Icon

    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Select Experimenter
    Create Dataset Using Icon and Check If Orphaned             ${orphaned}

    # We don't delete new Dataset 'False'. Get datasetId and delete below
    ${pId}=  Select First Project
    ${dId}=  Create Dataset Using Icon and Check If Orphaned    ${notOrphaned}      False       ${pId}

    # Dataset just-created is still selected. Create sibling Dataset (with Delete)
    Create Dataset Using Icon and Check If Orphaned             ${notOrphaned}      True        ${pId}

    # Now select and Delete the new Dataset we created under Project
    Select Dataset By Id                                        ${dId}
    Delete Container

    Select First Screen
    Create Dataset Using Icon and Check If Orphaned             ${orphaned}

    Select And Expand Image
    Create Dataset Using Icon and Check If Orphaned             ${orphaned}

    Select Orphaned Images Section
    Create Dataset Using Icon and Check If Orphaned             ${orphaned}

Test Create Dataset Using Right Click

    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Select Experimenter
    Create Dataset Using Right Click and Check If Orphaned      ${treeRootId}               ${CtxDatasetName}               ${orphaned}
    Delete Container

    ${pId}=                                                     Select First Project
    ${treeId}=                                                  Wait For Project Node       ${pId}
    ${dId}=  Create Dataset Using Right Click and Check If Orphaned      ${treeId}          ${CtxDatasetName}               ${notOrphaned}      ${pId}
    # Don't Delete new Dataset here - Test creating next Dataset as sibling...

    ${treeId}=                                                  Wait For Dataset Node       ${dId}
    Create Dataset Using Right Click and Check If Orphaned      ${treeId}                   ${CtxDatasetName}               ${notOrphaned}      ${pId}
    Delete Container

    # Now select and Delete the new Dataset we created under Project
    Select Dataset By Id                                       ${dId}
    Delete Container

    ${imageId}=                                                 Select And Expand Image
    ${treeId}=                                                  Wait For Image Node         ${imageId}
    Create Dataset Using Right Click and Check If Orphaned      ${treeId}                   ${CtxDatasetName}               ${orphaned}
    Delete Container

    ${screenId}=                                                Select First Screen
    ${treeId}=                                                  Wait For Screen Node        ${screenId}
    Create Dataset Using Right Click and Check If Orphaned      ${treeId}                   ${CtxDatasetName}               ${orphaned}
    Delete Container

    ${treeId}=                                                  Select Orphaned Images Section
    Create Dataset Using Right Click and Check If Orphaned      ${treeId}                   ${CtxDatasetName}               ${orphaned}
    Delete Container

    ${imageId}=                                                 Select First Orphaned Image
    ${treeId}=                                                  Wait For Image Node         ${imageId}
    ${datasetId}=                                               Create Dataset Using Right Click and Check If Orphaned      ${treeId}                   ${CtxDatasetName}               ${orphaned}
    Dataset Should Contain Image                                ${imageId}                  ${datasetId}
    Delete Container

Test Create Screen Using Icon

    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Select Experimenter
    Create Screen Using Icon and Check If It Exists

    Select First Project
    Create Screen Using Icon and Check If It Exists

    Select First Dataset
    Create Screen Using Icon and Check If It Exists

    Select First Screen
    Create Screen Using Icon and Check If It Exists

    Select And Expand Image
    Create Screen Using Icon and Check If It Exists

    Select Orphaned Images Section
    Create Screen Using Icon and Check If It Exists

Test Create Screen Using Right Click

    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Select Experimenter
    Create Screen Using Right Click and Check If It Exists      ${treeRootId}               ${CtxScreenName}

    ${imageId}=                                                 Select And Expand Image
    ${treeId}=                                                  Wait For Image Node         ${imageId}
    Create Screen Using Right Click and Check If It Exists      ${treeId}                   ${CtxScreenName}

    ${screenId}=                                                Select First Screen
    ${treeId}=                                                  Wait For Screen Node        ${screenId}
    Create Screen Using Right Click and Check If It Exists      ${treeId}                   ${CtxScreenName}

    ${treeId}=                                                  Select Orphaned Images Section
    Create Screen Using Right Click and Check If It Exists      ${treeId}                   ${CtxScreenName}

    ${imageId}=                                                 Select First Orphaned Image
    ${treeId}=                                                  Wait For Image Node         ${imageId}
    Create Screen Using Right Click and Check If It Exists      ${treeId}                   ${CtxScreenName}

Test Create Owned

    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Reload Page
    Select Experimenter
    ${projectId}=                               Create Project                      TestCreateOwned

    # Login as root and show Project
    Log Out
    User "${ROOT USERNAME}" logs in with password "${ROOT PASSWORD}"
    Go To                                       ${WELCOME URL}?show=project-${projectId}
    Wait Until Keyword Succeeds                 ${TIMEOUT}    ${INTERVAL}     Create Button Should Be Enabled             project
    Create Button Should Be Enabled             dataset
    Create Button Should Be Enabled             screen

    # Root creates Dataset (owned by User)
    Click Button                                adddatasetButton
    Wait Until Element Is Visible               id=new_pds_owner
    Element Text Should Be                      id=new_pds_owner        ${FULL NAME}
    Input Text                                  name                    OwnedDataset
    Click Dialog Button                         OK
    Wait Until Page Contains                    Dataset ID:
    Element Text Should Be                      id=owner_fullname       ${FULL NAME}

    # Root can Delete User's Project (and Dataset)
    Select Project By Id                        ${projectId}
    Delete Container

[Teardown]    Close Browser
