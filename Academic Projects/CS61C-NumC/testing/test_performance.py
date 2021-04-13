"""
Feel free to add more test classes and/or tests that are not provided by the skeleton code!
Make sure you follow these naming conventions: https://docs.pytest.org/en/reorganize-docs/goodpractices.html#test-discovery
for your new tests/classes/python files or else they might be skipped.
"""
from utils import *
import time
import pytest
"""
- We will test you on your performance on add, sub, abs, neg, mul, and pow, so make sure to test these
yourself! We will also test your implementation on matrices on different sizes. It is normal if
your smaller matrices are about the same speed as the naive implementation or even slower.
- Use time.time(), NOT time.perf_counter() to time your program!
- DO NOT count the time for initializing matrices into your performance time. Only count the
time the part where operations are carried out.
- Please also check for correctness while testing for performance!
- We provide the structure for test_small_add. All other tests should have similar structures
"""


def _binary_op_( func, is_numc=True, size=100):
    def _wrapper_(self, benchmark):
        a1, b1 = rand_dp_nc_matrix(size,size)
        a2, b2 = rand_dp_nc_matrix(size,size)
        if is_numc:
            return benchmark(func, b1, b2)
        else:
            return benchmark(func, a1, a2)
    return _wrapper_

def _pow_op(pow, is_numc, size):
    def _wrapper_(self, benchmark):
        a1, b1 = rand_dp_nc_matrix(size,size)
        if is_numc:
            return benchmark(lambda x,y: x ** y, b1, pow)
        else:
            return benchmark(lambda x,y: x ** y, a1, pow)
    return _wrapper_


@pytest.mark.benchmark(group="TestAddSmallPerformance")
class TestAddSmallPerformance:
    test_add_numc=_binary_op_(lambda a,b: a+b, True, 10)
    test_add_dumbpy=_binary_op_(lambda a,b: a+b, False, 10)

@pytest.mark.benchmark(group="TestAddMidPerformance")
class TestAddMidPerformance:
    test_add_numc=_binary_op_(lambda a,b: a+b, True, 100)
    test_add_dumbpy=_binary_op_(lambda a,b: a+b, False, 100)

@pytest.mark.benchmark(group="TestAddLargePerformance")
class TestAddLargePerformance:
    test_add_numc=_binary_op_(lambda a,b: a+b, True, 1000)
    test_add_dumbpy=_binary_op_(lambda a,b: a+b, False, 1000)


@pytest.mark.benchmark(group="TestSubSmallPerformance")
class TestSubSmallPerformance:
    test_sub_numc=_binary_op_(lambda a,b: a-b, True, 10)
    test_sub_dumbpy=_binary_op_(lambda a,b: a-b, False, 10)

@pytest.mark.benchmark(group="TestSubMidPerformance")
class TestSubMidPerformance:
    test_sub_numc=_binary_op_(lambda a,b: a-b, True, 100)
    test_sub_dumbpy=_binary_op_(lambda a,b: a-b, False, 100)

@pytest.mark.benchmark(group="TestSubLargePerformance")
class TestSubLargePerformance:
    test_sub_numc=_binary_op_(lambda a,b: a-b, True, 1000)
    test_sub_dumbpy=_binary_op_(lambda a,b: a-b, False, 1000)


@pytest.mark.benchmark(group="TestMulSmallPerformance")
class TestMulSmallPerformance:
    test_mul_numc=_binary_op_(lambda a,b: a*b, True, 10)
    test_mul_dumbpy=_binary_op_(lambda a,b: a*b, False, 10)

@pytest.mark.benchmark(group="TestMulMidPerformance")
class TestMulMidPerformance:
    test_mul_numc=_binary_op_(lambda a,b: a*b, True, 100)
    test_mul_dumbpy=_binary_op_(lambda a,b: a*b, False, 100)

@pytest.mark.benchmark(group="TestMulLargePerformance")
class TestMulLargePerformance:
    test_mul_numc=_binary_op_(lambda a,b: a*b, True, 500)
    test_mul_dumbpy=_binary_op_(lambda a,b: a*b, False, 500)


@pytest.mark.benchmark(group="TestPowLargePerformance")
class TestPowLargePerformance:
    test_pow_numc=_pow_op(1234, True, 10)
    test_pow_dumbpy=_pow_op(1234, False, 10)


# class TestSubPerformance:
#     def test_small_sub(self):
#         # TODO: YOUR CODE HERE
#         pass

#     def test_medium_sub(self):
#         # TODO: YOUR CODE HERE
#         pass

#     def test_large_sub(self):
#         # TODO: YOUR CODE HERE
#         pass

# class TestAbsPerformance:
#     def test_small_abs(self):
#         # TODO: YOUR CODE HERE
#         pass

#     def test_medium_abs(self):
#         # TODO: YOUR CODE HERE
#         pass

#     def test_large_abs(self):
#         # TODO: YOUR CODE HERE
#         pass

# class TestNegPerformance:
#     def test_small_neg(self):
#         # TODO: YOUR CODE HERE
#         pass

#     def test_medium_neg(self):
#         # TODO: YOUR CODE HERE
#         pass

#     def test_large_neg(self):
#         # TODO: YOUR CODE HERE
#         pass

# class TestMulPerformance:
#     def test_small_mul(self):
#         # TODO: YOUR CODE HERE
#         pass

#     def test_medium_mul(self):
#         # TODO: YOUR CODE HERE
#         pass

#     def test_large_mul(self):
#         # TODO: YOUR CODE HERE
#         pass

# class TestPowPerformance:
#     def test_small_pow(self):
#         # TODO: YOUR CODE HERE
#         pass

#     def test_medium_pow(self):
#         # TODO: YOUR CODE HERE
#         pass

#     def test_large_pow(self):
#         # TODO: YOUR CODE HERE
#         pass
