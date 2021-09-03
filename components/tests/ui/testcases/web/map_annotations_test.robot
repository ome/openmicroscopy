*** Settings ***
Documentation     Tests Map Annotations

Resource          ../../resources/config.txt
Resource          ../../resources/web/login.txt
Resource          ../../resources/web/tree.txt

Suite Setup         User "${USERNAME}" logs in with password "${PASSWORD}"

*** Test Cases ***

Test Map Annotation
    [Documentation]     Tests creation of Map Annotation on New Project

    ${projectId}=                       Create Project          TestMapAnnotation
    Wait Until Page Contains Element    css=div.mapAnnContainer
    Wait Until Page Contains Element    xpath=//h1[@data-name='keyvaluepairs']
    # Only click to expand if 'closed'. E.g. if mapr installed it could be expanded already
    Run Keyword And Ignore Error        Click Element         xpath=//h1[@data-name='keyvaluepairs'][contains(@class, 'closed')]
    # No rows selected. Toolbar should allow 'Insert' only.
    Page Should Contain Element       xpath=//ul[contains(@class, 'mapAnnToolbar')]//input[@title='Insert row'][contains(@class, 'button-disabled')]
    Page Should Contain Element       xpath=//ul[contains(@class, 'mapAnnToolbar')]//input[@title='Copy rows'][contains(@class, 'button-disabled')]
    Page Should Contain Element       xpath=//ul[contains(@class, 'mapAnnToolbar')]//input[@title='Paste rows'][contains(@class, 'button-disabled')]
    Page Should Contain Element       xpath=//ul[contains(@class, 'mapAnnToolbar')]//input[@title='Delete rows'][contains(@class, 'button-disabled')]

    # Map annotation should have a single row with 'Add Key', 'Add Value'
    Page Should Contain Element     //table[contains(@class, 'editableKeyValueTable')]/tbody/tr    limit=1
    Page Should Contain Element     xpath=//div[contains(@class, 'mapAnnContainer')]//td[contains(text(), 'Add Key')]
    Page Should Contain Element     xpath=//div[contains(@class, 'mapAnnContainer')]//td[contains(text(), 'Add Value')]

    # Before click...
    Page Should NotContain Element      xpath=//table[contains(@class, 'editableKeyValueTable')]/tbody/tr[contains(@class, 'ui-selected')]
    Page Should NotContain Element      xpath=//table[contains(@class, 'editableKeyValueTable')]/tbody/tr/td/input
    # Clicking the row should highlight it, clear placeholder text and start edit
    Click Element                       xpath=//table[contains(@class, 'editableKeyValueTable')]/tbody/tr/td[1]
    Page Should Contain Element             xpath=//table[contains(@class, 'editableKeyValueTable')]/tbody/tr[contains(@class, 'ui-selected')]
    Page Should Not Contain Element     xpath=//div[contains(@class, 'mapAnnContainer')]//td[contains(text(), 'Add Key')]
    Page Should Not Contain Element     xpath=//div[contains(@class, 'mapAnnContainer')]//td[contains(text(), 'Add Value')]
    Page Should Contain Element         xpath=//table[contains(@class, 'editableKeyValueTable')]/tbody/tr/td[1]/input

    # Enter some text, using 'Tab' to move between key/value fields
    ${key}=             Set Variable    RobotTestKey
    ${value}=           Set Variable    RobotTestValue
    Input Text          xpath=//table[contains(@class, 'editableKeyValueTable')]/tbody/tr/td[1]/input   ${key}
    Press Keys           xpath=//table[contains(@class, 'editableKeyValueTable')]/tbody/tr/td[1]/input   RETURN    # Enter key
    # Input should be re-created in the second td
    Page Should Not Contain Element     xpath=//table[contains(@class, 'editableKeyValueTable')]/tbody/tr/td[1]/input
    Page Should Contain Element         xpath=//table[contains(@class, 'editableKeyValueTable')]/tbody/tr/td[2]/input
    Input Text                          xpath=//table[contains(@class, 'editableKeyValueTable')]/tbody/tr/td[2]/input    ${value}
    Press Keys                           xpath=//table[contains(@class, 'editableKeyValueTable')]/tbody/tr/td[2]/input    RETURN
    # After value has been entered, we should have a second row
    Page Should Contain Element      //table[contains(@class, 'editableKeyValueTable')]/tbody/tr    limit=2

    # Refresh to check saved data
    Go To                               ${WELCOME URL}?show=project-${projectId}
    Wait Until Page Contains Element    css=div.mapAnnContainer
    # Only click to expand if 'closed'
    Run Keyword And Ignore Error        Click Element         xpath=//h1[@data-name='keyvaluepairs'][contains(@class, 'closed')]
    Wait Until Element Is Visible       xpath=//table[contains(@class, 'editableKeyValueTable')]//td[contains(text(), '${key}')]
    Wait Until Element Is Visible       xpath=//table[contains(@class, 'editableKeyValueTable')]//td[contains(text(), '${value}')]
    # Table shouldn't now have 'Add Key' or 'Add Value' placeholders
    Page Should Contain Element          //table[contains(@class, 'editableKeyValueTable')]/tbody/tr    limit=1
    Page Should Not Contain Element     xpath=//div[contains(@class, 'mapAnnContainer')]//td[contains(text(), 'Add Key')]
    Page Should Not Contain Element     xpath=//div[contains(@class, 'mapAnnContainer')]//td[contains(text(), 'Add Value')]

[Teardown]    Close Browser
