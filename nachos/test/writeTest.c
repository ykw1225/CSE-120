/*
 * writeTest.c
 * write string to a file, one byte at a time
 *
 */

#include "syscall.h"

int main(int argc, char *argv[])
{
  printf("...Executing Process[%s]\n", argv[1]);


  char *str = "\n random stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuffrandom stuff";

  int len = strlen(str);
  char buffer[len];

  char *fileName = "testWrite";

  int pid;
  pid = atoi(argv[1]);


  char *fname[20];
  sprintf(fname, "testWrite%d", pid);


  // crate and get file descriptor
  int fd = creat(fname);

  if(fd < 0 ){
    printf("error when creating file<%s>\n", fname);
  }

  //write str to file
  while(*str){
    int r = write(fd,str,1);
    if(r !=1){
      printf("Process [%d] failed to write character ( r=%d)\n", pid, r);
      exit(-1);
    }
    str++;
  }

  //close file
  int r = close(fd);

  if ( r < 0 ) {
    printf("error while closing file\n");
  }

  //validate file
  fd = open(fname);

  if(fd < 0){
    printf("failed when open file\n");
  }


  int c = 0;
  c = read(fd, buffer, len);

  if( c < 0 ) { printf("error reading file\n");}
  
  else if( c != len ){
    printf("!!!!!!!!!!!     Error!: Process %d expected to write %d bytes but wrote %d\n", pid, len, c);
  }

  else{
    //printf("buffer len: %d | str len: %d\n", c, len);
    //int i;
    //for(i = 0; i < c; i++){
      //printf("%c", buffer[i]);
    //}
    printf("-------------> [Process %d ]Success!\n", pid);
  }

  int closeChk;
  closeChk = close(fd);

  return 0;
}
