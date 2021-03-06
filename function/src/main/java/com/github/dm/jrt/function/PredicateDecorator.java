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

package com.github.dm.jrt.function;

import com.github.dm.jrt.core.util.ConstantConditions;
import com.github.dm.jrt.core.util.DeepEqualObject;
import com.github.dm.jrt.core.util.Reflection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.dm.jrt.core.util.Reflection.asArgs;

/**
 * Class decorating a predicate instance.
 * <p>
 * Created by davide-maestroni on 10/16/2015.
 *
 * @param <IN> the input data type.
 */
public class PredicateDecorator<IN> extends DeepEqualObject implements Predicate<IN>, Decorator {

  private static final LogicalPredicate AND_PREDICATE = new LogicalPredicate();

  private static final LogicalPredicate CLOSE_PREDICATE = new LogicalPredicate();

  private static final LogicalPredicate NEGATE_PREDICATE = new LogicalPredicate();

  private static final LogicalPredicate OPEN_PREDICATE = new LogicalPredicate();

  private static final LogicalPredicate OR_PREDICATE = new LogicalPredicate();

  private static final PredicateDecorator<Object> sNegative =
      new PredicateDecorator<Object>(new Predicate<Object>() {

        public boolean test(final Object o) {
          return false;
        }
      });

  private static final PredicateDecorator<Object> sNotNull =
      new PredicateDecorator<Object>(new Predicate<Object>() {

        public boolean test(final Object o) {
          return (o != null);
        }
      });

  private static final PredicateDecorator<Object> sIsNull = sNotNull.negate();

  private static final PredicateDecorator<Object> sPositive = sNegative.negate();

  private final Predicate<? super IN> mPredicate;

  private final List<Predicate<?>> mPredicates;

  /**
   * Constructor.
   *
   * @param predicate the core predicate.
   */
  private PredicateDecorator(@NotNull final Predicate<? super IN> predicate) {
    this(predicate, Collections.<Predicate<?>>singletonList(
        ConstantConditions.notNull("predicate instance", predicate)));
  }

  /**
   * Constructor.
   *
   * @param predicate  the core predicate.
   * @param predicates the list of wrapped predicates.
   */
  private PredicateDecorator(@NotNull final Predicate<? super IN> predicate,
      @NotNull final List<Predicate<?>> predicates) {
    super(asArgs(predicates));
    mPredicate = predicate;
    mPredicates = predicates;
  }

  /**
   * Decorates the specified predicate instance so to provide additional features.
   * <br>
   * The returned object will support concatenation and comparison.
   * <p>
   * Note that the passed object is expected to have a functional behavior, that is, it must not
   * retain a mutable internal state.
   * <br>
   * Note also that any external object used inside the function must be synchronized in order to
   * avoid concurrency issues.
   *
   * @param predicate the predicate instance.
   * @param <IN>      the input data type.
   * @return the decorated predicate.
   */
  @NotNull
  public static <IN> PredicateDecorator<IN> decorate(@NotNull final Predicate<IN> predicate) {
    if (predicate instanceof PredicateDecorator) {
      return (PredicateDecorator<IN>) predicate;
    }

    return new PredicateDecorator<IN>(predicate);
  }

  /**
   * Returns a predicate decorator testing for equality to the specified object.
   * <br>
   * The returned object will support concatenation and comparison.
   *
   * @param targetRef the target reference.
   * @param <IN>      the input data type.
   * @return the predicate decorator.
   */
  @NotNull
  public static <IN> PredicateDecorator<IN> isEqualTo(@Nullable final Object targetRef) {
    if (targetRef == null) {
      return isNull();
    }

    return new PredicateDecorator<IN>(new EqualToPredicate<IN>(targetRef));
  }

  /**
   * Returns a predicate decorator testing whether the passed inputs are instances of the specified
   * class.
   * <br>
   * The returned object will support concatenation and comparison.
   *
   * @param type the class type.
   * @param <IN> the input data type.
   * @return the predicate decorator.
   */
  @NotNull
  public static <IN> PredicateDecorator<IN> isInstanceOf(@NotNull final Class<?> type) {
    return new PredicateDecorator<IN>(
        new InstanceOfPredicate<IN>(ConstantConditions.notNull("type", type)));
  }

  /**
   * Returns a predicate decorator returning true when the passed argument is not null.
   * <br>
   * The returned object will support concatenation and comparison.
   *
   * @param <IN> the input data type.
   * @return the predicate decorator.
   */
  @NotNull
  @SuppressWarnings("unchecked")
  public static <IN> PredicateDecorator<IN> isNotNull() {
    return (PredicateDecorator<IN>) sNotNull;
  }

  /**
   * Returns a predicate decorator returning true when the passed argument is null.
   * <br>
   * The returned object will support concatenation and comparison.
   *
   * @param <IN> the input data type.
   * @return the predicate decorator.
   */
  @NotNull
  @SuppressWarnings("unchecked")
  public static <IN> PredicateDecorator<IN> isNull() {
    return (PredicateDecorator<IN>) sIsNull;
  }

  /**
   * Returns a predicate decorator testing for identity to the specified object.
   * <br>
   * The returned object will support concatenation and comparison.
   *
   * @param targetRef the target reference.
   * @param <IN>      the input data type.
   * @return the predicate decorator.
   */
  @NotNull
  public static <IN> PredicateDecorator<IN> isSameAs(@Nullable final Object targetRef) {
    if (targetRef == null) {
      return isNull();
    }

    return new PredicateDecorator<IN>(new SameAsPredicate<IN>(targetRef));
  }

  /**
   * Returns a predicate decorator always returning false.
   * <br>
   * The returned object will support concatenation and comparison.
   *
   * @param <IN> the input data type.
   * @return the predicate decorator.
   */
  @NotNull
  @SuppressWarnings("unchecked")
  public static <IN> PredicateDecorator<IN> negative() {
    return (PredicateDecorator<IN>) sNegative;
  }

  /**
   * Returns a predicate decorator always returning true.
   * <br>
   * The returned object will support concatenation and comparison.
   *
   * @param <IN> the input data type.
   * @return the predicate decorator.
   */
  @NotNull
  @SuppressWarnings("unchecked")
  public static <IN> PredicateDecorator<IN> positive() {
    return (PredicateDecorator<IN>) sPositive;
  }

  /**
   * Returns a composed predicate decorator that represents a short-circuiting logical AND of this
   * predicate and another.
   *
   * @param other a predicate that will be logically-ANDed with this predicate.
   * @return the composed predicate.
   */
  @NotNull
  public PredicateDecorator<IN> and(@NotNull final Predicate<? super IN> other) {
    ConstantConditions.notNull("predicate instance", other);
    final List<Predicate<?>> predicates = mPredicates;
    final ArrayList<Predicate<?>> newPredicates =
        new ArrayList<Predicate<?>>(predicates.size() + 4);
    newPredicates.add(OPEN_PREDICATE);
    newPredicates.addAll(predicates);
    newPredicates.add(AND_PREDICATE);
    if (other instanceof PredicateDecorator) {
      newPredicates.addAll(((PredicateDecorator<?>) other).mPredicates);

    } else {
      newPredicates.add(other);
    }

    newPredicates.add(CLOSE_PREDICATE);
    return new PredicateDecorator<IN>(new AndPredicate<IN>(mPredicate, other), newPredicates);
  }

  public boolean hasStaticScope() {
    for (final Predicate<?> predicate : mPredicates) {
      if (!Reflection.hasStaticScope(predicate)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Returns a predicate decorator that represents the logical negation of this predicate.
   *
   * @return the negated predicate.
   */
  @NotNull
  @SuppressWarnings("unchecked")
  public PredicateDecorator<IN> negate() {
    final List<Predicate<?>> predicates = mPredicates;
    final int size = predicates.size();
    final ArrayList<Predicate<?>> newPredicates = new ArrayList<Predicate<?>>(size + 1);
    if (size == 1) {
      newPredicates.add(NEGATE_PREDICATE);
      newPredicates.add(predicates.get(0));

    } else {
      final Predicate<?> first = predicates.get(0);
      if (first == NEGATE_PREDICATE) {
        newPredicates.add(predicates.get(1));

      } else {
        newPredicates.add(first);
        for (int i = 1; i < size; ++i) {
          final Predicate<?> predicate = predicates.get(i);
          if (predicate == NEGATE_PREDICATE) {
            ++i;

          } else if (predicate == OR_PREDICATE) {
            newPredicates.add(AND_PREDICATE);

          } else if (predicate == AND_PREDICATE) {
            newPredicates.add(OR_PREDICATE);

          } else {
            if ((predicate != OPEN_PREDICATE) && (predicate != CLOSE_PREDICATE)) {
              newPredicates.add(NEGATE_PREDICATE);
            }

            newPredicates.add(predicate);
          }
        }
      }
    }

    final Predicate<? super IN> predicate = mPredicate;
    if (predicate instanceof NegatePredicate) {
      return new PredicateDecorator<IN>(((NegatePredicate<? super IN>) predicate).mPredicate,
          newPredicates);
    }

    return new PredicateDecorator<IN>(new NegatePredicate<IN>(predicate), newPredicates);
  }

  /**
   * Returns a composed predicate decorator that represents a short-circuiting logical OR of this
   * predicate and another.
   *
   * @param other a predicate that will be logically-ORed with this predicate.
   * @return the composed predicate.
   */
  @NotNull
  public PredicateDecorator<IN> or(@NotNull final Predicate<? super IN> other) {
    ConstantConditions.notNull("predicate instance", other);
    final List<Predicate<?>> predicates = mPredicates;
    final ArrayList<Predicate<?>> newPredicates =
        new ArrayList<Predicate<?>>(predicates.size() + 4);
    newPredicates.add(OPEN_PREDICATE);
    newPredicates.addAll(predicates);
    newPredicates.add(OR_PREDICATE);
    if (other instanceof PredicateDecorator) {
      newPredicates.addAll(((PredicateDecorator<?>) other).mPredicates);

    } else {
      newPredicates.add(other);
    }

    newPredicates.add(CLOSE_PREDICATE);
    return new PredicateDecorator<IN>(new OrPredicate<IN>(mPredicate, other), newPredicates);
  }

  /**
   * Predicate implementation logically-ANDing the wrapped ones.
   *
   * @param <IN> the input data type.
   */
  private static final class AndPredicate<IN> implements Predicate<IN> {

    private final Predicate<? super IN> mOther;

    private final Predicate<? super IN> mPredicate;

    /**
     * Constructor.
     *
     * @param predicate the wrapped predicate.
     * @param other     the other predicate to be logically-ANDed.
     */
    private AndPredicate(@NotNull final Predicate<? super IN> predicate,
        @NotNull final Predicate<? super IN> other) {
      mPredicate = predicate;
      mOther = other;
    }

    public boolean test(final IN in) throws Exception {
      return mPredicate.test(in) && mOther.test(in);
    }
  }

  /**
   * Predicate implementation testing for equality.
   *
   * @param <IN> the input data type.
   */
  private static class EqualToPredicate<IN> extends DeepEqualObject implements Predicate<IN> {

    private final Object mOther;

    /**
     * Constructor.
     *
     * @param other the other object to test against.
     */
    private EqualToPredicate(@NotNull final Object other) {
      super(asArgs(other));
      mOther = other;
    }

    public boolean test(final IN in) {
      return mOther.equals(in);
    }
  }

  /**
   * Predicate testing whether an object is an instance of a specific class.
   *
   * @param <IN> the input data type.
   */
  private static class InstanceOfPredicate<IN> extends DeepEqualObject implements Predicate<IN> {

    private final Class<?> mType;

    /**
     * Constructor.
     *
     * @param type the class type.
     */
    private InstanceOfPredicate(@NotNull final Class<?> type) {
      super(asArgs(type));
      mType = type;
    }

    public boolean test(final IN in) {
      return mType.isInstance(in);
    }
  }

  /**
   * Class indicating a logical operation (like AND and OR).
   */
  private static class LogicalPredicate implements Predicate<Object> {

    public boolean test(final Object o) {
      throw new UnsupportedOperationException("should never be called");
    }
  }

  /**
   * Predicate implementation negating the wrapped one.
   *
   * @param <IN> the input data type.
   */
  private static final class NegatePredicate<IN> implements Predicate<IN> {

    private final Predicate<? super IN> mPredicate;

    /**
     * Constructor.
     *
     * @param predicate the wrapped predicate.
     */
    private NegatePredicate(@NotNull final Predicate<? super IN> predicate) {
      mPredicate = predicate;
    }

    public boolean test(final IN in) throws Exception {
      return !mPredicate.test(in);
    }
  }

  /**
   * Predicate implementation logically-ORing the wrapped ones.
   *
   * @param <IN> the input data type.
   */
  private static final class OrPredicate<IN> implements Predicate<IN> {

    private final Predicate<? super IN> mOther;

    private final Predicate<? super IN> mPredicate;

    /**
     * Constructor.
     *
     * @param predicate the wrapped predicate.
     * @param other     the other predicate to be logically-ORed.
     */
    private OrPredicate(@NotNull final Predicate<? super IN> predicate,
        @NotNull final Predicate<? super IN> other) {
      mPredicate = predicate;
      mOther = other;
    }

    public boolean test(final IN in) throws Exception {
      return mPredicate.test(in) || mOther.test(in);
    }
  }

  /**
   * Predicate implementation testing for identity.
   *
   * @param <IN> the input data type.
   */
  private static class SameAsPredicate<IN> implements Predicate<IN> {

    private final Object mOther;

    /**
     * Constructor.
     *
     * @param other the other object to test against.
     */
    private SameAsPredicate(@NotNull final Object other) {
      mOther = other;
    }

    @Override
    public int hashCode() {
      return mOther.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }

      if ((o == null) || (getClass() != o.getClass())) {
        return false;
      }

      final SameAsPredicate<?> that = (SameAsPredicate<?>) o;
      return (mOther == that.mOther);
    }

    public boolean test(final IN in) {
      return (mOther == in);
    }
  }

  public boolean test(final IN in) throws Exception {
    return mPredicate.test(in);
  }
}
