package io.github.sullis.micronaut.playground;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller
public class HelloController {
    @Get("/")
    public String hello() {
        return Constants.PAYLOAD;
    }
}
