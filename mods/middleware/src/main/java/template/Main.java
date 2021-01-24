package template;

import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.command.RemoveImageCmd;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.LocalNodeState;
import com.github.dockerjava.api.model.PruneType;
import com.github.dockerjava.api.model.SwarmSpec;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.val;
import org.slf4j.LoggerFactory;

public interface Main {

  /**
   * Builds middleware assets according to a given environment. Main purpose is
   * to create or refresh all required resources for running an application.
   *
   * @param args key-value entries treated by {@link Props#from(String...)}.
   */
  static void main(final String[] args) {
    val log = LoggerFactory.getLogger(Main.class);
    val props = Props.from(args);
    log.info("Properties:");
    props.entrySet().forEach(e -> log.info("* {}", e));
    // Client
    val cfg = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    val http = new ZerodepDockerHttpClient.Builder()
        .dockerHost(URI.create("unix://" + Middleware.Constants.SOCKET))
        .build();
    val client = DockerClientImpl.getInstance(cfg, http);
    // Prune
    client.listImagesCmd().withDanglingFilter(Boolean.TRUE).exec().stream()
          .map(Image::getId).map(client::removeImageCmd)
          .map(RemoveImageCmd::exec)
          .forEach(v -> log.info("Dangling image removed."));
    // Swarm
    val swarm = client.infoCmd().exec().getSwarm();
    // TODO create distinct manager and workers through docker-based containers.
    if (null != swarm && swarm.getLocalNodeState() == LocalNodeState.ACTIVE) {
      client.leaveSwarmCmd().withForceEnabled(Boolean.TRUE).exec();
      log.info("Swarm left.");
    }
    client.initializeSwarmCmd(new SwarmSpec()).exec();
    log.info("Swarm init.");
    // Refresh resources
    if (Boolean.parseBoolean(props.get(Props.DEV_MODE))) {
      client.listVolumesCmd().exec().getVolumes().stream()
            .map(InspectVolumeResponse::getName)
            .filter(Middleware.Constants.FILTER)
            .forEach(s -> client.removeVolumeCmd(s).exec());
      log.info("Volumes pruned.");
      client.pruneCmd(PruneType.NETWORKS).exec();
      log.info("Networks pruned.");
    }
    // Services and resources
    Middleware.stream(props.get(Props.SERVICES))
              .map(m -> EnumSet.of(m, m.dependOn().toArray(Middleware[]::new)))
              .flatMap(Collection::stream)
              .distinct()
              .forEach(m -> {
                log.info("Service [{}]:", m);
                if (Middleware.Constants.FILTER.test(m.name())) {
                  client.createNetworkCmd()
                        .withName(m.name())
                        .withAttachable(Boolean.TRUE)
                        .withDriver("overlay").exec();
                  log.info("* Network created.");
                }
                client.createVolumeCmd().withName(m.name()).exec();
                log.info("* Volume created.");
                client.createServiceCmd(m.spec()).exec();
                log.info("* Service created.");
              });
    log.info("Docker auth status: [{}].", client.authCmd().exec().getStatus());
  }

  @AllArgsConstructor
  enum Props {
    /**
     * Indicates if the build is going to work in a development environment.
     * Defaults to {@code false}.
     */
    DEV_MODE("false"),
    /**
     * Indicates the pod's name. Defaults to project's root folder.
     */
    POD_NAME(Path.of("").toAbsolutePath().toFile().getName()),
    /**
     * All middlewares should be activated if any is not sent.
     */
    SERVICES(EnumSet.allOf(Middleware.class).stream().map(Enum::name)
                    .collect(Collectors.joining(","))),
    ;
    private static final Pattern SPLIT = Pattern.compile("[:=]");
    private final String val;

    /**
     * Serialize input entries according to the enumerated items.
     *
     * @param args Key-value entries written in {@code k=v} or {@code k:v} under
     *             {@link Props pre-defined keys}. Any string that does not
     *             follow that pattern is going to be discarded. Main purpose is
     *             to be used in test context.
     * @return Map of properties with the following values:
     *     <ul>
     *       <li>Corresponding captured value;</li>
     *       <li>Input from system/command-line; or</li>
     *       <li>Pre-defined values.</li>
     *     </ul>
     * @throws IllegalArgumentException if number of arguments is grater than
     *                                  the enum {@link #values()}.
     */
    static Map<Props, String> from(final String... args) {
      val values = values();
      if (args.length > values.length) {
        throw new IllegalArgumentException(
            "Arguments given amount is greater than the ones can be afforded!");
      }
      val m = new EnumMap<Props, String>(Props.class);
      for (val p : values) {
        m.put(p, System.getProperty(p.name(), p.val));
      }
      for (val ss : args) {
        val s = SPLIT.split(ss, -1);
        m.putIfAbsent(Props.valueOf(s[0]), s[1]);
      }
      return m;
    }
  }
}
