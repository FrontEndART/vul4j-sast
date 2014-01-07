package net.onrc.onos.ofcontroller.util.serializers;

import java.io.IOException;

import net.onrc.onos.ofcontroller.util.IPv4;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deserialize an IPv4 address from a string.
 */
public class IPv4Deserializer extends JsonDeserializer<IPv4> {

    protected final static Logger log = LoggerFactory.getLogger(IPv4Deserializer.class);

    @Override
    public IPv4 deserialize(JsonParser jp,
			    DeserializationContext ctxt)
	throws IOException, JsonProcessingException {

	IPv4 ipv4 = null;

	jp.nextToken();		// Move to JsonToken.START_OBJECT
	while (jp.nextToken() != JsonToken.END_OBJECT) {
	    String fieldname = jp.getCurrentName();
	    if ("value".equals(fieldname)) {
		String value = jp.getText();
		log.debug("Fieldname: {} Value: {}", fieldname, value);
		ipv4 = new IPv4(value);
	    }
	}
	return ipv4;
    }
}
