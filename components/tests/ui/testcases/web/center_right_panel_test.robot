*** Settings ***
Documentation     Tests synchronization between panels

Resource          ../../resources/config.txt
Resource          ../../resources/web/login.txt
Resource          ../../resources/web/tree.txt

Suite Setup         User "${USERNAME}" logs in with password "${PASSWORD}"

*** Variables ***
# robot_setup script has created data with these parameters
${PLATE_NAME}               spwTests

*** Keywords ***

Get Number Of Selected Objects From Tree
    ${numOfObjects}=                Get Element Count        xpath=//li[contains(@class, 'jstree-node')]//span[contains(@class, 'jstree-clicked')]
    [return]                        ${numOfObjects}

Get Number Of Selected Objects From Center Panel
    ${numOfObjects}=                Get Element Count        xpath=//div[@id="icon_table"]//li[contains(@class, 'ui-selected')]
    [return]                        ${numOfObjects}


*** Test Cases ***

Test Select First Dataset
    ${pId}    ${datasetId}                      Select And Expand Project Dataset
    Wait Until Right Panel Loads Everything     Dataset                  ${datasetId}
    # Wait Until Center Panel Loads               Dataset
    # Check Center and Right Panel Sync For Image
    ${imageId}=                     Select First Image
    Wait Until Right Panel Loads Everything     Image                    ${imageId}

    ${imageId1}=                    Get Id From Selected Item In Tree
    ${imageId2}                     Get Id From Selected Thumbnail
    ${imageId3}                     Get Id From Right Panel

    Should Be Equal                 ${imageId1}     ${imageId}
    Should Be Equal                 ${imageId2}     ${imageId}
    Should Be Equal                 ${imageId3}     ${imageId}

    Click Next Thumbnail
    ${imageId1}                     Get Id From Selected Item In Tree
    ${imageId2}                     Get Id From Selected Thumbnail
    ${imageId3}                     Get Id From Right Panel

    Should Be Equal                 ${imageId1}     ${imageId2}
    Should Be Equal                 ${imageId2}     ${imageId3}
    Should Be Equal                 ${imageId3}     ${imageId1}

Test Multi Selections

    ${pId}    ${dId}    ${imageId}      Select And Expand Project Dataset Image
    Click Next Thumbnail
    ${imageId1}                     Get Id From Selected Thumbnail
    Meta Click Thumbnail            ${imageId}
    Wait Until Page Contains Element    xpath=//h1[@id='batch_ann_title']/span[contains(text(), '2 objects')]

    ${numOfObjects}                 Get Number Of Selected Objects From Tree
    ${numOfObjects1}                Get Number Of Selected Objects From Center Panel

    Should Be Equal                 ${numOfObjects}     ${numOfObjects1}

[Teardown]    Close Browser
