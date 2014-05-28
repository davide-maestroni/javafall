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
package com.bmd.wtf.example3;

import com.bmd.wtf.Waterfall;
import com.bmd.wtf.crr.Currents;
import com.bmd.wtf.dam.Dam;
import com.bmd.wtf.example1.Downloader;
import com.bmd.wtf.example1.UrlObserver;
import com.bmd.wtf.example2.ConsumeObserver;
import com.bmd.wtf.src.Spring;
import com.bmd.wtf.xtr.arr.CurrentFactories;
import com.bmd.wtf.xtr.arr.DamFactory;
import com.bmd.wtf.xtr.arr.WaterfallArray;
import com.bmd.wtf.xtr.fld.FloodControl;
import com.bmd.wtf.xtr.qdc.Aqueduct;
import com.bmd.wtf.xtr.qdc.QueueArchway;

import java.io.File;
import java.io.IOException;

/**
 * Optimized download manager with support for retry.<br/>
 * The same waterfall, split in several parallel streams, is re-used for every download.
 */
public class DownloadManager {

    private final FloodControl<String, String, UrlObserver> mControl =
            new FloodControl<String, String, UrlObserver>(UrlObserver.class);

    private final Spring<String> mDownloadSpring;

    public DownloadManager(final int maxThreads, final File downloadDir) throws IOException {

        if (!downloadDir.isDirectory() && !downloadDir.mkdirs()) {

            throw new IOException(
                    "Could not create temp directory: " + downloadDir.getAbsolutePath());
        }

        final QueueArchway<String> archway = new QueueArchway<String>();

        final ConsumeObserver downloadObserver = new ConsumeObserver(archway, downloadDir);

        mDownloadSpring = WaterfallArray.formingFrom(Aqueduct.binding(
                Waterfall.fallingFrom(mControl.leveeControlledBy(downloadObserver)))
                                                             .thenSeparatingIn(maxThreads)
                                                             .thenFallingThrough(archway))
                                        .thenFlowingInto(CurrentFactories.singletonCurrentFactory(
                                                Currents.threadPoolCurrent(maxThreads)))
                                        .thenFallingThrough(new DamFactory<String, String>() {

                                            @Override
                                            public Dam<String, String> createForStream(
                                                    final int streamNumber) {

                                                return new Downloader(downloadDir);
                                            }
                                        }).thenFallingThrough(new DamFactory<String, String>() {

                    @Override
                    public Dam<String, String> createForStream(final int streamNumber) {

                        return new RetryPolicy<String>(archway, streamNumber, 3);
                    }
                }).thenMergingThrough(mControl.leveeControlledBy(downloadObserver)).backToSource();
    }

    public static void main(final String args[]) throws IOException {

        final int maxThreads = Integer.parseInt(args[0]);

        final File tempDir = new File(args[1]);

        final DownloadManager manager = new DownloadManager(maxThreads, tempDir);

        for (int i = 2; i < args.length; i++) {

            manager.download(args[i]);
        }
    }

    public void download(final String url) {

        mDownloadSpring.discharge(url);
    }

    public boolean isComplete(final String url) {

        return mControl.controller().isDownloaded(url);
    }
}