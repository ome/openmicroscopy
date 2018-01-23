*** Settings ***
Documentation     Tests searching for file annotations

Resource          ../../resources/web/annotation.txt
Resource          ../../resources/config.txt
Resource          ../../resources/web/login.txt
Resource          ../../resources/web/tree.txt

Suite Setup         User "${USERNAME}" logs in with password "${PASSWORD}"

*** Variables ***

${commentText}          Robot test adding this comment
${fileName}             robot_file_annotation2.txt
${SEARCH URL}           ${WELCOME URL}search/

*** Test Cases ***

Test Search Results File Annotations

    Select Experimenter
    ${sId_One}=                                 Create Screen       robot file annotations_1

    # Annotate single Screen
    Click Element                               xpath=//h1[@data-name='attachments']
    Add File Annotation                         ${fileName}

    Input Text  id=id_search_query              ${fileName}
    Submit Form
    Wait Until Keyword Succeeds     ${TIMEOUT}  ${INTERVAL}   Location Should Be    ${SEARCH URL}?search_query=${fileName}

    Wait Until Page Contains Element            xpath=//img[contains(@alt, 'screen')]   ${WAIT}
    Click Element                               xpath=//img[contains(@alt, 'screen')]
    Wait Until Page Contains Element            //*[@id="general_tab"]//th[contains(text(), 'Screen ID:')]

    #Minimal checking to check if you can add Annotations on the search result page
    Click Element                               xpath=//h1[@data-name='attachments']
    Check For File Annotation                   ${fileName}
    Remove File Annotation                      ${fileName}
    Add File Annotation                         ${fileName}
    Click Element                               xpath=//h1[@data-name='comments']
    Add Comment                                 ${commentText}

    Go To                                       ${WELCOME URL}
    Select Experimenter
    Select Screen By Id                         ${sId_One}
    Delete Container

[Teardown]    Close Browser