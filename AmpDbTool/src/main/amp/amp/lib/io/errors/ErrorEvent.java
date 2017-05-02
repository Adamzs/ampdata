package amp.lib.io.errors;

import amp.lib.io.meta.MetaObject;

@SuppressWarnings("serial")
public class ErrorEvent {

    MetaObject source = null;
    String message = "";
    Severity severity = Severity.INFO;

    public ErrorEvent(MetaObject source, String message, Severity severity) {
        this.source = source;
        this.message = message;
        this.severity = severity;
    }

    public ErrorEvent(String message, Severity severity) {
        this(null, message, severity);
    }

    public Object getInfo() {
        return message;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setInfo(String message) {
        this.message = message;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    @Override
    public String toString() {
        if (source == null) {
            return getSeverity() + ": " + message;
        } else {
            return getSeverity() + " " + source.getIdentifier() + ": " + message;
        }
    }

    public enum Severity {
        INFO, WARNING, ERROR
    }
}
