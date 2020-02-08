package com.only.grain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.only.grain.payment.mapper")
public class GrainPaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrainPaymentServiceApplication.class, args);
    }

}
