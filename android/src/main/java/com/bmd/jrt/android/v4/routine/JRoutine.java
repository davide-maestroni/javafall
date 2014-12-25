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
package com.bmd.jrt.android.v4.routine;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.bmd.jrt.android.builder.AndroidRoutineBuilder;
import com.bmd.jrt.common.ClassToken;
import com.bmd.jrt.invocation.Invocation;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This utility class extends the base Java routine in order to support additional routine builders
 * specific to the Android platform.<br/>
 * Routine invocations created through the returned builders can be safely restored after a change
 * in the configuration, so to avoid duplicated calls and memory leaks. Be aware, though, that the
 * invocation results will always be dispatched in the main thread, no matter the calling one was,
 * so that waiting for the outputs right after the routine invocation will result in a deadlock.
 * <p/>
 * Note that the <code>equals()</code> and <code>hashCode()</code> methods of the input parameter
 * objects might be employed to check for clashing of invocations or compute the invocation ID.<br/>
 * In case the caller cannot guarantee the correct behavior of the aforementioned method
 * implementations, a user defined ID or an input independent clash resolution should be used to
 * avoid unexpected results.
 * <p/>
 * The routine invocations will be identified by an ID number. In case a clash is detected, that is,
 * an already running invocation with the same ID exists at the time the new invocation is executed,
 * the clash is resolved based on the strategy specified through the builder. When a clash cannot be
 * resolved, for example when invocations with different implementations share the same ID, the new
 * invocation is aborted with a {@link com.bmd.jrt.android.builder.RoutineClashException}.
 * <p/>
 * For example, in order to get a resource from the network, needed to fill an activity UI:
 * <pre>
 *     <code>
 *
 *         &#64;Override
 *         protected void onCreate(final Bundle savedInstanceState) {
 *
 *             super.onCreate(savedInstanceState);
 *             setContentView(R.layout.my_activity_layout);
 *
 *             JRoutine.initContext(this);
 *
 *             if (savedInstanceState != null) {
 *
 *                 mResource = savedInstanceState.getParcelable(RESOURCE_KEY);
 *             }
 *
 *             if (mResource != null) {
 *
 *                 displayResource(mResource);
 *
 *             } else {
 *
 *                 final Routine&lt;URI, MyResource&gt; routine =
 *                         JRoutine.from(this, ClassToken.tokenOf(LoadResource.class))
 *                                 .buildRoutine();
 *                 routine.callAsync(RESOURCE_URI)
 *                        .bind(new TemplateOutputConsumer&lt;MyResource&gt;() {
 *
 *                            &#64;Override
 *                            public void onError(&#64;Nullable final Throwable error) {
 *
 *                                displayError(error);
 *                            }
 *
 *                            &#64;Override
 *                            public void onOutput(final MyResource resource) {
 *
 *                                mResource = resource;
 *                                displayResource(resource);
 *                            }
 *                        });
 *             }
 *         }
 *
 *         &#64;Override
 *         protected void onSaveInstanceState(final Bundle outState) {
 *
 *             super.onSaveInstanceState(outState);
 *             outState.putParcelable(RESOURCE_KEY, mResource);
 *         }
 *     </code>
 * </pre>
 * The above code will ensure that the loading process survives any configuration change and the
 * resulting resource is dispatched only once.
 * <p/>
 * Created by davide on 12/8/14.
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS",
                    justification = "utility class extending functionalities of another utility "
                            + "class")
public class JRoutine extends com.bmd.jrt.routine.JRoutine {

    /**
     * Returns a builder of routines linked to the specified context.
     * <p/>
     * Note that, waiting for the outputs of the built routine immediately after its invocation on
     * the main thread, will result in a deadlock. In fact the routine results will be always
     * dispatched in the main UI thread.
     *
     * @param fragment   the fragment instance.
     * @param classToken the invocation class token.
     * @param <INPUT>    the input data type.
     * @param <OUTPUT>   the output data type.
     * @return the routine builder instance.
     * @throws IllegalStateException if the specified activity is not initialized.
     * @throws NullPointerException  if any of the specified parameters is null.
     */
    @Nonnull
    public static <INPUT, OUTPUT> AndroidRoutineBuilder<INPUT, OUTPUT> from(
            @Nonnull final Fragment fragment,
            @Nonnull ClassToken<? extends Invocation<INPUT, OUTPUT>> classToken) {

        if (!LoaderInvocation.isEnabled(fragment)) {

            throw new IllegalStateException(
                    "routine creation is not enabled: be sure to call JRoutine.initContext(this) "
                            + "in fragment onCreate() method");
        }

        return new DefaultAndroidRoutineBuilder<INPUT, OUTPUT>(fragment, classToken);
    }

    /**
     * Returns a builder of routines linked to the specified context.
     * <p/>
     * Note that, waiting for the outputs of the built routine immediately after its invocation on
     * the main thread, will result in a deadlock. In fact the routine results will be always
     * dispatched in the main UI thread.
     *
     * @param activity   the activity instance.
     * @param classToken the invocation class token.
     * @param <INPUT>    the input data type.
     * @param <OUTPUT>   the output data type.
     * @return the routine builder instance.
     * @throws IllegalStateException if the specified activity is not initialized.
     * @throws NullPointerException  if any of the specified parameters is null.
     */
    @Nonnull
    public static <INPUT, OUTPUT> AndroidRoutineBuilder<INPUT, OUTPUT> from(
            @Nonnull final FragmentActivity activity,
            @Nonnull ClassToken<? extends Invocation<INPUT, OUTPUT>> classToken) {

        if (!LoaderInvocation.isEnabled(activity)) {

            throw new IllegalStateException(
                    "routine creation is not enabled: be sure to call JRoutine.initContext(this) "
                            + "in activity onCreate() method");
        }

        return new DefaultAndroidRoutineBuilder<INPUT, OUTPUT>(activity, classToken);
    }

    /**
     * Initializes the specified context so to enable the creation of routines linked to the context
     * lifecycle.<br/>
     * This method must be called in the fragment <code>onCreate()</code> method.
     *
     * @param fragment the fragment instance.
     * @throws NullPointerException if the specified fragment is null.
     */
    public static void initContext(@Nonnull final Fragment fragment) {

        LoaderInvocation.initContext(fragment);
    }

    /**
     * Initializes the specified context so to enable the creation of routines linked to the context
     * lifecycle.<br/>
     * This method must be called in the activity <code>onCreate()</code> method.
     *
     * @param activity the activity instance.
     * @throws NullPointerException if the specified activity is null.
     */
    public static void initContext(@Nonnull final FragmentActivity activity) {

        LoaderInvocation.initContext(activity);
    }
}
