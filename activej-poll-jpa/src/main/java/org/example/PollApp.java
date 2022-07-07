package org.example;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.persistence.EntityManager;

import org.example.dao.PollDao;
import org.example.dao.impl.HibernateJpaPollDaoImpl;
import org.example.module.MySqlModule;
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
import io.activej.inject.module.Module;
import io.activej.launcher.Launcher;
import io.activej.launchers.http.HttpServerLauncher;
import io.activej.promise.Promise;

/**
 * Poll application based on ActiveJ + Rocker template + MySQL
 * https://github.com/fizzed/rocker/blob/master/docs/SYNTAX.md
 *
 * To run:
 *
 * mvn clean package
 * java -jar target\activej-poll-mysql.x.y.jar
 */
public final class PollApp extends HttpServerLauncher {
    @Override
    protected Module getOverrideModule() {
        return super.getOverrideModule()
                .combineWith(MySqlModule.create()) ;
    }

    @Provides
    Executor executor() {
        return Executors.newCachedThreadPool();
    }

    private static byte[] applyTemplate(BindableRockerModel rockerModel, Map<String, Object> scopes) {
        ArrayOfByteArraysOutput template = (ArrayOfByteArraysOutput) rockerModel.relaxedBind(scopes).render();
        return template.toByteArray();
    }

    @Provides
    PollDao pollDao(EntityManager entityManager, Executor executor) {
        return new HibernateJpaPollDaoImpl(entityManager, executor);
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
                    //return HttpResponse.ok200()
                    //    //.withBody(applyTemplate(listPolls,  Map.of("polls", pollDao.findAll().entrySet()))))
                    //    .withBody(applyTemplate(listPolls,  Utils.mapOf("polls", pollDao.findAll().entrySet())));
                    Promise<Map<Integer, Poll>> result = pollDao.findAll();
                    return result.map(map -> HttpResponse.ok200()
                                    .withBody(applyTemplate(listPolls,  Utils.mapOf("polls", map.entrySet())))
                            ).whenException(Exception::printStackTrace);
                })
                .map(HttpMethod.GET, "/poll/:id", request -> {
                    BindableRockerModel singlePollView = Rocker.template("views/singlePollView.rocker.html");
                    int id = Integer.parseInt(request.getPathParameter("id"));
                    //return HttpResponse.ok200() // mapOf("id", id, "poll", pollDao.find(id))
                    //        .withBody(applyTemplate(singlePollView, Utils.mapOf("id", id, "poll", pollDao.find(id))));
                    Promise<Poll> result = pollDao.find(id);
                    return result.map(poll -> HttpResponse.ok200()
                                    .withBody(applyTemplate(singlePollView, Utils.mapOf("id", id, "poll", poll)))
                            ).whenException(Exception::printStackTrace);
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
                            Promise<Poll> question = pollDao.find(id);

                            return question.then(poll -> Promise.of(poll.vote(option)))
                                .then(poll -> pollDao.update(id, poll))
                                .map(bool -> HttpResponse.redirect302(Utils.nonNullElse(request.getHeader(HttpHeaders.REFERER), "/")))
                                .whenException(Exception::printStackTrace);
                            //return Promise.of(HttpResponse.redirect302(
                            //    Utils.nonNullElse(request.getHeader(HttpHeaders.REFERER), "/")));
                        }))
                .map(HttpMethod.POST, "/add", request -> request.loadBody()
                      //.map($ -> { Type mismatch: cannot convert from Promise<Object> to Promisable<HttpResponse>
                        .then($ -> {
                            Map<String, String> params = request.getPostParameters();
                            String title = params.get("title");
                            String message = params.get("message");

                            String option1 = params.get("option1");
                            String option2 = params.get("option2");

                            Promise<Integer> result = pollDao.add(new Poll(title, message, Utils.listOf(option1, option2)));
                            //return HttpResponse.redirect302("poll/" + id);
                            return result.whenResult(id -> logger.debug("New poll id={}", id))
                                    .map(id -> HttpResponse.redirect302("poll/" + id))
                                    .whenException(Exception::printStackTrace);
                        }))
                .map(HttpMethod.POST, "/delete", request -> request.loadBody()
                        .then(() -> {
                            Map<String, String> params = request.getPostParameters();
                            String id = params.get("id");
                            if (id == null) {
                                return Promise.of(HttpResponse.ofCode(401));
                            }
                            Promise<Boolean> result = pollDao.remove(Integer.parseInt(id));

                            //return Promise.of(HttpResponse.redirect302("/"));
                            return result.map(bool -> HttpResponse.redirect302("/"))
                                    .whenException(Exception::printStackTrace);
                        })
                ).map(HttpMethod.GET, "/now", request -> {
                    return HttpResponse.ok200().withHtml(pollDao.now());
                }).map(HttpMethod.GET, "/asyncNow", request -> {
                    Promise<String> result = pollDao.asyncNow();
                    return result.map(now -> HttpResponse.ok200().withHtml(now));
                });
    }

    public static void main(String[] args) throws Exception {
        Launcher launcher = new PollApp();
        launcher.launch(args);
    }
}
