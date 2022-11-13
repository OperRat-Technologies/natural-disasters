package me.tcklpl.naturaldisaster.auth;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.player.cPlayer.CPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AuthCommands implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("register")) {
            if (!(sender instanceof Player p)) return true;
            if (args.length != 2) return false;

            if (NaturalDisaster.getAuthenticationManager().isAuthenticated(p)) {
                p.sendMessage(ChatColor.GRAY + "Você já está autenticado.");
                return true;
            }

            CPlayer cp = NaturalDisaster.getPlayerManager().getCPlayer(p.getUniqueId());
            if (cp.getPassword() != null) {
                p.sendMessage(ChatColor.RED + "Você já está registrado.");
                return true;
            }

            if (args[0].equals(args[1])) {
                HashingManager.HashData hashData = new HashingManager.HashData(p, HashingManager.HashingOption.HASH, args[0], null);
                if (NaturalDisaster.getAuthenticationManager().getHashingManager().isInQueue(hashData)) {
                    p.sendMessage(ChatColor.RED + "Você já está na fila de autenticação, por favor aguarde.");
                } else {
                    NaturalDisaster.getAuthenticationManager().getHashingManager().addToQueue(hashData);
                    p.sendMessage(ChatColor.GRAY + "Você foi adicionado na fila de autenticação. (Sua posição: " + (NaturalDisaster.getAuthManager().getHashingManager().getQueueSize() + 1) +")");
                }
            } else {
                p.sendMessage(ChatColor.RED + "As senhas não coincidem");
            }
            return true;
        } else {
            if (cmd.getName().equalsIgnoreCase("login")) {
                if (!(sender instanceof Player p)) return true;
                if (args.length != 1) return false;

                if (NaturalDisaster.getAuthenticationManager().isAuthenticated(p)) {
                    p.sendMessage(ChatColor.GRAY + "Você já está autenticado.");
                    return true;
                }

                CPlayer cp = NaturalDisaster.getPlayerManager().getCPlayer(p.getUniqueId());

                if (cp.getPassword() == null) {
                    p.sendMessage(ChatColor.RED + "Você precisa se registrar entes de se logar.");
                    return true;
                }

                HashingManager.HashData hashData = new HashingManager.HashData(p, HashingManager.HashingOption.COMPARE, args[0], cp.getPassword());
                if (NaturalDisaster.getAuthenticationManager().getHashingManager().isInQueue(hashData)) {
                    p.sendMessage(ChatColor.RED + "Você já está na fila de autenticação, por favor aguarde.");
                } else {
                    NaturalDisaster.getAuthenticationManager().getHashingManager().addToQueue(hashData);
                    p.sendMessage(ChatColor.GRAY + "Você foi adicionado na fila de autenticação.");
                }
                return true;
            } else {
                if (cmd.getName().equalsIgnoreCase("changepassword") || alias.equalsIgnoreCase("chgpwd")) {
                    if (args.length != 3 && args.length != 4) return false;
                    if (!(sender instanceof Player)) return false;
                    Player p = (Player) sender;

                }
            }
        }
        return false;
    }
}
