*** Settings ***
Documentation     Simple test that the History page loads and is functional

Resource          ../../resources/web/login.txt

Suite Setup         User "${USERNAME}" logs in with password "${PASSWORD}"

*** Variables ***

${HISTORY URL}           ${WELCOME URL}history/

*** Test Cases ***

Test History

    Go To                                   ${HISTORY URL}
    # Left panel should have at least one Day with some Items on it
    Wait Until Page Contains Element        xpath=//td[contains(@class, 'calendar_day')]//div[contains(@class, 'calendar_items')]/table
    # Click on Day that contains Items
    Click Element                           xpath=//td[contains(@class, 'calendar_day')]/a[descendant::div[contains(@class, 'calendar_items')]/table]

    # Wait for row(s) to load in central table - then click one to load right panel
    Wait Until Page Contains Element        xpath=//table[@id='dataTable']//td
    Click Element                           xpath=//table[@id='dataTable']//td
    Wait Until Page Contains Element        xpath=//div[contains(@class, 'data_heading')]

[Teardown]    Close Browser
