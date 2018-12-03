


public class SSLManager
{
    public static final String pathToFile = "/path/to/file/";
    public static final String fileName = "test.keystore";
    public static final String password = "password";

    public static void trust()
    {
        String file = pathToFile + fileName;
        System.setProperty("javax.net.ssl.keyStore", file);
        System.setProperty("javax.net.ssl.keyStorePassword", password);
    }
};
