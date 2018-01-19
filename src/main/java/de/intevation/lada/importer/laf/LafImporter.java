package de.intevation.lada.importer.laf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.log4j.Logger;

import de.intevation.lada.importer.ImportConfig;
import de.intevation.lada.importer.ImportFormat;
import de.intevation.lada.importer.Importer;
import de.intevation.lada.importer.ReportItem;
import de.intevation.lada.model.stammdaten.ImporterConfig;
import de.intevation.lada.util.auth.UserInfo;

@ImportConfig(format=ImportFormat.LAF)
public class LafImporter implements Importer{

    @Inject
    private Logger logger;

    @Inject
    private LafObjectMapper mapper;

    private Map<String, List<ReportItem>> errors = new HashMap<String, List<ReportItem>>();
    private Map<String, List<ReportItem>> warnings = new HashMap<String, List<ReportItem>>();

    public void doImport(String lafString, UserInfo userInfo, List<ImporterConfig> config) {
        errors = new HashMap<String, List<ReportItem>>();
        warnings = new HashMap<String, List<ReportItem>>();

        InputStream is = new ByteArrayInputStream(lafString.getBytes(StandardCharsets.UTF_8));
        try {
            ANTLRInputStream ais = new ANTLRInputStream(is);
            LafLexer lexer = new LafLexer(ais);
            CommonTokenStream cts = new CommonTokenStream(lexer);
            LafParser parser = new LafParser(cts);
            LafErrorListener errorListener = LafErrorListener.INSTANCE;
            errorListener.reset();
            parser.addErrorListener(errorListener);
            ParseTree tree = parser.probendatei();
            LafObjectListener listener = new LafObjectListener();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(listener, tree);
            List<ReportItem> items = new ArrayList<ReportItem>();
            if (!listener.hasUebertragungsformat()) {
                items.add(new ReportItem("missing header", "format", 673));
            }
            if (!listener.hasVersion()) {
                items.add(new ReportItem("missing header", "version", 673));
            }
            if (!items.isEmpty()) {
                warnings.put("parser", items);
            }
            if (!errorListener.getErrors().isEmpty()) {
                errors.put("parser", errorListener.getErrors());
                return;
            }
            errors.putAll(listener.getErrors());
            warnings.putAll(listener.getWarnings());
            mapper.setUserInfo(userInfo);
            mapper.setConfig(config);
            mapper.mapObjects(listener.getData());
            for (Entry<String, List<ReportItem>> entry : mapper.getErrors().entrySet()) {
                if (errors.containsKey(entry.getKey())) {
                    errors.get(entry.getKey()).addAll(entry.getValue());
                }
                else {
                    errors.put(entry.getKey(), entry.getValue());
                }
            }

            for (Entry<String, List<ReportItem>> entry : mapper.getWarnings().entrySet()) {
                if (warnings.containsKey(entry.getKey())) {
                    warnings.get(entry.getKey()).addAll(entry.getValue());
                }
                else {
                    warnings.putAll(mapper.getWarnings());
                }
            }
        } catch (IOException e) {
            logger.debug("Exception while reading LAF input", e);
        }
    }

    @Override
    public void reset() {

    }

    @Override
    public Map<String, List<ReportItem>> getErrors() {
        return this.errors;
    }

    @Override
    public Map<String, List<ReportItem>> getWarnings() {
        return this.warnings;
    }
}
