To add a test, add the cpp file to the test_SOURCES section of Makefile.am.
"make" will run "make test" and execute the generated "test" binary.

Several parameters are available for when running boost tests. E.g.

./integration --report_level=detailed --catch_system_errors=no --detect_memory_leak=0 --build_info=yes

See http://beta.boost.org/doc/libs/1_34_1/libs/test/doc/components/utf/parameters/index.html for more.

    * build_info
    * catch_system_errors
    * detect_memory_leaks
    * log_format
    * log_level
    * no_result_code
    * output_format
    * random
    * report_format
    * report_level = "no", "confirm", "short", "detailed"
    * show_progress

