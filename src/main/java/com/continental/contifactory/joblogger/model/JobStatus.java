package com.continental.contifactory.joblogger.model;

import java.util.Date;
import java.util.List;

/**
 * This class holds various information about a job in Jenkins.
 */
public class JobStatus {
    private String jobName;
    private int jobNumber;
    private Date startDate;
    private Date endDate;
    private String buildResult;
    private String buildCause;
    private List<String> nodeNames;
    private String consoleUrl;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public int getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(int jobNumber) {
        this.jobNumber = jobNumber;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getBuildResult() {
        return buildResult;
    }

    public void setBuildResult(String buildResult) {
        this.buildResult = buildResult;
    }

    public String getBuildCause() {
        return buildCause;
    }

    public void setBuildCause(String buildCause) {
        this.buildCause = buildCause;
    }

    public List<String> getNodeNames() {
        return nodeNames;
    }

    public void setNodeNames(List<String> nodeName) {
        this.nodeNames = nodeName;
    }

    public String getConsoleUrl() {
        return consoleUrl;
    }

    public void setConsoleUrl(String consoleUrl) {
        this.consoleUrl = consoleUrl;
    }

    @Override
    public String toString() {
        StringBuilder msg = new StringBuilder();

        msg.append("[");
        msg.append(jobName + "#" + jobNumber + ", ");
        msg.append("result: " + buildResult + ", ");
        msg.append("start date: " + startDate + ", ");
        msg.append("end date: " + endDate + ", ");
        msg.append("cause: " + buildCause + ", ");
        msg.append("node(s): " + nodeNames + ", ");
        msg.append("console URL: " + consoleUrl);
        msg.append("]");

        return msg.toString();
    }
}
