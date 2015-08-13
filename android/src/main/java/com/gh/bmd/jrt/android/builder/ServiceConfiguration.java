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
package com.gh.bmd.jrt.android.builder;

import android.os.Looper;

import com.gh.bmd.jrt.log.Log;
import com.gh.bmd.jrt.runner.Runner;
import com.gh.bmd.jrt.util.Reflection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class storing the service configuration.
 * <p/>
 * Each instance is immutable, thus, in order to modify a configuration parameter, a new builder
 * must be created starting from the specific configuration instance.
 * <p/>
 * Created by davide-maestroni on 20/04/15.
 */
public final class ServiceConfiguration {

    private static final DefaultConfigurable sDefaultConfigurable = new DefaultConfigurable();

    /**
     * Empty configuration constant.<br/>The configuration has all the values set to their default.
     */
    public static final ServiceConfiguration DEFAULT_CONFIGURATION = builder().buildConfiguration();

    private final Class<? extends Log> mLogClass;

    private final Looper mLooper;

    private final Class<? extends Runner> mRunnerClass;

    /**
     * Constructor.
     *
     * @param looper      the looper instance.
     * @param runnerClass the runner class.
     * @param logClass    the log class.
     */
    private ServiceConfiguration(@Nullable final Looper looper,
            @Nullable final Class<? extends Runner> runnerClass,
            @Nullable final Class<? extends Log> logClass) {

        mLooper = looper;
        mRunnerClass = runnerClass;
        mLogClass = logClass;
    }

    /**
     * Returns a service configuration builder.
     *
     * @return the builder.
     */
    @Nonnull
    public static Builder<ServiceConfiguration> builder() {

        return new Builder<ServiceConfiguration>(sDefaultConfigurable);
    }

    /**
     * Returns a service configuration builder initialized with the specified configuration.
     *
     * @param initialConfiguration the initial configuration.
     * @return the builder.
     */
    @Nonnull
    public static Builder<ServiceConfiguration> builderFrom(
            @Nullable final ServiceConfiguration initialConfiguration) {

        return (initialConfiguration == null) ? builder()
                : new Builder<ServiceConfiguration>(sDefaultConfigurable, initialConfiguration);
    }

    /**
     * Returns a service configuration builder initialized with this configuration.
     *
     * @return the builder.
     */
    @Nonnull
    public Builder<ServiceConfiguration> builderFrom() {

        return builderFrom(this);
    }

    @Override
    public boolean equals(final Object o) {

        // auto-generated code
        if (this == o) {

            return true;
        }

        if (!(o instanceof ServiceConfiguration)) {

            return false;
        }

        final ServiceConfiguration that = (ServiceConfiguration) o;
        return !(mLogClass != null ? !mLogClass.equals(that.mLogClass) : that.mLogClass != null)
                && !(mLooper != null ? !mLooper.equals(that.mLooper) : that.mLooper != null) && !(
                mRunnerClass != null ? !mRunnerClass.equals(that.mRunnerClass)
                        : that.mRunnerClass != null);

    }

    @Override
    public int hashCode() {

        // auto-generated code
        int result = mLogClass != null ? mLogClass.hashCode() : 0;
        result = 31 * result + (mLooper != null ? mLooper.hashCode() : 0);
        result = 31 * result + (mRunnerClass != null ? mRunnerClass.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {

        return "ServiceConfiguration{" +
                "mLogClass=" + mLogClass +
                ", mLooper=" + mLooper +
                ", mRunnerClass=" + mRunnerClass +
                '}';
    }

    /**
     * Returns the log class (null by default).
     *
     * @param valueIfNotSet the default value if none was set.
     * @return the log class instance.
     */
    public Class<? extends Log> getLogClassOr(@Nullable final Class<? extends Log> valueIfNotSet) {

        final Class<? extends Log> logClass = mLogClass;
        return (logClass != null) ? logClass : valueIfNotSet;
    }

    /**
     * Returns the looper used for dispatching the results from the service (null by default).
     *
     * @param valueIfNotSet the default value if none was set.
     * @return the looper instance.
     */
    public Looper getResultLooperOr(@Nullable final Looper valueIfNotSet) {

        final Looper looper = mLooper;
        return (looper != null) ? looper : valueIfNotSet;
    }

    /**
     * Returns the runner class (null by default).
     *
     * @param valueIfNotSet the default value if none was set.
     * @return the runner class instance.
     */
    public Class<? extends Runner> getRunnerClassOr(
            @Nullable final Class<? extends Runner> valueIfNotSet) {

        final Class<? extends Runner> runnerClass = mRunnerClass;
        return (runnerClass != null) ? runnerClass : valueIfNotSet;
    }

    /**
     * Interface defining a configurable object.
     *
     * @param <TYPE> the configurable object type.
     */
    public interface Configurable<TYPE> {

        /**
         * Sets the specified configuration and returns the configurable instance.
         *
         * @param configuration the configuration.
         * @return the configurable instance.
         */
        @Nonnull
        TYPE setConfiguration(@Nonnull ServiceConfiguration configuration);
    }

    /**
     * Builder of service configurations.
     *
     * @param <TYPE> the configurable object type.
     */
    public static class Builder<TYPE> {

        private final Configurable<? extends TYPE> mConfigurable;

        private Class<? extends Log> mLogClass;

        private Looper mLooper;

        private Class<? extends Runner> mRunnerClass;

        /**
         * Constructor.
         *
         * @param configurable the configurable instance.
         */
        @SuppressWarnings("ConstantConditions")
        public Builder(@Nonnull final Configurable<? extends TYPE> configurable) {

            if (configurable == null) {

                throw new NullPointerException("the configurable instance must no be null");
            }

            mConfigurable = configurable;
        }

        /**
         * Constructor.
         *
         * @param configurable         the configurable instance.
         * @param initialConfiguration the initial configuration.
         */
        @SuppressWarnings("ConstantConditions")
        public Builder(@Nonnull final Configurable<? extends TYPE> configurable,
                @Nonnull final ServiceConfiguration initialConfiguration) {

            if (configurable == null) {

                throw new NullPointerException("the configurable instance must no be null");
            }

            mConfigurable = configurable;
            setConfiguration(initialConfiguration);
        }

        /**
         * Sets the configuration and returns the configurable object.
         *
         * @return the configurable object.
         */
        @Nonnull
        public TYPE set() {

            return mConfigurable.setConfiguration(buildConfiguration());
        }

        /**
         * Applies the specified configuration to this builder. A null value means that all the
         * configuration options need to be set to their default value, otherwise only the set
         * options will be applied.
         *
         * @param configuration the service configuration.
         * @return this builder.
         */
        @Nonnull
        public Builder<TYPE> with(@Nullable final ServiceConfiguration configuration) {

            if (configuration == null) {

                setConfiguration(DEFAULT_CONFIGURATION);
                return this;
            }

            final Looper looper = configuration.mLooper;

            if (looper != null) {

                withResultLooper(looper);
            }

            final Class<? extends Runner> runnerClass = configuration.mRunnerClass;

            if (runnerClass != null) {

                withRunnerClass(runnerClass);
            }

            final Class<? extends Log> logClass = configuration.mLogClass;

            if (logClass != null) {

                withLogClass(logClass);
            }

            return this;
        }

        /**
         * Sets the log class. A null value means that it is up to the specific implementation to
         * choose a default class.
         *
         * @param logClass the log class.
         * @return this builder.
         * @throws java.lang.IllegalArgumentException if the specified class has no default
         *                                            constructor.
         */
        @Nonnull
        public Builder<TYPE> withLogClass(@Nullable final Class<? extends Log> logClass) {

            if (logClass != null) {

                Reflection.findConstructor(logClass);
            }

            mLogClass = logClass;
            return this;
        }

        /**
         * Sets the looper on which the results from the service are dispatched. A null value means
         * that results will be dispatched on the main thread (as by default).
         *
         * @param looper the looper instance.
         * @return this builder.
         */
        @Nonnull
        public Builder<TYPE> withResultLooper(@Nullable final Looper looper) {

            mLooper = looper;
            return this;
        }

        /**
         * Sets the runner class. A null value means that it is up to the specific implementation to
         * choose a default runner.
         *
         * @param runnerClass the runner class.
         * @return this builder.
         * @throws java.lang.IllegalArgumentException if the specified class has no default
         *                                            constructor.
         */
        @Nonnull
        public Builder<TYPE> withRunnerClass(@Nullable final Class<? extends Runner> runnerClass) {

            if (runnerClass != null) {

                Reflection.findConstructor(runnerClass);
            }

            mRunnerClass = runnerClass;
            return this;
        }

        @Nonnull
        private ServiceConfiguration buildConfiguration() {

            return new ServiceConfiguration(mLooper, mRunnerClass, mLogClass);
        }

        private void setConfiguration(@Nonnull final ServiceConfiguration configuration) {

            mLooper = configuration.mLooper;
            mRunnerClass = configuration.mRunnerClass;
            mLogClass = configuration.mLogClass;
        }
    }

    /**
     * Default configurable implementation.
     */
    private static class DefaultConfigurable implements Configurable<ServiceConfiguration> {

        @Nonnull
        public ServiceConfiguration setConfiguration(
                @Nonnull final ServiceConfiguration configuration) {

            return configuration;
        }
    }
}