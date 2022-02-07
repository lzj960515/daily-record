package com.my.response.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
@RestController
@RequestMapping("/foo")
public class FooController {

    @GetMapping("/a")
    public String a() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
        return "ok";
    }

    @GetMapping("/b")
    public String b() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
        return "ok";
    }
}
