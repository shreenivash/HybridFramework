package com.qa.framework.perf.jmx;

import com.qa.framework.perf.model.ApiJourney;
import com.qa.framework.perf.model.JourneyStep;
import com.qa.framework.perf.model.LoadProfile;

import java.util.Map;

/**
 * Builds a JMeter 5.x {@code .jmx} test plan: thread group, HTTP journey samplers, JSON extractors,
 * and a {@link ConstantThroughputTimer} driven by {@code -Jtps} via JEXL.
 */
public final class JmxGenerator {

    private JmxGenerator() {
    }

    public static String generate(ApiJourney journey, LoadProfile profile) {
        BaseUrlParts base = BaseUrlParts.parse(journey.getBaseUrl());
        StringBuilder jmx = new StringBuilder(16_384);
        jmx.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        jmx.append("<jmeterTestPlan version=\"1.2\" properties=\"5.0\" jmeter=\"5.6.3\">\n");
        jmx.append("  <hashTree>\n");
        appendTestPlan(jmx, journey.getName());
        jmx.append("    <hashTree>\n");
        appendThreadGroup(jmx, profile);
        jmx.append("      <hashTree>\n");
        appendHttpDefaults(jmx, base);
        jmx.append("        <hashTree/>\n");
        appendUserDefinedVariables(jmx, journey.getVariables());
        jmx.append("        <hashTree/>\n");
        appendHeaderManager(jmx);
        jmx.append("        <hashTree/>\n");

        for (JourneyStep step : journey.getSteps()) {
            appendHttpSampler(jmx, step);
            jmx.append("        <hashTree>\n");
            if (step.getExtractJson() != null) {
                for (Map.Entry<String, String> extract : step.getExtractJson().entrySet()) {
                    appendJsonExtractor(jmx, extract.getKey(), extract.getValue());
                    jmx.append("          <hashTree/>\n");
                }
            }
            jmx.append("        </hashTree>\n");
        }

        appendConstantThroughputTimer(jmx, profile);
        jmx.append("        <hashTree/>\n");
        appendResultCollector(jmx);
        jmx.append("        <hashTree/>\n");

        jmx.append("      </hashTree>\n");
        jmx.append("    </hashTree>\n");
        jmx.append("  </hashTree>\n");
        jmx.append("</jmeterTestPlan>\n");
        return jmx.toString();
    }

    private static void appendTestPlan(StringBuilder jmx, String name) {
        jmx.append("    <TestPlan guiclass=\"TestPlanGui\" testclass=\"TestPlan\" testname=\"")
                .append(JmxEscaper.xml(name)).append("\" enabled=\"true\">\n");
        jmx.append("      <boolProp name=\"TestPlan.functional_mode\">false</boolProp>\n");
        jmx.append("      <boolProp name=\"TestPlan.serialize_threadgroups\">false</boolProp>\n");
        jmx.append("      <elementProp name=\"TestPlan.user_defined_variables\" elementType=\"Arguments\" "
                + "guiclass=\"ArgumentsPanel\" testclass=\"Arguments\" enabled=\"true\">\n");
        jmx.append("        <collectionProp name=\"Arguments.arguments\"/>\n");
        jmx.append("      </elementProp>\n");
        jmx.append("    </TestPlan>\n");
    }

    private static void appendThreadGroup(StringBuilder jmx, LoadProfile profile) {
        jmx.append("      <ThreadGroup guiclass=\"ThreadGroupGui\" testclass=\"ThreadGroup\" "
                + "testname=\"Load - ${__P(users)} users @ ${__P(tps)} TPS\" enabled=\"true\">\n");
        jmx.append("        <stringProp name=\"ThreadGroup.num_threads\">${__P(users,")
                .append(profile.users()).append(")}</stringProp>\n");
        jmx.append("        <stringProp name=\"ThreadGroup.ramp_time\">${__P(rampUp,")
                .append(profile.rampUpSeconds()).append(")}</stringProp>\n");
        jmx.append("        <boolProp name=\"ThreadGroup.scheduler\">true</boolProp>\n");
        jmx.append("        <stringProp name=\"ThreadGroup.duration\">${__P(duration,")
                .append(profile.durationSeconds()).append(")}</stringProp>\n");
        jmx.append("        <longProp name=\"ThreadGroup.delay\">0</longProp>\n");
        jmx.append("        <boolProp name=\"ThreadGroup.same_user_on_next_iteration\">true</boolProp>\n");
        jmx.append("        <elementProp name=\"ThreadGroup.main_controller\" elementType=\"LoopController\" "
                + "guiclass=\"LoopControlPanel\" testclass=\"LoopController\" enabled=\"true\">\n");
        jmx.append("          <boolProp name=\"LoopController.continue_forever\">true</boolProp>\n");
        jmx.append("          <intProp name=\"LoopController.loops\">-1</intProp>\n");
        jmx.append("        </elementProp>\n");
        jmx.append("      </ThreadGroup>\n");
    }

    private static void appendHttpDefaults(StringBuilder jmx, BaseUrlParts base) {
        jmx.append("        <ConfigTestElement guiclass=\"HttpDefaultsGui\" testclass=\"ConfigTestElement\" "
                + "testname=\"HTTP Request Defaults\" enabled=\"true\">\n");
        jmx.append("          <elementProp name=\"HTTPsampler.Arguments\" elementType=\"Arguments\" "
                + "guiclass=\"HTTPArgumentsPanel\" testclass=\"Arguments\" enabled=\"true\">\n");
        jmx.append("            <collectionProp name=\"Arguments.arguments\"/>\n");
        jmx.append("          </elementProp>\n");
        jmx.append("          <stringProp name=\"HTTPSampler.domain\">").append(JmxEscaper.xml(base.domain()))
                .append("</stringProp>\n");
        jmx.append("          <stringProp name=\"HTTPSampler.port\">").append(JmxEscaper.xml(base.port()))
                .append("</stringProp>\n");
        jmx.append("          <stringProp name=\"HTTPSampler.protocol\">").append(JmxEscaper.xml(base.protocol()))
                .append("</stringProp>\n");
        jmx.append("          <stringProp name=\"HTTPSampler.path\"></stringProp>\n");
        jmx.append("        </ConfigTestElement>\n");
    }

    private static void appendUserDefinedVariables(StringBuilder jmx, Map<String, String> variables) {
        if (variables == null || variables.isEmpty()) {
            return;
        }
        jmx.append("        <Arguments guiclass=\"ArgumentsPanel\" testclass=\"Arguments\" "
                + "testname=\"Journey variables\" enabled=\"true\">\n");
        jmx.append("          <collectionProp name=\"Arguments.arguments\">\n");
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            jmx.append("            <elementProp name=\"").append(JmxEscaper.xml(entry.getKey()))
                    .append("\" elementType=\"Argument\">\n");
            jmx.append("              <stringProp name=\"Argument.name\">").append(JmxEscaper.xml(entry.getKey()))
                    .append("</stringProp>\n");
            jmx.append("              <stringProp name=\"Argument.value\">").append(JmxEscaper.xml(entry.getValue()))
                    .append("</stringProp>\n");
            jmx.append("              <stringProp name=\"Argument.metadata\">=</stringProp>\n");
            jmx.append("            </elementProp>\n");
        }
        jmx.append("          </collectionProp>\n");
        jmx.append("        </Arguments>\n");
    }

    private static void appendHeaderManager(StringBuilder jmx) {
        jmx.append("        <HeaderManager guiclass=\"HeaderPanel\" testclass=\"HeaderManager\" "
                + "testname=\"Default headers\" enabled=\"true\">\n");
        jmx.append("          <collectionProp name=\"HeaderManager.headers\">\n");
        jmx.append("            <elementProp name=\"\" elementType=\"Header\">\n");
        jmx.append("              <stringProp name=\"Header.name\">Content-Type</stringProp>\n");
        jmx.append("              <stringProp name=\"Header.value\">application/json</stringProp>\n");
        jmx.append("            </elementProp>\n");
        jmx.append("            <elementProp name=\"\" elementType=\"Header\">\n");
        jmx.append("              <stringProp name=\"Header.name\">Accept</stringProp>\n");
        jmx.append("              <stringProp name=\"Header.value\">application/json</stringProp>\n");
        jmx.append("            </elementProp>\n");
        jmx.append("          </collectionProp>\n");
        jmx.append("        </HeaderManager>\n");
    }

    private static void appendHttpSampler(StringBuilder jmx, JourneyStep step) {
        String label = step.getName() != null ? step.getName() : step.getMethod() + " " + step.getPath();
        jmx.append("        <HTTPSamplerProxy guiclass=\"HttpTestSampleGui\" testclass=\"HTTPSamplerProxy\" "
                + "testname=\"").append(JmxEscaper.xml(label)).append("\" enabled=\"true\">\n");
        jmx.append("          <elementProp name=\"HTTPsampler.Arguments\" elementType=\"Arguments\" "
                + "guiclass=\"HTTPArgumentsPanel\" testclass=\"Arguments\" enabled=\"true\">\n");
        if (step.getBody() != null && !step.getBody().isBlank()) {
            jmx.append("            <collectionProp name=\"Arguments.arguments\">\n");
            jmx.append("              <elementProp name=\"\" elementType=\"HTTPArgument\">\n");
            jmx.append("                <boolProp name=\"HTTPArgument.always_encode\">false</boolProp>\n");
            jmx.append("                <stringProp name=\"Argument.value\">").append(JmxEscaper.xml(step.getBody()))
                    .append("</stringProp>\n");
            jmx.append("                <stringProp name=\"Argument.metadata\">=</stringProp>\n");
            jmx.append("              </elementProp>\n");
            jmx.append("            </collectionProp>\n");
        } else {
            jmx.append("            <collectionProp name=\"Arguments.arguments\"/>\n");
        }
        jmx.append("          </elementProp>\n");
        jmx.append("          <stringProp name=\"HTTPSampler.path\">").append(JmxEscaper.xml(step.getPath()))
                .append("</stringProp>\n");
        jmx.append("          <stringProp name=\"HTTPSampler.method\">").append(JmxEscaper.xml(step.getMethod()))
                .append("</stringProp>\n");
        jmx.append("          <boolProp name=\"HTTPSampler.postBodyRaw\">")
                .append(step.getBody() != null && !step.getBody().isBlank()).append("</boolProp>\n");
        jmx.append("          <boolProp name=\"HTTPSampler.follow_redirects\">true</boolProp>\n");
        jmx.append("          <boolProp name=\"HTTPSampler.use_keepalive\">true</boolProp>\n");
        jmx.append("        </HTTPSamplerProxy>\n");
    }

    private static void appendJsonExtractor(StringBuilder jmx, String varName, String jsonPath) {
        jmx.append("          <JSONPostProcessor guiclass=\"JSONPostProcessorGui\" testclass=\"JSONPostProcessor\" "
                + "testname=\"Extract ").append(JmxEscaper.xml(varName)).append("\" enabled=\"true\">\n");
        jmx.append("            <stringProp name=\"JSONPostProcessor.referenceNames\">")
                .append(JmxEscaper.xml(varName)).append("</stringProp>\n");
        jmx.append("            <stringProp name=\"JSONPostProcessor.jsonPathExprs\">")
                .append(JmxEscaper.xml(jsonPath)).append("</stringProp>\n");
        jmx.append("            <stringProp name=\"JSONPostProcessor.match_numbers\">1</stringProp>\n");
        jmx.append("          </JSONPostProcessor>\n");
    }

    private static void appendConstantThroughputTimer(StringBuilder jmx, LoadProfile profile) {
        jmx.append("        <ConstantThroughputTimer guiclass=\"TestBeanGUI\" testclass=\"ConstantThroughputTimer\" "
                + "testname=\"Fixed TPS (${__P(tps)} via JEXL)\" enabled=\"true\">\n");
        jmx.append("          <intProp name=\"calcMode\">2</intProp>\n");
        jmx.append("          <doubleProp>\n");
        jmx.append("            <name>throughput</name>\n");
        jmx.append("            <value>${__jexl3(${__P(tps,").append((int) profile.tps()).append(")} * 60)}</value>\n");
        jmx.append("            <savedValue>0.0</savedValue>\n");
        jmx.append("          </doubleProp>\n");
        jmx.append("        </ConstantThroughputTimer>\n");
    }

    private static void appendResultCollector(StringBuilder jmx) {
        jmx.append("        <ResultCollector guiclass=\"SummaryReport\" testclass=\"ResultCollector\" "
                + "testname=\"Summary Report\" enabled=\"true\">\n");
        jmx.append("          <boolProp name=\"ResultCollector.error_logging\">false</boolProp>\n");
        jmx.append("          <objProp>\n");
        jmx.append("            <name>saveConfig</name>\n");
        jmx.append("            <value class=\"SampleSaveConfiguration\">\n");
        jmx.append("              <time>true</time>\n");
        jmx.append("              <latency>true</latency>\n");
        jmx.append("              <timestamp>true</timestamp>\n");
        jmx.append("              <success>true</success>\n");
        jmx.append("              <label>true</label>\n");
        jmx.append("              <code>true</code>\n");
        jmx.append("              <message>true</message>\n");
        jmx.append("              <threadName>true</threadName>\n");
        jmx.append("              <dataType>true</dataType>\n");
        jmx.append("              <encoding>false</encoding>\n");
        jmx.append("              <assertions>true</assertions>\n");
        jmx.append("              <subresults>true</subresults>\n");
        jmx.append("              <responseData>false</responseData>\n");
        jmx.append("              <samplerData>false</samplerData>\n");
        jmx.append("              <xml>false</xml>\n");
        jmx.append("              <fieldNames>true</fieldNames>\n");
        jmx.append("              <responseHeaders>false</responseHeaders>\n");
        jmx.append("              <requestHeaders>false</requestHeaders>\n");
        jmx.append("              <responseDataOnError>false</responseDataOnError>\n");
        jmx.append("              <saveAssertionResultsFailureMessage>true</saveAssertionResultsFailureMessage>\n");
        jmx.append("              <assertionsResultsToSave>0</assertionsResultsToSave>\n");
        jmx.append("              <bytes>true</bytes>\n");
        jmx.append("              <sentBytes>true</sentBytes>\n");
        jmx.append("              <url>true</url>\n");
        jmx.append("              <threadCounts>true</threadCounts>\n");
        jmx.append("              <idleTime>true</idleTime>\n");
        jmx.append("              <connectTime>true</connectTime>\n");
        jmx.append("            </value>\n");
        jmx.append("          </objProp>\n");
        jmx.append("          <stringProp name=\"filename\"></stringProp>\n");
        jmx.append("        </ResultCollector>\n");
    }
}
