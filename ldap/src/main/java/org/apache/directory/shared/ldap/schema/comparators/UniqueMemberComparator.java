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
package org.apache.directory.shared.ldap.schema.comparators;


import org.apache.directory.shared.i18n.I18n;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.name.Dn;
import org.apache.directory.shared.ldap.schema.LdapComparator;
import org.apache.directory.shared.ldap.schema.SchemaManager;


/**
 * A comparator that sorts OIDs based on their numeric id value.  Needs a 
 * OidRegistry to properly do it's job.  Public method to set the oid 
 * registry will be used by the server after instantiation in deserialization.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class UniqueMemberComparator extends LdapComparator<String>
{
    /** The serialVersionUID */
    private static final long serialVersionUID = 1L;

    /** A reference to the schema manager */
    private transient SchemaManager schemaManager;


    /**
     * The IntegerComparator constructor. Its OID is the IntegerOrderingMatch matching
     * rule OID.
     */
    public UniqueMemberComparator( String oid )
    {
        super( oid );
    }


    /**
     * Implementation of the Compare method
     */
    public int compare( String dnstr0, String dnstr1 )
    {
        int dash0 = dnstr0.lastIndexOf( '#' );
        int dash1 = dnstr1.lastIndexOf( '#' );

        if ( ( dash0 == -1 ) && ( dash1 == -1 ) )
        {
            // no UID part
            try
            {
                return getDn( dnstr0 ).compareTo( getDn( dnstr1 ) );
            }
            catch ( LdapInvalidDnException ne )
            {
                return -1;
            }
        }
        else
        {
            // Now, check that we don't have another '#'
            if ( dnstr0.indexOf( '#' ) != dash0 )
            {
                // Yes, we have one : this is not allowed, it should have been
                // escaped.
                return -1;
            }

            if ( dnstr1.indexOf( '#' ) != dash0 )
            {
                // Yes, we have one : this is not allowed, it should have been
                // escaped.
                return 1;
            }

            Dn dn0 = null;
            Dn dn1 = null;

            // This is an UID if the '#' is immediatly
            // followed by a BitString, except if the '#' is
            // on the last position
            String uid0 = dnstr0.substring( dash0 + 1 );

            if ( dash0 > 0 )
            {
                try
                {
                    dn0 = new Dn( dnstr0.substring( 0, dash0 ) );
                }
                catch ( LdapException ne )
                {
                    return -1;
                }
            }
            else
            {
                return -1;
            }

            // This is an UID if the '#' is immediatly
            // followed by a BitString, except if the '#' is
            // on the last position
            String uid1 = dnstr1.substring( dash1 + 1 );

            if ( dash1 > 0 )
            {
                try
                {
                    dn1 = new Dn( dnstr0.substring( 0, dash1 ) );
                }
                catch ( LdapException ne )
                {
                    return 1;
                }
            }
            else
            {
                return 1;
            }

            int dnComp = dn0.compareTo( dn1 );

            if ( dnComp != 0 )
            {
                return dnComp;
            }

            return uid0.compareTo( uid1 );
        }
    }


    /**
     * {@inheritDoc}
     */
    public void setSchemaManager( SchemaManager schemaManager )
    {
        this.schemaManager = schemaManager;
    }


    public Dn getDn( Object obj ) throws LdapInvalidDnException
    {
        Dn dn = null;

        if ( obj instanceof Dn)
        {
            dn = (Dn) obj;

            dn = ( dn.isNormalized() ? dn : Dn.normalize(dn, schemaManager.getNormalizerMapping()) );
        }
        else if ( obj instanceof String )
        {
            dn = new Dn( ( String ) obj, schemaManager );
        }
        else
        {
            throw new IllegalStateException( I18n.err( I18n.ERR_04218, ( obj == null ? null : obj.getClass() ) ) );
        }

        return dn;
    }
}
