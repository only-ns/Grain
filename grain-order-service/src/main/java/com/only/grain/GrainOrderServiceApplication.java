package com.only.grain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.only.grain.order.mapper")
public class GrainOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrainOrderServiceApplication.class, args);
    }

}
