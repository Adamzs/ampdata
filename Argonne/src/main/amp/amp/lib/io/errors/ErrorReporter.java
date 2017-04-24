package amp.lib.io.errors;

public interface ErrorReporter {
    public void addErrorEventListener(ErrorEventListener listener);

    public void reportError(ErrorEvent event);
}
