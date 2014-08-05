/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bmd.wtf.fll;

import com.bmd.wtf.crr.Current;
import com.bmd.wtf.crr.CurrentGenerator;
import com.bmd.wtf.crr.Currents;
import com.bmd.wtf.drp.Drops;
import com.bmd.wtf.flw.Barrage;
import com.bmd.wtf.flw.Collector;
import com.bmd.wtf.flw.Gate.Action;
import com.bmd.wtf.flw.Gate.ConditionEvaluator;
import com.bmd.wtf.flw.River;
import com.bmd.wtf.flw.Stream.Direction;
import com.bmd.wtf.lps.AbstractLeap;
import com.bmd.wtf.lps.FreeLeap;
import com.bmd.wtf.lps.Leap;
import com.bmd.wtf.lps.LeapGenerator;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.bmd.wtf.fll.Waterfall.fall;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Unit test for waterfall classes.
 * <p/>
 * Created by davide on 6/28/14.
 */
public class WaterfallTest extends TestCase {

    private static void testRiver(final River<Object> river) {

        river.push((Object) null)
             .push((Object[]) null)
             .push((Iterable<Object>) null)
             .push(new Object[0])
             .push(Arrays.asList())
             .push("push")
             .push(new Object[]{"push"})
             .push(new Object[]{"push", "push"})
             .push(Arrays.asList("push"))
             .push(Arrays.asList("push", "push"))
             .pushAfter(0, TimeUnit.MILLISECONDS, (Object) null)
             .pushAfter(0, TimeUnit.MILLISECONDS, (Object[]) null)
             .pushAfter(0, TimeUnit.MILLISECONDS, (Iterable<Object>) null)
             .pushAfter(0, TimeUnit.MILLISECONDS, new Object[0])
             .pushAfter(0, TimeUnit.MILLISECONDS, Arrays.asList())
             .pushAfter(0, TimeUnit.MILLISECONDS, "push")
             .pushAfter(0, TimeUnit.MILLISECONDS, new Object[]{"push"})
             .pushAfter(0, TimeUnit.MILLISECONDS, new Object[]{"push", "push"})
             .pushAfter(0, TimeUnit.MILLISECONDS, Arrays.asList("push"))
             .pushAfter(0, TimeUnit.MILLISECONDS, Arrays.asList("push", "push"))
             .forward(null)
             .forward(new RuntimeException("test"));

        river.discharge((Object) null)
             .discharge((Object[]) null)
             .discharge((Iterable<Object>) null)
             .discharge(new Object[0])
             .discharge(Arrays.asList())
             .discharge("push")
             .discharge(new Object[]{"push"})
             .discharge(new Object[]{"push", "push"})
             .discharge(Arrays.asList("push"))
             .discharge(Arrays.asList("push", "push"))
             .dischargeAfter(0, TimeUnit.MILLISECONDS, (Object) null)
             .dischargeAfter(0, TimeUnit.MILLISECONDS, (Object[]) null)
             .dischargeAfter(0, TimeUnit.MILLISECONDS, (Iterable<Object>) null)
             .dischargeAfter(0, TimeUnit.MILLISECONDS, new Object[0])
             .dischargeAfter(0, TimeUnit.MILLISECONDS, Arrays.asList())
             .dischargeAfter(0, TimeUnit.MILLISECONDS, "push")
             .dischargeAfter(0, TimeUnit.MILLISECONDS, new Object[]{"push"})
             .dischargeAfter(0, TimeUnit.MILLISECONDS, new Object[]{"push", "push"})
             .dischargeAfter(0, TimeUnit.MILLISECONDS, Arrays.asList("push"))
             .dischargeAfter(0, TimeUnit.MILLISECONDS, Arrays.asList("push", "push"))
             .discharge();
    }

    private static void testRivers(final River<Object> upRiver, final River<Object> downRiver) {

        testRiver(downRiver);
        testStream(downRiver);
        testRiver(upRiver);
        testStream(upRiver);

        assertThat(downRiver.size()).isEqualTo(1);
        assertThat(upRiver.size()).isEqualTo(1);
    }

    private static void testStream(final River<Object> river) {

        river.pushStream(0, (Object) null)
             .pushStream(0, (Object[]) null)
             .pushStream(0, (Iterable<Object>) null)
             .pushStream(0)
             .pushStream(0, Arrays.asList())
             .pushStream(0, "push")
             .pushStream(0, new Object[]{"push"})
             .pushStream(0, "push", "push")
             .pushStream(0, Arrays.asList("push"))
             .pushStream(0, Arrays.asList("push", "push"))
             .pushStreamAfter(0, 0, TimeUnit.MILLISECONDS, (Object) null)
             .pushStreamAfter(0, 0, TimeUnit.MILLISECONDS, (Object[]) null)
             .pushStreamAfter(0, 0, TimeUnit.MILLISECONDS, (Iterable<Object>) null)
             .pushStreamAfter(0, 0, TimeUnit.MILLISECONDS)
             .pushStreamAfter(0, 0, TimeUnit.MILLISECONDS, Arrays.asList())
             .pushStreamAfter(0, 0, TimeUnit.MILLISECONDS, "push")
             .pushStreamAfter(0, 0, TimeUnit.MILLISECONDS, new Object[]{"push"})
             .pushStreamAfter(0, 0, TimeUnit.MILLISECONDS, "push", "push")
             .pushStreamAfter(0, 0, TimeUnit.MILLISECONDS, Arrays.asList("push"))
             .pushStreamAfter(0, 0, TimeUnit.MILLISECONDS, Arrays.asList("push", "push"))
             .forwardStream(0, null)
             .forwardStream(0, new RuntimeException("test"));

        river.dischargeStream(0, (Object) null)
             .dischargeStream(0, (Object[]) null)
             .dischargeStream(0, (Iterable<Object>) null)
             .dischargeStream(0)
             .dischargeStream(0, Arrays.asList())
             .dischargeStream(0, "push")
             .dischargeStream(0, new Object[]{"push"})
             .dischargeStream(0, "push", "push")
             .dischargeStream(0, Arrays.asList("push"))
             .dischargeStream(0, Arrays.asList("push", "push"))
             .dischargeStreamAfter(0, 0, TimeUnit.MILLISECONDS, (Object) null)
             .dischargeStreamAfter(0, 0, TimeUnit.MILLISECONDS, (Object[]) null)
             .dischargeStreamAfter(0, 0, TimeUnit.MILLISECONDS, (Iterable<Object>) null)
             .dischargeStreamAfter(0, 0, TimeUnit.MILLISECONDS)
             .dischargeStreamAfter(0, 0, TimeUnit.MILLISECONDS, Arrays.asList())
             .dischargeStreamAfter(0, 0, TimeUnit.MILLISECONDS, "push")
             .dischargeStreamAfter(0, 0, TimeUnit.MILLISECONDS, new Object[]{"push"})
             .dischargeStreamAfter(0, 0, TimeUnit.MILLISECONDS, "push", "push")
             .dischargeStreamAfter(0, 0, TimeUnit.MILLISECONDS, Arrays.asList("push"))
             .dischargeStreamAfter(0, 0, TimeUnit.MILLISECONDS, Arrays.asList("push", "push"))
             .dischargeStream(0);
    }

    public void testBarrage() {

        final ArrayList<String> output = new ArrayList<String>();

        final Waterfall<String, List<String>, String> fall = fall().start(new FreeLeap<String>() {

            @Override
            public void onPush(final River<String> upRiver, final River<String> downRiver,
                    final int fallNumber, final String drop) {

                if ("test".equals(drop)) {

                    throw new IllegalArgumentException();
                }

                super.onPush(upRiver, downRiver, fallNumber, drop);
            }
        }).in(2).chain(new FreeLeap<String>() {

            @Override
            public void onPush(final River<String> upRiver, final River<String> downRiver,
                    final int fallNumber, final String drop) {

                if ((drop.length() == 0) || drop.toLowerCase().charAt(0) < 'm') {

                    if (fallNumber == 0) {

                        downRiver.push(drop);
                    }

                } else if (fallNumber == 1) {

                    downRiver.push(drop);
                }
            }
        }).chain(new LeapGenerator<String, List<String>>() {

            @Override
            public Leap<String, List<String>> start(final int fallNumber) {

                if (fallNumber == 0) {

                    return new AbstractLeap<String, List<String>>() {

                        private final ArrayList<String> mWords = new ArrayList<String>();

                        @Override
                        public void onPush(final River<String> upRiver,
                                final River<List<String>> downRiver, final int fallNumber,
                                final String drop) {

                            if ("atest".equals(drop)) {

                                throw new IllegalStateException();
                            }

                            mWords.add(drop);
                        }

                        @Override
                        public void onDischarge(final River<String> upRiver,
                                final River<List<String>> downRiver, final int fallNumber) {

                            Collections.sort(mWords);
                            downRiver.discharge(new ArrayList<String>(mWords));
                            mWords.clear();
                        }
                    };
                }

                return new AbstractLeap<String, List<String>>() {

                    private final ArrayList<String> mWords = new ArrayList<String>();

                    @Override
                    public void onPush(final River<String> upRiver,
                            final River<List<String>> downRiver, final int fallNumber,
                            final String drop) {

                        mWords.add(drop);
                    }

                    @Override
                    public void onDischarge(final River<String> upRiver,
                            final River<List<String>> downRiver, final int fallNumber) {

                        Collections.sort(mWords, Collections.reverseOrder());
                        downRiver.discharge(new ArrayList<String>(mWords));
                        mWords.clear();
                    }
                };
            }
        }).in(1).chain(new AbstractLeap<List<String>, String>() {

            private int mCount;

            private ArrayList<String> mList = new ArrayList<String>();

            @Override
            public void onPush(final River<List<String>> upRiver, final River<String> downRiver,
                    final int fallNumber, final List<String> drop) {

                if (mList.isEmpty() || drop.isEmpty()) {

                    mList.addAll(drop);

                } else {

                    final String first = drop.get(0);

                    if ((first.length() == 0) || first.toLowerCase().charAt(0) < 'm') {

                        mList.addAll(0, drop);

                    } else {

                        mList.addAll(drop);
                    }
                }

                if (++mCount == 2) {

                    downRiver.discharge(new ArrayList<String>(mList));
                    mList.clear();
                    mCount = 0;
                }
            }

            @Override
            public void onUnhandled(final River<List<String>> upRiver,
                    final River<String> downRiver, final int fallNumber,
                    final Throwable throwable) {

                // just ignore it
            }
        });

        fall.pull("Ciao", "This", "zOO", null, "is", "a", "3", "test", "1111", "CAPITAL", "atest")
            .allInto(output);

        assertThat(output).containsExactly("1111", "3", "CAPITAL", "Ciao", "a", "is", "zOO",
                                           "This");

        assertThat(fall.pull("test").all()).isEmpty();

        final Waterfall<Integer, Integer, Integer> fall0 = fall().start(Integer.class);
        final Waterfall<Integer, Integer, Integer> fall1 = fall().start(Integer.class);
        final Waterfall<Integer, Integer, Integer> fall2 = fall().start(Integer.class);
        final Waterfall<Integer, Integer, Integer> fall3 = fall().start(Integer.class);

        fall1.chain(fall0);
        fall2.chain(fall0);
        fall3.chain(fall0);

        final Collector<Integer> collector = fall0.collect();

        fall1.discharge(1);
        fall2.discharge(1);
        fall3.discharge(1);

        int i = 0;

        while (collector.hasNext()) {

            ++i;
            assertThat(collector.next()).isEqualTo(1);
        }

        assertThat(i).isEqualTo(3);

        assertThat(fall().in(3).start(Integer.class).pull(1).all()).containsExactly(1, 1, 1);
        assertThat(fall().in(3).start(Integer.class).in(1).pull(1).all()).containsExactly(1, 1, 1);

        assertThat(fall().inBackground(3).start(Integer.class).pull(1).all()).containsExactly(1, 1,
                                                                                              1);
        assertThat(fall().inBackground(3).start(Integer.class).in(1).pull(1).all()).containsExactly(
                1, 1, 1);
    }

    public void testChain() {

        assertThat(fall().chain().pull("test").all()).containsExactly("test");

        assertThat(fall().chain(new FreeLeap<Object>()).pull("test").all()).containsExactly("test");

        assertThat(fall().chain(new LeapGenerator<Object, String>() {

            @Override
            public Leap<Object, String> start(final int fallNumber) {

                return new AbstractLeap<Object, String>() {

                    @Override
                    public void onPush(final River<Object> upRiver, final River<String> downRiver,
                            final int fallNumber, final Object drop) {

                        downRiver.push(drop.toString());
                    }
                };
            }
        }).pull("test").all()).containsExactly("test");

        assertThat(fall().in(3).chain(new LeapGenerator<Object, String>() {

            @Override
            public Leap<Object, String> start(final int fallNumber) {

                return new AbstractLeap<Object, String>() {

                    @Override
                    public void onPush(final River<Object> upRiver, final River<String> downRiver,
                            final int fallNumber, final Object drop) {

                        downRiver.push(drop.toString());
                    }
                };
            }
        }).pull("test").all()).containsExactly("test", "test", "test");

        assertThat(fall().start().chain().pull("test").all()).containsExactly("test");

        assertThat(fall().start().chain(new FreeLeap<Object>()).pull("test").all()).containsExactly(
                "test");

        assertThat(fall().start().chain(new LeapGenerator<Object, String>() {

            @Override
            public Leap<Object, String> start(final int fallNumber) {

                return new AbstractLeap<Object, String>() {

                    @Override
                    public void onPush(final River<Object> upRiver, final River<String> downRiver,
                            final int fallNumber, final Object drop) {

                        downRiver.push(drop.toString());
                    }
                };
            }
        }).pull("test").all()).containsExactly("test");

        assertThat(fall().start().in(3).chain(new LeapGenerator<Object, String>() {

            @Override
            public Leap<Object, String> start(final int fallNumber) {

                return new AbstractLeap<Object, String>() {

                    @Override
                    public void onPush(final River<Object> upRiver, final River<String> downRiver,
                            final int fallNumber, final Object drop) {

                        downRiver.push(drop.toString());
                    }
                };
            }
        }).pull("test").all()).containsExactly("test", "test", "test");

        assertThat(fall().in(2).start().chain().pull("test").all()).containsExactly("test", "test");

        assertThat(fall().in(2)
                         .start()
                         .chain(new FreeLeap<Object>())
                         .pull("test")
                         .all()).containsExactly("test", "test");

        assertThat(fall().in(2).start().chain(new LeapGenerator<Object, String>() {

            @Override
            public Leap<Object, String> start(final int fallNumber) {

                return new AbstractLeap<Object, String>() {

                    @Override
                    public void onPush(final River<Object> upRiver, final River<String> downRiver,
                            final int fallNumber, final Object drop) {

                        downRiver.push(drop.toString());
                    }
                };
            }
        }).pull("test").all()).containsExactly("test", "test");

        assertThat(fall().in(2).start().in(3).chain(new LeapGenerator<Object, String>() {

            @Override
            public Leap<Object, String> start(final int fallNumber) {

                return new AbstractLeap<Object, String>() {

                    @Override
                    public void onPush(final River<Object> upRiver, final River<String> downRiver,
                            final int fallNumber, final Object drop) {

                        downRiver.push(drop.toString());
                    }
                };
            }
        }).pull("test").all()).containsExactly("test", "test", "test", "test", "test", "test");

        assertThat(fall().in(2)
                         .start()
                         .in(3)
                         .chain(new FreeLeap<Object>())
                         .pull("test")
                         .all()).containsExactly("test", "test", "test", "test", "test", "test");

        assertThat(fall().in(2).start().in(3).chain().pull("test").all()).containsExactly("test",
                                                                                          "test",
                                                                                          "test",
                                                                                          "test",
                                                                                          "test",
                                                                                          "test");

        assertThat(fall().start().in(3).chain().pull("test").all()).containsExactly("test", "test",
                                                                                    "test");

        assertThat(fall().asGate()
                         .chain()
                         .chain(new Classification<Leap<Object, Object>>() {})
                         .pull("test")
                         .all()).containsExactly("test");

        assertThat(fall().in(3)
                         .asGate()
                         .chain()
                         .in(1)
                         .chain(new Classification<Leap<Object, Object>>() {})
                         .pull("test")
                         .all()).containsExactly("test", "test", "test");

        assertThat(fall().in(2)
                         .asGate()
                         .chain()
                         .chain(new Classification<Leap<Object, Object>>() {})
                         .pull("test")
                         .all()).containsExactly("test", "test");

        assertThat(fall().in(2)
                         .asGate()
                         .chain()
                         .in(3)
                         .chain(new Classification<Leap<Object, Object>>() {})
                         .pull("test")
                         .all()).containsExactly("test", "test", "test", "test", "test", "test");

        assertThat(fall().asGate()
                         .chain()
                         .in(2)
                         .chain(new Classification<Leap<Object, Object>>() {})
                         .pull("test")
                         .all()).containsExactly("test", "test");

        final Waterfall<Object, Object, Object> fall0 = fall().start();
        final Waterfall<Object, Object, Object> fall1 = fall().in(2).start();
        fall1.chain(fall0);

        final Collector<Object> collector0 = fall0.collect();
        fall1.discharge("test");
        assertThat(collector0.all()).containsExactly("test", "test");

        final Waterfall<Object, Object, Object> fall2 = fall().in(2).start();
        final Waterfall<Object, Object, Object> fall3 = fall().in(2).start();
        fall3.chain(fall2);

        final Collector<Object> collector2 = fall2.collect();
        fall3.discharge("test");
        assertThat(collector2.all()).containsExactly("test", "test");

        final Waterfall<Object, Object, Object> fall4 = fall().in(2).start();
        final Waterfall<Object, Object, Object> fall5 = fall().in(3).start();
        fall5.chain(fall4);

        final Collector<Object> collector4 = fall4.collect();
        fall5.discharge("test");
        assertThat(collector4.all()).containsExactly("test", "test", "test", "test", "test",
                                                     "test");

        final Waterfall<Object, Object, Object> fall6 = fall().in(2).start();
        final Waterfall<Object, Object, Object> fall7 = fall().start();
        fall7.chain(fall6);

        final Collector<Object> collector6 = fall6.collect();
        fall7.discharge("test");
        assertThat(collector6.all()).containsExactly("test", "test");
    }

    public void testCollect() {

        final Waterfall<String, String, String> fall = fall().inBackground(1).start(String.class);
        Collector<String> collector = fall.collect();

        fall.source().discharge("test");

        assertThat(collector.next()).isEqualTo("test");

        collector = fall.collect();
        fall.source().dischargeAfter(2, TimeUnit.SECONDS, "test");

        try {

            collector.now().next();

            fail();

        } catch (final Exception ignored) {

        }

        assertThat(collector.now().all()).isEmpty();
        assertThat(collector.eventually().all()).containsExactly("test");

        collector = fall.collect();
        fall.source().dischargeAfter(2, TimeUnit.SECONDS, "test");

        try {

            collector.afterMax(100, TimeUnit.MILLISECONDS).next();

            fail();

        } catch (final Exception ignored) {

        }

        assertThat(collector.afterMax(100, TimeUnit.MILLISECONDS).all()).isEmpty();
        assertThat(collector.eventually().all()).containsExactly("test");

        collector = fall.collect();
        fall.source().dischargeAfter(2, TimeUnit.SECONDS, "test");

        try {

            collector.afterMax(100, TimeUnit.MILLISECONDS)
                     .eventuallyThrow(new IllegalStateException())
                     .next();

            fail();

        } catch (final Exception ignored) {

        }

        try {

            collector.afterMax(100, TimeUnit.MILLISECONDS)
                     .eventuallyThrow(new IllegalStateException())
                     .all();

            fail();

        } catch (final Exception ignored) {

        }

        assertThat(collector.eventually().all()).containsExactly("test");

        collector = fall.collect();
        fall.source().discharge("test");

        collector.next();

        try {

            collector.remove();

            fail();

        } catch (final Exception ignored) {

        }
    }

    public void testDeviate() {

        final Waterfall<Integer, Integer, Integer> fall1 = fall().start(Integer.class);
        final Waterfall<Integer, Integer, Integer> fall2 =
                fall1.chain(new AbstractLeap<Integer, Integer>() {

                    @Override
                    public void onPush(final River<Integer> upRiver, final River<Integer> downRiver,
                            final int fallNumber, final Integer drop) {

                        downRiver.push(drop - 1);

                        if (drop == 0) {

                            upRiver.deviate();
                            downRiver.deviate();
                        }
                    }
                });
        final Waterfall<Integer, Integer, String> fall3 =
                fall2.chain(new AbstractLeap<Integer, String>() {

                    @Override
                    public void onPush(final River<Integer> upRiver, final River<String> downRiver,
                            final int fallNumber, final Integer drop) {

                        downRiver.push(drop.toString());
                    }
                });
        final Waterfall<Integer, String, String> fall4 = fall3.chain();

        assertThat(fall4.pull(1).now().next()).isEqualTo("0");
        assertThat(fall4.pull(0).now().all()).isEmpty();
        assertThat(fall4.pull(1).now().all()).isEmpty();

        fall1.chain(new AbstractLeap<Integer, Integer>() {

            @Override
            public void onPush(final River<Integer> upRiver, final River<Integer> downRiver,
                    final int fallNumber, final Integer drop) {

                downRiver.push(drop);

                if (drop == -1) {

                    upRiver.drain();
                    downRiver.drain();
                }
            }
        }).chain(fall3);

        assertThat(fall4.pull(1).now().next()).isEqualTo("1");
        assertThat(fall4.pull(-1).now().all()).isEmpty();
        assertThat(fall4.pull(0).now().all()).isEmpty();

        fall1.chain(new AbstractLeap<Integer, Integer>() {

            @Override
            public void onPush(final River<Integer> upRiver, final River<Integer> downRiver,
                    final int fallNumber, final Integer drop) {

                downRiver.push(drop);
            }
        }).chain(fall3);

        assertThat(fall4.pull(0).now().all()).isEmpty();
        assertThat(fall4.pull(1).now().all()).isEmpty();

        final Waterfall<Object, Object, Object> fall5 = fall().start();
        final Waterfall<Object, Object, Object> fall6 = fall5.chain();
        final Waterfall<Object, Object, Object> fall7 = fall6.chain();

        assertThat(fall7.pull("test").all()).containsExactly("test");

        fall6.deviate();
        assertThat(fall6.pull("test").all()).containsExactly("test");
        assertThat(fall7.pull("test").afterMax(100, TimeUnit.MILLISECONDS).all()).isEmpty();

        fall6.chain(fall7);
        assertThat(fall7.pull("test").all()).containsExactly("test");

        fall6.deviate(Direction.DOWNSTREAM);
        assertThat(fall6.pull("test").all()).containsExactly("test");
        assertThat(fall7.pull("test").afterMax(100, TimeUnit.MILLISECONDS).all()).isEmpty();

        fall6.chain(fall7);
        assertThat(fall7.pull("test").all()).containsExactly("test");

        fall6.deviate(Direction.UPSTREAM);
        assertThat(fall6.pull("test").afterMax(100, TimeUnit.MILLISECONDS).all()).isEmpty();
        assertThat(fall7.pull("test").afterMax(100, TimeUnit.MILLISECONDS).all()).isEmpty();

        fall5.chain(fall6);
        assertThat(fall7.pull("test").all()).containsExactly("test");

        fall6.deviateStream(0);
        assertThat(fall6.pull("test").all()).containsExactly("test");
        assertThat(fall7.pull("test").afterMax(100, TimeUnit.MILLISECONDS).all()).isEmpty();

        fall6.chain(fall7);
        assertThat(fall7.pull("test").all()).containsExactly("test");

        fall6.deviateStream(0, Direction.DOWNSTREAM);
        assertThat(fall6.pull("test").all()).containsExactly("test");
        assertThat(fall7.pull("test").afterMax(100, TimeUnit.MILLISECONDS).all()).isEmpty();

        fall6.chain(fall7);
        assertThat(fall7.pull("test").all()).containsExactly("test");

        fall6.deviateStream(0, Direction.UPSTREAM);
        assertThat(fall6.pull("test").afterMax(100, TimeUnit.MILLISECONDS).all()).isEmpty();
        assertThat(fall7.pull("test").afterMax(100, TimeUnit.MILLISECONDS).all()).isEmpty();

        fall5.chain(fall6);
        assertThat(fall7.pull("test").all()).containsExactly("test");

        fall6.drain();
        assertThat(fall6.pull("test").all()).containsExactly("test");
        assertThat(fall7.pull("test").afterMax(100, TimeUnit.MILLISECONDS).all()).isEmpty();

        fall6.chain(fall7);
        assertThat(fall7.pull("test").all()).containsExactly("test");

        fall6.drain(Direction.DOWNSTREAM);
        assertThat(fall6.pull("test").all()).containsExactly("test");
        assertThat(fall7.pull("test").afterMax(100, TimeUnit.MILLISECONDS).all()).isEmpty();

        fall6.chain(fall7);
        assertThat(fall7.pull("test").all()).containsExactly("test");

        fall6.drain(Direction.UPSTREAM);
        assertThat(fall6.pull("test").afterMax(100, TimeUnit.MILLISECONDS).all()).isEmpty();
        assertThat(fall7.pull("test").afterMax(100, TimeUnit.MILLISECONDS).all()).isEmpty();

        fall5.chain(fall6);
        assertThat(fall7.pull("test").all()).containsExactly("test");

        fall6.drainStream(0);
        assertThat(fall6.pull("test").all()).containsExactly("test");
        assertThat(fall7.pull("test").afterMax(100, TimeUnit.MILLISECONDS).all()).isEmpty();

        fall6.chain(fall7);
        assertThat(fall7.pull("test").all()).containsExactly("test");

        fall6.drainStream(0, Direction.DOWNSTREAM);
        assertThat(fall6.pull("test").all()).containsExactly("test");
        assertThat(fall7.pull("test").afterMax(100, TimeUnit.MILLISECONDS).all()).isEmpty();

        fall6.chain(fall7);
        assertThat(fall7.pull("test").all()).containsExactly("test");

        fall6.drainStream(0, Direction.UPSTREAM);
        assertThat(fall6.pull("test").afterMax(100, TimeUnit.MILLISECONDS).all()).isEmpty();
        assertThat(fall7.pull("test").afterMax(100, TimeUnit.MILLISECONDS).all()).isEmpty();

        fall5.chain(fall6);
        assertThat(fall7.pull("test").all()).containsExactly("test");
    }

    public void testDeviateStream() {

        final Waterfall<Integer, Integer, Integer> fall1 = fall().start(Integer.class);
        final Waterfall<Integer, Integer, Integer> fall2 =
                fall1.chain(new AbstractLeap<Integer, Integer>() {

                    @Override
                    public void onPush(final River<Integer> upRiver, final River<Integer> downRiver,
                            final int fallNumber, final Integer drop) {

                        downRiver.push(drop - 1);

                        if (drop == 0) {

                            upRiver.deviateStream(0);
                            downRiver.deviateStream(0);
                        }
                    }
                });
        final Waterfall<Integer, Integer, String> fall3 =
                fall2.chain(new AbstractLeap<Integer, String>() {

                    @Override
                    public void onPush(final River<Integer> upRiver, final River<String> downRiver,
                            final int fallNumber, final Integer drop) {

                        downRiver.push(drop.toString());
                    }
                });
        final Waterfall<Integer, String, String> fall4 = fall3.chain();

        assertThat(fall4.pull(1).now().next()).isEqualTo("0");
        assertThat(fall4.pull(0).now().all()).isEmpty();
        assertThat(fall4.pull(1).now().all()).isEmpty();

        fall1.chain(new AbstractLeap<Integer, Integer>() {

            @Override
            public void onPush(final River<Integer> upRiver, final River<Integer> downRiver,
                    final int fallNumber, final Integer drop) {

                downRiver.push(drop);

                if (drop == -1) {

                    upRiver.drainStream(0);
                    downRiver.drainStream(0);
                }
            }
        }).chain(fall3);

        assertThat(fall4.pull(1).now().next()).isEqualTo("1");
        assertThat(fall4.pull(-1).now().all()).isEmpty();
        assertThat(fall4.pull(0).now().all()).isEmpty();

        fall1.chain(new AbstractLeap<Integer, Integer>() {

            @Override
            public void onPush(final River<Integer> upRiver, final River<Integer> downRiver,
                    final int fallNumber, final Integer drop) {

                downRiver.push(drop);
            }
        }).chain(fall3);

        assertThat(fall4.pull(0).now().all()).isEmpty();
        assertThat(fall4.pull(1).now().all()).isEmpty();
    }

    public void testDistribute() {

        final TraceLeap leap1 = new TraceLeap();

        final Waterfall<String, String, ?> source1 =
                fall().start(String.class).in(4).distribute().chain(leap1).source();

        final ArrayList<String> data = new ArrayList<String>();

        for (int i = 0; i < 30; ++i) {

            data.add(Integer.toString(i));
        }

        source1.push(data);
        assertThat(leap1.getData()).contains(data.toArray(new String[data.size()]));

        final IllegalStateException exception = new IllegalStateException();
        source1.forward(exception);
        assertThat(leap1.getUnhandled()).containsExactly(exception, exception, exception,
                                                         exception);

        source1.discharge();
        assertThat(leap1.getDischarges()).isEqualTo(4);

        assertThat(fall().distribute().pull("test", "test").all()).containsExactly("test", "test");
        assertThat(fall().start().in(1).distribute().pull("test", "test").all()).containsExactly(
                "test", "test");
        assertThat(fall().start().in(3).distribute().pull("test", "test").all()).containsExactly(
                "test", "test");

        final TraceLeap leap2 = new TraceLeap();

        final Waterfall<String, String, ?> source2 =
                fall().start(String.class).in(4).distribute(new Barrage<String>() {

                    @Override
                    public int onPush(final String drop) {

                        try {

                            return (Integer.parseInt(drop) % 4);

                        } catch (final NumberFormatException ignored) {

                        }

                        return DEFAULT_STREAM;
                    }
                }).chain(leap2).source();

        source2.push(data);
        assertThat(leap2.getData()).contains(data.toArray(new String[data.size()]));

        source2.forward(exception);
        assertThat(leap2.getUnhandled()).containsExactly(exception, exception, exception,
                                                         exception);

        source2.discharge();
        assertThat(leap2.getDischarges()).isEqualTo(4);

        assertThat(fall().distribute(new Barrage<Object>() {

            @Override
            public int onPush(final Object drop) {

                if (drop.equals("stop")) {

                    return NO_STREAM;
                }

                if (drop.equals("all")) {

                    return ALL_STREAMS;
                }

                return DEFAULT_STREAM;
            }
        }).pull("test", "stop").all()).containsExactly("test", "stop");
        assertThat(fall().start().in(1).distribute(new Barrage<Object>() {

            @Override
            public int onPush(final Object drop) {

                if (drop.equals("stop")) {

                    return NO_STREAM;
                }

                if (drop.equals("all")) {

                    return ALL_STREAMS;
                }

                return DEFAULT_STREAM;
            }
        }).pull("test", "stop", "all").all()).containsExactly("test", "stop", "all");
        assertThat(fall().start().in(3).distribute(new Barrage<Object>() {

            @Override
            public int onPush(final Object drop) {

                if (drop.equals("stop")) {

                    return NO_STREAM;
                }

                if (drop.equals("all")) {

                    return ALL_STREAMS;
                }

                return DEFAULT_STREAM;
            }
        }).pull("all", "test", "stop").all()).containsExactly("all", "all", "all", "test");
    }

    public void testError() throws InterruptedException {

        try {

            new DataStream<Object>(null,
                                   new DataFall<Object, Object>(fall().start(), Currents.straight(),
                                                                new FreeLeap<Object>(), 0));

            fail();

        } catch (final Exception ignored) {

        }

        try {

            new DataStream<Object>(new DataFall<Object, Object>(fall().start(), Currents.straight(),
                                                                new FreeLeap<Object>(), 0), null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            new DataFall<Object, Object>(null, Currents.straight(), new FreeLeap<Object>(), 0);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            new DataFall<Object, Object>(fall().start(), null, new FreeLeap<Object>(), 0);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            new DataFall<Object, Object>(fall().start(), Currents.straight(), null, 0);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            new WaterfallRiver<Object>(null, Direction.DOWNSTREAM);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            new WaterfallRiver<Object>(null, Direction.UPSTREAM);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            new BarrageLeap<Object>(null, 1);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            new BarrageLeap<Object>(new Barrage<Object>() {

                @Override
                public int onPush(final Object drop) {

                    return 0;
                }
            }, -1);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().start((Class) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().start((Classification) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().start((Leap) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().start((LeapGenerator) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().chain((Leap<Object, Object>) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().chain((LeapGenerator<Object, Object>) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().chain((Classification<Leap<Object, Object>>) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().chain(new Classification<Leap<Object, Object>>() {});

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().chain((Waterfall<Object, Object, Object>) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            final FreeLeap<Object> leap = new FreeLeap<Object>();

            fall().start(leap).chain(leap);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            final FreeLeap<Object> leap = new FreeLeap<Object>();

            fall().chain(leap).chain(leap);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().distribute(null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            final Barrage<Object> barrage = new Barrage<Object>() {

                @Override
                public int onPush(final Object drop) {

                    return 0;
                }
            };

            fall().in(2).distribute(barrage).distribute(barrage);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            final FreeLeap<Object> leap = new FreeLeap<Object>();

            fall().start(leap).start((Class) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            final FreeLeap<Object> leap = new FreeLeap<Object>();

            fall().start(leap).start((Classification) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            final FreeLeap<Object> leap = new FreeLeap<Object>();

            fall().start(leap).start((Leap) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            final FreeLeap<Object> leap = new FreeLeap<Object>();

            fall().start(leap).start((LeapGenerator) null);

            fail();

        } catch (final Exception ignored) {

        }


        try {

            fall().start().chain((Leap<Object, Object>) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().start().chain((LeapGenerator<Object, Object>) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().start().chain((Classification<Leap<Object, Object>>) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().start().chain(new Classification<Leap<Object, Object>>() {});

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().start().chain((Waterfall<Object, Object, Object>) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().start().chain(fall());

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().as((Class) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().as((Classification) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().as(Integer.class).chain(new FreeLeap<Object>());

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().as(new Classification<Integer>() {}).chain(new FreeLeap<Object>());

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().in(2).asGate().start(new LeapGenerator<Object, Object>() {

                @Override
                public Leap<Object, Object> start(final int fallNumber) {

                    return new FreeLeap<Object>();
                }
            });

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().in(2).asGate().chain(new LeapGenerator<Object, Object>() {

                @Override
                public Leap<Object, Object> start(final int fallNumber) {

                    return new FreeLeap<Object>();
                }
            });

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().in(2).start().asGate().chain(new LeapGenerator<Object, Object>() {

                @Override
                public Leap<Object, Object> start(final int fallNumber) {

                    return new FreeLeap<Object>();
                }
            });

            fail();

        } catch (final Exception ignored) {

        }

        try {

            final Waterfall<Object, Object, Object> waterfall = fall().start();

            waterfall.chain(waterfall);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            final Waterfall<Object, Object, Object> waterfall = fall().start();

            waterfall.chain().chain(waterfall);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().in((CurrentGenerator) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().in((Current) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().in(-1);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().inBackground(-1);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            final Waterfall<Object, Object, Object> waterfall = fall().start();

            waterfall.on((Class) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            final Waterfall<Object, Object, Object> waterfall = fall().start();

            waterfall.on((Classification) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            final Waterfall<Object, Object, Object> waterfall = fall().start();

            waterfall.on((Object) null);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            final Waterfall<Object, Object, Object> waterfall = fall().start();

            waterfall.on(new FreeLeap<Integer>());

            fail();

        } catch (final Exception ignored) {

        }

        try {

            final Waterfall<Object, Object, Object> waterfall = fall().start();

            waterfall.on(String.class);

            fail();

        } catch (final Exception ignored) {

        }

        try {

            fall().collect();

            fail();

        } catch (final Exception ignored) {

        }

        testRiver(fall());
        testRiver(fall().in(1));
        testRiver(fall().distribute());
        testRiver(fall().start());
        testStream(fall().start());
        testRiver(fall().chain());
        testStream(fall().chain());

        final Waterfall<Object, Object, Object> fall1 = fall().asGate()
                                                              .start(new LatchLeap())
                                                              .inBackground(1)
                                                              .chain(new TestLeap())
                                                              .chain(new FreeLeap<Object>() {

                                                                  @Override
                                                                  public void onUnhandled(
                                                                          final River<Object> upRiver,
                                                                          final River<Object> downRiver,
                                                                          final int fallNumber,
                                                                          final Throwable throwable) {

                                                                      if ((throwable != null)
                                                                              && !"test".equals(
                                                                              throwable.getMessage())) {

                                                                          downRiver.on(
                                                                                  LatchLeap.class)
                                                                                   .immediately()
                                                                                   .perform(
                                                                                           new Action<Void, LatchLeap>() {

                                                                                               @Override
                                                                                               public Void doOn(
                                                                                                       final LatchLeap leap,
                                                                                                       final Object... args) {

                                                                                                   leap.setFailed();

                                                                                                   return null;
                                                                                               }
                                                                                           });
                                                                      }
                                                                  }
                                                              });

        fall1.source().push("test");

        fall1.on(LatchLeap.class)
             .afterMax(30, TimeUnit.SECONDS)
             .eventuallyThrow(new IllegalStateException())
             .when(new ConditionEvaluator<LatchLeap>() {

                 @Override
                 public boolean isSatisfied(final LatchLeap leap) {

                     return (leap.getCount() == 3);
                 }
             })
             .perform(new Action<Void, LatchLeap>() {

                 @Override
                 public Void doOn(final LatchLeap leap, final Object... args) {

                     if (leap.isFailed()) {

                         fail();
                     }

                     return null;
                 }
             });

        final Waterfall<Object, Object, Object> fall2 = fall().asGate()
                                                              .start(new LatchLeap())
                                                              .inBackground(1)
                                                              .chain(new TestLeap())
                                                              .inBackground(1)
                                                              .chain(new FreeLeap<Object>() {

                                                                  @Override
                                                                  public void onUnhandled(
                                                                          final River<Object> upRiver,
                                                                          final River<Object> downRiver,
                                                                          final int fallNumber,
                                                                          final Throwable throwable) {

                                                                      if ((throwable != null)
                                                                              && !"test".equals(
                                                                              throwable.getMessage())) {

                                                                          downRiver.on(
                                                                                  LatchLeap.class)
                                                                                   .immediately()
                                                                                   .perform(
                                                                                           new Action<Void, LatchLeap>() {

                                                                                               @Override
                                                                                               public Void doOn(
                                                                                                       final LatchLeap leap,
                                                                                                       final Object... args) {

                                                                                                   leap.setFailed();

                                                                                                   return null;
                                                                                               }
                                                                                           });
                                                                      }
                                                                  }
                                                              });

        fall2.source().push("test");

        fall2.on(LatchLeap.class)
             .afterMax(3, TimeUnit.SECONDS)
             .eventuallyThrow(new IllegalStateException())
             .when(new ConditionEvaluator<LatchLeap>() {

                 @Override
                 public boolean isSatisfied(final LatchLeap leap) {

                     return (leap.getCount() == 3);
                 }
             })
             .perform(new Action<Void, LatchLeap>() {

                 @Override
                 public Void doOn(final LatchLeap leap, final Object... args) {

                     if (leap.isFailed()) {

                         fail();
                     }

                     return null;
                 }
             });
    }

    public void testGate() {

        final GateLeap2 gateLeap = new GateLeap2(1);

        final Waterfall<Object, Object, Object> fall = fall().as(GateLeap2.class).chain(gateLeap);

        assertThat(fall.on(GateLeap.class).immediately().perform(new Action<Integer, GateLeap>() {

            @Override
            public Integer doOn(final GateLeap leap, final Object... args) {

                return leap.getId();
            }
        })).isEqualTo(1);

        assertThat(fall.on(Classification.ofType(GateLeap2.class))
                       .immediately()
                       .perform(new Action<Integer, GateLeap>() {

                           @Override
                           public Integer doOn(final GateLeap leap, final Object... args) {

                               return leap.getId();
                           }
                       })).isEqualTo(1);

        assertThat(fall.on(gateLeap).immediately().perform(new Action<Integer, GateLeap>() {

            @Override
            public Integer doOn(final GateLeap leap, final Object... args) {

                return leap.getId();
            }
        })).isEqualTo(1);

        final Waterfall<Object, Object, Object> fall1 = fall.lock((Leap) null)
                                                            .lock(new FreeLeap<Object>())
                                                            .lock(gateLeap)
                                                            .as(Classification.ofType(
                                                                    GateLeap.class))
                                                            .chain(new GateLeap());

        assertThat(fall1.on(GateLeap.class).immediately().perform(new Action<Integer, GateLeap>() {

            @Override
            public Integer doOn(final GateLeap leap, final Object... args) {

                return leap.getId();
            }
        })).isEqualTo(0);

        try {

            fall1.on(GateLeap2.class);

            fail();

        } catch (final Exception ignored) {

        }

        final Waterfall<Object, Object, Object> fall2 =
                fall1.lock(Classification.ofType(String.class))
                     .lock(Classification.ofType(GateLeap.class))
                     .asGate()
                     .chain(new GateLeap2(2));

        assertThat(fall2.on(GateLeap.class).immediately().perform(new Action<Integer, GateLeap>() {

            @Override
            public Integer doOn(final GateLeap leap, final Object... args) {

                return leap.getId();
            }
        })).isEqualTo(2);

        assertThat(fall2.on(Classification.ofType(GateLeap2.class))
                        .immediately()
                        .perform(new Action<Integer, GateLeap>() {

                            @Override
                            public Integer doOn(final GateLeap leap, final Object... args) {

                                return leap.getId();
                            }
                        })).isEqualTo(2);

        assertThat(fall2.lock(Classification.ofType(GateLeap2.class))
                        .in(new CurrentGenerator() {

                            @Override
                            public Current create(final int fallNumber) {

                                return Currents.straight();
                            }
                        })
                        .in(3)
                        .asGate()
                        .chain(new GateLeap2(3))
                        .on(GateLeap2.class)
                        .immediately()
                        .perform(new Action<Integer, GateLeap>() {

                            @Override
                            public Integer doOn(final GateLeap leap, final Object... args) {

                                return leap.getId();
                            }
                        })).isEqualTo(3);
    }

    public void testIn() {

        assertThat(fall().in(1).in(Currents.straight()).chain().in(new CurrentGenerator() {

            @Override
            public Current create(final int fallNumber) {

                return Currents.straight();
            }
        }).chain().pull("test").all()).containsExactly("test");

        assertThat(fall().in(3).in(Currents.straight()).chain().in(new CurrentGenerator() {

            @Override
            public Current create(final int fallNumber) {

                return Currents.straight();
            }
        }).chain().pull("test").all()).containsExactly("test", "test", "test");

        assertThat(fall().in(3)
                         .in(Currents.straight())
                         .chain()
                         .inBackground(2)
                         .chain()
                         .pull("test")
                         .all()).containsExactly("test", "test", "test", "test", "test", "test");

        assertThat(fall().in(3).inBackground().chain().pull("test").all()).contains("test");
    }

    public void testJoin() {

        final Waterfall<Character, Integer, Integer> fall0 =
                fall().start(new AbstractLeap<Character, Integer>() {

                    private final StringBuffer mBuffer = new StringBuffer();

                    @Override
                    public void onPush(final River<Character> upRiver,
                            final River<Integer> downRiver, final int fallNumber,
                            final Character drop) {

                        mBuffer.append(drop);
                    }

                    @Override
                    public void onDischarge(final River<Character> upRiver,
                            final River<Integer> downRiver, final int fallNumber) {

                        downRiver.discharge(Integer.valueOf(mBuffer.toString()));

                        mBuffer.setLength(0);
                    }
                }).chain();

        final Waterfall<Character, Integer, Integer> fall1 = fall0.chain(new FreeLeap<Integer>() {

            private int mSum;

            @Override
            public void onPush(final River<Integer> upRiver, final River<Integer> downRiver,
                    final int fallNumber, final Integer drop) {

                mSum += drop;
            }

            @Override
            public void onDischarge(final River<Integer> upRiver, final River<Integer> downRiver,
                    final int fallNumber) {

                downRiver.discharge(new Integer[]{mSum});

                mSum = 0;
            }
        });

        final Waterfall<Integer, Integer, Integer> fall2 = fall().start(Integer.class);
        fall2.chain(fall0);
        fall2.source().discharge(Drops.asList(0, 1, 2, 3));

        final ArrayList<Integer> output = new ArrayList<Integer>(1);

        fall1.pull('0', '1', '2', '3').nextInto(output);
        assertThat(output).containsExactly(129);

        fall2.source().drain();

        final Waterfall<Integer, Integer, Integer> fall3 = fall().start(Integer.class);

        fall1.source().push(Drops.asList('0', '1', '2', '3'));
        fall3.chain(fall0);
        fall3.source().discharge(Drops.asList(4, 5, -4));
        fall2.source().discharge(77);

        output.clear();
        fall1.pull().allInto(output);

        assertThat(output).containsExactly(128);

        final Waterfall<Integer, Integer, Integer> fall4 = fall().start(new FreeLeap<Integer>() {

            private int mAbsSum;

            @Override
            public void onPush(final River<Integer> upRiver, final River<Integer> downRiver,
                    final int fallNumber, final Integer drop) {

                mAbsSum += Math.abs(drop);
            }

            @Override
            public void onDischarge(final River<Integer> upRiver, final River<Integer> downRiver,
                    final int fallNumber) {

                downRiver.discharge(mAbsSum);
            }
        });

        fall0.chain(fall4);

        fall3.source().discharge(Arrays.asList(4, 5, -4));

        output.clear();
        fall1.pull(Drops.asList('0', '1', '2', '3')).allInto(output);
        fall4.pull().nextInto(output);

        assertThat(output).containsExactly(128, 136);

        try {

            final Waterfall<Object, Object, Object> fall = fall().start();

            fall().chain(fall);

            fail();

        } catch (final Exception ignored) {

        }
    }

    public void testStart() {

        assertThat(fall().start().pull("test").all()).containsExactly("test");

        assertThat(fall().start(String.class).pull("test").all()).containsExactly("test");

        assertThat(
                fall().start(new Classification<String>() {}).pull("test").all()).containsExactly(
                "test");

        assertThat(fall().start(new FreeLeap<String>()).pull("test").all()).containsExactly("test");

        assertThat(fall().start(new LeapGenerator<String, Object>() {

            @Override
            public Leap<String, Object> start(final int fallNumber) {

                return new AbstractLeap<String, Object>() {

                    @Override
                    public void onPush(final River<String> upRiver, final River<Object> downRiver,
                            final int fallNumber, final String drop) {

                        downRiver.push(drop);
                    }
                };
            }
        }).pull("test").all()).containsExactly("test");

        assertThat(fall().in(3).start(new LeapGenerator<String, Object>() {

            @Override
            public Leap<String, Object> start(final int fallNumber) {

                return new AbstractLeap<String, Object>() {

                    @Override
                    public void onPush(final River<String> upRiver, final River<Object> downRiver,
                            final int fallNumber, final String drop) {

                        downRiver.push(drop);
                    }
                };
            }
        }).pull("test").all()).containsExactly("test", "test", "test");
    }

    private static class GateLeap extends FreeLeap<Object> {

        public int getId() {

            return 0;
        }
    }

    private static class GateLeap2 extends GateLeap {

        private int mId;

        public GateLeap2(final int id) {

            mId = id;
        }

        @Override
        public int getId() {

            return mId;
        }
    }

    private static class LatchLeap extends FreeLeap<Object> {

        private int mCount;

        private boolean mFailed;

        public int getCount() {

            return mCount;
        }

        public void incCount() {

            ++mCount;
        }

        public boolean isFailed() {

            return mFailed;
        }

        public void setFailed() {

            mFailed = true;
        }
    }

    private static class TestLeap extends FreeLeap<Object> {

        public boolean mDischarged;

        public boolean mPushed;

        public boolean mThrown;

        @Override
        public void onDischarge(final River<Object> upRiver, final River<Object> downRiver,
                final int fallNumber) {

            if (isFailed(upRiver) || mDischarged) {

                return;
            }

            mDischarged = true;

            try {

                testRivers(upRiver, downRiver);

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        try {

                            testRivers(upRiver, downRiver);

                        } catch (final Throwable ignored) {

                            setFailed(downRiver);

                        } finally {

                            incCount(downRiver);
                        }
                    }
                }).start();

            } catch (final Throwable ignored) {

                setFailed(downRiver);
            }
        }

        private void incCount(final River<Object> river) {

            river.on(LatchLeap.class).immediately().perform(new Action<Void, LatchLeap>() {

                @Override
                public Void doOn(final LatchLeap leap, final Object... args) {

                    leap.incCount();

                    return null;
                }
            });
        }

        private boolean isFailed(final River<Object> river) {

            return river.on(LatchLeap.class)
                        .immediately()
                        .perform(new Action<Boolean, LatchLeap>() {

                            @Override
                            public Boolean doOn(final LatchLeap leap, final Object... args) {

                                return leap.isFailed();
                            }
                        });
        }

        private void setFailed(final River<Object> river) {

            river.on(LatchLeap.class).immediately().perform(new Action<Void, LatchLeap>() {

                @Override
                public Void doOn(final LatchLeap leap, final Object... args) {

                    leap.setFailed();

                    return null;
                }
            });
        }

        @Override
        public void onPush(final River<Object> upRiver, final River<Object> downRiver,
                final int fallNumber, final Object drop) {

            if (isFailed(downRiver) || mPushed) {

                return;
            }

            mPushed = true;

            try {

                testRivers(upRiver, downRiver);

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        try {

                            testRivers(upRiver, downRiver);

                        } catch (final Throwable ignored) {

                            setFailed(upRiver);

                        } finally {

                            incCount(upRiver);
                        }
                    }
                }).start();

            } catch (final Throwable ignored) {

                setFailed(downRiver);
            }
        }

        @Override
        public void onUnhandled(final River<Object> upRiver, final River<Object> downRiver,
                final int fallNumber, final Throwable throwable) {

            if (isFailed(upRiver) || mThrown) {

                return;
            }

            mThrown = true;

            try {

                testRivers(upRiver, downRiver);

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        try {

                            testRivers(upRiver, downRiver);

                        } catch (final Throwable ignored) {

                            setFailed(downRiver);

                        } finally {

                            incCount(downRiver);
                        }
                    }
                }).start();

            } catch (final Throwable ignored) {

                setFailed(upRiver);
            }
        }
    }

    private class TraceLeap extends FreeLeap<String> {

        private final ArrayList<String> mData = new ArrayList<String>();

        private final ArrayList<Throwable> mThrows = new ArrayList<Throwable>();

        private int mDischargeCount;

        public List<String> getData() {

            return mData;
        }

        public int getDischarges() {

            return mDischargeCount;
        }

        public List<Throwable> getUnhandled() {

            return mThrows;
        }

        @Override
        public void onDischarge(final River<String> upRiver, final River<String> downRiver,
                final int fallNumber) {

            ++mDischargeCount;

            super.onDischarge(upRiver, downRiver, fallNumber);
        }

        @Override
        public void onPush(final River<String> upRiver, final River<String> downRiver,
                final int fallNumber, final String drop) {

            mData.add(drop);

            super.onPush(upRiver, downRiver, fallNumber, drop);
        }

        @Override
        public void onUnhandled(final River<String> upRiver, final River<String> downRiver,
                final int fallNumber, final Throwable throwable) {

            mThrows.add(throwable);

            super.onUnhandled(upRiver, downRiver, fallNumber, throwable);
        }
    }
}