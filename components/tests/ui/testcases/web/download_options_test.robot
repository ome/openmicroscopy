*** Settings ***
Documentation     Tests menu options.

Resource          ../../resources/config.txt
Resource          ../../resources/web/login.txt
Resource          ../../resources/web/tree.txt

Suite Setup         User "${USERNAME}" logs in with password "${PASSWORD}"

*** Variables ***
# robot_setup script has created data with these parameters
${PLATE_NAME}               spwTests

*** Keywords ***

Assert No Download Button
    Element Should Not Be Visible   xpath=//button[contains(@class, 'btn_download')]

Show Download Menu
    Wait Until Element Is Visible   xpath=//button[contains(@class, 'btn_download')]
    Click Element                   xpath=//button[contains(@class, 'btn_download')]
    Wait Until Element Is Visible   xpath=//div[contains(@class, 'toolbar_dropdown')]

Download Option Available
    [Arguments]                     ${linkText}         ${expected}=${true}
    # We want the link to be available and not 'disabled'
    Run Keyword If  ${expected}     Element Should Be Visible       xpath=//div[contains(@class, 'toolbar_dropdown')]//li[not(contains(@class, 'disabled'))]/a[contains(text(), "${linkText}")]
    ...             ELSE            Element Should Not Be Visible   xpath=//div[contains(@class, 'toolbar_dropdown')]//li[not(contains(@class, 'disabled'))]/a[contains(text(), "${linkText}")]

Download Option Not Available
    [Arguments]                     ${linkText}
    Download Option Available       ${linkText}         ${false}


*** Test Cases ***

Test Download Menu

    # Go To                           ${WELCOME URL}
    Select Experimenter
    # Project and Dataset should have no Download button
    Select First Project With Children
    Assert No Download Button
    Select First Dataset With Children
    Assert No Download Button

    # Regular image
    Select First Image
    Show Download Menu
    Download Option Available       Download...
    Download Option Available       Download Original Metadata
    Download Option Available       Export as OME-TIFF...
    Download Option Available       Export as JPEG
    Download Option Available       Export as PNG
    Download Option Available       Export as TIFF

    # SPW Well
    Select First Plate With Name    ${PLATE_NAME}
    Select First Run
    Click Well By Name              A1
    Wait For General Panel          Well
    Show Download Menu
    Download Option Not Available   Download...
    Download Option Not Available   Download Original Metadata
    Download Option Available       Export as OME-TIFF...
    Download Option Available       Export as JPEG
    Download Option Available       Export as PNG
    Download Option Available       Export as TIFF

    # SPW Image
    Click Element                   xpath=//div[@id='wellImages']//li/a/div/img[1]
    Wait For General Panel          Image
    Show Download Menu
    Download Option Not Available   Download...
    Download Option Not Available   Download Original Metadata
    Download Option Available       Export as OME-TIFF...
    Download Option Available       Export as JPEG
    Download Option Available       Export as PNG
    Download Option Available       Export as TIFF

Test Download Big images

    Go To                           ${WELCOME URL}
    Select Experimenter
    Select And Expand Node          Big Images
    Select First Image
    Show Download Menu
    Download Option Available       Download...
    Download Option Available       Download Original Metadata
    Download Option Not Available   Export as OME-TIFF...
    Download Option Available       Export as JPEG
    Download Option Available       Export as PNG
    Download Option Available       Export as TIFF

[Teardown]    Close Browser