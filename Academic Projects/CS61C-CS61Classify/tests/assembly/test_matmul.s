.import ../../src/matmul.s
.import ../../src/utils.s
.import ../../src/dot.s

# static values for testing
.data
m0: .word 1 2 3 4 5 6 7 8 9
m1: .word 1 2 3 4 5 6 7 8 9
d: .word 0 0 0 0 0 0 0 0 0 # allocate static space for output

.text
main:
    # Load addresses of input matrices (which are in static memory), and set their dimensions
    la s0, m0
    la s1, m1
    la s3, d

    addi a1, x0, 3
    addi a2, x0, 3
    addi a4, x0, 3
    addi a5, x0, 3

    # Call matrix multiply, m0 * m1
    mv a0 s0
    mv a3 s1
    mv a6 s3

    jal ra matmul

    # Print the output (use print_int_array in utils.s)
    mv a1 a6
    jal ra print_int_array

    # Exit the program
    jal exit