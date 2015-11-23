package org.jenkinsci.plugins.IBM_zOS_Connector;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.*;
import java.io.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <h1>zFTPConnector</h1>
 * FTP-based communication with z/OS-like systems.
 * Used for submitting jobs, fetching job log and extraction of MaxCC.
 *
 * @author <a href="mailto:candiduslynx@gmail.com">Alexander Shcherbakov</a>
 *
 * @version 1.0
 */
public class zFTPConnector
{
    // Server info.
    /**
     * LPAR name or IP to connect to.
     */
    private String server;
    /**
     * FTP port for connection
     */
    private int port;

    // Credentials.
    /**
     * UserID.
     */
    private String userID;
    /**
     * User password.
     */
    private String password;

    // Wait parameters.
    /**
     * Time to wait before giving up in milliseconds. If set to <code>0</code> will wait forever.
     */
    private long waitTime;

    // Job info from JES-like system.
    /**
     * JobID in JES.
     */
    private String jobID;
    /**
     * Jobname in JES.
     */
    private String jobName;
    /**
     * Job's MaxCC.
     */
    private String jobCC;

    // Work elements.
    /**
     * Will ask LPAR once in 10 seconds.
     */
    private static final long waitInterval = 10*1000;
    /**
     * FTPClient from <i>Apache Commons-Net</i>. Used for FTP communication.
     */
    private FTPClient FTPClient;
	/**
	 * Log prefix (default: "zFTPConnector")
	 */
	private String logPrefix;
    /**
     * Pattern for search of jobName
     */
    private static final Pattern JesJobName = Pattern.compile("250-It is known to JES as (.*)");
	/**
	 * Simple logger.
	 */
	private static final Logger logger = Logger.getLogger(zFTPConnector.class.getName());


    /**
     * Basic constructor with minimal parameters required.
     *
     * @param server LPAR name or IP address to connect to.
     * @param port LPAR password.
     * @param userID UserID.
     * @param password User password.
     * @param logPrefix Log prefix.
     */
    public zFTPConnector (String server, int port, String userID, String password, String logPrefix)
    {
        // Copy values
        this.server = server;
        this.port = port;
        this.userID = userID;
        this.password = password;

        // Create FTPClient
        this.FTPClient = new FTPClient();
        // Make password invisible from log
        this.FTPClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));

	    this.logPrefix = "";
	    if (logPrefix != null)
		    this.logPrefix = logPrefix;
	    logger.info(logPrefix + "created zFTPConnector");
    }

    /**
     * Try to connect to the <b><code>server</code></b> using the parameters passed to the constructor.
     *
     * @return Whether the connection was established using the parameters passed to the constructor.
     * @see zFTPConnector#zFTPConnector(String, int, String, String, String)
     */
    private boolean connect()
    {
        // Perform the connection.
        try
        {
            int reply; // Temp value to contain server response.

            // Try to connect.
            this.FTPClient.connect(this.server, this.port);

            // After connection attempt, check the reply code to verify success.
            reply = this.FTPClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply))
            {
                // Bad reply code.
                this.FTPClient.disconnect(); // Disconnect from LPAR.
                logger.severe(logPrefix + "FTP server refused connection."); // Print error.
                return false; // Finish with failure.
            }
	        logger.info(logPrefix + "FTP: connected to " + server + ":" + port);
        }
        // IOException handling
        catch (IOException e)
        {
            // Clos the connection if it's still opened.
            if (this.FTPClient.isConnected())
            {
                try
                {
                    this.FTPClient.disconnect();
                }
                catch (IOException f)
                {
                    // Do nothing
                }
            }
            logger.severe(logPrefix + "Could not connect to server.");
            e.printStackTrace();
            return false;
        }
        // Finall, return with success.
        return true;
    }

    /**
     * Try to logon to the <b><code>server</code></b> using the parameters passed to the constructor.
     * Also, <code>site filetype=jes jesjobname=* jesowner=*</code> command is invoked.
     *
     * @return Whether the credentials supplied are valid and the connection was established.
     *
     * @see zFTPConnector#zFTPConnector(String, int, String, String, String)
     * @see zFTPConnector#connect()
     */
    private boolean logon()
    {
        // Check whether we are already connected. If not, try to reconnect.
        if (!this.FTPClient.isConnected())
            if(!this.connect())
                return false; // Couldn't connect to the server. Can't check the credentials.

        // Perform the login process.
        try {
            int reply; // Temp value for server reply code.

            // Try to login.
            if (!this.FTPClient.login(this.userID, this.password)) {
                // If couldn't login, we should logout and return failure.
                this.FTPClient.logout();
                return false;
            }

            // Try to set filetype, jesjobname and jesstatus.
            this.FTPClient.site("filetype=jes jesjobname=* jesstatus=ALL");
            // Check reply.
            reply = this.FTPClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                this.FTPClient.disconnect();
                logger.severe(logPrefix + "FTP server refused to change FileType and JESJobName.");
                return false;
            }
        } catch (IOException e) {
            if (this.FTPClient.isConnected()) {
                try {
                    this.FTPClient.disconnect();
                } catch (IOException f) {
                    // do nothing
                }
            }
            logger.severe(logPrefix + "Could not connect to server.");
            e.printStackTrace();
            return false;
        }

        // If go here, everything went fine.
        return true;
    }

    /**
     * Submit job for execution.
     *
     * @param inputStream JCL text of the job.
     * @param wait Whether we need for the job to complete.
     * @param waitTime Maximum wait time in minutes. If set to <code>0</code>, will wait forever.
     * @param outputStream Stream to put job log. Can be <code>Null</code>.
     * @param deleteLogFromSpool Whether the job log should be deleted fro spool upon job end.
     *
     * @return Whether the job was successfully submitted and the job log was fetched.
     * <br><b><code>jobCC</code></b> holds the response of the operation (including errors).
     *
     * @see zFTPConnector#connect()
     * @see zFTPConnector#logon()
     * @see zFTPConnector#waitForCompletion(OutputStream)
     * @see zFTPConnector#deleteJobLog()
     */
    public boolean submit(InputStream inputStream, boolean wait, int waitTime, OutputStream outputStream, boolean deleteLogFromSpool)
    {
        this.waitTime = ((long)waitTime) * 60 * 1000; // Minutes to milliseconds.

	    // Clean-up
	    this.jobID = "";
	    this.jobName = "";
	    this.jobCC = "";

        // Verify connection.
        if(!this.FTPClient.isConnected())
            if(!this.logon())
            {
                this.jobCC = "COULD_NOT_CONNECT";
                return false;
            }

        this.FTPClient.enterLocalPassiveMode();

        try
        {
            // Submit the job.
            this.FTPClient.storeFile("jenkins.sub", inputStream);

            // Scan reply from server to get JobID.
            for (String s : this.FTPClient.getReplyStrings()) {
                Matcher matcher = JesJobName.matcher(s);
                if(matcher.matches())
                {
                    // Set jobID
                    this.jobID = matcher.group(1);
                    break;
                }
            }
	        logger.info(logPrefix + "submitted job [" + this.jobID + "]");
            inputStream.close();
        }
        catch (FTPConnectionClosedException e)
        {
            logger.severe(logPrefix + "Server closed connection.");
            e.printStackTrace();
            this.jobCC = "SERVER_CLOSED_CONNECTION";
            return false;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            this.jobCC = "IO_ERROR";
            return false;
        }

        if (wait)
        {
            // Wait for completion.
            if(this.waitForCompletion(outputStream))
            {
                if (deleteLogFromSpool)
                    // Delete job log from spool.
                    this.deleteJobLog();
                return true;
            }
            else
            {
                if (this.jobCC == null)
                    this.jobCC = "JOB_DID_NOT_FINISH_IN_TIME";
                return false;
            }
        }

        // If we are here, everything went fine.
        return true;
    }

    /**
     * Wait for he completion of the job.
     *
     * @param outputStream Stream to hold job log.
     *
     * @return Whether the job finished in time.
     *
     * @see zFTPConnector#submit(InputStream, boolean, int, OutputStream, boolean)
     * @see zFTPConnector#fetchJobLog(OutputStream)
     */
    private boolean waitForCompletion(OutputStream outputStream)
    {
        // Initialize current time and estimated time.
        long curr = System.currentTimeMillis();
        long jobEndTime = System.currentTimeMillis() + this.waitTime;
        boolean eternal = (waitTime == 0);

        // Perform wait
        while (eternal || (curr <= jobEndTime))
        {
            // check job state
            if (!this.checkJobAvailability())
                return false;

            // Try to fetch job log.
            if (this.fetchJobLog(outputStream))
                return true;

            // Wait
            try
            {
                Thread.sleep(waitInterval);
                curr = System.currentTimeMillis();
            } catch (InterruptedException e) {
                logger.severe(logPrefix + "Interrupted.");
                this.jobCC = "WAIT_INTERRUPTED";
                return false;
            }
        }

        // Exit with wait error.
        this.jobCC = "WAIT_ERROR";
        return false;
    }

    /**
     * @return true if job can be listed through FTP.
     */
    private boolean checkJobAvailability ()
    {
        // Verify connection.
        if(!this.FTPClient.isConnected())
            if(!this.logon())
            {
                this.jobCC = "FETCH_LOG_ERROR_LOGIN";
                return false;
            }

        this.FTPClient.enterLocalPassiveMode();

        // Try listing files
        try
        {
            for (String name : this.FTPClient.listNames("*"))
                if(this.jobID.equals(name)) {
                    // Found our jobId
                    return true;
                }
            logger.severe("Job [" + this.jobID + "] cannot be found in JES");
            this.jobCC = "JOB_NOT_FOUND_IN_JES";
            return false;
        }
        catch (IOException e)
        {
            this.jobCC = "FETCH_LOG_IO_ERROR";
            return false;
        }
    }
    /**
     * Fetch job log from spool.
     *
     * @param outputStream Stream to hold the job log.
     *
     * @return Whether the job log was fetched from the LPAR.
     *
     * @see zFTPConnector#waitForCompletion(OutputStream)
     */
    private boolean fetchJobLog(OutputStream outputStream)
    {
        // Verify connection.
        if(!this.FTPClient.isConnected())
            if(!this.logon())
            {
                this.jobCC = "FETCH_LOG_ERROR_LOGIN";
                return false;
            }

        this.FTPClient.enterLocalPassiveMode();

        // Try fetching.
        try
        {
            // Try fetching the log.
            if(!this.FTPClient.retrieveFile(this.jobID,outputStream))
            {
                this.jobCC = "RETR_ERR_JOB_NOT_FINISHED_OR_NOT_FOUND";
                return false;
            }

            return this.obtainJobRC();
        }
        catch (IOException e)
        {
            this.jobCC = "FETCH_LOG_IO_ERROR";
            return false;
        }
    }

    /**
     * @return Whether job RC was correctly obtained or not.
     */
    private boolean obtainJobRC()
    {

        this.jobCC =  "COULD_NOT_RETRIEVE_JOB_RC";
        // Verify connection.
        if(!this.FTPClient.isConnected())
            if(!this.logon())
            {
                return false;
            }

        this.FTPClient.enterLocalPassiveMode();

        Pattern CC = Pattern.compile("(\\S+)\\s+"+jobID+".* RC=(.*?) .*");
        Pattern CCUndefined = Pattern.compile("(\\S+)\\s+"+jobID+".* RC\\s+(\\S+)\\s+.*");
        Pattern ABEND = Pattern.compile("(\\S+)\\s+"+jobID+".* ABEND=(.*?)\\s+.*");
        Pattern JCLERROR = Pattern.compile("(\\S+)\\s+"+jobID+".* \\(JCL error\\)\\s+.*");

        // Check RC.
        try
        {
            for (FTPFile ftpFile : this.FTPClient.listFiles("*")) {
                String fileName = ftpFile.toString();

                Matcher CCMatcher = CC.matcher(fileName);
                Matcher CCUndefinedMatcher = CCUndefined.matcher(fileName);
                Matcher ABENDMatcher = ABEND.matcher(fileName);
                Matcher JCLERRORMatcher = JCLERROR.matcher(fileName);

                if (JCLERRORMatcher.matches()) {
	                this.jobName = JCLERRORMatcher.group(1);
                    this.jobCC = "JCL_ERROR";
                    return true;
                } else {
                    if (ABENDMatcher.matches()) {
	                    this.jobName = ABENDMatcher.group(1);
                        this.jobCC = "ABEND_"+ABENDMatcher.group(2);
                        return true;
                    } else {
                        if (CCUndefinedMatcher.matches()) {
	                        this.jobName = CCUndefinedMatcher.group(1);
                            this.jobCC = CCUndefinedMatcher.group(2).toUpperCase();
                            return true;
                        } else {
                            if (CCMatcher.matches()) {
	                            this.jobName = CCMatcher.group(1);
                                this.jobCC = CCMatcher.group(2);
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
        catch (IOException e)
        {
            // Do nothing.
        }
        return false;
    }

    /**
     * Delete job log from spool. Job is distinguished by previously obtained <code>jobID</code>.
     *
     * @see zFTPConnector#submit(InputStream, boolean, int, OutputStream, boolean)
     */
    private void deleteJobLog ()
    {
        // Verify connection.
        if(!this.FTPClient.isConnected())
            if(!this.logon())
            {
                return;
            }

        this.FTPClient.enterLocalPassiveMode();

        // Delete log.
        try
        {
            this.FTPClient.deleteFile(this.jobID);
        }
        catch (IOException e)
        {
            // Do nothing.
        }
    }

    /**
     * Get JobID.
     *
     * @return Current <b><code>jobID</code></b>.
     */
    public String getJobID() {
        return this.jobID;
    }
	/**
	 * Get Jobname.
	 *
	 * @return Current <b><code>jobName</code></b>.
	 */
	public String getJobName() {
		return this.jobName;
	}
    /**
     * Get JobCC.
     *
     * @return Current <b><code>jobCC</code></b>.
     */
    public String getJobCC() {
        return this.jobCC;
    }
}

