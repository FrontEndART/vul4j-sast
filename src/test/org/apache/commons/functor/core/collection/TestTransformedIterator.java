/* 
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons-sandbox//functor/src/test/org/apache/commons/functor/core/collection/TestTransformedIterator.java,v 1.3 2003/12/02 17:06:30 rwaldhoff Exp $
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived 
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.commons.functor.core.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.functor.BaseFunctorTest;
import org.apache.commons.functor.UnaryFunction;
import org.apache.commons.functor.core.Identity;

/**
 * @version $Revision: 1.3 $ $Date: 2003/12/02 17:06:30 $
 * @author Rodney Waldhoff
 */
public class TestTransformedIterator extends BaseFunctorTest {

    // Conventional
    // ------------------------------------------------------------------------

    public TestTransformedIterator(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestTransformedIterator.class);
    }
    
    public Object makeFunctor() {
        List list = new ArrayList();
        list.add("xyzzy");        
        return TransformedIterator.transform(list.iterator(),Identity.instance());
    }

    // Lifecycle
    // ------------------------------------------------------------------------

    public void setUp() throws Exception {
        super.setUp();
        list = new ArrayList();
        negatives = new ArrayList();
        for(int i=0;i<10;i++) {
            list.add(new Integer(i));
            negatives.add(new Integer(i*-1));
        }
    }

    public void tearDown() throws Exception {
        super.tearDown();
        list = null;
        negatives = null;
    }

    // Tests
    // ------------------------------------------------------------------------
    
    public void testBasicTransform() {
        Iterator expected = negatives.iterator();
        Iterator testing = new TransformedIterator(list.iterator(),negate);
        while(expected.hasNext()) {
            assertTrue(testing.hasNext());
            assertEquals(expected.next(),testing.next());
        }
        assertTrue(!testing.hasNext());
    }

    public void testEmptyList() {
        Iterator testing = new TransformedIterator(Collections.EMPTY_LIST.iterator(),negate);
        assertTrue(!testing.hasNext());
    }

    public void testNextWithoutHasNext() {
        Iterator testing = new TransformedIterator(list.iterator(),negate);
        Iterator expected = negatives.iterator();
        while(expected.hasNext()) {
            assertEquals(expected.next(),testing.next());
        }
        assertTrue(!(testing.hasNext()));
    }

    public void testNextAfterEndOfList() {
        Iterator testing = new TransformedIterator(list.iterator(),negate);
        Iterator expected = negatives.iterator();
        while(expected.hasNext()) {
            assertEquals(expected.next(),testing.next());
        }
        try {
            testing.next();
            fail("ExpectedNoSuchElementException");
        } catch(NoSuchElementException e) {
            // expected
        }
    }

    public void testNextOnEmptyList() {
        Iterator testing = new TransformedIterator(Collections.EMPTY_LIST.iterator(),negate);
        try {
            testing.next();
            fail("ExpectedNoSuchElementException");
        } catch(NoSuchElementException e) {
            // expected
        }
    }
    
    public void testRemoveBeforeNext() {
        Iterator testing = new TransformedIterator(list.iterator(),negate);
        try {
            testing.remove();
            fail("IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }
    
    public void testRemoveAfterNext() {
        Iterator testing = new TransformedIterator(list.iterator(),negate);
        testing.next();
        testing.remove();
        try {
            testing.remove();
            fail("IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }
    
    public void testRemoveAll() {
        Iterator testing = new TransformedIterator(list.iterator(),negate);
        while(testing.hasNext()) {
            testing.next();
            testing.remove();
        }
        assertTrue(list.isEmpty());
    }

    public void testRemoveWithoutHasNext() {
        Iterator testing = new TransformedIterator(list.iterator(),negate);
        for(int i=0,m = list.size();i<m;i++) {
            testing.next();
            testing.remove();
        }
        assertTrue(list.isEmpty());
    }
    
    public void testTransformWithNullIteratorReturnsNull() {
        assertNull(TransformedIterator.transform(null,negate));
    }
    
    public void testTransformWithNullPredicateReturnsIdentity() {
        Iterator iter = list.iterator();
        assertSame(iter,TransformedIterator.transform(iter,null));
    }

    public void testConstructorProhibitsNull() {
        try {
            new TransformedIterator(null,null);
            fail("ExpectedNullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
        try {
            new TransformedIterator(null,negate);
            fail("ExpectedNullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
        try {
            new TransformedIterator(list.iterator(),null);
            fail("ExpectedNullPointerException");
        } catch(NullPointerException e) {
            // expected
        }
    }
    

    // Attributes
    // ------------------------------------------------------------------------
    private List list = null;    
    private List negatives = null;
    private UnaryFunction negate = new UnaryFunction() { 
        public Object evaluate(Object obj) {
            return new Integer(((Number)obj).intValue() * -1);
        }
    };
    
}
