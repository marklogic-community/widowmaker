/*
 * Copyright 2003-2016 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.stress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.security.MessageDigest;
import java.math.BigInteger;

public class EvalLoadTester extends RestLoadTester {
    private int batchSize = 50;
    private List<String> uris         = new ArrayList<String>();
    private List<byte[]> contents     = new ArrayList<byte[]>();
    private List<String> collections  = new ArrayList<String>();

    public EvalLoadTester(ConnectionData connData, LoadTestData loadData,
            String threadName) {
        super(connData, loadData, threadName);
        //System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "debug");
    }

    private void resetLists() {
        uris         = new ArrayList<String>();
        contents     = new ArrayList<byte[]>();
        collections  = new ArrayList<String>();
    }

    @Override
    protected void putDocument(String uri, String collection, byte[] content, Object transaction) throws Exception {
        uris.add(uri);
        collections.add(collection);
        contents.add(content);
    }

    @Override
    protected void verifyLoaded(int numLoaded, List<String> uris) throws Exception {
        long count = getSession().countDocumentsViaEval(null, uris.toArray(new String[] {}));
        if ( count != numLoaded) {
            message("ERROR (EvalLoadTester.verifyLoaded) found " + count +
                " loaded but should have found " + numLoaded + " of these uris: " + uris);
            System.err.println("Exiting...");
            alive = false;
        }
    }

    /** @return number of documents loaded */
    @Override
    protected int finishBatch(List<String> uris, boolean rollback, Object transaction) throws Exception {
        try {
            if ( uris.size() > 0 ) {
                getSession().putDocumentsViaEval(uris, null, contents, collections, transaction);
            }
            if (!rollback) {
                commitRestTransaction(transaction);
                return uris.size(); // num pojos loaded
            } else {
                rollbackRestTransaction(transaction);
                return 0; // none loaded
            }
        } finally {
            resetLists();
        }
    }
}
