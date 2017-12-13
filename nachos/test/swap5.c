/*
 * swap5.c
 *
 * Test swapping by initializing an array larger than physical memory,
 * reading through it, and modifying it.  In each pass, the pages
 * containing array values will get dirty.
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
write_buf (int base)
{
    int i;

    for (i = 0; i < bigbufnum; i++) {
	bigbuf[i] = i + base;
    }
}

void
validate_buf (int base)
{
    int i;

    for (i = 0; i < bigbufnum; i++) {
	if (bigbuf[i] != (i + base)) {
	    // encode both the index and the bad data value in the status...
	    int s = i * 1000 * 1000;
	    s += bigbuf[i];
	    exit (s);
	}
	// this statement dirties the page
	bigbuf[i] = bigbuf[i];
    }
}


int
main (int argc, char *argv[])
{
    write_buf (0);
    validate_buf (0);
    write_buf (100 * 1000);
    validate_buf (100 * 1000);
    write_buf (200 * 1000);
    validate_buf (200 * 1000);
    exit (-1000);
}
