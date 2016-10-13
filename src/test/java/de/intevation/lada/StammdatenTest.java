/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3) 
 * and comes with ABSOLUTELY NO WARRANTY! Check out 
 * the documentation coming with IMIS-Labordaten-Application for details. 
 */
package de.intevation.lada;

import java.net.URL;
import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.persistence.ApplyScriptBefore;
import org.jboss.arquillian.persistence.Cleanup;
import org.jboss.arquillian.persistence.DataSource;
import org.jboss.arquillian.persistence.TestExecutionPhase;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import de.intevation.lada.model.stamm.DatensatzErzeuger;
import de.intevation.lada.model.stamm.MessprogrammKategorie;
import de.intevation.lada.model.stamm.Ort;
import de.intevation.lada.model.stamm.Probenehmer;
import de.intevation.lada.test.stamm.DatensatzErzeugerTest;
import de.intevation.lada.test.stamm.MessprogrammKategorieTest;
import de.intevation.lada.test.stamm.OrtTest;
import de.intevation.lada.test.stamm.ProbenehmerTest;
import de.intevation.lada.test.stamm.Stammdaten;


/**
 * Class to test the Lada server stammdaten services.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@RunWith(Arquillian.class)
public class StammdatenTest extends BaseTest {

    private static Logger logger = Logger.getLogger(StammdatenTest.class);

    @PersistenceContext(unitName="stamm")
    EntityManager em;

    private Stammdaten stammdatenTest;
    private DatensatzErzeugerTest datensatzerzeugerTest;
    private ProbenehmerTest probenehmerTest;
    private MessprogrammKategorieTest messprogrammkategorieTest;
    private OrtTest ortTest;

    public StammdatenTest () {
        stammdatenTest = new Stammdaten();
        datensatzerzeugerTest = new DatensatzErzeugerTest();
        probenehmerTest = new ProbenehmerTest();
        messprogrammkategorieTest = new MessprogrammKategorieTest();
        ortTest = new OrtTest();
        testProtocol = new ArrayList<Protocol>();
        verboseLogging = false;
    }

    @BeforeClass
    public static void beforeTests() {
        logger.info("---------- Testing Lada Stamm Services ----------");
    }

    /**
     * Insert a probe object into the database.
     */
    @Test
    @InSequence(1)
    @ApplyScriptBefore("datasets/clean_and_seed.sql")
    @UsingDataSet("datasets/dbUnit_datensatzerzeuger.json")
    @DataSource("java:jboss/lada-stamm")
    @Cleanup(phase=TestExecutionPhase.NONE)
    public final void prepareDatabaseDatensatzerzeuger() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("database");
        protocol.setType("insert datensatzerzeuger");
        protocol.addInfo("database", "Insert datensatzerzeuger into database");
        testProtocol.add(protocol);
        DatensatzErzeuger erzeuger = em.find(DatensatzErzeuger.class, 1000);
        Assert.assertNotNull(erzeuger);
        protocol.setPassed(true);
    }

    /**
     * Tests for probe operations
     */
    @Test
    @InSequence(2)
    @RunAsClient
    public final void testDatensatzerzeuger(@ArquillianResource URL baseUrl)
    throws Exception {
        datensatzerzeugerTest.init(baseUrl, testProtocol);
        datensatzerzeugerTest.execute();
    }

    /**
     * Insert a probe object into the database.
     */
    @Test
    @InSequence(3)
    @UsingDataSet("datasets/dbUnit_probenehmer.json")
    @DataSource("java:jboss/lada-stamm")
    @Cleanup(phase=TestExecutionPhase.NONE)
    public final void prepareDatabaseProbenehmer() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("database");
        protocol.setType("insert probenehmer");
        protocol.addInfo("database", "Insert Probenehmer into database");
        testProtocol.add(protocol);
        Probenehmer probenehmer = em.find(Probenehmer.class, 1000);
        Assert.assertNotNull(probenehmer);
        protocol.setPassed(true);
    }

    /**
     * Tests for probe operations
     */
    @Test
    @InSequence(4)
    @RunAsClient
    public final void testProbenehmer(@ArquillianResource URL baseUrl)
    throws Exception {
        probenehmerTest.init(baseUrl, testProtocol);
        probenehmerTest.execute();
    }

    /**
     * Insert a probe object into the database.
     */
    @Test
    @InSequence(5)
    @UsingDataSet("datasets/dbUnit_messprogrammkategorie.json")
    @DataSource("java:jboss/lada-stamm")
    @Cleanup(phase=TestExecutionPhase.NONE)
    public final void prepareDatabaseMessprogrammKategorie() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("database");
        protocol.setType("insert messprogrammkategorie");
        protocol.addInfo("database", "Insert messprogrammkategorie into database");
        testProtocol.add(protocol);
        MessprogrammKategorie kategorie = em.find(MessprogrammKategorie.class, 1000);
        Assert.assertNotNull(kategorie);
        protocol.setPassed(true);
    }

    /**
     * Tests for probe operations
     */
    @Test
    @InSequence(6)
    @RunAsClient
    public final void testMessprogrammKategorie(@ArquillianResource URL baseUrl)
    throws Exception {
        messprogrammkategorieTest.init(baseUrl, testProtocol);
        messprogrammkategorieTest.execute();
    }

    /**
     * Insert a probe object into the database.
     */
    @Test
    @InSequence(7)
    @UsingDataSet("datasets/dbUnit_ort.json")
    @DataSource("java:jboss/lada-stamm")
    @Cleanup(phase=TestExecutionPhase.NONE)
    public final void prepareDatabaseOrt() throws Exception {
        Protocol protocol = new Protocol();
        protocol.setName("database");
        protocol.setType("insert ort");
        protocol.addInfo("database", "Insert ortinto database");
        testProtocol.add(protocol);
        Ort ort = em.find(Ort.class, 1000);
        Assert.assertNotNull(ort);
        protocol.setPassed(true);
    }

    /**
     * Tests for probe operations
     */
    @Test
    @InSequence(8)
    @RunAsClient
    public final void testOrt(@ArquillianResource URL baseUrl)
    throws Exception {
        ortTest.init(baseUrl, testProtocol);
        ortTest.execute();
    }

    @Test
    @InSequence(9)
    @RunAsClient
    public final void testDatenbasisAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.getAll(baseUrl, "datenbasis", testProtocol);
    }

    @Test
    @InSequence(10)
    @RunAsClient
    public final void testDatenbasisById(@ArquillianResource URL baseUrl) {
        stammdatenTest.getById(baseUrl, "datenbasis", 9, testProtocol);
    }

    @Test
    @InSequence(11)
    @RunAsClient
    public final void testMesseinheitAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.getAll(baseUrl, "messeinheit", testProtocol);
    }

    @Test
    @InSequence(12)
    @RunAsClient
    public final void testMesseinheitById(@ArquillianResource URL baseUrl) {
        stammdatenTest.getById(baseUrl, "messeinheit", 207, testProtocol);
    }

    @Test
    @InSequence(13)
    @RunAsClient
    public final void testMessgroesseAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.getAll(baseUrl, "messgroesse", testProtocol);
    }

    @Test
    @InSequence(14)
    @RunAsClient
    public final void testMessgroesseById(@ArquillianResource URL baseUrl) {
        stammdatenTest.getById(baseUrl, "messgroesse", 56, testProtocol);
    }

    @Test
    @InSequence(15)
    @RunAsClient
    public final void testMessmethodeAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.getAll(baseUrl, "messmethode", testProtocol);
    }

    @Test
    @InSequence(16)
    @RunAsClient
    public final void testMessmethodeById(@ArquillianResource URL baseUrl) {
        stammdatenTest.getById(baseUrl, "messmethode", "GI", testProtocol);
    }

    @Test
    @InSequence(17)
    @RunAsClient
    public final void testMessstelleAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.getAll(baseUrl, "messstelle", testProtocol);
    }

    @Test
    @InSequence(18)
    @RunAsClient
    public final void testMessstelleById(@ArquillianResource URL baseUrl) {
        stammdatenTest.getById(baseUrl, "messstelle", "06010", testProtocol);
    }

    @Test
    @InSequence(19)
    @RunAsClient
    public final void testNetzbetreiberAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.getAll(baseUrl, "netzbetreiber", testProtocol);
    }

    @Test
    @InSequence(20)
    @RunAsClient
    public final void testNetzbetreiberById(@ArquillianResource URL baseUrl) {
        stammdatenTest.getById(baseUrl, "netzbetreiber", "06", testProtocol);
    }

    @Test
    @InSequence(21)
    @RunAsClient
    public final void testPflichtmessgroesseAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.getAll(baseUrl, "pflichtmessgroesse", testProtocol);
    }

    @Test
    @InSequence(22)
    @RunAsClient
    public final void testPflichtmessgroesseById(@ArquillianResource URL baseUrl) {
        stammdatenTest.getById(baseUrl, "pflichtmessgroesse", 33, testProtocol);
    }

    @Test
    @InSequence(23)
    @RunAsClient
    public final void testProbenartAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.getAll(baseUrl, "probenart", testProtocol);
    }

    @Test
    @InSequence(24)
    @RunAsClient
    public final void testProbenartById(@ArquillianResource URL baseUrl) {
        stammdatenTest.getById(baseUrl, "probenart", 1, testProtocol);
    }

    @Test
    @InSequence(25)
    @RunAsClient
    public final void testProbenzusatzAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.getAll(baseUrl, "probenzusatz", testProtocol);
    }

    @Test
    @InSequence(26)
    @RunAsClient
    public final void testProbenzusatzById(@ArquillianResource URL baseUrl) {
        stammdatenTest.getById(baseUrl, "probenzusatz", "A74", testProtocol);
    }

    @Test
    @InSequence(27)
    @RunAsClient
    public final void testKoordinatenartAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.getAll(baseUrl, "koordinatenart", testProtocol);
    }

    @Test
    @InSequence(28)
    @RunAsClient
    public final void testKoordinatenartById(@ArquillianResource URL baseUrl) {
        stammdatenTest.getById(baseUrl, "koordinatenart", 5, testProtocol);
    }

    @Test
    @InSequence(29)
    @RunAsClient
    public final void testStaatAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.getAll(baseUrl, "staat", testProtocol);
    }

    @Test
    @InSequence(30)
    @RunAsClient
    public final void testStaatById(@ArquillianResource URL baseUrl) {
        stammdatenTest.getById(baseUrl, "staat", 0, testProtocol);
    }

    @Test
    @InSequence(31)
    @RunAsClient
    public final void testUmweltAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.getAll(baseUrl, "umwelt", testProtocol);
    }

    @Test
    @InSequence(32)
    @RunAsClient
    public final void testUmweltById(@ArquillianResource URL baseUrl) {
        stammdatenTest.getById(baseUrl, "umwelt", "L6", testProtocol);
    }

    @Test
    @InSequence(33)
    @RunAsClient
    public final void testVerwaltungseinheitAll(@ArquillianResource URL baseUrl) {
        stammdatenTest.getAll(baseUrl, "verwaltungseinheit", testProtocol);
    }

    @Test
    @InSequence(34)
    @RunAsClient
    public final void testVerwaltungseinheitById(@ArquillianResource URL baseUrl) {
        stammdatenTest.getById(baseUrl, "verwaltungseinheit",
            "11000000", testProtocol);
    }
}
