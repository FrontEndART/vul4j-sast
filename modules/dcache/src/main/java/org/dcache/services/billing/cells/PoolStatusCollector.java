package org.dcache.services.billing.cells;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Date;

import com.google.common.io.Files;

import diskCacheV111.poolManager.PoolManagerCellInfo;
import org.dcache.cells.CellStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import diskCacheV111.util.CacheException;
import dmg.cells.nucleus.CellPath;

/**
 * Thread run when command-line statistics call is activated. Generates a
 * statistics report file.
 */
public final class PoolStatusCollector extends Thread
{
    private static final Logger _log =
        LoggerFactory.getLogger(PoolStatusCollector.class);
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final File _report;
    private final CellStub _poolManagerStub;

    public PoolStatusCollector(CellStub poolManagerStub, File file)
    {
        _poolManagerStub = poolManagerStub;
        _report = file;
    }

    /**
     * generates report
     */
    public void run() {
        PrintWriter pw;
        try {
            pw = new PrintWriter(Files.newWriter(_report, UTF8));
        } catch (IOException ioe) {
            _log.warn("Problem opening {} : {}", _report, ioe.getMessage());
            return;
        }

        try {
            PoolManagerCellInfo info =
                _poolManagerStub.sendAndWait("xgetcellinfo",
                                             PoolManagerCellInfo.class);
            for (String path: info.getPoolList()) {
                try {
                    String s =
                        _poolManagerStub.sendAndWait(new CellPath(path),
                                                     "rep ls -s", String.class);
                    for (String line: s.split("\n")) {
                        pw.println(path + "  " + line);
                    }
                } catch (CacheException t) {
                    _log.warn("CollectPoolStatus : {}: {}", path, t.toString());
                } catch (InterruptedException t) {
                    _log.warn("CollectPoolStatus : {}: {}", path, t.toString());
                }
            }
        } catch (CacheException t) {
            _log.warn("Exception in CollectPools status : {}", t.toString());
            _report.delete();
        } catch (InterruptedException t) {
            _log.warn("Exception in CollectPools status : {}", t.toString());
            _report.delete();
        } finally {
            pw.close();
        }
    }
}
