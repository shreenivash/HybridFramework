package com.qa.framework.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

import java.util.List;

/**
 * Rewrites the TestNG suite at runtime when {@code -DparallelMode} is set, so consumers can skip the CLI generator.
 */
public class RuntimeSuiteAlterer implements IAlterSuiteListener {

    private static final Logger LOG = LoggerFactory.getLogger(RuntimeSuiteAlterer.class);

    @Override
    public void alter(List<XmlSuite> suites) {
        String mode = System.getProperty("parallelMode");
        if (mode == null || mode.isBlank()) {
            return;
        }
        XmlSuite generated = TestNGXmlGenerator.generate();
        suites.clear();
        suites.add(generated);
        LOG.info("Replaced TestNG suite with runtime {} suite (thread-count={})", mode, generated.getThreadCount());
    }
}
