/*
 * This class is auto generated by https://github.com/hauner/openapi-processor-spring.
 * DO NOT EDIT.
 */

package generated.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

public interface Api {

    @GetMapping(
            path = "/foo",
            produces = {"text/plain"})
    String getFoo();

    @GetMapping(
            path = "/bar",
            produces = {"text/plain"})
    RessponseEntity<String> getBar();

    @GetMapping(
            path = "/bar2",
            produces = {"text/plain"})
    RessponseEntity<String> getBar2();

}
