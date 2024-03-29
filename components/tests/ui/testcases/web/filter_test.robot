*** Settings ***
Documentation     Tests submission of forms.

Resource          ../../resources/web/annotation.txt
Resource          ../../resources/config.txt
Resource          ../../resources/web/login.txt
Resource          ../../resources/web/tree.txt

Suite Setup         User "${USERNAME}" logs in with password "${PASSWORD}"

*** Variables ***

${imgName1}=                                    FilterImageOne
${imgName2}=                                    FilterImageTwo
${imgNameStart}=                                Filter

*** Keywords ***

Filter Tag
    [Arguments]                                 ${tagText}
    Select From List By Label                   id=filter_by_tag        ${tagText}

Remove Tag Filter
    [Arguments]                                 ${tagText}
    Mouse Over                                  xpath=//div[@id='currentFilterTags']//a[contains(text(), '${tagText}')]
    Click Element                               xpath=//div[@id='currentFilterTags']/div[descendant::a[contains(text(), '${tagText}')]]/span[contains(@class, 'removefilter')]

Filter Name
    [Arguments]                                 ${nameText}
    Input Text                                  xpath=//div[@id='filtername']/input         ${nameText}

Check Selected Count
    [Arguments]                                 ${selectedCount}
    Page Should Contain Element                 xpath=//span[contains(@class, 'jstree-clicked')]           limit=${selectedCount}

*** Test Cases ***

Test Filter By Tags
    ${tag1}=                                    Evaluate    "FilterTag1_%s" % random.randint(0, 1000)     modules=random
    ${tag2}=                                    Evaluate    "FilterTag2_%s" % random.randint(0, 1000)     modules=random
    ${tag3}=                                    Evaluate    "FilterTag3_%s" % random.randint(0, 1000)     modules=random

    Select Experimenter
    Select And Expand Node                      Project 1
    Select And Expand Node                      Dataset 1

    # Select first image - Add Tag1
    ${imageId1}=                                Select First Image
    ${nodeId1}=                                 Wait For Image Node         ${imageId1}
    Click Element                               xpath=//h1[@data-name='tags']
    Add New Tag                                 ${tag1}

    # Select second image - Add Tag2
    Click Next Thumbnail
    ${imageId2}=                                Get Id From Selected Thumbnail
    ${nodeId2}=                                 Wait For Image Node         ${imageId2}
    Add New Tag                                 ${tag2}

    # Select 2nd and 3rd images - Add Tag3
    Click Next Thumbnail
    ${imageId3}=                                Get Id From Selected Thumbnail
    ${nodeId3}=                                 Wait For Image Node         ${imageId3}
    Meta Click Node                             ${nodeId2}
    Wait Until Page Contains Element            xpath=//h1[@id='batch_ann_title']/span[contains(text(), '2 objects')]
    Add New Tag                                 ${tag3}

    # Filter by Tag3 -> 2 images
    Select From List By Label                   id=choosefilter         Tag
    Filter Tag                                  ${tag3}
    # Only 2 thumbnails should remain Visible and Selected
    Wait Until Element Is Not Visible           id=image_icon-${imageId1}
    Count Thumbs Without Class                  tagFilter_hidden        2
    Check Selected Count                        2

    # Add Filter by Tag2 -> 1 image
    Filter Tag                                  ${tag2}
    Count Thumbs Without Class                  tagFilter_hidden        1
    Check Selected Count                        1

    # Remove filter Tag3 (Tag2 remains)
    Remove Tag Filter                           ${tag3}
    Count Thumbs Without Class                  tagFilter_hidden        1

    # Remove filter Tag2
    Remove Tag Filter                           ${tag2}
    Count Thumbs With Class                     tagFilter_hidden        0
    Wait Until Element Is Visible               id=image_icon-${imageId1}
    Check Selected Count                        1

    # Filter by Tag1 -> 1 images, none selected
    Filter Tag                                  ${tag1}
    Count Thumbs Without Class                  tagFilter_hidden        1
    Check Selected Count                        0
    Remove Tag Filter                           ${tag1}

    # Clean up - Select 3 Images and remove Tags
    Click Node                                  ${nodeId3}
    Meta Click Node                             ${nodeId2}
    Meta Click Node                             ${nodeId1}
    # Tag1 is only on Node1. We select Node1 last, so tag1 doesn't hide/show (stale)
    Remove Tag                                  ${tag1}
    Remove Tag                                  ${tag2}
    Remove Tag                                  ${tag3}



Test Filter By Name Tags Rating

    ${tagText}=                                 Evaluate    "FilterName%s" % random.randint(0, 1000)     modules=random

    Reload Page
    Select Experimenter
    Select And Expand Node                      Project 1
    Select And Expand Node                      Dataset 1

    # Select first image - Rename it
    ${imageId1}=                                Select First Image
    ${nodeId1}=                                 Wait For Image Node         ${imageId1}
    Edit Object Name                            image   ${imageId1}         ${imgName1}

    # Select second image - Rename
    Click Next Thumbnail
    ${imageId2}=                                Get Id From Selected Thumbnail
    ${nodeId2}=                                 Wait For Image Node         ${imageId2}
    Edit Object Name                            image   ${imageId2}         ${imgName2}

    # Select 2nd and 3rd images - Add Tag
    Click Next Thumbnail
    ${imageId3}=                                Get Id From Selected Thumbnail
    ${nodeId3}=                                 Wait For Image Node         ${imageId3}
    Click Element                               xpath=//h1[@data-name='tags']
    Meta Click Node                             ${nodeId2}
    Wait Until Page Contains Element            xpath=//h1[@id='batch_ann_title']/span[contains(text(), '2 objects')]
    Add New Tag                                 ${tagText}

    # Filter by Tag
    Select From List By Label                   id=choosefilter         Tag
    Filter Tag                                  ${tagText}
    # Only images 2 and 3 visible
    Wait Until Element Is Not Visible           id=image_icon-${imageId1}
    Element Should Be Visible                   id=image_icon-${imageId2}
    Element Should Be Visible                   id=image_icon-${imageId3}

    # Filter by partial Name -> 1 images
    Select From List By Label                   id=choosefilter         Name
    Filter Name                                 ${imgNameStart}
    Wait Until Element Is Not Visible           id=image_icon-${imageId3}
    Element Should Be Visible                   id=image_icon-${imageId2}
    Check Selected Count                        1

    # Filter by Partial Name only
    Remove Tag Filter                           ${tagText}
    Element Should Be Visible                   id=image_icon-${imageId1}
    Element Should Be Visible                   id=image_icon-${imageId2}
    Element Should Not Be Visible               id=image_icon-${imageId3}

    # Filter by Full Name
    Filter Name                                 ${imgName2}
    Wait Until Element Is Not Visible           id=image_icon-${imageId1}
    Element Should Be Visible                   id=image_icon-${imageId2}

    # Clean up - Select 3 Images and remove Tags
    Click Node                                  ${nodeId3}
    Meta Click Node                             ${nodeId2}
    Meta Click Node                             ${nodeId1}
    # Tag is only on Node1. We select Node1 last, so tag doesn't hide/show (stale)
    Remove Tag                                  ${tagText}

[Teardown]    Close Browser
