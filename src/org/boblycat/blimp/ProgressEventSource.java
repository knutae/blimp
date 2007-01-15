package org.boblycat.blimp;

public class ProgressEventSource extends
        EventSource<ProgressListener, ProgressEvent> {

    protected void triggerListenerEvent(ProgressListener listener,
            ProgressEvent event) {
        listener.reportProgress(event);
    }
}
