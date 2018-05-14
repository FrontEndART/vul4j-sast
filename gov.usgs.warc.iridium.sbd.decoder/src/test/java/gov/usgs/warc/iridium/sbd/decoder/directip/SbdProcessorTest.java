package gov.usgs.warc.iridium.sbd.decoder.directip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.primitives.Bytes;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import gov.usgs.warc.iridium.sbd.decoder.ParsingTestsHelper;
import gov.usgs.warc.iridium.sbd.decoder.Tests;
import gov.usgs.warc.iridium.sbd.domain.SbdDecodeOrderProvider;
import gov.usgs.warc.iridium.sbd.domain.SbdStationIdProvider;
import gov.usgs.warc.iridium.sbd.domain.SbdDataType;
import gov.usgs.warc.iridium.sbd.domain.SbdDecodeOrder;
import gov.usgs.warc.iridium.sbd.domain.SbdStationId;
import gov.usgs.warc.iridium.sbd.decoder.directip.IridiumResponse;
import gov.usgs.warc.iridium.sbd.decoder.directip.SbdProcessor;
import gov.usgs.warc.iridium.sbd.decoder.directip.SbdProcessorImpl;
import gov.usgs.warc.iridium.sbd.decoder.parser.BinaryParser;

/**
 * Test the {@link SbdProcessor}
 *
 * @author darceyj
 * @since Feb 12, 2018
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration
@ActiveProfiles("test")
public class SbdProcessorTest
{

	/**
	 * @author darceyj
	 * @since Feb 12, 2018
	 *
	 */
	@Configuration
	static class ContextConfiguration
	{
		/**
		 * @param p_Context
		 *            {@link ApplicationContext}
		 * @param p_IridiumStationIdRepository
		 *            {@link IridiumStationIdRepository}
		 * @param p_IridiumDecodeOrderRepository
		 *            {@link IridiumDecodeOrderRepository}
		 * @return {@link SbdProcessorImpl} instance
		 * @author darceyj
		 * @since Feb 12, 2018
		 */
		@Bean
		@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
		@Primary
		public SbdProcessor sbdProcessor(final ApplicationContext p_Context,
				final SbdStationIdProvider<SbdStationId> p_IridiumStationIdRepository,
				final SbdDecodeOrderProvider<SbdDecodeOrder> p_IridiumDecodeOrderRepository)
		{
			return new SbdProcessorImpl(p_Context, p_IridiumStationIdRepository,
					p_IridiumDecodeOrderRepository);
		}

	}

	/**
	 * Assert that the required test methods are present
	 *
	 * @throws java.lang.Exception
	 * @since Feb 12, 2018
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		final Class<?> classToTest = SbdProcessor.class;
		final Class<?> testingClass = SbdProcessorTest.class;
		Tests.assertHasRequiredMethods(classToTest, testingClass);

	}

	/**
	 * The decode order repository bean
	 *
	 * @since Feb 12, 2018
	 */

	@MockBean
	private SbdDecodeOrderProvider<SbdDecodeOrder>	m_DecodeOrderRepository;

	/**
	 * The {@link IridiumStationIdRepository}
	 *
	 * @since Feb 12, 2018
	 */
	@MockBean
	private SbdStationIdProvider<SbdStationId>		m_IridiumStationRepository;

	/**
	 * The station ID to test with
	 *
	 * @since May 9, 2018
	 */
	private Long											m_StationIdTest;

	/**
	 * The {@link SbdProcessor} to test with.
	 *
	 * @since Feb 12, 2018
	 */
	@Autowired
	private SbdProcessor									m_Testable;

	/**
	 * @author mckelvym
	 * @since Mar 30, 2018
	 */
	private Map<List<Byte>, IridiumResponse>				m_TestingData;

	/**
	 * @throws Exception
	 * @author mckelvym
	 * @since Mar 30, 2018
	 */
	@Before
	public void setUp() throws Exception
	{
		m_TestingData = Maps.newLinkedHashMap();

		m_StationIdTest = 1L;
		final String imei = "300234010124740";
		final SbdStationId iridiumStationId = new SbdStationId()
		{

			@Override
			public Long getId()
			{
				return 221L;
			}

			@Override
			public String getImei()
			{
				return imei;
			}

			@Override
			public Long getStationId()
			{
				return m_StationIdTest;
			}
		};

		when(m_IridiumStationRepository.findByImei(imei))
				.thenReturn(Lists.newArrayList(iridiumStationId));
		when(m_DecodeOrderRepository.findByStationId(m_StationIdTest))
				.thenReturn(
						Sets.newTreeSet(ParsingTestsHelper.getDecodeList()));

		final List<List<Byte>> inputByteLists = Lists.newArrayList();
		inputByteLists.add(ParsingTestsHelper.setupMessageBytes("00"));
		for (final List<Byte> inputBytes : inputByteLists)
		{
			final BinaryParser parser = new BinaryParser(inputBytes);

			final Collection<SbdStationId> stationsList = m_IridiumStationRepository
					.findByImei(String.valueOf(
							parser.getMessage().getHeader().getImei()));
			final Optional<SbdStationId> opt = stationsList.stream()
					.findFirst();
			assertThat(opt).isNotEqualTo(Optional.empty());
			final Long stationId = opt.get().getStationId();

			parser.setDecodeOrder(
					m_DecodeOrderRepository.findByStationId(stationId));
			final Map<SbdDataType, Double> dataMap = parser
					.getValuesFromMessage();
			final Table<SbdStationId, SbdDataType, Double> valueTable = HashBasedTable
					.create();
			dataMap.forEach((datatype, value) -> valueTable
					.put(iridiumStationId, datatype, value));
			final IridiumResponse output = IridiumResponse.builder()
					.message(parser.getMessage()).stations(stationsList)
					.values(valueTable).build();
			m_TestingData.put(inputBytes, output);
		}
	}

	/**
	 * Test method for
	 * {@link gov.usgs.warc.iridium.sbd.decoder.directip.SbdProcessorImpl#process(byte[], java.util.function.Consumer)}.
	 */
	@Test
	public void testProcess()
	{
		for (final Entry<List<Byte>, IridiumResponse> entry : m_TestingData
				.entrySet())
		{
			final List<Byte> input = entry.getKey();
			final IridiumResponse expected = entry.getValue();
			try
			{
				final Optional<IridiumResponse> response = m_Testable
						.process(Bytes.toArray(input), null);
				assertThat(response.get())
						.isEqualToComparingFieldByField(expected);
			}
			catch (final Exception e)
			{
				fail(String.format(
						"Unable to parse the values from the binary directip message.  %s",
						Arrays.toString(e.getStackTrace())));
			}
		}
	}

	/**
	 * Test method for
	 * {@link gov.usgs.warc.iridium.sbd.decoder.directip.SbdProcessorImpl#process(byte[], java.util.function.Consumer)}.
	 */
	@Test
	public void testProcessBadOrShortPayload()
	{
		final List<List<Byte>> testingData = Lists.newArrayList();
		testingData.addAll(ParsingTestsHelper.getTestingDataBadPayload());
		testingData.addAll(ParsingTestsHelper.getTestingDataShortPayload());

		for (final List<Byte> bytes : testingData)
		{
			final AtomicReference<Throwable> t = new AtomicReference<>();
			assertThat(m_Testable.process(Bytes.toArray(bytes), t::set))
					.isEqualTo(Optional.empty());
			assertThat(t.get()).isNotNull();
		}
	}

}
