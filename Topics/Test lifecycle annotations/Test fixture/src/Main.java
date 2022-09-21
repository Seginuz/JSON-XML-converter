class SampleClassTests {
    // Your task is to setup TestUtils and instantiate the instance field.
    SampleClass instance;

    // @BeforeAll
    static void beforeAll() {
        TestUtils.timeConsumingSetup();
    }

    // @AfterAll
    static void afterAll() {

    }

    // @BeforeEach
    void beforeEach() {
        instance = TestUtils.getSampleClassInstance();
    }

    // @AfterEach
    void afterEach() {

    }

    // @Test
    void testMethodOne() {
        Assertions.assertTrue(instance.methodOne());
    }

    // @Test
    void testMethodTwo() {
        Assertions.assertTrue(instance.methodTwo());
    }
}