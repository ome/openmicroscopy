*** Settings ***
Documentation     Tests submission of forms.

Resource          ../../resources/config.txt
Resource          ../../resources/web/login.txt
Resource          ../../resources/web/tree.txt

Suite Setup         User "${USERNAME}" logs in with password "${PASSWORD}"

*** Test Cases ***

Test Script Run
    [Documentation]     Tests form based on base_custom_dialog.html and script_ui.html

    # Clear any activities from earlier tests etc.
    Click Element                               id=launch_activities
    Click Element                               id=clear_activities
    ${imageId}=                                 Select And Expand Image

    # First Test 'custom' script forms
    # Tricky to handle popup window. Just go to script dialog URL instead
    Go To                                       ${WELCOME URL}figure_script/Thumbnail/?Image=${imageId}
    Wait Until Page Contains Element            id=script_form
    Submit Form                                 id=script_form
    Sleep                                       5                   # make sure script added to Activities
    Go To                                       ${WELCOME URL}
    Click Element                               id=launch_activities
    Wait Until Page Contains                    Thumbnail Figure

    # Now Test 'Batch Image Export' (script UI on-the-fly)
    Click Element                               id=scriptButton
    Wait Until Page Contains Element            xpath=//a[contains(text(),'export_scripts')]
    # If extra /omero/ script level, click() it. Will fail silently if not needed
    Execute Javascript                          $("#scriptList>ul>li>a:contains('omero')").click()
    Click Link                                  export_scripts
    ${script_url}=                              Get Element Attribute       xpath=//a[contains(text(),'Batch Image Export...')]    attribute=href
    Go To                                       ${script_url}?Image=${imageId}
    Wait Until Page Contains Element            id=script_form
    Submit Form                                 id=script_form
    Sleep                                       5                   # make sure script added to Activities
    Go To                                       ${WELCOME URL}
    Click Element                               id=launch_activities
    Wait Until Page Contains                    Batch Image Export

Test Channel Rename
    [Documentation]     Select User, Project, Dataset, Image and checks
    ...                 whether the toolbar/right-click menu options for
    ...                 creating various containers are enabled.

    Tree Should Be Visible
    ${imageId}=                                 Select And Expand Image
    Wait Until Page Contains Element            id=editChannelNames
    Click Element                               id=editChannelNames
    ${chName}=                                  Get Time    epoch
    Input Text                                  channel0    ch${chName}
    Submit Form                                 channel_names_edit
    Wait Until Keyword Succeeds                 ${TIMEOUT}  ${INTERVAL}   Element Should Not Be Visible     channel0
    Wait Until Page Contains Element            xpath=//div[@id='channel_names_display']/span[contains(text(), "ch${chName}")]      ${WAIT}

Test Annotate
    [Documentation]     Test Annotation of a Project that we create

    Go To                                       ${WELCOME URL}
    Select Experimenter
    ${pid}=                                     Create Project      robot test annotate

    # Comment Form
    Click Element                               xpath=//h1[@data-name='comments']
    Input Text                                  comment     test add comment
    Submit Form                                 add_comment_form
    Wait Until Page Contains                    test add comment

    # Files
    Click Element                               xpath=//h1[@data-name='attachments']
    Click Element                               choose_file_anns
    Wait Until Page Contains Element            id_files
    Click Element                               xpath=//select[@id='id_files']/option    # just pick first file
    Submit Form                                 choose_attachments_form
    Wait Until Page Contains Element            xpath=//li[@class='file_ann_wrapper']   # check for any file annotation

Test Batch Annotate
    [Documentation]     Test Batch Annotation of 2 Projects that we create

    Go To                                       ${WELCOME URL}
    Select Experimenter
    ${projectId}=                               Create Project      robot test batch annotate
    ${pId}=                                     Create Project      robot test batch annotate
    Go To                                       ${WELCOME URL}?show=project-${projectId}|project-${pId}
    Wait Until Page Contains Element            id=batch_ann_title

    # Comment Form
    Click Element                               xpath=//*[@id="metadata_general"]//div/h1[contains(text(), 'Comments')]
    Wait Until Element Is Visible               id=id_comment
    Input Text                                  comment     test add comment
    Submit Form                                 add_comment_form
    Wait Until Page Contains                    test add comment

    # Files
    Click Element                               xpath=//*[@id="metadata_general"]//div/h1[contains(text(), 'Attachments')]
    Wait Until Element Is Visible               id=choose_file_anns
    Click Element                               choose_file_anns
    Wait Until Page Contains Element            id_files
    Click Element                               xpath=//select[@id='id_files']/option    # just pick first file
    Submit Form                                 choose_attachments_form
    Wait Until Page Contains Element            xpath=//li[@class='file_ann_wrapper']   # check for any file annotation

Test Search
    [Documentation]     Test basic search submission from header field or search page
    ...                 Searching e.g. for the Projects we just created above

    Go To                                   ${WELCOME URL}
    Input Text                              id_search_query     test
    Submit Form                             id=search

    Wait Until Page Contains Element        dataTable       ${WAIT}
    # We don't care about results, just check we get *some* results in dataTable
    Location Should Be                      ${WELCOME URL}search/?search_query=test
    # Repeat search
    Submit Form                             searching_form
    Wait Until Page Contains                Loading data...
    Wait Until Page Contains Element        dataTable       ${WAIT}
    ${rows}=    Get Matching XPath Count    xpath=//table[@id="DataTable"]/tr
    Wait Until Page Contains Element        xpath=//table[@id="dataTable"][@data-result-count!='${rows}']       ${WAIT}

[Teardown]    Close Browser
