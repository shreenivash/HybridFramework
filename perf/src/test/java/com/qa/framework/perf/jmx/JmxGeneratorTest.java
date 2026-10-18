package com.qa.framework.perf.jmx;

import com.qa.framework.perf.journey.JourneyLoader;
import com.qa.framework.perf.model.LoadProfile;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JmxGeneratorTest {

    @Test
    public void generatesFixedTpsTimerWithJexl() throws Exception {
        var journey = JourneyLoader.loadClasspath("journeys/sample-journey.yaml");
        String jmx = JmxGenerator.generate(journey, new LoadProfile(50, 200, 600, 60, 500));
        Assert.assertTrue(jmx.contains("ConstantThroughputTimer"));
        Assert.assertTrue(jmx.contains("__jexl3(${__P(tps,50)} * 60)"));
        Assert.assertTrue(jmx.contains("${__P(users,200)}"));
        Assert.assertTrue(jmx.contains("POST"));
        Assert.assertTrue(jmx.contains("JSONPostProcessor"));
    }
}
