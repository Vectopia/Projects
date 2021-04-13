#include "matrix.h"
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <omp.h>

// Include SSE intrinsics
#if defined(_MSC_VER)
#include <intrin.h>
#elif defined(__GNUC__) && (defined(__x86_64__) || defined(__i386__))
#include <immintrin.h>
#include <x86intrin.h>
#endif

/* Below are some intel intrinsics that might be useful
 * void _mm256_storeu_pd (double * mem_addr, __m256d a)
 * __m256d _mm256_set1_pd (double a)
 * __m256d _mm256_set_pd (double e3, double e2, double e1, double e0)
 * __m256d _mm256_loadu_pd (double const * mem_addr)
 * __m256d _mm256_add_pd (__m256d a, __m256d b)
 * __m256d _mm256_sub_pd (__m256d a, __m256d b)
 * __m256d _mm256_fmadd_pd (__m256d a, __m256d b, __m256d c)
 * __m256d _mm256_mul_pd (__m256d a, __m256d b)
 * __m256d _mm256_cmp_pd (__m256d a, __m256d b, const int imm8)
 * __m256d _mm256_and_pd (__m256d a, __m256d b)
 * __m256d _mm256_max_pd (__m256d a, __m256d b)
*/

/* Generates a random double between low and high */
double rand_double(double low, double high) {
    double range = (high - low);
    double div = RAND_MAX / range;
    return low + (rand() / div);
}

/* Generates a random matrix */
void rand_matrix(matrix *result, unsigned int seed, double low, double high) {
    srand(seed);
    for (int i = 0; i < result->rows; i++) {
        for (int j = 0; j < result->cols; j++) {
            set(result, i, j, rand_double(low, high));
        }
    }
}

/*
 * Allocates space for a matrix struct pointed to by the double pointer mat with
 * `rows` rows and `cols` columns. You should also allocate memory for the data array
 * and initialize all entries to be zeros. `parent` should be set to NULL to indicate that
 * this matrix is not a slice. You should also set `ref_cnt` to 1.
 * You should return -1 if either `rows` or `cols` or both have invalid values, or if any
 * call to allocate memory in this function fails. Return 0 upon success.
 */

//Done!
int allocate_matrix(matrix **mat, int rows, int cols) {
    /* TODO: YOUR CODE HERE */
    if (rows <= 0 || cols <= 0){
        return -1;
    }
    *mat = (malloc(sizeof(matrix)));
    matrix * mm = *mat;
    mm->parent=NULL;
    mm->ref_cnt=1;
    double * data = calloc(rows * cols, sizeof(double));
    for(int i = 0; i < rows * cols; i += 1){
        data[i] = 0;
    }
    mm->data = data;
    mm->rows = rows;
    mm->cols = cols;
    return 0;
}

/*
 * Allocates space for a matrix struct pointed to by `mat` with `rows` rows and `cols` columns.
 * Its data should point to the `offset`th entry of `from`'s data (you do not need to allocate memory)
 * for the data field. `parent` should be set to `from` to indicate this matrix is a slice of `from`.
 * You should return -1 if either `rows` or `cols` or both are non-positive or if any
 * call to allocate memory in this function fails. Return 0 upon success.
 */

//Done!
int allocate_matrix_ref(matrix **mat, matrix *from, int offset, int rows, int cols) {
    /* TODO: YOUR CODE HERE */
    if (rows <= 0 || cols <= 0){
        return -1;
    }

    *mat = (malloc(sizeof(matrix)));
    matrix * mm = *mat;
    if(mm == NULL){
        return -1;
    }
    mm->rows = rows;
    mm->cols = cols;
    mm->data = from->data + offset;
    mm->parent = from;
    from->ref_cnt += 1;
    mm->ref_cnt = 1;
    return 0;
}

/*
 * You need to maindex2e sure that you only free `mat->data` if `mat` is not a slice and has no existing slices,
 * or if `mat` is the last existing slice of its parent matrix and its parent matrix has no other references
 * (including itself). You cannot assume that mat is not NULL.__
 */

//Done!
void deallocate_matrix(matrix *mat) {
    /* TODO: YOUR CODE HERE */
    // printf("deleting in matrix.c\n");
    if(mat == NULL) return;
    mat->ref_cnt -= 1;
    if(mat->ref_cnt > 0) return;
    // printf("ref count == 0, deleting in matrix.c\n");
    if(mat->parent != NULL){
        deallocate_matrix(mat->parent);
    } else {
        free(mat->data);
    }
    free(mat);
}

/*
 * Returns the double value of the matrix at the given row and column.
 * You may assume `row` and `col` are valid.
 */

//Done!
double get(matrix *mat, int row, int col) {
    /* TODO: YOUR CODE HERE */
    return mat->data[row*mat->cols + col];
}

/*
 * Sets the value at the given row and column to val. You may assume `row` and
 * `col` are valid
 */

//Done!
void set(matrix *mat, int row, int col, double val) {
    /* TODO: YOUR CODE HERE */
    mat->data[row*mat->cols + col] = val;
}

/*
 * Sets all entries in mat to val
 */

//Done!
void fill_matrix(matrix *mat, double val) {
    /* TODO: YOUR CODE HERE */
    __m256d mat_fill = _mm256_set1_pd(val);
    int mat_size = mat->rows * mat->cols;

    #pragma omp parallel for
    for(int i = 0; i < mat_size / 4*4; i += 4){
       _mm256_storeu_pd(mat->data + i, mat_fill);
   }

   //Tail Case:
   for(int i = mat_size / 4*4; i < mat_size; i += 1){
        mat->data[i] = val;
   }
}

/*
 * Store the result of adding mat1 and mat2 to `result`.
 * Return 0 upon success and a nonzero value upon failure.
 */

//Done!
int add_matrix(matrix *result, matrix *mat1, matrix *mat2) {
    /* TODO: YOUR CODE HERE */

    //Error check
    if (result == NULL) {
        return -1;
    }
    if (    mat1->cols != mat2->cols || 
            mat1->rows != mat2->rows || 
            result->cols != mat2->cols || 
            result->cols < 1 ||
            result->rows < 1 ||
            result->rows != mat2->rows) {

        return -1;
    }

    //Initialize variables
    int cnt = mat1->cols * mat1->rows;
    __m256d tmpp, add1, add2;

    //OpenMp optmization for add operation
    #pragma omp parallel for private(tmpp, add1, add2)
    for(int i = 0; i < cnt / 4*4; i += 4) {

            add1 = _mm256_loadu_pd(mat1->data + i);
            add2 = _mm256_loadu_pd(mat2->data + i);
            tmpp = _mm256_add_pd (add1, add2);
            _mm256_storeu_pd(result->data + i, tmpp);
    }

    //Tail case
    for(int i = cnt / 4*4; i < cnt; i += 1) {
        result->data[i] = mat1->data[i] + mat2->data[i];
    }

    return 0;
}

/*
 * Store the result of subtracting mat2 from mat1 to `result`.
 * Return 0 upon success and a nonzero value upon failure.
 */

//Done!
int sub_matrix(matrix *result, matrix *mat1, matrix *mat2) {
    /* TODO: YOUR CODE HERE */

    //Error check
    if (result == NULL) {
        return -1;
    }
    if (    mat1->cols != mat2->cols || 
            mat1->rows != mat2->rows || 
            result->cols != mat2->cols || 
            result->cols < 1 ||
            result->rows < 1 ||
            result->rows != mat2->rows) {
        return -1;
    }

    //Initialize variables
    int cnt = mat1->cols * mat1->rows;
    __m256d tmpp, sub1, sub2;

    //OpenMp optmization for sub operation
    #pragma omp parallel for private(tmpp, sub1, sub2)
    for(int i = 0; i < cnt / 4*4; i += 4) {

            sub1 = _mm256_loadu_pd(mat1->data + i);
            sub2 = _mm256_loadu_pd(mat2->data + i);
            tmpp = _mm256_sub_pd (sub1, sub2);
            _mm256_storeu_pd(result->data + i, tmpp);
    }

    //Tail case
    for(int i = cnt / 4*4; i < cnt; i += 1) {
        result->data[i] = mat1->data[i] - mat2->data[i];
    }

    return 0;
}

/*
 * Store the result of multiplying mat1 and mat2 to `result`.
 * Return 0 upon success and a nonzero value upon failure.
 * Remember that matrix multiplication is not the same as multiplying individual elements.
 */
int mul_matrix(matrix *result, matrix *mat1, matrix *mat2) {
    /* TODO: YOUR CODE HERE */

    //DISCLAIMER:
    //DISCUSSED CONCEPTS WITH MY ROOMATE BUT WROTE OUR OWN CODE
    //PS(0810):My roommate said our code looks very similar lol
    //Been helped by Caroline during office hours for debugging.

    //BulletPoints:
    //Allocate buffer matrix for calculation, one for mat one for result
    //Put the data in mat into the buffer
    //Initialize result matrix
    //Calculate power
    //Store the result bacindex2
    //Been helped by Caroline during office hours for debugging.

    //Error check
    if (result == NULL) {
        return -1;
    }
    if (mat1->cols != mat2->rows || 
        result->rows != mat1->rows || 
        result->cols != mat2->cols ) {
        return -1;
    }

    //Last Version
    // static double *transpose = NULL;
    // static int transpose_len = 0;
    // if (result == NULL){
    //     return -1;
    // }
    // if (    mat1->cols != mat2->rows || 
    //         result->cols != mat2->cols || 
    //         result->rows != mat1->rows ){
    //     return -1;
    // }
    // // int cnt = mat1->cols * mat1->rows;
    // matrix * tmp_mat = result;
    // if(result == mat1 || result == mat2){
    //     allocate_matrix(&tmp_mat, result->cols, result->rows);
    // }
    // if(transpose_len < mat2->cols*mat2->rows){
    //     if(transpose !=NULL) free(transpose);
    //     transpose=malloc(sizeof(double)*mat2->cols*mat2->rows);
    // }

    matrix *mat_mul = result;
    //Allocate memory space for the copy of intermediate result
    if (result == mat1 || 
        result == mat2) {

        //Allocate memory space successfully
        mat_mul = (matrix*) malloc(sizeof(matrix));

        //If failed in allocation, return wiith malloc errors
        if(allocate_matrix(&mat_mul, result->rows, result->cols) != 0) {
            return -1;
        }
    }

    //Declare global variables
    int ix, px;
    int mat_shape = mat1->rows * mat2->cols;
    __m256d mat_404 = _mm256_set1_pd(0);

    //VERY IMPORTANT: Transposing matrix 2 for performance optmization
    //Source: Caroline Liu (Huge thanks) and Wikipedia ;)

    //Optmized for larger matrix generation
    // if (mat2->cols >= 96) {
    //     #pragma omp parallel for
    //     for (int i = 0; i < mat2->rows * mat2->cols; i += 1) {
    //         mat_tmp2[(i % mat2->cols) * mat2->rows + i / mat2->cols] = mat2->data[i];
    // } else {
    //     for (int i = 0; i < mat2->rows * mat2->cols; i += 1) {
    //         mat_tmp2[(i % mat2->cols) * mat2->rows + i / mat2->cols] = mat2->data[i];
    // }
    // //Declare local variables
    // int ix, px;
    // int mat_shape = mat1->rows * mat2->cols;
    // __m256d mat_404 = _mm256_set1_pd(0);

    //Allocate memory space for transposing matrix 2
    double * mat2_trans = (double *) malloc(mat2->rows * mat2->cols * sizeof(double));

    //If failed in allocation, return wiith malloc errors
    if (mat2_trans == NULL) {
        return -1;
    }

    for (ix = 0; ix < mat2->rows * mat2->cols; ix += 1) {
        mat2_trans[(ix % mat2->cols) * mat2->rows + ix / mat2->cols] = mat2->data[ix];
    }

    // for(int row = 0; row < mat2->rows; row += 1)
    // for(int col = 0; col < mat2->cols; col += 1){
    //     transpose[col * mat2->rows + row] = mat2->data[row * mat2->cols + col];
    // }

    

    // fill_matrix(result, 0);
    // void set(matrix *mat, int row, int col, double val) {
    // mat->data[row*mat->cols + col] = val;
    // }
    // #pragma omp parallel for
    // for(int i = 0; i < mat->cols; i += 1){
    //    result->data[i * result->cols + i] = 1;
    // }
    // unsigned int tmpp = 1 << 31;
    // #pragma omp parallel for
    // for (;tmpp & pow == 0;){
    //    tmpp = tmpp >> 1;
    // }
    // #pragma omp parallel for
    // for(;tmpp > 0;) {
    //    mul_matrix(result, result, result);
    //    if(tmpp & pow){
    //        mul_matrix(result, result, mat);
    //    }
    //    tmpp = tmpp >> 1;
    // }
    // return 0;


    //Divide multiplication into 2 parts: 
    //small matrix (<128*128) multiplication 
    //and large matrix (>128*128) multiplication 

    if (mat1->rows >= 96) {

//------------------------------------------ START OF WORKING SPACE-----------------------------------------------//

        //Version 0.1
        //     #pragma omp parallel for 
        //     for(int row = 0; row < result->rows; row += 1){
        //         for(int col = 0; col < result->cols; col += 1){
        //             double res = 0;
        //             int i = 0;
        //             double tmp_store[4];
        //             double *ref1 = mat1->data + row * mat1->cols + i;
        //             double *ref2 = transpose + col * mat2->rows + i;
        //             for(; i < mat1->cols-4; i += 4){
        //                 __m256d a = _mm256_loadu_pd(ref1);
        //                 __m256d b = _mm256_loadu_pd(ref2);
        //                 __m256d c = _mm256_mul_pd (a, b);
        //                 __m256d d = _mm256_hadd_pd(c, a);
        //                 _mm256_storeu_pd(tmp_store, d);
        //                 res += tmp_store[0] + tmp_store[2];
        //                 // res += tmp_store[1] + tmp_store[3];
        //                 ref1 += 4;
        //                 ref2 += 4;
        //             }
        //             for(; i < mat1->cols; i += 1){
        //                 res += *ref1 *  *ref2; // i*mat2->cols + col
        //                 ref1 += 1;
        //                 ref2 += 1;
        //             }
        //             // printf("%.3lf\n", res);
        //             set(tmp_mat, row, col, res);
        //         }
        //     }
        // }
        #pragma omp parallel for private(ix, px)
        for (ix = 0; ix < mat_shape - (mat_shape % 2); ix += 2) {

            //Initialize local veriables
            double tmp_store[4];
            __m256d tmpp = mat_404;

            int rx = ix / mat2->cols * mat1->cols;
            int cx = ix % mat2->cols * mat2->rows;
            int rx1 = (ix + 1) / mat2->cols * mat1->cols;
            int cx1 = (ix + 1) % mat2->cols * mat2->rows;

            for (px = 0; px < mat1->cols / 4*4; px += 4) {

                // __m26d mat1_tmp = 
                // _mm256_loadu_pd(mat1->data + mat_row + index2);
                // __m256d mat2_tmp = 
                // _mm256_loadu_pd(mat_tmp2 + mat_col + index2);
                // __m256d mat1_load = 
                // _mm256_loadu_pd(mat1->data + mat_row1 + index2);
                // __m256d mat2_load = 
                // _mm256_loadu_pd(mat_tmp2 + mat_col1 + index2);
                // __m256d mul_1 = 
                // _mm256_mul_pd(mat1_tmp, mat2_tmp);
                // __m256d mul_2 = 
                // _mm256_mul_pd(mat1_load, mat2_load);

                __m256d load_mat1_a = _mm256_loadu_pd(mat1->data + rx + px);
                __m256d load_mat1_b = _mm256_loadu_pd(mat1->data + rx1 + px);

                __m256d load_mat2_a = _mm256_loadu_pd(mat2_trans + cx + px);
                __m256d load_mat2_b = _mm256_loadu_pd(mat2_trans + cx1 + px);

                        // __m256d a = _mm256_loadu_pd(ref1);
                        // __m256d b = _mm256_loadu_pd(ref2);
                        // __m256d c = _mm256_mul_pd (a, b);
                        // __m256d d = _mm256_hadd_pd(c, a);
                        // _mm256_storeu_pd(tmp_store, d);
                        //  res += tmp_store[0] + tmp_store[2];

                __m256d mul_1 = _mm256_mul_pd(load_mat1_a, load_mat2_a);
                __m256d mul_2 = _mm256_mul_pd(load_mat1_b, load_mat2_b);

                // _mm256_mul_pd(mat1_load, mat2_load);
                // __m256d mul_result = 
                // _mm256_hadd_pd(mul_1, mul_2);
                // tmp = _mm256_add_pd(tmp, mul_result);

                __m256d tmp_res = _mm256_hadd_pd(mul_1, mul_2);
                tmpp = _mm256_add_pd(tmpp, tmp_res);
            }


            _mm256_storeu_pd(tmp_store, tmpp);
            double tmp1 = tmp_store[0] + tmp_store[2];
            double tmp2 = tmp_store[1] + tmp_store[3];

            for (px = mat1->cols / 4*4; px < mat1->cols; px += 1) {

                tmp1 += mat1->data[rx + px] * mat2_trans[cx + px];
                tmp2 += mat1->data[rx1 + px] * mat2_trans[cx1 + px];
            }

            mat_mul->data[ix] = tmp1;
            mat_mul->data[ix + 1] = tmp2;
        }

    } else {

        //Non-OpenMp Version
        for (ix = 0; ix < mat_shape - (mat_shape % 2); ix += 2) {

            //Initialize local veriables
            double tmp_store[4];
            __m256d tmpp = mat_404;

            int rx = ix / mat2->cols * mat1->cols;
            int cx = ix % mat2->cols * mat2->rows;
            int rx1 = (ix + 1) / mat2->cols * mat1->cols;
            int cx1 = (ix + 1) % mat2->cols * mat2->rows;

            for (px = 0; px < mat1->cols / 4*4; px += 4) {

                // _mm256_loadu_pd(mat_tmp2 + mat_col + index2);
                // __m256d mat1_load = 
                // _mm256_loadu_pd(mat1->data + mat_row1 + index2);
                // __m256d mat2_load = 
                // _mm256_loadu_pd(mat_tmp2 + mat_col1 + index2);
                // __m256d mul_1 = 
                // _mm256_mul_pd(mat1_tmp, mat2_tmp);

                __m256d load_mat1_a = _mm256_loadu_pd(mat1->data + rx + px);
                __m256d load_mat1_b = _mm256_loadu_pd(mat1->data + rx1 + px);

                __m256d load_mat2_a = _mm256_loadu_pd(mat2_trans + cx + px);
                __m256d load_mat2_b = _mm256_loadu_pd(mat2_trans + cx1 + px);

                        // __m256d a = _mm256_loadu_pd(ref1);
                        // __m256d b = _mm256_loadu_pd(ref2);
                        // __m256d c = _mm256_mul_pd (a, b);
                        // __m256d d = _mm256_hadd_pd(c, a);
                        // _mm256_storeu_pd(tmp_store, d);
                        //  res += tmp_store[0] + tmp_store[2];

                __m256d mul_1 = _mm256_mul_pd(load_mat1_a, load_mat2_a);
                __m256d mul_2 = _mm256_mul_pd(load_mat1_b, load_mat2_b);

                // _mm256_mul_pd(mat1_load, mat2_load);
                // __m256d mul_result = 
                // _mm256_hadd_pd(mul_1, mul_2);
                // tmp = _mm256_add_pd(tmp, mul_result);

                __m256d tmp_res = _mm256_hadd_pd(mul_1, mul_2);
                tmpp = _mm256_add_pd(tmpp, tmp_res);
            }

            _mm256_storeu_pd(tmp_store, tmpp);
            double tmpp1 = tmp_store[0] + tmp_store[2];
            double tmpp2 = tmp_store[1] + tmp_store[3];


            for (px = mat1->cols / 4*4; px < mat1->cols; px += 1) {

                tmpp1 += mat1->data[rx + px] * mat2_trans[cx + px];
                tmpp2 += mat1->data[rx1 + px] * mat2_trans[cx1 + px];
            }

            mat_mul->data[ix] = tmpp1;
            mat_mul->data[ix + 1] = tmpp2;
        }
    }

    //Source: My roommate :)
    //A big "tail case" for the case that it cannot be distributed by 2
    if (mat_shape % 2) {

        //Initialize local veriables
        double tmp_store[4];
        __m256d mat_0x = mat_404;

        //Calculate dimension
        int dim1 = (mat1->rows - 1) * mat1->cols;
        int dim2 = (mat2->cols - 1) * mat2->rows;

        for (px = 0; px < mat1->cols / 4*4; px += 4) {

            __m256d load_mat1 = _mm256_loadu_pd(mat1->data + dim1 + px);
            __m256d load_mat2 = _mm256_loadu_pd(mat2_trans + dim2 + px);

                // _mm256_mul_pd(mat1_load, mat2_load);
                // __m256d mul_result = 
                // _mm256_hadd_pd(mul_1, mul_2);
                // tmp = _mm256_add_pd(tmp, mul_result);

            __m256d tmp_mulx = _mm256_mul_pd(load_mat1, load_mat2);
            mat_0x = _mm256_add_pd(mat_0x, tmp_mulx);
        }

        _mm256_storeu_pd(tmp_store, mat_0x);
        double tmpp = tmp_store[0] + tmp_store[1] + tmp_store[2] + tmp_store[3];

        //tail case
        for (px = mat1->cols / 4*4; px < mat1->cols; px += 1) {

            tmpp += mat1->data[dim1 + px] * mat2_trans[dim2 + px];
        }

        mat_mul->data[mat_shape - 1] = tmpp;
    }

//------------------------------------------ END OF WORKING SPACE-----------------------------------------------//

    //     if(result == mat1 || result == mat2){
    //         double * tmp = tmp_mat->data;
    //         tmp_mat->data = result->data;
    //         result->data = tmp;
    //         deallocate_matrix(tmp_mat);
    //     }
    //     return 0;
    // }
    free(mat2_trans);

    if (result == mat1 || 
        result == mat2) {

        double * swap_buff = mat_mul->data;
        mat_mul->data = result->data;
        result->data = swap_buff;
        deallocate_matrix(mat_mul);
    }

    return 0;
}

/*
 * Store the result of raising mat to the (pow)th power to `result`.
 * Return 0 upon success and a nonzero value upon failure.
 * Remember that pow is defined with matrix multiplication, not element-wise multiplication.
 */
int pow_matrix(matrix *result, matrix *mat, int pow) {
    /* TODO: YOUR CODE HERE */

    //Error checking
    if (    result == NULL || 
            pow < 0 || 
            mat->rows != mat->cols || 
            mat->rows < 1 ||
            mat->cols < 1 ||
            result->cols != mat->cols || 
            result->rows != mat->rows ||
            result->cols != result->rows){
        return -1;
    }

    int cnt = mat->rows * mat->cols;
    //fill_matrix(result, 0);
    //void set(matrix *mat, int row, int col, double val) {
    /* TODO: YOUR CODE HERE */
    //mat->data[row*mat->cols + col] = val;
    //}

    //#pragma omp parallel for
    //for(int i = 0; i < mat->cols; i += 1){
    //    result->data[i * result->cols + i] = 1;
    //}
    //unsigned int tmpp = 1 << 31;

    //#pragma omp parallel for
    //for (;tmpp & pow == 0;){
    //    tmpp = tmpp >> 1;
    //}

    //#pragma omp parallel for
    //for(;tmpp > 0;) {
    //    mul_matrix(result, result, result);
    //    if(tmpp & pow){
    //        mul_matrix(result, result, mat);
    //    }
    //    tmpp = tmpp >> 1;
    //}
    //return 0;
    //}


    //DISCLAIMER:
    //DISCUSSED CONCEPTS WITH MY ROOMATE BUT WROTE OUR OWN CODE
    //Been helped by Caroline during office hours for debugging.

    //BulletPoints:
    //Allocate buffer matrix for calculation, one for mat one for result
    //Put the data in mat into the buffer
    //Initialize result matrix
    //Calculate power
    //Store the result bacindex2
    //Been helped by Caroline during office hours for debugging.

    //Allocate memory space for power calculation
    matrix *mat_pow = (matrix *) malloc(sizeof(matrix));
    //If failed in allocation, return wiith malloc errors
    if(allocate_matrix(&mat_pow, result->rows, result->cols) != 0) {
        return -1;
    }

    //Copy mat matrix

    __m256d val;
    #pragma omp parallel for
    for(int i = 0; i < cnt / 4 * 4; i += 4) {

        val = _mm256_loadu_pd(mat->data + i);
        _mm256_storeu_pd(mat_pow->data + i, val);
    }

    //tail case:
    for(int i = cnt/ 4 * 4; i < cnt; i += 1) {

        mat_pow->data[i] = mat->data[i];
    }

    //Allocate a temp to store result and access result at the end to reduce memory access
    matrix *mat_tmp = result;

    if(result == mat) {
        mat_tmp = (matrix*) malloc(sizeof(matrix));

        //Throw error if allocation fails
        if(allocate_matrix(&mat_tmp, result->rows, result->cols) != 0) {
            return -1;
        }
    }

    //Initialize the result matrix
    fill_matrix(mat_tmp, 0);

    #pragma omp parallel for
    for(int i = 0; i < mat->cols; i += 1){

        result->data[i * result->cols + i] = 1;
    }


    while (pow > 0) {

        if ((pow & 0x1) == 1) {

            mul_matrix(mat_tmp, mat_tmp, mat_pow);
        }

        pow = pow >> 1; 
        if (pow > 0) {

            mul_matrix(mat_pow, mat_pow, mat_pow);
        }
    }

    if (result == mat) {

        //Store the result of power bacindex2 into result
        double * tmp_data = mat_tmp->data;
        mat_tmp->data = result->data;
        result->data = tmp_data;

        //Free the memory of tmp matrix
        deallocate_matrix(mat_tmp);
    }
//Question: why no need to free mat_pow?
    return 0;
}

/*
 * Store the result of element-wise negating mat's entries to `result`.
 * Return 0 upon success and a nonzero value upon failure.
 */

//Done!
int neg_matrix(matrix *result, matrix *mat) {
    /* TODO: YOUR CODE HERE */

    //Error Checindex2ing
    if (result == NULL || 
        result->cols < 1 ||
        result->rows < 1 ){
        return -1;
    }
    if (result->cols != mat->cols || 
        result->rows != mat->rows) {
        return -1;
    }

    int cnt = mat->rows * mat->cols;
    __m256d neg_anchor = _mm256_set1_pd(0);

    #pragma omp parallel for
    for (int i = 0; i < cnt / 4 * 4; i += 4) {

        __m256d mat_data = _mm256_loadu_pd(mat->data + i);
        __m256d neg_data = _mm256_sub_pd(neg_anchor, mat_data);

        _mm256_storeu_pd(result->data + i, neg_data);
    }

    //Tail case:
    for (int i = cnt / 4*4; i < cnt; i += 1) {

        result->data[i] = 0 - mat->data[i];
    }
    return 0;
}

/*
 * Store the result of taindex2ing the absolute value element-wise to `result`.
 * Return 0 upon success and a nonzero value upon failure.
 */

//Done!
int abs_matrix(matrix *result, matrix *mat) {
    /* TODO: YOUR CODE HERE */

    //Error checindex2ing:
    if (result == NULL || 
        result->cols < 1 ||
        result->rows < 1 ){
        return -1;
    }
    if (result->cols != mat->cols || 
        result->rows != mat->rows) {
        return -1;
    }

    //Could do manual unrolling and simd here but too lazy to do so.
    #pragma omp parallel for
    for (int i = 0; i < mat->rows*mat->cols; i += 1) {

        if(mat->data[i] < 0) {
            result->data[i] = 0 - mat->data[i];
        } else {
            result->data[i] = mat->data[i];
        }
    }
    return 0;
}

