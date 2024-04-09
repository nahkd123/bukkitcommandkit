# nahkd's BukkitCommandKit
Annotation processor for generating Bukkit commands.

This was made with fast prototyping in mind. However, this does not includes a full command system like most command frameworks/libraries, so you might have a better chance at writing your own command parsing system if you are making a big plugin.

Please note that annotation processing is much different than runtime reflection; annotation processing only do processing during compile time and generates new classes, while reflection can be quite slow. **You shouldn't shade BukkitCommandKit**, because there is no need to do that ;) (although it is just 35KiB in size).

## See BukkitCommandKit in action!
```java
public class MyPlugin extends JavaPlugin {
	@Override
	public void onEnable() {
		getCommand("mycommand").setExecutor(new MyCommandGenerated(new MyCommand()));
		// Try these commands:
		// /mycommand hello (run as console and player)
		// /mycommand get <player name> item DIAMOND 32
		// /mycommand get <player name> meow
	}
}
```

```java
@Command(permission = "bukkitcommandkit.mycommand")
public class MyCommand {
	@Subcommand("hello")
	public void hello(@Sender CommandSender sender) {
		sender.sendMessage("Hi!");
	}

	@Subcommand("hello")
	public void hello(@Sender Player player) {
		player.sendMessage("Hi " + player.getDisplayName() + "!");
	}

	@Subcommand("get <player> item <type> <amount>")
	public void getItem(Player player, Material type, @ArgConstraint(min = 1, max = 64) int amount) {
		ItemStack stack = new ItemStack(type, amount);
		player.getInventory().addItem(stack);
	}

	@Subcommand(
		value = "get <player> item <type>",
		sampleUsages = {
			"get nahkd123 item DIAMOND",
			"get @s item STONE"
		})
	public void getItem(Player player, Material type) {
		getItem(player, type, 1);
	}

	@Subcommand(
		value = "get <player> meow",
		permission = "bukkitcommandkit.mycommand.meow")
	public void getMeow(Player player) {
		player.playSound(player, Sound.ENTITY_CAT_AMBIENT, 1f, 1f);
		player.sendMessage("Meow!");
	}
}
```

> **Note:** `MyCommandGenerated` is a generated class; you can try Ctrl-clicking it to see the generated code!

## Use BukkitCommandKit
### Maven
```xml
<!-- Include Jitpack if you haven't done this -->
<repository>
    <id>jitpack</id>
    <url>https://jitpack.io/</url>
</repository>
```

```xml
<dependency>
    <groupId>com.github.nahkd123.bukkitcommandkit</groupId>
    <artifactId>bukkitcommandkit-annotations</artifactId>
    <version>main-SNAPSHOT</version>
</dependency>
```

### Gradle
```groovy
repositories {
    maven { url 'https://jitpack.io/' }
}

dependencies {
    implementation annotationProcessor('io.github.nahkd123.bukkitcommandkit:bukkitcommandkit-annotations:main-SNAPSHOT')
}
```

## License
MIT License. I'm not mad if you forked this project (in fact, you should fork and improve it!).
