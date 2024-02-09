# ConfigAPI
Config API for Bukkit 1.8 - 1.20 based on dynamic proxies

## Features:
 - Works with Bukkit 1.8 - 1.20
 - Compatible with Java 8 - 17
 - Multiple configuration files
 - Fastly create configs via Java interface with `default` getters
 - Automatic generation of config's YAML file
 - Automatic update of config's file after add new methods to it's interface
 - Support for setting new values to config
 - System of serializers for custom objects (e.g. ItemStack, Location)
 - Automatic serialization of custom objects based on reflections (mainly for simple DAO/DTO objects)
 - Support of comments in YAML config
 - Automatic translation of `&` based colors

## Import
#### Gradle
```groovy
maven {
    url = 'https://repo.mikigal.pl/releases'
}

compile group: 'pl.mikigal', name: 'ConfigAPI', version: '1.2.5'
```

#### Maven
```xml
<repository>
    <id>mikigal-repo</id>
    <url>https://repo.mikigal.pl/releases</url>
</repository>

<dependency>
    <groupId>pl.mikigal</groupId>
    <artifactId>ConfigAPI</artifactId>
    <version>1.2.5</version>
    <scope>compile</scope>
</dependency>
```

## How to use?
#### Java code
```java
public class TestPlugin extends JavaPlugin {

    private static TestConfig testConfig;

    @Override
    public void onEnable() {
        testConfig = ConfigAPI.init(
            TestConfig.class, // Class of config's interface
            NameStyle.UNDERSCORE, // Style of fields' name in YAML file
            CommentStyle.INLINE, // Style of comments in YAML file
            true, // Automatic translation of '&' based colors
            this // Instance of plugin
        );

        // You can simply access data from the config by getters
        System.out.println(testConfig.getExampleMessage());
        Bukkit.getPlayer("mikigal").getInventory().addItem(testConfig.getAward());

        // After calling setter new data are automatically saved to file
        testConfig.setAward(new ItemStack(Material.DIRT));

        // If you want to do something manually you can access instance of YamlConfiguration
        testConfig.getBukkitConfiguration();
    }

    public static TestConfig getTestConfig() {
        return testConfig;
    }
}

@ConfigName("test.yml") // Name of YAML file
public interface TestConfig extends Config {

    @Comment("This comment will be saved to YAML file!")
    default String getExampleMessage() {
        // Getter method should return default value of the field
        return "&cIt's default value of example message";
    }

    default ItemStack getAward() {
        return Item.of(Material.DIAMOND_SWORD)
            .name("&cAward")
            .lore("&aFirst line", "&cSecond line")
            .toItem();
    }

    public void setAward(ItemStack award);

    // Key of Map must be String
    default Map<String, Integer> getValues() {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);

        return map;
    }

    default List<Location> getSpawnPoints() {
        World world = Bukkit.getWorld("world");
        return Arrays.asList(
            new Location(world, 0, 100, 0),
            new Location(world, 10, 90, 10, 90f, 0f),
            new Location(world, 20, 80, 20, 0f, 180f)
        );
    }
}
```

#### Automatic generated YAML from above Java code
```yaml
example_message: '&cIt''s default value of example message' # This comment will be saved to YAML file!
spawn_points:
  structure: java.util.ArrayList
  type: org.bukkit.Location
  '0':
    world: world
    x: 0.0
    y: 100.0
    z: 0.0
    yaw: 0.0
    pitch: 0.0
  '1':
    world: world
    x: 10.0
    y: 90.0
    z: 10.0
    yaw: 90.0
    pitch: 0.0
  '2':
    world: world
    x: 20.0
    y: 80.0
    z: 20.0
    yaw: 0.0
    pitch: 180.0
values:
  structure: java.util.HashMap
  type: java.lang.Integer
  a: 1
  b: 2
award:
  material: DIAMOND_SWORD
  amount: 1
  name: '&cAward'
  lore:
    structure: java.util.ArrayList
    type: java.lang.String
    '0': '&aFirst line'
    '1': '&cSecond line'
```

## Serializers
### API has built-in serializers for:
 - ItemStack
 - Location
 - PotionEffect
 - ShapedRecipe
 - UUID

#### Automatic serializing of custom objects
If you have some simple DAO/DTO object you can serialize it without writing custom serializer!

```java
public class User implements Serializable { // It must implement Serializable interface
    private String username;
    private int kills;
    private transient String temporary; // Transient fields will not be serialized!

    public User() { // It MUST have no-args constructor!

    }

    public User(String username, int kills, String temporary) {
        this.username = username;
        this.kills = kills;
        this.temporary = temporary;
    }

    // Getters and setters...
}

@ConfigName("config")
public interface MyConfig extends Config {

    void setUser(User user);

    default User getTest() {
        return new User("mikigal", 1, "some text");
    }
}
```

#### You can also make your own serializers
For more advanced objects you can make your own serializer
```java
public class PotionEffectSerializer extends Serializer<PotionEffect> {

    @Override
    protected void saveObject(String path, PotionEffect object, BukkitConfiguration configuration) {
        // In saveObject() method you have to set data of object to config. You can use set() method to set another object which need serialization too
        configuration.set(path + ".type", object.getType().getName());
        configuration.set(path + ".duration", object.getDuration());
        configuration.set(path + ".amplifier", object.getAmplifier());
    }

    @Override
    public PotionEffect deserialize(String path, BukkitConfiguration configuration) {
        // In deserialize() method you have to load data from config and return instance of object
        PotionEffectType type = PotionEffectType.getByName(configuration.getString(path + ".type"));
        int duration = configuration.getInt(path + ".duration");
        int amplifier = configuration.getInt(path + ".amplifier");

        if (type == null) {
            throw new InvalidConfigFileException("Invalid PotionEffect type (path: " + path + ")");
        }

        return new PotionEffect(type, duration, amplifier);
    }
}

public class TestPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Remember to register you Serializer before use!
        ConfigAPI.registerSerializer(PotionEffect.class, new PotionEffectSerializer());

        // Init your configs...

    }
}

