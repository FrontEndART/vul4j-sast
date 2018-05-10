package gov.la.coastal.cims.hgms.harvester.structuregages.parser;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.la.coastal.cims.hgms.harvester.structuregages.parser.Message.MessageBuilder;
import gov.la.coastal.cims.hgms.harvester.structuregages.parser.elements.Header;
import gov.la.coastal.cims.hgms.harvester.structuregages.parser.elements.Header.HeaderBuilder;
import gov.la.coastal.cims.hgms.harvester.structuregages.parser.elements.LocationInformation;
import gov.la.coastal.cims.hgms.harvester.structuregages.parser.elements.LocationInformation.LocationInformationBuilder;
import gov.la.coastal.cims.hgms.harvester.structuregages.parser.elements.Payload;
import gov.la.coastal.cims.hgms.harvester.structuregages.parser.elements.Payload.PayloadBuilder;
import gov.la.coastal.cims.hgms.harvester.structuregages.sixbitbinary.Decode;
import gov.usgs.warc.iridium.sbd.decoder.db.entity.IridiumDataType;
import gov.usgs.warc.iridium.sbd.decoder.db.entity.IridiumDecodeOrder;

/**
 * Parse a list of {@link Byte} into usable digits from the iridium source.
 *
 * @author darceyj
 * @since Jan 5, 2018
 */
public class BinaryParser
{
	/**
	 * Class logger.
	 *
	 * @author mckelvym
	 * @since Mar 3, 2018
	 */
	private static final Logger log = LoggerFactory
			.getLogger(BinaryParser.class);

	/**
	 * Take in a byte array and return the unsigned number as a long
	 *
	 * @param p_Bytes
	 *            the byte array to use.
	 * @return an unsigned number as long
	 * @since Jan 10, 2018
	 */
	static long getAsUnsignedNumber(final byte[] p_Bytes)
	{
		long result = 0L;
		final int remaining = p_Bytes.length - 1;
		int bitShifter = remaining * 8;
		for (int i = 0; i < p_Bytes.length - 1; i++)
		{
			final int convertedInt = p_Bytes[i] & 0xFF;
			result |= convertedInt << bitShifter;
			bitShifter -= 8;
		}
		result = result | p_Bytes[remaining] & 0xFF;
		return result & 0xFFFFFFFFL;
	}

	/**
	 * Convert the byte array to string and cast to a long.
	 *
	 * @param p_ImeiByteArray
	 *            the imei byte array
	 * @return the decoded string as a long.
	 * @since Jan 11, 2018
	 */
	static long getIMEIFromBytes(final byte[] p_ImeiByteArray)
	{
		checkState(p_ImeiByteArray.length == 15,
				String.format("Expected 15 bytes for imei but got %s",
						p_ImeiByteArray.length));

		return Long.parseLong(new String(p_ImeiByteArray));

	}

	/**
	 * Parse the latitude and longitude bytes
	 *
	 * @param p_Bytes
	 *            the byte array
	 * @param p_Builder
	 *            the location information builder to use put the latitude and
	 *            longitude fields.
	 * @return the LocationInformationBuilder updated.
	 * @since Jan 12, 2018
	 */
	private static LocationInformationBuilder parseLatLonBytes(
			final byte[] p_Bytes, final LocationInformationBuilder p_Builder)
	{
		checkState(p_Bytes.length == 7,
				"Invalid byte array for location information.  Expected byte array size of 7");
		final byte b1 = p_Bytes[0];
		final String binaryStrByte1 = String
				.format("%8s", Integer.toBinaryString(b1 & 0xFF))
				.replace(' ', '0');
		/**
		 * The first 5 bits are reserved and format code should be 0 so ignoring
		 * them for now
		 *
		 * bit 6 is the north-south indicator bit bit 7 is the east-west
		 * indicator bit
		 */
		final boolean nsFlag = binaryStrByte1.charAt(6) == '1';
		final boolean ewFlag = binaryStrByte1.charAt(7) == '1';
		final LocationDirection ew = LocationDirection.fromEWIBit(ewFlag);
		final LocationDirection ns = LocationDirection.fromNSIBit(nsFlag);
		/**
		 * Unused...direction string.
		 */
		String.format("%s%s", ns.name(), ew.name());
		final ByteBuffer byteBuffer = ByteBuffer.wrap(p_Bytes, 1, 6);
		/**
		 * Byte 2 lat degs
		 */
		final byte latDegByte = byteBuffer.get();
		final short latDeg = latDegByte;
		/**
		 * Bytes 3-4 thousandth of degree as unsigned int
		 */
		byte[] dst = new byte[2];
		byteBuffer.get(dst, 0, 2);
		final int latDegDecimals = (int) getAsUnsignedNumber(dst);

		final String latDegreesStr = String.format("%s.%s", latDeg,
				latDegDecimals);
		final double latDegrees = Double.parseDouble(latDegreesStr);

		/**
		 * Byte 5 lon degrees
		 */
		final byte lonDegByte = byteBuffer.get();
		final short lonDeg = lonDegByte;

		/**
		 * Bytes 6-7 thousands of lon degrees as an unsigned integer
		 */
		dst = new byte[2];
		byteBuffer.get(dst, 0, 2);
		final int lonDegDecimals = (int) getAsUnsignedNumber(dst);
		final String lonDegreesStr = String.format("%s.%s", lonDeg,
				lonDegDecimals);
		final double lonDegrees = Double.parseDouble(lonDegreesStr);

		return p_Builder.longitude(lonDegrees).latitude(latDegrees);
	}

	/**
	 * Process the Header Information element
	 *
	 * @param p_Buffer
	 *            the {@link ByteBuffer} to use.
	 * @param p_IeLength
	 *            the element length
	 * @return an {@link Header}
	 * @since Jan 11, 2018
	 */
	private static Header processHeader(final ByteBuffer p_Buffer,
			final int p_IeLength)
	{
		checkArgument(p_Buffer != null, "The byte buffer is null.");
		final ByteBuffer bytesBuffer = p_Buffer;
		final HeaderBuilder headerBuilder = Header.builder();
		/**
		 * The first three bytes of header have already been consumed
		 */
		headerBuilder.id('1');
		headerBuilder.length((short) p_IeLength);

		/**
		 * next 4 bytes is auto id
		 */
		byte[] dst = new byte[4];
		bytesBuffer.get(dst, 0, 4);
		final long autoId = getAsUnsignedNumber(dst);

		headerBuilder.cdrId(autoId);
		/**
		 * Next 15 bytes are the IMEI as string
		 */
		final byte[] imeiArray = new byte[15];
		bytesBuffer.get(imeiArray, 0, 15);
		final long imei = getIMEIFromBytes(imeiArray);
		headerBuilder.imei(imei);
		/**
		 * Next byte is the session status as a unsigned char
		 */
		final int statusInt = bytesBuffer.get();
		headerBuilder.status(statusInt);

		/**
		 * Next two bytes are the MOMSN as unsigned short
		 */
		dst = new byte[2];
		bytesBuffer.get(dst, 0, 2);
		final int momsn = (int) getAsUnsignedNumber(dst);
		headerBuilder.momsn(momsn);

		/**
		 * Next two bytes are the MTMSN as unsigned short
		 */
		dst = new byte[2];
		bytesBuffer.get(dst, 0, 2);
		final int mtmsn = (int) getAsUnsignedNumber(dst);
		headerBuilder.mtmsn(mtmsn);
		/**
		 * Next 4 bytes are the time of session as epoch second
		 */
		dst = new byte[4];
		bytesBuffer.get(dst, 0, 4);
		final long epochTime = getAsUnsignedNumber(dst);
		headerBuilder.sessionTime(epochTime);

		return headerBuilder.build();
	}

	/**
	 * Process the location information bytes and return it in
	 * {@link LocationInformation}
	 *
	 * @param p_BytesBuffer
	 *            the buffer with the bytes in it
	 * @param p_IeLength
	 * @return a {@link LocationInformation}
	 * @since Jan 11, 2018
	 */
	private static LocationInformation processLocationInformation(
			final ByteBuffer p_BytesBuffer, final int p_IeLength)
	{
		final LocationInformationBuilder locationBuilder = LocationInformation
				.builder();
		final int bufLength = p_BytesBuffer.array().length;
		checkState(bufLength == 11, String.format(
				"Expected buffer size of 11, instead got %s", bufLength));

		locationBuilder.id((byte) 0x03);
		locationBuilder.length((short) p_IeLength);
		final byte[] latLonBytes = new byte[7];
		p_BytesBuffer.get(latLonBytes, 0, 7);
		parseLatLonBytes(latLonBytes, locationBuilder);

		final byte[] dst = new byte[4];
		p_BytesBuffer.get(dst, 0, 4);
		locationBuilder.cepRadius(getAsUnsignedNumber(dst));

		return locationBuilder.build();
	}

	/**
	 * Process the payload information element
	 *
	 * @param p_BytesBuffer
	 *            the bytesBuffer to use with the payload bytes starting at
	 *            index 0
	 * @return a {@link Payload}
	 * @since Jan 11, 2018
	 */
	private static Payload processPayload(final ByteBuffer p_BytesBuffer)
	{
		final PayloadBuilder payloadBuilder = Payload.builder();

		final byte id = 2;
		payloadBuilder.id(id);

		final int payloadByteLength = p_BytesBuffer.array().length;

		/**
		 * Skip the first 4 bytes of the payload The rest of the bytes til the
		 * end is payload bytes
		 */
		final int validPayloadLength = payloadByteLength - 4;
		payloadBuilder.length((short) validPayloadLength);
		final byte[] payLoadBytes = new byte[validPayloadLength];
		p_BytesBuffer.position(4);
		p_BytesBuffer.get(payLoadBytes, 0, validPayloadLength);
		final Byte[] dst = new Byte[validPayloadLength];
		for (int i = 0; i < validPayloadLength; i++)
		{
			dst[i] = Byte.valueOf(payLoadBytes[i]);
		}

		for (final Byte b : dst)
		{
			checkArgument(b >= 63,
					"Minimum byte value for payload must be 63. Payload is: %s",
					Arrays.toString(dst));
		}

		payloadBuilder.payload(dst);
		return payloadBuilder.build();
	}

	/**
	 * List of bytes from the directip source
	 *
	 * @since Jan 9, 2018
	 */
	private final List<Byte>					m_ByteList;

	/**
	 * Set of {@link IridiumDecodeOrder} to use when decoding the payload.
	 *
	 * @since Feb 12, 2018
	 */
	private final SortedSet<IridiumDecodeOrder>	m_DecodeOrder;

	/**
	 * The {@link Message} object
	 *
	 * @since Jan 11, 2018
	 */
	private final Message						m_Message;

	/**
	 * Default Constructor
	 *
	 * @param p_List
	 *            the list of bytes to parse.
	 * @throws Exception
	 * @since Jan 5, 2018
	 */
	public BinaryParser(final List<Byte> p_List) throws Exception
	{
		m_ByteList = Lists.newArrayList();
		checkState(!p_List.isEmpty(), "The byte list is empty");
		m_Message = parseMessage(p_List);
		m_DecodeOrder = Sets.newTreeSet();
	}

	/**
	 * Parse the list of bytes and setup the message for for use with the
	 * Decoder
	 *
	 * @return a message object parsed from the bytes in the byte list.
	 * @throws Exception
	 *             if an error occurred
	 * @since Jan 9, 2018
	 */
	private Message generateMessageFromBytes() throws Exception
	{
		final MessageBuilder msgBuilder = Message.builder();

		final ByteBuffer bytesBuffer = ByteBuffer.allocate(m_ByteList.size());
		bytesBuffer.order(ByteOrder.BIG_ENDIAN);
		m_ByteList.stream().forEachOrdered(bytesBuffer::put);

		/**
		 * Protocol number
		 */
		bytesBuffer.position(0);

		final byte revNum = bytesBuffer.get();
		/**
		 * Next two bytes is the overall message length
		 */
		byte[] dst = new byte[2];
		bytesBuffer.get(dst, 0, 2);
		final int messageLength = (int) getAsUnsignedNumber(dst);
		msgBuilder.protocolVersion(revNum);
		msgBuilder.length((char) messageLength);
		int startingByte = 3;

		Payload payload = null;
		Header header = null;
		LocationInformation locInfo = null;
		int ieLength = 0;

		while (startingByte < messageLength)
		{
			final byte byteValue = m_ByteList.get(startingByte).byteValue();

			final Optional<InformationElementIdentifiers> fromByteOpt = InformationElementIdentifiers
					.getFromByte(byteValue);
			final InformationElementIdentifiers id = fromByteOpt
					.orElseThrow(() -> new Exception(
							" Unable to process.  Invalid information element byte."));
			bytesBuffer.position(startingByte + 1);
			dst = new byte[2];
			bytesBuffer.get(dst, 0, 2);
			ieLength = (int) getAsUnsignedNumber(dst);
			dst = new byte[ieLength];
			bytesBuffer.get(dst, 0, ieLength);

			switch (id)
			{
				case CONFIRMATION:
					/**
					 * TODO may not need this since I think it is a mtmsn
					 * transmission back to the station which we aren't
					 * interested in.
					 */
					break;
				case HEADER:
					header = processHeader(ByteBuffer.wrap(dst), ieLength);

					break;
				case LOCATION_INFORMATION:
					locInfo = processLocationInformation(ByteBuffer.wrap(dst),
							ieLength);
					break;
				case PAYLOAD:
					payload = processPayload(ByteBuffer.wrap(dst));
					break;
				default:
					break;

			}
			startingByte += ieLength + 3;
		}

		/**
		 * Put all information into a new Message object and return a full
		 * message
		 */
		return msgBuilder.header(header).locationInformation(locInfo)
				.payLoad(payload).build();
	}

	/**
	 * @return the {@link Message} object
	 * @since Feb 7, 2018
	 */
	public Message getMessage()
	{
		return m_Message;
	}

	/**
	 * Parse the payload and return a map of data types and its resulting values
	 * according to the given order and data types
	 *
	 * @return a Map of {@link IridiumDataType} and its value
	 * @throws Exception
	 *             If the status code is not ok, or an error occurred during
	 *             parsing
	 * @since Jan 16, 2018
	 */
	public Map<IridiumDataType, Double> getValuesFromMessage() throws Exception
	{
		/**
		 * Map the payload bytes to the station data types. Assumes that the
		 * incoming station data types matches the payload byte order.
		 */
		/**
		 * Check the header for a success status
		 */
		final Header header = m_Message.getHeader();
		final SessionStatus sessionStatus = SessionStatus
				.getStatus(header.getStatus());
		final boolean successStatus = sessionStatus
				.equals(SessionStatus.SUCCESS);
		final boolean isBadQuality = sessionStatus
				.equals(SessionStatus.UNACCEPTABLE_QUALITY);
		final boolean okToParse = successStatus || isBadQuality;
		if (!okToParse)
		{
			throw new Exception(String.format("Unable to parse payload. %s",
					sessionStatus.getErrorMessage()));
		}
		if (isBadQuality)
		{
			log.warn(String.format(
					"Parsing message with a status of bad quality for location information."));
		}
		if (m_DecodeOrder.isEmpty())
		{
			throw new Exception(
					"Unable to parse the payload. The decode order is unknown.");
		}
		final Payload payLoad = m_Message.getPayLoad();
		final List<Byte> payloadBytes = Lists
				.newArrayList(payLoad.getPayload());
		final Map<IridiumDataType, Double> dataMap = Maps.newLinkedHashMap();
		/**
		 * Build the map of data type and its corresponding value decoded from
		 * the payload bytes.
		 */
		for (final IridiumDecodeOrder order : m_DecodeOrder)
		{
			final IridiumDataType datatype = order.getDatatype();
			final int byteLength = datatype.getBytes();
			final int startIndex = (int) order.getByteOffset();

			checkElementIndex(startIndex, payloadBytes.size(), String.format(
					"The payload (%s; size: %s) does not have enough bytes to decode '%s' starting at byte %s.",
					Arrays.toString(payloadBytes.toArray()),
					payloadBytes.size(), datatype, startIndex));

			final float value = Decode.valueAtIndex(payloadBytes, startIndex,
					byteLength, 1);
			final double transformedVal = datatype.transformValue(value);

			dataMap.put(datatype, transformedVal);
		}

		/**
		 * Clear the byte list since it has been parsed.
		 */
		m_ByteList.clear();

		m_DecodeOrder.clear();

		/**
		 * TODO for now return the data map and print the message with the data
		 * map
		 */
		log.info(MoreObjects.toStringHelper(BinaryParser.class)
				.add("Message", m_Message).add("Values", dataMap.toString())
				.toString());
		return dataMap;
	}

	/**
	 * Parse the given bytes into a {@link Message}
	 *
	 * @param p_List
	 *            the list of {@link Byte} to use
	 * @return the {@link Message}
	 * @throws Exception
	 * @since Jan 26, 2018
	 */
	private Message parseMessage(final List<Byte> p_List) throws Exception
	{
		m_ByteList.addAll(p_List);
		/**
		 * Build the message from the bytes
		 */
		return generateMessageFromBytes();
	}

	/**
	 * Set the decode order
	 *
	 * @param p_Order
	 *            the order of iridium datatypes for this message
	 * @since Feb 9, 2018
	 */
	public void setDecodeOrder(
			final SortedSet<? extends IridiumDecodeOrder> p_Order)
	{
		final Set<? extends IridiumDecodeOrder> orderSet = Objects
				.requireNonNull(p_Order);
		checkState(!orderSet.isEmpty(), "The order set is empty.");
		m_DecodeOrder.clear();
		m_DecodeOrder.addAll(p_Order);
	}
}
