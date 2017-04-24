package amp.lib.io.errors;

import java.util.EventObject;

@SuppressWarnings("serial")
public class ErrorEvent extends EventObject {

    Object info = "";;
    Severity severity = Severity.INFO;

    public ErrorEvent(Object source, Object info) {
        this(source, info, Severity.ERROR);
    }

    public ErrorEvent(Object source, Object info, Severity severity) {
        super(source);
        this.info = info;
        this.severity = severity;
    }

    public Object getInfo() {
        return info;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setInfo(Object info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "ErrorEvent [" + source.toString() + " : " + info + "]";
    }

    public enum Severity {
        INFO, WARNING, ERROR
    }
}
