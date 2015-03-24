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
package com.gh.bmd.jrt.processor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to indicate interfaces used as templates to generate wrapper classes
 * enabling asynchronous calls to the target instance methods.
 * <p/>
 * The target class is specified in the annotation attribute. A wrapper class implementing the
 * annotated interface will be generated in the interface package and its name will be obtained by
 * prepending "JRoutine_" to the interface simple name.<br/>
 * The routines used for calling the methods will honor the attributes specified in any optional
 * {@link com.gh.bmd.jrt.annotation.Bind}, {@link com.gh.bmd.jrt.annotation.Timeout} and
 * {@link com.gh.bmd.jrt.annotation.Pass} annotations defined for each interface method.
 * <p/>
 * Note that, you'll need to enable annotation pre-processing by adding the "jroutine-processor"
 * artifact or module to the specific project dependencies. Be sure also to include a proper rule in
 * your Proguard file, so to keep the name of all the classes implementing the specific mirror
 * interface, like, for example:
 * <pre>
 *     <code>
 *
 *         -keep public class * extends my.mirror.Interface {
 *              public &lt;init&gt;;
 *         }
 *     </code>
 * </pre>
 * <p/>
 * Created by davide on 11/3/14.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Wrap {

    /**
     * The list of wrapped classes.
     *
     * @return the wrapped classes.
     */
    Class<?> value();
}
