#ifdef OMEROCPP_ICE_WARN
#  undef OMEROCPP_ICE_WARN
#  if __GNUC__ > 4 || (__GNUC__ == 4 && __GNUC_MINOR__ >= 6)
#    pragma GCC diagnostic pop
#  endif
#else
#  error "OMEROCPP_ICE_WARN not defined; missing diagnostic push"
#endif
