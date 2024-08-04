package io.github.nahkd123.bukkitcommandkit.demo;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import io.github.nahkd123.bukkitcommandkit.ArgConstraint;
import io.github.nahkd123.bukkitcommandkit.ArgumentProcessor;
import io.github.nahkd123.bukkitcommandkit.Command;
import io.github.nahkd123.bukkitcommandkit.Hook;
import io.github.nahkd123.bukkitcommandkit.HookType;
import io.github.nahkd123.bukkitcommandkit.Sender;
import io.github.nahkd123.bukkitcommandkit.Subcommand;

@Command(permission = "bukkitcommandkit.mycommand")
public class MyCommand {
    public MyCommand(Plugin plugin) {}

    @Hook(HookType.NO_PERMISSION)
    public void onNoPermission(CommandSender sender, String permission) {
        sender.sendMessage("\u00a7cNo permission: " + permission);
    }

    @Hook(HookType.UNKNOWN_ARGUMENT)
    public void onUnknownArgument(CommandSender sender, String[] args, int errorAt) {
        StringBuilder builder = new StringBuilder("\u00a7cError while parsing command:\u00a7r");
        for (int i = 0; i < errorAt; i++) builder.append(' ').append(args[i]);
        builder.append(" \u00a7c").append(args[errorAt]).append("\u00a7r");
        for (int i = errorAt + 1; i < args.length; i++) builder.append(' ').append(args[i]);
        sender.sendMessage(builder.toString());
    }

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

    // Generated command tree & ordering:
    // - /mycommand hello (run as player)
    // - /mycommand hello (all)
    // - /mycommand get <player> item <type> <amount>
    // - /mycommand get <player> item <type>
    // - /mycommand get <player> meow
    // Using your command: new MyCommandGenerated(myPlugin);

    @Subcommand(
        value = "message <player> <message>",
        permission = "bukkitcommandkit.mycommand.custommessage")
    public void customMessage(Player player, @ArgumentProcessor(parser = "stringParser",
        tabCompleter = "stringTabCompleter") String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public String stringParser(String[] args, AtomicInteger index) {
        return args[index.getAndIncrement()];
    }

    public void stringTabCompleter(String[] args, AtomicInteger index, Consumer<String> suggest) {
        suggest.accept("Hello");
        suggest.accept("Hi");
        suggest.accept("Bye");
        suggest.accept("Goodbye");
        suggest.accept("こんにちは");
        index.incrementAndGet();
    }
}
