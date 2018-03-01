*** Settings ***
Documentation     Tests submission of forms.

Resource          ../../resources/web/annotation.txt
Resource          ../../resources/config.txt
Resource          ../../resources/web/login.txt
Resource          ../../resources/web/tree.txt

Suite Setup         User "${USERNAME}" logs in with password "${PASSWORD}"

*** Variables ***
${commentText}          Robot test adding this comment
${commentTextTwo}       A second comment added by Robot test
${commentTextThree}     This will be added to Two Datasets
${commentTextFour}      I (Robot) just love adding comments!
${fileName}             robot_file_annotation.txt
${fileNameTwo}          robot_file_annotation2.txt
${SEARCH URL}           ${WELCOME URL}search/


*** Keywords ***

Check Comment Gone
    [Arguments]                                 ${text}
    Page Should Not Contain Element             xpath=//div[@class='commentText'][contains(text(), '${text}')]

Remove Comment
    [Arguments]                                 ${text}
    Click Element                               xpath=//div[contains(@class, 'ann_comment_text')][descendant::div[contains(text(), '${text}')]]/img[@class='removeComment']
    Click Dialog Button                         OK
    Wait Until Keyword Succeeds                 ${TIMEOUT}   ${INTERVAL}    Check Comment Gone  ${text}

Check For Rating
    [Arguments]                                 ${rating}
    Wait Until Element Is Visible               xpath=//img[@title='Click to add rating']
    ${src}=                                     Get Element Attribute   xpath=//img[@title='Click to add rating']   attribute=src
    Should Contain                              ${src}                  rating${rating}


*** Test Cases ***

Test Rate Images

    Select Experimenter
    Select And Expand Node                      Project 1
    Select And Expand Node                      Dataset 1
    ${imageId}=                                 Select First Image
    ${nodeId}=                                  Wait For Image Node         ${imageId}

    # Close the details tab
    Click Element                               xpath=//h1[@data-name='details']

    Click Element                               xpath=//h1[@data-name='ratings']
    Wait Until Element Is Visible               id=rating_annotations
    # Add Rating of 5 *****
    Click Rating                                5
    Meta Click Node                             ${nodeId}
    Click Node                                  ${nodeId}
    Check For Rating                            5

    # Add Rating of 2 **
    Click Next Thumbnail
    ${imageId2}=                                Get Id From Selected Thumbnail
    ${nodeId2}=                                 Wait For Image Node         ${imageId2}
    Click Rating                                2
    Meta Click Node                             ${nodeId2}
    Click Node                                  ${nodeId2}
    Check For Rating                            2

    # Add Rating of 2 **
    Click Next Thumbnail
    ${imageId3}=                                Get Id From Selected Thumbnail
    ${nodeId3}=                                 Wait For Image Node         ${imageId3}
    Click Rating                                2
    Meta Click Node                             ${nodeId3}
    Click Node                                  ${nodeId3}
    Check For Rating                            2

    # Check average
    Click Node                                  ${nodeId}
    Meta Click Node                             ${nodeId2}
    Meta Click Node                             ${nodeId3}
    Wait Until Page Contains Element            xpath=//h1[@id='batch_ann_title']/span[contains(text(), '3 objects')]
    Wait Until Element Is Visible               xpath=//span[@id='ratingsAverage'][contains(text(), '3')]    timeout=${TIMEOUT}
    Page Should Contain Element                 locator=//ul[@id='dataIcons']/li[contains(@class, 'ui-selected')]     limit=3

    # Filter by Rating 2 **
    Select From List By Label                   id=choosefilter         By Rating
    Filter Rating                               2
    # Only 2 thumbnails should remain Visible and Selected
    Wait Until Element Is Not Visible           id=image_icon-${imageId}
    Count Thumbs Without Class                  ratingFilter_hidden     2

    # Filter by 5 *****
    Filter Rating                               5
    Wait Until Element Is Not Visible           id=image_icon-${imageId2}
    Wait Until Element Is Not Visible           id=image_icon-${imageId3}
    Count Thumbs Without Class                  ratingFilter_hidden     1


Test Delete Rating

    Select Experimenter
    Select And Expand Node                      Project 1
    Select And Expand Node                      Dataset 1
    ${imageId}=                                 Select First Image
    ${nodeId}=                                  Wait For Image Node         ${imageId}
    # Try to open Ratings pane if closed
    Run Keyword And Ignore Error                Click Element  xpath=//h1[@data-name='ratings'][contains(@class, 'closed')]

    # Select second image
    Click Next Thumbnail
    ${imageId2}=                                Get Id From Selected Thumbnail
    ${nodeId2}=                                 Wait For Image Node         ${imageId2}

    # Select third image
    Click Next Thumbnail
    ${imageId3}=                                Get Id From Selected Thumbnail
    ${nodeId3}=                                 Wait For Image Node         ${imageId3}

    # Remove rating from ALL
    Click Node                                  ${nodeId}
    Meta Click Node                             ${nodeId2}
    Meta Click Node                             ${nodeId3}

    Wait Until Page Contains Element            xpath=//h1[@id='batch_ann_title']/span[contains(text(), '3 objects')]
    Wait Until Page Contains Element            xpath=//span[@id='ratingsAverage'][contains(text(), '3')]    timeout=${TIMEOUT}
    Remove Rating
    # Check rating was removed from first image
    Click Node                                  ${nodeId}
    Wait Until Element Is Visible               id=rating_annotations    timeout=${TIMEOUT}
    Check For Rating                            0

Test Comments

    Select Experimenter
    ${dsId_One}=                                Create Dataset      robot test comments_1
    ${dsId_Two}=                                Create Dataset      robot test comments_2

    # Comment a single Dataset
    Click Element                               xpath=//h1[@data-name='comments']
    Add Comment                                 ${commentText}

    # Refresh (select other Dataset and re-select)
    ${nodeId}=                                  Select Dataset By Id        ${dsId_One}
    Wait Until Right Panel Loads                Dataset                     ${dsId_One}
    Select Dataset By Id                        ${dsId_Two}
    # Check and add another Comment
    Check For Comment                           ${commentText}
    Add Comment                                 ${commentTextTwo}
    # Remove first comment
    Remove Comment                              ${commentText}

    # Now select both Datasets...
    Meta Click Node                             ${nodeId}
    Wait Until Page Contains Element            id=batch_ann_title
    # Previously added Comment will show up
    Check For Comment                           ${commentTextTwo}
    Page Should Not Contain Element             xpath=//div[@class='commentText'][contains(text(), '${commentText}')]
    # Add Comments to Both Datasets
    Add Comment                                 ${commentTextThree}
    Add Comment                                 ${commentTextFour}

    # Select each single Dataset to check for Comment(s)
    Select Dataset By Id                        ${dsId_One}
    Check Comment Gone                          ${commentTextTwo}
    Check For Comment                           ${commentTextThree}
    Select Dataset By Id                        ${dsId_Two}
    Check For Comment                           ${commentTextTwo}
    Check For Comment                           ${commentTextThree}

    # Select both Datasets and Remove Comments
    Meta Click Node                             ${nodeId}
    Wait Until Page Contains Element            id=batch_ann_title
    Remove Comment                              ${commentTextTwo}
    Remove Comment                              ${commentTextThree}

    # Select each single Dataset again to check for Comment(s)
    Select Dataset By Id                        ${dsId_One}
    Check For Comment                           ${commentTextFour}
    Check Comment Gone                          ${commentTextTwo}
    Check Comment Gone                          ${commentTextThree}
    Select Dataset By Id                        ${dsId_Two}
    Check For Comment                           ${commentTextFour}
    Check Comment Gone                          ${commentTextTwo}
    Check Comment Gone                          ${commentTextThree}

    Select Dataset By Id                        ${dsId_One}
    Delete Container

    Select Dataset By Id                        ${dsId_Two}
    Delete Container

Test File Annotations

    Select Experimenter
    ${sId_One}=                                 Create Screen       robot file annotations_1
    ${sId_Two}=                                 Create Screen       robot file annotations_2

    # Annotate single Screen
    Click Element                               xpath=//h1[@data-name='attachments']
    Add File Annotation                         ${fileName}

    # Refresh (select other Screen and re-select)
    ${nodeId}=                                  Select Screen By Id         ${sId_One}
    Wait Until Right Panel Loads                Screen                      ${sId_One}
    Select Screen By Id                         ${sId_Two}
    # Check and add another File Annotation
    Check For File Annotation                   ${fileName}
    Add File Annotation                         ${fileNameTwo}
    # Remove first File Annotation
    Remove File Annotation                      ${fileName}

    # Now select both Screens...
    Meta Click Node                             ${nodeId}
    Wait Until Page Contains Element            id=batch_ann_title
    # Previously added File Annotation will show up
    Check For File Annotation                   ${fileNameTwo}
    Check File Annotation Gone                  ${fileName}
    # Add File Annotation to Both Screens
    Add File Annotation                         ${fileName}
    Select Screen By Id                         ${sId_One}
    Check For File Annotation                   ${fileName}
    Select Screen By Id                         ${sId_Two}
    Check For File Annotation                   ${fileName}

    # Now select both Screens...
    Meta Click Node                             ${nodeId}
    Wait Until Page Contains Element            id=batch_ann_title
    # Remove first File Annotation
    Remove File Annotation                      ${fileName}
    Select Screen By Id                         ${sId_One}
    Check File Annotation Gone                  ${fileName}
    Select Screen By Id                         ${sId_Two}
    Check File Annotation Gone                  ${fileName}

    Delete Container
    Select Screen By Id                         ${sId_One}
    Delete Container

[Teardown]    Close Browser
