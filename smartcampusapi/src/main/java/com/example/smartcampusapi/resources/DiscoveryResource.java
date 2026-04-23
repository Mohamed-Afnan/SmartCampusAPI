package com.example.smartcampusapi.resources;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Context;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover(@Context UriInfo uriInfo) {
        String baseUri = trimTrailingSlash(uriInfo.getBaseUri().toString());

        Map<String, Object> contact = new LinkedHashMap<String, Object>();
        contact.put("team", "Smart Campus Facilities API");
        contact.put("email", "facilities-api@westminster.ac.uk");

        Map<String, String> resources = new LinkedHashMap<String, String>();
        resources.put("rooms", baseUri + "/rooms");
        resources.put("sensors", baseUri + "/sensors");

        Map<String, String> links = new LinkedHashMap<String, String>();
        links.put("self", baseUri);
        links.put("rooms", baseUri + "/rooms");
        links.put("sensors", baseUri + "/sensors");

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("name", "Smart Campus Sensor & Room Management API");
        payload.put("version", "v1");
        payload.put("administrativeContact", contact);
        payload.put("resources", resources);
        payload.put("_links", links);

        return Response.ok(payload).build();
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
