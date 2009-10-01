; http://nsis.sourceforge.net/Go_to_a_NSIS_Page
;----------------------------------------------------------------------------
; Title             : Go to a NSIS page
; Short Name        : RelGotoPage
; Last Changed      : 22/Feb/2005
; Code Type         : Function
; Code Sub-Type     : Special Restricted Call, One-way StrCpy Input
;----------------------------------------------------------------------------
; Description       : Makes NSIS to go to a specified page relatively from
;                     the current page. See this below for more information:
;                     "http://nsis.sf.net/wiki/Go to a NSIS page"
;----------------------------------------------------------------------------
; Function Call     : StrCpy $R9 "(number|X)"
;
;                     - If a number &gt; 0: Goes foward that number of
;                       pages. Code of that page will be executed, not
;                       returning to this point. If it excess the number of
;                       pages that are after that page, it simulates a
;                       "Cancel" click.
;
;                     - If a number &lt; 0: Goes back that number of pages.
;                       Code of that page will be executed, not returning to
;                       this point. If it excess the number of pages that
;                       are before that page, it simulates a "Cancel" click.
;
;                     - If X: Simulates a "Cancel" click. Code will go to
;                       callback functions, not returning to this point.
;
;                     - If 0: Continues on the same page. Code will still
;                        be running after the call.
;
;                     Call RelGotoPage
;----------------------------------------------------------------------------
; Author            : Diego Pedroso
; Author Reg. Name  : deguix
;----------------------------------------------------------------------------
 
Function RelGotoPage
  IntCmp $R9 0 0 Move Move
    StrCmp $R9 "X" 0 Move
      StrCpy $R9 "120"
 
  Move:
  SendMessage $HWNDPARENT "0x408" "$R9" ""
FunctionEnd
