package org.example;

import java.util.HashMap;
import java.util.Map;

import com.fizzed.rocker.Rocker;
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;

import io.activej.http.AsyncServlet;
import io.activej.http.HttpResponse;
import io.activej.inject.annotation.Provides;
import io.activej.launcher.Launcher;
import io.activej.launchers.http.HttpServerLauncher;

public class RockerApp extends HttpServerLauncher {
    @Provides
    AsyncServlet servlet() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Roseox");
        map.put("age", 47);

        String content = Rocker.template("views/hello.rocker.html")
            .relaxedBind(map)
            .render()
            .toString();

// Project Setting: Java build path add source folder:
//      target/generated-sources/rocker
//
//        ArrayOfByteArraysOutput template = (ArrayOfByteArraysOutput) views.hello
//                .template("World")
//                .render();
//        return request -> Promise.of(HttpResponse.ok200()
//                .withHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValue.ofContentType(ContentTypes.HTML_UTF_8))
//                .withBodyGzipCompression()
//                .withBody(template.toByteArray()));
        return request -> HttpResponse.ok200().withPlainText(content);
    }

    public static void main(String[] args) throws Exception {
        Launcher launcher = new RockerApp();
        launcher.launch(args);
    }
}
