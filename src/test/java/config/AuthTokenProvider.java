package config;

public class AuthTokenProvider {

    /**
     * This class provides the API bearer token.
     * For demonstration purposes, it's hardcoded here.
     *
     * IMPORTANT: Replace "YOUR_ACTUAL_API_TOKEN_HERE" with your valid token.
     */
    private static final String API_TOKEN = "API_TOKEN";

    public static String getToken() {
        return API_TOKEN;
    }
}