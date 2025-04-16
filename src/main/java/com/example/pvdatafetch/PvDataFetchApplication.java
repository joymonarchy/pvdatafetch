package com.example.pvdatafetch;

import com.example.pvdatafetch.mapper.DataMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@SpringBootApplication
@EnableScheduling
public class PvDataFetchApplication {

    public static void main(String[] args) {

        SpringApplication.run(PvDataFetchApplication.class, args);
    }

}
