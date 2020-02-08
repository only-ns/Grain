package com.only.grain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.only.grain.user.mapper")
public class GrainUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrainUserServiceApplication.class, args);
    }

}
