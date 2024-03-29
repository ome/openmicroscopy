*** Settings ***
Documentation     Tests copying, pasting and applying Rendering settings.

Resource          ../../resources/config.txt
Resource          ../../resources/web/login.txt
Resource          ../../resources/web/tree.txt

Suite Setup         User "${USERNAME}" logs in with password "${PASSWORD}"

*** Variables ***

${importedChColor}                  808080
${importedMax}                      255
${sizeZ}                            3
${defaultZ}                         2
${currZ}                            3
${multic_dataset}                   MultiChannel Images

*** Keywords ***

Edit Channel Start
    [Arguments]                             ${index}    ${value}
    Click Element                           xpath=//input[@id="wblitz-ch${index}-cw-start"]
    Input Text                              wblitz-ch${index}-cw-start    ${value}      False
    # Switch focus to other element to trigger a 'focus lost' event
    Click Element                           xpath=//input[@id="wblitz-ch0-cw-end"]

Toggle Channel Active
    [Arguments]                             ${index}
    Click Element                           xpath=//button[@id="rd-wblitz-ch${index}"]

Channel Should Be Active
    [Arguments]                             ${index}
    ${style}=                               Get Element Attribute       xpath=//button[@id="rd-wblitz-ch${index}"]    attribute=class
    Should Contain                          ${style}                    pressed

Channel Should Not Be Active
    [Arguments]                             ${index}
    ${style}=                               Get Element Attribute       xpath=//button[@id="rd-wblitz-ch${index}"]    attribute=class
    Should Not Contain                      ${style}                    pressed

Pick Color
    [Arguments]                             ${hexColor}
    Click Element                           xpath=//button[@id="wblitz-ch0-color"]
    Wait Until Element Is Visible           id=cbpicker-box
    # Click color-picker button
    Click Element                           xpath=//label[@for="${hexColor}"]
    # Wait for the channel toggle button to update
    Wait For Channel Color                  ${hexColor}

Wait For Channel Color
    [Arguments]          ${hexColor}
    # Can't use @style to check color since Firefox will auto convert hex to rgb() in the DOM but Chrome won't.
    # Wait Until Element Is Visible           xpath=//button[@id="rd-wblitz-ch0"][contains(@style, "background-color: ${rgbColor}")]      ${WAIT}
    Wait Until Element Is Visible           xpath=//button[@id="wblitz-ch0-color"][@data-color="${hexColor}"]

Pick Lut
    [Arguments]                             ${lutName}      ${channelIdx}
    Click Element                           xpath=//button[@id="wblitz-ch${channelIdx}-color"]
    Wait Until Element Is Visible           id=cbpicker-box
    Click Element                           xpath=//div[contains(@class, 'lutpicker')]//label[contains(text(), '${lutName}')]
    Wait For Image Src                      ${lutName}
    Element Should Not Be Visible           id=cbpicker-box

Check Image Src
    [Arguments]                             ${findInImgSrc}
    ${imageSrc}=                            Get Element Attribute       xpath=//img[contains(@class, 'weblitz-viewport-img')]    attribute=src
    Should Contain                          ${imageSrc}                 ${findInImgSrc}

Wait For Image Src
    [Arguments]                             ${findInImgSrc}
    Wait Until Keyword Succeeds             ${TIMEOUT}   ${INTERVAL}    Check Image Src         ${findInImgSrc}

Check Inverted
    [Arguments]                             ${channelIdx}               ${checked}
    Click Element                           xpath=//button[@id="wblitz-ch${channelIdx}-color"]
    Wait Until Element Is Visible           id=cbpicker-box
    Run Keyword If      ${checked}          Checkbox Should Be Selected                 id=invert
    ...                 ELSE                Checkbox Should Not Be Selected             id=invert
    Click Element                           xpath=//div[contains(@class, 'cbpicker')]//div[contains(@class, 'postit-close-btn')]
    Element Should Not Be Visible           id=cbpicker-box

Toggle Inverted
    [Arguments]                             ${channelIdx}
    Click Element                           xpath=//button[@id="wblitz-ch${channelIdx}-color"]
    Wait Until Element Is Visible           id=cbpicker-box
    Select Checkbox                         id=invert
    Element Should Not Be Visible           id=cbpicker-box

Wait For BlockUI
    # Wait Until Element Is Visible           xpath=//div[contains(@class, 'blockOverlay')]
    Wait For Condition                      return ($("div.blockOverlay").length == 0)

Wait For Preview Load
    [Arguments]          ${status}          ${oldIdentifier}
    Run Keyword If  '${status}'=='PASS'     Wait Until Page Contains Element        xpath=//button[@id='preview_open_viewer'][@rel!='${oldIdentifier}']       ${WAIT}
    Wait Until Element Is Visible           xpath=//button[@id="wblitz-ch0-color"]      ${WAIT}
    Wait Until Element Is Visible           xpath=//button[@class="rdef clicked"]       ${WAIT}
    ${status}    ${oldId}                   Run Keyword And Ignore Error    Get Element Attribute      xpath=//button[@id="preview_open_viewer"]              attribute=rel
    [Return]                                ${status}          ${oldId}

Wait For Toolbar Button Enabled
    [Arguments]            ${buttonId}
    Wait Until Keyword Succeeds     ${TIMEOUT}   ${INTERVAL}      Element Should Be Enabled      id=${buttonId}
    Page Should Not Contain Element     xpath=//button[@id='${buttonId}'][contains(@class, 'button-disabled')]

Wait For Toolbar Button Disabled
    [Arguments]            ${buttonId}
    Wait Until Keyword Succeeds     ${TIMEOUT}   ${INTERVAL}      Element Should Be Disabled     id=${buttonId}
    Page Should Contain Element     xpath=//button[@id='${buttonId}'][contains(@class, 'button-disabled')]

Right Click Image Rendering Settings
    [Arguments]            ${imageId}       ${optionText}
    ${treeId}=                              Wait For Image Node           ${imageId}
    Right Click Rendering Settings          ${treeId}       ${optionText}

Right Click Dataset Rendering Settings
    [Arguments]            ${datasetId}       ${optionText}
    ${treeId}=                              Wait For Dataset Node           ${datasetId}
    Right Click Rendering Settings          ${treeId}       ${optionText}

Right Click Rendering Settings
    [Arguments]            ${treeId}       ${optionText}
    Open Context Menu                       xpath=//li[@id='${treeId}']/span
    Mouse Over                              xpath=//ul[contains(@class, 'jstree-contextmenu')]//a[contains(text(), 'Rendering Settings...')]
    Click Element                           xpath=//ul[contains(@class, 'jstree-contextmenu')]//li[descendant::a[contains(text(), 'Rendering Settings')]]//a[contains(text(), "${optionText}")]
    # E.g. 'Copy' option won't have 'OK' dialog
    Run Keyword And Ignore Error            Click Dialog Button                     OK

*** Test Cases ***

Test Rdef Copy Paste Save
    [Documentation]     Tests Copy and Paste rdef, then Save and 'Save All'

    Go To                                   ${WELCOME URL}
    Select Experimenter

    Select First Project With Children
    # Start by resetting rdefs to imported settings within Dataset
    # In case previous failed tests left unexpected rdefs
    ${datasetId}=                           Select First Dataset With Children
    Right Click Dataset Rendering Settings  ${datasetId}            Set Imported and Save
    Select First Image

    ${imageId}=                             Wait For General Panel And Return Id    Image
    Click Link                              Preview
    ${status}    ${oldId}                   Wait For Preview Load       FAIL      '1'

    # Undo, Redo & Save should be disabled
    Element Should Be Disabled              id=rdef-undo-btn
    Element Should Be Disabled              id=rdef-redo-btn
    Element Should Be Disabled              id=rdef-setdef-btn

    # Change Z-index: on callback, Save is enabled but not Undo/Redo
    Element Text Should Be                  id=wblitz-z-count       ${sizeZ}
    Element Text Should Be                  id=wblitz-z-curr        ${defaultZ}
    Click Element                           id=viewport-zsl-bup
    Wait For Toolbar Button Enabled         rdef-setdef-btn
    Element Text Should Be                  id=wblitz-z-curr        ${currZ}
    Element Should Be Disabled              id=rdef-undo-btn
    Element Should Be Disabled              id=rdef-redo-btn
    # change back again...
    Click Element                           id=viewport-zsl-bdn
    Wait For Toolbar Button Disabled        rdef-setdef-btn
    Element Text Should Be                  id=wblitz-z-curr        ${defaultZ}

    # Set start / end slider values - Don't clear field first - empty field gives NaN
    Input Text                              id=wblitz-ch0-cw-start      111     False
    Input Text                              id=wblitz-ch0-cw-end        ""      False

    # Color-picker, Yellow then Blue.
    Pick Color          FFFF00
    Wait For Channel Color                  FFFF00
    Pick Color          0000FF
    Wait For Channel Color                  0000FF

    # ONLY Redo should be disabled
    Element Should Be Enabled               id=rdef-undo-btn
    Element Should Be Disabled              id=rdef-redo-btn
    Element Should Be Enabled               id=rdef-setdef-btn
    # Click Undo - Channel should be Yellow
    Click Element                           id=rdef-undo-btn
    Wait For Channel Color                  FFFF00

    # And all buttons Undo, Redo & Save enabled
    Element Should Be Enabled               id=rdef-undo-btn
    Element Should Be Enabled               id=rdef-redo-btn
    Element Should Be Enabled               id=rdef-setdef-btn

    # Save (with Yellow channel) & wait for thumbnail to update
    ${thumbSrc}=                            Get Element Attribute      xpath=//button[@class="rdef clicked"]/img    attribute=src
    Click Element                           id=rdef-setdef-btn
    Wait For BlockUI
    Wait Until Page Contains Element        xpath=//button[@class="rdef clicked"]/img[@src!='${thumbSrc}']
    # Redo (to Blue channel)
    Click Element                           id=rdef-redo-btn
    Wait For Channel Color                  0000FF
    # Copy (paste button is enabled)
    Click Element                           id=rdef-copy-btn
    Wait For Toolbar Button Enabled         rdef-paste-btn

    # Check that 'Save' has worked by refreshing right panel (click refresh)
    Click Element                           id=refreshButton
    ${status}    ${oldId}                   Wait For Preview Load   ${status}   ${oldId}
    Wait For Toolbar Button Enabled         rdef-paste-btn
    # Channel should be Yellow
    Wait For Channel Color                  FFFF00

    # Select Next Image
    Click Next Thumbnail
    ${status}    ${oldId}                   Wait For Preview Load   ${status}   ${oldId}

    # Images should be compatible, so 'Paste' should become enabled.
    Wait For Toolbar Button Enabled         rdef-paste-btn

    # Paste (Blue channel)
    Click Element                           xpath=//button[@id='rdef-paste-btn']
    Wait For Channel Color                  0000FF
    Textfield Value Should Be               wblitz-ch0-cw-start     111
    Textfield Value Should Be               wblitz-ch0-cw-end       255

    # Save to all (Blue channel)
    Click Element                           id=rdef-save-all

    # Return to Previous Image (now Blue)
    Select Image By Id                      ${imageId}
    ${status}    ${oldId}                   Wait For Preview Load   ${status}   ${oldId}
    Wait For Channel Color                  0000FF

Test Owners Rdef
    [Documentation]     Log in as non-owner and apply Imported and Owner's settings.

    Go To                                   ${WELCOME URL}
    Select Experimenter
    Select And Expand Image
    ${imageId}=                             Wait For General Panel And Return Id      Image
    Click Link                              Preview
    ${status}    ${oldId}                   Wait For Preview Load       FAIL      '1'

    # Set to "Imported"
    Click Element                           id=rdef-reset-btn
    Wait For Channel Color                  ${importedChColor}
    Textfield Value Should Be               wblitz-ch0-cw-end           255

    # Need user to save an Rdef that is different from 'Imported'
    # Save Channel 'Green' and Window End: 100.5
    Unselect Checkbox                       rd-wblitz-rmodel
    Pick Color                              00FF00
    Input Text                              id=wblitz-ch0-cw-start        10.5       False
    Click Element                           id=rdef-setdef-btn
    Wait For BlockUI

    # Click next thumbnail and get the image ID
    Click Next Thumbnail
    ${status}    ${oldId}                   Wait For Preview Load   ${status}   ${oldId}
    Click Link                              General
    ${imageId_2}=                           Wait For General Panel And Return Id      Image
    Log Out

    # Log in as Root - go to user's Image
    User "${ROOT USERNAME}" logs in with password "${ROOT PASSWORD}"
    Maximize Browser Window
    Go To                                   ${WELCOME URL}?show=image-${imageId}
    Wait For General Panel                  Image
    Click Link                              Preview
    ${status}    ${oldId}                   Wait For Preview Load   ${status}   ${oldId}

    # Set to "Imported"
    Click Element                           id=rdef-reset-btn
    Wait For Channel Color                  ${importedChColor}
    Textfield Value Should Be               wblitz-ch0-cw-start           0
    Checkbox Should Be Selected             rd-wblitz-rmodel

    # Set to Owner's (click on thumbnail)
    Click Element                           xpath=//button[contains(@class, 'rdef')][descendant::span[contains(@class, 'owner')]]
    Wait For Channel Color                  00FF00
    Textfield Value Should Be               wblitz-ch0-cw-start           10.5
    Checkbox Should Not Be Selected         rd-wblitz-rmodel

    # Set to Full Range
    Click Element                           id=rdef-fullrange-btn
    Wait For Channel Color                  00FF00
    Textfield Value Should Be               wblitz-ch0-cw-start           0
    Checkbox Should Not Be Selected         rd-wblitz-rmodel

    # 'Save All' with some different settings (Red channel)
    Unselect Checkbox                       rd-wblitz-rmodel
    Pick Color                              FF0000
    Click Element                           id=rdef-save-all
    Wait For BlockUI

    # Min/Max
    Click Element                           id=rdef-minmax-btn
    Wait For Channel Color                  FF0000
    Textfield Value Should Be               wblitz-ch0-cw-start           0

    # New settings (White channel, start) and 'Copy'
    Input Text                              id=wblitz-ch0-cw-start        12        False
    Pick Color                              FFFFFF
    Click Element                           xpath=//button[@id='rdef-copy-btn']
    Wait For Toolbar Button Enabled         rdef-paste-btn

    # Test 'Paste and Save' with right-click on different Image in tree
    # (check thumb refresh by change of src)
    ${thumbSrc}=                            Get Element Attribute      xpath=//li[@id="image_icon-${imageId_2}"]/div[@class="image"]/a/img    attribute=src
    Right Click Image Rendering Settings    ${imageId_2}            Paste and Save
    Wait Until Page Contains Element        xpath=//li[@id="image_icon-${imageId_2}"]/div[@class="image"]/a/img[@src!='${thumbSrc}']
    # Check applied by refresh right panel
    Select Image By Id                      ${imageId_2}
    ${status}    ${oldId}                   Wait For Preview Load   ${status}   ${oldId}
    Wait For Channel Color                  FFFFFF
    Textfield Value Should Be               wblitz-ch0-cw-start           12

    # Test Set Owner's in same way on first Image
    ${thumbSrc}=                            Get Element Attribute      xpath=//li[@id="image_icon-${imageId}"]/div[@class="image"]/a/img    attribute=src
    Right Click Image Rendering Settings    ${imageId}            Set Owner's and Save
    Wait Until Page Contains Element        xpath=//li[@id="image_icon-${imageId}"]/div[@class="image"]/a/img[@src!='${thumbSrc}']
    # Check applied by refresh right panel
    Click Element                           id=image_icon-${imageId}
    ${status}    ${oldId}                   Wait For Preview Load   ${status}   ${oldId}
    Wait For Channel Color                  00FF00
    Textfield Value Should Be               wblitz-ch0-cw-start           10.5

    # Test "Set Imported" on first Image
    ${thumbSrc}=                            Get Element Attribute      xpath=//li[@id="image_icon-${imageId}"]/div[@class="image"]/a/img    attribute=src
    Right Click Image Rendering Settings    ${imageId}            Set Imported and Save
    Wait Until Page Contains Element        xpath=//li[@id="image_icon-${imageId}"]/div[@class="image"]/a/img[@src!='${thumbSrc}']
    # Check applied by refresh right panel
    Click Element                           id=image_icon-${imageId}
    ${status}    ${oldId}                   Wait For Preview Load   ${status}   ${oldId}
    Wait For Channel Color                  ${importedChColor}
    Textfield Value Should Be               wblitz-ch0-cw-end           ${importedMax}

    # Open full image viewer
    # Toggle the color, then paste settings and check it has reverted

    # Bit more reliable with this, but try to remove it if possible...

    Go To                                   ${WELCOME URL}img_detail/${imageId}/
    Wait Until Page Contains Element        id=wblitz-ch0
    ${checked1}=                            Checkbox Should Be Selected  id=wblitz-rmodel
    Click Element                           id=wblitz-rmodel
    ${checked2}=                            Checkbox Should Not Be Selected  id=wblitz-rmodel
    Click Link                              Edit
    # We are currently at 'Imported' settings
    Textfield Value Should Be               wblitz-ch0-cw-end    255

    # Paste to settings above
    Click Element                           xpath=//button[@id='rdef-paste-btn']
    # Save
    Click Element                           id=rdef-setdef-btn
    # Wait for response
    Wait Until Page Contains Element        id=weblitz-viewport-msg
    Wait For Condition                      return ($(".blockUI").length == 0)

    # Refresh page to check Save
    Reload Page
    Wait Until Page Contains Element        id=wblitz-ch0
    Click Link                              Edit
    Textfield Value Should Be               wblitz-ch0-cw-end    255

Test Rdef Save
    [Documentation]     Tests saving rendering settings, including inactive channels

    Go To                               ${WELCOME URL}
    Select Experimenter
    Select And Expand Node              ${multic_dataset}
    ${nodeId}                           Wait For Dataset Node Text      ${multic_dataset}
    Right Click Rendering Settings      ${nodeId}                       Set Imported and Save
    ${imageId}=                         Select First Image

    Click Link                          Preview
    ${status}    ${oldId}               Wait For Preview Load       FAIL      '1'

    # Undo, Redo & Save should be disabled
    Element Should Be Disabled          id=rdef-undo-btn
    Element Should Be Disabled          id=rdef-redo-btn
    Element Should Be Disabled          id=rdef-setdef-btn

    Edit Channel Start                  0   50

    # Undo & Save should be enabled now
    Element Should Be Enabled           id=rdef-undo-btn
    Element Should Be Enabled           id=rdef-setdef-btn

    # Deactivate second and third channel
    Toggle Channel Active               1
    Toggle Channel Active               2

    Edit Channel Start                  1   50
    Edit Channel Start                  2   50

    # Save rendering settings
    ${thumbSrc}=                        Get Element Attribute      xpath=//button[@class="rdef clicked"]/img    attribute=src
    Click Element                       id=rdef-setdef-btn
    # Wait for response
    Wait For BlockUI
    Wait Until Page Contains Element    xpath=//button[@class="rdef clicked"]/img[@src!='${thumbSrc}']

    # Check that 'Save' has worked by switching to the dataset and back again to the image
    Select And Expand Node              nodeText=${multic_dataset}
    Click Link                          General
    Wait Until Element Is Visible       xpath=//div[@id="general_tab"]
    Select Image By Id                  ${imageId}
    Click Link                          Preview
    ${status}    ${oldId}               Wait For Preview Load       FAIL      '1'

    # Check that all values have been saved, including deactivated channels
    Textfield Value Should Be           wblitz-ch0-cw-start           50
    Textfield Value Should Be           wblitz-ch1-cw-start           50
    Textfield Value Should Be           wblitz-ch2-cw-start           50
    Channel Should Be Active            0
    Channel Should Not Be Active        1
    Channel Should Not Be Active        2

Test LUTs Inverted
    [Documentation]     Tests LUTs and Inverted, Copying and Pasting.
    Go To                                   ${WELCOME URL}
    Select Experimenter
    Select And Expand Node                  ${multic_dataset}
    ${nodeId}                               Wait For Dataset Node Text      ${multic_dataset}
    Right Click Rendering Settings          ${nodeId}                       Set Imported and Save
    Select First Image
    ${imageId}=                             Wait For General Panel And Return Id    Image
    Click Link                              Preview
    ${status}   ${oldId}                    Wait For Preview Load                   FAIL      '1'

    Pick Lut                                16_colors                       0

    # Inverted          of first channel
    Toggle Inverted                         0
    Wait For Image Src                      maps=[{%22inverted%22:{%22enabled%22:true}
    # Check the Intensity checkbox resets when dialog launched for different channels
    Check Inverted                          1                           ${False}
    Check Inverted                          0                           ${True}

    # Copy Settings, refresh...
    Click Element                           id=rdef-copy-btn
    Click Element                           id=refreshButton
    ${status}    ${oldId}                   Wait For Preview Load       ${status}   ${oldId}
    # Check changes above weren't saved (first channel Red)
    Wait For Image Src                      FF0000
    Check Inverted                          0                           ${False}
    Wait For Image Src                      maps=[{%22inverted%22:{%22enabled%22:false}

    # ...and paste (to same image)
    Click Element                           id=rdef-paste-btn
    Check Inverted                          0                           ${True}
    Check Inverted                          1                           ${False}
    Wait For Image Src                      maps=[{%22inverted%22:{%22enabled%22:true}
    Wait For Image Src                      16_colors

    # Paste and Save by right-click on next Image in Tree
    Click Next Thumbnail
    Wait For Image Src                      FF0000
    ${imageId2}=                            Get Selected Id From Tree
    ${imgNodeId2}=                          Wait For Image Node         ${imageId2}
    Right Click Rendering Settings          ${imgNodeId2}               Paste and Save
    Wait For Image Src                      16_colors
    Check Inverted                          0                           ${True}

    # Pick a different LUT and Save
    Pick Lut                                5_ramps                     0
    Click Element                           id=rdef-setdef-btn
    Wait For BlockUI

    # Copy in Tree (copies the 'fromid' to session instead of rdef json)
    Right Click Rendering Settings          ${imgNodeId2}               Copy
    # Return to first Image and check nothing seved yet
    Click Thumbnail                         ${imageId}
    Wait For Image Src                      FF0000
    Check Inverted                          0                           ${False}

    # Paste settings 'fromid' in Preview Panel...
    Click Element                           id=rdef-paste-btn
    Wait For Image Src                      5_ramps
    Check Inverted                          0                           ${True}
    # Refresh
    Click Element                           id=refreshButton
    Wait For Image Src                      FF0000

    # Paste settings 'fromid' in Tree
    ${imgNodeId}=                           Wait For Image Node         ${imageId}
    Right Click Rendering Settings          ${imgNodeId}                Paste and Save
    Wait For Image Src                      5_ramps
    Check Inverted                          0                           ${True}

Test Launch Full Viewer
    [Documentation]     Tests viewer launched from Preview panel with current settings
    Go To                                   ${WELCOME URL}
    Select Experimenter
    Select And Expand Node                  ${multic_dataset}
    ${nodeId}                               Wait For Dataset Node Text      ${multic_dataset}
    Right Click Rendering Settings          ${nodeId}                       Set Imported and Save
    Select First Image
    ${imageName}=                           Wait For General Panel And Return Name              Image
    Click Link                              Preview
    Wait For Preview Load                   FAIL      '1'

    # Change settings
    Pick Lut                                16_colors       1
    Toggle Inverted                         0
    Pick Color                              FFFF00
    Toggle Channel Active                   2

    Click Link                              id=preview_open_viewer
    # Wait Until Keyword Succeeds             ${TIMEOUT}     ${INTERVAL}     Select Window     title=${imageName}
    Switch Window                           NEW
    Wait Until Page Contains Element        id=wblitz-ch0
    # Check various channel settings and inverted map are applied
    Wait For Image Src                      c=1|0:255$FFFF00,2|0:255$16_colors.lut,-3|0:255$0000FF
    Wait For Image Src                      maps=[{%22inverted%22:{%22enabled%22:true}

[Teardown]    Close Browser
