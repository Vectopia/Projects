#include "numc.h"
#include <structmember.h>

static PyTypeObject Matrix61cType;

/* Helper functions for initalization of matrices and vectors */
/* Matrix(rows, cols, low, high). Fill a matrix random double values */
static int init_rand(PyObject *self, int rows, int cols, unsigned int seed, double low, double high) {
    matrix *new_mat;
    int alloc_failed = allocate_matrix(&new_mat, rows, cols);
    if (alloc_failed)
        return alloc_failed;
    rand_matrix(new_mat, seed, low, high);
    ((Matrix61c *)self)->mat = new_mat;
    ((Matrix61c *)self)->shape = PyTuple_Pack(2, PyLong_FromLong(rows), PyLong_FromLong(cols));
    return 0;
}

/* Matrix(rows, cols, val). Fill a matrix of dimension rows * cols with val*/
static int init_fill(PyObject *self, int rows, int cols, double val) {
    matrix *new_mat;
    int alloc_failed = allocate_matrix(&new_mat, rows, cols);
    if (alloc_failed)
        return alloc_failed;
    else {
        fill_matrix(new_mat, val);
        ((Matrix61c *)self)->mat = new_mat;
        ((Matrix61c *)self)->shape = PyTuple_Pack(2, PyLong_FromLong(rows), PyLong_FromLong(cols));
    }
    return 0;
}

/* Matrix(rows, cols, 1d_list). Fill a matrix with dimension rows * cols with 1d_list values */
static int init_1d(PyObject *self, int rows, int cols, PyObject *lst) {
    if (rows * cols != PyList_Size(lst)) {
        PyErr_SetString(PyExc_TypeError, "Incorrect number of elements in list");
        return -1;
    }
    matrix *new_mat;
    int alloc_failed = allocate_matrix(&new_mat, rows, cols);
    if (alloc_failed)
        return alloc_failed;
    int count = 0;
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            set(new_mat, i, j, PyFloat_AsDouble(PyList_GetItem(lst, count)));
            count++;
        }
    }
    ((Matrix61c *)self)->mat = new_mat;
    ((Matrix61c *)self)->shape = PyTuple_Pack(2, PyLong_FromLong(rows), PyLong_FromLong(cols));
    return 0;
}

/* Matrix(2d_list). Fill a matrix with dimension len(2d_list) * len(2d_list[0]) */
static int init_2d(PyObject *self, PyObject *lst) {
    int rows = PyList_Size(lst);
    if (rows == 0) {
        PyErr_SetString(PyExc_TypeError, "Cannot initialize numc.Matrix with an empty list");
        return -1;
    }
    int cols;
    if (!PyList_Check(PyList_GetItem(lst, 0))) {
        PyErr_SetString(PyExc_TypeError, "List values not valid");
        return -1;
    } else {
        cols = PyList_Size(PyList_GetItem(lst, 0));
    }
    for (int i = 0; i < rows; i++) {
        if (!PyList_Check(PyList_GetItem(lst, i)) || PyList_Size(PyList_GetItem(lst, i)) != cols) {
            PyErr_SetString(PyExc_TypeError, "List values not valid");
            return -1;
        }
    }
    matrix *new_mat;
    int alloc_failed = allocate_matrix(&new_mat, rows, cols);
    if (alloc_failed)
        return alloc_failed;
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            set(new_mat, i, j, PyFloat_AsDouble(PyList_GetItem(PyList_GetItem(lst, i), j)));
        }
    }
    ((Matrix61c *)self)->mat = new_mat;
    ((Matrix61c *)self)->shape = PyTuple_Pack(2, PyLong_FromLong(rows), PyLong_FromLong(cols));
    return 0;
}

/* This deallocation function is called when reference count is 0*/
static void Matrix61c_dealloc(Matrix61c *self) {
    // printf("de allocate \n");
    deallocate_matrix(self->mat);
    Py_TYPE(self)->tp_free(self);
}

/* For immutable types all initializations should take place in tp_new */
static PyObject *Matrix61c_new(PyTypeObject *type, PyObject *args, PyObject *kwds) {
    /* size of allocated memory is tp_basicsize + nitems*tp_itemsize*/
    Matrix61c *self = (Matrix61c *)type->tp_alloc(type, 0);
    return (PyObject *)self;
}

/* This matrix61c type is mutable, so needs init function. Return 0 on success otherwise -1 */
static int Matrix61c_init(PyObject *self, PyObject *args, PyObject *kwds) {
    /* Generate random matrices */
    if (kwds != NULL) {
        PyObject *rand = PyDict_GetItemString(kwds, "rand");
        if (!rand) {
            PyErr_SetString(PyExc_TypeError, "Invalid arguments");
            return -1;
        }
        if (!PyBool_Check(rand)) {
            PyErr_SetString(PyExc_TypeError, "Invalid arguments");
            return -1;
        }
        if (rand != Py_True) {
            PyErr_SetString(PyExc_TypeError, "Invalid arguments");
            return -1;
        }

        PyObject *low = PyDict_GetItemString(kwds, "low");
        PyObject *high = PyDict_GetItemString(kwds, "high");
        PyObject *seed = PyDict_GetItemString(kwds, "seed");
        double double_low = 0;
        double double_high = 1;
        unsigned int unsigned_seed = 0;

        if (low) {
            if (PyFloat_Check(low)) {
                double_low = PyFloat_AsDouble(low);
            } else if (PyLong_Check(low)) {
                double_low = PyLong_AsLong(low);
            }
        }

        if (high) {
            if (PyFloat_Check(high)) {
                double_high = PyFloat_AsDouble(high);
            } else if (PyLong_Check(high)) {
                double_high = PyLong_AsLong(high);
            }
        }

        if (double_low >= double_high) {
            PyErr_SetString(PyExc_TypeError, "Invalid arguments");
            return -1;
        }

        // Set seed if argument exists
        if (seed) {
            if (PyLong_Check(seed)) {
                unsigned_seed = PyLong_AsUnsignedLong(seed);
            }
        }

        PyObject *rows = NULL;
        PyObject *cols = NULL;
        if (PyArg_UnpackTuple(args, "args", 2, 2, &rows, &cols)) {
            if (rows && cols && PyLong_Check(rows) && PyLong_Check(cols)) {
                return init_rand(self, PyLong_AsLong(rows), PyLong_AsLong(cols), unsigned_seed, double_low, double_high);
            }
        } else {
            PyErr_SetString(PyExc_TypeError, "Invalid arguments");
            return -1;
        }
    }
    PyObject *arg1 = NULL;
    PyObject *arg2 = NULL;
    PyObject *arg3 = NULL;
    if (PyArg_UnpackTuple(args, "args", 1, 3, &arg1, &arg2, &arg3)) {
        /* arguments are (rows, cols, val) */
        if(arg1 && PyLong_Check(arg1) && PyLong_AsLong(arg1) <= 0){
            PyErr_SetString(PyExc_TypeError, "None possitive error");
        }
        if(arg2 && PyLong_Check(arg2) && PyLong_AsLong(arg2) <= 0){
            PyErr_SetString(PyExc_TypeError, "None possitive error");
        }
        if (arg1 && arg2 && arg3 && PyLong_Check(arg1) && PyLong_Check(arg2) && (PyLong_Check(arg3) || PyFloat_Check(arg3))) {
            if (PyLong_Check(arg3)) {
                return init_fill(self, PyLong_AsLong(arg1), PyLong_AsLong(arg2), PyLong_AsLong(arg3));
            }
            else
                return init_fill(self, PyLong_AsLong(arg1), PyLong_AsLong(arg2), PyFloat_AsDouble(arg3));
        } else if (arg1 && arg2 && arg3 && PyLong_Check(arg1) && PyLong_Check(arg2) && PyList_Check(arg3)) {
            /* Matrix(rows, cols, 1D list) */
            return init_1d(self, PyLong_AsLong(arg1), PyLong_AsLong(arg2), arg3);
        } else if (arg1 && PyList_Check(arg1) && arg2 == NULL && arg3 == NULL) {
            /* Matrix(rows, cols, 1D list) */
            return init_2d(self, arg1);
        } else if (arg1 && arg2 && PyLong_Check(arg1) && PyLong_Check(arg2) && arg3 == NULL) {
            /* Matrix(rows, cols, 1D list) */
            return init_fill(self, PyLong_AsLong(arg1), PyLong_AsLong(arg2), 0);
        } else {
            PyErr_SetString(PyExc_TypeError, "Invalid arguments");
            return -1;
        }
    } else {
        PyErr_SetString(PyExc_TypeError, "Invalid arguments");
        return -1;
    }
}

/* List of lists representations for matrices */
static PyObject *Matrix61c_to_list(Matrix61c *self) {
    int rows = self->mat->rows;
    int cols = self->mat->cols;
    PyObject *py_lst = PyList_New(rows);
    for (int i = 0; i < rows; i++) {
        PyList_SetItem(py_lst, i, PyList_New(cols));
        PyObject *curr_row = PyList_GetItem(py_lst, i);
        for (int j = 0; j < cols; j++) {
            PyList_SetItem(curr_row, j, PyFloat_FromDouble(get(self->mat, i, j)));
        }
    }
    return py_lst;
}

static PyObject *Matrix61c_class_to_list(Matrix61c *self, PyObject *args) {
    PyObject *mat = NULL;
    if (PyArg_UnpackTuple(args, "args", 1, 1, &mat)) {
        if (!PyObject_TypeCheck(mat, &Matrix61cType)) {
            PyErr_SetString(PyExc_TypeError, "Argument must of type numc.Matrix!");
            return NULL;
        }
        Matrix61c* mat61c = (Matrix61c*)mat;
        return Matrix61c_to_list(mat61c);
    } else {
        PyErr_SetString(PyExc_TypeError, "Invalid arguments");
        return NULL;
    }
}

/* Add class methods */
static PyMethodDef Matrix61c_class_methods[] = {
    {"to_list", (PyCFunction)Matrix61c_class_to_list, METH_VARARGS, "Returns a list representation of numc.Matrix"},
    {NULL, NULL, 0, NULL}
};

/* Matrix61c string representation. For printing purposes. */
static PyObject *Matrix61c_repr(PyObject *self) {
    PyObject *py_lst = Matrix61c_to_list((Matrix61c *)self);
    return PyObject_Repr(py_lst);
}

/* For __getitem__. (e.g. mat[0]) */
static PyObject *Matrix61c_subscript(Matrix61c* self, PyObject* key) {
    if (!PyLong_Check(key)) {
        PyErr_SetString(PyExc_TypeError, "Key is not valid");
        return NULL;
    }
    int index = PyLong_AsLong(key);
    if (index >= self->mat->rows || index < 0) {
        PyErr_SetString(PyExc_IndexError, "Index out of range");
        return NULL;
    }
    if (self->mat->cols == 1) { // if one single number, unwrap from list
        return PyFloat_FromDouble(get(self->mat, index, 0));
    }
    matrix *new_mat;
    int ref_failed = allocate_matrix_ref(&new_mat, self->mat, index * self->mat->cols, self->mat->cols, 1);
    if (ref_failed != 0) {
        return NULL;
    }
    Matrix61c* rv = (Matrix61c*) Matrix61c_new(&Matrix61cType, NULL, NULL);
    rv->mat = new_mat;
    rv->shape = PyTuple_Pack(2, PyLong_FromLong(new_mat->rows), PyLong_FromLong(1));
    return (PyObject*)rv;
}

/* For __setitem__ (e.g. mat[0] = 1) */
static int Matrix61c_set_subscript(Matrix61c* self, PyObject *key, PyObject *v) {
    if (!PyLong_Check(key)) {
        PyErr_SetString(PyExc_TypeError, "Key is not valid");
        return -1;
    }
    int index = PyLong_AsLong(key);
    if (index >= self->mat->rows || index < 0) {
        PyErr_SetString(PyExc_IndexError, "Index out of range");
        return -1;
    }
    int cols = self->mat->cols;
    if (cols == 1) {
        if (!PyFloat_Check(v) && !PyLong_Check(v)) {
            PyErr_SetString(PyExc_TypeError, "Value is not valid");
            return -1;
        }
        double val = PyFloat_AsDouble(v);
        set(self->mat, index, 0, val);
        return 0;
    } else {
        if (!PyList_Check(v)) {
            PyErr_SetString(PyExc_TypeError, "Value is not valid");
            return -1;
        }
        for (int i = 0; i < cols; i++) {
            if (!PyFloat_Check(PyList_GetItem(v, i)) && !PyLong_Check(PyList_GetItem(v, i))) {
                PyErr_SetString(PyExc_TypeError, "Value is not valid");
                return -1;
            }
            set(self->mat, index, i, PyFloat_AsDouble(PyList_GetItem(v, i)));
        }
        return 0;
    }
    return -1;
}

static PyMappingMethods Matrix61c_mapping = {
    NULL,
    (binaryfunc) Matrix61c_subscript,
    (objobjargproc) Matrix61c_set_subscript,
};

/* NUMBER METHODS */

/*
 * Adds two numc.Matrix (Matrix61c) objects together. The first operand is self, and
 * the second operand can be obtained by casting `args`.
 * You will have to check if the arguments' dimensions match and if the second operand is an
 * instance of Matrix61c, and throw a type error if anything is violated.
 */

static Matrix61c* new_matrix_with_data(int rows, int cols){
    matrix *new_mat;
    int ref_failed = allocate_matrix(&new_mat, rows, cols);

    if (ref_failed != 0) {
        return NULL;
    }

    Matrix61c* rv = (Matrix61c*) Matrix61c_new(&Matrix61cType, NULL, NULL);
    if (rv == NULL) {
        return NULL;
    }

    rv->mat = new_mat;
    rv->shape = PyTuple_Pack(2, PyLong_FromLong(new_mat->rows), PyLong_FromLong(new_mat->cols));
    return rv;
}

static PyObject *Matrix61c_add(Matrix61c* self, PyObject* args) {
    /* TODO: YOUR CODE HERE */
    
    Matrix61c* rv = new_matrix_with_data(self->mat->rows, self->mat->cols);

    if(add_matrix(rv->mat, self->mat, ((Matrix61c *)args)->mat)<0){
        PyErr_SetString(PyExc_TypeError, "dimension not match");
        return NULL;
    }

    return (PyObject*)rv;
}

/*
 * Subtracts the second numc.Matrix (Matrix61c) object from the first one. The first operand is
 * self, and the second operand can be obtained by casting `args`.
 * You will have to check if the arguments' dimensions match and if the second operand is an
 * instance of Matrix61c, and throw a type error if anything is violated.
 */
static PyObject *Matrix61c_sub(Matrix61c* self, PyObject* args) {
    /* TODO: YOUR CODE HERE */
    Matrix61c* rv = new_matrix_with_data(self->mat->rows, self->mat->cols);

    if(sub_matrix(rv->mat, self->mat, ((Matrix61c *)args)->mat)<0){
        PyErr_SetString(PyExc_TypeError, "dimension not match");
        return NULL;
    }

    return (PyObject*)rv;
}

/*
 * Multiplies two numc.Matrix (Matrix61c) objects together. The first operand is self, and
 * the second operand can be obtained by casting `args`.
 * You will have to check if the arguments' dimensions are valid and if the second operand is an
 * instance of Matrix61c, and throw a type error if anything is violated.
 */
static PyObject *Matrix61c_multiply(Matrix61c* self, PyObject *args) {
    /* TODO: YOUR CODE HERE */
    Matrix61c* rv = new_matrix_with_data(self->mat->rows, ((Matrix61c *)args)->mat->cols);

    if(mul_matrix(rv->mat, self->mat, ((Matrix61c *)args)->mat)<0){

        PyErr_SetString(PyExc_TypeError, "dimension not match");
        return NULL;
    }

    return (PyObject*)rv;
}

/*
 * Negates the given numc.Matrix (Matrix61c).
 */
static PyObject *Matrix61c_neg(Matrix61c* self) {
    /* TODO: YOUR CODE HERE */
    Matrix61c* rv = new_matrix_with_data(self->mat->rows, self->mat->cols);

    if(neg_matrix(rv->mat, self->mat)<0){

        PyErr_SetString(PyExc_TypeError, "dimension not match");
        return NULL;
    };

    return (PyObject*)rv;
}

/*
 * Take the element-wise absolute value of this numc.Matrix (Matrix61c).
 */
static PyObject *Matrix61c_abs(Matrix61c *self) {
    /* TODO: YOUR CODE HERE */
    Matrix61c* rv = new_matrix_with_data(self->mat->rows, self->mat->cols);

    if(abs_matrix(rv->mat, self->mat)<0){

        PyErr_SetString(PyExc_TypeError, "dimension not match");
        return NULL;
    }

    return (PyObject*)rv;
}

/*
 * Raise numc.Matrix (Matrix61c) to the `pow`th power. You can ignore the argument `optional`.
 */
static PyObject *Matrix61c_pow(Matrix61c *self, PyObject *pow, PyObject *optional) {
    /* TODO: YOUR CODE HERE */
    Matrix61c* rv = new_matrix_with_data(self->mat->rows, self->mat->cols);

    if(!PyLong_Check(pow)){

        PyErr_SetString(PyExc_TypeError, "dimension not match");
        return NULL;
    }

    long pow_l = PyLong_AsLong(pow);
    if(pow_l < 0){

        PyErr_SetString(PyExc_TypeError, "power can not be negative");
        return NULL;
    }

    if(pow_matrix(rv->mat, self->mat, pow_l)<0){

        PyErr_SetString(PyExc_TypeError, "power error");
        return NULL;
    }

    return (PyObject*)rv;
}

/*
 * Create a PyNumberMethods struct for overloading operators with all the number methods you have
 * define. You might find this link helpful: https://docs.python.org/3.6/c-api/typeobj.html
 */
static PyNumberMethods Matrix61c_as_number = {
    /* TODO: YOUR CODE HERE */

    .nb_add= (binaryfunc) &Matrix61c_add,
    .nb_subtract =(binaryfunc) &Matrix61c_sub,
    .nb_multiply=(binaryfunc) &Matrix61c_multiply,
    // binaryfunc nb_remainder;
    // binaryfunc nb_divmod;
    .nb_power=(ternaryfunc) &Matrix61c_pow,
    .nb_negative=(unaryfunc) &Matrix61c_neg,
    // unaryfunc nb_positive;
    .nb_absolute=(unaryfunc) &Matrix61c_abs,
    // inquiry nb_bool;
    // unaryfunc nb_invert;
    // binaryfunc nb_lshift;
    // binaryfunc nb_rshift;
    // binaryfunc nb_and;
    // binaryfunc nb_xor;
    // binaryfunc nb_or;
    // unaryfunc nb_int;
    // void *nb_reserved;
    // unaryfunc nb_float;
};


/* INSTANCE METHODS */
/*
 * Given a numc.Matrix self, parse `args` to (int) row, (int) col, and (double) val.
 * This function should return None in Python.
 */
static PyObject *Matrix61c_set_value(Matrix61c *self, PyObject* args) {
    /* TODO: YOUR CODE HERE */

    //Utilized the code from above sections
    //PyObject *arg1 = NULL;
    //PyObject *arg2 = NULL;
    //PyObject *arg3 = NULL;
    //if (PyArg_UnpackTuple(args, "args", 1, 3, &arg1, &arg2, &arg3)) {
        /* arguments are (rows, cols, val) */
        //if(arg1 && PyLong_Check(arg1) && PyLong_AsLong(arg1) <= 0){
            //PyErr_SetString(PyExc_TypeError, "None possitive error");
        //}
        //if(arg2 && PyLong_Check(arg2) && PyLong_AsLong(arg2) <= 0){
            //PyErr_SetString(PyExc_TypeError, "None possitive error");
        //}
        //if (arg1 && arg2 && arg3 && PyLong_Check(arg1) && PyLong_Check(arg2) && (PyLong_Check(arg3) || PyFloat_Check(arg3))) {
            //if (PyLong_Check(arg3)) {
                //return init_fill(self, PyLong_AsLong(arg1), PyLong_AsLong(arg2), PyLong_AsLong(arg3));
            //}
            //else
                //return init_fill(self, PyLong_AsLong(arg1), PyLong_AsLong(arg2), PyFloat_AsDouble(arg3));
        //} else if (arg1 && arg2 && arg3 && PyLong_Check(arg1) && PyLong_Check(arg2) && PyList_Check(arg3)) {
            /* Matrix(rows, cols, 1D list) */
            //return init_1d(self, PyLong_AsLong(arg1), PyLong_AsLong(arg2), arg3);
        //} else if (arg1 && PyList_Check(arg1) && arg2 == NULL && arg3 == NULL) {
            /* Matrix(rows, cols, 1D list) */
            //return init_2d(self, arg1);
        //} else if (arg1 && arg2 && PyLong_Check(arg1) && PyLong_Check(arg2) && arg3 == NULL) {
            /* Matrix(rows, cols, 1D list) */
            //return init_fill(self, PyLong_AsLong(arg1), PyLong_AsLong(arg2), 0);
        //} else {
            //PyErr_SetString(PyExc_TypeError, "Invalid arguments");
            //return -1;
        //}
    //} else {

    //Initialize intermediate objects
    PyObject *arg1 = NULL; PyObject *arg2 = NULL; PyObject *arg3 = NULL;

    //Check if arguments can be unpacked as in desired number, if not, throw argnum error
    if (PyArg_UnpackTuple(args, "args", 1, 3, &arg1, &arg2, &arg3)) {
    //  if(!PyArg_UnpackTuple(args, "ref", 3, 3, &objRow, &objCol, &objVal)){
    //      PyErr_SetString(PyExc_IndexError, "Invalid arguments");
    //      return NULL;
    //  }
    //  if(!(PySequence_Length(args) == 3)){
    //      PyErr_SetString(PyExc_IndexError, "Invalid arguments");
    //      return NULL;
    //  }
    //  if(!(PyLong_Check(objRow) && PyLong_Check(objCol) )){
    //      PyErr_SetString(PyExc_IndexError, "Invalid arguments");
    //      return NULL;
    //  }

        //Check if any given argument is empty, if any one is empty, throw argnum error
        if (arg1 && arg2 && arg3) {
            //Check if the type of arguments complies(DISCUSSED WITH MY ROOMATE BUT WROTE OUR OWN CODE).
            //If the type of arguments does not corresponds to the given type, then throw a type error. 
            //Note: row and val should be long, value could be long || float so make sure both are covered.
            if (PyLong_Check(arg1) && PyLong_Check(arg2) && 
                (PyFloat_Check(arg3) || PyLong_Check(arg3))) {

                    //Store row and col into ints by typecasting
                    int mat_row = (int) PyLong_AsLong(arg1);
                    int mat_col = (int) PyLong_AsLong(arg2);
                    //Store value-to-set into a double. 
                    //Note: Depending on the type we need to use different conversions
                    //if(PyLong_Check(tmpp3) != 0) {
                    //    double mat_val = PyLong_AsDouble(arg3);
                    //} else {
                    //    double mat_val = PyFloat_AsDouble(arg3);
                    //}
                    double mat_val = 0;
                    
                    if(PyLong_Check(arg3)){

                        mat_val = PyLong_AsDouble(arg3);
                    } else {

                        mat_val = PyFloat_AsDouble(arg3);
                    }
            //Dimension check: row and col cannot be negative, 
            //if any of them are negative, throw a non-positive error
            if (mat_row < 0 || mat_col < 0) {

                PyErr_SetString(PyExc_TypeError, "dimension error");
                return NULL;
            }

            //Dimension check: row and col cannot be out of bound
            //if any of them is out of bound, throw an index overflow error
            if(mat_row >= self->mat->rows || mat_col >= self->mat->cols) {

                PyErr_SetString(PyExc_IndexError, "index out of range");
                return NULL;
            }
                set(self->mat, mat_row, mat_col, mat_val);

            } else {

                PyErr_SetString(PyExc_TypeError, "argument type error");
                return NULL;
                }
        } else {

            PyErr_SetString(PyExc_TypeError, "argument number error");
            return NULL;
            }   
    } else {
        PyErr_SetString(PyExc_TypeError, "argument number error");
        return NULL;
        }

    return Py_None;

    //if(!PyArg_UnpackTuple(args, "ref", 3, 3, &objRow, &objCol, &objVal)){
    //    PyErr_SetString(PyExc_IndexError, "Invalid arguments");
    //    return NULL;
    //}
    //if(!(PySequence_Length(args) == 3)){
    //    PyErr_SetString(PyExc_IndexError, "Invalid arguments");
    //    return NULL;
    //}
    //if(!(PyLong_Check(objRow) && PyLong_Check(objCol) )){
    //    PyErr_SetString(PyExc_IndexError, "Invalid arguments");
    //    return NULL;
    //}
    //long row = PyLong_AsLong( objRow );
    //long col = PyLong_AsLong( objCol );
    // printf("FUCK %d, %d\n", row, col);
    //double val = PyFloat_AsDouble( objVal );
    //if(row <0 || row >= self->mat->rows || col < 0 || col >= self->mat->cols){
    //    PyErr_SetString(PyExc_IndexError, "dimension not match");
    //    return NULL;
    //}
    //set(self->mat, row, col, val);
    //return Py_BuildValue("s", NULL);
    // return NULL;
}

/*
 * Given a numc.Matrix `self`, parse `args` to (int) row and (int) col.
 * This function should return the value at the `row`th row and `col`th column, which is a Python
 * float.
 */
static PyObject *Matrix61c_get_value(Matrix61c *self, PyObject* args) {
    /* TODO: YOUR CODE HERE */
    //Get shared basically the same ideas in error checking with set.
    //Initialize intermediate objects
    PyObject *arg1 = NULL; PyObject *arg2 = NULL;

    if (PyArg_UnpackTuple(args, "args", 1, 2, &arg1, &arg2)) {
        /* arguments are (rows, cols) */
        if (arg1 && arg2) {
            //Check if the type of arguments complies(DISCUSSED WITH MY ROOMATE BUT WROTE OUR OWN CODE).
            //If the type of arguments does not corresponds to the given type, then throw a type error. 
            //Note: row and val should be long
            if (PyLong_Check(arg1) && PyLong_Check(arg2)) {

                //long row = PyLong_AsLong( objRow );
                //long col = PyLong_AsLong( objCol );
                // printf("FUCK %d, %d\n", row, col);
                //double val = PyFloat_AsDouble( objVal );
                //if(row <0 || row >= self->mat->rows || col < 0 || col >= self->mat->cols){
                //    PyErr_SetString(PyExc_IndexError, "dimension not match");
                //    return NULL;
                //}
                //set(self->mat, row, col, val);
                //return Py_BuildValue("s", NULL);
                // return NULL;
                int mat_row = (int) PyLong_AsLong(arg1);
                int mat_col = (int) PyLong_AsLong(arg2);

            //Dimension check: row and col cannot be negative, 
            //if any of them are negative, throw a non-positive error
            if (mat_row < 0 || mat_col < 0) {

                PyErr_SetString(PyExc_TypeError, "dimension error");
                return NULL;
            }

            //Dimension check: row and col cannot be out of bound
            //if any of them is out of bound, throw an index overflow error
            if(mat_row >= self->mat->rows || 
                mat_col >= self->mat->cols) {

                PyErr_SetString(PyExc_IndexError, "index out of range");
                return NULL;
            }

            //Use an intermediate to hold the get result and then return the result

            //if(!PyArg_UnpackTuple(args, "ref", 3, 3, &objRow, &objCol, &objVal)){
            //    PyErr_SetString(PyExc_IndexError, "Invalid arguments");
            //    return NULL;
            //}
            //if(!(PySequence_Length(args) == 3)){
            //    PyErr_SetString(PyExc_IndexError, "Invalid arguments");
            //    return NULL;
            //}
            //if(!(PyLong_Check(objRow) && PyLong_Check(objCol) )){
            //    PyErr_SetString(PyExc_IndexError, "Invalid arguments");
            //    return NULL;
            //}
            double mat_tmp = get(self->mat, mat_row, mat_col);
            return PyFloat_FromDouble(mat_tmp);

            } else {

                PyErr_SetString(PyExc_TypeError, "argument type error");
                return NULL;
                }
        } else {

            PyErr_SetString(PyExc_TypeError, "argument number error");
            return NULL;
            }
    } else {

        PyErr_SetString(PyExc_TypeError, "argument number error");
        return NULL;
    }

    //if(!PyArg_UnpackTuple(args, "ref", 3, 3, &objRow, &objCol, &objVal)){
    //    PyErr_SetString(PyExc_IndexError, "Invalid arguments");
    //    return NULL;
    //}
    //if(!(PySequence_Length(args) == 3)){
    //    PyErr_SetString(PyExc_IndexError, "Invalid arguments");
    //    return NULL;
    //}
    //if(!(PyLong_Check(objRow) && PyLong_Check(objCol) )){
    //    PyErr_SetString(PyExc_IndexError, "Invalid arguments");
    //    return NULL;
    //}
}

/*
 * Create an array of PyMethodDef structs to hold the instance methods.
 * Name the python function corresponding to Matrix61c_get_value as "get" and Matrix61c_set_value
 * as "set"
 * You might find this link helpful: https://docs.python.org/3.6/c-api/structures.html
 */
static PyMethodDef Matrix61c_methods[] = {
    /* TODO: YOUR CODE HERE */
    {"get", (PyCFunction)Matrix61c_get_value, METH_VARARGS, "getter func"},
    {"set", (PyCFunction)Matrix61c_set_value, METH_VARARGS, "setter func"},
    {NULL, NULL, 0, NULL},
};

/* INSTANCE ATTRIBUTES*/
static PyMemberDef Matrix61c_members[] = {
    {"shape", T_OBJECT_EX, offsetof(Matrix61c, shape), 0,
     "(rows, cols)"},
    {NULL}  /* Sentinel */
};

static PyTypeObject Matrix61cType = {
    PyVarObject_HEAD_INIT(NULL, 0)
    .tp_name = "numc.Matrix",
    .tp_basicsize = sizeof(Matrix61c),
    .tp_dealloc = (destructor)Matrix61c_dealloc,
    .tp_repr = (reprfunc)Matrix61c_repr,
    .tp_as_number = &Matrix61c_as_number,
    .tp_flags = Py_TPFLAGS_DEFAULT |
        Py_TPFLAGS_BASETYPE,
    .tp_doc = "numc.Matrix objects",
    .tp_methods = Matrix61c_methods,
    .tp_members = Matrix61c_members,
    .tp_as_mapping = &Matrix61c_mapping,
    .tp_init = (initproc)Matrix61c_init,
    .tp_new = Matrix61c_new
};


static struct PyModuleDef numcmodule = {
    PyModuleDef_HEAD_INIT,
    "numc",
    "Numc matrix operations",
    -1,
    Matrix61c_class_methods
};

/* Initialize the numc module */
PyMODINIT_FUNC PyInit_numc(void) {
    PyObject* m;

    if (PyType_Ready(&Matrix61cType) < 0)
        return NULL;

    m = PyModule_Create(&numcmodule);
    if (m == NULL)
        return NULL;

    Py_INCREF(&Matrix61cType);
    PyModule_AddObject(m, "Matrix", (PyObject *)&Matrix61cType);
    printf("CS61C Summer 2020 Project 4: numc imported!\n");
    fflush(stdout);
    return m;
}