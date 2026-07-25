package com.campus.lostfound;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.campus.lostfound.mapper")
public class LostFoundApplication {

    public static void main(String[] args) {
        SpringApplication.run(LostFoundApplication.class, args);
        System.out.println("========================================");
        System.out.println("  校园失物招领系统启动成功！");
        System.out.println("  Knife4j文档: http://localhost:8123/doc.html");
        System.out.println("========================================");
    }
}
