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

package com.github.dm.jrt.core.invocation;

import com.github.dm.jrt.core.common.RoutineException;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Exceptions unit tests.
 * <p>
 * Created by davide-maestroni on 10/02/2014.
 */
public class ExceptionTest {

  @Test
  public void tesInvocationDeadlockException() {

    assertThat(new InvocationDeadlockException("")).hasNoCause();
  }

  @Test
  public void testInvocationException() {

    assertThat(new InvocationException(new NullPointerException()).getCause()).isExactlyInstanceOf(
        NullPointerException.class);
    assertThat(new InvocationException(null)).hasNoCause();
    assertThat(InvocationException.wrapIfNeeded(new NullPointerException())).isExactlyInstanceOf(
        InvocationException.class);
    assertThat(InvocationException.wrapIfNeeded(new RoutineException())).isExactlyInstanceOf(
        RoutineException.class);
  }

  @Test
  public void testInvocationInterruptedException() {

    assertThat(new InvocationInterruptedException(
        new InterruptedException()).getCause()).isExactlyInstanceOf(InterruptedException.class);
    assertThat(new InvocationInterruptedException(null)).hasNoCause();
    InvocationInterruptedException.throwIfInterrupt(new NullPointerException());
    try {
      InvocationInterruptedException.throwIfInterrupt(new InterruptedException());
      fail();

    } catch (final InvocationInterruptedException ignored) {
    }

    try {
      InvocationInterruptedException.throwIfInterrupt(
          new InvocationInterruptedException(new InterruptedException()));
      fail();

    } catch (final InvocationInterruptedException ignored) {
    }
  }
}
