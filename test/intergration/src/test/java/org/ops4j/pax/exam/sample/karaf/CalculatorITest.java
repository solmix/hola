package org.ops4j.pax.exam.sample.karaf;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;

import java.io.File;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.sample.Calculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PaxExam.class)
public class CalculatorITest {

    private static Logger LOG = LoggerFactory.getLogger(CalculatorITest.class);

    @Inject
    protected Calculator calculator;

    @Configuration
    public Option[] config() {
        MavenArtifactUrlReference karafUrl = maven()
            .groupId("org.apache.karaf")
            .artifactId("apache-karaf")
            .version("3.0.0")
            .type("tar.gz");

        MavenUrlReference karafStandardRepo = maven()
            .groupId("org.apache.karaf.features")
            .artifactId("standard")
            .classifier("features")
            .type("xml")
            .versionAsInProject();
        return new Option[] {
            // KarafDistributionOption.debugConfiguration("5005", true),
            karafDistributionConfiguration()
                .frameworkUrl(karafUrl)
                .unpackDirectory(new File("target/exam"))
                .useDeployFolder(false),
            keepRuntimeFolder(),
            KarafDistributionOption.features(karafStandardRepo , "scr"),
            mavenBundle()
                .groupId("org.ops4j.pax.exam.samples")
                .artifactId("pax-exam-sample8-ds")
                .versionAsInProject().start(),
       };
    }
    
    @Test
    public void testAdd() {
        int result = calculator.add(1, 2);
        LOG.info("Result of add was {}", result);
        Assert.assertEquals(3, result);
    }

}