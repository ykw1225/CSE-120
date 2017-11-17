/*
 * test argument.c
 *
 * Write a string to stdout, one byte at a time.  Does not require any
 * of the other system calls to be implemented.
 *
 */

#include "syscall.h"

int
main (int argc, char *argv[])
{

    printf("!!!!-----------Argument test-----------!!!");

    char str[] = "this is the file Name";
    
    int r = creat (str);
    if (r != 1) {
	printf ("failed argument test");
	exit (-1);
    }

    return 0;
}
