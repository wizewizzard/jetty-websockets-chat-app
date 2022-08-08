package com.wu.chatserver.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wu.chatserver.dto.Errors;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

public class ErrorHandler  extends ErrorPageErrorHandler {

    private static final String ERROR_404_MESSAGE = "Target resource not found";

    private static final String ERROR_501_MESSAGE = "Server functionality to process request is not implemented";

    private static final String ERROR_502_MESSAGE = "Server cannot proxy request";

    private static final String ERROR_503_MESSAGE = "Server is currently unable to handle the request";

    private static final String ERROR_504_MESSAGE = "Server did not receive a timely response from an upstream server";

    private static final String ERROR_UNEXPECTED_MESSAGE = "Unexpected error occurs";

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void generateAcceptableResponse(Request baseRequest,
                                              HttpServletRequest request,
                                              HttpServletResponse response,
                                              int code, String message,
                                              String mimeType)
            throws IOException
    {
        Writer writer = response.getWriter();
        if (null != writer) {
            response.setContentType(MimeTypes.Type.APPLICATION_JSON.asString());
            response.setStatus(code);
            writer.write(mapper.writeValueAsString(new Errors(message)));
            //handleErrorPage(request, writer, code, message);
        }
    }
    private String getMessage(int code) {
        switch (code) {
            case 404 : return ERROR_404_MESSAGE;
            case 501 : return ERROR_501_MESSAGE;
            case 502 : return ERROR_502_MESSAGE;
            case 503 : return ERROR_503_MESSAGE;
            case 504 : return ERROR_504_MESSAGE;
            default  : return ERROR_UNEXPECTED_MESSAGE;
        }
    }
}
