package com.spotify.wasd.service;

import com.spotify.wasd.db.Contact;
import com.spotify.wasd.db.DatabaseHolder;
import com.spotify.wasd.db.Host;
import com.spotify.wasd.db.Service;
import com.spotify.wasd.db.Site;
import com.sun.jersey.api.NotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("UnusedDeclaration")
@Slf4j
@Path("/hosts")
public class HostResource {

    private final DatabaseHolder holder;

    public HostResource(DatabaseHolder holder) {
        this.holder = holder;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JSONArray getHosts() {
        final JSONArray res = new JSONArray();

        for (Host host : holder.current().getHosts().getHostSet())
            res.add(host.getReverseName());

        return res;
    }

    /* will become more complete, we'll need to make it a JSON object first */
    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public JSONArray getHost(@PathParam("name") String name) {
        if (!name.endsWith("."))
            name = name + ".";

        final Host host = holder.current().getHosts().getNameHostMap().get(name);
        if (host == null)
            throw new NotFoundException("No such host");

        final JSONArray res = new JSONArray();
        for (Service service : host.getServiceSet())
            res.add(service.getName());

        return res;
    }

    @GET
    @Path("/{name}/with_sites")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public JSONObject getHostWithSites(@PathParam("name") String name) {
        if (!name.endsWith("."))
            name = name + ".";

        final Host host = holder.current().getHosts().getNameHostMap().get(name);
        if (host == null)
            throw new NotFoundException("No such host");

        final JSONObject res = new JSONObject();

        for (Map.Entry<Service, Set<Site>> entry : host.getServiceSiteMap().entrySet()) {
            final JSONArray siteList = new JSONArray();
            for (Site site : entry.getValue())
                siteList.add(site.getName());
            res.put(entry.getKey().getName(), siteList);
        }

        return res;
    }


    @GET
    @Path("/starting_with/{prefix}")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public JSONArray getHostByPrefix(@PathParam("prefix") String prefix) {
        final Set<Host> hostList = holder.current().getHosts().getHostsByPrefix(prefix);
        if (hostList.size() == 0)
            throw new NotFoundException("No such hosts");

        final JSONArray res = new JSONArray();

        for (Host host : hostList)
            res.add(host.getReverseName());

        return res;
    }

    @GET
    @Path("/{name}/contacts")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public JSONObject getHostWithContacts(@PathParam("name") String name) {
        if (!name.endsWith("."))
            name = name + ".";

        final Host host = holder.current().getHosts().getNameHostMap().get(name);
        if (host == null)
            throw new NotFoundException("No such host");

        final Map<String, HashSet<Contact>> mergedContacts = new HashMap<String, HashSet<Contact>>();
        for (Service service : host.getServiceSet()) {
            final Map<String, HashSet<Contact>> contactMap = service.getContactMap();
            for(Map.Entry<String, HashSet<Contact>> entry : contactMap.entrySet()) {
                if (mergedContacts.get(entry.getKey())== null) {
                    mergedContacts.put(entry.getKey(), new HashSet<Contact>());
                }
                mergedContacts.get(entry.getKey()).addAll(entry.getValue());
            }
        }
        final JSONObject res = new JSONObject();
        for (Map.Entry<String, HashSet<Contact>> entry : mergedContacts.entrySet())
            res.put(entry.getKey(), entry.getValue());
        return res;
    }
}
