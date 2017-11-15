/*
 * write4.c
 *
 * Echo lines of input to the output.  Terminate on a ".".  Requires basic
 * functionality for both write and read.
 *
 * Invoking as "java nachos.machine.Machine -x write4.coff" will echo
 * the characters you type at the prompt (using the "../bin/nachos"
 * script turns off echo).
 *
 * Geoff Voelker
 * 11/9/15
 */

#include "stdio.h"
#include "stdlib.h"

int
main ()
{
    char buffer[80];
    char prompt[4];
    int i, n;

    prompt[0] = '-';
    prompt[1] = '>';
    prompt[2] = ' ';
    prompt[3] = '\0';

    while (1) {
       // print the prompt
       puts (prompt);

       // read the input terminated by a newline
       i = 0;
       do {
	   buffer[i] = getchar ();
       } while (buffer[i++] != '\n');
       buffer[i] = '\0';

       // if the input is just a period, then exit
       if (buffer[0] == '.' &&
	   buffer[1] == '\n') {
	   return 0;
       }

       // echo the input to the output
       puts (buffer);
   }
}
