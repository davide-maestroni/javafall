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

package com.github.dm.jrt.android.retrofit;

import com.github.dm.jrt.android.core.config.LoaderConfiguration.CacheStrategyType;
import com.github.dm.jrt.android.object.annotation.CacheStrategy;
import com.github.dm.jrt.core.channel.Channel;
import com.github.dm.jrt.stream.builder.StreamBuilder;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

/**
 * GitHub service interface.
 * <p>
 * Created by davide-maestroni on 03/25/2016.
 */
public interface GitHubService {

  @GET("users/{user}/repos")
  @Headers("Accept: application/json")
  @CacheStrategy(CacheStrategyType.CACHE)
  Channel<Object, List<Repo>> listRepos(@Path("user") String user);

  @GET("users/{user}/repos")
  @Headers("Accept: application/json")
  @CacheStrategy(CacheStrategyType.CACHE)
  StreamBuilder<Object, List<Repo>> streamRepos(@Path("user") String user);
}
