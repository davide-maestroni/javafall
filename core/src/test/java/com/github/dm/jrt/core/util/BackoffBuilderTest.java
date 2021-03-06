package com.github.dm.jrt.core.util;

import com.github.dm.jrt.core.common.Backoff;
import com.github.dm.jrt.core.common.BackoffBuilder;
import com.github.dm.jrt.core.common.BackoffDecorator;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.github.dm.jrt.core.common.Backoff.NO_DELAY;
import static com.github.dm.jrt.core.util.UnitDuration.millis;
import static com.github.dm.jrt.core.util.UnitDuration.seconds;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Backoff unit tests.
 * <p>
 * Created by davide-maestroni on 05/09/2016.
 */
public class BackoffBuilderTest {

  @Test
  public void testAdd() {
    final Backoff backoff = BackoffBuilder.afterCount(2)
                                          .constantDelay(seconds(1))
                                          .plus(BackoffBuilder.afterCount(1)
                                                              .linearDelay(seconds(1.5)));
    assertThat(backoff.getDelay(1)).isEqualTo(NO_DELAY);
    assertThat(backoff.getDelay(2)).isEqualTo(1500);
    assertThat(backoff.getDelay(3)).isEqualTo(4000);
    assertThat(backoff.getDelay(4)).isEqualTo(5500);
  }

  @Test
  public void testAdd2() {
    final Backoff backoff = BackoffBuilder.afterCount(1)
                                          .linearDelay(seconds(1.5))
                                          .plus(BackoffBuilder.afterCount(2)
                                                              .constantDelay(seconds(1)));
    assertThat(backoff.getDelay(1)).isEqualTo(NO_DELAY);
    assertThat(backoff.getDelay(2)).isEqualTo(1500);
    assertThat(backoff.getDelay(3)).isEqualTo(4000);
    assertThat(backoff.getDelay(4)).isEqualTo(5500);
  }

  @Test
  public void testBackoffDecorator() {
    final Backoff backoff =
        new BackoffDecorator(BackoffBuilder.afterCount(2).constantDelay(seconds(1)));
    assertThat(backoff.getDelay(1)).isEqualTo(NO_DELAY);
    assertThat(backoff.getDelay(2)).isEqualTo(NO_DELAY);
    assertThat(backoff.getDelay(3)).isEqualTo(1000);
    assertThat(backoff.getDelay(4)).isEqualTo(1000);
  }

  @Test
  public void testConstant() {

    final Backoff backoff1 = BackoffBuilder.afterCount(0).constantDelay(1, TimeUnit.SECONDS);
    assertThat(backoff1.getDelay(1)).isEqualTo(1000);
    assertThat(backoff1.getDelay(2)).isEqualTo(1000);
    assertThat(backoff1.getDelay(3)).isEqualTo(1000);
    final Backoff backoff2 = BackoffBuilder.afterCount(0).constantDelay(seconds(1));
    assertThat(backoff2.getDelay(1)).isEqualTo(1000);
    assertThat(backoff2.getDelay(2)).isEqualTo(1000);
    assertThat(backoff2.getDelay(3)).isEqualTo(1000);
    assertThat(backoff2.getDelay(4)).isEqualTo(1000);
    final Backoff backoff3 = BackoffBuilder.afterCount(2).constantDelay(seconds(1));
    assertThat(backoff3.getDelay(1)).isEqualTo(NO_DELAY);
    assertThat(backoff3.getDelay(2)).isEqualTo(NO_DELAY);
    assertThat(backoff3.getDelay(3)).isEqualTo(1000);
    assertThat(backoff3.getDelay(4)).isEqualTo(1000);
  }

  @Test
  public void testConstantCapped() {

    final Backoff backoff1 =
        BackoffBuilder.afterCount(0).constantDelay(1, TimeUnit.SECONDS).cappedTo(millis(500));
    assertThat(backoff1.getDelay(1)).isEqualTo(500);
    assertThat(backoff1.getDelay(2)).isEqualTo(500);
    final Backoff backoff2 =
        BackoffBuilder.afterCount(0).constantDelay(seconds(1)).cappedTo(700, TimeUnit.MILLISECONDS);
    assertThat(backoff2.getDelay(1)).isEqualTo(700);
    assertThat(backoff2.getDelay(2)).isEqualTo(700);
    assertThat(backoff2.getDelay(3)).isEqualTo(700);
    final Backoff backoff3 =
        BackoffBuilder.afterCount(1).constantDelay(seconds(1)).cappedTo(700, TimeUnit.MILLISECONDS);
    assertThat(backoff3.getDelay(1)).isEqualTo(NO_DELAY);
    assertThat(backoff3.getDelay(2)).isEqualTo(700);
    assertThat(backoff3.getDelay(3)).isEqualTo(700);
  }

  @Test
  public void testConstantCappedJitter() {

    final Backoff backoff1 = BackoffBuilder.afterCount(0)
                                           .constantDelay(seconds(1))
                                           .cappedTo(millis(500))
                                           .withJitter(0.5f);
    assertThat(backoff1.getDelay(1)).isBetween(250L, 500L);
    assertThat(backoff1.getDelay(2)).isBetween(250L, 500L);
    final Backoff backoff2 = BackoffBuilder.afterCount(0)
                                           .constantDelay(seconds(1))
                                           .withJitter(0.3f)
                                           .cappedTo(700, TimeUnit.MILLISECONDS);
    assertThat(backoff2.getDelay(1)).isEqualTo(700);
    assertThat(backoff2.getDelay(2)).isEqualTo(700);
    assertThat(backoff2.getDelay(3)).isEqualTo(700);
    final Backoff backoff3 = BackoffBuilder.afterCount(1)
                                           .constantDelay(seconds(1))
                                           .withJitter(0.3f)
                                           .cappedTo(700, TimeUnit.MILLISECONDS);
    assertThat(backoff3.getDelay(1)).isEqualTo(NO_DELAY);
    assertThat(backoff3.getDelay(2)).isEqualTo(700);
    assertThat(backoff3.getDelay(3)).isEqualTo(700);
  }

  @Test
  public void testConstantJitter() {

    final Backoff backoff1 =
        BackoffBuilder.afterCount(0).constantDelay(1, TimeUnit.SECONDS).withJitter(0.7f);
    assertThat(backoff1.getDelay(1)).isBetween(300L, 1000L);
    assertThat(backoff1.getDelay(2)).isBetween(300L, 1000L);
    final Backoff backoff2 =
        BackoffBuilder.afterCount(0).constantDelay(seconds(1)).withJitter(0.4f);
    assertThat(backoff2.getDelay(1)).isBetween(600L, 1000L);
    assertThat(backoff2.getDelay(2)).isBetween(600L, 1000L);
    assertThat(backoff2.getDelay(3)).isBetween(600L, 1000L);
    final Backoff backoff3 =
        BackoffBuilder.afterCount(1).constantDelay(seconds(1)).withJitter(0.4f);
    assertThat(backoff3.getDelay(1)).isEqualTo(NO_DELAY);
    assertThat(backoff3.getDelay(2)).isBetween(600L, 1000L);
    assertThat(backoff3.getDelay(3)).isBetween(600L, 1000L);
  }

  @Test
  public void testExponential() {

    final Backoff backoff1 = BackoffBuilder.afterCount(0).exponentialDelay(1, TimeUnit.SECONDS);
    assertThat(backoff1.getDelay(1)).isEqualTo(1000);
    assertThat(backoff1.getDelay(2)).isEqualTo(2000);
    assertThat(backoff1.getDelay(3)).isEqualTo(4000);
    final Backoff backoff2 = BackoffBuilder.afterCount(0).exponentialDelay(seconds(1));
    assertThat(backoff2.getDelay(1)).isEqualTo(1000);
    assertThat(backoff2.getDelay(2)).isEqualTo(2000);
    assertThat(backoff2.getDelay(3)).isEqualTo(4000);
    assertThat(backoff2.getDelay(4)).isEqualTo(8000);
    final Backoff backoff3 = BackoffBuilder.afterCount(2).exponentialDelay(seconds(1));
    assertThat(backoff3.getDelay(1)).isEqualTo(NO_DELAY);
    assertThat(backoff3.getDelay(2)).isEqualTo(NO_DELAY);
    assertThat(backoff3.getDelay(3)).isEqualTo(1000);
    assertThat(backoff3.getDelay(4)).isEqualTo(2000);
  }

  @Test
  public void testExponentialCapped() {

    final Backoff backoff1 =
        BackoffBuilder.afterCount(0).exponentialDelay(1, TimeUnit.SECONDS).cappedTo(seconds(5));
    assertThat(backoff1.getDelay(1)).isEqualTo(1000);
    assertThat(backoff1.getDelay(2)).isEqualTo(2000);
    assertThat(backoff1.getDelay(3)).isEqualTo(4000);
    final Backoff backoff2 =
        BackoffBuilder.afterCount(0).exponentialDelay(seconds(1)).cappedTo(seconds(5));
    assertThat(backoff2.getDelay(1)).isEqualTo(1000);
    assertThat(backoff2.getDelay(2)).isEqualTo(2000);
    assertThat(backoff2.getDelay(3)).isEqualTo(4000);
    assertThat(backoff2.getDelay(4)).isEqualTo(5000);
    final Backoff backoff3 =
        BackoffBuilder.afterCount(2).exponentialDelay(seconds(1)).cappedTo(seconds(5));
    assertThat(backoff3.getDelay(1)).isEqualTo(NO_DELAY);
    assertThat(backoff3.getDelay(2)).isEqualTo(NO_DELAY);
    assertThat(backoff3.getDelay(3)).isEqualTo(1000);
    assertThat(backoff3.getDelay(4)).isEqualTo(2000);
  }

  @Test
  public void testExponentialCappedJitter() {

    final Backoff backoff1 = BackoffBuilder.afterCount(0)
                                           .exponentialDelay(seconds(1))
                                           .cappedTo(millis(1500))
                                           .withJitter(0.5f);
    assertThat(backoff1.getDelay(1)).isBetween(500L, 1000L);
    assertThat(backoff1.getDelay(2)).isBetween(750L, 1500L);
    final Backoff backoff2 = BackoffBuilder.afterCount(0)
                                           .exponentialDelay(seconds(1))
                                           .withJitter(0.3f)
                                           .cappedTo(seconds(5));
    assertThat(backoff2.getDelay(1)).isBetween(700L, 1000L);
    assertThat(backoff2.getDelay(2)).isBetween(1400L, 2000L);
    assertThat(backoff2.getDelay(3)).isBetween(2800L, 4000L);
    assertThat(backoff2.getDelay(4)).isEqualTo(5000);
    final Backoff backoff3 = BackoffBuilder.afterCount(2)
                                           .exponentialDelay(seconds(1))
                                           .withJitter(0.3f)
                                           .cappedTo(seconds(5));
    assertThat(backoff3.getDelay(1)).isEqualTo(NO_DELAY);
    assertThat(backoff3.getDelay(2)).isEqualTo(NO_DELAY);
    assertThat(backoff3.getDelay(3)).isBetween(700L, 1000L);
    assertThat(backoff3.getDelay(4)).isBetween(1400L, 2000L);
  }

  @Test
  public void testExponentialJitter() {

    final Backoff backoff1 =
        BackoffBuilder.afterCount(0).exponentialDelay(1, TimeUnit.SECONDS).withJitter(0.7f);
    assertThat(backoff1.getDelay(1)).isBetween(300L, 1000L);
    assertThat(backoff1.getDelay(2)).isBetween(600L, 2000L);
    final Backoff backoff2 =
        BackoffBuilder.afterCount(0).exponentialDelay(seconds(1)).withJitter(0.4f);
    assertThat(backoff2.getDelay(1)).isBetween(600L, 1000L);
    assertThat(backoff2.getDelay(2)).isBetween(1200L, 2000L);
    assertThat(backoff2.getDelay(3)).isBetween(2400L, 4000L);
    final Backoff backoff3 =
        BackoffBuilder.afterCount(1).exponentialDelay(seconds(1)).withJitter(0.4f);
    assertThat(backoff3.getDelay(1)).isEqualTo(NO_DELAY);
    assertThat(backoff3.getDelay(2)).isBetween(600L, 1000L);
    assertThat(backoff3.getDelay(3)).isBetween(1200L, 2000L);
  }

  @Test
  public void testJitter() {

    final Backoff backoff1 = BackoffBuilder.afterCount(0).jitterDelay(1, TimeUnit.SECONDS);
    assertThat(backoff1.getDelay(1)).isBetween(1000L, 3000L);
    assertThat(backoff1.getDelay(2)).isBetween(1000L, 9000L);
    assertThat(backoff1.getDelay(3)).isBetween(1000L, 27000L);
    final Backoff backoff2 = BackoffBuilder.afterCount(0).jitterDelay(seconds(1));
    assertThat(backoff2.getDelay(1)).isBetween(1000L, 3000L);
    assertThat(backoff2.getDelay(2)).isBetween(1000L, 9000L);
    assertThat(backoff2.getDelay(3)).isBetween(1000L, 27000L);
    final Backoff backoff3 = BackoffBuilder.afterCount(1).jitterDelay(seconds(1));
    assertThat(backoff3.getDelay(1)).isEqualTo(NO_DELAY);
    assertThat(backoff3.getDelay(2)).isBetween(1000L, 3000L);
    assertThat(backoff3.getDelay(3)).isBetween(1000L, 9000L);
  }

  @Test
  public void testJitterCapped() {

    final Backoff backoff1 =
        BackoffBuilder.afterCount(0).jitterDelay(1, TimeUnit.SECONDS).cappedTo(seconds(5));
    assertThat(backoff1.getDelay(1)).isBetween(1000L, 3000L);
    assertThat(backoff1.getDelay(2)).isBetween(1000L, 5000L);
    assertThat(backoff1.getDelay(3)).isBetween(1000L, 5000L);
    final Backoff backoff2 =
        BackoffBuilder.afterCount(0).jitterDelay(seconds(1)).cappedTo(seconds(5));
    assertThat(backoff2.getDelay(1)).isBetween(1000L, 3000L);
    assertThat(backoff2.getDelay(2)).isBetween(1000L, 5000L);
    assertThat(backoff2.getDelay(3)).isBetween(1000L, 5000L);
    final Backoff backoff3 =
        BackoffBuilder.afterCount(1).jitterDelay(seconds(1)).cappedTo(seconds(5));
    assertThat(backoff3.getDelay(1)).isEqualTo(NO_DELAY);
    assertThat(backoff3.getDelay(2)).isBetween(1000L, 3000L);
    assertThat(backoff3.getDelay(3)).isBetween(1000L, 5000L);
  }

  @Test
  public void testLinear() {

    final Backoff backoff1 = BackoffBuilder.afterCount(0).linearDelay(1, TimeUnit.SECONDS);
    assertThat(backoff1.getDelay(1)).isEqualTo(1000);
    assertThat(backoff1.getDelay(2)).isEqualTo(2000);
    assertThat(backoff1.getDelay(3)).isEqualTo(3000);
    final Backoff backoff2 = BackoffBuilder.afterCount(0).linearDelay(seconds(1));
    assertThat(backoff2.getDelay(1)).isEqualTo(1000);
    assertThat(backoff2.getDelay(2)).isEqualTo(2000);
    assertThat(backoff2.getDelay(3)).isEqualTo(3000);
    assertThat(backoff2.getDelay(4)).isEqualTo(4000);
    final Backoff backoff3 = BackoffBuilder.afterCount(2).linearDelay(seconds(1));
    assertThat(backoff3.getDelay(1)).isEqualTo(NO_DELAY);
    assertThat(backoff3.getDelay(2)).isEqualTo(NO_DELAY);
    assertThat(backoff3.getDelay(3)).isEqualTo(1000);
    assertThat(backoff3.getDelay(4)).isEqualTo(2000);
  }

  @Test
  public void testLinearCapped() {

    final Backoff backoff1 = BackoffBuilder.afterCount(0)
                                           .linearDelay(1, TimeUnit.SECONDS)
                                           .cappedTo(seconds(2).plus(millis(500)));
    assertThat(backoff1.getDelay(1)).isEqualTo(1000);
    assertThat(backoff1.getDelay(2)).isEqualTo(2000);
    assertThat(backoff1.getDelay(3)).isEqualTo(2500);
    final Backoff backoff2 =
        BackoffBuilder.afterCount(0).linearDelay(seconds(1)).cappedTo(seconds(2.5));
    assertThat(backoff2.getDelay(1)).isEqualTo(1000);
    assertThat(backoff2.getDelay(2)).isEqualTo(2000);
    assertThat(backoff2.getDelay(3)).isEqualTo(2500);
    assertThat(backoff2.getDelay(4)).isEqualTo(2500);
    final Backoff backoff3 =
        BackoffBuilder.afterCount(2).linearDelay(seconds(1)).cappedTo(seconds(1.5));
    assertThat(backoff3.getDelay(1)).isEqualTo(NO_DELAY);
    assertThat(backoff3.getDelay(2)).isEqualTo(NO_DELAY);
    assertThat(backoff3.getDelay(3)).isEqualTo(1000);
    assertThat(backoff3.getDelay(4)).isEqualTo(1500);
  }

  @Test
  public void testLinearCappedJitter() {

    final Backoff backoff1 = BackoffBuilder.afterCount(0)
                                           .linearDelay(seconds(1))
                                           .cappedTo(500, TimeUnit.MILLISECONDS)
                                           .withJitter(0.5f);
    assertThat(backoff1.getDelay(1)).isBetween(250L, 500L);
    assertThat(backoff1.getDelay(2)).isBetween(250L, 500L);
    final Backoff backoff2 =
        BackoffBuilder.afterCount(0).linearDelay(seconds(1)).withJitter(0.3f).cappedTo(millis(700));
    assertThat(backoff2.getDelay(1)).isEqualTo(700);
    assertThat(backoff2.getDelay(2)).isEqualTo(700);
    assertThat(backoff2.getDelay(3)).isEqualTo(700);
    final Backoff backoff3 =
        BackoffBuilder.afterCount(1).linearDelay(seconds(1)).withJitter(0.3f).cappedTo(millis(700));
    assertThat(backoff3.getDelay(1)).isEqualTo(NO_DELAY);
    assertThat(backoff3.getDelay(2)).isEqualTo(700);
    assertThat(backoff3.getDelay(3)).isEqualTo(700);
  }

  @Test
  public void testLinearJitter() {

    final Backoff backoff1 =
        BackoffBuilder.afterCount(0).linearDelay(1, TimeUnit.SECONDS).withJitter(0.7f);
    assertThat(backoff1.getDelay(1)).isBetween(300L, 1000L);
    assertThat(backoff1.getDelay(2)).isBetween(600L, 2000L);
    assertThat(backoff1.getDelay(3)).isBetween(900L, 3000L);
    final Backoff backoff2 = BackoffBuilder.afterCount(0).linearDelay(seconds(1)).withJitter(0.4f);
    assertThat(backoff2.getDelay(1)).isBetween(600L, 1000L);
    assertThat(backoff2.getDelay(2)).isBetween(1200L, 2000L);
    assertThat(backoff2.getDelay(3)).isBetween(1800L, 3000L);
    final Backoff backoff3 = BackoffBuilder.afterCount(1).linearDelay(seconds(1)).withJitter(0.4f);
    assertThat(backoff3.getDelay(1)).isEqualTo(NO_DELAY);
    assertThat(backoff3.getDelay(2)).isBetween(600L, 1000L);
    assertThat(backoff3.getDelay(3)).isBetween(1200L, 2000L);
  }

  @Test
  public void testNoDelay() {

    final Backoff backoff = BackoffBuilder.noDelay();
    assertThat(backoff.getDelay(1)).isEqualTo(NO_DELAY);
    assertThat(backoff.getDelay(2)).isEqualTo(NO_DELAY);
    assertThat(backoff.getDelay(3)).isEqualTo(NO_DELAY);
  }
}
