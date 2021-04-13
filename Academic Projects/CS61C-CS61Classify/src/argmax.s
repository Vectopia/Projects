.globl argmax

.text
# =================================================================
# FUNCTION: Given a int vector, return the index of the largest
#	element. If there are multiple, return the one
#	with the smallest index.
# Arguments:
# 	a0 (int*) is the pointer to the start of the vector
#	a1 (int)  is the # of elements in the vector
# Returns:
#	a0 (int)  is the first index of the largest element
#
# If the length of the vector is less than 1, 
# this function exits with error code 7.
# =================================================================
argmax:

    # Prologue
    addi sp, sp, -12
    sw ra, 0(sp)
    sw a0, 4(sp)
    sw a1, 8(sp)
    addi t4, x0, 1
    #initialized the index
    addi t0, x0, 0
    lw t2, 0(a0)
  
    bge a1, t4, loop_start
    li a1, 7
    jal exit2

loop_start:
	#load the incoming element
    lw t1, 0(a0)  
    # if the max is >= current, do nothing and go to continue
    bge t2, t1, loop_continue
    # Give the curreent value to max
    addi t2, t1, 0
    #store the current index into s1
    addi t3, t0, 0

loop_continue:
    
    addi t0, t0, 1
    addi a0, a0, 4
    #end when the whole array is transversed
    bge a1, t0, loop_start 

loop_end:
    
    # Epilogue
    lw ra, 0(sp)
    lw a0, 4(sp)
    lw a1, 8(sp)
    addi sp, sp, 12
    addi a0, t3, 0
    ret