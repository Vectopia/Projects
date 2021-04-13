/*
 * Include the provided hash table library.
 */
#include "hashtable.h"

/*
 * Include the header file.
 */
#include "philspel.h"

/*
 * Standard IO and file routines.
 */
#include <stdio.h>

/*
 * General utility routines (including malloc()).
 */
#include <stdlib.h>

/*
 * Character utility routines.
 */
#include <ctype.h>

/*
 * String utility routines.
 */
#include <string.h>

/*
 * This hash table stores the dictionary.
 */
HashTable *dictionary;

/*
 * The MAIN routine.  You can safely print debugging information
 * to standard error (stderr) as shown and it will be ignored in 
 * the grading process.
 */
int main(int argc, char **argv) {
  if (argc != 2) {
    fprintf(stderr, "Specify a dictionary\n");
    
    // include some tests

  //test stringHash
    char *a, *b, *c;
    a = "monday";
    fprintf(stderr, "the hash for %s is %d\n", a, stringHash(a));

    b = "berkeley";
    fprintf(stderr, "the hash for %s is %d\n", b, stringHash(b));

    c = "a";
    fprintf(stderr, "the hash for %s is %d\n", c, stringHash(c));
    fprintf(stderr, "the hash for %s is %d\n", c, stringHash(c));

    /* test for readDictionary. */
    dictionary = createHashTable(2255, &stringHash, &stringEquals);
    readDictionary("sampleDictionary");
   
    return 0;
  }
  /*
   * Allocate a hash table to store the dictionary.
   */
  fprintf(stderr, "Creating hashtable\n");
  dictionary = createHashTable(2255, &stringHash, &stringEquals);

  fprintf(stderr, "Loading dictionary %s\n", argv[1]);
  readDictionary(argv[1]);
  fprintf(stderr, "Dictionary loaded\n");

  fprintf(stderr, "Processing stdin\n");
  processInput();

  /*
   * The MAIN function in C should always return 0 as a way of telling
   * whatever program invoked this that everything went OK.
   */
  return 0;
}

/*
 * This should hash a string to a bucket index.  Void *s can be safely cast
 * to a char * (null terminated string) and is already done for you here 
 * for convenience.
 */
unsigned int stringHash(void *s) {
  char *string = (char *)s;
  // -- TODO --
  // Idea of the structure comes from djb2 hash algorithem 
  // from : http://www.cse.yorku.ca/~oz/hash.html
  int v;
  unsigned long strHash = 5381;
  
  while((v = *string++)) {
    strHash = v + ((strHash << 5) + strHash);
  } 

  return strHash;

}

/*
 * This should return a nonzero value if the two strings are identical 
 * (case sensitive comparison) and 0 otherwise.
 */
int stringEquals(void *s1, void *s2) {
  char *string1 = (char *)s1;
  char *string2 = (char *)s2;
  // -- TODO --

  // Ideas: Use strcmp.. Case Sensitive.. Flip the Cmp result.. 
  if (strcmp(string1, string2) == 0){
    return 5;
  } else return 0;


}

/*
 * This function should read in every word from the dictionary and
 * store it in the hash table.  You should first open the file specified,
 * then read the words one at a time and insert them into the dictionary.
 * Once the file is read in completely, return.  You will need to allocate
 * (using malloc()) space for each word.  As described in the spec, you
 * can initially assume that no word is longer than 60 characters.  However,
 * for the final 20% of your grade, you cannot assumed that words have a bounded
 * length.  You CANNOT assume that the specified file exists.  If the file does
 * NOT exist, you should print some message to standard error and call exit(0)
 * to cleanly exit the program.
 *
 * Since the format is one word at a time, with new lines in between,
 * you can safely use fscanf() to read in the strings until you want to handle
 * arbitrarily long dictionary chacaters.
 */
void readDictionary(char *dictName) {
  // -- TODO -

  //Some local variables(may modify)
  int wordLim = 60;
  int oriLim = 60;
  //Some local variables(may not modify)
  int getChar = 0;
  int numChar = 0;
  int size = wordLim + 1;
  char *insert;
  char *word = (char*) malloc(size*sizeof(char));


  //open the file specified
  FILE *dicFile = fopen(dictName, "r"); 

  //check file existence
  if(dicFile == NULL) {
    fprintf(stderr, "Error: File doesn't exist.\n");
    exit(0);
  }

  //read word one by one char, also consider the overbound conditions
  //The idea of using fgetC comes from: https://www.geeksforgeeks.org/fgetc-fputc-c/
  //The idea of implement the edge condition of fgetC comes from: 
  //https://stackoverflow.com/questions/19029852/reading-char-by-char-from-an-input-file-in-c?noredirect=1&lq=1
  //Could done with multiple if conditions and allocate the space manually 
  //but failed for much larger results (it is not safe to assume length of word)
  while(getChar != EOF) {
        getChar = fgetc(dicFile);
    //if overflow, bring the size up
    //if word is not over, put current word in the list, count+1, then read the next char. 
    if (isalpha(getChar)){

      word[numChar] = getChar;
      numChar = numChar + 1;

      if(numChar > (wordLim - 2)){
        wordLim = oriLim + wordLim;
        word = realloc(word, ((wordLim)*sizeof(char)));

      }
    } else{
      //Put the read data back into words, add the terminator
      //inseert into file (insertData! Use it!!)
      word[numChar] = '\0';
      insert = malloc(sizeof(char)*(1+strlen(word)));
      strcpy(insert, word);
      insertData(dictionary, insert, insert);

      // if word is over, clear word, clear wordcount, clear character
      wordLim = oriLim;
      numChar = 0;
    }

  }

  //end of file, return
 //Finally, close file, free memory
  fclose(dicFile);
  free(word);

}
/*
 * This should process standard input (stdin) and copy it to standard
 * output (stdout) as specified in the spec (e.g., if a standard 
 * dictionary was used and the string "this is a taest of  this-proGram" 
 * was given to stdin, the output to stdout should be 
 * "this is a teast [sic] of  this-proGram").  All words should be checked
 * against the dictionary as they are input, then with all but the first
 * letter converted to lowercase, and finally with all letters converted
 * to lowercase.  Only if all 3 cases are not in the dictionary should it
 * be reported as not found by appending " [sic]" after the error.
 *
 * Since we care about preserving whitespace and pass through all non alphabet
 * characters untouched, scanf() is probably insufficent (since it only considers
 * whitespace as breaking strings), meaning you will probably have
 * to get characters from stdin one at a time.
 *
 * Do note that even under the initial assumption that no word is longer than 60
 * characters, you may still encounter strings of non-alphabetic characters (e.g.,
 * numbers and punctuation) which are longer than 60 characters. Again, for the 
 * final 20% of your grade, you cannot assume words have a bounded length.
 */
void processInput() {
    // -- TODO --
	//User defined word length
	int wordLim = 60;

	//Local Variables
	int loc = 0;
 	char readChar;
 	char *err = " [sic]";
  //store words character by character
  char *word = (char*) malloc(60*sizeof(char));
  char *storage = (char*) malloc(60 * sizeof(char));
  	
  	//Get words character by character and check against dic.
  	while((readChar = getchar())) {

    //if the char we get is in the alphabet, then put it in words
    //idea of utilizing isaplha comes from piazza discussions
    if (isalpha(readChar)) {

      word[loc] = readChar; loc = loc + 1;
      //If word is longer than 60, then expand the memory
      //Minus 2 is used as a buffer, minus 1 also works :)
      if(loc > (wordLim - 2)){
        //increase only by 30 for more memory efficiency
        wordLim = wordLim + 30;
        word = realloc(word, wordLim*sizeof(char));
    	}
    } else {

      word[loc] = '\0';
      //Get storage ready for comparision
      storage = realloc(storage, wordLim*sizeof(char));

      if (word[0] != '\0') {

        fprintf(stdout, "%s", word);
        strcpy(storage, word);
        int check = 0;
        int index = 0;

        //case1: find by original
        if (findData(dictionary, storage) != NULL) {
          check = 1;
        }

        //process characters to lowercase except for 1.
        for (index = 1; storage[index] != '\0'; index = index + 1){
          storage[index] = tolower(storage[index]);
        }

        //case2: find by lower case except for 1
        if (findData(dictionary, storage) != NULL) {
          check = 1;
        }

        //case3: find by all lower case
        storage[0] = tolower(storage[0]);
        if (findData(dictionary, storage) != NULL) {
          check = 1;
        }

        if (!check) {
          //if all 3 cases are not satisfied, print err mesg
          fprintf(stdout, "%s", err);
        }
      }

      //reset wordsize and location for reading the next word.
      wordLim = 60;
      loc = 0;
      
      if (readChar != EOF) {
        //put the checked char into iostream.
        	putchar(readChar);
          
      } else { exit(0); }
	  }
	}

	 free(storage);
	 free(word);
  }



	
