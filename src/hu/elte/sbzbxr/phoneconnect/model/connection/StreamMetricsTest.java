package hu.elte.sbzbxr.phoneconnect.model.connection;

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
        metrics.arrivedPicture("a__part2.jpg");
        metrics.arrivedPicture("a__part3.jpg");
        metrics.arrivedPicture("a__part5.jpg");
        assertEquals(25,metrics.getMetrics());
    }

    @Test
    void resetTest() throws InterruptedException {
        test();
        metrics.reset();
        assertEquals(0,metrics.getMetrics());
        metrics.arrivedPicture("b__part92.jpg");
        metrics.arrivedPicture("b__part93.jpg");
        metrics.arrivedPicture("b__part95.jpg");
        assertEquals(25,metrics.getMetrics());
    }
}