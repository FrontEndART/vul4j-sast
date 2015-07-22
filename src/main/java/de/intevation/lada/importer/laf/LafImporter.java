/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.laf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.intevation.lada.importer.ImportConfig;
import de.intevation.lada.importer.ImportFormat;
import de.intevation.lada.importer.Importer;
import de.intevation.lada.importer.ReportItem;
import de.intevation.lada.util.auth.UserInfo;

/**
 * LAF importer implements Importer to read LAF formatted files.
 * The importer parses the files and extracts probe objects and their children
 * and persists them in the database.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ImportConfig(format=ImportFormat.LAF)
public class LafImporter implements Importer {

    /**
     * The parser used for this importer.
     */
    @Inject
    private LafParser parser;

    private Map<String, List<ReportItem>> warnings;
    private Map<String, List<ReportItem>> errors;

    /**
     * Default constructor.
     */
    public LafImporter() {
        warnings = new HashMap<String, List<ReportItem>>();
        errors = new HashMap<String, List<ReportItem>>();
    }

    /**
     * @return the warnings
     */
    @Override
    public Map<String, List<ReportItem>> getWarnings() {
        return warnings;
    }

    /**
     * @return the errors
     */
    @Override
    public Map<String, List<ReportItem>> getErrors() {
        return errors;
    }

    /**
     * Reset the errors and warnings. Use this before calling doImport()
     * to have a clean error and warning report.
     */
    @Override
    public void reset() {
        parser.reset();
        warnings = new HashMap<String, List<ReportItem>>();
        errors = new HashMap<String, List<ReportItem>>();
    }

    /**
     * Start the import.
     *
     * @param content   The laf data as string.
     * @param userInfo  The user information.
     */
    @Override
    public void doImport(String content, UserInfo userInfo) {
        this.warnings.clear();
        this.errors.clear();
        this.parser.reset();
        boolean success = parser.parse(userInfo, content);
        if (!success) {
                List<ReportItem> report = new ArrayList<ReportItem>();
                report.add(new ReportItem("parser", "no success", 660));
                errors.put("parser", report);
                warnings.put("parser", new ArrayList<ReportItem>());
        }
        this.warnings.putAll(this.parser.getWarnings());
        this.errors.putAll(this.parser.getErrors());
    }
}
