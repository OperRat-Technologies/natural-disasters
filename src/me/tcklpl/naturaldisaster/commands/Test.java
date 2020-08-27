package me.tcklpl.naturaldisaster.commands;

import me.tcklpl.naturaldisaster.reflection.Packets;
import me.tcklpl.naturaldisaster.reflection.ReflectionUtils;
import me.tcklpl.naturaldisaster.reflection.ReflectionWorldUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Test implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;
            if (command.getName().equalsIgnoreCase("test")) {
                List<Block> blocks = new ArrayList<>();
                blocks.add(p.getLocation().getBlock());
                try {
                    ReflectionWorldUtils.setBlocksInSameChunk(blocks, Material.LIME_CONCRETE, false);
                    ReflectionUtils.sendPacket(p, Packets.Play.PlayOutMapChunk(blocks.get(0).getChunk()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        return false;
    }
}
