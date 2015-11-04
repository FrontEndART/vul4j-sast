package de.intevation.lada.test.stamm;

import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.Assert;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import de.intevation.lada.BaseTest;
import de.intevation.lada.Protocol;

public class Stammdaten {

    private static Map<String, Matcher> matchers;

    public Stammdaten() {
        matchers = new HashMap<String, Matcher>();

        matchers.put("datenbasis",
            Matchers.containsInAnyOrder("id","beschreibung","datenbasis")
        );

        matchers.put("messeinheit",
            Matchers.containsInAnyOrder(
                "id",
                "beschreibung",
                "einheit",
                "eudfMesseinheitId",
                "umrechnungsFaktorEudf"
            )
        );

        matchers.put("messgroesse",
            Matchers.containsInAnyOrder(
                "id",
                "beschreibung",
                "defaultFarbe",
                "eudfNuklidId",
                "idfNuklidKey",
                "istLeitnuklid",
                "kennungBvl",
                "messgroesse"
            )
        );

        matchers.put("messmethode",
            Matchers.containsInAnyOrder("id","beschreibung","messmethode")
        );

        matchers.put("messstelle",
            Matchers.containsInAnyOrder(
                "id",
                "amtskennung",
                "beschreibung",
                "messStelle",
                "mstTyp",
                "netzbetreiberId"
            )
        );

        matchers.put("netzbetreiber",
            Matchers.containsInAnyOrder(
                "id",
                "aktiv",
                "idfNetzbetreiber",
                "isBmn",
                "mailverteiler",
                "netzbetreiber",
                "zustMstId"
            )
        );

        matchers.put("pflichtmessgroesse",
            Matchers.containsInAnyOrder(
                "id",
                "messgroesseId",
                "datenbasisId",
                "mmtId",
                "umweltId"
            )
        );

        matchers.put("probenart",
            Matchers.containsInAnyOrder(
                "id",
                "beschreibung",
                "probenart",
                "probenartEudfId"
            )
        );

        matchers.put("probenzusatz",
            Matchers.containsInAnyOrder(
                "id",
                "beschreibung",
                "eudfKeyword",
                "zusatzwert",
                "mehId"
            )
        );

        matchers.put("location",
            Matchers.containsInAnyOrder(
                "id",
                "beschreibung",
                "bezeichnung",
                "hoeheLand",
                "koordXExtern",
                "koordYExtern",
                "latitude",
                "longitude",
                "letzteAenderung",
                "nutsCode",
                "unscharf",
                "netzbetreiberId",
                "staatId",
                "verwaltungseinheitId",
                "otyp",
                "koordinatenartId"
            )
        );

        matchers.put("koordinatenart",
            Matchers.containsInAnyOrder("id","idfGeoKey","koordinatenart")
        );

        matchers.put("staat",
            Matchers.containsInAnyOrder(
                "id",
                "eu",
                "hklId",
                "koordXExtern",
                "koordYExtern",
                "staat",
                "staatIso",
                "staatKurz",
                "koordinatenartId"
            )
        );

        matchers.put("umwelt",
            Matchers.containsInAnyOrder("id","beschreibung","umweltBereich","mehId")
        );

        matchers.put("verwaltungseinheit",
            Matchers.containsInAnyOrder(
                "id",
                "bezeichnung",
                "bundesland",
                "isBundesland",
                "isGemeinde",
                "isLandkreis",
                "isRegbezirk",
                "koordXExtern",
                "koordYExtern",
                "kreis",
                "latitude",
                "longitude",
                "nuts",
                "plz",
                "regbezirk",
                "koordinatenartId"
            )
        );
    }

    /**
     * Test the GET Service by requesting all objects.
     *
     * @param baseUrl The url pointing to the test deployment.
     */
    public final void getAll(URL baseUrl, String type, List<Protocol> protocol) {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName(type + "Service");
        prot.setType("get all");
        prot.setPassed(false);
        protocol.add(prot);
        Assert.assertNotNull(type);
        /* Create a client*/
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(baseUrl + type);
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

    public final void getById(
        URL baseUrl,
        String type,
        Object id,
        List<Protocol> protocol
    ) {
        System.out.print(".");
        Protocol prot = new Protocol();
        prot.setName(type + "Service");
        prot.setType("get by Id");
        prot.setPassed(false);
        protocol.add(prot);
        try {
            /* Create a client*/
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(baseUrl + type +"/" + id);
            prot.addInfo(type + "Id", id);
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
            Assert.assertThat(content.getJsonObject("data").keySet(),
                matchers.get(type));
            prot.addInfo("object", "equals");
        }
        catch(JsonException je) {
            prot.addInfo("exception", je.getMessage());
            Assert.fail(je.getMessage());
        }
        prot.setPassed(true);
    }
}
