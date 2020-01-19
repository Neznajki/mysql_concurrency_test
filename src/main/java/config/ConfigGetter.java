package config;

public class ConfigGetter {

    public Integer getInteger(String paramName)
    {
        return Integer.valueOf(System.getenv(paramName));
    }

    public String getString(String paramName)
    {
        return System.getenv(paramName);
    }

}
