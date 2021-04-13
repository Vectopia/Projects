.globl read_matrix

.text
# ==============================================================================
# FUNCTION: Allocates memory and reads in a binary file as a matrix of integers
#   If any file operation fails or doesn't read the proper number of bytes,
#   exit the program with exit code 1.
# FILE FORMAT:
#   The first 8 bytes are two 4 byte ints representing the # of rows and columns
#   in the matrix. Every 4 bytes afterwards is an element of the matrix in
#   row-major order.
# Arguments:
#   a0 (char*) is the pointer to string representing the filename
#   a1 (int*)  is a pointer to an integer, we will set it to the number of rows
#   a2 (int*)  is a pointer to an integer, we will set it to the number of columns
# Returns:
#   a0 (int*)  is the pointer to the matrix in memory
#
# If you receive an fopen error or eof, 
# this function exits with error code 50.
# If you receive an fread error or eof,
# this function exits with error code 51.
# If you receive an fclose error or eof,
# this function exits with error code 52.
# ==============================================================================
read_matrix:

    # Prologue
    addi sp, sp, -36
    sw ra, 0(sp)
    sw s0, 4(sp) 		#store file desp
    sw s1, 8(sp)		#store current malloc
    sw s2, 12(sp)		#store bytes to read for matrix
    sw s4, 20(sp)		#store row
    sw s5, 24(sp)		#store col
    sw s7, 28(sp)		#store a1 from given
    sw s8, 32(sp)		#store a2 from given

    #Open file before other operations with READ-ONLY permission
    #a0 is a file descriptor. We will call future file-related 
    #functions on this file descriptor, so we know which opened 
    #file we’re reading/writing/closing. On failure, a0 is set to -1.

    #Set a2 to 0 for permission checking, set t0 for bne check
    addi s2, x0, 0 
    addi s4, x0, 0
    addi s5, x0, 0
    addi s7, a1, 0
    addi s8, a2, 0
    #Put a0 to a1 for fopen, a2 for permission
    addi a1, a0, 0
    addi a2, x0, 0
    #open the file
    jal ra fopen
    #check open error
    addi t0, x0, -1
    bne a0, t0, pass_open_check
    li a1, 50
    jal exit2

pass_open_check:
	#store the file descriptor
	addi s0, a0, 0
	#set a0 to 8 for reading first two
	addi a0, x0, 8
	jal ra malloc
	addi t0, x0, 0
	bne a0, t0, pass_malloc1_check
	jal ra free
	li a1, 48
	jal exit2

pass_malloc1_check:
	#set s1 to point to the allocated memory
	addi s1, a0, 0
	
	addi a1, s0, 0
	addi a2, s1, 0
	addi a3, x0, 8
	jal ra fread
	addi t0, x0, 8
	beq a0, t0, pass_read1_check
	addi a0, s1, 0
	jal ra free
	li a1 51
	jal exit2

pass_read1_check:
	lw s4, 0(s1)
	lw s5, 4(s1)
	#free the first buffer recorded by s1
	addi a0, s1, 0
	jal ra free
	#store the deminsion
	mul s2, s4, s5
	addi t3, x0, 4
	mul s2, s2, t3
	#allocate memory for reading matrix elements
	addi a0, s2, 0
	jal ra malloc
	addi t0, x0, 0
	bne a0, t0, pass_malloc2_check
	jal ra free
	li a1, 48
	jal exit2

pass_malloc2_check:
	#store results in s1 (current malloc)
	addi s1, a0, 0
	addi a1, s0, 0
	addi a2, s1, 0
	addi a3, s2, 0
	jal ra fread
	beq a0, s2, pass_read2_check
	addi a0, s1, 0
	jal ra free
	li a1 51
	jal exit2

pass_read2_check:
	addi a1, s0, 0
	jal ra fclose
	addi t0, x0, -1
	bne a0, t0, pass_close_check
	addi a0, s1, 0
	jal ra free
	li a1 52
	jal exit2 

pass_close_check:
	addi a0, s1, 0
	addi a1, s7, 0
	addi a2, s8, 0
	sw s4, 0(a1) 
	sw s5, 0(a2)
	#free s1
	
    # Epilogue
    lw ra, 0(sp)
    lw s0, 4(sp) 		#store file desp
    lw s1, 8(sp)		#store row and colomn
    lw s2, 12(sp)		#store bytes to read for matrix
    lw s4, 20(sp)		#store row
    lw s5, 24(sp)		#store col
    lw s7, 28(sp)		#store a1 from given
    lw s8, 32(sp)		#store a2 from given
	addi sp, sp, 36

    ret