*** Settings ***
Documentation     Tests the display of Screen-Plate-Well data

Resource          ../../resources/config.txt
Resource          ../../resources/web/login.txt
Resource          ../../resources/web/tree.txt

Suite Setup         User "${USERNAME}" logs in with password "${PASSWORD}"

*** Variables ***
# robot_setup script has created data with these parameters
${PLATE_NAME}               spwTests
${TINY_PLATE_NAME}          tinyPlate
${FIELD_COUNT}              5
${FIELD_COUNT3}             15


*** Keywords ***

Bulk Annotation Should Contain Row
    [Arguments]    ${key}   ${value}
    Wait Until Page Contains Element    xpath=//table[@id='bulk_annotations_table']//tr[descendant::td[contains(text(), '${key}')]]/td[contains(text(), '${value}')]

*** Test Cases ***

Test Auto Load Single Run
    [Documentation]     Selecting Plate with single Run should load plate
    Select Experimenter
    Select First Plate With Name            ${TINY_PLATE_NAME}
    # Plate should auto-load, allowing click on Well
    Click Well By Name                      A1

Test Spw Grid Layout
    [Documentation]     Test loading of Well Images in bottom panel

    Select Experimenter
    Select First Plate With Name            ${PLATE_NAME}
    Select First Run
    Click Well By Name                      A1
    Wait For General Panel And Return Id    Well
    # Right panel should show Well
    Page Should Contain Element             xpath=//div[contains(@class,'data_heading')]/h1[contains(text(), 'A1')]

    # Preview panel enabled for Well
    Click Link                              Preview
    Wait Until Page Contains Element        id=viewport-img
    Click Link                              General

    # Images from a single Well shown in bottom panel
    Wait Until Page Contains Element        xpath=//div[@id='wellImages']//li/a
    Page Should Contain Element             //div[@id='wellImages']//li/a                 limit=${FIELD_COUNT}

    # Shift-Click to select 3 Wells - All images shown in bottom panel
    Shift Click Element                     "img[name='A3']"
    Wait Until Keyword Succeeds             ${TIMEOUT}    ${INTERVAL}     Page Should Contain Element    //div[@id='wellImages']//li/a     limit=${FIELD_COUNT3}
    # Right Panel - batch annotate 3 Wells
    Wait Until Page Contains Element        //h1[@id='batch_ann_title']/span[contains(text(), '3 objects')]

    # No Well-Images selected. Click to select
    Page Should Not Contain Element         xpath=//div[@id='wellImages']//li[contains(@class,'ui-selected')]
    Click Element                           xpath=//div[@id='wellImages']//li/a/div/img[1]
    Page Should Contain Element             xpath=//div[@id='wellImages']//li[1][contains(@class,'ui-selected')]
    ${imgName}=                             Wait For General Panel And Return Name          Image

    # Wrap rows of images by fixed column count
    Input Text                              id=imagesPerRow             2
    Press Keys                              id=imagesPerRow             RETURN
    # First, Third and Fifth images should be aligned
    ${well1x}=                              Get Horizontal Position  xpath=//div[@id='wellImages']//li[1]/a
    ${well3x}=                              Get Horizontal Position  xpath=//div[@id='wellImages']//li[3]/a
    ${well5x}=                              Get Horizontal Position  xpath=//div[@id='wellImages']//li[5]/a
    Should Be Equal                         ${well1x}                   ${well3x}
    Should Be Equal                         ${well1x}                   ${well5x}

Test Spw Spatial Birds Eye
    [Documentation]     Tests layout and clicking of 'spacial bird's eye' panel

    Select Experimenter
    Select First Plate With Name            ${PLATE_NAME}
    Select First Run
    Click Well By Name                      A1

    # Birds eye panel shows all images/fields
    Wait Until Page Contains Element        xpath=//div[@id='well_birds_eye']//img
    Page Should Contain Element             //div[@id='well_birds_eye']//img                 limit=${FIELD_COUNT}

    # Spatial layout: x and y
    ${well1x}=                              Get Horizontal Position  xpath=//div[@id='well_birds_eye']//img[1]
    ${well2x}=                              Get Horizontal Position  xpath=//div[@id='well_birds_eye']//img[2]
    ${well3x}=                              Get Horizontal Position  xpath=//div[@id='well_birds_eye']//img[3]
    ${well4x}=                              Get Horizontal Position  xpath=//div[@id='well_birds_eye']//img[4]
    ${well1y}=                              Get Vertical Position  xpath=//div[@id='well_birds_eye']//img[1]
    ${well2y}=                              Get Vertical Position  xpath=//div[@id='well_birds_eye']//img[2]
    ${well4y}=                              Get Vertical Position  xpath=//div[@id='well_birds_eye']//img[4]
    # First and 4th images should be aligned left (Not 2nd image)
    Log     ${well1x}
    Log     ${well2x}
    Log     ${well3x}
    Log     ${well4x}
    Should Not Be Equal                     ${well1x}                   ${well2x}
    Should Be Equal                         ${well1x}                   ${well4x}
    # First and 2nd images should be aligned left (Not 4th image)
    Should Not Be Equal                     ${well1y}                   ${well4y}
    Should Be Equal                         ${well1y}                   ${well2y}

    # Click image in birds eye - should select in wellImages bottom panel
    Page Should Not Contain Element         xpath=//div[@id='wellImages']//li[contains(@class,'ui-selected')]
    Click Element                           xpath=//div[@id='well_birds_eye']//img[1]
    Page Should Contain Element             xpath=//div[@id='wellImages']//li[1][contains(@class,'ui-selected')]
    Wait For General Panel And Return Id    Image

    # Meta Click to select additional images
    Meta Click Element                      "#well_birds_eye img:eq(2)"
    Wait Until Page Contains Element        //h1[@id='batch_ann_title']/span[contains(text(), '2 objects')]
    Page Should Contain Element              //div[@id='wellImages']//li[contains(@class,'ui-selected')]             limit=2

    # Shift-Click range in wellImages panel - updates selection in 'birds eye'
    Shift Click Element                     "#wellImages img:eq(4)"
    Wait Until Page Contains Element        //h1[@id='batch_ann_title']/span[contains(text(), '5 objects')]
    Page Should Contain Element              //div[@id='well_birds_eye']//img[contains(@class,'ui-selected')]        limit=5

Test Bulk Annotations
    [Documentation]     Test display of bulk annotations added in setup

    Select Experimenter
    Select First Plate With Name            ${PLATE_NAME}
    Select First Run
    Click Well By Name                      A1
    Wait Until Page Contains Element        xpath=//h1[@data-name='tables']
    Click Element                           xpath=//h1[@data-name='tables']
    Wait Until Element Is Visible           id=bulk_annotations_table
    Bulk Annotation Should Contain Row      Well Type       Control
    Bulk Annotation Should Contain Row      Concentration   0
    Click Well By Name                      A2
    Bulk Annotation Should Contain Row      Well Type       Treatment
    Bulk Annotation Should Contain Row      Concentration   10

[Teardown]    Close Browser
