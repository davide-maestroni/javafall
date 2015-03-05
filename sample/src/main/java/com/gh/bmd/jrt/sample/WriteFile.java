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
package com.gh.bmd.jrt.sample;

import com.gh.bmd.jrt.channel.ResultChannel;
import com.gh.bmd.jrt.common.InvocationException;
import com.gh.bmd.jrt.invocation.TemplateInvocation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Invocation writing into output file.
 * <p/>
 * Created by davide on 10/17/14.
 */
public class WriteFile extends TemplateInvocation<Chunk, Boolean> {

    private final File mFile;

    private BufferedOutputStream mOutputStream;

    public WriteFile(final File file) {

        mFile = file;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void onAbort(@Nullable final Throwable reason) {

        closeStream();
        mFile.delete();
    }

    @Override
    public void onInit() {

        try {

            mOutputStream = new BufferedOutputStream(new FileOutputStream(mFile));

        } catch (final FileNotFoundException e) {

            throw new InvocationException(e);
        }
    }

    @Override
    public void onInput(final Chunk chunk, @Nonnull final ResultChannel<Boolean> result) {

        try {

            chunk.writeTo(mOutputStream);

        } catch (final IOException e) {

            throw new InvocationException(e);
        }
    }

    @Override
    public void onResult(@Nonnull final ResultChannel<Boolean> result) {

        closeStream();
        result.pass(true);
    }

    private void closeStream() {

        try {

            mOutputStream.close();

        } catch (final IOException e) {

            throw new InvocationException(e);
        }
    }
}