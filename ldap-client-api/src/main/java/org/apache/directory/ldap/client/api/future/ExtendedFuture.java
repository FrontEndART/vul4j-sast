/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.directory.ldap.client.api.future;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.shared.ldap.message.Response;


/**
 * A Future to manage ExtendedRequests
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExtendedFuture extends ResponseFuture<Response>
{
    /**
     *
     * Creates a new instance of ExtendedFuture.
     *
     */
    public ExtendedFuture( LdapConnection connection, int messageId )
    {
        super( connection, messageId );
    }


    /**
     * Get the ExtendedResponse, blocking until one is received.
     *
     * @return The ExtendedResponse
     */
    public Response get() throws InterruptedException, ExecutionException
    {
        return super.get();
    }


    /**
     * Get the ExtendedResponse, blocking until one is received, or until the
     * given timeout is reached.
     *
     * @param timeout Number of TimeUnit to wait
     * @param unit The TimeUnit
     * @return The ExtendedResponse The ExtendedResponse found
     */
    public Response get( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException,
        TimeoutException
    {
        return super.get( timeout, unit );
    }


    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "ExtendedFuture" ).append( super.toString() );

        return sb.toString();
    }
}
