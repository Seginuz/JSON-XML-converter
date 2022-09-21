class CalculatorTest {
    void testAddition() {
        Calculator calculator = new Calculator();
        int a = 123;
        int b = 321;

        int expected = 444;
        int result = calculator.add(a, b);

        Assertions.assertEquals(expected, result);
    }
}