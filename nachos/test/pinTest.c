/*
 * pinTest.c
 *
 * test if proj3 works under multi process and all pages are pinned condition
 * When running multiple process, in StubfileSystem.java
 * need to change maxOpenFiles to a large number so it allows to create more than 16 files
 * otherwise it will raise exception while creating files and affects test result
 *
 */

#include "syscall.h"

int main ( int argc, char*argv[])
{
  //comment or uncomment to choose the .coff file to run.

  char *prog = "writeTest.coff";

  //char *prog = "write101.coff";

  //char *prog = "swap5.coff";

  //char *prog = "join1.coff";

  //char *prog = "execarg1.coff";




  int pid, r, status = 0;

  char *argAry[2];
  
  argAry[0] = prog;

  //argAry[0] = "write101.coff";



  //printf("Executing %s...\n", prog);

  int numPro = 30;
  int i;

  for(i = 0; i < 20; i++){
    char arg[2];

    sprintf(arg, "%d", i);

    argAry[1] = arg;

    pid = exec(argAry[0], 2, argAry);

  }


  return 0;
}

