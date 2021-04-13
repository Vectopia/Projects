.globl write_matrix

.text
# ==============================================================================
# FUNCTION: Writes a matrix of integers into a binary file
#   If any file operation fails or doesn't write the proper number of bytes,
#   exit the program with exit code 1.
# FILE FORMAT:
#   The first 8 bytes of the file will be two 4 byte ints representing the
#   numbers of rows and columns respectively. Every 4 bytes thereafter is an
#   element of the matrix in row-major order.
# Arguments:
#   a0 (char*) is the pointer to string representing the filename
#   a1 (int*)  is the pointer to the start of the matrix in memory
#   a2 (int)   is the number of rows in the matrix
#   a3 (int)   is the number of columns in the matrix
# Returns:
#   None
#
# If you receive an fopen error or eof, 
# this function exits with error code 53.
# If you receive an fwrite error or eof,
# this function exits with error code 54.
# If you receive an fclose error or eof,
# this function exits with error code 55.
# ==============================================================================
write_matrix:

    # Prologue
    addi sp, sp, -32
    sw ra, 0(sp)
    sw s0, 4(sp) 		#store file desp
    sw s1, 8(sp)		#store current malloc
    sw s2, 12(sp)		#store number of elements to write matrix
    sw s4, 16(sp)		#store start of matrix
    sw s5, 20(sp)		#store data size
    sw s7, 24(sp)		#store a2 from given
    sw s8, 28(sp)		#store a3 from given

    addi s2, x0, 0 
    addi s3, x0, 0
    addi s4, a1, 0
    addi s5, x0, 4
    addi s7, a2, 0
    addi s8, a3, 0
    #Put a0 to a1 for fopen, a2 for permission
    addi a1, a0, 0
    addi a2, x0, 1
    #open the file
    jal ra fopen
    #check open error
    addi t0, x0, -1
    bne a0, t0, pass_open_check
    li a1, 53
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

	sw s7, 0(s1)
	sw s8, 4(s1)

	addi a1, s0, 0
	addi a2, s1, 0
	addi a3, x0, 2
	add a4, x0, s5

	jal ra fwrite
	addi t0, x0, 2

	beq a0, t0, pass_write1_check
	addi a0, s1, 0
	jal ra free
	li a1 54
	jal exit2

pass_write1_check:
	#free the first buffer recorded by s1
	addi a0, s1, 0
	jal ra free
	#store the deminsion
	mul s2, s7, s8
	addi a1, s0, 0
	addi a2, s4, 0
	addi a3, s2, 0
	addi a4, x0, 4
	jal ra fwrite
	beq a0, s2, pass_write2_check
	li a1 54
	jal exit2

pass_write2_check:
	addi a1, s0, 0
	jal ra fclose
	addi t0, x0, -1
	bne a0, t0, pass_close_check
	li a1 55
	jal exit2 

pass_close_check:
	# Epilogue
    lw ra, 0(sp)
    lw s0, 4(sp) 		#store file desp
    lw s1, 8(sp)		#store current malloc
    lw s2, 12(sp)		#store number of elements to write matrix
    lw s4, 16(sp)		#store start of matrix
    lw s5, 20(sp)		#store data size
    lw s7, 24(sp)		#store a2 from given
    lw s8, 28(sp)		#store a3 from given
    addi sp, sp, 32
    ret
