package nl.inferno.infernoCrate.commands;

import nl.inferno.infernoCrate.InfernoCrate;
import nl.inferno.infernoCrate.gui.CrateEditGUI;
import nl.inferno.infernoCrate.models.Crate;
import nl.inferno.infernoCrate.models.CrateType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CrateCommand implements CommandExecutor, TabCompleter {

    private final InfernoCrate plugin;

    public CrateCommand(InfernoCrate plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (!sender.hasPermission("infernocrate.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /crate create <id> <type>");
                    return true;
                }
                createCrate(sender, args[1], args[2]);
                break;

            case "give":
                if (!sender.hasPermission("infernocrate.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /crate give <player> <crateId> [amount]");
                    return true;
                }
                giveKey(sender, args[1], args[2], args.length > 3 ? args[3] : "1");
                break;

            case "list":
                if (!sender.hasPermission("infernocrate.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }
                listCrates(sender);
                break;

            case "delete":
                if (!sender.hasPermission("infernocrate.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /crate delete <crateId>");
                    return true;
                }
                deleteCrate(sender, args[1]);
                break;

            case "edit":
                if (!sender.hasPermission("infernocrate.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cThis command can only be used by players!");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /crate edit <crateId>");
                    return true;
                }

                Crate crateToEdit = plugin.getCrateManager().getCrate(args[1]);
                if (crateToEdit == null) {
                    sender.sendMessage("§cCrate not found!");
                    return true;
                }

                new CrateEditGUI(plugin, crateToEdit).openGUI((Player) sender);
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void createCrate(CommandSender sender, String id, String typeStr) {
        CrateType type;
        try {
            type = CrateType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid crate type! Available types: " +
                    Arrays.toString(CrateType.values()));
            return;
        }

        if (plugin.getCrateManager().getCrate(id) != null) {
            sender.sendMessage("§cA crate with this ID already exists!");
            return;
        }

        Crate crate = new Crate(id, type.getDisplayName() + " Crate", type);

        ItemStack key = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta meta = key.getItemMeta();
        meta.setDisplayName("§6" + type.getDisplayName() + " Crate Key");
        meta.setLore(Arrays.asList(
                "§7Use this key to open a",
                "§7" + type.getDisplayName() + " §7crate!",
                "",
                "§8ID: " + id
        ));
        key.setItemMeta(meta);
        crate.setKey(key);

        plugin.getCrateManager().getCrates().put(id, crate);
        plugin.getCrateManager().saveCrates();

        sender.sendMessage("§aCrate successfully created!");
    }

    private void giveKey(CommandSender sender, String playerName, String crateId, String amountStr) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return;
        }

        Crate crate = plugin.getCrateManager().getCrate(crateId);
        if (crate == null) {
            sender.sendMessage("§cCrate not found!");
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
            return;
        }

        ItemStack key = crate.getKey().clone();
        key.setAmount(amount);
        target.getInventory().addItem(key);

        sender.sendMessage("§aGave " + amount + " key(s) to " + target.getName());
        target.sendMessage("§aYou received " + amount + " " + crate.getType().getDisplayName() + " §acrate key(s)!");
    }

    private void listCrates(CommandSender sender) {
        if (plugin.getCrateManager().getCrates().isEmpty()) {
            sender.sendMessage("§cNo crates found!");
            return;
        }

        sender.sendMessage("§6§lAvailable Crates:");
        for (Crate crate : plugin.getCrateManager().getCrates().values()) {
            sender.sendMessage("§7- §f" + crate.getId() + " §7(" + crate.getType().getDisplayName() + "§7)");
        }
    }

    private void deleteCrate(CommandSender sender, String id) {
        Crate crate = plugin.getCrateManager().getCrate(id);
        if (crate == null) {
            sender.sendMessage("§cCrate not found!");
            return;
        }

        if (!crate.getHolograms().isEmpty()) {
            crate.getHolograms().forEach(hologram -> hologram.remove());
        }

        plugin.getCrateManager().getCrates().remove(id);
        plugin.getCrateManager().saveCrates();

        sender.sendMessage("§aCrate successfully deleted!");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§lInfernoCrate Commands:");
        if (sender.hasPermission("infernocrate.admin")) {
            sender.sendMessage("§6/crate create <id> <type> §7- Create a new crate");
            sender.sendMessage("§6/crate give <player> <crateId> [amount] §7- Give crate keys");
            sender.sendMessage("§6/crate list §7- List all crates");
            sender.sendMessage("§6/crate delete <crateId> §7- Delete a crate");
            sender.sendMessage("§6/crate edit <crateId> §7- Edit a crate");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("infernocrate.admin")) {
            return completions;
        }

        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "give", "list", "delete", "edit"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "give":
                    completions.addAll(Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList()));
                    break;
                case "delete":
                case "edit":
                    completions.addAll(plugin.getCrateManager().getCrates().keySet());
                    break;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("create")) {
                completions.addAll(Arrays.stream(CrateType.values())
                        .map(Enum::name)
                        .collect(Collectors.toList()));
            } else if (args[0].equalsIgnoreCase("give")) {
                completions.addAll(plugin.getCrateManager().getCrates().keySet());
            }
        }

        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
