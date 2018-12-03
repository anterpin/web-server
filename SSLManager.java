


public class SSLManager
{
    public static final String pathToFile = "/home/anterpin/Java/";
    public static final String fileName = "test.keystore";
    public static final String password = "ciaociao";

    public static void trust()
    {
        String file = pathToFile + fileName;
        System.setProperty("javax.net.ssl.keyStore", file);
        System.setProperty("javax.net.ssl.keyStorePassword", password);
    }
};