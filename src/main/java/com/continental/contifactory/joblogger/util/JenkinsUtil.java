package com.continental.contifactory.joblogger.util;

import hudson.FilePath;
import hudson.console.ConsoleNote;
import hudson.model.Run;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class to provide various Jenkins information.
 */
public final class JenkinsUtil {
    private static final Logger LOGGER = Logger.getLogger(JenkinsUtil.class.getName());

    private JenkinsUtil() {
        // avoid instantiation
    }

    /**
     * @return the Jenkins Home directory
     * @throws FileNotFoundException if the Jenkins instance cannot be found, or if the Jenkins root path is null
     */
    public static String getJenkinsHomeDirectory() throws FileNotFoundException {
        String jenkinsHome = "";

        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            String msg = "Could not find JENKINS singleton instance!";
            throw new FileNotFoundException(msg);
        } else {
            FilePath rootPath = jenkins.getRootPath();

            if (rootPath == null) {
                String msg = "Could not find JENKINS root path on this node!";
                throw new FileNotFoundException(msg);
            } else {
                jenkinsHome = rootPath.getRemote();
            }
        }

        if (jenkinsHome == null || jenkinsHome.isEmpty()) {
            String msg = "Could not find JENKINS_HOME directory!";
            throw new FileNotFoundException(msg);
        }

        return jenkinsHome;
    }

    /**
     * @return the user content directoy of Jenkins
     * @throws FileNotFoundException if {@link #getJenkinsHomeDirectory()} throws a FileNotFoundException
     */
    public static String getUserContentDirectory() throws FileNotFoundException {
        return getJenkinsHomeDirectory() + File.separator + "userContent";
    }

    /**
     * @param run an instance of Jenkins {@link Run}
     * @return the console URL of the given run
     */
    public static String getJobConsoleUrl(@Nonnull Run<?, ?> run) {
        String jobConsoleUrl = "";

        if (run == null) {
            throw new NullPointerException();
        }

        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            String msg = "Could not find JENKINS singleton instance!";
            LOGGER.severe(msg);
            return jobConsoleUrl;
        }

        if (jenkins.getRootUrl() == null) {
            String msg = "Could not find JENKINS root URL!";
            LOGGER.severe(msg);
            return jobConsoleUrl;
        }

        return jenkins.getRootUrl() + run.getUrl() + "/console";
    }

    /**
     * Extract node names from the given run, by searching for the regex {@code "Running on [node name] in"}
     * in the console log of the run.
     *
     * @param run an instance of Jenkins {@link Run}
     * @return the node names used by the given run; or an empty list if nothing is found
     */
    public static List<String> getNodeNames(@Nonnull Run<?, ?> run) {
        List<String> nodeNames = new ArrayList<>();

        final String nodeRegex = "(Running on )(.+)( in .+)";
        final int nodeNameIndex = 2;
        Pattern pattern = Pattern.compile(nodeRegex);

        try (BufferedReader reader = new BufferedReader(run.getLogReader())) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                // remove HA (High Availability) protocol notes from the line
                String cleanedLine = ConsoleNote.removeNotes(line);
                Matcher matcher = pattern.matcher(cleanedLine);
                while (matcher.find()) {
                    nodeNames.add(matcher.group(nodeNameIndex));
                }
            }
        } catch (IOException e) {
            LOGGER.warning("Could not retrieve node name. "
                    + "IOException occurred while reading log file for build \"" + run.getParent().getName() + "\": "
                    + e.getMessage());
        }

        return nodeNames;
    }

    /**
     * @param run an instance of Jenkins {@link Run}
     * @return the first cause which started the run; or an empry string if nothing is found
     */
    public static String getFirstCause(@Nonnull Run<?, ?> run) {
        String cause = "";
        if (!run.getCauses().isEmpty()) {
            cause = run.getCauses().get(0).getShortDescription();
        }
        return cause;
    }

    /**
     * @param run an instance of Jenkins {@link Run}
     * @return the run's build name (= Jenkins job name)
     */
    public static String getBuildName(@Nonnull Run<?, ?> run) {
        String buildName = "";
        if (run != null && run.getParent() != null) {
            buildName = run.getParent().getName();
        }
        return buildName;
    }
}
