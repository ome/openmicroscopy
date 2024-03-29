*** Settings ***
Documentation     Tests managing data e.g. edit/chgrp.

Resource          ../../resources/config.txt
Resource          ../../resources/web/login.txt
Resource          ../../resources/web/tree.txt

Suite Setup         User "${USERNAME}" logs in with password "${PASSWORD}"

*** Test Cases ***

Test Edit Project
    [Documentation]     Create a Project and edit its name and description

    Wait Until Keyword Succeeds             ${TIMEOUT}    ${INTERVAL}     Reload Page
    Select Experimenter
    ${pid}=                                 Create project      robot test edit
    Wait Until Page Contains Element        projectname-${pid}
    # Edit Name
    Click Element                           css=#projectname-${pid} button.btn_edit
    Wait Until Page Contains Element        form-projectname-${pid}
    # id 'id_name' is not unique!
    Input Text                              xpath=//form[@id='form-projectname-${pid}']//input[@id='id_name']  editedName
    Submit Form                             form-projectname-${pid}
    Wait Until Page Contains Element        xpath=//span[@id='projectname-${pid}-name'][contains(text(), 'editedName')]

    # Edit Description
    Click Element                           css=#projectdescription-${pid} button.btn_edit
    Wait Until Page Contains Element        form-projectdescription-${pid}
    Input Text                              xpath=//form[@id='form-projectdescription-${pid}']//textarea[@id='id_description']  newDesc
    Submit Form                             form-projectdescription-${pid}
    Wait Until Page Contains Element        xpath=//span[@id='projectdescription-${pid}-description'][contains(text(), 'newDesc')]

Test Chgrp
    [Documentation]     Tests chgrp of a Project to any other group

    Wait Until Keyword Succeeds             ${TIMEOUT}    ${INTERVAL}     Reload Page
    # Clear any activities from earlier tests etc.
    Click Element                           id=launch_activities
    Click Element                           id=clear_activities
    Select Experimenter
    ${pid}=                                 Create project      robot test chgrp
    Wait Until Keyword Succeeds             ${TIMEOUT}    ${INTERVAL}     Click Element                           refreshButton
    ${nodeId}=                              Select Project By Id            ${pid}
    Open Context Menu                       xpath=//li[@id='${nodeId}']/span
    Click Element                           xpath=//ul[contains(@class, 'jstree-contextmenu')]//a[contains(text(), 'Move to Group...')]
    Wait Until Element Is Visible           id=group_chooser
    Wait Until Element Is Visible           xpath=//div[@id='group_chooser']/div[contains(@class, 'chgrpGroup')]
    # Simply click first target group option
    Click Element                           xpath=//div[@id='group_chooser']/div[contains(@class, 'chgrpGroup')]
    Submit Form                             id=chgrp-form
    Click Element                           id=launch_activities
    Wait Until Page Contains                Project moved to Group              60

[Teardown]    Close Browser
