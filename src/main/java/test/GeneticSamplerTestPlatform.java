package test;

import jdk.jfr.*;
import org.algorithm.GeneticDNAFinderPlatformJava;
import org.algorithm.GeneticDNAFinderVirtualJava;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdk.jfr.Recording;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;


public class GeneticSamplerTestPlatform extends AbstractJavaSamplerClient {

    private static final Logger log = LoggerFactory.getLogger(GeneticSamplerTestPlatform.class);

    private Recording currentRecording = null;

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("JFR_OUTPUT_PATH", "C:/Users/davik/Downloads/apache-jmeter-5.6.3/apache-jmeter-5.6.3/bin/");
        defaultParameters.addArgument("JFR_FILENAME_PREFIX", "Profile_App_");
        defaultParameters.addArgument("JFR_SETTINGS", "profile");
        defaultParameters.addArgument("ENABLE_JFR", "true");
        return defaultParameters;
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();

        boolean enableJfr = context.getParameter("ENABLE_JFR", "false").equalsIgnoreCase("true");
        String jfrOutputPath = context.getParameter("JFR_OUTPUT_PATH", "./");
        String filenamePrefix = context.getParameter("JFR_FILENAME_PREFIX", "profile_");
        String jfrSettingsName = context.getParameter("JFR_SETTINGS", "profile");

        String jfrFilePath = "";

        if (enableJfr) {
            if (!FlightRecorder.isAvailable()) {
                log.warn("JFR (Flight Recorder) não está disponível nesta JVM. Gravação JFR desabilitada.");
                enableJfr = false;
            } else {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
                String threadName = context.getJMeterContext().getThread().getThreadName();
                String fullFilename = filenamePrefix + threadName + "_" + timestamp + ".jfr";

                try {
                    Path outputDir = Paths.get(jfrOutputPath);
                    if (!outputDir.toFile().exists()) {
                        log.warn("Diretório de saída do JFR não existe, tentando usar o diretório atual: " + outputDir.toString());
                    }
                    jfrFilePath = outputDir.resolve(fullFilename).toString();
                } catch (InvalidPathException e) {
                    log.error("Caminho de saída do JFR inválido: " + jfrOutputPath, e);
                    result.sampleStart();
                    result.sampleEnd();
                    result.setSuccessful(false);
                    result.setResponseMessage("Error: Invalid JFR output path: " + e.getMessage());
                    result.setResponseCode("500");
                    return result;
                }
            }
        }

        result.sampleStart();

        try {
            if (enableJfr) {
                Configuration jfrConfig = Configuration.getConfiguration(jfrSettingsName);
                currentRecording = new Recording(jfrConfig);
                currentRecording.setName("JMeterSamplerProfile_" + Thread.currentThread().getName());
                currentRecording.setDestination(Paths.get(jfrFilePath));
                currentRecording.start();
                log.info("JFR Recording started by {}: {}", context.getJMeterContext().getThread().getThreadName(), jfrFilePath);
            }


            GeneticDNAFinderPlatformJava.run();

            result.sampleEnd();
            result.setSuccessful(true);
            result.setResponseMessage("Executed Successfully" + (enableJfr ? ". JFR: " + jfrFilePath : ". JFR disabled."));
            result.setResponseCodeOK();

        } catch (Throwable t) {
            result.sampleEnd();
            result.setSuccessful(false);
            String errorMessage = (t.getMessage() == null) ? t.getClass().getSimpleName() : t.getMessage();
            result.setResponseMessage("Error: " + t.getClass().getName() + ": " + errorMessage);
            result.setResponseCode("500");
            log.error("Error during Sampler execution by {}: ", context.getJMeterContext().getThread().getThreadName(), t);
        } finally {
            if (enableJfr && currentRecording != null) {
                try {
                    if (currentRecording.getState() == RecordingState.RUNNING) {
                        currentRecording.stop();
                        log.info("JFR Recording stopped by {}: {}", context.getJMeterContext().getThread().getThreadName(), jfrFilePath);
                    }
                } catch (Exception e) { // Captura exceção genérica ao parar
                    log.error("Error stopping JFR recording by {}: {}", context.getJMeterContext().getThread().getThreadName(), jfrFilePath, e);
                } finally {
                    currentRecording.close();
                    log.info("JFR Recording closed by {}: {}", context.getJMeterContext().getThread().getThreadName(), jfrFilePath);
                }
                currentRecording = null;
            }
        }
        return result;
    }
}