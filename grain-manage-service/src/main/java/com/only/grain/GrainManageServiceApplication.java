package com.only.grain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.only.grain.manage.mapper")
public class GrainManageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrainManageServiceApplication.class, args);
    }

}
