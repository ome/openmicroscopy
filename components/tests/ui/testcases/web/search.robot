*** Settings ***
Documentation     Tests Search on Web
...               https://docs.openmicroscopy.org/internal/testing_scenarios/Search.html

Resource          ../../resources/config.txt
Resource          ../../resources/web/login.txt
Resource          ../../resources/web/tree.txt

Library           DateTime

Suite Setup         User "${USERNAME}" logs in with password "${PASSWORD}"

*** Variables ***

${XpathImage}           xpath=//table[@id='dataTable']//img[@alt='image']
${XpathImageThumb}      xpath=//table[@id='dataTable']//img[@class='search_thumb']
${XpathDataset}         xpath=//table[@id='dataTable']//img[@alt='dataset']
${XpathDatasetThumb}    xpath=//table[@id='dataTable']//img[contains(@src,'folder_image16.png')]
${XpathProject}         xpath=//table[@id='dataTable']//img[@alt='project']
${XpathProjectThumb}    xpath=//table[@id='dataTable']//img[contains(@src,'folder16.png')]
${XpathPlate}           xpath=//table[@id='dataTable']//img[@alt='plate']
${XpathPlateThumb}      xpath=//table[@id='dataTable']//img[contains(@src,'folder_plate16.png')]
${XpathScreen}          xpath=//table[@id='dataTable']//img[@alt='screen']
${XpathScreenThumb}     xpath=//table[@id='dataTable']//img[contains(@src,'folder_screen16.png')]
${XpathWell}            xpath=//table[@id='dataTable']//img[@alt='well']
${XpathWellThumb}       xpath=//table[@id='dataTable']//img[contains(@src,'well16.png')]
${XpathTable}           xpath=//table[@id='dataTable']
${XpathTableColumn}     xpath=//table[@id='dataTable']//tr/td[position()=3]
${XPathAlertText}       xpath=//div[contains(@class,'ui-dialog')][contains(@style,'display: block')]//p
${RandomClick}          xpath=//form[@id="searching_form"]//label
${SearchInProgress}     xpath=//div[@id='content_details']//img[contains(@src,'webgateway/img/spinner.gif')]
${QueryError}           xpath=//div[@id='content_details']//div[contains(@class,'error')]
${NoResultsFound}       xpath=//div[@id='content_details']//p[contains(@class,'center_message message_nodata')]

${Search_Query}         1
${Search_Query1}        test
${Default_Srch_Group}   All Groups
${Default_Srch_Date}    Import date
${Table_Header}         Name
${ImplicitDELAY}        0.5
${DayStartTime}         00:00:00
${DayEndTime}           2014-07-18 00:00:00
${FromDate}             2014-01-15
${FromDateWithTime}     2014-01-15 00:00:00
${EndDate}              2014-07-17
${WrongStartDate}       2014-07-20
${AcquiredDateCol}      4
${NotAnImage}           3
${ITERATION}              0
${alertText}            Wildcard searches (*) must contain more than a single wildcard.
${FailureMessage}       Search should Not Contain an Alert Window.
${warning}              There was an error parsing your query. Colons ':' are reserved for searches of key-value annotations in the form: 'key:value'.
${SEARCH URL}           ${WELCOME URL}search/

*** Keywords ***

Unselect All Checkboxes

    Unselect Checkbox  name
    Unselect Checkbox  description
    Unselect Checkbox  show_annotations
    Unselect Checkbox  images
    Unselect Checkbox  datasets
    Unselect Checkbox  projects
    Unselect Checkbox  plates
    Unselect Checkbox  screens
    Unselect Checkbox  wells

Select All Checkboxes

    Select Checkbox  name
    Select Checkbox  description
    Select Checkbox  show_annotations
    Select Checkbox  images
    Select Checkbox  datasets
    Select Checkbox  projects
    Select Checkbox  plates
    Select Checkbox  screens
    Select Checkbox  wells

Omero Default Search

    Input Text  id=id_search_query  ${SEARCH_QUERY}
    Submit Form

For-Loop-In-Range-Acquired       [Arguments]         ${rows}
    FOR     ${INDEX}    IN RANGE    2    ${rows}
            ${RANDOM_STRING}   Get Table Cell    ${XpathTable}    ${INDEX}    3
            Run Keyword Unless    "${RANDOM_STRING}" == ""    Condition Check Acquired Date       ${RANDOM_STRING}
    END

For-Loop-In-Range-Imported       [Arguments]         ${rows}
    FOR     ${INDEX}    IN RANGE    2    ${rows}
            ${RANDOM_STRING}   Get Table Cell    ${XpathTable}    ${INDEX}    4
            Run Keyword Unless    "${RANDOM_STRING}" == ""  Condition Check Imported Date         ${RANDOM_STRING}
    END

Condition Check Imported Date
    [Arguments]             ${RANDOM_STRING}
    ${DATE}=    Get Current Date    result_format=timestamp
    ${time} =  Subtract Date From Date  ${RANDOM_STRING}  ${Date}
    Should Be True     ${time}<=0

Condition Check Acquired Date
    [Arguments]             ${RANDOM_STRING}
    ${DATE}=    Get Current Date    result_format=timestamp
    ${time} =    Subtract Date From Date    ${RANDOM_STRING}   ${Date}
    Should Be True     ${time}<=0

Clear Date Text Field
    Input Text  id=startdateinput   ${EMPTY}
    Input Text  id=enddateinput     ${EMPTY}
    sleep           ${ImplicitDELAY}
    Textfield Value Should be  id=startdateinput  ${EMPTY}
    Textfield Value Should be  id=enddateinput  ${EMPTY}

Clear Search Text Field
    Input Text  id=id_search_query  ${EMPTY}
    Input Text  name=query          ${EMPTY}
    Textfield Value Should be  id=id_search_query   ${EMPTY}
    Textfield Value Should be  name=query           ${EMPTY}

*** Test Cases ***

Test Default Search Check
    [Documentation]     login,search and Check that you are taken to the search ‘page’/UI with your search query entered into the search field.

    Omero Default Search
    Location Should Be                  ${SEARCH URL}?search_query=${SEARCH_QUERY}
    Page Should Contain Textfield       name=query
    Textfield Value Should be           name=query      ${SEARCH_QUERY}
    Wait Until Page Contains Element    id=dataTable    ${WAIT}


Test Check Search Results
    [Documentation]     Check if results are displayed on the centre pane

    Page Should Contain Element     id=dataTable
    Page Should Contain Element     id=center_panel
    Table Header Should Contain     ${XpathTable}  Type
    Table Header Should Contain     ${XpathTable}  Name
    Table Header Should Contain     ${XpathTable}  Imported
    Table Header Should Contain     ${XpathTable}  Group
    Table Header Should Contain     ${XpathTable}  Link
    Table Header Should Contain     ${XpathTable}  Acquired

    ${status}   Run Keyword And Return status       Page Should Contain Element     ${XpathScreen}
    Run Keyword If    '${status}' == 'True'         Page Should Contain Element     ${XpathScreenThumb}
    ${status1}   Run Keyword And Return status      Page Should Contain Element     ${XpathPlate}
    Run Keyword If    '${status1}' == 'True'        Page Should Contain Element     ${XpathPlateThumb}
    ${status2}   Run Keyword And Return status      Page Should Contain Element     ${XpathDataset}
    Run Keyword If    '${status2}' == 'True'        Page Should Contain Element     ${XpathDatasetThumb}
    ${status3}   Run Keyword And Return status      Page Should Contain Element     ${XpathProject}
    Run Keyword If    '${status3}' == 'True'        Page Should Contain Element     ${XpathProjectThumb}
    ${status4}   Run Keyword And Return status      Page Should Contain Element     ${XpathImage}
    Run Keyword If    '${status4}' == 'True'        Page Should Contain Element     ${XpathImageThumb}
    ${status5}   Run Keyword And Return status      Page Should Contain Element     ${XpathWell}
    Run Keyword If    '${status5}' == 'True'        Page Should Contain Element     ${XpathWellThumb}


Test Default Search Selections
    [Documentation]     Check that the objects searched for are Projects, Datasets, Images, Screens, Plates (all checkboxes selected by default).

    Checkbox Should Be Selected  images
    Checkbox Should Be Selected  datasets
    Checkbox Should Be Selected  projects
    Checkbox Should Be Selected  plates
    Checkbox Should Be Selected  screens
    Checkbox Should Be Selected  wells
    Checkbox Should Not Be Selected  name
    Checkbox Should Not Be Selected  description
    Checkbox Should Not Be Selected  show_annotations
    Click Button  id=search_button

Test Additional Search With Selections
    [Documentation]     Checkbox selections for name description and annotation and search again

    Select Checkbox  name
    Select Checkbox  description
    Select Checkbox  show_annotations
    Click Button  id=search_button

Test Check Search Date Field
    [Documentation]     Check that no date range has been chosen by default.

    Page Should Contain Textfield  id=startdateinput
    Textfield Value Should be  id=startdateinput  ${EMPTY}
    Page Should Contain Textfield  id=enddateinput
    Textfield Value Should be  id=enddateinput  ${EMPTY}

Test Check Search Default Group Selection
    [Documentation]     Check that the default Scope for the search is All Groups

    @{In group}  Get List Items  id=searchGroup
    ${expected_group}  Get Selected List Label  id=searchGroup
    Should Be Equal  ${expected_group}  ${Default_Srch_Group}

Test Check Search Default User Selection
    [Documentation]     Check : user matching the username you are logged in as, e.g user-4 user-4.

    @{DataOwned by}  Get List Items  id=searchByGroupMember--1
    ${expected_user}  Get Selected List Label  id=searchByGroupMember--1
    Should Be Equal  ${expected_user.strip()}  ${FULL NAME.strip()}

Test Check Search Default Date Selection
    [Documentation]     Check : Default selection for search should be using Import Date

    @{Default Date:}  Get List Items  id=useAcquisitionDate
    ${expected_date}  Get Selected List Label  id=useAcquisitionDate
    Should Be Equal  ${expected_date}  ${Default_Srch_Date}

Test Object Types and Fields

    # Select Image Alone
    Unselect All Checkboxes
    Select Checkbox  images
    Click Button  id=search_button
    sleep                               ${ImplicitDELAY}
    Page Should Contain Element         ${XpathImage}
    Page Should Not Contain Element     ${XpathDataset}
    Page Should Not Contain Element     ${XpathProject}
    Page Should Not Contain Element     ${XpathPlate}
    Page Should Not Contain Element     ${XpathScreen}
    Page Should Not Contain Element     ${XpathWell}

    # Select Dataset Alone
    Unselect All Checkboxes
    Select Checkbox  datasets
    Click Button  id=search_button
    sleep                               ${ImplicitDELAY}
    Page Should Contain Element         ${XpathDataset}
    Page Should Not Contain Element     ${XpathImage}
    Page Should Not Contain Element     ${XpathProject}
    Page Should Not Contain Element     ${XpathPlate}
    Page Should Not Contain Element     ${XpathScreen}
    Page Should Not Contain Element     ${XpathWell}

    # Select Project Alone
    Unselect All Checkboxes
    Select Checkbox  projects
    Click Button  id=search_button
    sleep                               ${ImplicitDELAY}
    Page Should Contain Element         ${XpathProject}
    Page Should Not Contain Element     ${XpathDataset}
    Page Should Not Contain Element     ${XpathImage}
    Page Should Not Contain Element     ${XpathPlate}
    Page Should Not Contain Element     ${XpathScreen}
    Page Should Not Contain Element     ${XpathWell}

    # Select Plate Alone
    Unselect All Checkboxes
    Select Checkbox  plates
    Click Button  id=search_button
    sleep                               ${ImplicitDELAY}
    Page Should Not Contain Element     ${XpathProject}
    Page Should Not Contain Element     ${XpathDataset}
    Page Should Not Contain Element     ${XpathImage}
    Page Should Not Contain Element     ${XpathWell}

    # Select Screen Alone
    Unselect All Checkboxes
    Select Checkbox  screens
    Click Button  id=search_button
    sleep                               ${ImplicitDELAY}
    Page Should Not Contain Element     ${XpathProject}
    Page Should Not Contain Element     ${XpathDataset}
    Page Should Not Contain Element     ${XpathImage}
    Page Should Not Contain Element     ${XpathWell}

    # Select Well Alone
    Unselect All Checkboxes
    Select Checkbox  wells
    Click Button  id=search_button
    sleep                               ${ImplicitDELAY}
    Page Should Contain Element         ${XpathWell}
    Page Should Not Contain Element     ${XpathProject}
    Page Should Not Contain Element     ${XpathDataset}
    Page Should Not Contain Element     ${XpathImage}


Test Date Range

    #Check with a From and End date
    Input Text  name=query          ${Search_Query}
    Input Text  id=startdateinput   ${FromDate}
    Input Text  id=enddateinput     ${EndDate}
    Select All Checkboxes
    sleep                           ${ImplicitDELAY}
    Click Button    id=search_button

    Page Should Not Contain         ${XpathTable}
    Page Should Contain Element     id=center_panel
    ${rows}=        Get Element Count    xpath=//table[@id='dataTable']/tbody/tr
    For-Loop-In-Range-Acquired      ${rows}+1
    For-Loop-In-Range-Imported      ${rows}+1

    #Check with a From date alone
    Clear Date Text Field
    Input Text  id=startdateinput   ${FromDate}
    Click Element   id=searching
    sleep                           ${ImplicitDELAY}
    Click Button  id=search_button
    Page Should Contain Element     id=center_panel
    ${rows}=        Get Element Count    xpath=//table[@id='dataTable']/tbody/tr
    ${cols}=        Get Element Count    xpath=//table[@id='dataTable']/tbody/tr/td
    For-Loop-In-Range-Acquired      ${rows}+1

    #Check with a End date alone
    Clear Date Text Field
    Input Text  id=enddateinput     ${EndDate}
    Click Element   id=searching
    sleep                           ${ImplicitDELAY}
    Click Button    id=search_button
    Click Dialog Button             OK

    #Check with a From date > End date
    Clear Date Text Field
    Input Text      id=startdateinput    ${WrongStartDate}
    Input Text      id=enddateinput      ${EndDate}
    sleep                            ${ImplicitDELAY}
    ${text1}         Get Text        id=startdateinput
    ${text2}         Get Text        id=enddateinput
    Should Be Equal As Strings           ${text1}    ${text2}
    Page Should Contain Element         id=dataTable

    #Check with just dates alone (No Search Query)
    Clear Search Text Field
    Input Text  id=startdateinput   ${FromDate}
    Input Text  id=enddateinput     ${EndDate}
    Click Element   id=searching
    sleep                           ${ImplicitDELAY}
    Select All Checkboxes
    Click Button    id=search_button
    Click Dialog Button             OK


Test Key Word Search

    #Search keyword *
    Clear Date Text Field
    Clear Search Text Field
    Input Text     name=query   *
    Click Element   ${RandomClick}
    sleep           ${ImplicitDELAY}
    Click Button    id=search_button
    Page Should Contain     ${alertText}
    Click Dialog Button             OK

    #Search keyword ?
    Clear Search Text Field
    Input Text     name=query   ?
    Click Button    id=search_button
    Page Should Not Contain Element     ${XpathTable}

    #Search keyword ****
    Clear Search Text Field
    Input Text     name=query   ****
    Click Button    id=search_button
    Page Should Not Contain Element     ${XpathTable}

    #Search 'test' with leading wildcard,trailing wildcard and leading&trailing wildcard and compare results
    Clear Search Text Field
    Input Text     name=query           ${Search_Query1}
    Click Button    id=search_button
    Wait Until Page Contains Element  id=dataTable  ${TIMEOUT}
    Page Should Contain Element         ${XpathTable}
    ${rows}=        Get Element Count   xpath=//table[@id='dataTable']/tbody/tr

    Clear Search Text Field
    Input Text     name=query           ${Search_Query1}*
    Click Button    id=search_button
    Wait Until Page Contains Element  id=dataTable  ${TIMEOUT}
    Page Should Contain Element         ${XpathTable}
    ${rows1}=        Get Element Count   xpath=//table[@id='dataTable']/tbody/tr

    Clear Search Text Field
    Input Text     name=query           *${Search_Query1}*
    Click Button    id=search_button
    Wait Until Page Contains Element    id=dataTable  ${TIMEOUT}
    Page Should Contain Element         ${XpathTable}
    ${rows2}=        Get Element Count    xpath=//table[@id='dataTable']/tbody/tr

    #Checkpoints for results obtained from the above search
    Should Be True                      ${rows1}>=${rows}
    Should Be True                      ${rows2}>=${rows1}
    Should Be True                      ${rows2}>=${rows1}

Test Keywords Warning

    # Search keyword *:
    Clear Search Text Field
    Input Text     name=query   *:
    Click Button    id=search_button
    Page Should Not Contain Element     ${warning}

    # Search keyword :
    Clear Search Text Field
    Input Text     name=query   :
    Click Button    id=search_button
    Page Should Not Contain Element     ${warning}

    # Search keyword :abc
    Clear Search Text Field
    Input Text     name=query   :abc
    Click Button    id=search_button
    Page Should Not Contain Element     ${warning}


[Teardown]    Close Browser
