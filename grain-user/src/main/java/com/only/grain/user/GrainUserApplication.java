package com.only.grain.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.only.grain.user.mapper")
public class GrainUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrainUserApplication.class, args);
    }

}
