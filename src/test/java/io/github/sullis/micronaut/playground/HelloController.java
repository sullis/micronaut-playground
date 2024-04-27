package io.github.sullis.micronaut.playground;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

@Controller
public class HelloController {
    @Get("/")
    @Produces(value = "text/plain")
    public String hello() {
        return Constants.PAYLOAD;
    }
}
