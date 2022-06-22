package org.example;

import java.util.Map;

import org.example.dao.PollDao;
import org.example.dao.impl.PollDaoImpl;
import org.example.pojo.Poll;

import com.fizzed.rocker.BindableRockerModel;
import com.fizzed.rocker.Rocker;
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;

import io.activej.common.Utils;
import io.activej.http.AsyncServlet;
import io.activej.http.HttpHeaders;
import io.activej.http.HttpMethod;
import io.activej.http.HttpResponse;
import io.activej.http.RoutingServlet;
import io.activej.inject.annotation.Provides;
import io.activej.launcher.Launcher;
import io.activej.launchers.http.HttpServerLauncher;
import io.activej.promise.Promise;

/**
 * Poll application based on ActiveJ + Rocker template
 * https://github.com/fizzed/rocker/blob/master/docs/SYNTAX.md
 *
 * To run:
 *
 * mvn clean package
 * java -jar target\activej-poll-x.y.jar
 */
public final class PollApp extends HttpServerLauncher {
    @Provides // @Provides注解意味着它是通过ActiveJ Inject DI完成的
    PollDao pollRepo() {
        //return new PollDaoImpl();
        PollDao pollDao = new PollDaoImpl();
        pollDao.add(new Poll("人教社毒教材事件", "人教社毒教材事件", Utils.listOf("正", "反")));
        pollDao.add(new Poll("唐山烧烤打人事件", "唐山烧烤打人事件", Utils.listOf("正", "反")));
        return pollDao;
    }

    private static byte[] applyTemplate(BindableRockerModel rockerModel, Map<String, Object> scopes) {
        ArrayOfByteArraysOutput template = (ArrayOfByteArraysOutput) rockerModel.relaxedBind(scopes).render();
        return template.toByteArray();
    }

    @Provides
    AsyncServlet servlet(PollDao pollDao) {
        //BindableRockerModel singlePollView = Rocker.template("views/singlePollView.rocker.html");
        //BindableRockerModel singlePollCreate = Rocker.template("views/singlePollCreate.rocker.html");
        //BindableRockerModel listPolls = Rocker.template("views/listPolls.rocker.html");
        //Use local variable, to avoid throw below Exception:
        //com.fizzed.rocker.RenderingException: Template already rendered (templates are single use only!)

        return RoutingServlet.create()
                .map(HttpMethod.GET, "/", request -> {
                    // rocker.template 只能render() 1次!
                    BindableRockerModel listPolls = Rocker.template("views/listPolls.rocker.html");
                    return HttpResponse.ok200()
                        //.withBody(applyTemplate(listPolls,  Map.of("polls", pollDao.findAll().entrySet()))))
                        .withBody(applyTemplate(listPolls,  Utils.mapOf("polls", pollDao.findAll().entrySet())));
                })
                .map(HttpMethod.GET, "/poll/:id", request -> {
                    BindableRockerModel singlePollView = Rocker.template("views/singlePollView.rocker.html");
                    int id = Integer.parseInt(request.getPathParameter("id"));
                    return HttpResponse.ok200() // mapOf("id", id, "poll", pollDao.find(id))
                            .withBody(applyTemplate(singlePollView, Utils.mapOf("id", id, "poll", pollDao.find(id))));
                })
                .map(HttpMethod.GET, "/create", request -> {
                    BindableRockerModel singlePollCreate = Rocker.template("views/singlePollCreate.rocker.html");
                    return HttpResponse.ok200()
                        .withBody(applyTemplate(singlePollCreate, Utils.mapOf())); // emptyMap()
                })
                .map(HttpMethod.POST, "/vote", request -> request.loadBody()
                        .then(() -> {
                            Map<String, String> params = request.getPostParameters();
                            String option = params.get("option");
                            String stringId = params.get("id");
                            if (option == null || stringId == null) {
                                return Promise.of(HttpResponse.ofCode(401));
                            }

                            int id = Integer.parseInt(stringId);
                            Poll question = pollDao.find(id);

                            question.vote(option);

                            return Promise.of(HttpResponse.redirect302(Utils.nonNullElse(request.getHeader(HttpHeaders.REFERER), "/")));
                        }))
                .map(HttpMethod.POST, "/add", request -> request.loadBody()
                        .map($ -> {
                            Map<String, String> params = request.getPostParameters();
                            String title = params.get("title");
                            String message = params.get("message");

                            String option1 = params.get("option1");
                            String option2 = params.get("option2");

                            int id = pollDao.add(new Poll(title, message, Utils.listOf(option1, option2)));
                            logger.debug("New poll id={}", id);
                            return HttpResponse.redirect302("poll/" + id);
                        }))
                .map(HttpMethod.POST, "/delete", request -> request.loadBody()
                        .then(() -> {
                            Map<String, String> params = request.getPostParameters();
                            String id = params.get("id");
                            if (id == null) {
                                return Promise.of(HttpResponse.ofCode(401));
                            }
                            pollDao.remove(Integer.parseInt(id));

                            return Promise.of(HttpResponse.redirect302("/"));
                        }));
    }

    public static void main(String[] args) throws Exception {
        Launcher launcher = new PollApp();
        launcher.launch(args);
    }
}
