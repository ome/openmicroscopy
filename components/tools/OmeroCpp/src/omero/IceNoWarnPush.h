#ifndef OMEROCPP_ICE_WARN
#  if __GNUC__ > 4 || (__GNUC__ == 4 && __GNUC_MINOR__ >= 6)
#    pragma GCC diagnostic push
#    pragma GCC diagnostic ignored "-Woverloaded-virtual"
#    pragma GCC diagnostic ignored "-Wredundant-decls"
#    pragma GCC diagnostic ignored "-Wold-style-cast"
#  endif
#  define OMEROCPP_ICE_WARN
#else
#  error "OMEROCPP_ICE_WARN already defined; missing diagnostic pop"
#endif
