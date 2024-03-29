*** Settings ***
Library         String

Resource          ../../resources/config.txt
Resource          ../../resources/web/login.txt
Resource          ../../resources/web/tree.txt

Suite Setup         User "${USERNAME}" logs in with password "${PASSWORD}"

*** Variables ***

${imageName1}                       FilterImageOne
${imageName2}                       FilterImageTwo
${plateName}                        spwTests
${FIELD_COUNT}                      5
# Full Well name may make test fragile?
${wellName}                         test&plates=1&plateAcqs=1&plateRows=2&plateCols=3&fields=1&screens=0.fake [test]
${runName}                          PlateAcquisition Name 0

*** Keywords ***

Wait For Right Panel
    [Arguments]                             ${dataType}       ${dataName}
    Wait Until Page Contains Element        xpath=//tr[contains(@class,'data_heading_id')]//th[contains(text(),'${dataType} ID:')]
    Wait Until Page Contains Element        xpath=//div[contains(@class,'data_heading')]//*[contains(text(),'${dataName}')]
    ${dataId}=                              Get Text                css=tr.data_heading_id strong
    [Return]                                ${dataId}

Check Well Selected
    [Arguments]                             ${wellName}       ${wellId}
    Wait Until Page Contains Element        xpath=//td[@id='well-${wellId}'][contains(@class,'well')][contains(@class,'ui-selected')]
    Wait Until Page Contains Element        xpath=//td[@id='well-${wellId}']/div/img[@name='${wellName}']

Get Link Button Url
    Click Element                           id=show_link_btn
    ${linkUrl}=                             Get Value               xpath=//div[@id='link_info_popup']//input
    [Return]                                ${linkUrl}

*** Test Cases ***

Test Show Plate
    [Documentation]     Tests that ?show functionality works on pre-imported Plates

    # Load the Plate by show
    Go To                               ${WELCOME URL}?show=plate.name-${plateName}
    ${plateId}=                         Wait For Right Panel            Plate               ${plateName}
    ${nodeId}=                          Plate Should Be Selected By Name                    ${plateName}
    # Check URL from the Link button
    ${plateUrl}=                        Get Link Button Url
    Should Be Equal                     ${plateUrl}                     ${WELCOME URL}?show=plate-${plateId}

    #Load Well by path
    Go To                               ${WELCOME URL}?path=plate.name-${plateName}|well.name-A1
    ${wellId}=                          Wait For Right Panel            Well                A1
    Check Well Selected                 A1                              ${wellId}
    ${runId}=                           Get Selected Id From Tree
    # Check URL from the Link button
    ${wellUrl}=                         Get Link Button Url
    Should Be Equal                     ${wellUrl}                      ${WELCOME URL}?show=well-${wellId}

    # Get Next Well IDs to Test multi-selection of Wells
    ${well-id-A2}=                      Get Element Attribute           xpath=//td[contains(@class, 'well')][descendant::img[@name='A2']]    attribute=id
    ${w}    ${wellA2Id}=                Split String                    ${well-id-A2}    -
    Go To                               ${WELCOME URL}?show=well-${wellId}|well-${wellA2Id}
    Wait Until Page Contains Element    id=batch_ann_title
    Check Well Selected                 A1                              ${wellId}
    Check Well Selected                 A2                              ${wellA2Id}
    Page Should Contain Element         //td[contains(@class, 'well')][contains(@class,'ui-selected')]        limit=2

    # Load Run by run.id
    Go To                               ${WELCOME URL}?show=run.id-${runId}
    Wait For Right Panel                Run                             ${runName}
    Run Should Be Selected By Id        ${runId}

    # Load the Plate by ID
    Go To                               ${WELCOME URL}?show=plate-${plateId}
    Wait For Right Panel                Plate                           ${plateName}
    ${nodeId}=                          Wait For Plate Node Text        ${plateName}
    Node Should Be Selected By Id       ${nodeId}

Test Show Well Images
    [Documentation]     Tests Show of multiple images in a Well

    Select Experimenter
    Select First Plate With Name            ${plateName}
    Select First Run
    Click Well By Name                      A1

    # Images from a single Well shown in bottom panel
    Wait Until Page Contains Element        xpath=//div[@id='wellImages']//li/a
    Page Should Contain Element             //div[@id='wellImages']//li/a                 limit=${FIELD_COUNT}

    # Select Range: Shift-click 5th image
    Click Element                           xpath=//div[@id='wellImages']//li/a/div/img[1]
    Shift Click Element                     "#wellImages img:eq(4)"
    Wait Until Page Contains Element        //h1[@id='batch_ann_title']/span[contains(text(), '5 objects')]

    # Use 'Link' button to get link
    Click Element                           id=show_link_btn
    ${imagesUrl}=                           Get Element Attribute                   id=link_info_popup_string    attribute=value
    Go To                                   ${imagesUrl}
    Wait Until Page Contains Element        //h1[@id='batch_ann_title']/span[contains(text(), '5 objects')]
    Page Should Contain Element             //div[@id='wellImages']//li/a                 limit=${FIELD_COUNT}

Test Show PDI
    [Documentation]     Tests that ?show functionality works on Images in Project/Dataset
    # show image.name
    Go To                               ${WELCOME URL}?show=image.name-${imageName1}
    ${imageId1}=                        Wait For Right Panel                Image               ${imageName1}
    Thumbnail Should Be Selected        ${imageId1}

    # path image.name
    Go To                               ${WELCOME URL}?path=image.name-${imageName2}
    ${imageId2}=                        Wait For Right Panel                Image               ${imageName2}
    Thumbnail Should Be Selected        ${imageId2}

    # show Multiple images by ID
    Go To                               ${WELCOME URL}?show=image-${imageId1}|image-${imageId2}
    Wait Until Page Contains Element    id=batch_ann_title
    Page Should Contain Element         //li[contains(@class, 'row')][contains(@class,'ui-selected')]        limit=2

[Teardown]    Close Browser
