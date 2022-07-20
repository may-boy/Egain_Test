package com.egain.platform.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Enumeration;

import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;

import com.egain.platform.common.CallerContext;
import com.egain.platform.util.logging.Level;
import com.egain.platform.util.logging.LogSource;
import com.egain.platform.util.logging.Logger;

public class JDKTrustStoreUtil {

	private static final String classname = JDKTrustStoreUtil.class.getCanonicalName();
	private static final String filename = JDKTrustStoreUtil.class.getSimpleName() + ".java";
	private static final CallerContext callerContext = CallerContext.STATIC_INIT_CONTEXT;
	private static final String FS = File.separator;

	public static int validateArgs(String args[], String tsPath, KeyStore ts) throws Exception {
		Logger mLogger = Logger.getLogger("com.egain.installer");
		LogSource logSource = LogSource.getObject(classname, "validateArgs()", filename);
		mLogger.log(Level.DEBUG, callerContext, logSource, "In validateArgs()");
		int count = args.length;
		if ((count <= 1) || (count > 3)) {
			mLogger.log(Level.ERROR, callerContext, logSource,
					"Please enter the valid argument for the operation to be performed. Number of arguments passed : "
							+ count);
			return -1;
		}

		// List Operation
		if (args[0].equalsIgnoreCase("List")) {
			mLogger.log(Level.DEBUG, callerContext, logSource, "List operation started");
			if (args[1].equalsIgnoreCase("ALL")) {
				System.out.println("Reached Here!");
				String listCert = args[1];
				mLogger.log(Level.DEBUG, callerContext, logSource, "All certificates listed below : ");
				listCertificates(listCert, ts);
			} else if (count == 3) {
				mLogger.log(Level.ERROR, callerContext, logSource,
						"Please enter the valid argument for the List operation.");
				return -1;
			} else {
				String list = args[1];
				listCertificates(list, ts);
			}
		}

		// Import Operation
		if (args[0].equalsIgnoreCase("Import")) {
			mLogger.log(Level.DEBUG, callerContext, logSource, "Import operation started");
			if (args[1].contains(".cer") || args[1].contains(".crt")) {
				String certPath = args[1];
				mLogger.log(Level.INFO, callerContext, logSource, "Certificate path = " + certPath);
				if (count < 3) {
					mLogger.log(Level.ERROR, callerContext, logSource,
							"Enter valid alias name of the certificate to be imported");
					return -1;
				} else {
					String alias = args[2];
					mLogger.log(Level.INFO, callerContext, logSource,
							"Importing certificate : " + alias + "into KeyStore : " + ts + "with alias = " + alias);
					importCertificate(certPath, alias, tsPath, ts);
				}
			} else {
				mLogger.log(Level.ERROR, callerContext, logSource,
						"Enter valid certificate path for import operation!!");
				return -1;
			}
		}

		// Delete Operation
		if (args[0].equalsIgnoreCase("Delete")) {
			mLogger.log(Level.DEBUG, callerContext, logSource, "Delete Operation Started");
			String alias = args[1];
			deleteCertificate(alias, tsPath, ts);
		}
		return 0;
	}

	private static void setFSHomeDir() throws Exception {
		// Using log message
		LogMessage("In setFsHomeDir()");
		String localInstallDir = System.getProperty("INSTALL_DIR_LOCAL");
		String filename = localInstallDir + FS + "bin" + FS + "platform" + FS + "windows" + FS + "egainstart.bat";

		File file = new File(filename);
		if (!file.exists()) {
			LogError("Could not find egainstart.bat file in local home directory !! ");
			throw new Exception("Could not find egainstart.bat file in local home directory !!");
		}
		try (FileInputStream fis = new FileInputStream(filename);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));) {
			String strLine, installDir = null;
			int index = 0;
			while ((strLine = br.readLine()) != null) {
				strLine = strLine.toLowerCase();
				index = strLine.indexOf("@rem");
				if (index != -1)
					strLine = strLine.substring(0, index).trim();

				strLine = strLine.replaceAll("^set\\s+(install_dir|env.fs.shared.location)\\s*=", "@@@@");

				if (strLine.startsWith("@@@@")) {
					installDir = strLine.substring(4).trim();
					break;
				}
			}
			if (installDir == null || installDir.isBlank())
				throw new Exception("INSTALL_DIR could not be set !!");

			System.out.println("File Server home directory: " + installDir);
			System.setProperty("INSTALL_DIR", installDir);
			LogMessage("FS_HOME_DIR path : " + installDir);
		} catch (Exception e) {
			LogError("Exception occured" + e);
			System.exit(-1);
		}
		return;
	}

	public static void LogMessage(String msg) {
		System.out.println(" @DEBUG - " + msg);
	}

	public static void LogError(String msg) {
		System.out.println(" @ERROR - " + msg);
	}

	public static void main(String args[]) {

		Logger mLogger = null;
		LogSource logSource = null;

		try {
			setFSHomeDir();
			mLogger = Logger.getLogger("com.egain.installer");
			logSource = LogSource.getObject(classname, "main()", filename);
			Path parent = Paths.get(System.getProperty("INSTALL_DIR")).getParent();
			Path path = Paths.get(parent + FS + "env" + FS + "jdk" + FS + "lib" + FS + "security" + FS + "cacerts");
			String tsPath = String.valueOf(path);

			// Path required!
			File file = new File(tsPath);
			if (!file.exists()) {
				mLogger.log(Level.ERROR, callerContext, logSource, "Could not find cacerts file from location : ",
						tsPath);
				System.exit(-1);
			}
			KeyStore ts = KeyStore.getInstance("PKCS12");
			boolean loaded = false;
			try (FileInputStream fis = new FileInputStream(tsPath)) {
				ts.load(fis, "".toCharArray());
				loaded = true;
			} catch (IOException e) {
				loaded = false;
			}
			if (!loaded) {
				Security.addProvider(new BouncyCastleFipsProvider());
				ts = KeyStore.getInstance("PKCS12", BouncyCastleFipsProvider.PROVIDER_NAME);
				try (FileInputStream fis = new FileInputStream(tsPath)) {
					ts.load(fis, "".toCharArray());
					loaded = true;
				} catch (IOException e) {
					loaded = false;
				}
			}
			if (!loaded)
				throw new Exception("Could not load truststore: " + path);

			validateArgs(args, tsPath, ts);

			mLogger.log(Level.DEBUG, callerContext, logSource,
					"---------------Utility execution finished!-------------- ");
			System.exit(0);
		} catch (Exception e) {
			mLogger.log(Level.ERROR, callerContext, logSource, "Exception occured:", e);
			System.exit(-1);
		}
	}

	private static void importCertificate(String certPath, String alias, String tsPath, KeyStore ts) throws Exception {

		Logger mLogger = Logger.getLogger("com.egain.installer");
		LogSource logSource = LogSource.getObject(classname, "importCertificate()", filename);
		mLogger.log(Level.DEBUG, callerContext, logSource,
				"********************* Import Certificate operation Started *********************");

		try {
			mLogger.log(Level.INFO, callerContext, logSource, "Number of Certificates before addition : " + ts.size());
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			int count = 0;
			if (ts.containsAlias(alias)) {
				mLogger.log(Level.DEBUG, callerContext, logSource,
						"Alias already present in truststore, hence overwriting it: " + alias);
			}
			try (FileInputStream fis = new FileInputStream(certPath)) {
				while (fis.available() > 0) {
					Certificate cert = cf.generateCertificate(fis);
					if (cert == null)
						throw new Exception("Invalid certificate : Certificate is null.");

					mLogger.log(Level.INFO, callerContext, logSource, "Certificate count: " + ++count);
					if (count > 1)
						alias = alias + "_" + count;

					ts.setCertificateEntry(alias, cert);
				}
			}
			try (FileOutputStream fos = new FileOutputStream(tsPath)) {
				ts.store(fos, "".toCharArray());
			}
			mLogger.log(Level.DEBUG, callerContext, logSource, "KeyStore size after the Load operation:", ts.size());
		} catch (Exception e) {
			mLogger.log(Level.ERROR, callerContext, logSource, "Could not import given certificate.", e);
			throw e;
		}
	}

	private static String listCertificates(String list, KeyStore ts) {
		Logger mLogger = Logger.getLogger("com.egain.installer");
		LogSource logSource = LogSource.getObject(classname, "listCertificates()", filename);
		mLogger.log(Level.DEBUG, callerContext, logSource, "---------------List Operation Started-----------------");
		String currentAlias = null;

		try {
			Enumeration<String> aliases = ts.aliases();
			if (list.equalsIgnoreCase("ALL")) {
				while (aliases.hasMoreElements()) {
					currentAlias = aliases.nextElement();
					if (ts.isCertificateEntry(currentAlias)) {
						mLogger.log(Level.INFO, callerContext, logSource,
								"Certificate Alias:" + currentAlias + System.lineSeparator());
					}
				}
				mLogger.log(Level.DEBUG, callerContext, logSource,"Total number of certificates in the trustStore : " + ts.size());
				}
				else
					mLogger.log(Level.DEBUG, callerContext, logSource, "Listing Certificate : ",list);
					while (aliases.hasMoreElements()) {
						currentAlias = aliases.nextElement();
						if(ts.isCertificateEntry(currentAlias) && currentAlias.equals(list)){
							mLogger.log(Level.INFO, callerContext, logSource, "Alias found :" + currentAlias);
							return list;
						}}
					mLogger.log(Level.ERROR, callerContext, logSource,"Alias not found : " + list);
					System.exit(-1);
			
		} catch (Exception e) {
			mLogger.log(Level.ERROR, callerContext, logSource, "Exception occured : " + e);
		}
		return list;
	}

	public static void deleteCertificate(String alias, String tsPath, KeyStore ts) throws Exception {
		Logger mLogger = Logger.getLogger("com.egain.installer");
		LogSource logSource = LogSource.getObject(classname, "deleteCertificate()", filename);
		mLogger.log(Level.DEBUG, callerContext, logSource, "----------Delete operation started---------------");

		try {
			if (!ts.containsAlias(alias)) {
				mLogger.log(Level.ERROR, callerContext, logSource, "Alias not present, hence nothing to delete.");
				return;
			}
			ts.deleteEntry(alias);
			mLogger.log(Level.INFO, callerContext, logSource, "Certificate deleted..!");
			try (FileOutputStream fos = new FileOutputStream(tsPath)) {
				ts.store(fos, "".toCharArray());
			}
			mLogger.log(Level.INFO, callerContext, logSource, "Deletion successful.");
		} catch (Exception e) {
			mLogger.log(Level.ERROR, callerContext, logSource, "Could not delete certificate with alias: ", alias, e);
			throw e;
		}
	}
}
