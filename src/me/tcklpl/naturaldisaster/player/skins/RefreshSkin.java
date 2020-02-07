package me.tcklpl.naturaldisaster.player.skins;

import me.tcklpl.naturaldisaster.util.SkinUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

public class RefreshSkin implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("refresh")) {
            if (args.length == 0) return false;
            if (!(sender instanceof Player)) return false;
            Player p = (Player) sender;
            if (args[0].equalsIgnoreCase("skin")) {

                String uuid = SkinUtils.getOriginalUUIDString(p.getName());
                if (uuid == null) {
                    p.sendMessage(ChatColor.RED + "Não tem razão para você pedir um refresh de skin se sua conta é pirata caralho");
                    return true;
                }


                if (args.length == 2) {
                    if (args[1].equalsIgnoreCase("-f") || args[1].equalsIgnoreCase("--force")) {
                        if (p.isOp()) {
                            SkinManager.getInstance().addPlayerToSkinQueue(p, uuid);
                        } else {
                            p.sendMessage(ChatColor.RED + "Você não tem permissão para forçar um refresh de skin.");
                        }
                        return true;
                    }
                } else if (args.length == 1) {
                    Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
                    CustomSkin currentSkin = SkinManager.getInstance().getSkin(p.getName());
                    if (currentSkin != null) {
                        long diffInMil = Math.abs(currentTimestamp.getTime() - currentSkin.getTimestamp().getTime());
                        long days = TimeUnit.DAYS.convert(diffInMil, TimeUnit.MILLISECONDS);
                        if (days >= 3) {
                            SkinManager.getInstance().addPlayerToSkinQueue(p, uuid);
                        } else p.sendMessage(ChatColor.RED + "Você deve esperar ao menos 3 dias desde seu último refresh de skin");
                    } else p.sendMessage(ChatColor.RED + "Você precisa ter uma skin aplicada antes de dar refresh, aguarde até sua skin ser aplicada");
                    return true;
                }
            }
        }
        return false;
    }
}
