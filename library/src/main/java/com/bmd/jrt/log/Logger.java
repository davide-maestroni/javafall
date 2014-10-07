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

import com.bmd.jrt.log.Log.LogLevel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class used for log messages.
 * <p/>
 * Created by davide on 10/3/14.
 */
public class Logger {

    private static final int DEBUG_LEVEL = LogLevel.DEBUG.ordinal();

    private static final int ERROR_LEVEL = LogLevel.ERROR.ordinal();

    private static final int WARNING_LEVEL = LogLevel.WARNING.ordinal();

    private static final AtomicReference<Log> sLog = new AtomicReference<Log>(new SystemLog());

    private static final AtomicReference<LogLevel> sLogLevel =
            new AtomicReference<LogLevel>(LogLevel.ERROR);

    private final List<Object> mContextList;

    private final Object[] mContexts;

    private final int mLevel;

    private final Log mLog;

    private final LogLevel mLogLevel;

    /**
     * Constructor.
     *
     * @param contexts the array of contexts.
     * @param log      the log instance.
     * @param level    the log level.
     * @throws NullPointerException if one of the parameters is null.
     */
    private Logger(final Object[] contexts, final Log log, final LogLevel level) {

        if (log == null) {

            throw new NullPointerException("the log instance must not be null");
        }

        if (level == null) {

            throw new NullPointerException("the log level must not be null");
        }

        mContexts = contexts.clone();
        mLog = log;
        mLogLevel = level;
        mLevel = level.ordinal();
        mContextList = Arrays.asList(mContexts);
    }

    /**
     * Creates a new logger.
     *
     * @param log      the log instance.
     * @param level    the log level.
     * @param contexts the array of contexts.
     * @return the new logger.
     * @throws NullPointerException if one of the parameters is null.
     */
    public static Logger create(final Log log, final LogLevel level, final Object... contexts) {

        return new Logger(contexts, log, level);
    }

    /**
     * Gets the default log instance.
     *
     * @return the log instance.
     */
    public static Log getDefaultLog() {

        return sLog.get();
    }

    /**
     * Sets the default log instance.
     *
     * @param log the log instance.
     * @throws NullPointerException if the specified level is null.
     */
    public static void setDefaultLog(final Log log) {

        if (log == null) {

            throw new NullPointerException("the log instance must not be null");
        }

        sLog.set(log);
    }

    /**
     * Gets the default log level.
     *
     * @return the log level.
     */
    public static LogLevel getDefaultLogLevel() {

        return sLogLevel.get();
    }

    /**
     * Sets the default log level.
     *
     * @param level the log level.
     * @throws NullPointerException if the specified level is null.
     */
    public static void setDefaultLogLevel(final LogLevel level) {

        if (level == null) {

            throw new NullPointerException("the log level must not be null");
        }

        sLogLevel.set(level);
    }

    /**
     * Prints the stack trace of the specified throwable into a string.
     *
     * @param throwable the throwable instance.
     * @return the printed stack trace.
     * @throws NullPointerException if the specified throwable is null.
     */
    public static String printStackTrace(final Throwable throwable) {

        if (throwable == null) {

            return null;
        }

        final StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));

        return writer.toString();
    }

    /**
     * Logs a debug message.
     *
     * @param message the message.
     */
    public void dbg(final String message) {

        if (mLevel <= DEBUG_LEVEL) {

            mLog.dbg(mContextList, message);
        }
    }

    /**
     * Logs a debug message.
     *
     * @param format the message format.
     * @param arg1   the first format argument.
     */
    public void dbg(final String format, final Object arg1) {

        if (mLevel <= DEBUG_LEVEL) {

            mLog.dbg(mContextList, String.format(format, arg1));
        }
    }

    /**
     * Logs a debug message.
     *
     * @param format the message format.
     * @param arg1   the first format argument.
     * @param arg2   the second format argument.
     */
    public void dbg(final String format, final Object arg1, final Object arg2) {

        if (mLevel <= DEBUG_LEVEL) {

            mLog.dbg(mContextList, String.format(format, arg1, arg2));
        }
    }

    /**
     * Logs a debug message.
     *
     * @param format the message format.
     * @param arg1   the first format argument.
     * @param arg2   the second format argument.
     * @param arg3   the third format argument.
     */
    public void dbg(final String format, final Object arg1, final Object arg2, final Object arg3) {

        if (mLevel <= DEBUG_LEVEL) {

            mLog.dbg(mContextList, String.format(format, arg1, arg2, arg3));
        }
    }

    /**
     * Logs a debug message.
     *
     * @param format the message format.
     * @param arg1   the first format argument.
     * @param arg2   the second format argument.
     * @param arg3   the third format argument.
     * @param arg4   the fourth format argument.
     */
    public void dbg(final String format, final Object arg1, final Object arg2, final Object arg3,
            final Object arg4) {

        if (mLevel <= DEBUG_LEVEL) {

            mLog.dbg(mContextList, String.format(format, arg1, arg2, arg3, arg4));
        }
    }

    /**
     * Logs a debug message.
     *
     * @param format the message format.
     * @param args   the format arguments.
     */
    public void dbg(final String format, final Object... args) {

        if (mLevel <= DEBUG_LEVEL) {

            mLog.dbg(mContextList, String.format(format, args));
        }
    }

    /**
     * Logs a debug exception.
     *
     * @param t the related throwable.
     */
    public void dbg(final Throwable t) {

        if (mLevel <= DEBUG_LEVEL) {

            mLog.dbg(mContextList, printStackTrace(t));
        }
    }

    /**
     * Logs a debug message.
     *
     * @param t       the related throwable.
     * @param message the message.
     */
    public void dbg(final Throwable t, final String message) {

        if (mLevel <= DEBUG_LEVEL) {

            mLog.dbg(mContextList, message, t);
        }
    }

    /**
     * Logs a debug message.
     *
     * @param t      the related throwable.
     * @param format the message format.
     * @param arg1   the first format argument.
     */
    public void dbg(final Throwable t, final String format, final Object arg1) {

        if (mLevel <= DEBUG_LEVEL) {

            mLog.dbg(mContextList, String.format(format, arg1), t);
        }
    }

    /**
     * Logs a debug message.
     *
     * @param t      the related throwable.
     * @param format the message format.
     * @param arg1   the first format argument.
     * @param arg2   the second format argument.
     */
    public void dbg(final Throwable t, final String format, final Object arg1, final Object arg2) {

        if (mLevel <= DEBUG_LEVEL) {

            mLog.dbg(mContextList, String.format(format, arg1, arg2), t);
        }
    }

    /**
     * Logs a debug message.
     *
     * @param t      the related throwable.
     * @param format the message format.
     * @param arg1   the first format argument.
     * @param arg2   the second format argument.
     * @param arg3   the third format argument.
     */
    public void dbg(final Throwable t, final String format, final Object arg1, final Object arg2,
            final Object arg3) {

        if (mLevel <= DEBUG_LEVEL) {

            mLog.dbg(mContextList, String.format(format, arg1, arg2, arg3), t);
        }
    }

    /**
     * Logs a debug message.
     *
     * @param t      the related throwable.
     * @param format the message format.
     * @param arg1   the first format argument.
     * @param arg2   the second format argument.
     * @param arg3   the third format argument.
     * @param arg4   the fourth format argument.
     */
    public void dbg(final Throwable t, final String format, final Object arg1, final Object arg2,
            final Object arg3, final Object arg4) {

        if (mLevel <= DEBUG_LEVEL) {

            mLog.dbg(mContextList, String.format(format, arg1, arg2, arg3, arg4), t);
        }
    }

    /**
     * Logs a debug message.
     *
     * @param t      the related throwable.
     * @param format the message format.
     * @param args   the format arguments.
     */
    public void dbg(final Throwable t, final String format, final Object... args) {

        if (mLevel <= DEBUG_LEVEL) {

            mLog.dbg(mContextList, String.format(format, args), t);
        }
    }

    /**
     * Logs an error message.
     *
     * @param message the message.
     */
    public void err(final String message) {

        if (mLevel <= ERROR_LEVEL) {

            mLog.err(mContextList, message);
        }
    }

    /**
     * Logs an error message.
     *
     * @param format the message format.
     * @param arg1   the first format argument.
     */
    public void err(final String format, final Object arg1) {

        if (mLevel <= ERROR_LEVEL) {

            mLog.err(mContextList, String.format(format, arg1));
        }
    }

    /**
     * Logs an error message.
     *
     * @param format the message format.
     * @param arg1   the first format argument.
     * @param arg2   the second format argument.
     */
    public void err(final String format, final Object arg1, final Object arg2) {

        if (mLevel <= ERROR_LEVEL) {

            mLog.err(mContextList, String.format(format, arg1, arg2));
        }
    }

    /**
     * Logs an error message.
     *
     * @param format the message format.
     * @param arg1   the first format argument.
     * @param arg2   the second format argument.
     * @param arg3   the third format argument.
     */
    public void err(final String format, final Object arg1, final Object arg2, final Object arg3) {

        if (mLevel <= ERROR_LEVEL) {

            mLog.err(mContextList, String.format(format, arg1, arg2, arg3));
        }
    }

    /**
     * Logs an error message.
     *
     * @param format the message format.
     * @param arg1   the first format argument.
     * @param arg2   the second format argument.
     * @param arg3   the third format argument.
     * @param arg4   the fourth format argument.
     */
    public void err(final String format, final Object arg1, final Object arg2, final Object arg3,
            final Object arg4) {

        if (mLevel <= ERROR_LEVEL) {

            mLog.err(mContextList, String.format(format, arg1, arg2, arg3, arg4));
        }
    }

    /**
     * Logs an error message.
     *
     * @param format the message format.
     * @param args   the format arguments.
     */
    public void err(final String format, final Object... args) {

        if (mLevel <= ERROR_LEVEL) {

            mLog.err(mContextList, String.format(format, args));
        }
    }

    /**
     * Logs an error exception.
     *
     * @param t the related throwable.
     */
    public void err(final Throwable t) {

        if (mLevel <= ERROR_LEVEL) {

            mLog.err(mContextList, printStackTrace(t));
        }
    }

    /**
     * Logs an error message.
     *
     * @param t       the related throwable.
     * @param message the message.
     */
    public void err(final Throwable t, final String message) {

        if (mLevel <= ERROR_LEVEL) {

            mLog.err(mContextList, message, t);
        }
    }

    /**
     * Logs an error message.
     *
     * @param t      the related throwable.
     * @param format the message format.
     * @param arg1   the first format argument.
     */
    public void err(final Throwable t, final String format, final Object arg1) {

        if (mLevel <= ERROR_LEVEL) {

            mLog.err(mContextList, String.format(format, arg1), t);
        }
    }

    /**
     * Logs an error message.
     *
     * @param t      the related throwable.
     * @param format the message format.
     * @param arg1   the first format argument.
     * @param arg2   the second format argument.
     */
    public void err(final Throwable t, final String format, final Object arg1, final Object arg2) {

        if (mLevel <= ERROR_LEVEL) {

            mLog.err(mContextList, String.format(format, arg1, arg2), t);
        }
    }

    /**
     * Logs an error message.
     *
     * @param t      the related throwable.
     * @param format the message format.
     * @param arg1   the first format argument.
     * @param arg2   the second format argument.
     * @param arg3   the third format argument.
     */
    public void err(final Throwable t, final String format, final Object arg1, final Object arg2,
            final Object arg3) {

        if (mLevel <= ERROR_LEVEL) {

            mLog.err(mContextList, String.format(format, arg1, arg2, arg3), t);
        }
    }

    /**
     * Logs an error message.
     *
     * @param t      the related throwable.
     * @param format the message format.
     * @param arg1   the first format argument.
     * @param arg2   the second format argument.
     * @param arg3   the third format argument.
     * @param arg4   the fourth format argument.
     */
    public void err(final Throwable t, final String format, final Object arg1, final Object arg2,
            final Object arg3, final Object arg4) {

        if (mLevel <= ERROR_LEVEL) {

            mLog.err(mContextList, String.format(format, arg1, arg2, arg3, arg4), t);
        }
    }

    /**
     * Logs an error message.
     *
     * @param t      the related throwable.
     * @param format the message format.
     * @param args   the format arguments.
     */
    public void err(final Throwable t, final String format, final Object... args) {

        if (mLevel <= ERROR_LEVEL) {

            mLog.err(mContextList, String.format(format, args), t);
        }
    }

    /**
     * Returns the list of contexts.
     *
     * @return the list of contexts.
     */
    public List<Object> getContextList() {

        return mContextList;
    }

    /**
     * Returns the log instance of this logger.
     *
     * @return the log instance.
     */
    public Log getLog() {

        return mLog;
    }

    /**
     * Returns the log level of this logger.
     *
     * @return the log level.
     */
    public LogLevel getLogLevel() {

        return mLogLevel;
    }

    /**
     * Creates a new logger with the same log instance and log level, but adding the specified
     * contexts to the contexts array.
     *
     * @param contexts the array of contexts.
     * @return the new logger.
     */
    public Logger subContextLogger(final Object... contexts) {

        final Object[] thisContexts = mContexts;
        final int thisLength = thisContexts.length;
        final int length = contexts.length;
        final Object[] newContexts = new Object[thisLength + length];

        System.arraycopy(thisContexts, 0, newContexts, 0, thisLength);
        System.arraycopy(contexts, 0, newContexts, thisLength, length);

        return new Logger(newContexts, mLog, mLogLevel);
    }

    /**
     * Logs a warning message.
     *
     * @param message the message.
     */
    public void wrn(final String message) {

        if (mLevel <= WARNING_LEVEL) {

            mLog.wrn(mContextList, message);
        }
    }

    /**
     * Logs a warning message.
     *
     * @param format the message format.
     * @param arg1   the first format argument.
     */
    public void wrn(final String format, final Object arg1) {

        if (mLevel <= WARNING_LEVEL) {

            mLog.wrn(mContextList, String.format(format, arg1));
        }
    }

    /**
     * Logs a warning message.
     *
     * @param format the message format.
     * @param arg1   the first format argument.
     * @param arg2   the second format argument.
     */
    public void wrn(final String format, final Object arg1, final Object arg2) {

        if (mLevel <= WARNING_LEVEL) {

            mLog.wrn(mContextList, String.format(format, arg1, arg2));
        }
    }

    /**
     * Logs a warning message.
     *
     * @param format the message format.
     * @param arg1   the first format argument.
     * @param arg2   the second format argument.
     * @param arg3   the third format argument.
     */
    public void wrn(final String format, final Object arg1, final Object arg2, final Object arg3) {

        if (mLevel <= WARNING_LEVEL) {

            mLog.wrn(mContextList, String.format(format, arg1, arg2, arg3));
        }
    }

    /**
     * Logs a warning message.
     *
     * @param format the message format.
     * @param arg1   the first format argument.
     * @param arg2   the second format argument.
     * @param arg3   the third format argument.
     * @param arg4   the fourth format argument.
     */
    public void wrn(final String format, final Object arg1, final Object arg2, final Object arg3,
            final Object arg4) {

        if (mLevel <= WARNING_LEVEL) {

            mLog.wrn(mContextList, String.format(format, arg1, arg2, arg3, arg4));
        }
    }

    /**
     * Logs a warning message.
     *
     * @param format the message format.
     * @param args   the format arguments.
     */
    public void wrn(final String format, final Object... args) {

        if (mLevel <= WARNING_LEVEL) {

            mLog.wrn(mContextList, String.format(format, args));
        }
    }

    /**
     * Logs a warning exception.
     *
     * @param t the related throwable.
     */
    public void wrn(final Throwable t) {

        if (mLevel <= WARNING_LEVEL) {

            mLog.wrn(mContextList, printStackTrace(t));
        }
    }

    /**
     * Logs a warning message.
     *
     * @param t       the related throwable.
     * @param message the message.
     */
    public void wrn(final Throwable t, final String message) {

        if (mLevel <= WARNING_LEVEL) {

            mLog.wrn(mContextList, message, t);
        }
    }

    /**
     * Logs a warning message.
     *
     * @param t      the related throwable.
     * @param format the message format.
     * @param arg1   the first format argument.
     */
    public void wrn(final Throwable t, final String format, final Object arg1) {

        if (mLevel <= WARNING_LEVEL) {

            mLog.wrn(mContextList, String.format(format, arg1), t);
        }
    }

    /**
     * Logs a warning message.
     *
     * @param t      the related throwable.
     * @param format the message format.
     * @param arg1   the first format argument.
     * @param arg2   the second format argument.
     */
    public void wrn(final Throwable t, final String format, final Object arg1, final Object arg2) {

        if (mLevel <= WARNING_LEVEL) {

            mLog.wrn(mContextList, String.format(format, arg1, arg2), t);
        }
    }

    /**
     * Logs a warning message.
     *
     * @param t      the related throwable.
     * @param format the message format.
     * @param arg1   the first format argument.
     * @param arg2   the second format argument.
     * @param arg3   the third format argument.
     */
    public void wrn(final Throwable t, final String format, final Object arg1, final Object arg2,
            final Object arg3) {

        if (mLevel <= WARNING_LEVEL) {

            mLog.wrn(mContextList, String.format(format, arg1, arg2, arg3), t);
        }
    }

    /**
     * Logs a warning message.
     *
     * @param t      the related throwable.
     * @param format the message format.
     * @param arg1   the first format argument.
     * @param arg2   the second format argument.
     * @param arg3   the third format argument.
     * @param arg4   the fourth format argument.
     */
    public void wrn(final Throwable t, final String format, final Object arg1, final Object arg2,
            final Object arg3, final Object arg4) {

        if (mLevel <= WARNING_LEVEL) {

            mLog.wrn(mContextList, String.format(format, arg1, arg2, arg3, arg4), t);
        }
    }

    /**
     * Logs a warning message.
     *
     * @param t      the related throwable.
     * @param format the message format.
     * @param args   the format arguments.
     */
    public void wrn(final Throwable t, final String format, final Object... args) {

        if (mLevel <= WARNING_LEVEL) {

            mLog.wrn(mContextList, String.format(format, args), t);
        }
    }
}