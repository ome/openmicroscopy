*** Settings ***

Documentation       A test suite with a single test for valid login.
...
...                 This test has a workflow that is created using keywords in
...                 the imported resource file.

Resource            ../../resources/web/webadmin.txt
Resource            ../../resources/config.txt

Suite Setup         Open Browser To Login Page      ${WEBADMIN WELCOME URL}     ${WEBADMIN LOGIN URL}

*** Test Cases ***

Login Page
    [Documentation]    Tests elements on the login page
    
    Page Should Contain Image       xpath=//div[contains(@class, 'login-logos')]/img
    Page Should Contain Element     id_server
    Page Should Contain Element     id_username
    Page Should Contain Element     id_password
    Page Should Contain Button      xpath=//input[@value='Login']

Valid Login
    [Documentation]    Tests logging in as root
    
    Log In As Root
    Page Should Be Open     ${USERS URL}                    OMERO Users
    Log Out
    
    Go To                   ${WEBADMIN WELCOME URL}
    Location Should Be      ${WEBADMIN LOGIN URL}           # redirect confirms logout
    Log In As               ${ROOT USERNAME}                ${ROOT PASSWORD}    ${SERVER_ID}
    Page Should Be Open     ${USERS URL}                    OMERO Users
    Log Out

Invalid Login
    [Documentation]    Tests login form validation messages on logging in as root
    
    Go To                   ${WEBADMIN WELCOME URL}
    Log In As               foo                             secret              ${SERVER_ID}
    Page Should Be Open     ${WEBADMIN LOGIN URL}           OMERO.web - Login
    Page Should Contain     Error: Connection not available, please check your credentials and version compatibility.
    
    Go To                   ${WEBADMIN WELCOME URL}

    # Browsers now prevent 'submit' if 'required' fields are empty
    # Log In As               foo                             ${EMPTY}            ${SERVER_ID}
    # Page Should Be Open     ${WEBADMIN LOGIN URL}           OMERO.web - Login
    # Page Should Contain     This field is required.
    
    # Log In As               ${EMPTY}                        secret              ${SERVER_ID}
    # Page Should Be Open     ${WEBADMIN LOGIN URL}           OMERO.web - Login
    # Page Should Contain     This field is required.

Guest Login

    Go To                   ${LOGIN URL}
    # Check that 'g' is not mistaken for 'guest' https://github.com/openmicroscopy/openmicroscopy/pull/4686
    Log In As               g                               secret                  ${SERVER_ID}
    Page Should Be Open     ${LOGIN URL}                    OMERO.web - Login
    Page Should Contain     Error: Connection not available, please check your credentials and version compatibility.
    Go To                   ${LOGIN URL}
    Log In As               guest                           secret                  ${SERVER_ID}
    Page Should Be Open     ${LOGIN URL}                    OMERO.web - Login
    Page Should Contain     Guest account is not supported.

[Teardown]    Close Browser
