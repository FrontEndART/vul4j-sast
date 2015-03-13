package de.intevation.lada;

import java.util.ArrayList;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.test.validator.Messung;
import de.intevation.lada.test.validator.Probe;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.annotation.ValidationConfig;

@RunWith(Arquillian.class)
public class LadaValidatorTest extends BaseTest {

    private static Logger logger = Logger.getLogger(LadaStammTest.class);

    @Inject
    @ValidationConfig(type="Probe")
    private Validator probeValidator;
    private Probe probeTest;

    @Inject
    @ValidationConfig(type="Messung")
    private Validator messungValidator;
    private Messung messungTest;

    public LadaValidatorTest() {
        probeTest = new Probe();
        messungTest = new Messung();
        testProtocol = new ArrayList<Protocol>();
    }

    @BeforeClass
    public static void beforeTests() {
        logger.info("---------- Testing Lada Validator ----------");
    }

    @Test
    public final void probeHasHauptprobenNr() {
        probeTest.setValidator(probeValidator);
        probeTest.hasHauptprobenNr(testProtocol);
    }

    @Test
    public final void probeHasNoHauptprobenNr() {
        probeTest.setValidator(probeValidator);
        probeTest.hasNoHauptprobenNr(testProtocol);
    }

    @Test
    public final void probeExistingHauptprobenNrNew() {
        probeTest.setValidator(probeValidator);
        probeTest.existingHauptprobenNrNew(testProtocol);
    }

    @Test
    public final void probeUniqueHauptprobenNrNew() {
        probeTest.setValidator(probeValidator);
        probeTest.uniqueHauptprobenNrNew(testProtocol);
    }

    @Test
    public final void probeExistingHauptprobenNrUpdate() {
        probeTest.setValidator(probeValidator);
        probeTest.existingHauptprobenNrUpdate(testProtocol);
    }

    @Test
    public final void probeUniqueHauptprobenNrUpdate() {
        probeTest.setValidator(probeValidator);
        probeTest.uniqueHauptprobenNrUpdate(testProtocol);
    }

    @Test
    public final void probeHasEntnahmeOrt() {
        probeTest.setValidator(probeValidator);
        probeTest.hasEntnahmeOrt(testProtocol);
    }

    @Test
    public final void probeHasNoEntnahmeOrt() {
        probeTest.setValidator(probeValidator);
        probeTest.hasNoEntnahmeOrt(testProtocol);
    }

    @Test
    public final void probeHasProbenahmeBegin() {
        probeTest.setValidator(probeValidator);
        probeTest.hasProbeentnahmeBegin(testProtocol);
    }

    @Test
    public final void probeHasNoProbenahmeBegin() {
        probeTest.setValidator(probeValidator);
        probeTest.hasNoProbeentnahmeBegin(testProtocol);
    }

    @Test
    public final void probeTimeNoEndProbenahmeBegin() {
        probeTest.setValidator(probeValidator);
        probeTest.timeNoEndProbeentnahmeBegin(testProtocol);
    }

    @Test
    public final void probeTimeNoBeginProbenahmeBegin() {
        probeTest.setValidator(probeValidator);
        probeTest.timeNoBeginProbeentnahmeBegin(testProtocol);
    }

    @Test
    public final void probeTimeBeginAfterEndProbenahmeBegin() {
        probeTest.setValidator(probeValidator);
        probeTest.timeBeginAfterEndProbeentnahmeBegin(testProtocol);
    }

    @Test
    public final void probeTimeBeginFutureProbenahmeBegin() {
        probeTest.setValidator(probeValidator);
        probeTest.timeBeginFutureProbeentnahmeBegin(testProtocol);
    }

    @Test
    public final void probeHasUmwelt() {
        probeTest.setValidator(probeValidator);
        probeTest.hasUmwelt(testProtocol);
    }

    @Test
    public final void probeHasNoUmwelt() {
        probeTest.setValidator(probeValidator);
        probeTest.hasNoUmwelt(testProtocol);
    }

    @Test
    public final void probeHasEmptyUmwelt() {
        probeTest.setValidator(probeValidator);
        probeTest.hasEmptyUmwelt(testProtocol);
    }

    @Test
    public final void messungHasNebenprobenNr() {
        messungTest.setValidator(messungValidator);
        messungTest.hasNebenprobenNr(testProtocol);
    }

    @Test
    public final void messungHasNoNebenprobenNr() {
        messungTest.setValidator(messungValidator);
        messungTest.hasNoNebenprobenNr(testProtocol);
    }

    @Test
    public final void messungHasEmptyNebenprobenNr() {
        messungTest.setValidator(messungValidator);
        messungTest.hasEmptyNebenprobenNr(testProtocol);
    }

    @Test
    public final void messungUniqueNebenprobenNrNew() {
        messungTest.setValidator(messungValidator);
        messungTest.uniqueNebenprobenNrNew(testProtocol);
    }

    @Test
    public final void messungUniqueNebenprobenNrUpdate() {
        messungTest.setValidator(messungValidator);
        messungTest.uniqueNebenprobenNrUpdate(testProtocol);
    }

    @Test
    public final void messungExistingNebenprobenNrNew() {
        messungTest.setValidator(messungValidator);
        messungTest.existingNebenprobenNrNew(testProtocol);
    }

    @Test
    public final void messungExistingNebenprobenNrUpdate() {
        messungTest.setValidator(messungValidator);
        messungTest.existingHauptprobenNrUpdate(testProtocol);
    }

    @Test
    public final void messungHasMesswert() {
        messungTest.setValidator(messungValidator);
        messungTest.hasMesswert(testProtocol);
    }

    @Test
    public final void messungHasNoMesswert() {
        messungTest.setValidator(messungValidator);
        messungTest.hasNoMesswert(testProtocol);
    }
}
