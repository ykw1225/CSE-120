/*
 * pinTest.c
 *
 * test if proj3 works under multi process and all pages are pinned condition
 *
 *
 */

#include "syscall.h"

int main ( int argc, char*argv[])
{
  //char *prog = "writeTest.coff";

  char *prog = "write101.coff";

  //char *prog = "swap5.coff";

  //char *prog1 = "write10.coff";
  int pid, r, status = 0;

  char *argAry[2];
  
  argAry[0] = prog;

  //argAry[0] = "write101.coff";



  //printf("Executing %s...\n", prog);

  int numPro = 30;
  int i;

  for(i = 0; i < 4; i++){
    char arg[2];

    sprintf(arg, "%d", i);

    argAry[1] = arg;

    pid = exec(argAry[0], 2, argAry);

  }


  return 0;
}

