package org.ossandme;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.processor.AbstractInterceptingMessageProcessor;
import org.mule.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

// Naive implementation
public class AhcProcessor extends AbstractInterceptingMessageProcessor {

    @Override
    public MuleEvent process(final MuleEvent event) throws MuleException {
        final CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
        final HttpGet request = new HttpGet("http://www.google.com/");

        httpclient.start();

        httpclient.execute(request, new FutureCallback<HttpResponse>() {

            public void completed(final HttpResponse response) {
                try {
                    InputStream input = response.getEntity().getContent();
                    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                    IOUtils.copy(input, byteOut);
                    event.setMessage(new DefaultMuleMessage(byteOut.toString(), event.getMessage().getMuleContext()));
                    final MuleEvent lastEvent = processNext(event);
                    lastEvent.getReplyToHandler().processReplyTo(lastEvent, lastEvent.getMessage(), event.getReplyToDestination());
                } catch (Exception e) {
                    throw new RuntimeException();
                }
            }

            public void failed(final Exception ex) {
                System.out.println(request.getRequestLine() + "->" + ex);
            }

            public void cancelled() {
                System.out.println(request.getRequestLine() + " cancelled");
            }

        });

        return null;
    }

}