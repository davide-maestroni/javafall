/*
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
package com.github.dm.jrt.function;

import com.github.dm.jrt.channel.ResultChannel;
import com.github.dm.jrt.core.JRoutine;
import com.github.dm.jrt.invocation.CommandInvocation;
import com.github.dm.jrt.invocation.FilterInvocation;
import com.github.dm.jrt.invocation.Invocation;
import com.github.dm.jrt.invocation.InvocationFactory;
import com.github.dm.jrt.invocation.PassingInvocation;
import com.github.dm.jrt.routine.Routine;

import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.github.dm.jrt.function.Functions.biSink;
import static com.github.dm.jrt.function.Functions.constant;
import static com.github.dm.jrt.function.Functions.consumerCommand;
import static com.github.dm.jrt.function.Functions.consumerFactory;
import static com.github.dm.jrt.function.Functions.consumerFilter;
import static com.github.dm.jrt.function.Functions.functionFactory;
import static com.github.dm.jrt.function.Functions.functionFilter;
import static com.github.dm.jrt.function.Functions.identity;
import static com.github.dm.jrt.function.Functions.isNull;
import static com.github.dm.jrt.function.Functions.negative;
import static com.github.dm.jrt.function.Functions.notNull;
import static com.github.dm.jrt.function.Functions.positive;
import static com.github.dm.jrt.function.Functions.predicateFilter;
import static com.github.dm.jrt.function.Functions.sink;
import static com.github.dm.jrt.function.Functions.supplierCommand;
import static com.github.dm.jrt.function.Functions.supplierFactory;
import static com.github.dm.jrt.function.Functions.wrapBiConsumer;
import static com.github.dm.jrt.function.Functions.wrapBiFunction;
import static com.github.dm.jrt.function.Functions.wrapConsumer;
import static com.github.dm.jrt.function.Functions.wrapFunction;
import static com.github.dm.jrt.function.Functions.wrapPredicate;
import static com.github.dm.jrt.function.Functions.wrapSupplier;
import static com.github.dm.jrt.util.TimeDuration.seconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Functions unit tests.
 * <p/>
 * Created by davide-maestroni on 09/24/2015.
 */
public class FunctionsTest {

    private static CommandInvocation<String> createCommand() {

        return consumerCommand(new Consumer<ResultChannel<String>>() {

            public void accept(final ResultChannel<String> result) {

                result.pass("test");
            }
        });
    }

    private static CommandInvocation<String> createCommand2() {

        return supplierCommand(new Supplier<String>() {

            public String get() {

                return "test";
            }
        });
    }

    private static InvocationFactory<Object, String> createFactory() {

        return supplierFactory(new Supplier<Invocation<Object, String>>() {

            public Invocation<Object, String> get() {

                return new FilterInvocation<Object, String>() {

                    public void onInput(final Object input,
                            @NotNull final ResultChannel<String> result) {

                        result.pass(input.toString());
                    }
                };
            }
        });
    }

    private static FilterInvocation<Object, String> createFilter() {

        return consumerFilter(new BiConsumer<Object, ResultChannel<String>>() {

            public void accept(final Object o, final ResultChannel<String> result) {

                result.pass(o.toString());
            }
        });
    }

    private static FilterInvocation<Object, String> createFilter2() {

        return functionFilter(new com.github.dm.jrt.function.Function<Object, String>() {

            public String apply(final Object o) {

                return o.toString();
            }
        });
    }

    private static FilterInvocation<String, String> createFilter3() {

        return predicateFilter(new Predicate<String>() {

            public boolean test(final String s) {

                return s.length() > 0;
            }
        });
    }

    private static InvocationFactory<?, String> createFunction() {

        return consumerFactory(new BiConsumer<List<?>, ResultChannel<String>>() {

            public void accept(final List<?> objects, final ResultChannel<String> result) {

                for (final Object object : objects) {

                    result.pass(object.toString());
                }
            }
        });
    }

    private static InvocationFactory<?, String> createFunction2() {

        return functionFactory(new com.github.dm.jrt.function.Function<List<?>, String>() {

            public String apply(final List<?> objects) {

                final StringBuilder builder = new StringBuilder();

                for (final Object object : objects) {

                    builder.append(object.toString());
                }

                return builder.toString();
            }
        });
    }

    @Test
    public void testBiConsumer() {

        final TestBiConsumer consumer1 = new TestBiConsumer();
        final BiConsumerWrapper<Object, Object> consumer2 = wrapBiConsumer(consumer1);
        assertThat(wrapBiConsumer(consumer2)).isSameAs(consumer2);
        consumer2.accept("test", "test");
        assertThat(consumer1.isCalled()).isTrue();
        consumer1.reset();
        final TestBiConsumer consumer3 = new TestBiConsumer();
        final BiConsumerWrapper<Object, Object> consumer4 = consumer2.andThen(consumer3);
        consumer4.accept("test", "test");
        assertThat(consumer1.isCalled()).isTrue();
        assertThat(consumer3.isCalled()).isTrue();
    }

    @Test
    public void testBiConsumerContext() {

        final BiConsumerWrapper<Object, Object> consumer1 =
                wrapBiConsumer(new TestBiConsumer()).andThen(new TestBiConsumer());
        assertThat(consumer1.hasStaticContext()).isTrue();
        assertThat(consumer1.andThen(new BiConsumer<Object, Object>() {

            public void accept(final Object o, final Object o2) {

            }
        }).hasStaticContext()).isFalse();
        assertThat(consumer1.andThen(consumer1).hasStaticContext()).isTrue();
    }

    @Test
    public void testBiConsumerEquals() {

        final TestBiConsumer consumer1 = new TestBiConsumer();
        assertThat(wrapBiConsumer(consumer1)).isEqualTo(wrapBiConsumer(consumer1));
        final BiConsumerWrapper<Object, Object> consumer2 = wrapBiConsumer(consumer1);
        assertThat(consumer2).isEqualTo(consumer2);
        assertThat(consumer2).isNotEqualTo(null);
        assertThat(consumer2).isNotEqualTo("test");
        assertThat(wrapBiConsumer(consumer1).andThen(consumer2).hashCode()).isEqualTo(
                consumer2.andThen(consumer2).hashCode());
        assertThat(wrapBiConsumer(consumer1).andThen(consumer2)).isEqualTo(
                consumer2.andThen(consumer2));
        assertThat(consumer2.andThen(consumer2)).isEqualTo(
                wrapBiConsumer(consumer1).andThen(consumer2));
        assertThat(wrapBiConsumer(consumer1).andThen(consumer2).hashCode()).isEqualTo(
                consumer2.andThen(consumer1).hashCode());
        assertThat(wrapBiConsumer(consumer1).andThen(consumer2)).isEqualTo(
                consumer2.andThen(consumer1));
        assertThat(consumer2.andThen(consumer1)).isEqualTo(
                wrapBiConsumer(consumer1).andThen(consumer2));
        assertThat(wrapBiConsumer(consumer1).andThen(consumer2).hashCode()).isNotEqualTo(
                consumer2.andThen(consumer2.andThen(consumer1)).hashCode());
        assertThat(wrapBiConsumer(consumer1).andThen(consumer2)).isNotEqualTo(
                consumer2.andThen(consumer2.andThen(consumer1)));
        assertThat(consumer2.andThen(consumer2.andThen(consumer1))).isNotEqualTo(
                wrapBiConsumer(consumer1).andThen(consumer2));
        assertThat(wrapBiConsumer(consumer1).andThen(consumer1).hashCode()).isNotEqualTo(
                consumer2.andThen(consumer2.andThen(consumer1)).hashCode());
        assertThat(wrapBiConsumer(consumer1).andThen(consumer1)).isNotEqualTo(
                consumer2.andThen(consumer2.andThen(consumer1)));
        assertThat(consumer2.andThen(consumer2.andThen(consumer1))).isNotEqualTo(
                wrapBiConsumer(consumer1).andThen(consumer1));
        assertThat(consumer2.andThen(consumer1).hashCode()).isNotEqualTo(
                consumer2.andThen(biSink()).hashCode());
        assertThat(consumer2.andThen(consumer1)).isNotEqualTo(consumer2.andThen(biSink()));
        assertThat(consumer2.andThen(biSink())).isNotEqualTo(consumer2.andThen(consumer1));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testBiConsumerError() {

        try {

            new BiConsumerWrapper<Object, Object>(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            new BiConsumerWrapper<Object, Object>(Collections.<BiConsumer<?, ?>>emptyList());

            fail();

        } catch (final IllegalArgumentException ignored) {

        }

        try {

            wrapBiConsumer(new TestBiConsumer()).andThen(null);

            fail();

        } catch (final NullPointerException ignored) {

        }
    }

    @Test
    public void testBiFunction() {

        final TestBiFunction function1 = new TestBiFunction();
        final BiFunctionWrapper<Object, Object, Object> function2 = wrapBiFunction(function1);
        assertThat(wrapBiFunction(function2)).isSameAs(function2);
        assertThat(function2.apply("test", function1)).isSameAs(function1);
        assertThat(function1.isCalled()).isTrue();
        function1.reset();
        final TestFunction function = new TestFunction();
        final BiFunctionWrapper<Object, Object, Object> function3 = function2.andThen(function);
        assertThat(function3.apply("test", function1)).isSameAs(function1);
        assertThat(function1.isCalled()).isTrue();
        assertThat(function.isCalled()).isTrue();
        Assertions.assertThat(
                Functions.<String, String>first().andThen(new Function<String, Integer>() {

                    public Integer apply(final String s) {

                        return s.length();
                    }
                }).andThen(new Function<Integer, Integer>() {

                    public Integer apply(final Integer integer) {

                        return integer * 3;
                    }
                }).apply("test", "long test")).isEqualTo(12);
        Assertions.assertThat(
                Functions.<String, Integer>second().andThen(new Function<Integer, Integer>() {

                    public Integer apply(final Integer integer) {

                        return integer + 2;
                    }
                }).apply("test", 3)).isEqualTo(5);
    }

    @Test
    public void testBiFunctionContext() {

        final BiFunctionWrapper<Object, Object, Object> function1 =
                wrapBiFunction(new TestBiFunction()).andThen(new TestFunction());
        assertThat(function1.hasStaticContext()).isTrue();
        assertThat(function1.andThen(new Function<Object, Object>() {

            public Object apply(final Object o) {

                return null;
            }
        }).hasStaticContext()).isFalse();
        assertThat(function1.andThen(identity()).hasStaticContext()).isTrue();
    }

    @Test
    public void testBiFunctionEquals() {

        final TestBiFunction function1 = new TestBiFunction();
        assertThat(wrapBiFunction(function1)).isEqualTo(wrapBiFunction(function1));
        final BiFunctionWrapper<Object, Object, Object> function2 = wrapBiFunction(function1);
        assertThat(function2).isEqualTo(function2);
        assertThat(function2).isNotEqualTo(null);
        assertThat(function2).isNotEqualTo("test");
        final TestFunction function = new TestFunction();
        assertThat(wrapBiFunction(function1).andThen(function).hashCode()).isEqualTo(
                function2.andThen(function).hashCode());
        assertThat(wrapBiFunction(function1).andThen(function)).isEqualTo(
                function2.andThen(function));
        assertThat(function2.andThen(function)).isEqualTo(
                wrapBiFunction(function1).andThen(function));
        assertThat(wrapBiFunction(function1).andThen(wrapFunction(function)).hashCode()).isEqualTo(
                function2.andThen(function).hashCode());
        assertThat(wrapBiFunction(function1).andThen(wrapFunction(function))).isEqualTo(
                function2.andThen(function));
        assertThat(function2.andThen(function)).isEqualTo(
                wrapBiFunction(function1).andThen(wrapFunction(function)));
        assertThat(
                wrapBiFunction(function1).andThen(wrapFunction(function)).hashCode()).isNotEqualTo(
                function2.andThen(wrapFunction(function).andThen(function)).hashCode());
        assertThat(wrapBiFunction(function1).andThen(wrapFunction(function))).isNotEqualTo(
                function2.andThen(wrapFunction(function).andThen(function)));
        assertThat(function2.andThen(wrapFunction(function).andThen(function))).isNotEqualTo(
                wrapBiFunction(function1).andThen(wrapFunction(function)));
        assertThat(wrapBiFunction(function1).andThen(function).hashCode()).isNotEqualTo(
                function2.andThen(wrapFunction(function).andThen(function)).hashCode());
        assertThat(wrapBiFunction(function1).andThen(function)).isNotEqualTo(
                function2.andThen(wrapFunction(function).andThen(function)));
        assertThat(function2.andThen(wrapFunction(function).andThen(function))).isNotEqualTo(
                wrapBiFunction(function1).andThen(function));
        assertThat(function2.andThen(function).hashCode()).isNotEqualTo(
                function2.andThen(identity()).hashCode());
        assertThat(function2.andThen(function)).isNotEqualTo(function2.andThen(identity()));
        assertThat(function2.andThen(identity())).isNotEqualTo(function2.andThen(function));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testBiFunctionError() {

        try {

            new BiFunctionWrapper<Object, Object, Object>(null, wrapFunction(new TestFunction()));

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            new BiFunctionWrapper<Object, Object, Object>(new TestBiFunction(), null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            wrapBiFunction(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            wrapBiFunction(new TestBiFunction()).andThen(null);

            fail();

        } catch (final NullPointerException ignored) {

        }
    }

    @Test
    public void testBiSink() {

        final TestBiConsumer consumer1 = new TestBiConsumer();
        final BiConsumerWrapper<Object, Object> consumer2 = biSink().andThen(consumer1);
        consumer2.accept("test", "test");
        assertThat(consumer1.isCalled()).isTrue();
        assertThat(consumer2.hasStaticContext()).isTrue();
        assertThat(biSink()).isSameAs(biSink());
    }

    @Test
    public void testCommand() {

        final Routine<Void, String> routine = JRoutine.on(createCommand()).buildRoutine();
        assertThat(routine.asyncCall().afterMax(seconds(1)).all()).containsOnly("test");
    }

    @Test
    public void testCommand2() {

        final Routine<Void, String> routine = JRoutine.on(createCommand2()).buildRoutine();
        assertThat(routine.asyncCall().afterMax(seconds(1)).all()).containsOnly("test");
    }

    @Test
    public void testCommand2Equals() {

        final InvocationFactory<Void, String> factory = createCommand2();
        final SupplierWrapper<String> constant = constant("test");
        assertThat(factory).isEqualTo(factory);
        assertThat(factory).isEqualTo(createCommand2());
        assertThat(factory).isNotEqualTo(supplierCommand(constant));
        assertThat(factory).isNotEqualTo(createFactory());
        assertThat(factory).isNotEqualTo("");
        assertThat(factory.hashCode()).isEqualTo(createCommand2().hashCode());
        assertThat(supplierCommand(constant)).isEqualTo(supplierCommand(constant));
        assertThat(supplierCommand(constant).hashCode()).isEqualTo(
                supplierCommand(constant).hashCode());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testCommand2Error() {

        try {

            supplierCommand(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            JRoutine.on(supplierCommand(new Supplier<Object>() {

                public Object get() {

                    return "test";
                }
            })).buildRoutine();

            fail();

        } catch (final IllegalArgumentException ignored) {

        }
    }

    @Test
    public void testCommandEquals() {

        final InvocationFactory<Void, String> factory = createCommand();
        final ConsumerWrapper<ResultChannel<String>> sink = sink();
        assertThat(factory).isEqualTo(factory);
        assertThat(factory).isEqualTo(createCommand());
        assertThat(factory).isNotEqualTo(consumerCommand(sink));
        assertThat(factory).isNotEqualTo(createFactory());
        assertThat(factory).isNotEqualTo("");
        assertThat(factory.hashCode()).isEqualTo(createCommand().hashCode());
        assertThat(consumerCommand(sink)).isEqualTo(consumerCommand(sink));
        assertThat(consumerCommand(sink).hashCode()).isEqualTo(consumerCommand(sink).hashCode());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testCommandError() {

        try {

            consumerCommand(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            JRoutine.on(consumerCommand(new Consumer<ResultChannel<String>>() {

                public void accept(final ResultChannel<String> result) {

                    result.pass("test");
                }
            })).buildRoutine();

            fail();

        } catch (final IllegalArgumentException ignored) {

        }
    }

    @Test
    public void testConstant() {

        final TestFunction function = new TestFunction();
        final SupplierWrapper<Object> supplier = constant("test").andThen(function);
        assertThat(supplier.get()).isEqualTo("test");
        assertThat(function.isCalled()).isTrue();
        assertThat(supplier.hasStaticContext()).isTrue();
    }

    @Test
    public void testConsumer() {

        final TestConsumer consumer1 = new TestConsumer();
        final ConsumerWrapper<Object> consumer2 = wrapConsumer(consumer1);
        assertThat(wrapConsumer(consumer2)).isSameAs(consumer2);
        consumer2.accept("test");
        assertThat(consumer1.isCalled()).isTrue();
        consumer1.reset();
        final TestConsumer consumer3 = new TestConsumer();
        final ConsumerWrapper<Object> consumer4 = consumer2.andThen(consumer3);
        consumer4.accept("test");
        assertThat(consumer1.isCalled()).isTrue();
        assertThat(consumer3.isCalled()).isTrue();
    }

    @Test
    public void testConsumerContext() {

        final ConsumerWrapper<Object> consumer1 =
                wrapConsumer(new TestConsumer()).andThen(new TestConsumer());
        assertThat(consumer1.hasStaticContext()).isTrue();
        assertThat(consumer1.andThen(new Consumer<Object>() {

            public void accept(final Object o) {

            }
        }).hasStaticContext()).isFalse();
        assertThat(consumer1.andThen(consumer1).hasStaticContext()).isTrue();
    }

    @Test
    public void testConsumerEquals() {

        final TestConsumer consumer1 = new TestConsumer();
        assertThat(wrapConsumer(consumer1)).isEqualTo(wrapConsumer(consumer1));
        final ConsumerWrapper<Object> consumer2 = wrapConsumer(consumer1);
        assertThat(consumer2).isEqualTo(consumer2);
        assertThat(consumer2).isNotEqualTo(null);
        assertThat(consumer2).isNotEqualTo("test");
        assertThat(wrapConsumer(consumer1).andThen(consumer2).hashCode()).isEqualTo(
                consumer2.andThen(consumer2).hashCode());
        assertThat(wrapConsumer(consumer1).andThen(consumer2)).isEqualTo(
                consumer2.andThen(consumer2));
        assertThat(consumer2.andThen(consumer2)).isEqualTo(
                wrapConsumer(consumer1).andThen(consumer2));
        assertThat(wrapConsumer(consumer1).andThen(consumer2).hashCode()).isEqualTo(
                consumer2.andThen(consumer1).hashCode());
        assertThat(wrapConsumer(consumer1).andThen(consumer2)).isEqualTo(
                consumer2.andThen(consumer1));
        assertThat(consumer2.andThen(consumer1)).isEqualTo(
                wrapConsumer(consumer1).andThen(consumer2));
        assertThat(wrapConsumer(consumer1).andThen(consumer2).hashCode()).isNotEqualTo(
                consumer2.andThen(consumer2.andThen(consumer1)).hashCode());
        assertThat(wrapConsumer(consumer1).andThen(consumer2)).isNotEqualTo(
                consumer2.andThen(consumer2.andThen(consumer1)));
        assertThat(consumer2.andThen(consumer2.andThen(consumer1))).isNotEqualTo(
                wrapConsumer(consumer1).andThen(consumer2));
        assertThat(wrapConsumer(consumer1).andThen(consumer1).hashCode()).isNotEqualTo(
                consumer2.andThen(consumer2.andThen(consumer1)).hashCode());
        assertThat(wrapConsumer(consumer1).andThen(consumer1)).isNotEqualTo(
                consumer2.andThen(consumer2.andThen(consumer1)));
        assertThat(consumer2.andThen(consumer2.andThen(consumer1))).isNotEqualTo(
                wrapConsumer(consumer1).andThen(consumer1));
        assertThat(consumer2.andThen(consumer1).hashCode()).isNotEqualTo(
                consumer2.andThen(sink()).hashCode());
        assertThat(consumer2.andThen(consumer1)).isNotEqualTo(consumer2.andThen(sink()));
        assertThat(consumer2.andThen(sink())).isNotEqualTo(consumer2.andThen(consumer1));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testConsumerError() {

        try {

            new ConsumerWrapper<Object>(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            new ConsumerWrapper<Object>(Collections.<Consumer<?>>emptyList());

            fail();

        } catch (final IllegalArgumentException ignored) {

        }

        try {

            wrapConsumer(new TestConsumer()).andThen(null);

            fail();

        } catch (final NullPointerException ignored) {

        }
    }

    @Test
    public void testFactory() {

        final Routine<Object, String> routine = JRoutine.on(createFactory()).buildRoutine();
        assertThat(routine.asyncCall("test", 1).afterMax(seconds(1)).all()).containsOnly("test",
                                                                                         "1");
    }

    @Test
    public void testFactoryEquals() {

        final Supplier<Invocation<Object, Object>> supplier =
                constant(PassingInvocation.factoryOf().newInvocation());
        final InvocationFactory<Object, String> factory = createFactory();
        assertThat(factory).isEqualTo(factory);
        assertThat(factory).isEqualTo(createFactory());
        assertThat(factory).isNotEqualTo(supplierFactory(supplier));
        assertThat(factory).isNotEqualTo(createFilter());
        assertThat(factory).isNotEqualTo("");
        assertThat(factory.hashCode()).isEqualTo(createFactory().hashCode());
        assertThat(supplierFactory(supplier)).isEqualTo(supplierFactory(supplier));
        assertThat(supplierFactory(supplier).hashCode()).isEqualTo(
                supplierFactory(supplier).hashCode());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testFactoryError() {

        try {

            supplierFactory(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            JRoutine.on(supplierFactory(new Supplier<Invocation<Object, String>>() {

                public Invocation<Object, String> get() {

                    return new FilterInvocation<Object, String>() {

                        public void onInput(final Object input,
                                @NotNull final ResultChannel<String> result) {

                            result.pass(input.toString());
                        }
                    };
                }
            })).buildRoutine();

            fail();

        } catch (final IllegalArgumentException ignored) {

        }
    }

    @Test
    public void testFilter() {

        final Routine<Object, String> routine = JRoutine.on(createFilter()).buildRoutine();
        assertThat(routine.asyncCall("test", 1).afterMax(seconds(1)).all()).containsOnly("test",
                                                                                         "1");
    }

    @Test
    public void testFilter2() {

        final Routine<Object, String> routine = JRoutine.on(createFilter2()).buildRoutine();
        assertThat(routine.asyncCall("test", 1).afterMax(seconds(1)).all()).containsOnly("test",
                                                                                         "1");
    }

    @Test
    public void testFilter2Equals() {

        final FunctionWrapper<Object, ? super Object> identity = identity();
        final InvocationFactory<Object, String> factory = createFilter2();
        assertThat(factory).isEqualTo(factory);
        assertThat(factory).isEqualTo(createFilter2());
        assertThat(factory).isNotEqualTo(functionFilter(identity));
        assertThat(factory).isNotEqualTo(createFactory());
        assertThat(factory).isNotEqualTo("");
        assertThat(factory.hashCode()).isEqualTo(createFilter2().hashCode());
        assertThat(functionFilter(identity)).isEqualTo(functionFilter(identity));
        assertThat(functionFilter(identity).hashCode()).isEqualTo(
                functionFilter(identity).hashCode());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testFilter2Error() {

        try {

            functionFilter((Function<Object, ResultChannel<Object>>) null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            JRoutine.on(functionFilter(new Function<Object, Object>() {

                public Object apply(final Object o) {

                    return o.toString();
                }
            })).buildRoutine();

            fail();

        } catch (final IllegalArgumentException ignored) {

        }
    }

    @Test
    public void testFilter3() {

        final Routine<String, String> routine = JRoutine.on(createFilter3()).buildRoutine();
        assertThat(routine.asyncCall("test", "").afterMax(seconds(1)).all()).containsOnly("test");
    }

    @Test
    public void testFilter3Equals() {

        final PredicateWrapper<Object> negative = negative();
        final InvocationFactory<String, String> factory = createFilter3();
        assertThat(factory).isEqualTo(factory);
        assertThat(factory).isEqualTo(createFilter3());
        assertThat(factory).isNotEqualTo(predicateFilter(negative));
        assertThat(factory).isNotEqualTo(createFactory());
        assertThat(factory).isNotEqualTo("");
        assertThat(factory.hashCode()).isEqualTo(createFilter3().hashCode());
        assertThat(predicateFilter(negative)).isEqualTo(predicateFilter(negative));
        assertThat(predicateFilter(negative).hashCode()).isEqualTo(
                predicateFilter(negative).hashCode());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testFilter3Error() {

        try {

            predicateFilter(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            JRoutine.on(predicateFilter(new Predicate<Object>() {

                public boolean test(final Object o) {

                    return o != null;
                }
            })).buildRoutine();

            fail();

        } catch (final IllegalArgumentException ignored) {

        }
    }

    @Test
    public void testFilterEquals() {

        final InvocationFactory<Object, String> factory = createFilter();
        final BiConsumerWrapper<Object, ResultChannel<String>> sink = biSink();
        assertThat(factory).isEqualTo(factory);
        assertThat(factory).isEqualTo(createFilter());
        assertThat(factory).isNotEqualTo(consumerFilter(sink));
        assertThat(factory).isNotEqualTo(createFactory());
        assertThat(factory).isNotEqualTo("");
        assertThat(factory.hashCode()).isEqualTo(createFilter().hashCode());
        assertThat(consumerFilter(sink)).isEqualTo(consumerFilter(sink));
        assertThat(consumerFilter(sink).hashCode()).isEqualTo(consumerFilter(sink).hashCode());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testFilterError() {

        try {

            consumerFilter(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            JRoutine.on(consumerFilter(new BiConsumer<Object, ResultChannel<String>>() {

                public void accept(final Object o, final ResultChannel<String> result) {

                    result.pass(o.toString());
                }
            })).buildRoutine();

            fail();

        } catch (final IllegalArgumentException ignored) {

        }
    }

    @Test
    public void testFunction() {

        final TestFunction function1 = new TestFunction();
        final FunctionWrapper<Object, Object> function2 = wrapFunction(function1);
        assertThat(wrapFunction(function2)).isSameAs(function2);
        assertThat(function2.apply("test")).isEqualTo("test");
        assertThat(function1.isCalled()).isTrue();
        function1.reset();
        final TestFunction function3 = new TestFunction();
        final FunctionWrapper<Object, Object> function4 = function2.andThen(function3);
        assertThat(function4.apply("test")).isEqualTo("test");
        assertThat(function1.isCalled()).isTrue();
        assertThat(function3.isCalled()).isTrue();
        final FunctionWrapper<String, Integer> function5 =
                wrapFunction(new Function<String, Integer>() {

                    public Integer apply(final String s) {

                        return s.length();
                    }
                }).andThen(new Function<Integer, Integer>() {

                    public Integer apply(final Integer integer) {

                        return integer * 3;
                    }
                });
        assertThat(function5.apply("test")).isEqualTo(12);
        assertThat(function5.compose(new Function<String, String>() {

            public String apply(final String s) {

                return s + s;
            }
        }).apply("test")).isEqualTo(24);
    }

    @Test
    public void testFunctionContext() {

        final FunctionWrapper<Object, Object> function1 =
                wrapFunction(new TestFunction()).andThen(new TestFunction());
        assertThat(function1.hasStaticContext()).isTrue();
        assertThat(function1.andThen(new Function<Object, Object>() {

            public Object apply(final Object o) {

                return null;
            }
        }).hasStaticContext()).isFalse();
        assertThat(function1.andThen(function1).hasStaticContext()).isTrue();
        assertThat(function1.compose(new Function<Object, Object>() {

            public Object apply(final Object o) {

                return null;
            }
        }).hasStaticContext()).isFalse();
        assertThat(function1.compose(function1).hasStaticContext()).isTrue();
    }

    @Test
    public void testFunctionEquals() {

        final TestFunction function1 = new TestFunction();
        assertThat(wrapFunction(function1)).isEqualTo(wrapFunction(function1));
        final FunctionWrapper<Object, Object> function2 = wrapFunction(function1);
        assertThat(function2).isEqualTo(function2);
        assertThat(function2).isNotEqualTo(null);
        assertThat(function2).isNotEqualTo("test");
        assertThat(wrapFunction(function1).andThen(function2).hashCode()).isEqualTo(
                function2.andThen(function2).hashCode());
        assertThat(wrapFunction(function1).andThen(function2)).isEqualTo(
                function2.andThen(function2));
        assertThat(function2.andThen(function2)).isEqualTo(
                wrapFunction(function1).andThen(function2));
        assertThat(wrapFunction(function1).andThen(function2).hashCode()).isEqualTo(
                function2.andThen(function1).hashCode());
        assertThat(wrapFunction(function1).andThen(function2)).isEqualTo(
                function2.andThen(function1));
        assertThat(function2.andThen(function1)).isEqualTo(
                wrapFunction(function1).andThen(function2));
        assertThat(wrapFunction(function1).andThen(function2).hashCode()).isNotEqualTo(
                function2.andThen(function2.andThen(function1)).hashCode());
        assertThat(wrapFunction(function1).andThen(function2)).isNotEqualTo(
                function2.andThen(function2.andThen(function1)));
        assertThat(function2.andThen(function2.andThen(function1))).isNotEqualTo(
                wrapFunction(function1).andThen(function2));
        assertThat(wrapFunction(function1).andThen(function1).hashCode()).isNotEqualTo(
                function2.andThen(function2.andThen(function1)).hashCode());
        assertThat(wrapFunction(function1).andThen(function1)).isNotEqualTo(
                function2.andThen(function2.andThen(function1)));
        assertThat(function2.andThen(function2.andThen(function1))).isNotEqualTo(
                wrapFunction(function1).andThen(function1));
        assertThat(function2.andThen(function1).hashCode()).isNotEqualTo(
                function2.andThen(identity()).hashCode());
        assertThat(function2.andThen(function1)).isNotEqualTo(function2.andThen(identity()));
        assertThat(function2.andThen(identity())).isNotEqualTo(function2.andThen(function1));
        assertThat(wrapFunction(function1).compose(function2).hashCode()).isEqualTo(
                function2.compose(function2).hashCode());
        assertThat(wrapFunction(function1).compose(function2)).isEqualTo(
                function2.compose(function2));
        assertThat(function2.compose(function2)).isEqualTo(
                wrapFunction(function1).compose(function2));
        assertThat(wrapFunction(function1).compose(function2).hashCode()).isEqualTo(
                function2.compose(function1).hashCode());
        assertThat(wrapFunction(function1).compose(function2)).isEqualTo(
                function2.compose(function1));
        assertThat(function2.compose(function1)).isEqualTo(
                wrapFunction(function1).compose(function2));
        assertThat(wrapFunction(function1).compose(function2).hashCode()).isNotEqualTo(
                function2.compose(function2.compose(function1)).hashCode());
        assertThat(wrapFunction(function1).compose(function2)).isNotEqualTo(
                function2.compose(function2.compose(function1)));
        assertThat(function2.compose(function2.compose(function1))).isNotEqualTo(
                wrapFunction(function1).compose(function2));
        assertThat(wrapFunction(function1).compose(function1).hashCode()).isNotEqualTo(
                function2.compose(function2.compose(function1)).hashCode());
        assertThat(wrapFunction(function1).compose(function1)).isNotEqualTo(
                function2.compose(function2.compose(function1)));
        assertThat(function2.compose(function2.compose(function1))).isNotEqualTo(
                wrapFunction(function1).compose(function1));
        assertThat(function2.compose(function1).hashCode()).isNotEqualTo(
                function2.compose(identity()).hashCode());
        assertThat(function2.compose(function1)).isNotEqualTo(function2.compose(identity()));
        assertThat(function2.compose(identity())).isNotEqualTo(function2.compose(function1));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testFunctionError() {

        try {

            new FunctionWrapper<Object, Object>(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            new FunctionWrapper<Object, Object>(Collections.<Function<?, ?>>emptyList());

            fail();

        } catch (final IllegalArgumentException ignored) {

        }

        try {

            wrapFunction(new TestFunction()).andThen(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            identity().compose(null);

            fail();

        } catch (final NullPointerException ignored) {

        }
    }

    @Test
    public void testFunctionFactory() {

        final Routine<?, String> routine = JRoutine.on(createFunction()).buildRoutine();
        assertThat(routine.asyncCall("test", 1).afterMax(seconds(1)).all()).containsOnly("test",
                                                                                         "1");
    }

    @Test
    public void testFunctionFactory2() {

        final Routine<?, String> routine = JRoutine.on(createFunction2()).buildRoutine();
        assertThat(routine.asyncCall("test", 1).afterMax(seconds(1)).all()).containsOnly("test1");
    }

    @Test
    public void testFunctionFactory2Equals() {

        final InvocationFactory<?, String> factory = createFunction2();
        final FunctionWrapper<List<?>, ? super List<?>> identity = identity();
        assertThat(factory).isEqualTo(factory);
        assertThat(factory).isEqualTo(createFunction2());
        assertThat(factory).isNotEqualTo(functionFactory(identity));
        assertThat(factory).isNotEqualTo(createFactory());
        assertThat(factory).isNotEqualTo("");
        assertThat(factory.hashCode()).isEqualTo(createFunction2().hashCode());
        assertThat(functionFactory(identity)).isEqualTo(functionFactory(identity));
        assertThat(functionFactory(identity).hashCode()).isEqualTo(
                functionFactory(identity).hashCode());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testFunctionFactory2Error() {

        try {

            functionFactory(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            JRoutine.on(functionFactory(new Function<List<?>, String>() {

                public String apply(final List<?> objects) {

                    final StringBuilder builder = new StringBuilder();

                    for (final Object object : objects) {

                        builder.append(object.toString());
                    }

                    return builder.toString();
                }
            })).buildRoutine();

            fail();

        } catch (final IllegalArgumentException ignored) {

        }
    }

    @Test
    public void testFunctionFactoryEquals() {

        final InvocationFactory<?, String> factory = createFunction();
        final BiConsumerWrapper<List<?>, ResultChannel<Object>> sink = biSink();
        assertThat(factory).isEqualTo(factory);
        assertThat(factory).isEqualTo(createFunction());
        assertThat(factory).isNotEqualTo(consumerFactory(sink));
        assertThat(factory).isNotEqualTo(createFactory());
        assertThat(factory).isNotEqualTo("");
        assertThat(factory.hashCode()).isEqualTo(createFunction().hashCode());
        assertThat(consumerFactory(sink)).isEqualTo(consumerFactory(sink));
        assertThat(consumerFactory(sink).hashCode()).isEqualTo(consumerFactory(sink).hashCode());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testFunctionFactoryError() {

        try {

            consumerFactory(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            JRoutine.on(consumerFactory(new BiConsumer<List<?>, ResultChannel<String>>() {

                public void accept(final List<?> objects, final ResultChannel<String> result) {

                    for (final Object object : objects) {

                        result.pass(object.toString());
                    }
                }
            })).buildRoutine();

            fail();

        } catch (final IllegalArgumentException ignored) {

        }
    }

    @Test
    public void testIdentity() {

        final TestFunction function1 = new TestFunction();
        final FunctionWrapper<Object, Object> function2 = identity().andThen(function1);
        assertThat(function2.apply("test")).isEqualTo("test");
        assertThat(function1.isCalled()).isTrue();
        assertThat(function2.hasStaticContext()).isTrue();
        assertThat(identity()).isSameAs(identity());
    }

    @Test
    public void testPredicate() {

        final TestPredicate predicate1 = new TestPredicate();
        final PredicateWrapper<Object> predicate2 = wrapPredicate(predicate1);
        assertThat(wrapPredicate(predicate2)).isSameAs(predicate2);
        assertThat(predicate2.test(this)).isTrue();
        assertThat(predicate1.isCalled()).isTrue();
        predicate1.reset();
        final TestPredicate predicate3 = new TestPredicate();
        final PredicateWrapper<Object> predicate4 = predicate2.and(predicate3);
        assertThat(predicate4.test(this)).isTrue();
        assertThat(predicate1.isCalled()).isTrue();
        assertThat(predicate3.isCalled()).isTrue();
        predicate1.reset();
        predicate3.reset();
        assertThat(predicate4.test(null)).isFalse();
        assertThat(predicate1.isCalled()).isTrue();
        assertThat(predicate3.isCalled()).isFalse();
        predicate1.reset();
        predicate3.reset();
        final PredicateWrapper<Object> predicate5 = predicate2.or(predicate3);
        assertThat(predicate5.test(this)).isTrue();
        assertThat(predicate1.isCalled()).isTrue();
        assertThat(predicate3.isCalled()).isFalse();
        predicate1.reset();
        predicate3.reset();
        assertThat(predicate5.test(null)).isFalse();
        assertThat(predicate1.isCalled()).isTrue();
        assertThat(predicate3.isCalled()).isTrue();
        predicate1.reset();
        predicate3.reset();
        final PredicateWrapper<Object> predicate6 = predicate4.negate();
        assertThat(predicate6.test(this)).isFalse();
        assertThat(predicate1.isCalled()).isTrue();
        assertThat(predicate3.isCalled()).isTrue();
        predicate1.reset();
        predicate3.reset();
        assertThat(predicate6.test(null)).isTrue();
        assertThat(predicate1.isCalled()).isTrue();
        assertThat(predicate3.isCalled()).isFalse();
        predicate1.reset();
        predicate3.reset();
        final PredicateWrapper<Object> predicate7 = predicate5.negate();
        assertThat(predicate7.test(this)).isFalse();
        assertThat(predicate1.isCalled()).isTrue();
        assertThat(predicate3.isCalled()).isFalse();
        predicate1.reset();
        predicate3.reset();
        assertThat(predicate7.test(null)).isTrue();
        assertThat(predicate1.isCalled()).isTrue();
        assertThat(predicate3.isCalled()).isTrue();
        predicate1.reset();
        predicate3.reset();
        assertThat(negative().or(positive()).test(null)).isTrue();
        assertThat(negative().and(positive()).test("test")).isFalse();
        assertThat(notNull().or(isNull()).test(null)).isTrue();
        assertThat(notNull().and(isNull()).test("test")).isFalse();
    }

    @Test
    public void testPredicateContext() {

        final PredicateWrapper<Object> predicate1 =
                wrapPredicate(new TestPredicate()).and(new TestPredicate());
        assertThat(predicate1.hasStaticContext()).isTrue();
        assertThat(predicate1.or(new Predicate<Object>() {

            public boolean test(final Object o) {

                return false;
            }
        }).hasStaticContext()).isFalse();
        assertThat(predicate1.or(negative()).hasStaticContext()).isTrue();
        assertThat(predicate1.or(isNull()).hasStaticContext()).isTrue();
    }

    @Test
    public void testPredicateEquals() {

        final TestPredicate predicate1 = new TestPredicate();
        assertThat(wrapPredicate(predicate1)).isEqualTo(wrapPredicate(predicate1));
        final PredicateWrapper<Object> predicate2 = wrapPredicate(predicate1);
        assertThat(predicate2).isEqualTo(predicate2);
        assertThat(predicate2).isNotEqualTo(null);
        assertThat(predicate2).isNotEqualTo("test");
        assertThat(wrapPredicate(predicate1).and(predicate2).hashCode()).isEqualTo(
                predicate2.and(predicate2).hashCode());
        assertThat(wrapPredicate(predicate1).and(predicate2)).isEqualTo(predicate2.and(predicate2));
        assertThat(predicate2.and(predicate2)).isEqualTo(wrapPredicate(predicate1).and(predicate2));
        assertThat(wrapPredicate(predicate1).and(predicate2).hashCode()).isEqualTo(
                predicate2.and(predicate1).hashCode());
        assertThat(wrapPredicate(predicate1).and(predicate2)).isEqualTo(predicate2.and(predicate1));
        assertThat(predicate2.and(predicate1)).isEqualTo(wrapPredicate(predicate1).and(predicate2));
        assertThat(wrapPredicate(predicate1).and(predicate2).hashCode()).isNotEqualTo(
                predicate2.and(predicate2.and(predicate1)).hashCode());
        assertThat(wrapPredicate(predicate1).and(predicate2)).isNotEqualTo(
                predicate2.and(predicate2.and(predicate1)));
        assertThat(predicate2.and(predicate2.and(predicate1))).isNotEqualTo(
                wrapPredicate(predicate1).and(predicate2));
        assertThat(wrapPredicate(predicate1).and(predicate1).hashCode()).isNotEqualTo(
                predicate2.and(predicate2.and(predicate1)).hashCode());
        assertThat(wrapPredicate(predicate1).and(predicate1)).isNotEqualTo(
                predicate2.and(predicate2.and(predicate1)));
        assertThat(predicate2.and(predicate2.and(predicate1))).isNotEqualTo(
                wrapPredicate(predicate1).and(predicate1));
        assertThat(predicate2.and(predicate1).hashCode()).isNotEqualTo(
                predicate2.and(positive()).hashCode());
        assertThat(predicate2.and(predicate1)).isNotEqualTo(predicate2.and(positive()));
        assertThat(predicate2.and(positive())).isNotEqualTo(predicate2.and(predicate1));
        assertThat(wrapPredicate(predicate1).or(predicate2).hashCode()).isEqualTo(
                predicate2.or(predicate2).hashCode());
        assertThat(wrapPredicate(predicate1).or(predicate2)).isEqualTo(predicate2.or(predicate2));
        assertThat(predicate2.or(predicate2)).isEqualTo(wrapPredicate(predicate1).or(predicate2));
        assertThat(wrapPredicate(predicate1).or(predicate2).hashCode()).isEqualTo(
                predicate2.or(predicate1).hashCode());
        assertThat(wrapPredicate(predicate1).or(predicate2)).isEqualTo(predicate2.or(predicate1));
        assertThat(predicate2.or(predicate1)).isEqualTo(wrapPredicate(predicate1).or(predicate2));
        assertThat(wrapPredicate(predicate1).or(predicate2).hashCode()).isNotEqualTo(
                predicate2.or(predicate2.or(predicate1)).hashCode());
        assertThat(wrapPredicate(predicate1).or(predicate2)).isNotEqualTo(
                predicate2.or(predicate2.or(predicate1)));
        assertThat(predicate2.or(predicate2.or(predicate1))).isNotEqualTo(
                wrapPredicate(predicate1).or(predicate2));
        assertThat(wrapPredicate(predicate1).or(predicate1).hashCode()).isNotEqualTo(
                predicate2.or(predicate2.or(predicate1)).hashCode());
        assertThat(wrapPredicate(predicate1).or(predicate1)).isNotEqualTo(
                predicate2.or(predicate2.or(predicate1)));
        assertThat(predicate2.or(predicate2.or(predicate1))).isNotEqualTo(
                wrapPredicate(predicate1).or(predicate1));
        assertThat(predicate2.or(predicate1).hashCode()).isNotEqualTo(
                predicate2.or(positive()).hashCode());
        assertThat(predicate2.or(predicate1)).isNotEqualTo(predicate2.or(positive()));
        assertThat(predicate2.or(positive())).isNotEqualTo(predicate2.or(predicate1));
        assertThat(predicate2.negate().negate()).isEqualTo(wrapPredicate(predicate1));
        assertThat(predicate2.and(predicate1).negate()).isEqualTo(
                predicate2.negate().or(wrapPredicate(predicate1).negate()));
        assertThat(predicate2.and(predicate1).negate().hashCode()).isEqualTo(
                predicate2.negate().or(wrapPredicate(predicate1).negate()).hashCode());
        final PredicateWrapper<Object> chain =
                predicate2.negate().or(predicate2.negate().and(predicate2.negate()));
        assertThat(predicate2.and(predicate2.or(predicate1)).negate()).isEqualTo(chain);
        assertThat(negative().negate()).isEqualTo(positive());
        assertThat(positive().negate()).isEqualTo(negative());
        assertThat(notNull().negate()).isEqualTo(isNull());
        assertThat(isNull().negate()).isEqualTo(notNull());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testPredicateError() {

        try {

            new PredicateWrapper<Object>(null, Collections.<Predicate<?>>singletonList(
                    new TestPredicate()));

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            new PredicateWrapper<Object>(new TestPredicate(), null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            new PredicateWrapper<Object>(new TestPredicate(),
                                         Collections.<Predicate<?>>emptyList());

            fail();

        } catch (final IllegalArgumentException ignored) {

        }

        try {

            wrapPredicate(new TestPredicate()).and(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            wrapPredicate(new TestPredicate()).or(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            negative().and(null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            positive().or(null);

            fail();

        } catch (final NullPointerException ignored) {

        }
    }

    @Test
    public void testSink() {

        final TestConsumer consumer1 = new TestConsumer();
        final ConsumerWrapper<Object> consumer2 = sink().andThen(consumer1);
        consumer2.accept("test");
        assertThat(consumer1.isCalled()).isTrue();
        assertThat(consumer2.hasStaticContext()).isTrue();
        assertThat(sink()).isSameAs(sink());
    }

    @Test
    public void testSupplier() {

        final TestSupplier supplier1 = new TestSupplier();
        final SupplierWrapper<Object> supplier2 = wrapSupplier(supplier1);
        assertThat(wrapSupplier(supplier2)).isSameAs(supplier2);
        assertThat(supplier2.get()).isSameAs(supplier1);
        assertThat(supplier1.isCalled()).isTrue();
        supplier1.reset();
        final TestFunction function = new TestFunction();
        final SupplierWrapper<Object> supplier3 = supplier2.andThen(function);
        assertThat(supplier3.get()).isSameAs(supplier1);
        assertThat(supplier1.isCalled()).isTrue();
        assertThat(function.isCalled()).isTrue();
        Assertions.assertThat(constant("test").andThen(new Function<String, Integer>() {

            public Integer apply(final String s) {

                return s.length();
            }
        }).andThen(new Function<Integer, Integer>() {

            public Integer apply(final Integer integer) {

                return integer * 3;
            }
        }).get()).isEqualTo(12);
    }

    @Test
    public void testSupplierContext() {

        final SupplierWrapper<Object> supplier1 =
                wrapSupplier(new TestSupplier()).andThen(new TestFunction());
        assertThat(supplier1.hasStaticContext()).isTrue();
        assertThat(supplier1.andThen(new Function<Object, Object>() {

            public Object apply(final Object o) {

                return null;
            }
        }).hasStaticContext()).isFalse();
        assertThat(supplier1.andThen(identity()).hasStaticContext()).isTrue();
    }

    @Test
    public void testSupplierEquals() {

        final TestSupplier supplier1 = new TestSupplier();
        assertThat(wrapSupplier(supplier1)).isEqualTo(wrapSupplier(supplier1));
        final SupplierWrapper<Object> supplier2 = wrapSupplier(supplier1);
        assertThat(supplier2).isEqualTo(supplier2);
        assertThat(supplier2).isNotEqualTo(null);
        assertThat(supplier2).isNotEqualTo("test");
        final TestFunction function = new TestFunction();
        assertThat(wrapSupplier(supplier1).andThen(function).hashCode()).isEqualTo(
                supplier2.andThen(function).hashCode());
        assertThat(wrapSupplier(supplier1).andThen(function)).isEqualTo(
                supplier2.andThen(function));
        assertThat(supplier2.andThen(function)).isEqualTo(
                wrapSupplier(supplier1).andThen(function));
        assertThat(wrapSupplier(supplier1).andThen(wrapFunction(function)).hashCode()).isEqualTo(
                supplier2.andThen(function).hashCode());
        assertThat(wrapSupplier(supplier1).andThen(wrapFunction(function))).isEqualTo(
                supplier2.andThen(function));
        assertThat(supplier2.andThen(function)).isEqualTo(
                wrapSupplier(supplier1).andThen(wrapFunction(function)));
        assertThat(wrapSupplier(supplier1).andThen(wrapFunction(function)).hashCode()).isNotEqualTo(
                supplier2.andThen(wrapFunction(function).andThen(function)).hashCode());
        assertThat(wrapSupplier(supplier1).andThen(wrapFunction(function))).isNotEqualTo(
                supplier2.andThen(wrapFunction(function).andThen(function)));
        assertThat(supplier2.andThen(wrapFunction(function).andThen(function))).isNotEqualTo(
                wrapSupplier(supplier1).andThen(wrapFunction(function)));
        assertThat(wrapSupplier(supplier1).andThen(function).hashCode()).isNotEqualTo(
                supplier2.andThen(wrapFunction(function).andThen(function)).hashCode());
        assertThat(wrapSupplier(supplier1).andThen(function)).isNotEqualTo(
                supplier2.andThen(wrapFunction(function).andThen(function)));
        assertThat(supplier2.andThen(wrapFunction(function).andThen(function))).isNotEqualTo(
                wrapSupplier(supplier1).andThen(function));
        assertThat(supplier2.andThen(function).hashCode()).isNotEqualTo(
                supplier2.andThen(identity()).hashCode());
        assertThat(supplier2.andThen(function)).isNotEqualTo(supplier2.andThen(identity()));
        assertThat(supplier2.andThen(identity())).isNotEqualTo(supplier2.andThen(function));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testSupplierError() {

        try {

            new SupplierWrapper<Object>(null, wrapFunction(new TestFunction()));

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            new SupplierWrapper<Object>(new TestSupplier(), null);

            fail();

        } catch (final NullPointerException ignored) {

        }

        try {

            wrapSupplier(new TestSupplier()).andThen(null);

            fail();

        } catch (final NullPointerException ignored) {

        }
    }

    private static class TestBiConsumer implements BiConsumer<Object, Object> {

        private boolean mIsCalled;

        public boolean isCalled() {

            return mIsCalled;
        }

        public void reset() {

            mIsCalled = false;
        }

        public void accept(final Object in1, final Object in2) {

            mIsCalled = true;
        }
    }

    private static class TestBiFunction implements BiFunction<Object, Object, Object> {

        private boolean mIsCalled;

        public Object apply(final Object in1, final Object in2) {

            mIsCalled = true;
            return in2;
        }

        public boolean isCalled() {

            return mIsCalled;
        }

        public void reset() {

            mIsCalled = false;
        }
    }

    private static class TestConsumer implements Consumer<Object> {

        private boolean mIsCalled;

        public boolean isCalled() {

            return mIsCalled;
        }

        public void reset() {

            mIsCalled = false;
        }

        public void accept(final Object out) {

            mIsCalled = true;
        }
    }

    private static class TestFunction implements Function<Object, Object> {

        private boolean mIsCalled;

        public boolean isCalled() {

            return mIsCalled;
        }

        public void reset() {

            mIsCalled = false;
        }

        public Object apply(final Object in) {

            mIsCalled = true;
            return in;
        }
    }

    private static class TestPredicate implements Predicate<Object> {

        private boolean mIsCalled;

        public boolean isCalled() {

            return mIsCalled;
        }

        public void reset() {

            mIsCalled = false;
        }

        public boolean test(final Object in) {

            mIsCalled = true;
            return in != null;
        }
    }

    private static class TestSupplier implements Supplier<Object> {

        private boolean mIsCalled;

        public boolean isCalled() {

            return mIsCalled;
        }

        public void reset() {

            mIsCalled = false;
        }

        public Object get() {

            mIsCalled = true;
            return this;
        }
    }
}