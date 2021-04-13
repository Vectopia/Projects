.globl relu

.text
# ==============================================================================
# FUNCTION: Performs an inplace element-wise ReLU on an array of ints
# Arguments:
# 	a0 (int*) is the pointer to the array
#	a1 (int)  is the # of elements in the array
# Returns:
#	None
#
# If the length of the vector is less than 1, 
# this function exits with error code 8.
# ==============================================================================
relu:
    # Prologue
    addi sp, sp, -12
    sw ra, 0(sp)
    sw a0, 4(sp)
    sw a1, 8(sp)
    addi t4, x0, 1
    addi t0, x0, 1

    bge a1, t4, loop_start
    li a1, 8
    jal exit2

loop_start:

    lw t1, 0(a0)
    bge t1, x0, loop_continue
    sw x0, 0(a0)

loop_continue:
   
    addi t0, t0, 1
    addi a0, a0, 4
    bge a1, t0, loop_start 

loop_end:

    lw ra, 0(sp)
    lw a0, 4(sp)
    lw a1, 8(sp)
    addi sp, sp, 12
    jr ra
    # Epilogue

    
