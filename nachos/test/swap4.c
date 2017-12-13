/*
 * swap4.c
 *
 * Test swapping by initializing an array larger than physical memory
 * and reading through it.  Once the array is initialized and its
 * pages are saved to swap, those pages will never get dirty again.
 *
 * Note that the program does not use printf to avoid the use of the
 * write system call.  Instead, it uses the exit status to indicate an
 * error or success.  If the data validates, then the program exits
 * with status -1000.  If the data does not validate, then the program
 * exits with a status indicating the index and bad value encountered.
 *
 * Geoff Voelker
 * 11/29/15
 */

int bigbufnum = 16 * 1024 / sizeof (int);
int bigbuf[16 * 1024 / sizeof (int)];

void
init_buf ()
{
    int i;

    for (i = 0; i < bigbufnum; i++) {
	bigbuf[i] = i;
    }
}

void
validate_buf ()
{
    int i;

    for (i = 0; i < bigbufnum; i++) {
	if (bigbuf[i] != i) {
	    // encode both the index and the bad data value in the status...
	    int s = i * 1000 * 1000;
	    s += bigbuf[i];
	    exit (s);
	}
    }
}


int
main (int argc, char *argv[])
{
    init_buf ();
    validate_buf ();
    validate_buf ();
    validate_buf ();
    exit (-1000);
}
