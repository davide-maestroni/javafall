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

package com.github.dm.jrt.object.config;

import com.github.dm.jrt.core.util.ConstantConditions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Class storing the proxy configuration.
 * <p>
 * Each instance is immutable, thus, in order to modify an existing configuration, a new builder
 * must be created from it.
 * <p>
 * The configuration allows to set:
 * <ul>
 * <li>The set of fields which are shared by the target methods and need to be synchronized. By
 * default the access to all the fields is protected. Note, however, that methods sharing the same
 * fields will never be executed in parallel.</li>
 * </ul>
 * <p>
 * Created by davide-maestroni on 04/20/2015.
 */
public final class ProxyConfiguration {

    private static final DefaultConfigurable sDefaultConfigurable = new DefaultConfigurable();

    private static final ProxyConfiguration sDefaultConfiguration = builder().buildConfiguration();

    private final List<String> mFieldNames;

    /**
     * Constructor.
     *
     * @param fieldNames the shared field names.
     */
    private ProxyConfiguration(@Nullable final List<String> fieldNames) {

        mFieldNames = fieldNames;
    }

    /**
     * Returns a proxy configuration builder.
     *
     * @return the builder.
     */
    @NotNull
    public static Builder<ProxyConfiguration> builder() {

        return new Builder<ProxyConfiguration>(sDefaultConfigurable);
    }

    /**
     * Returns a proxy configuration builder initialized with the specified configuration.
     *
     * @param initialConfiguration the initial proxy configuration.
     * @return the builder.
     */
    @NotNull
    public static Builder<ProxyConfiguration> builderFrom(
            @Nullable final ProxyConfiguration initialConfiguration) {

        return (initialConfiguration == null) ? builder()
                : new Builder<ProxyConfiguration>(sDefaultConfigurable, initialConfiguration);
    }

    /**
     * Returns a configuration with all the options set to their default.
     *
     * @return the configuration instance.
     */
    @NotNull
    public static ProxyConfiguration defaultConfiguration() {

        return sDefaultConfiguration;
    }

    /**
     * Returns a proxy configuration builder initialized with this configuration.
     *
     * @return the builder.
     */
    @NotNull
    public Builder<ProxyConfiguration> builderFrom() {

        return builderFrom(this);
    }

    /**
     * Returns the shared field names (null by default).
     *
     * @param valueIfNotSet the default value if none was set.
     * @return the field names.
     */
    public List<String> getSharedFieldsOr(@Nullable final List<String> valueIfNotSet) {

        final List<String> fieldNames = mFieldNames;
        return (fieldNames != null) ? fieldNames : valueIfNotSet;
    }

    @Override
    public int hashCode() {

        // AUTO-GENERATED CODE
        return mFieldNames != null ? mFieldNames.hashCode() : 0;
    }

    @Override
    public boolean equals(final Object o) {

        // AUTO-GENERATED CODE
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ProxyConfiguration that = (ProxyConfiguration) o;
        return !(mFieldNames != null ? !mFieldNames.equals(that.mFieldNames)
                : that.mFieldNames != null);
    }

    @Override
    public String toString() {

        // AUTO-GENERATED CODE
        return "ProxyConfiguration{" +
                "mFieldNames='" + mFieldNames + '\'' +
                '}';
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
        @NotNull
        TYPE setConfiguration(@NotNull ProxyConfiguration configuration);
    }

    /**
     * Builder of proxy configurations.
     *
     * @param <TYPE> the configurable object type.
     */
    public static final class Builder<TYPE> {

        private final Configurable<? extends TYPE> mConfigurable;

        private List<String> mFieldNames;

        /**
         * Constructor.
         *
         * @param configurable the configurable instance.
         */
        public Builder(@NotNull final Configurable<? extends TYPE> configurable) {

            mConfigurable = ConstantConditions.notNull("configurable instance", configurable);
        }

        /**
         * Constructor.
         *
         * @param configurable         the configurable instance.
         * @param initialConfiguration the initial proxy configuration.
         */
        public Builder(@NotNull final Configurable<? extends TYPE> configurable,
                @NotNull final ProxyConfiguration initialConfiguration) {

            mConfigurable = ConstantConditions.notNull("configurable instance", configurable);
            setConfiguration(initialConfiguration);
        }

        /**
         * Applies this configuration and returns the configured object.
         *
         * @return the configured object.
         */
        @NotNull
        public TYPE setConfiguration() {

            return mConfigurable.setConfiguration(buildConfiguration());
        }

        /**
         * Applies the specified configuration to this builder. A null value means that all the
         * configuration options will be reset to their default, otherwise only the non-default
         * options will be applied.
         *
         * @param configuration the proxy configuration.
         * @return this builder.
         */
        @NotNull
        public Builder<TYPE> with(@Nullable final ProxyConfiguration configuration) {

            if (configuration == null) {
                setConfiguration(defaultConfiguration());
                return this;
            }

            final List<String> fieldNames = configuration.mFieldNames;
            if (fieldNames != null) {
                withSharedFields(fieldNames);
            }

            return this;
        }

        /**
         * Sets the shared field names to empty, that is, no field is shared.
         *
         * @return this builder.
         */
        @NotNull
        public Builder<TYPE> withSharedFields() {

            mFieldNames = Collections.emptyList();
            return this;
        }

        /**
         * Sets the shared field names. A null value means that all fields are shared.
         *
         * @param fieldNames the field names.
         * @return this builder.
         */
        @NotNull
        public Builder<TYPE> withSharedFields(@Nullable final String... fieldNames) {

            mFieldNames = (fieldNames != null) ? Arrays.asList(fieldNames.clone()) : null;
            return this;
        }

        /**
         * Sets the shared field names. A null value means that all fields are shared.
         *
         * @param fieldNames the field names.
         * @return this builder.
         */
        @NotNull
        public Builder<TYPE> withSharedFields(@Nullable final List<String> fieldNames) {

            mFieldNames = (fieldNames != null) ? Collections.unmodifiableList(
                    new ArrayList<String>(fieldNames)) : null;
            return this;
        }

        @NotNull
        private ProxyConfiguration buildConfiguration() {

            return new ProxyConfiguration(mFieldNames);
        }

        private void setConfiguration(@NotNull final ProxyConfiguration configuration) {

            mFieldNames = configuration.mFieldNames;
        }
    }

    /**
     * Default configurable implementation.
     */
    private static class DefaultConfigurable implements Configurable<ProxyConfiguration> {

        @NotNull
        public ProxyConfiguration setConfiguration(
                @NotNull final ProxyConfiguration configuration) {

            return configuration;
        }
    }
}