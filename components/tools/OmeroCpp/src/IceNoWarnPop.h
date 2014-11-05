#ifdef OMEROCPP_ICE_WARN
#  undef OMEROCPP_ICE_WARN
#  ifdef __GNUC__
#    pragma GCC diagnostic pop
#  endif
#else
#  error "OMEROCPP_ICE_WARN not defined; missing diagnostic push"
#endif
