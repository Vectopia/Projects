.globl dot

.text
# =======================================================
# FUNCTION: Dot product of 2 int vectors
# Arguments:
#   a0 (int*) is the pointer to the start of v0
#   a1 (int*) is the pointer to the start of v1
#   a2 (int)  is the length of the vectors
#   a3 (int)  is the stride of v0
#   a4 (int)  is the stride of v1
# Returns:
#   a0 (int)  is the dot product of v0 and v1
#
# If the length of the vector is less than 1, 
# this function exits with error code 5.
# If the stride of either vector is less than 1,
# this function exits with error code 6.
# =======================================================
dot:
    # Prologue
    addi sp, sp, -24
    sw ra, 0(sp)
    sw a0, 4(sp)
    sw a1, 8(sp)
    sw a2, 12(sp)
    sw a3, 16(sp)
    sw a4, 20(sp)

    #ADD 1 for termination pre-evaluation
    addi t4, x0, 4
    mul t2, a3, t4
	mul t3, a4, t4
    addi t5, x0, 0
    addi a6, x0, 0
    addi t4, t4, -3
    #if length is less than 1 exit with code 5
    bge a2, t4, loop_check1
    li a1, 5
    jal exit2

loop_check1:
    #if stride is less than 1 exit with code 6
    bge a3, t4, loop_check2
    li a1, 6
    jal exit2

loop_check2:
    #if stride is less than 1 exit with code 6
    bge a4, t4, loop_start
    li a1, 6
    jal exit2

loop_start:
	#load the next element
    lw t0, 0(a0)
    lw t1, 0(a1)
    #mutiply corresponding element
    mul a5, t0, t1
    #store the result in temp
    add t5, a5, t5 
	#keep track and move the pointer as spec requested
	addi a6, a6, 1
	add a0, a0, t2
    add a1, a1, t3
    #check if v0 or v1 reached its end, if so, stop loop
    blt a6, a2, loop_start

loop_end:
	# Epilogue
    lw ra, 0(sp)
    lw a0, 4(sp)
    lw a1, 8(sp)
    lw a2, 12(sp)
    lw a3, 16(sp)
    lw a4, 20(sp)
    addi sp, sp, 24
	# Add result to a0 and return    
    addi a0, t5, 0 
    ret