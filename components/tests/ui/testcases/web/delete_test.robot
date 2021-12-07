*** Settings ***
Documentation     Tests delete of Projects, Datasets, Images

Resource          ../../resources/config.txt
Resource          ../../resources/web/login.txt
Resource          ../../resources/web/tree.txt

Suite Setup         User "${USERNAME}" logs in with password "${PASSWORD}"

*** Variables ***

${thumbnailsXpath}      //ul[@id='dataIcons']//div[contains(@class, 'image')]

*** Keywords ***

Clear Activity
    [Documentation]         Clear any activities from earlier tests etc.
    Click Element           id=launch_activities
    Click Element           id=clear_activities

Check Fileset Delete Warning
    Wait Until Element Is Visible           id=delete-dialog-form
    Wait Until Element Is Visible           xpath=//div[@class='split_filesets_info']
    # For some reason, fails to find full text: 'Multi-image filesets cannot be partially deleted'
    Wait Until Page Contains                partially deleted
    # Should be 3 thumbnails shown for this fileset
    Page Should Contain Element             //div[@class='split_fileset']//img[@class='fileset_image']        limit=3
    # Yes button shouldn't be visible
    ${xpath}=                               Get Dialog Button Xpath     Yes
    Element Should Not Be Visible           xpath=${xpath}

Check Job Status
    [Arguments]                 ${status}
    Wait Until Keyword Succeeds     ${TIMEOUT}   ${INTERVAL}   Page Should Contain Element     xpath=//span[@id='jobstatus'][contains(text(),'${status}')]

Check Activities
    [Arguments]                 ${title}    ${message}
    Wait Until Element Is Visible           id=jobsTable
    Wait Until Element Is Visible           xpath=//span[contains(text(),'${title}')]
    # Wait till job completes to check message
    Wait Until Keyword Succeeds             ${TIMEOUT}   ${INTERVAL}    Element Should Be Visible    jquery=span:contains("${message}")

*** Test Cases ***

Test Delete Project
    [Documentation]     Create and Delete a Project

    Clear Activity
    Select Experimenter
    ${pid}=                                 Create project          robot test delete
    Click Element                           refreshButton
    ${nodeId}=                              Wait For Project Node   ${pid}
    Click Element                           id=deleteButton
    Wait Until Element Is Visible           id=delete-dialog-form
    Click Dialog Button                     Yes
    # Wait for activities to show job done, then refresh tree...
    Check Job Status                        1
    Page Should Not Contain Element         id=${nodeId}
    Click Element                           refreshButton
    # On POST success, Experimenter should be selected and project removed from tree
    User Should Be Selected
    Project Should Not Exist in Tree        ${pid}

Test Delete Project Dataset
    [Documentation]     Create and Delete a Project containing a Dataset

    Clear Activity
    Select Experimenter
    ${pid}=                                 Create project      robot test delete
    ${did}=                                 Create Dataset      robot test deleteChildren
    Click Element                           refreshButton
    ${projectNodeId}=                       Select Project By Id                    ${pid}
    Click Element                           id=deleteButton
    Wait Until Element Is Visible           id=delete-dialog-form
    Click Dialog Button                     Yes
    # Wait for activities to show job done, then refresh tree...
    Check Job Status                        1
    Click Element                           refreshButton
    User Should Be Selected
    Project Should Not Exist in Tree        ${pid}
    # Dataset should be Deleted too
    Dataset Should Not Exist In Tree        ${did}

Test Delete Images in Dataset
    [Documentation]     Deletes images pre-imported into a dataset named "Delete"

    Clear Activity
    Select Experimenter
    # Click on Dataset named "Delete", wait for thumbnails and count them
    ${nodeId}=                              Wait For Dataset Node Text              Delete
    Click Node                              ${nodeId}
    Wait Until Page Contains Element        id=dataIcons
    ${thumbCount}=                          Get Element Count     ${thumbnailsXpath}
    # Click first image in Tree
    Select First Image
    Click Element                           id=deleteButton
    Wait Until Element Is Visible           id=delete-dialog-form
    Click Dialog Button                     Yes
    # Should see almost instant removal of 1 thumbnail...
    ${delThumbCount}=                       Evaluate   ${thumbCount} - 1
    Wait Until Keyword Succeeds             1   0.1   Page Should Contain Element   ${thumbnailsXpath}   limit=${delThumbCount}
    # ...Need to check that centre panel doesn't reload and show image during delete: #12866
    Sleep                                   5
    Page Should Contain Element               ${thumbnailsXpath}   limit=${delThumbCount}

Test Delete MIF Images
    [Documentation]     Checks warnings when trying to delete Multi-Image-Fileset images

    Clear Activity
    Select Experimenter
    # Click on Dataset named "MIF Images"
    Select First Dataset With Name          MIF Images
    ${nodeId}=                              Select First Image With Name            test&series=3.fake [test]
    Wait Until Element Is Visible           xpath=//ul[@id='dataIcons']//li[contains(@class, 'fs-selected')]
    Page Should Contain Element             xpath=//ul[@id='dataIcons']//li[contains(@class, 'fs-selected')]       limit=3
    # Check warning is generated via toolbar button...
    Click Element                           id=deleteButton
    Check Fileset Delete Warning
    Click Dialog Button                     Cancel
    # ...and from right-click menu
    Node Context Menu Select Item           ${nodeId}       Delete
    Check Fileset Delete Warning
    Click Dialog Button                     Cancel

Test Chgrp MIF Images
    [Documentation]     Checks Chgrp workflow for MIF Images

    Clear Activity
    Select Experimenter
    Select First Dataset With Name          MIF Images
    ${nodeId}=                              Select First Image With Name            test&series=3.fake [test]
    Wait Until Element Is Visible           xpath=//ul[@id='dataIcons']//li[contains(@class, 'fs-selected')]
    Page Should Contain Element             xpath=//ul[@id='dataIcons']//li[contains(@class, 'fs-selected')]       limit=3
    # Chgrp...
    Node Context Menu Select Item           ${nodeId}       Move to Group...
    Wait Until Element Is Visible           id=chgrp-form
    Wait Until Element Is Visible           xpath=//div[@class='split_filesets_info']
    # Wait Until Element Is Visible           xpath=//div[contains(@class,'split_filesets_info')]//h1[contains(text(), 'filesets')]
    # Should be 3 thumbnails shown for this fileset
    Element Should Be Visible               //div[@class='split_fileset']//img[@class='fileset_image']        limit=3
    # Check that "Cancel" button works
    Click Dialog Button                     Cancel
    Element Should Not Be Visible           id=chgrp-form

    # Select just 1 Image and re-open dialog...
    ${nodeId}=                              Select First Image With Name            test&series=3.fake [test]
    Node Context Menu Select Item           ${nodeId}       Move to Group...
    Click Dialog Button                     Move All
    Wait Until Page Contains                Please choose target group below
    Wait Until Page Contains                ${GROUP_NAME_2}
    Click Element                           xpath=//div[@id='group_chooser']//div[contains(text(), '${GROUP_NAME_2}')]
    # dry-run check of what gets moved -> "3 Images"
    Wait Until Keyword Succeeds             ${TIMEOUT}   ${INTERVAL}   Page Should Contain Element     xpath=//div[@id='group_chooser']//p[contains(text(), '3 Images')]
    Click Dialog Button                     New...
    ${datasetName}=                         Evaluate    "chgrpToNewDataset_%s" % random.randint(0, 1000)     modules=random
    Input Text                              new_container_name      ${datasetName}
    Click Dialog Button                     OK
    Check Activities                        Move to Group           Images moved to Group
    Wait Until Keyword Succeeds             ${TIMEOUT}   ${INTERVAL}     Click Link     Show Images
    # Should link to 3 selected images in new Group
    Wait Until Keyword Succeeds             ${TIMEOUT}   ${INTERVAL}     Page Should Contain Element             xpath=//ul[@id='dataIcons']//li[contains(@class, 'ui-selected')]       limit=3
    Wait Until Page Contains                ${GROUP_NAME_2}
    # Contained under Dataset
    ${dsNodeId}=                            Wait For Dataset Node Text    ${datasetName}
    ${nodeId}=                              Wait For Image Node Text    test&series=3.fake [test]
    Page Should Contain Element             xpath=//li[@id='${dsNodeId}']//li[@id='${nodeId}']
    # Chgrp back again... With 3 images still selected...
    Node Context Menu Select Item           ${nodeId}       Move to Group...
    Wait Until Page Contains                ${GROUP_NAME}
    Click Element                           xpath=//div[@id='group_chooser']//div[contains(text(), '${GROUP_NAME}')]
    Wait Until Keyword Succeeds             ${TIMEOUT}   ${INTERVAL}   Page Should Contain Element     xpath=//div[@id='group_chooser']//p[contains(text(), '3 Images')]
    # Original Dataset should be listed as a link
    Click Element                           jquery=a:contains("MIF Images")
    Click Dialog Button                     OK
    Check Activities                        Move to Group           ${GROUP_NAME}
    Click Element                           id=logo         # closes the Activities panel
    # Delete Dataset
    Click Node                              ${dsNodeId}
    Delete Container
    # Click "Show Images" in Activities dialog - should return to "MIF Images" Dataset
    Click Element                           id=launch_activities
    Wait Until Keyword Succeeds             ${TIMEOUT}   ${INTERVAL}     Click Link     Show Images
    # Returned to original Dataset
    Wait Until Keyword Succeeds             ${TIMEOUT}   ${INTERVAL}     Page Should Contain Element             xpath=//ul[@id='dataIcons']//li[contains(@class, 'ui-selected')]       limit=3
    Wait Until Page Contains                ${GROUP_NAME}
    ${dsNodeId}=                            Wait For Dataset Node Text    MIF Images
    ${nodeId}=                              Wait For Image Node Text    test&series=3.fake [test]
    Page Should Contain Element             xpath=//li[@id='${dsNodeId}']//li[@id='${nodeId}']


Test Share Deprecated
    [Documentation]     Tests that Share button gives deprecated message

    Clear Activity
    Select Experimenter
    # Add image in 'Delete' Dataset to Share
    ${nodeId}=                                  Wait For Dataset Node Text              Delete
    Click Node                                  ${nodeId}
    Wait Until Page Contains Element            id=dataIcons
    # Click first image in Tree
    ${imageId}=                                 Select First Image

    Click Element                               createshareButton
    Wait Until Page Contains Element            xpath=//span[contains(text(),'Shares not supported')]
    Click Dialog Button                         OK

[Teardown]    Close Browser
