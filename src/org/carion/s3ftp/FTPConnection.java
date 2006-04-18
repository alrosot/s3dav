package org.carion.s3ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import org.carion.s3.S3Log;
import org.carion.s3dav.Version;

/**
 * Interesting reading about PASSIVE vs ACTIVE ftp :
 * http://slacksite.com/other/ftp.html
 * 
 * @author pcarion
 * 
 */
public class FTPConnection implements Runnable {
	private final S3Log _log;

	private final FTPServer _ftpServer;

	private final Socket _socket;

	private boolean running = false;

	private boolean inBinaryMode = false;

	private boolean debugMode = true;

	private BufferedReader inflow;

	private PrintWriter outflow;

	private String renameFrom;

	private Socket clientSocket;

	private String clientAddress;

	private int clientPort;

	private String currentDirectory;

	private FTPObject currentObject;

	private FTPObject rootObject;

	private int resumePosition;

	private boolean _isLoggedIn;

	public FTPConnection(FTPServer ftpServer, Socket socket,
			FTPObject startingObject, FTPObject root, S3Log log) {
		_log = log;
		_isLoggedIn = false;
		_ftpServer = ftpServer;

		_socket = socket;
		clientPort = 0;

		currentObject = startingObject;
		rootObject = root;

		currentDirectory = currentObject.path();

		resumePosition = 0;
	}

	public void start() {
		running = true;
		try {
			outflow = new PrintWriter(new OutputStreamWriter(_socket
					.getOutputStream()), true);
			inflow = new BufferedReader(new InputStreamReader(_socket
					.getInputStream()));
		} catch (Exception e) {
			_log.log("Error creating flows", e);
			return;
		}
		new Thread(this).start();
	}

	public void stop() {
		running = false;
		_ftpServer.signalConnectionTerminated(this);
	}

	public void run() {
		String incomingString = null;

		output("220 s3DAV/FTP Server " + Version.VERSION);

		while (running) {
			try {
				incomingString = inflow.readLine();
				if (incomingString == null) {
					throw new IOException(
							"Client inflow interrupted during standard processing");
				} else {
					parse(incomingString);
				}
			} catch (Exception e) {
				_log.log("Error reading command", e);
				stop();
			}
		}
		_log.log("Client terminated connection normally");
		stop();
	}

	public void output(String out) {
		try {
			outflow.println(out);
		} catch (Exception e) {
			_log.log("Error sending response", e);
		}
	}

	public void endSession() {
		try {
			clientSocket.close();
		} catch (Exception e) {
		}
		try {
			inflow.close();
		} catch (Exception e) {
		}
		try {
			outflow.close();
		} catch (Exception e) {
		}
		try {
			_socket.close();
		} catch (Exception e) {
		}

		stop();
	}

	public Socket getDataConnection() {
		if (clientPort > 0) {
			try {
				clientSocket = new Socket(clientAddress, clientPort);
			} catch (Exception e) {
				return null;
			}
			return clientSocket;
		}
		return null;
	}

	public FTPObject getCurrentObject() {
		return currentObject;
	}

	public void rebuildCurrentChildList() {
		currentObject.buildChildList();
	}

	public void parse(String command) {
		StringTokenizer st = new StringTokenizer(command);
		String ftpCommand = null;

		ftpCommand = st.nextToken();

		_log.log("parse> " + command);

		if (ftpCommand.equalsIgnoreCase("USER")) {
			try {
				user(st.nextToken());
			} catch (NoSuchElementException nse) {
			}
			return;
		}

		if (ftpCommand.equalsIgnoreCase("PASS")) {
			try {
				pass(st.nextToken());
			} catch (NoSuchElementException nse) {
			}
			return;
		}

		if (!_isLoggedIn) {
			output("530 Login incorrect");
			return;
		}

		if (ftpCommand.equalsIgnoreCase("PWD")) {
			pwd();
			return;
		}

		if (ftpCommand.equalsIgnoreCase("SYST")) {
			syst();
			return;
		}

		if (ftpCommand.equalsIgnoreCase("PASV")) {
			pasv();
			return;
		}

		if (ftpCommand.equalsIgnoreCase("QUIT")) {
			quit();
			return;
		}

		if (ftpCommand.equalsIgnoreCase("LIST")) {
			list(false);
			return;
		}

		if (ftpCommand.equalsIgnoreCase("NLST")) {
			list(true);
			return;
		}

		if (ftpCommand.equalsIgnoreCase("CDUP")) {
			cdup();
			return;
		}

		if (ftpCommand.equalsIgnoreCase("CWD")) {
			try {
				cwd(allRemainingTokens(st).trim());
			} catch (NoSuchElementException nse) {
			}
			return;
		}

		if (ftpCommand.equalsIgnoreCase("RETR")) {
			try {
				retr(allRemainingTokens(st).trim());
			} catch (NoSuchElementException nse) {
			}
			return;
		}

		if (ftpCommand.equalsIgnoreCase("TYPE")) {
			try {
				type(allRemainingTokens(st).trim());
			} catch (NoSuchElementException nse) {
			}
			return;
		}

		if (ftpCommand.equalsIgnoreCase("STRU")) {
			try {
				stru(allRemainingTokens(st).trim());
			} catch (NoSuchElementException nse) {
			}
			return;
		}

		if (ftpCommand.equalsIgnoreCase("MODE")) {
			try {
				mode(allRemainingTokens(st).trim());
			} catch (NoSuchElementException nse) {
			}
			return;
		}

		if (ftpCommand.equalsIgnoreCase("STOR")) {
			try {
				stor(allRemainingTokens(st).trim(), false);
			} catch (NoSuchElementException nse) {
			}
			return;
		}

		if (ftpCommand.equalsIgnoreCase("APPE")) {
			try {
				stor(allRemainingTokens(st).trim(), true);
			} catch (NoSuchElementException nse) {
			}
			return;
		}

		if (ftpCommand.equalsIgnoreCase("PORT")) {
			try {
				port(allRemainingTokens(st).trim());
			} catch (NoSuchElementException nse) {
			}
			return;
		}

		if (ftpCommand.equalsIgnoreCase("DELE")) {
			try {
				dele(allRemainingTokens(st).trim());
			} catch (NoSuchElementException nse) {
			}
			return;
		}

		if (ftpCommand.equalsIgnoreCase("NOOP")) {
			noop();
			return;
		}

		if (ftpCommand.equalsIgnoreCase("REST")) {
			try {
				rest(allRemainingTokens(st).trim());
			} catch (NoSuchElementException nse) {
			}
			return;
		}

		if (ftpCommand.equalsIgnoreCase("RNFR")) {
			try {
				rnfr(allRemainingTokens(st).trim());
			} catch (NoSuchElementException nse) {
			}
			return;
		}

		if (ftpCommand.equalsIgnoreCase("RNTO")) {
			try {
				rnto(allRemainingTokens(st).trim());
			} catch (NoSuchElementException nse) {
			}
			return;
		}

		if (ftpCommand.equalsIgnoreCase("MKD")) {
			try {
				mkd(allRemainingTokens(st).trim());
			} catch (NoSuchElementException nse) {
			}
			return;
		}

		if (ftpCommand.equalsIgnoreCase("RMD")) {
			try {
				rmd(allRemainingTokens(st).trim());
			} catch (NoSuchElementException nse) {
			}
			return;
		}
		output("502 " + ftpCommand + " command not supported");
	}

	/**
	 * Creates a single string that contains all the tokens that have not yet
	 * been parsed out of a given StringTokenizer.
	 */
	private String allRemainingTokens(StringTokenizer st) {
		String r = new String();

		while (st.hasMoreTokens()) {
			r = r + st.nextToken() + " ";
		}

		return r;
	}

	public void user(String userName) {
		if (_ftpServer.isValidUser(userName)) {
			output("331 Password required for " + userName);
		} else {
			output("530 Login incorrect");
		}
	}

	public void pass(String password) {
		_isLoggedIn = _ftpServer.isValidPassword(password);
		if (_isLoggedIn) {
			output("230 user logged in");
		} else {
			output("530 Login incorrect");
		}
	}

	public void pwd() {
		output("257 \"" + currentObject.completeObjectPath()
				+ "\" is current directory");
	}

	public void syst() {
		output("215 s3DAV");
	}

	public void pasv() {
		output("502 PASV command not supported");
	}

	public void quit() {
		output("221 Goodbye!");
		endSession();
	}

	public void cwd(String commandArgs) {
		System.out.println("Directory request: " + commandArgs);

		if (setDirectory(commandArgs)) {
			output("250 CWD command successfull");
		} else {
			output("250 CWD command successfull");
		}
	}

	private boolean setDirectory(String newDir) {
		FTPObject attempt = currentObject.childAsDirectory(newDir);

		System.out.println("setDirectory on " + this);

		if (attempt != null) {
			System.out.println("Relative change directory attempt succeeded");
			currentObject = attempt;
			currentDirectory = currentObject.path();
			return true;
		} else {
			System.out.println("Relative change directory attempt failed");
			attempt = rootObject.nestedDirectory(newDir);
			if (attempt != null) {
				System.out.println("Nested change directory attempt succeeded");
				currentObject = attempt;
				currentDirectory = currentObject.path();
				return true;
			} else {
				System.out.println("Nested change directory attempt failed");
				return false;
			}
		}
	}

	public void cdup() {
		currentObject = currentObject.parentAsDirectory();
		currentDirectory = currentObject.path();
		output("250 CDUP command successfull");
	}

	/**
	 * This outputs a list of filenames to the data connection. It will send
	 * details if the shortForm parameter is false, otherwise it sends just a
	 * stripped down list of file names.
	 */
	public void list(boolean shortForm) {
		FTPObject thisChild;
		Vector children;
		int f;

		Socket listSocket = getDataConnection();
		output("150 Opening ASCII mode data connection for LIST command");

		try {
			OutputStream out = listSocket.getOutputStream();
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));

			children = getCurrentObject().getChildren();

			for (f = 0; f < children.size(); f++) {
				thisChild = (FTPObject) children.elementAt(f);
				if (shortForm) {
					if (debugMode) {
						System.out.println("[L] > " + thisChild.toString());
					}
					writer.println(thisChild.toString());
				} else {
					if (debugMode) {
						System.out.println("[L] > "
								+ formatObjectList(thisChild));
					}
					writer.println(formatObjectList(thisChild));
				}
			}

			writer.close();
			listSocket.close();
		} catch (Exception e) {
			System.out.println("[P] reply> Error LISTing");
			System.out.println(e);
			return;
		}

		output("226 ASCII transfer complete");
	}

	/**
	 * Call the list method with a default of 'false' for the shortForm
	 * parameter, causing full details of the files to be returned.
	 */
	public void list() {
		list(false);
	}

	/**
	 * Put information about a particular object in a UNIX-ish listing suitable
	 * for return by the LIST command.
	 */
	private String formatObjectList(FTPObject obj) {
		String fs = new String();

		if (obj.isDirectory()) {
			fs = "dr-xr-xr-x 1 owner group 0";
		} else {
			fs = "-r-xr-xr-x 1 owner group " + obj.size();
		}

		fs = fs + " " + obj.getFormattedDate();
		fs = fs + " " + obj.toString();

		return fs;
	}

	public void retr(String commandArgs) {
		if (inBinaryMode) {
			retrI(commandArgs);
		} else {
			retrA(commandArgs);
		}
	}

	public void type(String commandArgs) {
		if (commandArgs.equalsIgnoreCase("I")) {
			inBinaryMode = true;
			output("200 Type set to I");
		} else {
			inBinaryMode = false;
			output("200 Type set to A");
		}
	}

	public void stru(String commandArgs) {
		if (commandArgs.equalsIgnoreCase("F")) {
			output("200 Structure set to F");
		} else if (commandArgs.equalsIgnoreCase("R")) {
			output("504 Structure cannot be set to R");
		} else if (commandArgs.equalsIgnoreCase("P")) {
			output("504 Structure cannot be set to P");
		} else {
			output("504 Structure cannot be set to " + commandArgs);
		}
	}

	public void mode(String commandArgs) {
		if (commandArgs.equalsIgnoreCase("S")) {
			output("200 Mode set to S (Stream)");
		} else if (commandArgs.equalsIgnoreCase("B")) {
			output("504 Mode cannot be set to B (Block)");
		} else if (commandArgs.equalsIgnoreCase("C")) {
			output("504 Mode cannot be set to C (Compressed)");
		} else {
			output("504 Mode cannot be set to " + commandArgs);
		}
	}

	public void noop() {
		output("200 NOOP succeeded");
	}

	/**
	 * This will STORe a file on the server. If the append argument is true this
	 * method will APPEnd to the existing file.
	 */
	public void stor(String commandArgs, boolean append) {
		if (inBinaryMode) {
			storI(commandArgs, append);
		} else {
			storA(commandArgs, append);
		}
	}

	public void storI(String commandArgs, boolean append) {
		BufferedInputStream incomingData = null;
		BufferedOutputStream diskFile = null;
		int byt = -1;

		File saveFile = new File(currentObject.physicalObjectPath()
				+ System.getProperty("file.separator") + commandArgs);
		System.out
				.println("Attempt to retrieve for STOR the file (in binary): "
						+ saveFile.toString());

		Socket listSocket = getDataConnection();
		output("150 Opening BINARY mode data connection to receive "
				+ commandArgs);

		try {
			incomingData = new BufferedInputStream(listSocket.getInputStream());
			diskFile = new BufferedOutputStream(new FileOutputStream(saveFile,
					append));

			byt = incomingData.read();
			while (byt >= 0) {
				if (byt >= 0) {
					diskFile.write(byt);
				}
				byt = incomingData.read();
			}

			diskFile.flush();

			diskFile.close();
			incomingData.close();

			rebuildCurrentChildList();
		} catch (Exception e) {
			System.out
					.println("Error receiving file in BINARY format for STOR");
			System.out.println(e);
			return;
		}

		output("226 BINARY transfer complete");
	}

	public void storA(String commandArgs, boolean append) {
		BufferedReader incomingData = null;
		PrintWriter diskFile = null;
		String line = null;

		File saveFile = new File(currentObject.physicalObjectPath()
				+ System.getProperty("file.separator") + commandArgs);
		System.out.println("Attempt to retrieve for STOR the file: "
				+ saveFile.toString());

		Socket listSocket = getDataConnection();
		output("150 Opening ASCII mode data connection to receive "
				+ commandArgs);

		try {
			incomingData = new BufferedReader(new InputStreamReader(listSocket
					.getInputStream()));
			diskFile = new PrintWriter(new FileOutputStream(saveFile, append));

			line = incomingData.readLine();
			while (line != null) {
				if (line != null) {
					diskFile.println(line);
				}
				line = incomingData.readLine();
			}

			diskFile.flush();

			diskFile.close();
			incomingData.close();

			rebuildCurrentChildList();
		} catch (Exception e) {
			System.out.println("Error receiving file in ASCII format for STOR");
			System.out.println(e);
			return;
		}

		output("226 ASCII transfer complete");
	}

	private void retrA(String commandArgs) {
		PrintWriter writer = null;
		BufferedReader dataStream = null;
		String line = null;

		dataStream = new BufferedReader(getCurrentObject().getReaderFromObject(
				commandArgs));

		if (dataStream == null) {
			System.out
					.println("Error sending file in ASCII format for RETR, file not found, or object not in child list");
			return;
		}

		Socket listSocket = getDataConnection();
		output("150 Opening ASCII mode data connection for " + commandArgs);

		try {
			if (resumePosition > 0) {
				dataStream.skip(resumePosition);
			}
			writer = new PrintWriter(listSocket.getOutputStream());

			line = dataStream.readLine();
			while (line != null) {
				if (line != null) {
					writer.println(line);
					writer.flush();
				}
				line = dataStream.readLine();
			}

			writer.close();
			listSocket.close();
			dataStream.close();
		} catch (Exception e) {
			System.out.println("Error sending file in ASCII format for RETR");
			System.out.println(e);
			return;
		}
		output("226 ASCII transfer complete");
	}

	private void retrI(String commandArgs) {
		BufferedOutputStream writer = null;
		BufferedInputStream dataStream = null;
		int byt = -1;

		dataStream = new BufferedInputStream(getCurrentObject()
				.getInputFromObject(commandArgs));

		if (dataStream == null) {
			System.out
					.println("Error sending file in BINARY format for RETR, file not found, or object not in child list");
			return;
		}

		Socket listSocket = getDataConnection();
		output("150 Opening BINARY mode data connection for " + commandArgs);

		try {
			if (resumePosition > 0) {
				dataStream.skip(resumePosition);
			}
			writer = new BufferedOutputStream(listSocket.getOutputStream(), 1);

			byt = dataStream.read();
			while (byt >= 0) {
				if (byt >= 0) {
					writer.write(byt);
					writer.flush();
				}
				byt = dataStream.read();
			}

			writer.close();
			listSocket.close();
			dataStream.close();
		} catch (Exception e) {
			System.out.println("Error sending file in BINARY format for RETR");
			System.out.println(e);
			return;
		}

		output("226 BINARY transfer complete");
	}

	public void port(String commandArgs) {
		StringTokenizer st = new StringTokenizer(commandArgs, ",");
		int portHi, portLo;

		clientAddress = new String(st.nextToken() + "." + st.nextToken() + "."
				+ st.nextToken() + "." + st.nextToken());

		try {
			portHi = new Integer(st.nextToken()).intValue();
			portLo = new Integer(st.nextToken()).intValue();
			clientPort = (portHi << 8) | portLo;
		} catch (Exception e) {
			_log.log("PORT command failed, "
					+ "could not interperate port value", e);
		}
		output("200 PORT command succeded");
	}

	/**
	 * This method deletes the file named by the commandArgs string. The file
	 * must be in the current directory (i.e. a child of the current object).
	 */
	public void dele(String commandArgs) {
		getCurrentObject().delete(commandArgs);
		output("250 DELE command succeded, " + commandArgs + "deleted.");
	}

	/**
	 * This sets the offset from the beginning of the file at which transfers
	 * will begin.
	 */
	public void rest(String commandArgs) {
		int position;

		try {
			position = Integer.parseInt(commandArgs);
		} catch (Exception e) {

			output("504 Resume position cannot be set to " + commandArgs);
			return;
		}

		/**
		 * Sets the position in a file at which transfers begin.
		 */
		resumePosition = position;
		output("350 REST command succeded, transfers will begin at " + position);
	}

	/**
	 * Set the filename from which we will be renaming. This is the first half
	 * of the complete rename command.
	 */
	public void rnfr(String commandArgs) {
		if (getCurrentObject().childExists(commandArgs)) {
			/**
			 * Set the source name for a rename command
			 */
			renameFrom = commandArgs;
			output("350 RNFR command succeded, send RNTO command to proceed");
		} else {
			output("550 File specified by RNFR does not exist");
		}
	}

	/**
	 * Set the new name of the file we are about to rename, then actually rename
	 * it. This is the second half of the rename command.
	 */
	public void rnto(String commandArgs) {
		currentObject.renameChild(renameFrom, commandArgs);
		renameFrom = "";
		output("250 RNTO command succeded");
	}

	/**
	 * Create a directory below the currently active directory.
	 */
	public void mkd(String commandArgs) {
		getCurrentObject().makeDirectory(commandArgs);
		output("257 Directory created");
	}

	/**
	 * Delete a directory below the currently active directory. This method will
	 * trim the path to its last element before passing it to the delete method
	 * of the object.
	 */
	public void rmd(String commandArgs) {
		String fixedName = commandArgs;

		if (commandArgs.indexOf('\\') > 0) {
			fixedName = commandArgs
					.substring(commandArgs.lastIndexOf('\\') + 1);
		} else if (commandArgs.indexOf('/') > 0) {
			fixedName = commandArgs.substring(commandArgs.lastIndexOf('/') + 1);
		}

		getCurrentObject().delete(fixedName);
		output("250 Directory '" + fixedName + "' removed");
	}

	/**
	 * Returns a usefull string representation of this FTPConnection object.
	 */
	public String toString() {
		return "Connection in " + currentObject.toString();
	}
}
