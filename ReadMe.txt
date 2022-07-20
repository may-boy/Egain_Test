About the Utility
********************************************************************************************************************
This utility can be used to Import, Delete and List all as well as a particular certificate from the Java Keystore.
This Utility can append a certificate into the Java Cacerts. It would require path of the Certificate, the Alias as well as the FS_HOME_DIR

Using the Utility
****************************************************************
1) This utility can be run from any machine in eGain's network.
2) Provide appropriate parameters in input.properties file.
4) Open command prompt, go to utility home folder (JDK_KeyStore_Util) and run following command:

	(JDK_KeyStore_Util.bat)

5) The operation that can be performed are : 

	- Import 
	- List 
	- Delete

6) For Import operation we need to provide Certificate and alias along with the Import argument. Example is as follows :

	JDK_KeyStore_Util.bat Import C:\egainbatch\keystore_crud\Certificate.cer eg_custom_1
	where Import is the operation, then Certificate path is provided along with Alias name.

7) For List operation we can either list all the certificates or a particular one by providing the alias name. Example is given below : 

	JDK_KeyStore_Util.bat List ALL
	JDK_KeyStore_Util.bat List eg_custom_1

8) For Delete operation we need to provide the alias name of the Certificate to be deleted. Example is given below :

	JDK_KeyStore_Util.bat eg_custom_1