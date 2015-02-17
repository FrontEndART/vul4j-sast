package de.intevation.lada.test;

import java.io.StringReader;
import java.net.URL;
import java.util.List;

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

import de.intevation.lada.Protocol;

public class Messung {

    private static final String COMPARE_MESSUNG =
        "{\"id\":1,\"fertig\":true,\"letzteAenderung\":1331536340000," +
        "\"messdauer\":73929,\"messzeitpunkt\":1329139620000,\"mmtId\":" +
        "\"G1\",\"probeId\":575,\"nebenprobenNr\":\"01G1\",\"geplant\":true," +
        "\"messungsIdAlt\":1}";

    private static final String CREATE_MESSUNG =
        "{\"probeId\":\"PID\",\"mmtId\":\"A4\",\"nebenprobenNr\":\"10R1\"," +
        "\"messdauer\":10,\"fertig\":false,\"letzteAenderung\":null," +
        "\"geplant\":true,\"messzeitpunkt\":\"2015-02-09T10:58:36\"}";

    private List<Protocol> protocol;

    private static Integer createdMessungId;

    public Integer getCreatedMessungId() {
        return createdMessungId;
    }

    /**
     * @return the protocol
     */
    public List<Protocol> getProtocol() {
        return protocol;
    }

    /**
     * Test the GET Service by requesting all messung objects.
     *
     * @param baseUrl The url pointing to the test deployment.
     */
    public final void getAllService(URL baseUrl, List<Protocol> protocol)
    throws Exception {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("MessungService");
        prot.setType("get all");
        prot.setPassed(false);
        protocol.add(prot);
        /* Create a client*/
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(baseUrl + "messung");
        /* Request all probe objects*/
        Response response = target.request().get();
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
     * Test the GET Service by requesting a single messung object by id.
     *
     * @param baseUrl The url pointing to the test deployment.
     */
    public final void getByIdService(URL baseUrl, List<Protocol> protocol)
    throws Exception {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("MessungService");
        prot.setType("get by Id");
        prot.setPassed(false);
        protocol.add(prot);
        try {
            /* Create a json object from static messung string*/
            JsonReader fromStringRreader =
                Json.createReader(new StringReader(COMPARE_MESSUNG));
            JsonObject staticMessung = fromStringRreader.readObject();
            /* Create a client*/
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(baseUrl + "messung/1");
            prot.addInfo("messungId", 1);
            /* Request a probe object by id*/
            Response response = target.request().get();
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
            Assert.assertEquals(staticMessung,
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
     * Test the GET messung service using filters.
     *
     * @param baseUrl The url poining to the test deployment.
     */
    public final void filterService(URL baseUrl, List<Protocol> protocol) {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("MessungService");
        prot.setType("get by filter");
        prot.setPassed(false);
        protocol.add(prot);
        try {
            /* Create a client*/
            Client client = ClientBuilder.newClient();
            WebTarget target =
                client.target(baseUrl + "messung?probeId=1");
            prot.addInfo("filter", "probeId=1");
            /* Request the probe objects using the filter*/
            Response response = target.request().get();
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
     * Test the messung CREATE Service.
     *
     * @param baseUrl The url pointing to the test deployment.
     */
    public final void createService(
        URL baseUrl,
        List<Protocol> protocol,
        Integer probeId)
    throws Exception {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("MessungService");
        prot.setType("create");
        prot.setPassed(false);
        protocol.add(prot);
        try {
            /* Create a client*/
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(baseUrl + "messung");
            /* Send a post request containing a new probe*/
            String mess = CREATE_MESSUNG.replace("PID", probeId.toString());
            Response response = target.request().post(
                    Entity.entity(mess, MediaType.APPLICATION_JSON));
            String entity = response.readEntity(String.class);
            /* Try to parse the response*/
            JsonReader fromServiceReader =
                Json.createReader(new StringReader(entity));
            JsonObject content = fromServiceReader.readObject();
            /* Save the probeid*/
            createdMessungId =
                content.getJsonObject("data").getJsonNumber("id").intValue();
            prot.addInfo("messungId", createdMessungId);
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
     * Test the messung UPDATE Service.
     *
     * @param baseUrl The url pointing to the test deployment.
     */
    public final void updateService(URL baseUrl, List<Protocol> protocol)
    throws Exception {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("MessungService");
        prot.setType("update");
        prot.setPassed(false);
        protocol.add(prot);
        try {
            /* Create a client*/
            Client client = ClientBuilder.newClient();
            WebTarget target =
                client.target(baseUrl + "messung/" + createdMessungId);
            prot.addInfo("messungId", createdMessungId);
            /* Request a probe with the id saved when created a probe*/
            Response response = target.request().get();
            String entity = response.readEntity(String.class);
            /* Try to parse the response*/
            JsonReader reader = Json.createReader(new StringReader(entity));
            JsonObject oldMessung = reader.readObject().getJsonObject("data");
            /* Change the hauptprobenNr*/
            String updatedEntity =
                oldMessung.toString().replace("A4", "G1");
            prot.addInfo("updated field", "mmtId");
            prot.addInfo("updated value", "A4");
            prot.addInfo("updated to", "G1");
            /* Send the updated probe via put reauest*/
            WebTarget putTarget = client.target(baseUrl + "messung");
            Response updated = putTarget.request().put(
                Entity.entity(updatedEntity, MediaType.APPLICATION_JSON));
            /* Try to parse the response*/
            JsonReader updatedReader = Json.createReader(
                new StringReader(updated.readEntity(String.class)));
            JsonObject updatedMessung = updatedReader.readObject();
            /* Verify the response*/
            Assert.assertTrue(updatedMessung.getBoolean("success"));
            prot.addInfo("success", updatedMessung.getBoolean("success"));
            Assert.assertEquals("200", updatedMessung.getString("message"));
            prot.addInfo("message", updatedMessung.getString("message"));
            Assert.assertEquals("G1",
                updatedMessung.getJsonObject("data").getString("mmtId"));
        }
        catch(JsonException je) {
            prot.addInfo("exception", je.getMessage());
            Assert.fail(je.getMessage());
        }
        prot.setPassed(true);
    }

    /**
     * Test the messung DELETE Service.
     *
     * @param baseUrl The url pointing to the test deployment.
     */
    public final void deleteService(URL baseUrl, List<Protocol> protocol) {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName("MessungService");
        prot.setType("delete");
        prot.setPassed(false);
        protocol.add(prot);
        try {
            /* Create a client*/
            Client client = ClientBuilder.newClient();
            WebTarget target =
                client.target(baseUrl + "messung/" + createdMessungId);
            prot.addInfo("messungId", createdMessungId);
            /* Delete a probe with the id saved when created a probe*/
            Response response = target.request().delete();
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
