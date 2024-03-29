*** Settings ***
Documentation     Tests browsing to an Image and opening Image Viewer

Resource          ../../resources/config.txt
Resource          ../../resources/web/login.txt
Resource          ../../resources/web/tree.txt

Library           Collections

Suite Setup         User "${USERNAME}" logs in with password "${PASSWORD}"

*** Variables ***
# robot_setup script has created data with these parameters
${PLATE_NAME}               spwTests


*** Keywords ***

Check Image Viewer
    [Arguments]                         ${title}    ${datasetId}
    # Image Viewer title is the image name.
    Switch Window                       ${title}
    # Loading of image within viewport indicates all is OK
    Wait Until Page Contains Element    xpath=//img[@id='weblitz-viewport-img']     ${WAIT}
    ${titles}=                          Get Window Titles
    List Should Contain Value           ${titles}               ${title}
    Location Should Contain             dataset=${datasetId}
    # Should have 2 windows open
    ${windowIds}=                       Get Window Identifiers
    Length Should Be                    ${windowIds}            2
    # Close Popup window
    Close Window
    Switch Window                       MAIN

Right Click Open With
    [Arguments]                             ${nodeId}       ${optionText}
    Open Context Menu                       xpath=//li[@id='${nodeId}']/span
    Mouse Over                              xpath=//ul[contains(@class, 'jstree-contextmenu')]//a[contains(text(), 'Open With...')]
    Click Element                           xpath=//ul[contains(@class, 'jstree-contextmenu')]//li[descendant::a[contains(text(), 'Open With...')]]//a[contains(text(), "${optionText}")]


*** Test Cases ***

Test Open Viewer Select in Tree
    [Documentation]     Tests select image in Tree open image viewer

    Tree Should Be Visible
    ${pId}    ${dId}    ${imageId}      Select And Expand Project Dataset Image
    ${nodeId}=                          Wait For Image Node         ${imageId}
    ${imageName}=                       Wait For General Panel And Return Name      Image
    Click Element                       xpath=//a[@title='Open full image viewer in new tab']
    Check Image Viewer                  ${imageName}                ${dId}

Test Open Viewer Double Click
    [Documentation]     Tests double-click to open image viewer

    Tree Should Be Visible
    ${pId}    ${dId}    ${imageId}      Select And Expand Project Dataset Image
    ${nodeId}=                          Wait For Image Node         ${imageId}
    ${imageName}=                       Wait For General Panel And Return Name      Image
    Double Click Element                xpath=//li[@id='image_icon-${imageId}']//img
    Check Image Viewer                  ${imageName}                ${dId}

Test Open Viewer Open With right-panel
    [Documentation]     Tests Open With in right-hand panel to open image viewer

    Tree Should Be Visible
    ${pId}    ${dId}    ${imageId}      Select And Expand Project Dataset Image
    ${nodeId}=                          Wait For Image Node         ${imageId}
    ${imageName}=                       Wait For General Panel And Return Name      Image
    Click Element                       xpath=//button[contains(@class, 'btn_openwith')]
    Wait Until Element Is Visible       xpath=//a[@title='Open image with Image viewer']
    Click Element                       xpath=//a[@title='Open image with Image viewer']
    Check Image Viewer                  ${imageName}                ${dId}

Test Open Viewer Open With jsTree
    [Documentation]     Tests Open With in jsTree to open image viewer

    Tree Should Be Visible
    ${pId}    ${dId}    ${imageId}      Select And Expand Project Dataset Image
    ${nodeId}=                          Wait For Image Node         ${imageId}
    ${imageName}=                       Wait For General Panel And Return Name      Image
    Right Click Open With               ${nodeId}      Image viewer
    Check Image Viewer                  ${imageName}                ${dId}

Test Prev Next Buttons

    ${pId}    ${dId}    ${imageId}      Select And Expand Project Dataset Image
    ${nodeId}=                          Wait For Image Node         ${imageId}
    ${imageName}=                       Wait For General Panel And Return Name      Image
    Click Element                       xpath=//a[@title='Open full image viewer in new tab']
    Switch Window                       ${imageName}
    # Loading of image within viewport indicates all is OK
    Wait Until Page Contains Element    xpath=//img[@id='weblitz-viewport-img']     ${WAIT}
    # Prev button should be disabled, Next button enabled
    Element Should Be Disabled          id=prevImage
    Element Should Be Enabled           id=nextImage

    # Clicking Next button will enable Prev button
    Click Element                       id=nextImage
    Wait Until Page Contains Element    xpath=//button[@id='prevImage' and not (@disabled='disabled')]
    # Close Popup window for next test
    Close Window


Test Bulk Annotations
    [Documentation]     Test display of bulk annotations in viewer (tested in main webclient in spw_test.txt)

    Select Experimenter
    Select First Plate With Name        ${PLATE_NAME}
    Select First Run
    Click Well By Name                  A1

    # Select Image to get Name
    Wait Until Page Contains Element    xpath=//div[@id='wellImages']//li/a/div/img[1]
    Click Element                       xpath=//div[@id='wellImages']//li/a/div/img[1]
    ${imgName}=                         Wait For General Panel And Return Name          Image

    Click Element                       xpath=//a[@title='Open full image viewer in new tab']
    Switch Window                       ${imgName}
    # Loading of image within viewport indicates all is OK
    Wait Until Page Contains Element    xpath=//img[@id='weblitz-viewport-img']     ${WAIT}

    Click Link                          Image Information
    Wait Until Element Is Visible       id=bulk-annotations
    Page Should Contain                 Well Type
    Page Should Contain                 Control
    Page Should Contain                 Concentration

[Teardown]    Close Browser
