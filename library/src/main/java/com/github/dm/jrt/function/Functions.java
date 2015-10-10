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

import com.github.dm.jrt.util.Reflection;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Utility class supporting functional programming.
 * <p/>
 * Created by davide-maestroni on 09/21/2015.
 */
public class Functions {

    private static final BiConsumerChain<?, ?> sBiSink =
            biConsumerChain(new BiConsumer<Object, Object>() {

                public void accept(final Object in1, final Object in2) {

                }
            });

    private static final FunctionChain<?, ?> sIdentity =
            functionChain(new Function<Object, Object>() {

                public Object apply(final Object in) {

                    return in;
                }
            });

    private static final ConsumerChain<?> sSink = consumerChain(new Consumer<Object>() {

        public void accept(final Object in) {

        }
    });

    /**
     * Avoid direct instantiation.
     */
    protected Functions() {

    }

    /**
     * Wraps the specified bi-consumer instance so to provide additional features.<br/>
     * The returned object will support concatenation and comparison.
     * <p/>
     * Note that the passed object is expected to behave like a function, that is, it must not
     * retain a mutable internal state.<br/>
     * Note also that any external object used inside the function must be synchronized in order to
     * avoid concurrency issues.
     *
     * @param consumer the bi-consumer instance.
     * @param <IN1>    the first input data type.
     * @param <IN2>    the second input data type.
     * @return the wrapped bi-consumer.
     */
    @NotNull
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST",
            justification = "class comparison with == is done")
    public static <IN1, IN2> BiConsumerChain<IN1, IN2> biConsumerChain(
            @NotNull final BiConsumer<IN1, IN2> consumer) {

        if (consumer.getClass() == BiConsumerChain.class) {

            return (BiConsumerChain<IN1, IN2>) consumer;
        }

        return new BiConsumerChain<IN1, IN2>(Collections.<BiConsumer<?, ?>>singletonList(consumer));
    }

    /**
     * Returns a bi-consumer chain just discarding the passed inputs.<br/>
     * The returned object will support concatenation and comparison.
     *
     * @param <IN1> the first input data type.
     * @param <IN2> the second input data type.
     * @return the wrapped bi-consumer.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static <IN1, IN2> BiConsumerChain<IN1, IN2> biSink() {

        return (BiConsumerChain<IN1, IN2>) sBiSink;
    }

    /**
     * Returns a supplier chain always returning the same result.<br/>
     * The returned object will support concatenation and comparison.
     *
     * @param result the result.
     * @param <OUT>  the output data type.
     * @return the wrapped supplier.
     */
    @NotNull
    public static <OUT> SupplierChain<OUT> constant(final OUT result) {

        return supplierChain(new Supplier<OUT>() {

            public OUT get() {

                return result;
            }
        });
    }

    /**
     * Wraps the specified consumer instance so to provide additional features.<br/>
     * The returned object will support concatenation and comparison.
     * <p/>
     * Note that the passed object is expected to behave like a function, that is, it must not
     * retain a mutable internal state.<br/>
     * Note also that any external object used inside the function must be synchronized in order to
     * avoid concurrency issues.
     *
     * @param consumer the consumer instance.
     * @param <IN>     the input data type.
     * @return the wrapped consumer.
     */
    @NotNull
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST",
            justification = "class comparison with == is done")
    public static <IN> ConsumerChain<IN> consumerChain(@NotNull final Consumer<IN> consumer) {

        if (consumer.getClass() == ConsumerChain.class) {

            return (ConsumerChain<IN>) consumer;
        }

        return new ConsumerChain<IN>(Collections.<Consumer<?>>singletonList(consumer));
    }

    /**
     * Wraps the specified function instance so to provide additional features.<br/>
     * The returned object will support concatenation and comparison.
     * <p/>
     * Note that the passed object is expected to behave like a function, that is, it must not
     * retain a mutable internal state.<br/>
     * Note also that any external object used inside the function must be synchronized in order to
     * avoid concurrency issues.
     *
     * @param function the function instance.
     * @param <IN>     the input data type.
     * @param <OUT>    the output data type.
     * @return the wrapped function.
     */
    @NotNull
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST",
            justification = "class comparison with == is done")
    public static <IN, OUT> FunctionChain<IN, OUT> functionChain(
            @NotNull final Function<IN, OUT> function) {

        if (function.getClass() == FunctionChain.class) {

            return (FunctionChain<IN, OUT>) function;
        }

        return new FunctionChain<IN, OUT>(Collections.<Function<?, ?>>singletonList(function));
    }

    /**
     * Returns the identity function.<br/>
     * The returned object will support concatenation and comparison.
     *
     * @param <IN> the input data type.
     * @return the wrapped function.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static <IN> FunctionChain<IN, IN> identity() {

        return (FunctionChain<IN, IN>) sIdentity;
    }

    /**
     * Returns a consumer chain just discarding the passed inputs.<br/>
     * The returned object will support concatenation and comparison.
     *
     * @param <IN> the input data type.
     * @return the wrapped consumer.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static <IN> ConsumerChain<IN> sink() {

        return (ConsumerChain<IN>) sSink;
    }

    /**
     * Wraps the specified supplier instance so to provide additional features.<br/>
     * The returned object will support concatenation and comparison.
     * <p/>
     * Note that the passed object is expected to behave like a function, that is, it must not
     * retain a mutable internal state.<br/>
     * Note also that any external object used inside the function must be synchronized in order to
     * avoid concurrency issues.
     *
     * @param supplier the supplier instance.
     * @param <OUT>    the output data type.
     * @return the wrapped supplier.
     */
    @NotNull
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST",
            justification = "class comparison with == is done")
    public static <OUT> SupplierChain<OUT> supplierChain(@NotNull final Supplier<OUT> supplier) {

        if (supplier.getClass() == SupplierChain.class) {

            return (SupplierChain<OUT>) supplier;
        }

        return new SupplierChain<OUT>(supplier, Collections.<Function<?, ?>>emptyList());
    }

    /**
     * Class wrapping a bi-consumer instance.
     *
     * @param <IN1> the first input data type.
     * @param <IN2> the second input data type.
     */
    public static class BiConsumerChain<IN1, IN2> implements BiConsumer<IN1, IN2> {

        private final List<BiConsumer<?, ?>> mConsumers;

        /**
         * Constructor.
         *
         * @param consumers the list of wrapped consumers.
         */
        private BiConsumerChain(@NotNull final List<BiConsumer<?, ?>> consumers) {

            mConsumers = consumers;
        }

        /**
         * Returns a composed bi-consumer chain that performs, in sequence, this operation followed
         * by the after operation.
         *
         * @param after the operation to perform after this operation.
         * @return the composed bi-consumer.
         */
        @NotNull
        @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST",
                justification = "class comparison with == is done")
        public BiConsumerChain<IN1, IN2> andThen(
                @NotNull final BiConsumer<? super IN1, ? super IN2> after) {

            final Class<? extends BiConsumer> consumerClass = after.getClass();
            final List<BiConsumer<?, ?>> consumers = mConsumers;
            final ArrayList<BiConsumer<?, ?>> newConsumers =
                    new ArrayList<BiConsumer<?, ?>>(consumers.size() + 1);
            newConsumers.addAll(consumers);

            if (consumerClass == BiConsumerChain.class) {

                newConsumers.addAll(((BiConsumerChain<?, ?>) after).mConsumers);

            } else {

                newConsumers.add(after);
            }

            return new BiConsumerChain<IN1, IN2>(newConsumers);
        }

        /**
         * Checks if this bi-consumer chain has a static context.
         *
         * @return whether this instance has a static context.
         */
        public boolean hasStaticContext() {

            for (final BiConsumer<?, ?> consumer : mConsumers) {

                if (!Reflection.hasStaticContext(consumer.getClass())) {

                    return false;
                }
            }

            return true;
        }

        @Override
        public int hashCode() {

            int result = 0;

            for (final BiConsumer<?, ?> consumer : mConsumers) {

                result = 31 * result + consumer.getClass().hashCode();
            }

            return result;
        }

        @Override
        public boolean equals(final Object o) {

            if (this == o) {

                return true;
            }

            if ((o == null) || (getClass() != o.getClass())) {

                return false;
            }

            final BiConsumerChain<?, ?> that = (BiConsumerChain<?, ?>) o;
            final List<BiConsumer<?, ?>> thisConsumers = mConsumers;
            final List<BiConsumer<?, ?>> thatConsumers = that.mConsumers;
            final int size = thisConsumers.size();

            if (size != thatConsumers.size()) {

                return false;
            }

            for (int i = 0; i < size; ++i) {

                if (thisConsumers.get(i).getClass() != thatConsumers.get(i).getClass()) {

                    return false;
                }
            }

            return true;
        }

        /**
         * Performs this operation on the given arguments.
         *
         * @param in1 the first input argument.
         * @param in2 the second input argument.
         */
        @SuppressWarnings("unchecked")
        public void accept(final IN1 in1, final IN2 in2) {

            for (final BiConsumer<?, ?> consumer : mConsumers) {

                ((BiConsumer<Object, Object>) consumer).accept(in1, in2);
            }
        }
    }

    /**
     * Class wrapping a consumer instance.
     *
     * @param <IN> the input data type.
     */
    public static class ConsumerChain<IN> implements Consumer<IN> {

        private final List<Consumer<?>> mConsumers;

        /**
         * Constructor.
         *
         * @param consumers the list of wrapped consumers.
         */
        private ConsumerChain(@NotNull final List<Consumer<?>> consumers) {

            mConsumers = consumers;
        }

        /**
         * Returns a composed consumer chain that performs, in sequence, this operation followed by
         * the after operation.
         *
         * @param after the operation to perform after this operation.
         * @return the composed consumer.
         */
        @NotNull
        @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST",
                justification = "class comparison with == is done")
        public ConsumerChain<IN> andThen(@NotNull final Consumer<? super IN> after) {

            final Class<? extends Consumer> consumerClass = after.getClass();
            final List<Consumer<?>> consumers = mConsumers;
            final ArrayList<Consumer<?>> newConsumers =
                    new ArrayList<Consumer<?>>(consumers.size() + 1);
            newConsumers.addAll(consumers);

            if (consumerClass == ConsumerChain.class) {

                newConsumers.addAll(((ConsumerChain<?>) after).mConsumers);

            } else {

                newConsumers.add(after);
            }

            return new ConsumerChain<IN>(newConsumers);
        }

        /**
         * Checks if this consumer chain has a static context.
         *
         * @return whether this instance has a static context.
         */
        public boolean hasStaticContext() {

            for (final Consumer<?> consumer : mConsumers) {

                if (!Reflection.hasStaticContext(consumer.getClass())) {

                    return false;
                }
            }

            return true;
        }

        @Override
        public int hashCode() {

            int result = 0;

            for (final Consumer<?> consumer : mConsumers) {

                result = 31 * result + consumer.getClass().hashCode();
            }

            return result;
        }

        @Override
        public boolean equals(final Object o) {

            if (this == o) {

                return true;
            }

            if ((o == null) || (getClass() != o.getClass())) {

                return false;
            }

            final ConsumerChain<?> that = (ConsumerChain<?>) o;
            final List<Consumer<?>> thisConsumers = mConsumers;
            final List<Consumer<?>> thatConsumers = that.mConsumers;
            final int size = thisConsumers.size();

            if (size != thatConsumers.size()) {

                return false;
            }

            for (int i = 0; i < size; ++i) {

                if (thisConsumers.get(i).getClass() != thatConsumers.get(i).getClass()) {

                    return false;
                }
            }

            return true;
        }

        /**
         * Performs this operation on the given argument.
         *
         * @param in the input argument.
         */
        @SuppressWarnings("unchecked")
        public void accept(final IN in) {

            for (final Consumer<?> consumer : mConsumers) {

                ((Consumer<Object>) consumer).accept(in);
            }
        }
    }

    /**
     * Class wrapping a function instance.
     *
     * @param <IN>  the input data type.
     * @param <OUT> the output data type.
     */
    public static class FunctionChain<IN, OUT> implements Function<IN, OUT> {

        private final List<Function<?, ?>> mFunctions;

        /**
         * Constructor.
         *
         * @param functions the list of wrapped functions.
         */
        private FunctionChain(@NotNull final List<Function<?, ?>> functions) {

            mFunctions = functions;
        }

        /**
         * Returns a composed function chain that first applies this function to its input, and then
         * applies the after function to the result.
         *
         * @param after   the function to apply after this function is applied.
         * @param <AFTER> the type of output of the after function.
         * @return the composed function.
         */
        @NotNull
        @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST",
                justification = "class comparison with == is done")
        public <AFTER> FunctionChain<IN, AFTER> andThen(
                @NotNull final Function<? super OUT, AFTER> after) {

            final Class<? extends Function> functionClass = after.getClass();
            final List<Function<?, ?>> functions = mFunctions;
            final ArrayList<Function<?, ?>> newFunctions =
                    new ArrayList<Function<?, ?>>(functions.size() + 1);
            newFunctions.addAll(functions);

            if (functionClass == FunctionChain.class) {

                newFunctions.addAll(((FunctionChain<?, ?>) after).mFunctions);

            } else {

                newFunctions.add(after);
            }

            return new FunctionChain<IN, AFTER>(newFunctions);
        }

        /**
         * Returns a composed function chain that first applies the before function to its input,
         * and then applies this function to the result.
         *
         * @param before   the function to apply before this function is applied.
         * @param <BEFORE> the type of input to the before function.
         * @return the composed function.
         */
        @NotNull
        @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST",
                justification = "class comparison with == is done")
        public <BEFORE> FunctionChain<BEFORE, OUT> compose(
                @NotNull final Function<BEFORE, ? extends IN> before) {

            final Class<? extends Function> functionClass = before.getClass();
            final List<Function<?, ?>> functions = mFunctions;
            final ArrayList<Function<?, ?>> newFunctions =
                    new ArrayList<Function<?, ?>>(functions.size() + 1);

            if (functionClass == FunctionChain.class) {

                newFunctions.addAll(((FunctionChain<?, ?>) before).mFunctions);

            } else {

                newFunctions.add(before);
            }

            newFunctions.addAll(functions);
            return new FunctionChain<BEFORE, OUT>(newFunctions);
        }

        /**
         * Checks if this function chain has a static context.
         *
         * @return whether this instance has a static context.
         */
        public boolean hasStaticContext() {

            for (final Function<?, ?> function : mFunctions) {

                if (!Reflection.hasStaticContext(function.getClass())) {

                    return false;
                }
            }

            return true;
        }

        @Override
        public int hashCode() {

            int result = 0;

            for (final Function<?, ?> function : mFunctions) {

                result = 31 * result + function.getClass().hashCode();
            }

            return result;
        }

        @Override
        @SuppressFBWarnings(value = "EQ_GETCLASS_AND_CLASS_CONSTANT",
                justification = "comparing class of the internal list objects")
        public boolean equals(final Object o) {

            if (this == o) {

                return true;
            }

            if ((o == null) || (getClass() != o.getClass())) {

                return false;
            }

            final FunctionChain<?, ?> that = (FunctionChain<?, ?>) o;
            final List<Function<?, ?>> thisFunctions = mFunctions;
            final List<Function<?, ?>> thatFunctions = that.mFunctions;
            final int size = thisFunctions.size();

            if (size != thatFunctions.size()) {

                return false;
            }

            for (int i = 0; i < size; ++i) {

                if (thisFunctions.get(i).getClass() != thatFunctions.get(i).getClass()) {

                    return false;
                }
            }

            return true;
        }

        /**
         * Applies this function to the given argument.
         *
         * @param in the input argument.
         * @return the function result.
         */
        @SuppressWarnings("unchecked")
        public OUT apply(final IN in) {

            Object result = in;

            for (final Function<?, ?> function : mFunctions) {

                result = ((Function<Object, Object>) function).apply(result);
            }

            return (OUT) result;
        }
    }

    /**
     * Class wrapping a supplier instance.
     *
     * @param <OUT> the output data type.
     */
    public static class SupplierChain<OUT> implements Supplier<OUT> {

        private final List<Function<?, ?>> mFunctions;

        private final Supplier<?> mSupplier;

        /**
         * Constructor.
         *
         * @param supplier  the initial wrapped supplier.
         * @param functions the list of wrapped functions.
         */
        private SupplierChain(@NotNull final Supplier<?> supplier,
                @NotNull final List<Function<?, ?>> functions) {

            mSupplier = supplier;
            mFunctions = functions;
        }

        /**
         * Returns a composed supplier chain that first gets this supplier result, and then applies
         * the after function to it.
         *
         * @param after   the function to apply after this function is applied.
         * @param <AFTER> the type of output of the after function.
         * @return the composed function.
         */
        @NotNull
        @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST",
                justification = "class comparison with == is done")
        public <AFTER> SupplierChain<AFTER> andThen(
                @NotNull final Function<? super OUT, AFTER> after) {

            final Class<? extends Function> functionClass = after.getClass();
            final List<Function<?, ?>> functions = mFunctions;
            final ArrayList<Function<?, ?>> newFunctions =
                    new ArrayList<Function<?, ?>>(functions.size() + 1);
            newFunctions.addAll(functions);

            if (functionClass == FunctionChain.class) {

                newFunctions.addAll(((FunctionChain<?, ?>) after).mFunctions);

            } else {

                newFunctions.add(after);
            }

            return new SupplierChain<AFTER>(mSupplier, newFunctions);
        }

        /**
         * Checks if this supplier chain has a static context.
         *
         * @return whether this instance has a static context.
         */
        public boolean hasStaticContext() {

            final Supplier<?> supplier = mSupplier;

            if (!Reflection.hasStaticContext(supplier.getClass())) {

                return false;
            }

            for (final Function<?, ?> function : mFunctions) {

                if (!Reflection.hasStaticContext(function.getClass())) {

                    return false;
                }
            }

            return true;
        }

        @Override
        public int hashCode() {

            int result = mSupplier.getClass().hashCode();

            for (final Function<?, ?> function : mFunctions) {

                result = 31 * result + function.getClass().hashCode();
            }

            return result;
        }

        @Override
        public boolean equals(final Object o) {

            if (this == o) {

                return true;
            }

            if ((o == null) || (getClass() != o.getClass())) {

                return false;
            }

            final SupplierChain<?> that = (SupplierChain<?>) o;

            if (mSupplier.getClass() != that.mSupplier.getClass()) {

                return false;
            }

            final List<Function<?, ?>> thisFunctions = mFunctions;
            final List<Function<?, ?>> thatFunctions = that.mFunctions;
            final int size = thisFunctions.size();

            if (size != thatFunctions.size()) {

                return false;
            }

            for (int i = 0; i < size; ++i) {

                if (thisFunctions.get(i).getClass() != thatFunctions.get(i).getClass()) {

                    return false;
                }
            }

            return true;
        }

        /**
         * Gets a result.
         *
         * @return a result.
         */
        @SuppressWarnings("unchecked")
        public OUT get() {

            Object result = mSupplier.get();

            for (final Function<?, ?> function : mFunctions) {

                result = ((Function<Object, Object>) function).apply(result);
            }

            return (OUT) result;
        }
    }
}
