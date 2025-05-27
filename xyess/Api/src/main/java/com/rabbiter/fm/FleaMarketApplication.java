package com.rabbiter.fm;

import com.rabbiter.fm.common.utils.OrderTaskHandler;
import com.rabbiter.fm.common.utils.PathUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FleaMarketApplication {

    public static void main(String[] args) {
        System.out.println("Project Path: " + PathUtils.getClassLoadRootPath());
        SpringApplication.run(FleaMarketApplication.class, args);
        OrderTaskHandler.run();
    }

}
