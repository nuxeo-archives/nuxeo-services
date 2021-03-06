/*
 * (C) Copyright 2006-2012 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo
 *     Florent Guillaume
 *     Thierry Delprat
 */
package org.nuxeo.ecm.platform.convert.tests;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.core.convert.api.ConverterCheckResult;
import org.nuxeo.ecm.platform.convert.plugins.JODBasedConverter;
import org.nuxeo.runtime.api.Framework;

public class TestAnyToPDFConverters extends BaseConverterTest {

    private static final Log log = LogFactory.getLog(TestAnyToPDFConverters.class);

    protected void doTestPDFConverter(String srcMT, String fileName)
            throws Exception {
        doTestPDFConverter(srcMT, fileName, false); // normal PDF
        doTestPDFConverter(srcMT, fileName, true); // PDF/A-1
    }

    protected String doTestPDFConverter(String srcMT, String fileName,
            boolean pdfa) throws Exception {
        return doTestPDFConverter(srcMT, fileName, pdfa, false);
    }

    protected String doTestPDFConverter(String srcMT, String fileName,
            boolean pdfa, boolean updateIndex) throws Exception {

        ConversionService cs = Framework.getLocalService(ConversionService.class);

        String converterName = cs.getConverterName(srcMT, "application/pdf");
        assertEquals("any2pdf", converterName);

        ConverterCheckResult check = cs.isConverterAvailable(converterName);
        assertNotNull(check);
        if (!check.isAvailable()) {
            log.warn("Skipping JOD based converter tests since OOo is not installed");
            log.warn("  converter check output : "
                    + check.getInstallationMessage());
            log.warn("  converter check output : " + check.getErrorMessage());
            return null;
        }

        BlobHolder hg = getBlobFromPath("test-docs/" + fileName, srcMT);

        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        if (pdfa) {
            parameters.put(JODBasedConverter.PDFA1_PARAM, Boolean.TRUE);
        }
        if (updateIndex) {
            parameters.put(JODBasedConverter.UPDATE_INDEX_PARAM, Boolean.TRUE);
        }
        BlobHolder result = cs.convert(converterName, hg, parameters);
        assertNotNull(result);

        File pdfFile = File.createTempFile("testingPDFConverter", ".pdf");
        String text = null;
        try {
            result.getBlob().transferTo(pdfFile);
            text = readPdfText(pdfFile);
            assertTrue(text.contains("Hello") || text.contains("hello"));
            if (pdfa) {
                assertTrue("Output is not PDF/A", isPDFA(pdfFile));
            }
            return text;
        } finally {
            pdfFile.delete();
        }
    }

    @Test
    public void testAnyToPDFConverter() throws Exception {
        ConversionService cs = Framework.getLocalService(ConversionService.class);
        ConverterCheckResult check = cs.isConverterAvailable("any2pdf");
        assertNotNull(check);
        if (!check.isAvailable()) {
            log.warn("Skipping JOD based converter tests since OOo is not installed");
            log.warn("  converter check output : "
                    + check.getInstallationMessage());
            log.warn("  converter check output : " + check.getErrorMessage());
            return;
        }

        doTestPDFConverter("text/html", "hello.html");
//        doTestPDFConverter("text/xml", "hello.xml");
        doTestPDFConverter("application/vnd.ms-excel", "hello.xls");
        doTestPDFConverter("application/vnd.sun.xml.writer", "hello.sxw");
        doTestPDFConverter("application/vnd.oasis.opendocument.text",
                "hello.odt");
        doTestPDFConverter("application/vnd.sun.xml.calc", "hello.sxc");
        doTestPDFConverter("application/vnd.oasis.opendocument.spreadsheet",
                "hello.ods");
        doTestPDFConverter("application/vnd.sun.xml.impress", "hello.sxi");
        doTestPDFConverter("application/vnd.oasis.opendocument.presentation",
                "hello.odp");

        doTestPDFConverter(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "hello.docx");
        doTestPDFConverter(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "hello.xlsx");
        doTestPDFConverter(
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "hello.pptx");

    }

    @Test
    public void testAnyToPDFConverterWithToc() throws Exception {
        ConversionService cs = Framework.getLocalService(ConversionService.class);
        ConverterCheckResult check = cs.isConverterAvailable("any2pdf");
        assertNotNull(check);
        if (!check.isAvailable()) {
            log.warn("Skipping JOD based converter tests since OOo is not installed");
            log.warn("  converter check output : "
                    + check.getInstallationMessage());
            log.warn("  converter check output : " + check.getErrorMessage());
            return;
        }

        // generate without TOC
        String textContent = doTestPDFConverter(
                "application/vnd.oasis.opendocument.text", "toc.odt", false,
                false);
        // check that there is no TOC generated
        assertFalse(textContent.contains("..........."));

        // generate with TOC
        textContent = doTestPDFConverter(
                "application/vnd.oasis.opendocument.text", "toc.odt", false,
                true);
        assertTrue(textContent.contains("..........."));

    }

    protected class ConversionThread extends Thread {

        boolean exception = false;

        boolean terminated = false;

        @Override
        public void run() {

            try {
                testAnyToPDFConverter();
            } catch (Exception e) {
                exception = false;
            } finally {
                terminated = true;
            }

        }
    }

    @Test
    public void testMultiThreadsConverter() throws Exception {

        int t = 0;
        int tMax = 120;
        ConversionThread t1 = new ConversionThread();
        ConversionThread t2 = new ConversionThread();

        t1.start();
        t2.start();

        while (!(t1.terminated && t2.terminated)) {
            Thread.sleep(1000);
            t += 1;
            if (t > tMax) {
                if (!t1.terminated) {
                    t1.interrupt();
                }
                if (!t2.terminated) {
                    t2.interrupt();
                }
                break;
            }
        }

        assertFalse(t1.exception);
        assertFalse(t2.exception);
    }

}
