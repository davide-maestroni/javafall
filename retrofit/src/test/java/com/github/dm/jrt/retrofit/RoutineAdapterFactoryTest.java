/*
 * Copyright 2016 Davide Maestroni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dm.jrt.retrofit;

import com.github.dm.jrt.core.routine.InvocationMode;
import com.github.dm.jrt.function.Consumer;
import com.github.dm.jrt.stream.Streams;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.github.dm.jrt.core.util.UnitDuration.seconds;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Routine adapter factory unit tests.
 * <p>
 * Created by davide-maestroni on 05/16/2016.
 */
public class RoutineAdapterFactoryTest {

    private static final String BODY = "[{\"id\":\"1\", \"name\":\"Repo1\"}, {\"id\":\"2\","
            + " \"name\":\"Repo2\"}, {\"id\":\"3\", \"name\":\"Repo3\", \"isPrivate\":true}]";

    @Test
    public void testOutputChannelAdapter() throws IOException {

        final MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(BODY));
        server.enqueue(new MockResponse().setBody(BODY));
        server.enqueue(new MockResponse().setBody(BODY));
        server.enqueue(new MockResponse().setBody(BODY));
        server.start();
        try {
            {
                final RoutineAdapterFactory adapterFactory = //
                        RoutineAdapterFactory.builder()
                                             .invocationConfiguration()
                                             .withReadTimeout(seconds(3))
                                             .apply()
                                             .buildFactory();
                final GsonConverterFactory converterFactory = GsonConverterFactory.create();
                final Retrofit retrofit =
                        new Builder().baseUrl("http://localhost:" + server.getPort())
                                     .addCallAdapterFactory(adapterFactory)
                                     .addConverterFactory(converterFactory)
                                     .build();
                final GitHubService service = retrofit.create(GitHubService.class);
                final List<Repo> repos = service.listRepos("octocat").next();
                assertThat(repos).hasSize(3);
                assertThat(repos.get(0).getId()).isEqualTo("1");
                assertThat(repos.get(0).getName()).isEqualTo("Repo1");
                assertThat(repos.get(0).isPrivate()).isFalse();
                assertThat(repos.get(1).getId()).isEqualTo("2");
                assertThat(repos.get(1).getName()).isEqualTo("Repo2");
                assertThat(repos.get(1).isPrivate()).isFalse();
                assertThat(repos.get(2).getId()).isEqualTo("3");
                assertThat(repos.get(2).getName()).isEqualTo("Repo3");
                assertThat(repos.get(2).isPrivate()).isTrue();
            }

            {
                final RoutineAdapterFactory adapterFactory = //
                        RoutineAdapterFactory.builder()
                                             .invocationMode(InvocationMode.PARALLEL)
                                             .invocationConfiguration()
                                             .withReadTimeout(seconds(3))
                                             .apply()
                                             .buildFactory();
                final GsonConverterFactory converterFactory = GsonConverterFactory.create();
                final Retrofit retrofit =
                        new Builder().baseUrl("http://localhost:" + server.getPort())
                                     .addCallAdapterFactory(adapterFactory)
                                     .addConverterFactory(converterFactory)
                                     .build();
                final GitHubService service = retrofit.create(GitHubService.class);
                final List<Repo> repos = service.listRepos("octocat").next();
                assertThat(repos).hasSize(3);
                assertThat(repos.get(0).getId()).isEqualTo("1");
                assertThat(repos.get(0).getName()).isEqualTo("Repo1");
                assertThat(repos.get(0).isPrivate()).isFalse();
                assertThat(repos.get(1).getId()).isEqualTo("2");
                assertThat(repos.get(1).getName()).isEqualTo("Repo2");
                assertThat(repos.get(1).isPrivate()).isFalse();
                assertThat(repos.get(2).getId()).isEqualTo("3");
                assertThat(repos.get(2).getName()).isEqualTo("Repo3");
                assertThat(repos.get(2).isPrivate()).isTrue();
            }

            {
                final RoutineAdapterFactory adapterFactory = //
                        RoutineAdapterFactory.builder()
                                             .invocationMode(InvocationMode.SYNC)
                                             .invocationConfiguration()
                                             .withReadTimeout(seconds(3))
                                             .apply()
                                             .buildFactory();
                final GsonConverterFactory converterFactory = GsonConverterFactory.create();
                final Retrofit retrofit =
                        new Builder().baseUrl("http://localhost:" + server.getPort())
                                     .addCallAdapterFactory(adapterFactory)
                                     .addConverterFactory(converterFactory)
                                     .build();
                final GitHubService service = retrofit.create(GitHubService.class);
                final List<Repo> repos = service.listRepos("octocat").next();
                assertThat(repos).hasSize(3);
                assertThat(repos.get(0).getId()).isEqualTo("1");
                assertThat(repos.get(0).getName()).isEqualTo("Repo1");
                assertThat(repos.get(0).isPrivate()).isFalse();
                assertThat(repos.get(1).getId()).isEqualTo("2");
                assertThat(repos.get(1).getName()).isEqualTo("Repo2");
                assertThat(repos.get(1).isPrivate()).isFalse();
                assertThat(repos.get(2).getId()).isEqualTo("3");
                assertThat(repos.get(2).getName()).isEqualTo("Repo3");
                assertThat(repos.get(2).isPrivate()).isTrue();
            }

            {
                final RoutineAdapterFactory adapterFactory = //
                        RoutineAdapterFactory.builder()
                                             .invocationMode(InvocationMode.SERIAL)
                                             .invocationConfiguration()
                                             .withReadTimeout(seconds(3))
                                             .apply()
                                             .buildFactory();
                final GsonConverterFactory converterFactory = GsonConverterFactory.create();
                final Retrofit retrofit =
                        new Builder().baseUrl("http://localhost:" + server.getPort())
                                     .addCallAdapterFactory(adapterFactory)
                                     .addConverterFactory(converterFactory)
                                     .build();
                final GitHubService service = retrofit.create(GitHubService.class);
                final List<Repo> repos = service.listRepos("octocat").next();
                assertThat(repos).hasSize(3);
                assertThat(repos.get(0).getId()).isEqualTo("1");
                assertThat(repos.get(0).getName()).isEqualTo("Repo1");
                assertThat(repos.get(0).isPrivate()).isFalse();
                assertThat(repos.get(1).getId()).isEqualTo("2");
                assertThat(repos.get(1).getName()).isEqualTo("Repo2");
                assertThat(repos.get(1).isPrivate()).isFalse();
                assertThat(repos.get(2).getId()).isEqualTo("3");
                assertThat(repos.get(2).getName()).isEqualTo("Repo3");
                assertThat(repos.get(2).isPrivate()).isTrue();
            }

        } finally {
            server.shutdown();
        }
    }

    @Test
    public void testStreamChannelAdapter() throws IOException {

        final MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(BODY));
        server.enqueue(new MockResponse().setBody(BODY));
        server.enqueue(new MockResponse().setBody(BODY));
        server.enqueue(new MockResponse().setBody(BODY));
        server.start();
        try {
            {
                final RoutineAdapterFactory adapterFactory = RoutineAdapterFactory.defaultFactory();
                final GsonConverterFactory converterFactory = GsonConverterFactory.create();
                final Retrofit retrofit =
                        new Builder().baseUrl("http://localhost:" + server.getPort())
                                     .addCallAdapterFactory(adapterFactory)
                                     .addConverterFactory(converterFactory)
                                     .build();
                final GitHubService service = retrofit.create(GitHubService.class);
                assertThat(service.streamRepos("octocat")
                                  .map(Streams.<Repo>unfold())
                                  .onOutput(new Consumer<Repo>() {

                                      public void accept(final Repo repo) throws Exception {

                                          final int id = Integer.parseInt(repo.getId());
                                          assertThat(id).isBetween(1, 3);
                                          assertThat(repo.getName()).isEqualTo("Repo" + id);
                                          assertThat(repo.isPrivate()).isEqualTo(id == 3);
                                      }
                                  })
                                  .afterMax(seconds(3))
                                  .getError()).isNull();
            }

            {
                final RoutineAdapterFactory adapterFactory = //
                        RoutineAdapterFactory.builder()
                                             .invocationMode(InvocationMode.PARALLEL)
                                             .buildFactory();
                final GsonConverterFactory converterFactory = GsonConverterFactory.create();
                final Retrofit retrofit =
                        new Builder().baseUrl("http://localhost:" + server.getPort())
                                     .addCallAdapterFactory(adapterFactory)
                                     .addConverterFactory(converterFactory)
                                     .build();
                final GitHubService service = retrofit.create(GitHubService.class);
                assertThat(service.streamRepos("octocat")
                                  .map(Streams.<Repo>unfold())
                                  .onOutput(new Consumer<Repo>() {

                                      public void accept(final Repo repo) throws Exception {

                                          final int id = Integer.parseInt(repo.getId());
                                          assertThat(id).isBetween(1, 3);
                                          assertThat(repo.getName()).isEqualTo("Repo" + id);
                                          assertThat(repo.isPrivate()).isEqualTo(id == 3);
                                      }
                                  })
                                  .afterMax(seconds(3))
                                  .getError()).isNull();
            }

            {
                final RoutineAdapterFactory adapterFactory = //
                        RoutineAdapterFactory.builder()
                                             .invocationMode(InvocationMode.SYNC)
                                             .buildFactory();
                final GsonConverterFactory converterFactory = GsonConverterFactory.create();
                final Retrofit retrofit =
                        new Builder().baseUrl("http://localhost:" + server.getPort())
                                     .addCallAdapterFactory(adapterFactory)
                                     .addConverterFactory(converterFactory)
                                     .build();
                final GitHubService service = retrofit.create(GitHubService.class);
                assertThat(service.streamRepos("octocat")
                                  .map(Streams.<Repo>unfold())
                                  .onOutput(new Consumer<Repo>() {

                                      public void accept(final Repo repo) throws Exception {

                                          final int id = Integer.parseInt(repo.getId());
                                          assertThat(id).isBetween(1, 3);
                                          assertThat(repo.getName()).isEqualTo("Repo" + id);
                                          assertThat(repo.isPrivate()).isEqualTo(id == 3);
                                      }
                                  })
                                  .afterMax(seconds(3))
                                  .getError()).isNull();
            }

            {
                final RoutineAdapterFactory adapterFactory = //
                        RoutineAdapterFactory.builder()
                                             .invocationMode(InvocationMode.SERIAL)
                                             .buildFactory();
                final GsonConverterFactory converterFactory = GsonConverterFactory.create();
                final Retrofit retrofit =
                        new Builder().baseUrl("http://localhost:" + server.getPort())
                                     .addCallAdapterFactory(adapterFactory)
                                     .addConverterFactory(converterFactory)
                                     .build();
                final GitHubService service = retrofit.create(GitHubService.class);
                assertThat(service.streamRepos("octocat")
                                  .map(Streams.<Repo>unfold())
                                  .onOutput(new Consumer<Repo>() {

                                      public void accept(final Repo repo) throws Exception {

                                          final int id = Integer.parseInt(repo.getId());
                                          assertThat(id).isBetween(1, 3);
                                          assertThat(repo.getName()).isEqualTo("Repo" + id);
                                          assertThat(repo.isPrivate()).isEqualTo(id == 3);
                                      }
                                  })
                                  .afterMax(seconds(3))
                                  .getError()).isNull();
            }

        } finally {
            server.shutdown();
        }
    }
}
