.globl matmul

.text
# =======================================================
# FUNCTION: Matrix Multiplication of 2 integer matrices
# 	d = matmul(m0, m1)
#   The order of error codes (checked from top to bottom):
#   If the dimensions of m0 do not make sense, 
#   this function exits with exit code 2.
#   If the dimensions of m1 do not make sense, 
#   this function exits with exit code 3.
#   If the dimensions don't match, 
#   this function exits with exit code 4.
# Arguments:
# 	a0 (int*)  is the pointer to the start of m0 
#	a1 (int)   is the # of rows (height) of m0
#	a2 (int)   is the # of columns (width) of m0
#	a3 (int*)  is the pointer to the start of m1
# 	a4 (int)   is the # of rows (height) of m1
#	a5 (int)   is the # of columns (width) of m1
#	a6 (int*)  is the pointer to the the start of d
# Returns:
#	None (void), sets d = matmul(m0, m1)
# =======================================================
matmul:
	# Prologue
	addi sp, sp, -20
	sw s1, 0(sp)
	sw s2, 4(sp)
	sw s3, 8(sp)
	sw s4, 12(sp)
	sw s5, 16(sp) 

	#Save arguments
	addi t4, x0, 4
	mul s1, a2, t4  	#m0 jump row
	mul s2, a5, t4  	#m1 jump row
	mul s3, a4, a5
	mul s3, s3, t4 		#m1 rewind to beginning
	addi s4, x0, 0 		#inner product
	addi s5, x0, 0 		#inner sum
	
    # Error checks
    mul t0, a1, a2
    bge t0, x0, check_1
    li a1, 2
    jal exit2
check_1:
	mul t1, a3, a4
	bge t1, x0, check_2
	li a1, 3
	jal exit2
check_2:
	beq a1, a5, loop_pre
	li a1, 4
	jal exit2

#prepare temps for loop
loop_pre:
	addi t4, x0, 0
loop_1:
	addi t3, x0, 0
loop_2:
	addi s5, x0, 0		#reset the sum holder
	addi t2, x0, 0

loop_3:
	lw t0, 0(a0)		#load m0
	lw t1, 0(a3)		#load m1
	mul s4, t0, t1		#store result of multiply
	add s5, s4, s5		#store result of add
	addi a0, a0, 4		#m0 shfift to next element in row
	add a3, a3, s2		#m1 shift to next element in column 
	addi t2, t2, 1		#loop + 1
	blt t2, a2, loop_3 	#check loop end
loop_3_end:
	sw s5, 0(a6)		#put first result in a6
	sub a0, a0, s1		#rewind m0
	sub a3, a3, s3		#rewind m1
	addi a3, a3, 4		#shift m1 to the next coloumn
	addi, a6, a6, 4		#shift a6 to next element to store the value
	addi t3, t3, 1		#ready to shift to the next colomn
	blt t3, a5, loop_2 	#if still colomns left in m1, return to loop
loop_2_end:
	addi t4, t4, 1
	add a0, a0, s1		#put m0 to the next row
	sub a3, a3, s2		#rewind m1 one row up
	blt t4, a1, loop_1
loop_1_end:
    # Epilogue
	lw s1, 0(sp)
	lw s2, 4(sp)
	lw s3, 8(sp)
	lw s4, 12(sp)
	lw s5, 16(sp) 
	addi sp, sp, 20
    ret