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
package com.bmd.jrt.routine;

import com.bmd.jrt.common.ClassToken;
import com.bmd.jrt.invocation.Invocation;

import javax.annotation.Nonnull;

/**
 * This utility class represents the entry point to the framework functionalities by acting as a
 * factory of routine builders.
 * <p/>
 * There are mainly two ways to create a routine object:
 * <p/>
 * <b>Routine by invocation customization</b><br/>
 * The first approach consists in implementing an invocation object and build on its class token
 * a routine instance.
 * <p/>
 * <b>Routine by method invocation</b><br/>
 * The second approach is based on the asynchronous invocation of a method of an existing class or
 * object via reflection.<br/>
 * It is possible to annotate selected methods to be asynchronously invoked, or to simply select
 * a method through its signature. It is also possible to build a proxy object whose methods will
 * in turn asynchronously invoke the target object ones.<br/>
 * Note that a proxy object can be simply defined as an interface implemented by the target, but
 * also as a completely unrelated one mirroring the target methods. In this way it is possible to
 * apply the framework functionalities to objects defined by third party libraries which are not
 * under direct control.<br/>
 * A mirror interface adds the possibility to override input and output parameters with output
 * channels, so that data are transferred asynchronously avoiding the need to block execution while
 * waiting for them to be available.
 * <p/>
 * <b>Some usage examples</b>
 * <p/>
 * <b>Example 1:</b> Asynchronously merge the output of two routines.
 * <pre>
 *     <code>
 *
 *         final Routine&lt;Result, Result&gt; routine =
 *                  JavaRoutine.&lt;Result&gt;on().buildRoutine();
 *
 *         routine.invokeAsync()
 *                .pass(doSomething1.runAsync())
 *                .pass(doSomething2.runAsync())
 *                .results()
 *                .readAllInto(results);
 *     </code>
 * </pre>
 * Or simply:
 * <pre>
 *     <code>
 *
 *         final OutputChannel&lt;Result&gt; output1 = doSomething1.runAsync();
 *         final OutputChannel&lt;Result&gt; output2 = doSomething2.runAsync();
 *
 *         output1.readAllInto(results);
 *         output2.readAllInto(results);
 *     </code>
 * </pre>
 * (Note that, the order of the input or the output of the routine is not guaranteed unless the
 * proper methods are called)
 * <p/>
 * <b>Example 2:</b> Asynchronously concatenate the output of two routines.
 * <pre>
 *     <code>
 *
 *         final Routine&lt;Result, Result&gt; routine =
 *                  JavaRoutine.&lt;Result&gt;on().buildRoutine();
 *
 *         routine.invokeAsync()
 *                .pass(doSomething1.runAsync(doSomething2.runAsync()))
 *                .results()
 *                .readAllInto(results);
 *     </code>
 * </pre>
 * Or, in a more compact way:
 * <pre>
 *     <code>
 *
 *         final Routine&lt;Result, Result&gt; routine =
 *                  JavaRoutine.&lt;Result&gt;on().buildRoutine();
 *
 *         routine.runAsync(doSomething1.runAsync(doSomething2.runAsync())).readAllInto(results);
 *     </code>
 * </pre>
 * <p/>
 * <b>Example 3:</b> Asynchronously get the output of two routines.
 * <pre>
 *     <code>
 *
 *         public interface AsyncCallback {
 *
 *             &#64;AsyncOverride({Result.class, Result.class})
 *             public void onResults(OutputChannel&lt;Result&gt; result1,
 *                                      OutputChannel&lt;Result&gt; result2);
 *         }
 *
 *         final Callback callback = JavaRoutine.on(myCallback).as(Callback.class);
 *
 *         callback.onResults(doSomething1.runAsync(), doSomething2.runAsync());
 *     </code>
 * </pre>
 * Where the object <code>myCallback</code> implements a method <code>public void onResults(Result
 * result1, Result result2)</code>.
 * <p/>
 * <b>Example 4:</b> Asynchronously feed a routine from a different thread.
 * <pre>
 *     <code>
 *
 *         final IOChannel&lt;Result&gt; channel = JavaRoutine.io().&lt;Result&gt;buildChannel();
 *
 *         new Thread() {
 *
 *             &#64;Override
 *             public void run() {
 *
 *                 try {
 *
 *                     channel.input().pass(new Result());
 *
 *                 } finally {
 *
 *                     channel.close();
 *                 }
 *             }
 *
 *         }.start();
 *
 *         final Routine&lt;Result, Result&gt; routine =
 *                  JavaRoutine.&lt;Result&gt;on().buildRoutine();
 *
 *         routine.runAsync(channel).readAllInto(results);
 *     </code>
 * </pre>
 * <p/>
 * Created by davide on 9/7/14.
 *
 * @see com.bmd.jrt.annotation.Async
 * @see com.bmd.jrt.annotation.AsyncOverride
 */
public class JavaRoutine {

    //TODO: processor builder

    /**
     * Avoid direct instantiation.
     */
    protected JavaRoutine() {

    }

    /**
     * Returns a channel builder.
     *
     * @return the channel builder instance.
     */
    public static IOChannelBuilder io() {

        return new IOChannelBuilder();
    }

    /**
     * Returns a builder for a routine simply passing on the input data.
     *
     * @param <DATA> the data type.
     * @return the routine builder instance.
     */
    @Nonnull
    public static <DATA> InvocationRoutineBuilder<DATA, DATA> on() {

        return new InvocationRoutineBuilder<DATA, DATA>(
                new ClassToken<PassThroughInvocation<DATA>>() {});
    }

    /**
     * Returns a routine builder wrapping the specified target class.
     *
     * @param target the target class.
     * @return the routine builder instance.
     * @throws NullPointerException     if the specified target is null.
     * @throws IllegalArgumentException if a duplicate tag in the annotations is
     *                                  detected.
     */
    @Nonnull
    public static ClassRoutineBuilder on(@Nonnull final Class<?> target) {

        return new ClassRoutineBuilder(target);
    }

    /**
     * Returns a routine builder wrapping the specified invocation class token.
     *
     * @param classToken the invocation class token.
     * @param <INPUT>    the input type.
     * @param <OUTPUT>   the output type.
     * @return the routine builder instance.
     * @throws NullPointerException if the class token is null.
     */
    @Nonnull
    public static <INPUT, OUTPUT> InvocationRoutineBuilder<INPUT, OUTPUT> on(
            @Nonnull final ClassToken<? extends Invocation<INPUT, OUTPUT>> classToken) {

        return new InvocationRoutineBuilder<INPUT, OUTPUT>(classToken);
    }

    /**
     * Returns a routine builder wrapping the specified target object.
     *
     * @param target the target object.
     * @return the routine builder instance.
     * @throws NullPointerException     if the specified target is null.
     * @throws IllegalArgumentException if a duplicate tag in the annotations is
     *                                  detected.
     */
    @Nonnull
    public static ObjectRoutineBuilder on(@Nonnull final Object target) {

        return new ObjectRoutineBuilder(target);
    }
}
