
/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <IceUtil/UUID.h>
#include <boost_fixture.h>

void stringHandler(std::string str) {
  std::cout << "Handling:" << str << std::endl;
}

Fixture::Fixture() 
{
  /*     log_successful_tests     = 0,
	 log_test_suites          = 1,
	 log_messages             = 2,
	 log_warnings             = 3,
	 log_all_errors           = 4, // reported by unit test macros
	 log_cpp_exception_errors = 5, // uncaught C++ exceptions
	 log_system_errors        = 6, // including timeouts, signals, traps
	 log_fatal_errors         = 7, // including unit test macros or
	 // fatal system errors
	 log_progress_only        = 8, // only unit test progress to be reported
	 log_nothing              = 9
  */

  // NOT WORKING AS IT SHOULD
  b_ut::unit_test_monitor.register_exception_translator<std::string>( &stringHandler );
  b_ut::unit_test_log.set_threshold_level( b_ut::log_messages );
  //    set_unexpected(printUnexpected);
}

Fixture::~Fixture()
{
  //    results_reporter::detailed_report();
  //    printUnexpected();
  //    show_stackframe();
}

void Fixture::show_stackframe() {
#ifdef LINUX
  void *trace[16];
  char **messages = (char **)NULL;
  int i, trace_size = 0;

  trace_size = backtrace(trace, 16);
  messages = backtrace_symbols(trace, trace_size);
  printf("[bt] Execution path:\n");
  for (i=0; i<trace_size; ++i)
	printf("[bt] %s\n", messages[i]);
#endif
}

std::string Fixture::uuid()
{
  return IceUtil::generateUUID();
}

void Fixture::printUnexpected()
{
  /* Need printStackTrace.h for this
  char* buf = new char[1024];
  printStackTrace(buf, 1024);
  std::cout << "Trace:" << buf << std::endl;
  delete[] buf;
  */
}

b_ut::test_case const & Fixture::current() {
  return b_ut::framework::current_test_case();
}


b_ut::unit_test_monitor_t& Fixture::monitor() {
  return b_ut::unit_test_monitor;
}

b_ut::unit_test_log_t& Fixture::log() {
  return b_ut::unit_test_log;
}

const omero::client* Fixture::login(const std::string& username, const std::string& password) {
    int argc = 0;
    char** argv = new char*[0];
    omero::client* client = new omero::client(argc, argv);
    client->createSession(username, password);
    clients.push_back(client);
    return client;
}

