*** Settings ***

Documentation       A test suite with a single test for valid login.
...
...                 This test has a workflow that is created using keywords in
...                 the imported resource file.

Resource            ../../resources/web/webadmin.txt
Resource            ../../resources/web/tree.txt
Resource            ../../resources/config.txt

Suite Setup         Open Browser To Webadmin And Log In As Root

*** Test Cases ***

Check Group Form
    [Documentation]    Tests elements on the create group page

    Page Should Be Open                 ${USERS URL}
    Click Link                          link=Groups
    Click Link                          link=Add new Group
    
    Page Should Contain                 Add group
    Page Should Contain                 Fields marked in red are mandatory.
    
    Page Should Contain Input Field     Name            name
    Page Should Contain Input Field     Description     description
    
    Page Should Contain Choice Field    Owners          Type owner names to add...
    Page Should Contain Choice Field    Members         Type member names to add...
    
    Page Should Contain Radio Field     Permissions     permissions                     0
    
    Page Should Contain                 Private
    Page Should Contain                 Read-Only
    Page Should Contain                 Read-Annotate
    
    Page Should Contain Button          Save
    
    Page Should Contain Link            OMERO Permissions


Check User Form
    [Documentation]    Tests elements on the create user page
    
    Go To                                   ${USERS URL}
    Page Should Be Open                     ${USERS URL}
    Click Link                              link=Add new User
    
    Page Should Contain                     New User
    Page Should Contain                     Fields marked in red are mandatory.
    
    Page Should Contain Input Field         Username        omename
    Page Should Contain Password Field      Password        password
    Page Should Contain Password Field      Confirmation    confirmation
    Page Should Contain Input Field         First name      first_name
    Page Should Contain Input Field         Middle name     middle_name
    Page Should Contain Input Field         Last name       last_name
    Page Should Contain Input Field         Email           email
    Page Should Contain Input Field         Institution     institution
    
    Page Should Contain Radio Button        xpath=//input[@type="radio" and @value="user"]
    Page Should Contain Radio Button        xpath=//input[@type="radio" and @value="administrator"]
    Page Should Contain Radio Button        xpath=//input[@type="radio" and @value="restricted_administrator"]
    
    Page Should Contain Checkbox Field      Active          active          selected=${True}
    
    Page Should Contain Choice Field        Group           Type group names to add...
    
    Page Should Contain Button              Save


Create Edit Group
    [Documentation]     Tests group creation
    
    Go To                   ${USERS URL}
    Page Should Be Open     ${USERS URL}
    Click Link              link=Groups
    Click Link              link=Add new Group
    
    ${group_name}           Unique name     test_group
    
    Input Text              name            ${group_name}
    Input Text              description     This is test group
    Select Radio Button     permissions     1
    
    Click Button            Save

    Location Should Be      ${GROUPS URL}
    Page Should Contain     ${group_name}
    # find row which contains group name, and click 'btn_edit' of that row
    Click Element           xpath=//table[@id="groupTable"]/tbody/tr[descendant::td[contains(text(), '${group_name}')]]//a[contains(@class, "btn_edit")]
    Wait Until Page Contains Element    xpath=//input[@value='${group_name}']       ${WAIT}
    Input Text                          name    ${group_name}-Edited
    Click Button                        Save
    Location Should Be                  ${GROUPS URL}
    Wait Until Page Contains            ${group_name}-Edited
    

Create Edit User
    Go To                                   ${USERS URL}
    Click Link                              link=Add new User
    
    ${user_name}            Unique name     test_user
    Input Text              omename         ${user_name}
    Input Text              password        ${user_name}
    Input Text              confirmation    ${user_name}
    Input Text              first_name      ${user_name}
    Input Text              middle_name     ${user_name}
    Input Text              last_name       ${user_name}
    Input Text              institution     ${user_name}
    
    
    Click Element           xpath=//div[@id='id_other_groups_chosen']/ul[@class='chosen-choices']
    Page Should Contain Element             xpath=//div[@id='id_other_groups_chosen']/div[@class='chosen-drop']/ul[@class='chosen-results']
    Click Element           xpath=//div[@id='id_other_groups_chosen']/div[@class='chosen-drop']/ul[@class='chosen-results']/li[contains(text(),'test_group')]

    # Add to 'system' group - Role should be 'administrator'
    Click Element           xpath=//div[@id='id_other_groups_chosen']/ul[@class='chosen-choices']
    Click Element           xpath=//div[@id='id_other_groups_chosen']/div[@class='chosen-drop']/ul[@class='chosen-results']/li[contains(text(),'system')]
    Radio Button Should Be Set To       role        administrator

    # Editing 'Role' should update 'system' group in "Groups" selection
    Select Radio Button     role        user
    Page Should Not Contain Element     xpath=//ul[@class='chosen-choices']//span[contains(text(),'system')]
    Select Radio Button     role        administrator
    Wait Until Page Contains Element    xpath=//ul[@class='chosen-choices']//span[contains(text(),'system')]
    Select Radio Button     role        user
    Page Should Not Contain Element     xpath=//ul[@class='chosen-choices']//span[contains(text(),'system')]

    Click Button            Save
    Location Should Be      ${USERS URL}
    Page Should Contain     ${user_name}
    # find row which contains user name, and click 'btn_edit' of that row
    Click Element           xpath=//table[@id="experimenterTable"]/tbody/tr[descendant::td[contains(text(), '${user_name}')]]//a[contains(@class, "btn_edit")]

    Wait Until Page Contains Element    id=id_first_name    ${WAIT}
    ${createdName}=                     Get Element Attribute   xpath=//input[@id='id_first_name']    attribute=value
    Should Be Equal                     "${user_name}"    "${createdName}"

    # Role should be saved as User
    Radio Button Should Be Set To       role        user

    # Edit Password
    Click Element           id=change_password
    Input Text              id_old_password     ${ROOT PASSWORD}
    Input Text              id_password         ${user_name}new
    Input Text              id_confirmation     ${user_name}new
    Click Dialog Button       OK
    Wait Until Page Contains  Password reset OK

    Input Text                  first_name    ${user_name}-Edited
    Click Button                Save
    Location Should Be          ${USERS URL}
    Wait Until Page Contains    ${user_name}-Edited

Check Admin Edit Self

    Go To                             ${USERS URL}
    Click Element                     xpath=//table[@id="experimenterTable"]/tbody/tr[descendant::td[contains(text(), '${ROOT FULL NAME}')]]//a[contains(@class, "btn_edit")] 

    Wait Until Page Contains Element  xpath=//input[@type="radio" and @value="administrator"]       ${WAIT}
    Radio Button Should Be Set To     role  administrator
    
    Click Element                     xpath=//div[@id='id_other_groups_chosen']/ul[@class='chosen-choices']
    Page Should Contain Element       xpath=//div[@id='id_other_groups_chosen']/div[@class='chosen-drop']/ul[@class='chosen-results']
    Click Element                     xpath=//div[@id='id_other_groups_chosen']/div[@class='chosen-drop']/ul[@class='chosen-results']/li[contains(text(),'test_group')]

    Element Should Not Be Visible     xpath=//div[@id="id_other_groups_chosen"]//li[descendant::span[contains(text(), "system")]]/a
    Element Should Be Visible         xpath=//div[@id="id_other_groups_chosen"]//li[descendant::span[contains(text(), "test_group")]]/a

[Teardown]    Close Browser
