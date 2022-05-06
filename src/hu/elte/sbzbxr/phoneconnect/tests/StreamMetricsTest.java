package hu.elte.sbzbxr.phoneconnect.tests;

import hu.elte.sbzbxr.phoneconnect.model.connection.DroppedFrameMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StreamMetricsTest {
    DroppedFrameMetrics metrics;

    @BeforeEach
    void setUp() {
        metrics= new DroppedFrameMetrics();
    }

    @Test
    void test() {
        assertEquals(0,metrics.getMetrics());
        metrics.arrivedPicture("a",2);
        metrics.arrivedPicture("a",3);
        metrics.arrivedPicture("a",5);
        assertEquals(25,metrics.getMetrics());
    }

    @Test
    void resetTest() {
        test();
        metrics.reset();
        assertEquals(0,metrics.getMetrics());
        metrics.arrivedPicture("b",92);
        metrics.arrivedPicture("b",93);
        metrics.arrivedPicture("b",95);
        assertEquals(25,metrics.getMetrics());
    }
}