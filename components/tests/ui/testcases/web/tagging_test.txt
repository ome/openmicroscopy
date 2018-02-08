*** Settings ***
Documentation     Tests submission of forms.

Resource          ../../resources/config.txt
Resource          ../../resources/web/login.txt
Resource          ../../resources/web/tree.txt
Resource          ../../resources/web/annotation.txt

Suite Setup         User "${USERNAME}" logs in with password "${PASSWORD}"

*** Keywords ***

Check For Tag
    [Arguments]                             ${tagText}
    Wait Until Page Contains Element        xpath=//div[@class='tag']/a[contains(text(), '${tagText}')]  ${WAIT}

Check No Tag
    [Arguments]                             ${tagText}
    Wait Until Keyword Succeeds             ${TIMEOUT}     ${INTERVAL}     Page Should Not Contain Element         xpath=//div[@class='tag']/a[contains(text(), '${tagText}')]


*** Test Cases ***

Test Tag Dialog
    [Documentation]     Several tests of the Tag dialog for single or batch tagging.

    Go To                                       ${WELCOME URL}
    Select Experimenter
    ${projectId}=                               Create Project      robot test tagging_1
    ${pId}=                                     Create Project      robot test tagging_2
    Check No Tag                                robotTagTest${pid}TagOne
    Check No Tag                                robotTagTest${pid}TagTwo

    # Tag Project robot test tagging_2
    Click Element                               xpath=//h1[@data-name='tags']
    Add New Tag                                 robotTagTest${pid}TagOne

    # Check and add another Tag, remove first one
    Check For Tag                               robotTagTest${pid}TagOne
    Launch Tag Dialog
    # Create a second Tag
    Input Text                                  id_tag     robotTagTest${pid}TagTwo
    Click Element                               id_add_new_tag
    # Remove previously added Tag
    Click Element                               xpath=//div[@id='id_selected_tags']/div[contains(text(),'robotTagTest${pid}TagOne')]
    Click Element                               id=id_tag_deselect_button
    Page Should Not Contain Element             xpath=//div[@id='id_selected_tags']/div[contains(text(),'robotTagTest${pid}TagOne')]
    # Save - check Tag added and Tag removed
    Click Dialog Button                         Save
    # Refresh (select other Project and re-select)
    ${nodeId}=                                  Select Project By Id     ${pid}
    Wait Until Right Panel Loads                Project                  ${pid}
    Check For Tag                               robotTagTest${pid}TagTwo
    Check No Tag                                robotTagTest${pid}TagOne


    # Now select both Projects...
    Select Project By Id                        ${projectId}
    Meta Click Node                             ${nodeId}
    Wait Until Page Contains Element            id=batch_ann_title
    # Previously added Tag will show up
    Check For Tag                               robotTagTest${pid}TagTwo
    Wait Until Element Is Visible               launch_tags_form
    Launch Tag Dialog

    # Tags created above should be available to add to second Project
    Wait Until Page Contains Element            xpath=//div[@id='id_all_tags']/div[contains(text(),'robotTagTest${pid}TagOne')]
    Page Should Contain Element                 xpath=//div[@id='id_all_tags']/div[contains(text(),'robotTagTest${pid}TagTwo')]
    # And shouldn't appear in right column
    Page Should Not Contain Element             xpath=//div[@id='id_selected_tags']/div[contains(text(),'robotTagTest${pid}TagOne')]
    Page Should Not Contain Element             xpath=//div[@id='id_selected_tags']/div[contains(text(),'robotTagTest${pid}TagTwo')]
    # Add the first Tag to both Projects
    Click Element                               xpath=//div[@id='id_all_tags']/div[contains(text(),'robotTagTest${pid}TagOne')]
    Click Element                               id=id_tag_select_button
    # Just 1 tag should be moved to right
    Wait Until Page Contains Element            xpath=//div[@id='id_selected_tags']/div[contains(text(),'robotTagTest${pid}TagOne')]
    Page Should Not Contain Element             xpath=//div[@id='id_selected_tags']/div[contains(text(),'robotTagTest${pid}TagTwo')]
    # Save - check Tag added and Tag removed
    Click Dialog Button                         Save
    # Right panel Tag tooltip should indicate one tag is on BOTH Projects 'Can remove Tag from <b>2 objects</b>'
    Wait Until Page Contains Element            xpath=//span[@class='tooltip_html']/b[contains(text(),'2 objects')]
    # Other tag is still on one Project
    Page Should Contain Element                 xpath=//span[@class='tooltip_html']/b[contains(text(),'1 object')]
    Check For Tag                               robotTagTest${pid}TagOne
    Check For Tag                               robotTagTest${pid}TagTwo

    # Open Tag dialog again...
    Launch Tag Dialog
    # Same tag as before should be on right
    Wait Until Page Contains Element            xpath=//div[@id='id_selected_tags']/div[contains(text(),'robotTagTest${pid}TagOne')]
    Page Should Not Contain Element             xpath=//div[@id='id_selected_tags']/div[contains(text(),'robotTagTest${pid}TagTwo')]
    # Remove this Tag form Both projects...
    Click Element                               xpath=//div[@id='id_selected_tags']/div[contains(text(),'robotTagTest${pid}TagOne')]
    Click Element                               id=id_tag_deselect_button
    # Create a third Tag
    Input Text                                  id_tag     robotTagTest${pid}TagThree
    Click Element                               id_add_new_tag
    Click Dialog Button                         Save
    # After Save, Tags Two and Three should be added, Tag One removed
    # Seems we need a proper refresh here to be sure that TagOne is removed.
    Go To                                       ${WELCOME URL}?show=project-${projectId}|project-${pId}
    Wait Until Element Is Visible               xpath=//h1[@data-name='tags']
    Click Element                               xpath=//h1[@data-name='tags']
    Check For Tag                               robotTagTest${pid}TagThree
    Check For Tag                               robotTagTest${pid}TagTwo
    Check No Tag                                robotTagTest${pid}TagOne

    # Delete the projects
    Select Project By Id                        ${projectId}
    Meta Click Node                             ${nodeId}
    Click Element                               id=deleteButton
    Wait Until Element Is Visible               id=delete-dialog-form
    Select Checkbox                             id=delete_anns
    Click Dialog Button                         Yes


Test Tag Removal
    [Documentation]     Tests the removal of tags by clicking on the Tag (-) button

    Go To                                       ${WELCOME URL}
    Select Experimenter
    ${projectId}=                               Create Project      robot test tag remove_1
    ${pId}=                                     Create Project      robot test tag remove_2
    # Tag a single Project
    Click Element                               xpath=//h1[@data-name='tags']
    Launch Tag Dialog
    Input Text                                  id_tag     removeTest${pid}One
    Click Element                               id_add_new_tag
    Click Dialog Button                         Save

    Check For Tag                               removeTest${pid}One
    # Click (-) button on Tag to remove
    Remove Tag                                  removeTest${pid}One
    Check No Tag                                removeTest${pid}One

    # Batch Tag 2 Projects
    ${pNodeId}=                                 Select Project By Id    ${pId}
    ${projectNodeId}=                           Select Project By Id    ${projectId}
    Meta Click Node                             ${pNodeId}
    Wait Until Page Contains Element            id=batch_ann_title
    Launch Tag Dialog
    # Add 2 Tags to both Projects...
    Input Text                                  id_tag     removeTest${pid}Two
    Click Element                               id_add_new_tag
    Input Text                                  id_tag     removeTest${pid}Three
    Click Element                               id_add_new_tag
    Click Dialog Button                         Save
    Check For Tag                               removeTest${pid}Two
    # Click (-) button on Tag to remove
    Remove Tag                                  removeTest${pid}Two
    Check No Tag                                removeTest${pid}Two

    # Select each Project in turn, check tags One and Two are gone (but Three remains)
    Select Project By Id                        ${projectId}
    Wait Until Page Contains                    robot test tag remove_1
    Check For Tag                               removeTest${pid}Three
    Check No Tag                                removeTest${pid}One
    Check No Tag                                removeTest${pid}Two
    Select Project By Id                        ${pId}
    Wait Until Page Contains                    robot test tag remove_2
    Check For Tag                               removeTest${pid}Three
    Check No Tag                                removeTest${pid}One
    Check No Tag                                removeTest${pid}Two

    # Delete the projects
    Select Project By Id                        ${pId}
    Meta Click Node                             ${projectNodeId}
    Click Element                               id=deleteButton
    Wait Until Element Is Visible               id=delete-dialog-form
    Select Checkbox                             id=delete_anns
    Click Dialog Button                         Yes

[Teardown]    Close Browser
