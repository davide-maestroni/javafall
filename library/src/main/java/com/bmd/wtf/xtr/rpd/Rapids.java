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
package com.bmd.wtf.xtr.rpd;

import com.bmd.wtf.crr.Current;
import com.bmd.wtf.crr.CurrentGenerator;
import com.bmd.wtf.fll.Classification;
import com.bmd.wtf.fll.Waterfall;
import com.bmd.wtf.lps.Leap;
import com.bmd.wtf.lps.LeapGenerator;

/**
 * This class is meant to provide utility method for employing rapid classes which use reflection
 * to simplify the building of the waterfall chain at the expense of a small performance loss.
 * <p/>
 * Even if the use of reflection in Java may slow down code execution
 * (see <a href="http://docs.oracle.com/javase/tutorial/reflect/index.html">The Reflection API</a>),
 * it is also true that this library has been designed to support parallel programming, which
 * usually involve long running tasks (such as network, disk IO, heavy processing, etc.). In such
 * cases the overhead brought by reflection does not have any significant impact on the overall
 * performance.
 * <p/>
 * Created by davide on 6/19/14.
 */
public class Rapids {

    /**
     * Avoid direct instantiation.
     */
    private Rapids() {

    }

    /**
     * Creates and returns a leap generator from the specified leap instances.
     *
     * @param leaps The leaps to return.
     * @return The newly created leap generator.
     */
    public static <SOURCE, IN, OUT> LeapGenerator<SOURCE, IN, OUT> asGenerator(
            final Leap<SOURCE, IN, OUT>... leaps) {

        return RapidGenerators.leapGenerator(leaps);
    }

    /**
     * Creates and returns a current generator from the specified current instances.
     *
     * @param currents The currents to return.
     * @return The newly created current generator.
     */
    public static CurrentGenerator asGenerator(final Current... currents) {

        return RapidGenerators.currentGenerator(currents);
    }

    /**
     * Creates and returns a current generator which instantiates objects of the specified
     * classification through a method taking the specified parameters. A method taking
     * an additional Integer parameter (that is, the fall number) is preferred to the default one.
     * A one taking a primitive int is preferred to the Integer. Finally, a method annotated
     * with {@link RapidAnnotations.Generator} is preferred to the not annotated one.<br/>
     * In case a suitable method is not found, an exception will be thrown.
     * <p/>
     * Note that a method might need to be made accessible in order to be called via
     * reflection. That means that, in case a {@link java.lang.SecurityManager} is installed, a
     * security exception might be raised based on the specific policy implemented.
     *
     * @param generator      The generator object whose method will be called.
     * @param classification The current classification.
     * @param args           The arguments to be passed to the method.
     * @return The newly created current generator.
     */
    public static CurrentGenerator currents(final Object generator,
            final Classification<? extends Current> classification, final Object... args) {

        return RapidGenerators.currentGenerator(generator, classification, args);
    }

    /**
     * Creates and returns a current generator which instantiates objects of the specified
     * type through a constructor taking the specified parameters. A constructor taking
     * an additional Integer parameter (that is, the fall number) is preferred to the default one.
     * A one taking a primitive int is preferred to the Integer. Finally, a constructor annotated
     * with {@link RapidAnnotations.Generator} is preferred to the not annotated one.<br/>
     * In case a suitable constructor is not found, an exception will be thrown.
     * <p/>
     * Note that a constructor might need to be made accessible in order to be called via
     * reflection. That means that, in case a {@link java.lang.SecurityManager} is installed, a
     * security exception might be raised based on the specific policy implemented.
     *
     * @param current     The current object to instantiate.
     * @param contextArgs The arguments to be passed to the constructor.
     * @return The newly created current generator.
     */
    public static CurrentGenerator currents(final Current current, final Object... contextArgs) {

        return RapidGenerators.currentGenerator(current.getClass(), contextArgs);
    }

    /**
     * Creates and returns a current generator which instantiates objects of the specified
     * classification through a constructor taking the specified parameters. A constructor taking
     * an additional Integer parameter (that is, the fall number) is preferred to the default one.
     * A one taking a primitive int is preferred to the Integer. Finally, a constructor annotated
     * with {@link RapidAnnotations.Generator} is preferred to the not annotated one.<br/>
     * In case a suitable constructor is not found, an exception will be thrown.
     * <p/>
     * Note that a constructor might need to be made accessible in order to be called via
     * reflection. That means that, in case a {@link java.lang.SecurityManager} is installed, a
     * security exception might be raised based on the specific policy implemented.
     *
     * @param classification The current classification.
     * @param contextArgs    The arguments to be passed to the constructor.
     * @return The newly created current generator.
     */
    public static CurrentGenerator currents(final Classification<? extends Current> classification,
            final Object... contextArgs) {

        //noinspection unchecked
        return RapidGenerators.currentGenerator(
                (Class<? extends Current>) classification.getRawType(), contextArgs);
    }

    /**
     * Creates and returns a current generator which instantiates objects of the specified
     * type through a constructor taking the specified parameters. A constructor taking
     * an additional Integer parameter (that is, the fall number) is preferred to the default one.
     * A one taking a primitive int is preferred to the Integer. Finally, a constructor annotated
     * with {@link RapidAnnotations.Generator} is preferred to the not annotated one.<br/>
     * In case a suitable constructor is not found, an exception will be thrown.
     * <p/>
     * Note that a constructor might need to be made accessible in order to be called via
     * reflection. That means that, in case a {@link java.lang.SecurityManager} is installed, a
     * security exception might be raised based on the specific policy implemented.
     *
     * @param type        The current type.
     * @param contextArgs The arguments to be passed to the constructor.
     * @return The newly created current generator.
     */
    public static CurrentGenerator currents(final Class<? extends Current> type,
            final Object... contextArgs) {

        return RapidGenerators.currentGenerator(type, contextArgs);
    }

    /**
     * Creates and returns a current generator which instantiates objects of the specified
     * type through a constructor. A constructor taking an Integer parameter (that is, the fall
     * number) is preferred to the default one. A one taking a primitive int is preferred to the
     * Integer. Finally, a constructor annotated with {@link RapidAnnotations.Generator} is
     * preferred to the not annotated one.<br/>
     * In case a suitable constructor is not found, an exception will be thrown.
     * <p/>
     * Note that a constructor might need to be made accessible in order to be called via
     * reflection. That means that, in case a {@link java.lang.SecurityManager} is installed, a
     * security exception might be raised based on the specific policy implemented.
     *
     * @param type The current type.
     * @return The newly created current generator.
     */
    public static CurrentGenerator currents(final Class<? extends Current> type) {

        return RapidGenerators.currentGenerator(type);
    }

    /**
     * Creates and returns a current generator which instantiates objects of the specified
     * classification through a constructor. A constructor taking an Integer parameter (that is,
     * the fall number) is preferred to the default one. A one taking a primitive int is preferred
     * to the Integer. Finally, a constructor annotated with {@link RapidAnnotations.Generator} is
     * preferred to the not annotated one.<br/>
     * In case a suitable constructor is not found, an exception will be thrown.
     * <p/>
     * Note that a constructor might need to be made accessible in order to be called via
     * reflection. That means that, in case a {@link java.lang.SecurityManager} is installed, a
     * security exception might be raised based on the specific policy implemented.
     *
     * @param classification The current classification.
     * @return The newly created current generator.
     */
    public static CurrentGenerator currents(
            final Classification<? extends Current> classification) {

        //noinspection unchecked
        return RapidGenerators.currentGenerator(
                (Class<? extends Current>) classification.getRawType());
    }

    /**
     * Creates and returns a rapid gate wrapping the specified waterfall.
     *
     * @param waterfall The waterfall to wrap.
     * @param <SOURCE>  The source data type.
     * @param <MOUTH>   The mouth data type.
     * @param <IN>      The input data type.
     * @param <OUT>     The output data type.
     * @param <TYPE>    The gate type.
     * @return The newly created rapid gate.
     */
    public static <SOURCE, MOUTH, IN, OUT, TYPE> RapidGate<SOURCE, MOUTH, IN, OUT, TYPE> gate(
            final Waterfall<SOURCE, MOUTH, OUT> waterfall) {

        return new WaterfallRapidGate<SOURCE, MOUTH, IN, OUT, TYPE>(waterfall);
    }

    /**
     * Creates and returns a leap generator which instantiates objects of the specified
     * classification through a method taking the specified parameters. A method taking
     * an additional Integer parameter (that is, the fall number) is preferred to the default one.
     * A one taking a primitive int is preferred to the Integer. Finally, a method annotated
     * with {@link RapidAnnotations.Generator} is preferred to the not annotated one.<br/>
     * In case a suitable method is not found, an exception will be thrown.
     * <p/>
     * Note that a method might need to be made accessible in order to be called via
     * reflection. That means that, in case a {@link java.lang.SecurityManager} is installed, a
     * security exception might be raised based on the specific policy implemented.
     *
     * @param generator      The generator object whose method will be called.
     * @param classification The leap classification.
     * @param args           The arguments to be passed to the method.
     * @return The newly created leap generator.
     */
    public static <SOURCE, IN, OUT> LeapGenerator<SOURCE, IN, OUT> leaps(final Object generator,
            final Classification<? extends Leap<SOURCE, IN, OUT>> classification,
            final Object... args) {

        return RapidGenerators.leapGenerator(generator, classification, args);
    }

    /**
     * Creates and returns a leap generator which instantiates objects of the specified
     * type through a constructor taking the specified parameters. A constructor taking
     * an additional Integer parameter (that is, the fall number) is preferred to the default one.
     * A one taking a primitive int is preferred to the Integer. Finally, a constructor annotated
     * with {@link RapidAnnotations.Generator} is preferred to the not annotated one.<br/>
     * In case a suitable constructor is not found, an exception will be thrown.
     * <p/>
     * Note that a constructor might need to be made accessible in order to be called via
     * reflection. That means that, in case a {@link java.lang.SecurityManager} is installed, a
     * security exception might be raised based on the specific policy implemented.
     *
     * @param leap        The leap object to instantiate.
     * @param contextArgs The arguments to be passed to the constructor.
     * @return The newly created leap generator.
     */
    public static <SOURCE, IN, OUT> LeapGenerator<SOURCE, IN, OUT> leaps(
            final Leap<SOURCE, IN, OUT> leap, final Object... contextArgs) {

        //noinspection unchecked
        return RapidGenerators.leapGenerator(
                (Class<? extends Leap<SOURCE, IN, OUT>>) leap.getClass(), contextArgs);
    }

    /**
     * Creates and returns a leap generator which instantiates objects of the specified
     * classification through a constructor taking the specified parameters. A constructor taking
     * an additional Integer parameter (that is, the fall number) is preferred to the default one.
     * A one taking a primitive int is preferred to the Integer. Finally, a constructor annotated
     * with {@link RapidAnnotations.Generator} is preferred to the not annotated one.<br/>
     * In case a suitable constructor is not found, an exception will be thrown.
     * <p/>
     * Note that a constructor might need to be made accessible in order to be called via
     * reflection. That means that, in case a {@link java.lang.SecurityManager} is installed, a
     * security exception might be raised based on the specific policy implemented.
     *
     * @param classification The leap classification.
     * @param contextArgs    The arguments to be passed to the constructor.
     * @return The newly created leap generator.
     */
    public static <SOURCE, IN, OUT> LeapGenerator<SOURCE, IN, OUT> leaps(
            final Classification<? extends Leap<SOURCE, IN, OUT>> classification,
            final Object... contextArgs) {

        //noinspection unchecked
        return RapidGenerators.leapGenerator(
                (Class<? extends Leap<SOURCE, IN, OUT>>) classification.getRawType(), contextArgs);
    }

    /**
     * Creates and returns a leap generator which instantiates objects of the specified
     * type through a constructor taking the specified parameters. A constructor taking
     * an additional Integer parameter (that is, the fall number) is preferred to the default one.
     * A one taking a primitive int is preferred to the Integer. Finally, a constructor annotated
     * with {@link RapidAnnotations.Generator} is preferred to the not annotated one.<br/>
     * In case a suitable constructor is not found, an exception will be thrown.
     * <p/>
     * Note that a constructor might need to be made accessible in order to be called via
     * reflection. That means that, in case a {@link java.lang.SecurityManager} is installed, a
     * security exception might be raised based on the specific policy implemented.
     *
     * @param type        The leap type.
     * @param contextArgs The arguments to be passed to the constructor.
     * @return The newly created leap generator.
     */
    public static <SOURCE, IN, OUT> LeapGenerator<SOURCE, IN, OUT> leaps(
            final Class<? extends Leap<SOURCE, IN, OUT>> type, final Object... contextArgs) {

        return RapidGenerators.leapGenerator(type, contextArgs);
    }

    /**
     * Creates and returns a leap generator which instantiates objects of the specified
     * type through a constructor. A constructor taking an Integer parameter (that is, the fall
     * number) is preferred to the default one. A one taking a primitive int is preferred to the
     * Integer. Finally, a constructor annotated with {@link RapidAnnotations.Generator} is
     * preferred to the not annotated one.<br/>
     * In case a suitable constructor is not found, an exception will be thrown.
     * <p/>
     * Note that a constructor might need to be made accessible in order to be called via
     * reflection. That means that, in case a {@link java.lang.SecurityManager} is installed, a
     * security exception might be raised based on the specific policy implemented.
     *
     * @param type The leap type.
     * @return The newly created leap generator.
     */
    public static <SOURCE, IN, OUT> LeapGenerator<SOURCE, IN, OUT> leaps(
            final Class<? extends Leap<SOURCE, IN, OUT>> type) {

        return RapidGenerators.leapGenerator(type);
    }

    /**
     * Creates and returns a leap generator which instantiates objects of the specified
     * classification through a constructor. A constructor taking an Integer parameter (that is, the fall
     * number) is preferred to the default one. A one taking a primitive int is preferred to the
     * Integer. Finally, a constructor annotated with {@link RapidAnnotations.Generator} is
     * preferred to the not annotated one.<br/>
     * In case a suitable constructor is not found, an exception will be thrown.
     * <p/>
     * Note that a constructor might need to be made accessible in order to be called via
     * reflection. That means that, in case a {@link java.lang.SecurityManager} is installed, a
     * security exception might be raised based on the specific policy implemented.
     *
     * @param classification The leap classification.
     * @return The newly created leap generator.
     */
    public static <SOURCE, IN, OUT> LeapGenerator<SOURCE, IN, OUT> leaps(
            final Classification<? extends Leap<SOURCE, IN, OUT>> classification) {

        //noinspection unchecked
        return RapidGenerators.leapGenerator(
                (Class<? extends Leap<SOURCE, IN, OUT>>) classification.getRawType());
    }
}