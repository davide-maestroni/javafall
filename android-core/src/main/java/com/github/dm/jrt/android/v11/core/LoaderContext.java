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

package com.github.dm.jrt.android.v11.core;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import com.github.dm.jrt.core.util.ConstantConditions;
import com.github.dm.jrt.core.util.Reflection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

/**
 * Class representing an Android Loader context (like Activities or Fragments).
 * <p>
 * No strong reference to the wrapped objects will be retained by this class implementations.
 * <p>
 * Created by davide-maestroni on 07/08/2015.
 */
@TargetApi(VERSION_CODES.HONEYCOMB)
public abstract class LoaderContext {

  /**
   * Avoid explicit instantiation.
   */
  private LoaderContext() {
  }

  /**
   * Returns a context wrapping the specified Activity.
   *
   * @param activity the Activity instance.
   * @return the Loader context.
   */
  @NotNull
  public static LoaderContext loaderFrom(@NotNull final Activity activity) {
    checkVersion();
    return new ActivityContext(activity, activity);
  }

  /**
   * Returns a context wrapping the specified Activity, with the specified instance as the Loader
   * Context.
   * <br>
   * In order to prevent undesired leaks, the class of the specified Context must be static.
   *
   * @param activity the Activity instance.
   * @param context  the Context used to get the application one.
   * @return the Loader context.
   * @throws java.lang.IllegalArgumentException if the class of the specified Context has not a
   *                                            static scope.
   */
  @NotNull
  public static LoaderContext loaderFrom(@NotNull final Activity activity,
      @NotNull final Context context) {
    checkVersion();
    return new ActivityContext(activity, context);
  }

  /**
   * Returns a context wrapping the specified Fragment.
   *
   * @param fragment the Fragment instance.
   * @return the Loader context.
   */
  @NotNull
  public static LoaderContext loaderFrom(@NotNull final Fragment fragment) {
    checkVersion();
    return new FragmentContext(fragment, fragment.getActivity());
  }

  /**
   * Returns a context wrapping the specified Fragment, with the specified instance as the Loader
   * Context.
   * <br>
   * In order to prevent undesired leaks, the class of the specified Context must be static.
   *
   * @param fragment the Fragment instance.
   * @param context  the Context used to get the application one.
   * @return the Loader context.
   * @throws java.lang.IllegalArgumentException if the class of the specified Context has not a
   *                                            static scope.
   */
  @NotNull
  public static LoaderContext loaderFrom(@NotNull final Fragment fragment,
      @NotNull final Context context) {
    checkVersion();
    return new FragmentContext(fragment, context);
  }

  private static void checkVersion() {
    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
      throw new UnsupportedOperationException(
          "this method is supported only for API level >= " + VERSION_CODES.HONEYCOMB
              + ": use com.github.dm.jrt.android.v4.routine.JRoutineLoaderCompat "
              + "class instead");
    }
  }

  /**
   * Returns the wrapped component.
   *
   * @return the component or null.
   */
  @Nullable
  public abstract Object getComponent();

  /**
   * Returns the Loader Context.
   *
   * @return the Context or null.
   */
  @Nullable
  public abstract Context getLoaderContext();

  /**
   * Returns the Loader manager of the specific component.
   *
   * @return the Loader manager or null.
   */
  @Nullable
  public abstract LoaderManager getLoaderManager();

  /**
   * Loader context wrapping an Activity.
   */
  @TargetApi(VERSION_CODES.HONEYCOMB)
  private static class ActivityContext extends LoaderContext {

    private final WeakReference<Activity> mActivity;

    private final WeakReference<Context> mContext;

    /**
     * Constructor.
     *
     * @param activity the wrapped Activity.
     * @param context  the wrapped Context.
     * @throws java.lang.IllegalArgumentException if the class of the specified Context has not
     *                                            a static scope.
     */
    private ActivityContext(@NotNull final Activity activity, @NotNull final Context context) {
      mActivity = new WeakReference<Activity>(ConstantConditions.notNull("Activity", activity));
      final Class<? extends Context> contextClass = context.getClass();
      if (!Reflection.hasStaticScope(contextClass)) {
        throw new IllegalArgumentException(
            "the Context class must have a static scope: " + contextClass.getName());
      }

      mContext = new WeakReference<Context>(context);
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }

      if (!(o instanceof ActivityContext)) {
        return false;
      }

      final ActivityContext that = (ActivityContext) o;
      final Activity activity = mActivity.get();
      if ((activity == null) || !activity.equals(that.mActivity.get())) {
        return false;
      }

      final Context context = mContext.get();
      return (context != null) && context.equals(that.mContext.get());
    }

    @Nullable
    @Override
    public Object getComponent() {
      return mActivity.get();
    }

    @Override
    public int hashCode() {
      final Activity activity = mActivity.get();
      int result = (activity != null) ? activity.hashCode() : 0;
      final Context context = mContext.get();
      result = 31 * result + (context != null ? context.hashCode() : 0);
      return result;
    }

    @Nullable
    @Override
    public Context getLoaderContext() {
      return mContext.get();
    }

    @Nullable
    @Override
    public LoaderManager getLoaderManager() {
      final Activity activity = mActivity.get();
      return (activity != null) ? activity.getLoaderManager() : null;
    }
  }

  /**
   * Loader context wrapping a Fragment.
   */
  @TargetApi(VERSION_CODES.HONEYCOMB)
  private static class FragmentContext extends LoaderContext {

    private final WeakReference<Context> mContext;

    private final WeakReference<Fragment> mFragment;

    /**
     * Constructor.
     *
     * @param fragment the wrapped Fragment.
     * @param context  the wrapped Context.
     * @throws java.lang.IllegalArgumentException if the class of the specified Context has not
     *                                            a static scope.
     */
    private FragmentContext(@NotNull final Fragment fragment, @NotNull final Context context) {
      mFragment = new WeakReference<Fragment>(ConstantConditions.notNull("Fragment", fragment));
      final Class<? extends Context> contextClass = context.getClass();
      if (!Reflection.hasStaticScope(contextClass)) {
        throw new IllegalArgumentException(
            "the Context class must have a static scope: " + contextClass.getName());
      }

      mContext = new WeakReference<Context>(context);
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }

      if (!(o instanceof FragmentContext)) {
        return false;
      }

      final FragmentContext that = (FragmentContext) o;
      final Fragment fragment = mFragment.get();
      if ((fragment == null) || !fragment.equals(that.mFragment.get())) {
        return false;
      }

      final Context context = mContext.get();
      return (context != null) && context.equals(that.mContext.get());
    }

    @Override
    public int hashCode() {
      final Fragment fragment = mFragment.get();
      int result = (fragment != null) ? fragment.hashCode() : 0;
      final Context context = mContext.get();
      result = 31 * result + (context != null ? context.hashCode() : 0);
      return result;
    }

    @Nullable
    @Override
    public Context getLoaderContext() {
      return mContext.get();
    }

    @Nullable
    @Override
    public LoaderManager getLoaderManager() {
      final Fragment fragment = mFragment.get();
      return (fragment != null) ? fragment.getLoaderManager() : null;
    }

    @Nullable
    @Override
    public Object getComponent() {
      return mFragment.get();
    }
  }
}
