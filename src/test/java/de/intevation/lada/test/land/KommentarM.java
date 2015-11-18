/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import java.io.StringReader;
import java.net.URL;
import java.sql.Date;
import java.util.List;
import java.util.TimeZone;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.Protocol;


/**
 * Class containing test cases for messung kommentar objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class KommentarM {

    private static final String COMPARE_KOMMENTARM =
        "{\"messungsId\":5,\"datum\":1336634700000,\"erzeuger\":\"12010\"," +
        "\"id\":478,\"text\":\"Hofladen Lenzen geschlossen\",\"owner\":false," +
        "\"readonly\":true}";

    private static final String CREATE_KOMMENTARM =
        "{\"messungsId\":\"MID\",\"erzeuger\":\"06010\",\"text\":" +
        "\"test\",\"datum\":\"2015-02-09T10:58:36\",\"owner\":false}";

    private List<Protocol> protocol;

    private static Integer createdKommentarId;

    /**
     * @return The test protocol
     */
    public List<Protocol> getProtocol() {
        return protocol;
    }

    /**
     * @return The created KommentarId
     */
    public Integer getCreatedKommentarId() {
        return createdKommentarId;
    }

    /**
     * Test the GET Service by requesting all objects.
     *
     * @param baseUrl The url pointing to the test deployment.
     */
    public final void getAllService(URL baseUrl, List<Protocol> protocol)
    throws Exception {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("MessungKommentarService");
        prot.setType("get all");
        prot.setPassed(false);
        protocol.add(prot);
        /* Create a client*/
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(baseUrl + "mkommentar");
        /* Request all objects*/
        Response response = target.request()
            .header("X-SHIB-user", BaseTest.TEST_USER)
            .header("X-SHIB-roles", BaseTest.TEST_ROLES)
            .get();
        String entity = response.readEntity(String.class);
        try{
            /* Try to parse the response*/
            JsonReader reader = Json.createReader(new StringReader(entity));
            JsonObject content = reader.readObject();
            /* Verify the response*/
            Assert.assertTrue(content.getBoolean("success"));
            prot.addInfo("success", content.getBoolean("success"));
            Assert.assertEquals("200", content.getString("message"));
            prot.addInfo("message", content.getString("message"));
            Assert.assertNotNull(content.getJsonArray("data"));
            prot.addInfo("objects", content.getJsonArray("data").size());
        }
        catch(JsonException je) {
            prot.addInfo("exception", je.getMessage());
            Assert.fail(je.getMessage());
        }
        prot.setPassed(true);
    }

    /**
     * Test the GET Service by requesting a single object by id.
     *
     * @param baseUrl The url pointing to the test deployment.
     */
    public final void getByIdService(URL baseUrl, List<Protocol> protocol)
    throws Exception {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("MessungKommentarService");
        prot.setType("get by Id");
        prot.setPassed(false);
        protocol.add(prot);
        try {
            /* Create a json object from static string*/
            JsonReader fromStringRreader =
                Json.createReader(new StringReader(COMPARE_KOMMENTARM));
            JsonObject staticKommentar = fromStringRreader.readObject();
            /* Create a client*/
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(baseUrl + "mkommentar/478");
            prot.addInfo("kommentarId", 478);
            /* Request an object by id*/
            Response response = target.request()
                .header("X-SHIB-user", BaseTest.TEST_USER)
                .header("X-SHIB-roles", BaseTest.TEST_ROLES)
                .get();
            String entity = response.readEntity(String.class);
            /* Try to parse the response*/
            JsonReader fromServiceReader =
                Json.createReader(new StringReader(entity));
            JsonObject content = fromServiceReader.readObject();
            /* Verify the response*/
            Assert.assertTrue(content.getBoolean("success"));
            prot.addInfo("success", content.getBoolean("success"));
            Assert.assertEquals("200", content.getString("message"));
            prot.addInfo("message", content.getString("message"));
            Assert.assertEquals(staticKommentar,
                content.getJsonObject("data"));
            prot.addInfo("object", "equals");
        }
        catch(JsonException je) {
            prot.addInfo("exception", je.getMessage());
            Assert.fail(je.getMessage());
        }
        prot.setPassed(true);
    }

    /**
     * Test the GET service using filters.
     *
     * @param baseUrl The url poining to the test deployment.
     */
    public final void filterService(URL baseUrl, List<Protocol> protocol) {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("MessungKommentarService");
        prot.setType("get by filter");
        prot.setPassed(false);
        protocol.add(prot);
        try {
            /* Create a client*/
            Client client = ClientBuilder.newClient();
            WebTarget target =
                client.target(baseUrl + "mkommentar?messungId=988");
            prot.addInfo("filter", "messungId=988");
            /* Request the objects using the filter*/
            Response response = target.request()
                .header("X-SHIB-user", BaseTest.TEST_USER)
                .header("X-SHIB-roles", BaseTest.TEST_ROLES)
                .get();
            String entity = response.readEntity(String.class);
            /* Try to parse the response*/
            JsonReader reader = Json.createReader(new StringReader(entity));
            JsonObject respObj = reader.readObject();
            /* Verify the response*/
            Assert.assertTrue(respObj.getBoolean("success"));
            prot.addInfo("success", respObj.getBoolean("success"));
            Assert.assertEquals("200", respObj.getString("message"));
            prot.addInfo("message", respObj.getString("message"));
            Assert.assertNotNull(respObj.getJsonArray("data"));
            prot.addInfo("objects", respObj.getJsonArray("data").size());
        }
        catch(JsonException je) {
            prot.addInfo("exception", je.getMessage());
            Assert.fail(je.getMessage());
        }
        prot.setPassed(true);
    }

    /**
     * Test the CREATE Service.
     *
     * @param baseUrl The url pointing to the test deployment.
     */
    public final void createService(
        URL baseUrl,
        List<Protocol> protocol,
        Integer messungId)
    throws Exception {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("MessungKommentarService");
        prot.setType("create");
        prot.setPassed(false);
        protocol.add(prot);
        try {
            /* Create a client*/
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(baseUrl + "mkommentar");
            /* Send a post request containing a new kommentar*/
            String mess = CREATE_KOMMENTARM.replace("MID", messungId.toString());
            Response response = target.request()
                .header("X-SHIB-user", BaseTest.TEST_USER)
                .header("X-SHIB-roles", BaseTest.TEST_ROLES)
                .post(
                    Entity.entity(mess, MediaType.APPLICATION_JSON));
            String entity = response.readEntity(String.class);
            /* Try to parse the response*/
            JsonReader fromServiceReader =
                Json.createReader(new StringReader(entity));
            JsonObject content = fromServiceReader.readObject();
            /* Save the id*/
            createdKommentarId =
                content.getJsonObject("data").getJsonNumber("id").intValue();
            prot.addInfo("kommentarId", createdKommentarId);
            /* Verify the response*/
            Assert.assertTrue(content.getBoolean("success"));
            prot.addInfo("success", content.getBoolean("success"));
            Assert.assertEquals("200", content.getString("message"));
            prot.addInfo("message", content.getString("message"));
        }
        catch(JsonException je) {
            prot.addInfo("exception", je.getMessage());
            Assert.fail(je.getMessage());
        }
        prot.setPassed(true);
    }

    /**
     * Test the UPDATE Service.
     *
     * @param baseUrl The url pointing to the test deployment.
     */
    public final void updateService(URL baseUrl, List<Protocol> protocol)
    throws Exception {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("MessungKommentarService");
        prot.setType("update");
        prot.setPassed(false);
        protocol.add(prot);
        try {
            /* Create a client*/
            Client client = ClientBuilder.newClient();
            WebTarget target =
                client.target(baseUrl + "mkommentar/" + createdKommentarId);
            prot.addInfo("kommentarId", createdKommentarId);
            /* Request a kommentar with the id saved when created a kommentar*/
            Response response = target.request()
                .header("X-SHIB-user", BaseTest.TEST_USER)
                .header("X-SHIB-roles", BaseTest.TEST_ROLES)
                .get();
            String entity = response.readEntity(String.class);
            /* Try to parse the response*/
            JsonReader reader = Json.createReader(new StringReader(entity));
            JsonObject oldKommentar = reader.readObject().getJsonObject("data");
            /* Change the text*/
            String updatedEntity =
                oldKommentar.toString().replace("test", "neu");
            prot.addInfo("updated field", "text");
            prot.addInfo("updated value", "test");
            prot.addInfo("updated to", "neu");
            /* Send the updated kommentar via put reauest*/
            WebTarget putTarget = client.target(baseUrl + "mkommentar/" + createdKommentarId);
            Response updated = putTarget.request()
                .header("X-SHIB-user", BaseTest.TEST_USER)
                .header("X-SHIB-roles", BaseTest.TEST_ROLES)
                .put(Entity.entity(updatedEntity, MediaType.APPLICATION_JSON));
            /* Try to parse the response*/
            JsonReader updatedReader = Json.createReader(
                new StringReader(updated.readEntity(String.class)));
            JsonObject updatedMessung = updatedReader.readObject();
            /* Verify the response*/
            Assert.assertTrue(updatedMessung.getBoolean("success"));
            prot.addInfo("success", updatedMessung.getBoolean("success"));
            Assert.assertEquals("200", updatedMessung.getString("message"));
            prot.addInfo("message", updatedMessung.getString("message"));
            Assert.assertEquals("neu",
                updatedMessung.getJsonObject("data").getString("text"));
        }
        catch(JsonException je) {
            prot.addInfo("exception", je.getMessage());
            Assert.fail(je.getMessage());
        }
        prot.setPassed(true);
    }

    /**
     * Test the DELETE Service.
     *
     * @param baseUrl The url pointing to the test deployment.
     */
    public final void deleteService(URL baseUrl, List<Protocol> protocol) {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("MessungKommentarService");
        prot.setType("delete");
        prot.setPassed(false);
        protocol.add(prot);
        try {
            /* Create a client*/
            Client client = ClientBuilder.newClient();
            WebTarget target =
                client.target(baseUrl + "mkommentar/" + createdKommentarId);
            prot.addInfo("kommentarId", createdKommentarId);
            /* Delete a kommentar with the saved id*/
            Response response = target.request()
                .header("X-SHIB-user", BaseTest.TEST_USER)
                .header("X-SHIB-roles", BaseTest.TEST_ROLES)
                .delete();
            String entity = response.readEntity(String.class);
            /* Try to parse the response*/
            JsonReader reader = Json.createReader(new StringReader(entity));
            JsonObject respObj = reader.readObject();
            /* Verify the response*/
            Assert.assertTrue(respObj.getBoolean("success"));
            prot.addInfo("success", respObj.getBoolean("success"));
            Assert.assertEquals("200", respObj.getString("message"));
            prot.addInfo("message", respObj.getString("message"));
        }
        catch(JsonException je) {
            prot.addInfo("exception", je.getMessage());
            Assert.fail(je.getMessage());
        }
        prot.setPassed(true);
    }
}
