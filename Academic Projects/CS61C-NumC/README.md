# numc

Here's what I did in project 4:

1.Summary

This project is designed to be both a C project as well as a performance project. In this project we are asked to implement a slower version of numpy. Firstly, we completed a naive solution for some matrix functions in C, such as add (adding two matrix together element by element), sub (substracting two matrix element by element), mul (matrix mutiplication), pow (take the matrix to certain powers element by element), abs (calculate the absolute value of matrix elements), and neg (negate the matrix element by element). Then we wrote the setup file in Python to install the numc module. After that, we utilized certain methods taught in the class, such as manual loop unrolling, SIMD, OpenMp, and code optmization, to improve the performance of our naive solution, thus making numc.Matrix operations faster.

2.Procedures

	Step 1: Unrolling and Other Optimizations
	It is a very important step to speed up the computation by trying to apply conventional code optimizations. Ands there are few important ideas to better optmize. Firstly, function calls are expensive since they involve setting up a stack frame and jumping to a different part of code. To improve the performance we need to check if there are any redundant function calls. The second great idea is about manual loop unrolling. It reduces the number of iterations the function need to go through and therfore improved the performance to an acceptable level. Moreover, combining the power of loop unrolling with SIMD instructions and OpenMp parallel computation optmizations would significantly improve the overall performance.Once these optimizations are applied, we can start applying vectorization and parallelization to make the program even faster.

	Step 2: SIMD
	Single instruction, multiple data (SIMD) is a class of parallel computers in Flynn's taxonomy. It describes computers with multiple processing elements that perform the same operation on multiple data points simultaneously. Such machines exploit data level parallelism, but not concurrency: there are simultaneous (parallel) computations, but only a single process (instruction) at a given moment. SIMD is particularly applicable to common tasks such as matrix operations.

	Step 3: 
	OpenMP is an implementation of multithreading, a method of parallelizing whereby a master thread (a series of instructions executed consecutively) forks a specified number of slave threads and the system divides a task among them. The threads then run concurrently, with the runtime environment allocating threads to different processors. The section of code that is meant to run in parallel is marked accordingly, with a compiler directive that will cause the threads to form before the section is executed. Each thread has an id attached to it which can be obtained using a function (called omp_get_thread_num()). The thread id is an integer, and the master thread has an id of 0. After the execution of the parallelized code, the threads join back into the master thread, which continues onward to the end of the program. By default, each thread executes the parallelized section of code independently. Work-sharing constructs can be used to divide a task among the threads so that each thread executes its allocated part of the code. Both task parallelism and data parallelism can be achieved using OpenMP in this way. Finally we utilized OpenMP to parallelize computation. Due to the potential risk of data race, we need to be very careful to make sure that none of the different threads overwrites each others’ data. 

3.Statement of Academic Integrity

For this project, I make the following truthful statements: I have not received, I have not given, nor will I give or receive, any assistance related to coding directly from/to another student taking this course. 
