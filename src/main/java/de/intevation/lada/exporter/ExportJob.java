/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.exporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.apache.log4j.Logger;

import de.intevation.lada.util.auth.UserInfo;

/**
 * Abstract class for an export job.
 */
public abstract class ExportJob extends Thread{

    /**
     * Result encoding
     */
    protected String encoding;

    /**
     * Parameters used for the export
     */
    protected JsonObject exportParameters;

    /**
     * The export format
     */
    protected String format;

    /**
     * Logger instance
     */
    @Inject
    protected Logger logger;

    /**
     * Message String, used in case of an error
     */
    protected String message;

    /**
     * Filename set by the users request
     */
    protected String downloadFileName;

    /**
     * Temporary output file's name
     */
    protected String outputFileName;

    /**
     * Output file's location
     */
    protected String outputFileLocation;

    /**
     * Complete path to the output file
     */
    protected Path outputFilePath;

    /**
     * Id of this export job
     */
    protected String jobId;

    /**
     * UserInfo
     */
    protected UserInfo userInfo;

    /**
     * Possible status values for export jobs
     */
    enum status {waiting, running, finished, error}

    /**
     * The current job status
     */
    protected status currentStatus;

    /**
     * Create a new job with the given id
     * @param jobId Job identifier
     */
    public ExportJob (String jobId) {
        this.jobId = jobId;
        this.currentStatus = status.waiting;
        this.outputFileLocation = "/tmp/lada-server/";
        if (!outputFileLocation.endsWith("/")) {
            outputFileLocation += "/";
        }
        this.outputFileName = jobId;
        this.outputFilePath = Paths.get(outputFileLocation + outputFileName);

        this.message = "";
    }

    /**
     * Clean up after the export has finished.
     * 
     * Removes the result file
     * @throws JobNotFinishedException Thrown if job is still running
     */
    public void cleanup() throws JobNotFinishedException {
        if (currentStatus != status.finished && currentStatus != status.error) {
            throw new JobNotFinishedException();
        }
        removeResultFile();
    }

    /**
     * Set this job to failed state
     * @param message Optional message
     */
    protected void fail(String message) {
        this.currentStatus = status.error;
        this.message = message != null ? message: "";
    }

    /**
     * Get the filename used for downloading
     * @return Filename as String
     */
    public String getDownloadFileName() {
        return downloadFileName;
    }

    /**
     * Get the encoding
     * @return Encoding as String
     */
    public String getEncoding() {
        return this.encoding;
    }

    /**
     * Get the export format as String.
     * @return Export format as String
     */
    public String getFormat() {
        return format;
    }

    /**
     * Return the job identifier.
     * @return Identifier as String
     */
    public String getJobId() {
        return jobId;
    }

    /**
     * Return the message String.
     * @return message as String
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the output file name.
     * @return Output file name String
     */
    public String getOutputFileName() {
        return outputFileName;
    }

    /**
     * Get the export file's path
     * @return File path
     */
    public Path getOutputFilePath() {
        return outputFilePath;
    }

    /**
     * Return the current job status.
     * @return Job status
     */
    public status getStatus() {
        return currentStatus;
    }
    /**
     * Return the current status as String.
     * @return Status as String
     */
    public String getStatusName() {
        return currentStatus.name();
    }

    /**
     * Run the ExportJob.
     * Should be overwritten in child classes.
     */
    public void run() {
        currentStatus = status.running;
    }

    /**
     * Set the filename used for downloading the result file
     * @param downloadFileName File name
     */
    public void setDownloadFileName(String downloadFileName) {
        this.downloadFileName = downloadFileName;
    }

    /**
     * Set the export encoding
     * @param encoding Encoding as String
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Set parameters used for the export
     * @param exportParameters Parameters as JsonObject
     */
    public void setExportParameter(JsonObject exportParameters) {
        this.exportParameters = exportParameters;
    }

    /**
     * Set user info
     * @param userInfo New userInfo
     */
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    /**
     * Remove the export's result file if present
     */
    protected void removeResultFile() {
        try {
            Files.delete(outputFilePath);
        } catch (NoSuchFileException nsfe) {
            logger.warn(String.format("Jobid %s: Can not remove result file: File not found", jobId));
        } catch (IOException ioe) {
            logger.error(String.format("Jobid %s: Cannot delete result file. IOException: %s", jobId, ioe.getStackTrace()));
        }
    }

    /**
     * Write the export result to a file
     * @param result Result string to export
     * @return True if written successfully, else false
     */
    protected boolean writeResultToFile(String result) {
        Path tmpPath = Paths.get(outputFileLocation);
        logger.debug(String.format("Jobid %s: Writing result to file %s", jobId, outputFilePath));

        //Create dir
        if (!Files.exists(tmpPath)) {
            try {
                Files.createDirectories(tmpPath);
            } catch (IOException ioe) {
                logger.error(String.format("Jobid %s: Cannot create export folder. IOException: %s", jobId, ioe.getStackTrace()));
                return false;
            } catch (SecurityException se) {
                logger.error(String.format("Jobid %s: Security Exception during directory creation %s", jobId, se.getStackTrace()));
                return false;
            }
        }

        //Create file
        try {
            Files.createFile(outputFilePath);
        } catch (FileAlreadyExistsException faee) {
            logger.error(String.format("Jobid %s: Cannot create export file. File already exists", jobId));
            return false;
        } catch (IOException ioe) {
            logger.error(String.format("Jobid %s: Cannot create export file. IOException: %s", jobId, ioe.getStackTrace()));
            return false;
        } catch (SecurityException se) {
            logger.error(String.format("Jobid %s: Security Exception during file creation %s", jobId, se.getStackTrace()));
            return false;
        }

        //Write to file
        try (BufferedWriter writer = Files.newBufferedWriter(outputFilePath, StandardOpenOption.WRITE)) {
            writer.write(result);
        } catch (IOException ioe) {
            logger.error(String.format("Jobid %s: Cannot write to export file. IOException: %s", jobId, ioe.getStackTrace()));
            return false;
        }

        return true;
    }

    /**
     * Exception thrown if an unfished ExportJob is about to be removed while still runnning
     */
    public static class JobNotFinishedException extends Exception {
        private static final long serialVersionUID = 1L;
    }
}