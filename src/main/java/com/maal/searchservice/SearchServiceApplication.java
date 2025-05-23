package com.maal.searchservice;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class SearchServiceApplication {

    public static void main(String[] args) {
         new SpringApplicationBuilder(SearchServiceApplication.class)
//                .web(WebApplicationType.NONE) // Define para não ser uma aplicação web
                .run(args);
    }

}
