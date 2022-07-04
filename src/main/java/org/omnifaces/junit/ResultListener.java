/*
 * Copyright (c) 2022-2022 OmniFaces
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package org.omnifaces.junit;

import static java.nio.file.Files.writeString;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

/**
 * Listener for JUnit based tests that peeks track of the result and progress for all
 * tests in a multi-module maven project.
 *
 * @author Arjan Tijms
 *
 */
public class ResultListener extends RunListener {

    private static final Logger logger = Logger.getLogger(ResultListener.class.getName());

    private static final String GREEN = "\033[0;32m";
    private static final String GREEN_BOLD = "\033[1;32m";

    private static final String RED = "\033[0;31m";
    public static final String RED_BOLD = "\033[1;31m";

    @Override
    public void testRunFinished(Result result) throws Exception {
        String home = System.getProperty("test.home");

        try (RandomAccessFile file = new RandomAccessFile(home + "/test_summary.txt", "rw");
                FileChannel channel = file.getChannel();
                FileLock lock = channel.lock()) {

            String[] summary = Files.readString(Path.of(home + "/test_summary.txt"))
                                    .split(" ");

            int runCount = result.getRunCount();
            int assumptionFailureCount = result.getAssumptionFailureCount();
            int failureCount = result.getFailureCount();
            int ignoreCount = result.getIgnoreCount();

            if (summary.length >= 4) {
                runCount += Integer.valueOf(summary[0]);
                assumptionFailureCount += Integer.valueOf(summary[1]);
                failureCount += Integer.valueOf(summary[2]);
                ignoreCount += Integer.valueOf(summary[3]);
            }

            String content =
                    runCount +  " " +
                    assumptionFailureCount + " " +
                    failureCount + " " +
                    ignoreCount ;

            String color1 = getColor1(assumptionFailureCount, failureCount);
            String color2 = getColor2(assumptionFailureCount, failureCount);

            logger.info(
                color1 +
                "\n########################################################\n" +
                color2 +
                "Tests run: " + runCount +
                ", Failures: " + assumptionFailureCount +
                ", Errors: " + failureCount +
                ", Skipped: " + ignoreCount +
                color1 +
                "\n########################################################\n" +
                "\033[0m");

            writeString(Path.of(home + "/test_summary.txt"), content);
        }

    }

    String getColor1(int assumptionFailureCount, int failureCount) {
        if (assumptionFailureCount > 0 || failureCount > 0) {
            return RED;
        }

        return GREEN;
    }

    String getColor2(int assumptionFailureCount, int failureCount) {
        if (assumptionFailureCount > 0 || failureCount > 0) {
            return RED_BOLD;
        }

        return GREEN_BOLD;

    }

}
