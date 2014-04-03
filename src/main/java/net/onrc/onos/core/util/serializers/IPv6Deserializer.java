package net.onrc.onos.core.util.serializers;

import java.io.IOException;

import net.onrc.onos.core.util.IPv6;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deserialize an IPv6 address from a string.
 */
public class IPv6Deserializer extends JsonDeserializer<IPv6> {

    protected final static Logger log = LoggerFactory.getLogger(IPv6Deserializer.class);

    @Override
    public IPv6 deserialize(JsonParser jp,
			    DeserializationContext ctxt)
	throws IOException, JsonProcessingException {

	IPv6 ipv6 = null;

	jp.nextToken();		// Move to JsonToken.START_OBJECT
	while (jp.nextToken() != JsonToken.END_OBJECT) {
	    String fieldname = jp.getCurrentName();
	    if ("value".equals(fieldname)) {
		String value = jp.getText();
		log.debug("Fieldname: {} Value: {}", fieldname, value);
		ipv6 = new IPv6(value);
	    }
	}
	return ipv6;
    }
}
