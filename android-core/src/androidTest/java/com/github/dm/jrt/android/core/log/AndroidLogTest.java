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

package com.github.dm.jrt.android.core.log;

import android.test.AndroidTestCase;

import com.github.dm.jrt.core.log.Log.Level;
import com.github.dm.jrt.core.log.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Android log unit tests.
 * <p>
 * Created by davide-maestroni on 10/27/2014.
 */
public class AndroidLogTest extends AndroidTestCase {

  private static final String[] ARGS = new String[]{"test1", "test2", "test3", "test4", "test5"};

  private static final String FORMAT0 = "0: %s";

  private static final String FORMAT1 = "0: %s - 1: %s";

  private static final String FORMAT2 = "0: %s - 1: %s - 2: %s";

  private static final String FORMAT3 = "0: %s - 1: %s - 2: %s - 3: %s";

  private static final String FORMAT4 = "0: %s - 1: %s - 2: %s - 3: %s - 4: %s";

  public void testConstructor() {

    boolean failed = false;
    try {
      new AndroidLogs();
      failed = true;

    } catch (final Throwable ignored) {

    }

    assertThat(failed).isFalse();
  }

  public void testLogDbg() {

    final NullPointerException ex = new NullPointerException();
    final Logger logger = Logger.newLogger(new AndroidLog(), Level.DEBUG, this);

    logger.dbg(ARGS[0]);
    logger.dbg(FORMAT0, ARGS[0]);
    logger.dbg(FORMAT1, ARGS[0], ARGS[1]);
    logger.dbg(FORMAT2, ARGS[0], ARGS[1], ARGS[2]);
    logger.dbg(FORMAT3, ARGS[0], ARGS[1], ARGS[2], ARGS[3]);
    logger.dbg(FORMAT4, ARGS[0], ARGS[1], ARGS[2], ARGS[3], ARGS[4]);
    logger.dbg(ex);
    logger.dbg(ex, ARGS[0]);
    logger.dbg(ex, FORMAT0, ARGS[0]);
    logger.dbg(ex, FORMAT1, ARGS[0], ARGS[1]);
    logger.dbg(ex, FORMAT2, ARGS[0], ARGS[1], ARGS[2]);
    logger.dbg(ex, FORMAT3, ARGS[0], ARGS[1], ARGS[2], ARGS[3]);
    logger.dbg(ex, FORMAT4, ARGS[0], ARGS[1], ARGS[2], ARGS[3], ARGS[4]);
  }

  public void testLogErr() {

    final NullPointerException ex = new NullPointerException();
    final Logger logger = Logger.newLogger(new AndroidLog(), Level.DEBUG, this);

    logger.err(ARGS[0]);
    logger.err(FORMAT0, ARGS[0]);
    logger.err(FORMAT1, ARGS[0], ARGS[1]);
    logger.err(FORMAT2, ARGS[0], ARGS[1], ARGS[2]);
    logger.err(FORMAT3, ARGS[0], ARGS[1], ARGS[2], ARGS[3]);
    logger.err(FORMAT4, ARGS[0], ARGS[1], ARGS[2], ARGS[3], ARGS[4]);
    logger.err(ex);
    logger.err(ex, ARGS[0]);
    logger.err(ex, FORMAT0, ARGS[0]);
    logger.err(ex, FORMAT1, ARGS[0], ARGS[1]);
    logger.err(ex, FORMAT2, ARGS[0], ARGS[1], ARGS[2]);
    logger.err(ex, FORMAT3, ARGS[0], ARGS[1], ARGS[2], ARGS[3]);
    logger.err(ex, FORMAT4, ARGS[0], ARGS[1], ARGS[2], ARGS[3], ARGS[4]);
  }

  public void testLogWrn() {

    final NullPointerException ex = new NullPointerException();
    final Logger logger = Logger.newLogger(new AndroidLog(), Level.DEBUG, this);

    logger.wrn(ARGS[0]);
    logger.wrn(FORMAT0, ARGS[0]);
    logger.wrn(FORMAT1, ARGS[0], ARGS[1]);
    logger.wrn(FORMAT2, ARGS[0], ARGS[1], ARGS[2]);
    logger.wrn(FORMAT3, ARGS[0], ARGS[1], ARGS[2], ARGS[3]);
    logger.wrn(FORMAT4, ARGS[0], ARGS[1], ARGS[2], ARGS[3], ARGS[4]);
    logger.wrn(ex);
    logger.wrn(ex, ARGS[0]);
    logger.wrn(ex, FORMAT0, ARGS[0]);
    logger.wrn(ex, FORMAT1, ARGS[0], ARGS[1]);
    logger.wrn(ex, FORMAT2, ARGS[0], ARGS[1], ARGS[2]);
    logger.wrn(ex, FORMAT3, ARGS[0], ARGS[1], ARGS[2], ARGS[3]);
    logger.wrn(ex, FORMAT4, ARGS[0], ARGS[1], ARGS[2], ARGS[3], ARGS[4]);
  }
}
