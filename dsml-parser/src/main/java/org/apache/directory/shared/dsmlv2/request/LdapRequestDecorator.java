/*
 *  Licensed to the Apache Software Foundation (ASF) under one
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
package org.apache.directory.shared.dsmlv2.request;


import org.apache.directory.shared.dsmlv2.LdapMessageDecorator;
import org.apache.directory.shared.ldap.model.message.Message;
import org.apache.directory.shared.ldap.message.control.Control;


/**
 * Decorator abstract class for LdapRequest
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapRequestDecorator extends LdapMessageDecorator
{
    /**
     * Creates a new instance of LdapRequestDecorator.
     *
     * @param ldapMessage the message to decorate
     */
    public LdapRequestDecorator( Message ldapMessage )
    {
        super( ldapMessage );
    }


    /**
     * {@inheritDoc}
     */
    public void addAllControls( Control[] controls )
    {
        instance.addAllControls( controls );
    }


    /**
     * {@inheritDoc}
     */
    public Object get( Object key )
    {
        return instance.get( key );
    }


    /**
     * {@inheritDoc}
     */
    public Control getCurrentControl()
    {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * {@inheritDoc}
     */
    public boolean hasControl( String oid )
    {
        // TODO Auto-generated method stub
        return false;
    }


    /**
     * {@inheritDoc}
     */
    public Object put( Object key, Object value )
    {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * {@inheritDoc}
     */
    public void removeControl( Control control )
    {
        // TODO Auto-generated method stub

    }

}
