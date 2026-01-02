package com.urlshortener.api;

import com.urlshortener.manager.LinkManager;
import com.urlshortener.models.CreateLinkRequest;
import com.urlshortener.models.CreateLinkResponse;
import com.urlshortener.models.LinkCreationResult;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/links")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LinksResource {

    private final LinkManager linkManager;

    public LinksResource(LinkManager linkManager) {
        this.linkManager = linkManager;
    }

    @POST
    public Response createShortLink(@Valid CreateLinkRequest request) {
        LinkCreationResult result = linkManager.createLink(
                request.getLongUrl(),
                request.getCustomShortCode()
        );

        if (!result.isSuccess()) {
            return switch (result.getError()) {
                case CUSTOM_CODE_TOO_LONG ->
                    Response.status(Response.Status.BAD_REQUEST)
                    .entity(result.getErrorMessage())
                    .build();
                case CUSTOM_CODE_ALREADY_EXISTS ->
                    Response.status(Response.Status.CONFLICT)
                    .entity(result.getErrorMessage())
                    .build();
            };
        }

        CreateLinkResponse response = new CreateLinkResponse(result.getShortUrl(), result.getShortCode());
        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }
}
