package claudeagent.rest;

public class SessionContext {

    private static final ThreadLocal<String> CURRENT_SESSION_ID = new ThreadLocal<>();

    public static void setCurrentSessionId(String sessionId) {
        CURRENT_SESSION_ID.set(sessionId);
    }

    public static String getCurrentSessionId() {
        return CURRENT_SESSION_ID.get();
    }

    public static void clearCurrentSessionId() {
        CURRENT_SESSION_ID.remove();
    }
}
