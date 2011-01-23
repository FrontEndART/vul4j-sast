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
package org.apache.directory.shared.ldap.message.extended;


import java.nio.ByteBuffer;

import javax.naming.NamingException;
import javax.naming.ldap.ExtendedResponse;

import org.apache.directory.shared.asn1.EncoderException;
import org.apache.directory.shared.i18n.I18n;
import org.apache.directory.shared.ldap.model.message.ResultResponse;
import org.apache.directory.shared.util.exception.NotImplementedException;
import org.apache.directory.shared.ldap.codec.extended.operations.storedProcedure.StoredProcedure;
import org.apache.directory.shared.ldap.codec.extended.operations.storedProcedure.StoredProcedureContainer;
import org.apache.directory.shared.ldap.codec.extended.operations.storedProcedure.StoredProcedureDecoder;
import org.apache.directory.shared.ldap.codec.extended.operations.storedProcedure.StoredProcedure.StoredProcedureParameter;
import org.apache.directory.shared.ldap.message.ExtendedRequestImpl;
import org.apache.directory.shared.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An extended operation requesting the server to execute a stored procedure.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StoredProcedureRequest extends ExtendedRequestImpl
{
    
    /** The logger. */
    private static final Logger LOG = LoggerFactory.getLogger( StoredProcedureRequest.class );
    
    /** The serialVersionUID. */
    private static final long serialVersionUID = -4682291068700593492L;
    
    /** The OID for the stored procedure extended operation request. */
    public static final String EXTENSION_OID = "1.3.6.1.4.1.18060.0.1.6";

    /** The procedure. */
    private StoredProcedure procedure;


    /**
     * Instantiates a new stored procedure request.
     *
     * @param messageId the message id
     */
    public StoredProcedureRequest( int messageId )
    {
        super( messageId );
        this.setRequestName( EXTENSION_OID );
        this.procedure = new StoredProcedure();
    }


    /**
     * Instantiates a new stored procedure request.
     *
     * @param messageId the message id
     * @param procedure the procedure
     * @param language the language
     */
    public StoredProcedureRequest( int messageId, String procedure, String language )
    {
        super( messageId );
        this.setRequestName( EXTENSION_OID );
        this.procedure = new StoredProcedure();
        this.setLanguage( language );
        this.setProcedure( procedure );
    }


    /**
     * {@inheritDoc}
     */
    public void setRequestValue( byte[] payload )
    {
        StoredProcedureDecoder decoder = new StoredProcedureDecoder();
        StoredProcedureContainer container = new StoredProcedureContainer();

        try
        {
            decoder.decode( ByteBuffer.wrap( payload ), container );
            this.procedure = container.getStoredProcedure();
        }
        catch ( Exception e )
        {
            LOG.error( I18n.err( I18n.ERR_04165 ), e );
            throw new RuntimeException( e );
        }
    }


    /**
     * {@inheritDoc}
     */
    public ExtendedResponse createExtendedResponse( String id, byte[] berValue, int offset, int length )
        throws NamingException
    {
        StoredProcedureResponse resp = ( StoredProcedureResponse ) getResultResponse();
        resp.setResponseValue( berValue );
        resp.setResponseName( id );
        return resp;
    }


    /**
     * {@inheritDoc}
     */
    public byte[] getRequestValue()
    {
        if ( requestValue == null )
        {
            try
            {
                requestValue = procedure.encode().array();
            }
            catch ( EncoderException e )
            {
                LOG.error( I18n.err( I18n.ERR_04174 ), e );
                throw new RuntimeException( e );
            }
        }

        return requestValue;
    }


    /**
     * {@inheritDoc}
     */
    public ResultResponse getResultResponse()
    {
        if ( response == null )
        {
            StoredProcedureResponse spr = new StoredProcedureResponse( getMessageId() );
            spr.setResponseName( EXTENSION_OID );
            response = spr;
        }

        return response;
    }


    // -----------------------------------------------------------------------
    // Parameters of the Extended Request Payload
    // -----------------------------------------------------------------------

    /**
     * Gets the language.
     *
     * @return the language
     */
    public String getLanguage()
    {
        return procedure.getLanguage();
    }


    /**
     * Sets the language.
     *
     * @param language the new language
     */
    public void setLanguage( String language )
    {
        this.procedure.setLanguage( language );
    }


    /**
     * Sets the procedure.
     *
     * @param procedure the new procedure
     */
    public void setProcedure( String procedure )
    {
        this.procedure.setProcedure( Strings.getBytesUtf8(procedure) );
    }


    /**
     * Gets the procedure specification.
     *
     * @return the procedure specification
     */
    public String getProcedureSpecification()
    {
        return Strings.utf8ToString(procedure.getProcedure());
    }


    /**
     * Size.
     *
     * @return the int
     */
    public int size()
    {
        return this.procedure.getParameters().size();
    }


    /**
     * Gets the parameter type.
     *
     * @param index the index
     * @return the parameter type
     */
    public Object getParameterType( int index )
    {
        if ( !this.procedure.getLanguage().equals( "java" ) )
        {
            return procedure.getParameters().get( index ).getType();
        }

        return getJavaParameterType( index );
    }


    /**
     * Gets the java parameter type.
     *
     * @param index the index
     * @return the java parameter type
     */
    public Class<?> getJavaParameterType( int index )
    {
        throw new NotImplementedException( I18n.err( I18n.ERR_04175 ) );
    }


    /**
     * Gets the parameter value.
     *
     * @param index the index
     * @return the parameter value
     */
    public Object getParameterValue( int index )
    {
        if ( !this.procedure.getLanguage().equals( "java" ) )
        {
            return procedure.getParameters().get( index ).getValue();
        }

        return getJavaParameterValue( index );
    }


    /**
     * Gets the java parameter value.
     *
     * @param index the index
     * @return the java parameter value
     */
    public Object getJavaParameterValue( int index )
    {
        throw new NotImplementedException( I18n.err( I18n.ERR_04176 ) );
    }


    /**
     * Adds the parameter.
     *
     * @param type the type
     * @param value the value
     */
    public void addParameter( Object type, Object value )
    {
        /**
         *
         * FIXME: Why do we check here whether it's Java or not ?
         * Codec has nothing to do with these details.
         *
         if ( ! this.procedure.getLanguage().equals( "java" ) )
         {
             StoredProcedureParameter parameter = new StoredProcedureParameter();
             parameter.setType( ( byte[] ) type );
             parameter.setValue( ( byte[] ) value );
             this.procedure.addParameter( parameter );
         }
         
         * Replacing this code with the one below without the conditional check.
         
         */

        StoredProcedureParameter parameter = new StoredProcedureParameter();
        parameter.setType( ( byte[] ) type );
        parameter.setValue( ( byte[] ) value );
        this.procedure.addParameter( parameter );

        // below here try to convert parameters to their appropriate byte[] representations

        /**
         * FIXME: What is this for?
         * 
         * throw new NotImplementedException( "conversion of value to java type not implemented" );
         */
    }
}
