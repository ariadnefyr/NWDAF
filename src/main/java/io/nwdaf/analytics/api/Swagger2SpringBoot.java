package io.nwdaf.analytics.api;

import io.nwdaf.analytics.util.MetricFetcher;
import io.nwdaf.analytics.util.MetricDataStore;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@ComponentScan(basePackages = { "io.nwdaf.analytics.api", "io.nwdaf.analytics.api" , "io.nwdaf.analytics.api.config"})
public class Swagger2SpringBoot implements CommandLineRunner {

    @Override
    public void run(String... arg0) throws Exception {
        if (arg0.length > 0 && arg0[0].equals("exitcode")) {
            throw new ExitException();
        }
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Swagger2SpringBoot.class, args);

        String server_uop = new Targets().getUniversityOfPatrasPrometheus();
        String server_5GS = new Targets().getOpen5GSPrometheus();

        //MetricFetcher.fetchRanUe(server_5GS);
        MetricFetcher.fetchUesActive(server_5GS);
        MetricFetcher.fetchS5cRxCreateSession(server_5GS);
        MetricFetcher.fetchCpuSeconds(server_5GS);
        MetricFetcher.fetchMemoryBytes(server_5GS);

        MetricFetcher.fetchUeCount(server_uop);
        MetricFetcher.fetchCpuPercentage(server_uop);
        MetricFetcher.fetchMemoryUsage(server_uop);


        //System.out.println("RAN UEs: " + MetricDataStore.ranUeValues);
        System.out.println("UEs Active: " + MetricDataStore.uesActiveValues);
        System.out.println("S5C Rx Create Session: " + MetricDataStore.s5cRxCreateSessionValues);
        System.out.println("CPU Seconds: " + MetricDataStore.cpuSecondsValues);
        System.out.println("Memory Bytes: " + MetricDataStore.memoryBytesValues);

        System.out.println("UE Count: " + MetricDataStore.ueCountValues);
        System.out.println("CPU Percentage: " + MetricDataStore.cpuPercentageValues);
        System.out.println("Memory Usage: " + MetricDataStore.memoryUsageValues);
    }

    class ExitException extends RuntimeException implements ExitCodeGenerator {
        private static final long serialVersionUID = 1L;

        @Override
        public int getExitCode() {
            return 10;
        }

    }
}
