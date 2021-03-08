package com.jinyframework.keva.server.config;

import com.jinyframework.keva.server.util.ArgsHolder;
import lombok.*;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Properties;

@Builder(toBuilder = true)
@Getter
@Setter
public class ConfigHolder {
    @ConfigProp(name = "heartbeat_enabled", defaultVal = "true")
    @CliProp(name = "hb", type = CliPropType.FLAG)
    private Boolean heartbeatEnabled;

    @ConfigProp(name = "snapshot_enabled", defaultVal = "true")
    @CliProp(name = "ss", type = CliPropType.FLAG)
    private Boolean snapshotEnabled;

    @ConfigProp(name = "hostname", defaultVal = "localhost")
    @CliProp(name = "h", type = CliPropType.VAL)
    private String hostname;

    @ConfigProp(name = "port", defaultVal = "6767")
    @CliProp(name = "p", type = CliPropType.VAL)
    private Integer port;

    @ConfigProp(name = "heartbeat_timeout", defaultVal = "120000")
    @CliProp(name = "ht", type = CliPropType.VAL)
    private Long heartbeatTimeout;

    @ConfigProp(name = "snapshot_location", defaultVal = "")
    @CliProp(name = "sl", type = CliPropType.VAL)
    private String snapshotLocation;

    @ConfigProp(name = "heap_size", defaultVal = "64")
    @CliProp(name = "hs", type = CliPropType.VAL)
    private Integer heapSize;

    public static ConfigHolder fromProperties(@NonNull Properties props) throws Exception {
        val configHolder = builder().build();
        val fields = ConfigHolder.class.getDeclaredFields();
        for (val field : fields) {
            if (field.isAnnotationPresent(ConfigProp.class)) {
                val annotation = field.getAnnotation(ConfigProp.class);
                val value = parse(props.getProperty(annotation.name(), annotation.defaultVal()), field.getType());
                field.set(configHolder, value);
            }
        }

        return configHolder;
    }

    public static ConfigHolder fromArgs(@NonNull ArgsHolder args) throws Exception {
        final ConfigHolder configHolder = builder().build();

        final Field[] fields = ConfigHolder.class.getDeclaredFields();
        for (val field : fields) {
            if (field.isAnnotationPresent(CliProp.class)) {
                val cliAnnotate = field.getAnnotation(CliProp.class);
                String strVal = null;
                switch (cliAnnotate.type()) {
                    case VAL:
                        strVal = args.getArgVal(cliAnnotate.name());
                        break;
                    case FLAG:
                        strVal = args.getFlag(cliAnnotate.name());
                        break;
                }
                if (strVal != null) {
                    val value = parse(strVal, field.getType());
                    field.set(configHolder, value);
                }
            }
        }

        return configHolder;
    }

    private static <T> T parse(String s, Class<T> clazz) throws Exception {
        return clazz.getConstructor(new Class[]{String.class}).newInstance(s);
    }

    public void merge(ConfigHolder overrideHolder) throws Exception {
        if (overrideHolder != null) {
            for (final Field field : overrideHolder.getClass().getDeclaredFields()) {
                final Object overrideVal = field.get(overrideHolder);
                if (overrideVal != null) {
                    field.set(this, overrideVal);
                }
            }
        }
    }
    public static ConfigHolder makeDefaultConfig() {
        return builder()
                .snapshotLocation("")
                .hostname("localhost")
                .port(6767)
                .heapSize(64)
                .heartbeatEnabled(true)
                .heartbeatTimeout(120000L)
                .snapshotEnabled(true)
                .build();
    }

    @Override
    public String toString() {
        return "ConfigHolder{" +
                "heartbeatEnabled=" + heartbeatEnabled +
                ", snapshotEnabled=" + snapshotEnabled +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                ", heartbeatTimeout=" + heartbeatTimeout +
                ", snapshotLocation='" + snapshotLocation + '\'' +
                ", heapSize=" + heapSize +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ConfigHolder that = (ConfigHolder) o;
        return Objects.equals(heartbeatEnabled, that.heartbeatEnabled) &&
                Objects.equals(snapshotEnabled, that.snapshotEnabled) &&
                Objects.equals(hostname, that.hostname) &&
                Objects.equals(port, that.port) &&
                Objects.equals(heartbeatTimeout, that.heartbeatTimeout) &&
                Objects.equals(snapshotLocation, that.snapshotLocation) &&
                Objects.equals(heapSize, that.heapSize);
    }
}
