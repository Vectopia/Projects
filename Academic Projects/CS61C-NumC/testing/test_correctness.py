"""
Feel free to add more test classes and/or tests that are not provided by the skeleton code!
Make sure you follow these naming conventions: https://docs.pytest.org/en/reorganize-docs/goodpractices.html#test-discovery
for your new tests/classes/python files or else they might be skipped.
"""
from utils import *

"""
For each operation, you should write tests to test correctness on matrices of different sizes.
Hint: use rand_dp_nc_matrix to generate dumbpy and numc matrices with the same data and use
      cmp_dp_nc_ma1trix to compare the results
"""
class TestAddCorrectness:
    def test_small_add(self):
        # TODO: YOUR CODE HERE
        db, dc = rand_dp_nc_matrix(10,10)
        db2, dc2 = rand_dp_nc_matrix(10,10)
        db3 = db + db2
        dc3 = dc + dc2
        assert db3 == dc3
        assert db == dc
        db[0][0] = 10
        assert db[0][0] == 10
        assert db != dc
        db3[0][0] += 1
        assert db3 != dc3
        pass

    def test_medium_add(self):
        db, dc = rand_dp_nc_matrix(100,100)
        db2, dc2 = rand_dp_nc_matrix(100,100)
        db3 = db + db2
        dc3 = dc + dc2
        assert db3 == dc3

        pass

    def test_large_add(self):
        db, dc = rand_dp_nc_matrix(1000,1000)
        db2, dc2 = rand_dp_nc_matrix(1000,1000)
        db3 = db + db2
        dc3 = dc + dc2
        assert db3 == dc3

class TestSubCorrectness:
    def test_small_sub(self):
        db, dc = rand_dp_nc_matrix(10,10)
        db2, dc2 = rand_dp_nc_matrix(10,10)
        db3 = db - db2
        dc3 = dc - dc2
        assert db3 == dc3

    def test_medium_sub(self):
        db, dc = rand_dp_nc_matrix(100,100)
        db2, dc2 = rand_dp_nc_matrix(100,100)
        db3 = db - db2
        dc3 = dc - dc2
        assert db3 == dc3

    def test_large_sub(self):
        db, dc = rand_dp_nc_matrix(1000,1000)
        db2, dc2 = rand_dp_nc_matrix(1000,1000)
        db3 = db - db2
        dc3 = dc - dc2
        assert db3 == dc3

class TestAbsCorrectness:
    def test_small_abs(self):
        db, dc = rand_dp_nc_matrix(10,100)
        db3 = abs(db)
        dc3 = abs(dc)
        assert db3 == dc3

    def test_medium_abs(self):
        db, dc = rand_dp_nc_matrix(10,100)
        db3 = abs(db)
        dc3 = abs(dc)
        assert db3 == dc3

    def test_large_abs(self):
        db, dc = rand_dp_nc_matrix(1000,1000)
        db3 = abs(db)
        dc3 = abs(dc)
        assert db3 == dc3

class TestNegCorrectness:
    def test_small_neg(self):
        db, dc = rand_dp_nc_matrix(10,10)
        db3 = - db
        dc3 = - dc
        assert db3 == dc3

    def test_medium_neg(self):
        db, dc = rand_dp_nc_matrix(10,100)
        db3 = -db
        dc3 = -dc
        assert db3 == dc3

    def test_large_neg(self):
        db, dc = rand_dp_nc_matrix(1000,1000)
        db3 = -db
        dc3 = -dc
        assert db3 == dc3

class TestMulCorrectness:
    def test_small_mul(self):
        db, dc = rand_dp_nc_matrix(5,10)
        db2, dc2 = rand_dp_nc_matrix(10,5)
        db3 = db * db2
        dc3 = dc * dc2
        assert db3 == dc3

    def test_medium_mul(self):
        db, dc = rand_dp_nc_matrix(100,100)
        db2, dc2 = rand_dp_nc_matrix(100,100)
        db3 = db * db2
        dc3 = dc * dc2
        assert db3 == dc3

    def test_large_mul(self):
        db, dc = rand_dp_nc_matrix(200,200)
        db2, dc2 = rand_dp_nc_matrix(200,200)
        db3 = db * db2
        dc3 = dc * dc2
        assert db3 == dc3

class TestPowCorrectness:
    def test_small_pow(self):
        db, dc = rand_dp_nc_matrix(10,10)
        db3 = db ** 10 
        dc3 = dc ** 10
        assert db3 == dc3

    def test_medium_pow(self):
        db, dc = rand_dp_nc_matrix(20,20)
        db3 = db ** 10 
        dc3 = dc ** 10
        assert db3 == dc3

    def test_large_pow(self):
        db, dc = rand_dp_nc_matrix(100,100)
        db3 = db ** 5 
        dc3 = dc ** 5
        assert db3 == dc3

class TestGetCorrectness:
    def test_get(self):
        db, dc = rand_dp_nc_matrix(50,50)

        for i in range(50):
            for j in range(50):
                assert db.get(i,j) == dc.get(i,j)
                dc.set(i,j,i*j)
        for i in range(50):
            for j in range(50):
                dc.set(i,j,i*j)
        for i in range(50):
            for j in range(50):
                dc.get(i,j) == i*j

class TestSetCorrectness:
    def test_set(self):
        # TODO: YOUR CODE HERE
        pass
