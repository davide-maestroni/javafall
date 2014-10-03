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
package com.bmd.jrt.log;

/**
 * Created by davide on 10/3/14.
 */
public interface Log {

    public void dbg(String message);

    public void err(String message);

    public void wrn(String message);

    public enum LogLevel {

        DEBUG,
        WARNING,
        ERROR,
        SILENT
    }
}
