package com.capitalone.interview.codingchallenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;
import static springfox.documentation.builders.RequestHandlerSelectors.any;

@EnableRetry
@EnableSwagger2
@SpringBootApplication
@EntityScan(basePackageClasses = {StockReportApplication.class, Jsr310JpaConverters.class})
public class StockReportApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(StockReportApplication.class);
    }

    @Bean
    public Docket docket()
    {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("Stock report service")
                        .description("REST services to lookup securities and report query aggregate reports.")
                        .build())
                .select()
                .apis(any())
                .paths(or(regex("/v1/.*"), regex("/metrics"), regex("/health")))
                .build()
                .pathMapping("/");
    }

    @Bean
    public ConversionService conversionService()
    {
        return new DefaultConversionService();
    }

    @Bean
    public RestTemplate restTemplate()
    {
        return new RestTemplate();
    }
}
